package land.face.arena.events;

import lombok.Getter;
import org.bukkit.entity.Player;

public class ArenaWaveCompleteEvent extends ArenaEvent {

  @Getter
  private final String arenaId;
  @Getter
  private final int wave;
  @Getter
  private final Player player;

  public ArenaWaveCompleteEvent(String arenaId, int wave, Player player) {
    this.arenaId = arenaId;
    this.wave = wave;
    this.player = player;
  }

}
