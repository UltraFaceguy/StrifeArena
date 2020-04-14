package land.face.arena.data;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TitleUtils;
import java.util.ArrayList;
import land.face.arena.StrifeArenaPlugin;
import land.face.arena.tasks.ArenaKickRunner;
import land.face.arena.tasks.WaveRunner;
import land.face.arena.tasks.WaveStartRunner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class ArenaInstance {

  private final Arena arena;
  private final String instanceId;
  private final Location location;
  private final Player player;
  private int wave = 0;
  private long startTime;

  private WaveRunner waveRunner;
  private WaveStartRunner waveStartRunner;
  private ArenaKickRunner arenaKickRunner;

  public ArenaInstance(Arena arena, Player player, String instanceId) {
    this.arena = arena;
    this.player = player;
    this.instanceId = instanceId;
    this.location = arena.getInstances().get(instanceId).asLocation();
    this.startTime = System.currentTimeMillis();
  }

  public void beginNextWave() {
    if (waveRunner != null && !waveRunner.isCancelled()) {
      Bukkit.getLogger().warning("Tried to begin a wave when there's already one running!");
      return;
    }
    if (waveStartRunner != null && !waveStartRunner.isCancelled()) {
      waveStartRunner.cancel();
      waveStartRunner = null;
    }
    waveRunner = new WaveRunner(this, arena.getWaves().get(wave), location);
    waveRunner.runTaskTimer(StrifeArenaPlugin.getInstance(), 1L, 20L);
    wave++;
    TitleUtils.sendTitle(player, TextUtils.color("&6Prepare For Battle!"),
        TextUtils.color("&eWave &f" + wave + " &ehas begun!"));
    player.getWorld().playSound(location, Sound.EVENT_RAID_HORN, 1, 1);
    Location loc = arena.getInstances().get(instanceId).asLocation();
    loc.getBlock().setType(Material.AIR);
  }

  public void doWaveEnd(Player player) {

    ArenaWave completedWave = arena.getWaves().get(wave - 1);

    if (completedWave.getLootRewards() == null) {
      completedWave.setLootRewards(new ArrayList<>());
    }
    for (LootReward reward : completedWave.getLootRewards()) {
      if (Math.random() > reward.getProbability()) {
        continue;
      }
      StrifeArenaPlugin.getInstance().getLootManager().addItem(player, LootReward
          .toItemStack(reward, arena.getArenaLevel() + wave * arena.getArenaLevelPerWave()));
    }

    StrifeArenaPlugin.getInstance().getLootManager().addExp(player, arena.getMinExpPerWave(),
        arena.getMaxExpPerWave(), arena.getExpExponent(), completedWave.getExpBonus());
    StrifeArenaPlugin.getInstance().getLootManager().addCash(player, arena.getMinMoneyPerWave(),
        arena.getMaxMoneyPerWave(), arena.getMoneyExponent(), completedWave.getMoneyBonus());

    if (wave >= arena.getWaves().size()) {
      doArenaEnd(player);
      return;
    }

    bumpRecord(player, wave, -1);

    Location loc = arena.getInstances().get(instanceId).asLocation();
    loc.getBlock().setType(Material.CHEST);
    loc.getBlock()
        .setMetadata("ARENA_CHEST", new FixedMetadataValue(StrifeArenaPlugin.getInstance(), true));
    TitleUtils.sendTitle(player, TextUtils.color("&cWAVE VANQUISHED!"),
        TextUtils.color("&eCompleted Wave &f" + wave + "&e!"));
    waveStartRunner = null;
    waveStartRunner = new WaveStartRunner(this);
    waveStartRunner.runTaskTimer(StrifeArenaPlugin.getInstance(), 100L, 20L);
  }

  public void doArenaEnd(Player player) {
    if (waveStartRunner != null && !waveStartRunner.isCancelled()) {
      waveStartRunner.cancel();
      waveStartRunner = null;
    }

    long time = wave == arena.getWaves().size() ? System.currentTimeMillis() - startTime : -1;
    bumpRecord(player, wave, time);

    location.getBlock().setType(Material.ENDER_CHEST);
    StrifeArenaPlugin.getInstance().getLootManager().explodeLoot(player, location);
    if (arena.getWaves().size() != wave) {
      TitleUtils.sendTitle(player, TextUtils.color("&2ARENA ENDED!"),
          TextUtils.color("&aCompleted &f" + wave + " &awaves!"));
      player.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 1, 0.8F);
    } else {
      TitleUtils.sendTitle(player, TextUtils.color("&2ARENA COMPLETE!"),
          TextUtils.color("&aCompleted all &f" + wave + " &awaves!"));
      player.playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1.5F);
    }
    arenaKickRunner = null;
    arenaKickRunner = new ArenaKickRunner(this);
    arenaKickRunner.runTaskTimer(StrifeArenaPlugin.getInstance(), 100L, 20L);
  }

  public void cancelTimers() {
    if (waveRunner != null && !waveRunner.isCancelled()) {
      waveRunner.clearSummons();
      waveRunner.cancel();
    }
    if (waveStartRunner != null && !waveStartRunner.isCancelled()) {
      waveStartRunner.cancel();
    }
    if (arenaKickRunner != null && !arenaKickRunner.isCancelled()) {
      arenaKickRunner.cancel();
    }
  }

  public Arena getArena() {
    return arena;
  }

  public String getInstanceId() {
    return instanceId;
  }

  public Player getPlayer() {
    return player;
  }

  private void bumpRecord(Player player, int wave, long time) {
    Record record = arena.getRecords().get(player.getUniqueId());
    if (record == null) {
      record = new Record();
      record.setShortestTime(-1);
      arena.getRecords().put(player.getUniqueId(), record);
    }
    Record.bumpRecord(player, record, wave, time);
  }
}
