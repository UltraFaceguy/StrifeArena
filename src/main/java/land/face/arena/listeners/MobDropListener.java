package land.face.arena.listeners;

import com.tealcube.minecraft.bukkit.bullion.GoldDropEvent;
import info.faceland.loot.events.LootDropEvent;
import java.util.Map;
import java.util.WeakHashMap;
import land.face.arena.StrifeArenaPlugin;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import land.face.strife.events.StrifeCombatXpEvent;
import land.face.strife.stats.StrifeStat;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobDropListener implements Listener {

  public static Map<LivingEntity, Boolean> mobs = new WeakHashMap<>();
  private final StrifeArenaPlugin plugin;

  public MobDropListener(StrifeArenaPlugin plugin) {
    this.plugin = plugin;
  }

  public static void addHandledMob(LivingEntity livingEntity) {
    mobs.put(livingEntity, true);
  }

  @EventHandler
  public void onLootDrop(LootDropEvent event) {
    if (mobs.containsKey(event.getEntity())) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onKillMob(EntityDeathEvent event) {
    if (mobs.containsKey(event.getEntity()) && event.getEntity().getKiller() != null) {
      StrifeMob playerMob = StrifePlugin.getInstance()
          .getStrifeMobManager().getStatMob(event.getEntity().getKiller());
      float quantity = playerMob.getStat(StrifeStat.ITEM_DISCOVERY) / 100;
      float rarity = playerMob.getStat(StrifeStat.ITEM_RARITY) / 100;
      plugin.getLootManager().addLootBonusRecord(event.getEntity().getKiller(), quantity, rarity);
    }
  }

  @EventHandler
  public void onXpDrop(StrifeCombatXpEvent event) {
    if (mobs.containsKey(event.getEntity())) {
      StrifeMob mob = StrifePlugin.getInstance()
          .getStrifeMobManager().getStatMob(event.getPlayer());
      float amount = event.getBaseAmount();
      amount *= 1 + mob.getStat(StrifeStat.XP_GAIN) / 300;
      plugin.getLootManager().addExp(event.getPlayer(), amount);
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onMoneyDrop(GoldDropEvent event) {
    if (mobs.containsKey(event.getLivingEntity())) {
      plugin.getLootManager().addCash(event.getKiller(), Math.max(1, event.getAmount()));
      event.setCancelled(true);
    }
  }
}

