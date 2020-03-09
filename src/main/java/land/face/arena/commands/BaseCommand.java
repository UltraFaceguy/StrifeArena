package land.face.arena.commands;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;
import land.face.arena.StrifeArenaPlugin;
import land.face.arena.data.Arena;
import land.face.arena.data.ArenaSpawn;
import land.face.arena.data.ArenaWave;
import land.face.arena.data.BasicLocation;
import land.face.arena.data.LootReward;
import land.face.arena.data.LootReward.RewardType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import se.ranzdo.bukkit.methodcommand.Arg;
import se.ranzdo.bukkit.methodcommand.Command;

public class BaseCommand {

  private StrifeArenaPlugin plugin;

  public BaseCommand(StrifeArenaPlugin plugin) {
    this.plugin = plugin;
  }

  @Command(identifier = "arena start", permissions = "arenas.start", onlyPlayers = false)
  public void startCommand(CommandSender sender, @Arg(name = "player") Player player,
      @Arg(name = "id") String id) {
    if (plugin.getArenaManager().getArena(id) == null) {
      sendMessage(sender, "&eArena " + id + " does not exist!");
      return;
    }
    plugin.getArenaManager().joinArena(player, id);
  }

  @Command(identifier = "arena create", permissions = "arenas.create", onlyPlayers = false)
  public void reloadCommand(CommandSender sender, @Arg(name = "id") String id) {
    if (plugin.getArenaManager().getArena(id) != null) {
      sendMessage(sender, "&eArena " + id + " already exists!");
      return;
    }

    Arena arena = new Arena(id);
    arena.setArenaLevel(1);
    arena.setArenaLevelPerWave(0);
    arena.setMinExpPerWave(0);
    arena.setMaxExpPerWave(0);
    arena.setExpExponent(0);
    arena.setMinMoneyPerWave(0);
    arena.setMaxMoneyPerWave(0);
    arena.setMoneyExponent(1);

    plugin.getArenaManager().addArena(arena);
    sendMessage(sender, "&aAdded arena " + id + "! Use /arena addinstance!");
  }

  @Command(identifier = "arena setlevel", permissions = "arenas.create", onlyPlayers = false)
  public void setLevelCommand(CommandSender sender, @Arg(name = "id") String id,
      @Arg(name = "level") int level, @Arg(name = "levelPerWave") double levelPerWave) {
    if (plugin.getArenaManager().getArena(id) == null) {
      sendMessage(sender, "&eArena " + id + " doesn't exist!");
      return;
    }

    Arena arena = plugin.getArenaManager().getArena(id);
    arena.setArenaLevel(level);
    arena.setArenaLevelPerWave(levelPerWave);
    sendMessage(sender, "&aset");
  }

  @Command(identifier = "arena setexit", permissions = "arenas.edit")
  public void addExist(Player sender, @Arg(name = "id") String id) {
    if (plugin.getArenaManager().getArena(id) == null) {
      sendMessage(sender, "&eArena " + id + " does not exist!");
      return;
    }
    plugin.getArenaManager().getArena(id).setExitLocation(BasicLocation.fromLocation(sender.getLocation()));
    sendMessage(sender, "&aexit location updated");
  }

