package land.face.arena.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class ArenaWave {

  private HashMap<Integer, Set<ArenaSpawn>> arenaTask = new HashMap<>();
  private ArrayList<LootReward> lootRewards;
  private double moneyBonus;
  private double expBonus;

  public HashMap<Integer, Set<ArenaSpawn>> getArenaTask() {
    return arenaTask;
  }

  public void setArenaTask(HashMap<Integer, Set<ArenaSpawn>> arenaTask) {
    this.arenaTask = arenaTask;
  }

  public ArrayList<LootReward> getLootRewards() {
    return lootRewards;
  }

  public void setLootRewards(ArrayList<LootReward> lootRewards) {
    this.lootRewards = lootRewards;
  }

  public double getMoneyBonus() {
    return moneyBonus;
  }

  public void setMoneyBonus(double moneyBonus) {
    this.moneyBonus = moneyBonus;
  }

  public double getExpBonus() {
    return expBonus;
  }

  public void setExpBonus(double expBonus) {
    this.expBonus = expBonus;
  }

}
