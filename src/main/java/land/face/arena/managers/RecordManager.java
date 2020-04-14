package land.face.arena.managers;

import com.tealcube.minecraft.bukkit.TextUtils;
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

    if (record.getHighestWave() == arena.getWaves().size()) {
      return record.getUsername() + COMPLETED_STRING + DurationFormatUtils
          .formatDuration(record.getShortestTime(), "m'm 's's'");
    }
    return record.getUsername() + WAVE_STRING + record.getHighestWave();
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
