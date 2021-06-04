package land.face.arena.commands;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.shade.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import land.face.arena.StrifeArenaPlugin;
import land.face.arena.data.Arena;
import land.face.arena.data.ArenaInstance;
import land.face.arena.data.ArenaSpawn;
import land.face.arena.data.ArenaWave;
import land.face.arena.data.BasicLocation;
import land.face.arena.data.LootReward;
import land.face.arena.data.LootReward.RewardType;
import land.face.arena.data.Record;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import se.ranzdo.bukkit.methodcommand.Arg;
import se.ranzdo.bukkit.methodcommand.Command;

public class BaseCommand {

  private StrifeArenaPlugin plugin;
  private Gson gson = new Gson();

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

  @Command(identifier = "arena exit", permissions = "arenas.exit")
  public void startCommand(Player sender) {
    ArenaInstance instance = plugin.getArenaManager().getInstance(sender);
    if (instance == null || !instance.isArenaDone()) {
      MessageUtils.sendMessage(sender, "&eThis command can only be run when you've finished an arena!");
      return;
    }
    plugin.getArenaManager().exitArena(sender, true);
  }

  @Command(identifier = "arena create", permissions = "arenas.create", onlyPlayers = false)
  public void reloadCommand(CommandSender sender, @Arg(name = "id") String id) {
    if (plugin.getArenaManager().getArena(id) != null) {
      sendMessage(sender, "&eArena " + id + " already exists!");
      return;
    }

    Arena arena = new Arena(id);
    arena.setRecords(new HashMap<>());
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

  @Command(identifier = "arena setRequiredArena", permissions = "arenas.edit", onlyPlayers = false)
  public void setLevelCommand(CommandSender sender, @Arg(name = "arena") String arena,
      @Arg(name = "requiredArena") String requiredArena) {
    if (plugin.getArenaManager().getArena(arena) == null) {
      sendMessage(sender, "&eArena " + arena + " doesn't exist!");
      return;
    }
    if (plugin.getArenaManager().getArena(requiredArena) == null) {
      sendMessage(sender, "&eArena " + requiredArena + " doesn't exist!");
      return;
    }

    plugin.getArenaManager().getArena(arena).setRequiredArena(requiredArena);
    sendMessage(sender, "&aset!!");
  }

  @Command(identifier = "arena setexit", permissions = "arenas.edit")
  public void addExist(Player sender, @Arg(name = "id") String id) {
    if (plugin.getArenaManager().getArena(id) == null) {
      sendMessage(sender, "&eArena " + id + " does not exist!");
      return;
    }
    plugin.getArenaManager().getArena(id)
        .setExitLocation(BasicLocation.fromLocation(sender.getLocation()));
    sendMessage(sender, "&aexit location updated");
  }

  @Command(identifier = "arena instance add", permissions = "arenas.instances")
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

  @Command(identifier = "arena instance remove", permissions = "arenas.instances")
  public void removeInstCommand(Player sender, @Arg(name = "arenaId") String arenaId,
      @Arg(name = "instanceId") String instanceId) {
    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      sendMessage(sender, "&eNo arena named " + arenaId + " found!");
      return;
    }
    if (arena.getInstances() == null) {
      arena.setInstances(new HashMap<>());
    }
    if (arena.getInstances().get(instanceId) == null) {
      sendMessage(sender, "&eInstance with " + instanceId + " does not exist");
      return;
    }
    arena.getInstances().remove(instanceId);
    sendMessage(sender, "&aRemoved arena instance!");
  }

