package land.face.arena.managers;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import land.face.arena.StrifeArenaPlugin;
import land.face.strife.StrifePlugin;
import land.face.strife.util.FireworkUtil;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.nunnerycode.mint.MintPlugin;

public class LootManager {

  private Map<UUID, Inventory> lootMap = new HashMap<>();
  private Map<UUID, Double> cashMap = new HashMap<>();
  private Map<UUID, Double> expMap = new HashMap<>();

  public void addExp(Player player, double amount) {
    if (!expMap.containsKey(player.getUniqueId())) {
      expMap.put(player.getUniqueId(), 0D);
    }
    expMap.put(player.getUniqueId(), expMap.get(player.getUniqueId()) + amount);
  }

  public void addCash(Player player, double amount) {
    if (!cashMap.containsKey(player.getUniqueId())) {
      cashMap.put(player.getUniqueId(), 0D);
    }
    cashMap.put(player.getUniqueId(), cashMap.get(player.getUniqueId()) + amount);
  }

  public void addItem(Player player, ItemStack stack) {
    if (!lootMap.containsKey(player.getUniqueId())) {
      lootMap.put(player.getUniqueId(), Bukkit.createInventory(player, 54, "The Loots"));
    }
    lootMap.get(player.getUniqueId()).addItem(stack);
  }

  public void purgeLoot(Player player) {
    lootMap.remove(player.getUniqueId());
    cashMap.remove(player.getUniqueId());
    expMap.remove(player.getUniqueId());
  }

  public Inventory getLootInventory(Player player) {
    return lootMap.get(player.getUniqueId());
  }

  public void addCash(Player player, double min, double max, double exponent, double bonus) {
    double current = cashMap.getOrDefault(player.getUniqueId(), 0D);
    double newValue = Math.pow(current, exponent);
    newValue += min + ((max - min) * Math.random());
    newValue += bonus;
    cashMap.put(player.getUniqueId(), newValue);
  }

  public void addExp(Player player, double min, double max, double exponent, double bonus) {
    double current = expMap.getOrDefault(player.getUniqueId(), 0D);
    double newValue = Math.pow(current, exponent);
    newValue += min + ((max - min) * Math.random());
    newValue += bonus;
    expMap.put(player.getUniqueId(), newValue);
  }

  public double getCash(Player player) {
    return cashMap.getOrDefault(player.getUniqueId(), 0D);
  }

  public double getExp(Player player) {
    return expMap.getOrDefault(player.getUniqueId(), 0D);
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
    double money = cashMap.getOrDefault(player.getUniqueId(), 0D);
    double exp = expMap.getOrDefault(player.getUniqueId(), 0D);
    if (money >= 1) {
      MessageUtils.sendMessage(player,
          "&eArena Reward: &f" + StrifeArenaPlugin.INT_FORMAT.format(money) + " Bits&e!");
      MintPlugin.getInstance().getEconomy().depositPlayer(player, money);
    }
    if (exp >= 1) {
      MessageUtils.sendMessage(player,
          "&2Arena Reward: &f" + StrifeArenaPlugin.INT_FORMAT.format(exp) + " XP&2!");
      StrifePlugin.getInstance().getExperienceManager().addExperience(player, exp, false);
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
