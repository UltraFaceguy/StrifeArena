package land.face.arena.commands;

import static com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils.sendMessage;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.shade.acf.BaseCommand;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandAlias;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandCompletion;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.CommandPermission;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Subcommand;
import com.tealcube.minecraft.bukkit.shade.acf.annotation.Syntax;
import com.tealcube.minecraft.bukkit.shade.acf.bukkit.contexts.OnlinePlayer;
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

@CommandAlias("arena|arenas")
public class ArenaCommand extends BaseCommand {

  private final StrifeArenaPlugin plugin;
  private final Gson gson = new Gson();

  public ArenaCommand(StrifeArenaPlugin plugin) {
    this.plugin = plugin;
  }

  @Subcommand("start")
  @CommandCompletion("@players @arenas")
  @CommandPermission("arenas.start")
  public void start(CommandSender sender, OnlinePlayer player, String id) {
    if (plugin.getArenaManager().getArena(id) == null) {
      sendMessage(sender, "&eArena " + id + " does not exist!");
      return;
    }
    plugin.getArenaManager().joinArena(player.getPlayer(), id);
  }

  @Subcommand("exit|leave|quit")
  @CommandPermission("arenas.exit")
  public void exit(OnlinePlayer sender) {
    ArenaInstance instance = plugin.getArenaManager().getInstance(sender.getPlayer());
    if (instance == null || !instance.isArenaDone()) {
      MessageUtils.sendMessage(sender.getPlayer(), "&eThis command can only be run when you've finished an arena!");
      return;
    }
    plugin.getArenaManager().exitArena(sender.getPlayer(), true);
  }

  @Subcommand("create")
  @Syntax("<ArenaID>")
  @CommandPermission("arena.create")
  public void create(CommandSender sender, String id) {
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

  @Subcommand("edit")
  @CommandPermission("arenas.edit")
  public class EditCommand extends BaseCommand {

    @Subcommand("level")
    @CommandCompletion("@arenas @range:1-100 @range:1-100")
    public void editLevel(CommandSender sender, String id, int level, double levelPerWave) {
      if (plugin.getArenaManager().getArena(id) == null) {
        sendMessage(sender, "&eArena " + id + " doesn't exist!");
        return;
      }

      Arena arena = plugin.getArenaManager().getArena(id);
      arena.setArenaLevel(level);
      arena.setArenaLevelPerWave(levelPerWave);
      sendMessage(sender, "&aset");
    }

    @Subcommand("requirement")
    @CommandCompletion("@arenas @arenas")
    public void editReq(CommandSender sender, String arena, String requiredArena) {
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

    @Subcommand("exit")
    @CommandCompletion("@arenas")
    public void editExit(Player sender, String id) {
      if (plugin.getArenaManager().getArena(id) == null) {
        sendMessage(sender, "&eArena " + id + " does not exist!");
        return;
      }
      plugin.getArenaManager().getArena(id)
          .setExitLocation(BasicLocation.fromLocation(sender.getLocation()));
      sendMessage(sender, "&aexit location updated");
    }

    @Subcommand("instance add")
    @CommandCompletion("@arenas")
    public void menuCommand(Player sender, String arenaId, String instanceId) {
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

    @Subcommand("instance remove")
    @CommandCompletion("@arenas")
    public void removeInstCommand(Player sender, String arenaId, String instanceId) {
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

    @Subcommand("instance list")
    @CommandCompletion("@arenas")
    public void menuCommand(Player sender, String arenaId) {
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
  }

  @Subcommand("addItem")
  @CommandCompletion("@arenas")
  public void setBaseRewardsCommand(Player sender, String arenaId, String type, int amount,
      double chance, String data1, String data2) {

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

  @Subcommand("listItems")
  @CommandCompletion("@arenas")
  public void listBaseRewardCommand(Player sender, String arenaId) {

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

  @Subcommand("records")
  @CommandPermission("arenas.records")
  public class RecordsCommand extends BaseCommand {

    @Subcommand("list")
    @CommandCompletion("@arenas")
    public void recordsCommand(CommandSender sender, String arenaId) {
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

    @Subcommand("clear")
    @CommandCompletion("@arenas")
    public void clearRecordsCommand(CommandSender sender, String arenaId) {
      Arena arena = plugin.getArenaManager().getArena(arenaId);
      if (arena == null) {
        sendMessage(sender, "&eNo arena named " + arenaId + " found!");
        return;
      }
      arena.getRecords().clear();
      sendMessage(sender, "cleared records for  " + arenaId);
    }

    @Subcommand("remove")
    @CommandCompletion("@arenas @players")
    public void removeRecordCommand(CommandSender sender, String arenaId, String name) {
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
  }

  @Subcommand("wave")
  @CommandPermission("arenas.records")
  public class WaveCommand extends BaseCommand {

    @Subcommand("add")
    @CommandCompletion("@arenas")
    public void addWaveCommand(Player sender, String arenaId) {
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

    @Subcommand("rewards")
    @CommandCompletion("@arenas")
    public void setRewardsCommand(Player sender, String arenaId, double minXp, double maxXp,
        double expExp, double minMoney, double maxMoney, double moneyExp) {
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

    @Subcommand("addItem")
    @CommandCompletion("@arenas")
    public void setRewardsCommand(Player sender, String arenaId, int wave, String type, int amount,
        double chance, String data1, String data2) {

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

    @Subcommand("listItems")
    @CommandCompletion("@arenas")
    public void setRewardsCommand(Player sender, String arenaId, int wave) {

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

    @Subcommand("removeItem")
    @CommandCompletion("@arenas")
    public void setRewardsCommand(Player sender, String arenaId, int wave, int reward) {

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

    @Subcommand("remove")
    @CommandCompletion("@arenas")
    public void removeWaveCommand(Player sender, String arenaId, int wave) {
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

    @Subcommand("clear")
    @CommandCompletion("@arenas")
    public void clearWaveCommand(Player sender, String arenaId, int wave) {
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

    @Subcommand("rewards")
    @CommandCompletion("@arenas")
    public void waveRewardsCommand(Player sender, String arenaId, int wave, double expBonus,
        double moneyBonus) {
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
  }

  @Subcommand("spawns")
  @CommandPermission("arenas.spawns")
  public class SpawnsCommand extends BaseCommand {

    @Subcommand("add")
    @CommandCompletion("@arenas")
    public void spawnsAdd(Player sender, String arenaId, String instanceId, int wave, int second,
        String uniqueId, int amount) {
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

    @Subcommand("remove")
    @CommandCompletion("@arenas")
    public void spawnsRemove(Player sender, String arenaId, int wave) {
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

    @Subcommand("list")
    @CommandCompletion("@arenas")
    public void listSpawns(Player sender, String arenaId, int wave) {
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
}
