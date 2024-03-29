package land.face.arena.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.shade.apache.commons.lang3.time.DurationFormatUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import land.face.arena.StrifeArenaPlugin;
import land.face.arena.data.Arena;
import land.face.arena.data.Record;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class RecordManager {

  private StrifeArenaPlugin plugin;
  private static String COMPLETED_STRING = TextUtils.color(" &7- &aComplete! &f");
  private static String WAVE_STRING = TextUtils.color(" &7- &eWave: &f");
  private static String NO_RECORD = TextUtils.color("&7< No Record! >");

  private HashMap<String, Long> lastUpdatedMap = new HashMap<>();
  private HashMap<String, List<Record>> recordList = new HashMap<>();

  public RecordManager(StrifeArenaPlugin plugin) {
    this.plugin = plugin;
  }

  public boolean hasCompleted(Player player, String arenaId) {
    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      return false;
    }
    Record record = arena.getRecords().get(player.getUniqueId());
    if (record == null) {
      return false;
    }
    return record.getShortestTime() > 0;
  }

  public String getRecord(String arenaId, int position) {
    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      return "INVALID-ARENA-" + arenaId;
    }

    if (lastUpdatedMap.containsKey(arenaId)
        || System.currentTimeMillis() - lastUpdatedMap.getOrDefault(arenaId, 0L) > 60000) {
      lastUpdatedMap.put(arenaId, System.currentTimeMillis());

      List<Record> records = new ArrayList<>();

      LinkedHashMap<UUID, Record> sortedMap = sortByValues(arena.getRecords());

      Iterator<Entry<UUID, Record>> iterator = sortedMap.entrySet().iterator();
      while (iterator.hasNext()) {
        Entry<UUID, Record> entry = iterator.next();
        records.add(entry.getValue());
      }
      recordList.put(arenaId, records);
    }

    if (position >= recordList.get(arenaId).size()) {
      return NO_RECORD;
    }

    Record record = recordList.get(arenaId).get(position);

    return formatRecordString(record, arena);
  }

  public String getRecord(String arenaId, UUID uuid) {
    Arena arena = plugin.getArenaManager().getArena(arenaId);
    if (arena == null) {
      return "INVALID-ARENA-" + arenaId;
    }

    Record record = arena.getRecords().get(uuid);
    if (record == null) {
      return NO_RECORD;
    }

    return formatRecordString(record, arena);
  }

  private static String formatRecordString(Record record, Arena arena) {
    if (record.getHighestWave() == arena.getWaves().size()) {
      return ChatColor.WHITE + record.getUsername() + ChatColor.GRAY + " [" + ChatColor.GREEN
          + DurationFormatUtils.formatDuration(record.getShortestTime(),
          "m'm 's's'" + ChatColor.DARK_GREEN + "✔" + ChatColor.GRAY + "]");
    }
    return ChatColor.WHITE + record.getUsername() + ChatColor.GRAY + " [" + ChatColor.YELLOW
        + "Wave " + record.getHighestWave() + ChatColor.GRAY + "]";
  }

  private static LinkedHashMap<UUID, Record> sortByValues(Map<UUID, Record> unsortedMap) {
    LinkedHashMap<UUID, Record> sortedMap = new LinkedHashMap<>();

    unsortedMap.entrySet()
        .stream()
        .sorted(Map.Entry.comparingByValue())
        .forEachOrdered(x -> sortedMap.put(x.getKey(), x.getValue()));

    return sortedMap;
  }
}
