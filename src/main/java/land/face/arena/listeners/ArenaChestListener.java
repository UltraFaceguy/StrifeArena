package land.face.arena.listeners;

import land.face.arena.StrifeArenaPlugin;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class ArenaChestListener implements Listener {

  private StrifeArenaPlugin plugin;

  public ArenaChestListener(StrifeArenaPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onPlayerDeath(InventoryOpenEvent event) {
    if (event.getInventory().getHolder() instanceof Chest) {
      if (((Chest) event.getInventory().getHolder()).hasMetadata("ARENA_CHEST")) {
        plugin.getRewardsMenu().open((Player) event.getPlayer());
        event.setCancelled(true);
      }
    }
  }
}

