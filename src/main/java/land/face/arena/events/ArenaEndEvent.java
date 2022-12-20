package land.face.arena.events;

import lombok.Getter;
import org.bukkit.entity.Player;

public class ArenaEndEvent extends ArenaEvent {

  @Getter
  private final String arenaId;
  @Getter
  private final int seconds;
  @Getter
  private final boolean completed;
  @Getter
  private final Player player;

  public ArenaEndEvent(String arenaId, int seconds, boolean completed, Player player) {
    this.arenaId = arenaId;
    this.seconds = seconds;
    this.completed = completed;
    this.player = player;
  }

}
