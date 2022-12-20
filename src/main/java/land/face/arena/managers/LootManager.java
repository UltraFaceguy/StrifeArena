package land.face.arena.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.FireworkUtil;
import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import info.faceland.mint.MintEconomy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import land.face.arena.StrifeArenaPlugin;
import land.face.strife.StrifePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.nunnerycode.mint.MintPlugin;

public class LootManager {

  private final StrifeArenaPlugin plugin;

  private final Map<UUID, Inventory> lootMap = new HashMap<>();
  private final Map<UUID, Double> cashMap = new HashMap<>();
  private final Map<UUID, Double> expMap = new HashMap<>();

  private final Map<UUID, Integer> storedLootEntries = new HashMap<>();
  private final Map<UUID, Float> storedLootChance = new HashMap<>();
  private final Map<UUID, Float> storedLootRarity = new HashMap<>();

  private final Map<UUID, Float> storedCashExponent = new HashMap<>();
  private final Map<UUID, Float> storedXpExponent = new HashMap<>();

  private final MintEconomy mintEconomy = MintPlugin.getInstance().getEconomy();

  private final float expExponentGain;
  private final float cashExponentGain;

  public LootManager(StrifeArenaPlugin plugin) {
    this.plugin = plugin;
    expExponentGain = (float) plugin.getSettings().getDouble("reward-xp-exponent", 1.01) - 1;
    cashExponentGain = (float) plugin.getSettings().getDouble("reward-money-exponent", 1.01) - 1;
  }

  public void initializeLoot(Player player) {
    lootMap.put(player.getUniqueId(), Bukkit.createInventory(player, 54, "The Loots"));
  }

  public void createLootBonusRecords(Player player) {
    UUID uuid = player.getUniqueId();
    storedLootEntries.put(uuid, 0);
    storedLootChance.put(uuid, 0f);
    storedLootRarity.put(uuid, 0f);
  }

  public void purgeLootBonusRecords(Player player) {
    UUID uuid = player.getUniqueId();
    storedLootEntries.remove(uuid);
    storedLootChance.remove(uuid);
    storedLootRarity.remove(uuid);
  }

  public void purgeLoot(Player player) {
    lootMap.remove(player.getUniqueId());
    cashMap.remove(player.getUniqueId());
    expMap.remove(player.getUniqueId());
    storedXpExponent.remove(player.getUniqueId());
    storedCashExponent.remove(player.getUniqueId());
  }

  public void addItem(Player player, ItemStack stack) {
    lootMap.get(player.getUniqueId()).addItem(stack);
  }

  public Inventory getLootInventory(Player player) {
    return lootMap.get(player.getUniqueId());
  }

  public void addLootBonusRecord(Player player, float quantity, float rarity) {
    UUID uuid = player.getUniqueId();
    storedLootEntries.put(uuid, storedLootEntries.get(uuid) + 1);
    storedLootChance.put(uuid, storedLootChance.get(uuid) + quantity);
    storedLootRarity.put(uuid, storedLootRarity.get(uuid) + rarity);
  }

  public float getAvgLootBonus(Player player) {
    UUID uuid = player.getUniqueId();
    return storedLootChance.get(uuid) / storedLootEntries.get(uuid);
  }

  public float getAvgRarityBonus(Player player) {
    UUID uuid = player.getUniqueId();
    return storedLootRarity.get(uuid) / storedLootEntries.get(uuid);
  }

  public void addCash(Player player, double amount) {
    double current = cashMap.getOrDefault(player.getUniqueId(), 0D);
    cashMap.put(player.getUniqueId(), current + amount);
  }

  public void compoundCash(Player player) {
    storedCashExponent.put(player.getUniqueId(),
        storedCashExponent.getOrDefault(player.getUniqueId(), 1f) + cashExponentGain);
  }

  public void addExp(Player player, double amount) {
    double current = expMap.getOrDefault(player.getUniqueId(), 0D);
    expMap.put(player.getUniqueId(), current + amount);
  }

  public void compoundExp(Player player) {
    storedXpExponent.put(player.getUniqueId(),
        storedXpExponent.getOrDefault(player.getUniqueId(), 1f) + expExponentGain);
  }

  public double getCash(Player player) {
    return cashMap.getOrDefault(player.getUniqueId(), 0D) *
        storedCashExponent.getOrDefault(player.getUniqueId(), 1f);
  }

  public double getExp(Player player) {
    return expMap.getOrDefault(player.getUniqueId(), 0D) *
        storedXpExponent.getOrDefault(player.getUniqueId(), 1f);
  }

  public void explodeLoot(Player player, Location location) {
    location = location.clone().add(0, 1, 0);
    for (ItemStack stack : lootMap.get(player.getUniqueId())) {
      if (stack == null || stack.getType() == Material.AIR) {
        continue;
      }
      Item item = location.getWorld().dropItem(location, stack);
      double xSpeed = (0.05 + Math.random() * 0.25) * (Math.random() > 0.5 ? -1 : 1);
      double ySpeed = 0.1 + Math.random() * 0.5;
      double zSpeed = (0.05 + Math.random() * 0.25) * (Math.random() > 0.5 ? -1 : 1);
      Vector vec = new Vector(xSpeed, ySpeed, zSpeed);
      item.setVelocity(vec);
    }
    lootMap.put(player.getUniqueId(), null);
    double money = getCash(player);
    double exp = getExp(player);
    if (money >= 1) {
      MessageUtils.sendMessage(player, "&6 Bits Awarded: &e" + StrifeArenaPlugin.INT_FORMAT.format(money) + "◎");
      mintEconomy.depositPlayer(player, money);
    }
    if (exp >= 1) {
      MessageUtils.sendMessage(player, "&2 XP Awarded: &a" + StrifeArenaPlugin.INT_FORMAT.format(exp) + " XP");
      StrifePlugin.getInstance().getExperienceManager().addExperience(player, exp, true);
    }
    purgeLoot(player);
    player.playSound(location, Sound.BLOCK_CHEST_OPEN, 2, 1F);
    player.playSound(location, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE, 1, 1F);
    FireworkUtil.spawnFirework(location, Type.BALL, Color.WHITE, Color.BLUE, false, true);
    FireworkUtil.spawnFirework(location, Type.BALL, Color.BLUE, Color.GREEN, false, true);
    FireworkUtil.spawnFirework(location, Type.BALL, Color.GREEN, Color.WHITE, false, true);
    location.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, location, 35, 1D, 1D, 1D, 0D);
  }
}
