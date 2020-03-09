package land.face.arena.data;

public class Record {

  private int highestWave = 0;
  private long shortestTime = 0;

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

  public static void bumpRecord(Record record, int wave, long time) {
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
