package land.face.arena.data;

import org.bukkit.entity.Player;

public class Record implements Comparable<Record> {

  private String username;
  private int highestWave = 0;
  private long shortestTime = 0;

  @Override
  public int compareTo(Record compareRecord) {
    if (this.highestWave == compareRecord.highestWave) {
      return (int) this.shortestTime - (int) compareRecord.shortestTime;
    } else {
      return compareRecord.highestWave - this.highestWave;
    }
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public int getHighestWave() {
    return highestWave;
  }

  public void setHighestWave(int highestWave) {
    this.highestWave = highestWave;
  }

  public long getShortestTime() {
    return shortestTime;
  }

  public void setShortestTime(long shortestTime) {
    this.shortestTime = shortestTime;
  }

  public static boolean bumpRecord(Player player, Record record, int newHighestWave, long newTime) {
    record.username = player.getName();
    int oldHighestWave = record.highestWave;
    record.highestWave = Math.max(oldHighestWave, newHighestWave);
    if (newTime == -1) {
      return newHighestWave > oldHighestWave;
    }
    long oldShortestTime = record.shortestTime;
    if (oldShortestTime == -1) {
      record.shortestTime = newTime;
      return true;
    }
    record.shortestTime = Math.min(record.shortestTime, newTime);
    return newTime < oldShortestTime;
  }

}