  @Command(identifier = "arena addinstance", permissions = "arenas.instances")
  public void menuCommand(Player sender, @Arg(name = "arenaId") String arenaId,
      @Arg(name = "instanceId") String instanceId) {
    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      sendMessage(sender, "&eNo arena named " + arenaId + " found!");
      return;
    }
    if (arena.getInstances() == null) {
      arena.setInstances(new HashMap<>());
    }
    if (arena.getInstances().get(instanceId) != null) {
      sendMessage(sender, "&eInstance with " + instanceId + " already exists!");
      return;
    }
    arena.getInstances().put(instanceId, BasicLocation.fromLocation(sender.getLocation()));
    sendMessage(sender, "&aAdded new arena location!");
  }

  @Command(identifier = "arena records", permissions = "arenas.reconds")
  public void recordsCommand(Player sender, @Arg(name = "arenaId") String arenaId) {
    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      sendMessage(sender, "&eNo arena named " + arenaId + " found!");
      return;
    }
    sendMessage(sender, "Records for " + arenaId);
    for (UUID uuid : arena.getRecords().keySet()) {
      sendMessage(sender, uuid + " - " + arena.getRecords().get(uuid).getHighestWave());
    }
  }

  @Command(identifier = "arena instances", permissions = "arenas.instances")
  public void menuCommand(Player sender, @Arg(name = "arenaId") String arenaId) {
    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      sendMessage(sender, "&eNo arena named " + arenaId + " found!");
      return;
    }
    sendMessage(sender, "Arena instance list for " + arenaId);
    for (String s : arena.getInstances().keySet()) {
      sendMessage(sender, " - " + s);
    }
  }

  @Command(identifier = "arena addwave", permissions = "arenas.wave")
  public void addWaveCommand(Player sender, @Arg(name = "arenaId") String arenaId) {
    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      sendMessage(sender, "&eNo arena named " + arenaId + " found!");
      return;
    }
    if (arena.getWaves() == null) {
      arena.setWaves(new ArrayList<>());
    }
    ArenaWave wave = new ArenaWave();
    wave.setArenaTask(new HashMap<>());
    wave.setLootRewards(new ArrayList<>());
    wave.setExpBonus(0);
    wave.setMoneyBonus(0);

    arena.getWaves().add(wave);

    sendMessage(sender, "Added wave. New Size: " + arena.getWaves().size());
  }

  @Command(identifier = "arena setRewards", permissions = "arenas.wave")
  public void setRewardsCommand(Player sender, @Arg(name = "arenaId") String arenaId,
      @Arg(name = "minExpPerWave") double minXp, @Arg(name = "maxXpPerWave") double maxXp,
      @Arg(name = "expExponent") double expExp, @Arg(name = "minMoneyPerWave") double minMoney,
      @Arg(name = "maxMoneyPerWave") double maxMoney, @Arg(name = "moneyExponent") double moneyExp) {
    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      sendMessage(sender, "&eNo arena named " + arenaId + " found!");
      return;
    }
    arena.setMinMoneyPerWave(minMoney);
    arena.setMaxMoneyPerWave(maxMoney);
    arena.setMoneyExponent(moneyExp);
    arena.setMinExpPerWave(minXp);
    arena.setMaxExpPerWave(maxXp);
    arena.setExpExponent(expExp);
    sendMessage(sender, "Set per wave rewards");
  }

  @Command(identifier = "arena addItemReward", permissions = "arenas.wave")
  public void setRewardsCommand(Player sender, @Arg(name = "arenaId") String arenaId,
      @Arg(name = "waveNumber") int wave, @Arg(name = "rewardType") String type,
      @Arg(name = "amount") int amount, @Arg(name = "chance") double chance,
      @Arg(name = "extraDataOne") String data1, @Arg(name = "extraDataTwo") String data2) {

    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      sendMessage(sender, "&eNo arena named " + arenaId + " found!");
      return;
    }

    if (arena.getWaves().get(wave - 1) == null) {
      sendMessage(sender, "&eThis arena has no wave " + wave);
      return;
    }

    LootReward lootReward = new LootReward();

    lootReward.setType(RewardType.valueOf(type.toUpperCase()));
    lootReward.setAmount(amount);
    lootReward.setProbability(chance);
    lootReward.setDataString(data1);
    lootReward.setDataStringTwo(data2);

    if (arena.getWaves().get(wave - 1).getLootRewards() == null) {
      arena.getWaves().get(wave - 1).setLootRewards(new ArrayList<>());
    }

    arena.getWaves().get(wave - 1).getLootRewards().add(lootReward);

    sendMessage(sender, "Added reward to wave " + wave);
  }

  @Command(identifier = "arena listItemReward", permissions = "arenas.wave")
  public void setRewardsCommand(Player sender, @Arg(name = "arenaId") String arenaId,
      @Arg(name = "waveNumber") int wave) {

    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      sendMessage(sender, "&eNo arena named " + arenaId + " found!");
      return;
    }

    if (arena.getWaves().get(wave - 1) == null) {
      sendMessage(sender, "&eThis arena has no wave " + wave);
      return;
    }

    if (arena.getWaves().get(wave - 1).getLootRewards() == null) {
      arena.getWaves().get(wave - 1).setLootRewards(new ArrayList<>());
    }

    sendMessage(sender, "LootReward List for wave " + wave);
    int index = 0;
    for (LootReward lootReward : arena.getWaves().get(wave - 1).getLootRewards()) {
      sendMessage(sender, index + " - " + lootReward.getType() + " - " + lootReward.getDataString());
      index++;
    }
  }

  @Command(identifier = "arena removeItemReward", permissions = "arenas.wave")
  public void setRewardsCommand(Player sender, @Arg(name = "arenaId") String arenaId,
      @Arg(name = "waveNumber") int wave, @Arg(name = "rewardIndex") int reward) {

    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      sendMessage(sender, "&eNo arena named " + arenaId + " found!");
      return;
    }

    if (arena.getWaves().get(wave - 1) == null) {
      sendMessage(sender, "&eThis arena has no wave " + wave);
      return;
    }

    if (arena.getWaves().get(wave - 1).getLootRewards() == null) {
      arena.getWaves().get(wave - 1).setLootRewards(new ArrayList<>());
    }

    if (arena.getWaves().get(wave - 1).getLootRewards().size() < reward) {
      sendMessage(sender, "&eNo reward with index " + reward);
      return;
    }

    arena.getWaves().get(wave - 1).getLootRewards().remove(reward);

    sendMessage(sender, "Removed reward with index " + reward);
  }

  @Command(identifier = "arena removewave", permissions = "arenas.wave")
  public void removeWaveCommand(Player sender, @Arg(name = "arenaId") String arenaId, @Arg(name = "waveNumber") int wave) {
    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      sendMessage(sender, "&eNo arena named " + arenaId + " found!");
      return;
    }
    if (arena.getWaves() == null) {
      arena.setWaves(new ArrayList<>());
    }
    if (arena.getWaves().size() == 0) {
      sendMessage(sender, "&eArena " + arenaId + " has no waves to clear!");
      return;
    }
    if (arena.getWaves().get(wave - 1) == null) {
      sendMessage(sender, "&eArena " + arenaId + " does not have a wave " + wave + "!");
      return;
    }
    arena.getWaves().remove(wave - 1);
    sendMessage(sender, "Remove wave. New Size: " + arena.getWaves().size());
  }

  @Command(identifier = "arena clearwave", permissions = "arenas.wave")
  public void clearWaveCommand(Player sender, @Arg(name = "arenaId") String arenaId, @Arg(name = "waveNumber") int wave) {
    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      sendMessage(sender, "&eNo arena named " + arenaId + " found!");
      return;
    }
    if (arena.getWaves() == null) {
      arena.setWaves(new ArrayList<>());
    }
    if (arena.getWaves().size() == 0) {
      sendMessage(sender, "&eArena " + arenaId + " has no waves to clear!");
      return;
    }
    if (arena.getWaves().get(wave - 1) == null) {
      sendMessage(sender, "&eArena " + arenaId + " does not have a wave " + wave + "!");
      return;
    }
    arena.getWaves().get(wave - 1).getArenaTask().clear();
    arena.getWaves().get(wave - 1).setLootRewards(new ArrayList<>());
    arena.getWaves().get(wave - 1).setExpBonus(0);
    arena.getWaves().get(wave - 1).setMoneyBonus(0);
    sendMessage(sender, "Cleared wave " + wave);
  }

  @Command(identifier = "arena waveRewards", permissions = "arenas.wave")
  public void waveRewardsCommand(Player sender, @Arg(name = "arenaId") String arenaId,
      @Arg(name = "waveNumber") int wave, @Arg(name = "expBonus") double expBonus,
      @Arg(name = "moneyBonus") double moneyBonus) {
    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      sendMessage(sender, "&eNo arena named " + arenaId + " found!");
      return;
    }
    if (arena.getWaves() == null) {
      arena.setWaves(new ArrayList<>());
    }
    if (arena.getWaves().size() == 0) {
      sendMessage(sender, "&eArena " + arenaId + " has no waves to clear!");
      return;
    }
    if (arena.getWaves().get(wave - 1) == null) {
      sendMessage(sender, "&eArena " + arenaId + " does not have a wave " + wave + "!");
      return;
    }

    arena.getWaves().get(wave - 1).setExpBonus(expBonus);
    arena.getWaves().get(wave - 1).setMoneyBonus(moneyBonus);

    sendMessage(sender, "&aSet exp and money bonus on wave " + wave);
  }

  @Command(identifier = "arena addspawn", permissions = "arenas.spawn")
  public void addSpawn(Player sender, @Arg(name = "arenaId") String arenaId,
      @Arg(name = "waveNumber") int wave, @Arg(name = "amount") int amount,
      @Arg(name = "instanceId") String instanceId, @Arg(name = "second") int second,
      @Arg(name = "uniqueId") String uniqueId) {
    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      sendMessage(sender, "&eNo arena named " + arenaId + " found!");
      return;
    }
    if (wave > arena.getWaves().size()) {
      sendMessage(sender,
          "&eArena " + arenaId + " has only " + arena.getWaves().size() + " waves!");
      return;
    }
    if (!arena.getWaves().get(wave - 1).getArenaTask().containsKey(second)) {
      arena.getWaves().get(wave - 1).getArenaTask().put(second, new HashSet<>());
    }

    BasicLocation loc = arena.getInstances().get(instanceId);
    Vector offset = sender.getLocation().clone()
        .subtract(new Vector(loc.getX(), loc.getY(), loc.getZ())).toVector();

    ArenaSpawn spawn = new ArenaSpawn();
    spawn.setMobId(uniqueId);
    spawn.setAmount(amount);
    spawn.setOffset(offset);

    arena.getWaves().get(wave - 1).getArenaTask().get(second).add(spawn);
    sendMessage(sender, "&aAdded spawn!");
  }
}
