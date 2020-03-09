package land.face.arena.listeners;

import com.tealcube.minecraft.bukkit.bullion.GoldDropEvent;
import info.faceland.loot.events.LootDropEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MobDropListener implements Listener {

  public static String ARENA_META = "ARENA_MOB";

  @EventHandler
  public void onLootDrop(LootDropEvent event) {
    if (event.getEntity().hasMetadata(ARENA_META)) {
      event.setCancelled(true);
    }
  }

  @EventHandler
  public void onMoneyDrop(GoldDropEvent event) {
    if (event.getLivingEntity().hasMetadata(ARENA_META)) {
      event.setCancelled(true);
    }
  }
}

