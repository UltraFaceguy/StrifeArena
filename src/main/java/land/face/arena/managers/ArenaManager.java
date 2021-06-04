package land.face.arena.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.shade.google.gson.Gson;
import com.tealcube.minecraft.bukkit.shade.google.gson.JsonArray;
import com.tealcube.minecraft.bukkit.shade.google.gson.JsonElement;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import land.face.arena.StrifeArenaPlugin;
import land.face.arena.data.Arena;
import land.face.arena.data.ArenaInstance;
import land.face.arena.data.Record;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class ArenaManager {

  private final StrifeArenaPlugin plugin;

  private final Map<String, Arena> arenas = new HashMap<>();
  private final Map<String, List<ArenaInstance>> runningArenas = new HashMap<>();
  private final Map<UUID, ArenaInstance> playerArenaMap = new HashMap<>();

  private final Gson gson = new Gson();

  public ArenaManager(StrifeArenaPlugin plugin) {
    this.plugin = plugin;
  }

  public void joinArena(Player player, String arenaId) {
    if (!arenas.containsKey(arenaId)) {
      Bukkit.getLogger().warning("Tried to add player not non-existing arena " + arenaId);
      return;
    }
    if (isInArena(player)) {
      MessageUtils.sendMessage(player, "&cYou are already in an arena...");
      Bukkit.getLogger()
          .warning("Tried to start arena for player already in one! " + player.getDisplayName());
      return;
    }
    Arena arena = arenas.get(arenaId);
    String instance = getFirstOpenInstance(arena);
    if (StringUtils.isBlank(instance)) {
      MessageUtils.sendMessage(player,
          "&eSorry! There are no open slots in this arena. Please wait and try again!");
      return;
    }

    ArenaInstance arenaInstance = new ArenaInstance(arena, player, instance);
    if (!runningArenas.containsKey(arenaId)) {
      runningArenas.put(arenaId, new ArrayList<>());
    }
    runningArenas.get(arenaId).add(arenaInstance);
    player.teleport(arena.getInstances().get(instance).asLocation());
    playerArenaMap.put(player.getUniqueId(), arenaInstance);
    plugin.getLootManager().initializeLoot(player);
    arenaInstance.beginNextWave();
  }

  public void beginNextWave(Player player) {
    playerArenaMap.get(player.getUniqueId()).beginNextWave();
  }

  public void endArena(Player player) {
    playerArenaMap.get(player.getUniqueId()).doArenaEnd(player);
  }

  public boolean isInArena(Player player) {
    return playerArenaMap.containsKey(player.getUniqueId());
  }

  public void exitArena(Player player, boolean teleportToExitLocation) {
    if (!isInArena(player)) {
      return;
    }
    plugin.getLootManager().purgeLoot(player);
    ArenaInstance arenaInstance = playerArenaMap.get(player.getUniqueId());
    if (arenaInstance == null) {
      return;
    }

    Location loc = arenaInstance.getArena().getInstances().get(arenaInstance.getInstanceId())
        .asLocation();
    Collection<Entity> entities = loc.getNearbyEntities(50, 50, 50);
    for (Entity entity : entities) {
      if (!(entity instanceof Player || entity instanceof ArmorStand)) {
        entity.remove();
      }
    }

    String arena = arenaInstance.getArena().getId();
    runningArenas.get(arena).remove(arenaInstance);
    playerArenaMap.remove(player.getUniqueId());
    arenaInstance.cancelTimers();
    MessageUtils.sendMessage(player, "&eYour arena run has ended.");
    if (teleportToExitLocation) {
      if (arenaInstance.getArena().getExitLocation() == null) {
        Bukkit.getServer().broadcastMessage(
            "HEY DING DONG, YOU FORGOT TO ADD AN ARENA EXIT FOR " + arenaInstance.getArena().getId()
                + " NOW HOW IS " + player.getName() + "SUPPOSED TO GET OUT?");
      } else {
        player.teleport(arenaInstance.getArena().getExitLocation().asLocation());
      }
    }
  }

  public ArenaInstance getInstance(Player player) {
    return playerArenaMap.get(player.getUniqueId());
  }

  public void addArena(Arena arena) {
    arenas.put(arena.getId(), arena);
  }

  public Arena getArena(String id) {
    return arenas.getOrDefault(id, null);
  }

  private String getFirstOpenInstance(Arena arena) {
    for (String instanceId : arena.getInstances().keySet()) {
      if (runningArenas.get(arena.getId()) == null) {
        return instanceId;
      }
      boolean isRunning = false;
      for (ArenaInstance runningInstance : runningArenas.get(arena.getId())) {
        if (runningInstance.getInstanceId().equals(instanceId)) {
          isRunning = true;
          break;
        }
      }
      if (!isRunning) {
        return instanceId;
      }
    }
    return null;
  }

  public void updateRecordUsernames() {
    Bukkit.getLogger().info("[Strife Arena] Updating record usernames...");
    long stamp = System.currentTimeMillis();
    for (Arena arena : arenas.values()) {
      for (Entry<UUID, Record> entry : arena.getRecords().entrySet()) {
        entry.getValue().setUsername(Bukkit.getOfflinePlayer(entry.getKey()).getName());
      }
    }
    Bukkit.getLogger()
        .info("[Strife Arena] Updated records in " + (System.currentTimeMillis() - stamp) + "ms");
  }

  public void saveArenas() {
    try (FileWriter writer = new FileWriter(plugin.getDataFolder() + "/arenas.json")) {
      gson.toJson(arenas.values(), writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void loadArenas() {
    try (FileReader reader = new FileReader(plugin.getDataFolder() + "/arenas.json")) {
      JsonArray array = gson.fromJson(reader, JsonArray.class);
      for (JsonElement e : array) {
        Arena arena = gson.fromJson(e, Arena.class);
        arenas.put(arena.getId(), arena);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
