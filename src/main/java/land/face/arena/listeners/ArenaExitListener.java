package land.face.arena.listeners;

import land.face.arena.StrifeArenaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class ArenaExitListener implements Listener {

  private StrifeArenaPlugin plugin;

  public ArenaExitListener(StrifeArenaPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event) {
    plugin.getArenaManager().exitArena(event.getEntity(), false);
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    plugin.getArenaManager().exitArena(event.getPlayer(), true);
  }

  @EventHandler
  public void onPlayerDeath(PlayerKickEvent event) {
    plugin.getArenaManager().exitArena(event.getPlayer(), true);
  }

  @EventHandler
  public void onPlayerTeleport(PlayerTeleportEvent event) {
    if (event.getTo().getWorld() != event.getFrom().getWorld()) {
      plugin.getArenaManager().exitArena(event.getPlayer(), false);
    }
  }
}

