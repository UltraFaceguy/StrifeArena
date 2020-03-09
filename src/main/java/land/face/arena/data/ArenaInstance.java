package land.face.arena.data;

import com.tealcube.minecraft.bukkit.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TitleUtils;
import java.util.ArrayList;
import java.util.HashMap;
import land.face.arena.StrifeArenaPlugin;
import land.face.arena.tasks.ArenaKickRunner;
import land.face.arena.tasks.WaveRunner;
import land.face.arena.tasks.WaveStartRunner;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class ArenaInstance {

  private final Arena arena;
  private final String instanceId;
  private final Player player;
  private int wave = 0;
  private long startTime = System.currentTimeMillis();

  private WaveRunner runner;
  private WaveStartRunner waveDelay;

  public ArenaInstance(Arena arena, Player player, String instanceId) {
    this.arena = arena;
    this.player = player;
    this.instanceId = instanceId;
  }

  public void beginNextWave() {
    if (runner != null && !runner.isCancelled()) {
      Bukkit.getLogger().warning("Tried to begin a wave when there's already one running!");
      return;
    }
    if (waveDelay != null && !waveDelay.isCancelled()) {
      waveDelay.cancel();
      waveDelay = null;
    }
    runner = new WaveRunner(this, arena.getWaves().get(wave), arena.getInstances().get(instanceId));
    runner.runTaskTimer(StrifeArenaPlugin.getInstance(), 1L, 20L);
    wave++;
    TitleUtils.sendTitle(player, TextUtils.color("&6Prepare For Battle!"),
        TextUtils.color("&eWave &f" + wave + " &ehas begun!"));
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
    bumpRecord(-1);
    Location loc = arena.getInstances().get(instanceId).asLocation();
    loc.getBlock().setType(Material.CHEST);
    loc.getBlock()
        .setMetadata("ARENA_CHEST", new FixedMetadataValue(StrifeArenaPlugin.getInstance(), true));
    TitleUtils.sendTitle(player, TextUtils.color("&cWAVE VANQUISHED!"),
        TextUtils.color("&eCompleted Wave &f" + wave + "&e!"));
    waveDelay = null;
    waveDelay = new WaveStartRunner(this);
    waveDelay.runTaskTimer(StrifeArenaPlugin.getInstance(), 100L, 20L);
  }

  public void doArenaEnd(Player player) {
    if (waveDelay != null && !waveDelay.isCancelled()) {
      waveDelay.cancel();
      waveDelay = null;
    }
    bumpRecord(System.currentTimeMillis() - startTime);
    Location loc = arena.getInstances().get(instanceId).asLocation();
    loc.getBlock().setType(Material.ENDER_CHEST);
    StrifeArenaPlugin.getInstance().getLootManager().explodeLoot(player, loc);
    TitleUtils.sendTitle(player, TextUtils.color("&2ARENA COMPLETE!"),
        TextUtils.color("&aCompleted all &f" + wave + " &awaves!"));
    runner = null;
    new ArenaKickRunner(this).runTaskTimer(StrifeArenaPlugin.getInstance(), 100L, 20L);
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

  private void bumpRecord(long time) {
    if (arena.getRecords() == null) {
      arena.setRecords(new HashMap<>());
    }
    Record record = arena.getRecords().get(player.getUniqueId());
    if (record == null) {
      record = new Record();
      arena.getRecords().put(player.getUniqueId(), record);
    }
    Record.bumpRecord(record, wave, time);
  }
}
