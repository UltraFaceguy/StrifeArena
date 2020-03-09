package land.face.arena.data;

import org.bukkit.util.Vector;

public class ArenaSpawn {

  private String mobId;
  private int amount;
  private Vector offset;

  public String getMobId() {
    return mobId;
  }

  public void setMobId(String mobId) {
    this.mobId = mobId;
  }

  public int getAmount() {
    return amount;
  }

  public void setAmount(int amount) {
    this.amount = amount;
  }

  public Vector getOffset() {
    return offset;
  }

  public void setOffset(Vector offset) {
    this.offset = offset;
  }

}