  @Command(identifier = "arena records list", permissions = "arenas.reconds")
  public void recordsCommand(Player sender, @Arg(name = "arenaId") String arenaId) {
    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      sendMessage(sender, "&eNo arena named " + arenaId + " found!");
      return;
    }
    sendMessage(sender, "Records for " + arenaId);
    for (int i = 0; i < 10; i++) {
      sendMessage(sender, plugin.getRecordManager().getRecord(arenaId, i));
    }
  }

  @Command(identifier = "arena records clear", permissions = "arenas.reconds")
  public void clearRecordsCommand(Player sender, @Arg(name = "arenaId") String arenaId) {
    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      sendMessage(sender, "&eNo arena named " + arenaId + " found!");
      return;
    }
    arena.getRecords().clear();
    sendMessage(sender, "cleared records for  " + arenaId);
  }

  @Command(identifier = "arena records remove", permissions = "arenas.reconds")
  public void removeRecordCommand(Player sender, @Arg(name = "arenaId") String arenaId,
      @Arg(name = "playerName") String name) {
    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      sendMessage(sender, "&eNo arena named " + arenaId + " found!");
      return;
    }
    for (Entry<UUID, Record> entry : new HashMap<>(arena.getRecords()).entrySet()) {
      if (name.equals(entry.getValue().getUsername())) {
        arena.getRecords().remove(entry.getKey());
        break;
      }
    }
    sendMessage(sender, "Removed record for " + name + " in arena " + arenaId);
  }

  @Command(identifier = "arena instance list", permissions = "arenas.instances")
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
      @Arg(name = "maxMoneyPerWave") double maxMoney,
      @Arg(name = "moneyExponent") double moneyExp) {
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
  public void setBaseRewardsCommand(Player sender, @Arg(name = "arenaId") String arenaId,
      @Arg(name = "rewardType") String type, @Arg(name = "amount") int amount,
      @Arg(name = "chance") double chance, @Arg(name = "extraDataOne") String data1,
      @Arg(name = "extraDataTwo") String data2) {

    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      sendMessage(sender, "&eNo arena named " + arenaId + " found!");
      return;
    }

    LootReward lootReward = new LootReward();

    lootReward.setType(RewardType.valueOf(type.toUpperCase()));
    lootReward.setAmount(amount);
    lootReward.setProbability(chance);
    lootReward.setDataString(data1);
    lootReward.setDataStringTwo(data2);

    if (arena.getLootRewards() == null) {
      arena.setLootRewards(new ArrayList<>());
    }

    arena.getLootRewards().add(lootReward);

    sendMessage(sender, "Added reward to arena " + arenaId);
  }

  @Command(identifier = "arena listItemReward", permissions = "arenas.wave")
  public void listBaseRewardCommand(Player sender, @Arg(name = "arenaId") String arenaId) {

    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      sendMessage(sender, "&eNo arena named " + arenaId + " found!");
      return;
    }

    if (arena.getLootRewards() == null) {
      arena.setLootRewards(new ArrayList<>());
    }

    sendMessage(sender, "LootReward List for arena " + arenaId);
    int index = 0;
    for (LootReward lootReward : arena.getLootRewards()) {
      sendMessage(sender,
          index + " - " + lootReward.getType() + " - " + lootReward.getDataString());
      index++;
    }
  }

  @Command(identifier = "arena wave addItemReward", permissions = "arenas.wave")
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

  @Command(identifier = "arena wave listItemReward", permissions = "arenas.wave")
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
      sendMessage(sender,
          index + " - " + lootReward.getType() + " - " + lootReward.getDataString());
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
  public void removeWaveCommand(Player sender, @Arg(name = "arenaId") String arenaId,
      @Arg(name = "waveNumber") int wave) {
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
  public void clearWaveCommand(Player sender, @Arg(name = "arenaId") String arenaId,
      @Arg(name = "waveNumber") int wave) {
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

  @Command(identifier = "arena spawns add", permissions = "arenas.spawn")
  public void addSpawn(Player sender, @Arg(name = "arenaId") String arenaId,
      @Arg(name = "instanceId") String instanceId, @Arg(name = "waveNumber") int wave,
      @Arg(name = "time") int second, @Arg(name = "uniqueId") String uniqueId,
      @Arg(name = "amount") int amount) {
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

  @Command(identifier = "arena spawns remove", permissions = "arenas.spawn")
  public void addSpawn(Player sender, @Arg(name = "arenaId") String arenaId,
      @Arg(name = "waveNumber") int wave) {
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
    arena.getWaves().get(wave - 1).getArenaTask().clear();

    sendMessage(sender, "&acleared spawns on wave " + wave);
  }

  @Command(identifier = "arena spawns list", permissions = "arenas.spawn")
  public void listSpawns(Player sender, @Arg(name = "arenaId") String arenaId,
      @Arg(name = "waveNumber") int wave) {
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
    for (Entry<Integer, Set<ArenaSpawn>> entry : arena.getWaves().get(wave - 1).getArenaTask()
        .entrySet()) {
      for (ArenaSpawn spawn : entry.getValue()) {
        sendMessage(sender, "S:" + entry.getKey() + " - " + gson.toJson(spawn));
      }
    }
  }
}
