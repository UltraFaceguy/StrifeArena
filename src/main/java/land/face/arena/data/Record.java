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

  public static void bumpRecord(Player player, Record record, int wave, long time) {
    record.setUsername(player.getName());
    if (time == -1) {
      record.setHighestWave(Math.max(record.getHighestWave(), wave));
      return;
    }
    if (record.getShortestTime() == 0) {
      record.setShortestTime(time);
      return;
    }
    record.setShortestTime(Math.min(record.getShortestTime(), time));
  }

}
