package land.face.arena.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Arena {

  private final String id;
  private ArrayList<ArenaWave> waves;
  private HashMap<String, BasicLocation> instances;
  private HashMap<UUID, Record> records;
  private BasicLocation exitLocation;
  private double arenaLevel;
  private double arenaLevelPerWave;
  private double minMoneyPerWave;
  private double maxMoneyPerWave;
  private double moneyExponent;
  private double minExpPerWave;
  private double maxExpPerWave;
  private double expExponent;

  public Arena(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public ArrayList<ArenaWave> getWaves() {
    return waves;
  }

  public void setWaves(ArrayList<ArenaWave> waves) {
    this.waves = waves;
  }

  public HashMap<String, BasicLocation> getInstances() {
    return instances;
  }

  public void setInstances(HashMap<String, BasicLocation> instances) {
    this.instances = instances;
  }

  public HashMap<UUID, Record> getRecords() {
    return records;
  }

  public void setRecords(HashMap<UUID, Record> records) {
    this.records = records;
  }

  public BasicLocation getExitLocation() {
    return exitLocation;
  }

  public void setExitLocation(BasicLocation exitLocation) {
    this.exitLocation = exitLocation;
  }

  public double getArenaLevel() {
    return arenaLevel;
  }

  public void setArenaLevel(double arenaLevel) {
    this.arenaLevel = arenaLevel;
  }

  public double getArenaLevelPerWave() {
    return arenaLevelPerWave;
  }

  public void setArenaLevelPerWave(double arenaLevelPerWave) {
    this.arenaLevelPerWave = arenaLevelPerWave;
  }

  public double getMinMoneyPerWave() {
    return minMoneyPerWave;
  }

  public void setMinMoneyPerWave(double minMoneyPerWave) {
    this.minMoneyPerWave = minMoneyPerWave;
  }

  public double getMaxMoneyPerWave() {
    return maxMoneyPerWave;
  }

  public void setMaxMoneyPerWave(double maxMoneyPerWave) {
    this.maxMoneyPerWave = maxMoneyPerWave;
  }

  public double getMoneyExponent() {
    return moneyExponent;
  }

  public void setMoneyExponent(double moneyExponent) {
    this.moneyExponent = moneyExponent;
  }

  public double getMinExpPerWave() {
    return minExpPerWave;
  }

  public void setMinExpPerWave(double minExpPerWave) {
    this.minExpPerWave = minExpPerWave;
  }

  public double getMaxExpPerWave() {
    return maxExpPerWave;
  }

  public void setMaxExpPerWave(double maxExpPerWave) {
    this.maxExpPerWave = maxExpPerWave;
  }

  public double getExpExponent() {
    return expExponent;
  }

  public void setExpExponent(double expExponent) {
    this.expExponent = expExponent;
  }
}
