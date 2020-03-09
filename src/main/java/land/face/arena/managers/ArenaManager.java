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
import java.util.UUID;
import land.face.arena.StrifeArenaPlugin;
import land.face.arena.data.Arena;
import land.face.arena.data.ArenaInstance;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public class ArenaManager {

  private Map<String, Arena> arenas = new HashMap<>();
  private Map<String, List<ArenaInstance>> runningArenas = new HashMap<>();
  private Map<UUID, ArenaInstance> playerArenaMap = new HashMap<>();

  private Gson gson = new Gson();

  public void joinArena(Player player, String arenaId) {
    if (!arenas.containsKey(arenaId)) {
      Bukkit.getLogger().warning("Tried to add player not non-existing arena " + arenaId);
      return;
    }
    if (playerArenaMap.containsKey(player.getUniqueId())) {
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
    playerArenaMap.put(player.getUniqueId(), arenaInstance);

    player.teleport(arena.getInstances().get(instance).asLocation());
    arenaInstance.beginNextWave();
  }

  public void beginNextWave(Player player) {
    playerArenaMap.get(player.getUniqueId()).beginNextWave();
  }

  public void endArena(Player player) {
    playerArenaMap.get(player.getUniqueId()).doArenaEnd(player);
  }

  public void exitArena(Player player, boolean teleportToExitLocation) {
    StrifeArenaPlugin.getInstance().getLootManager().purgeLoot(player);
    ArenaInstance arenaInstance = playerArenaMap.get(player.getUniqueId());
    if (arenaInstance == null) {
      return;
    }

    Location loc = arenaInstance.getArena().getInstances().get(arenaInstance.getInstanceId()).asLocation();
    Collection<Entity> entities = loc.getNearbyEntities(40, 40, 40);
    for (Entity entity : entities) {
      if (entity instanceof Item) {
        entity.remove();
      }
    }

    String arena = arenaInstance.getArena().getId();
    runningArenas.get(arena).remove(arenaInstance);
    playerArenaMap.remove(player.getUniqueId());
    if (teleportToExitLocation) {
      player.teleport(arenaInstance.getArena().getExitLocation().asLocation());
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
    if (!runningArenas.containsKey(arena.getId())) {
      for (String instanceId : arena.getInstances().keySet()) {
        return instanceId;
      }
      throw new IllegalArgumentException("arena " + arena.getId() + " has no instances");
    }
    for (String instanceId : arena.getInstances().keySet()) {
      if (runningArenas.get(arena.getId()).isEmpty()) {
        return instanceId;
      }
      for (ArenaInstance arenaInstance : runningArenas.get(arena.getId())) {
        if (arenaInstance.getInstanceId().equals(instanceId)) {
          continue;
        }
        return instanceId;
      }
    }
    return null;
  }

  public void saveArenas() {
    try (FileWriter writer = new FileWriter(
        StrifeArenaPlugin.getInstance().getDataFolder() + "/arenas.json")) {
      gson.toJson(arenas.values(), writer);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void loadArenas() {
    try (FileReader reader = new FileReader(
        StrifeArenaPlugin.getInstance().getDataFolder() + "/arenas.json")) {
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
