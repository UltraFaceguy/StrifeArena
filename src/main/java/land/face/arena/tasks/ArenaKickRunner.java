/**
 * The MIT License Copyright (c) 2015 Teal Cube Games
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package land.face.arena.tasks;

import com.tealcube.minecraft.bukkit.facecore.utilities.TextUtils;
import com.tealcube.minecraft.bukkit.facecore.utilities.TitleUtils;
import land.face.arena.StrifeArenaPlugin;
import land.face.arena.data.ArenaInstance;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class ArenaKickRunner extends BukkitRunnable {

  private final ArenaInstance instance;
  private int seconds = 60;
  private final String blank = "   " + ChatColor.GRAY + "   ";
  private final String secondLeft = TextUtils.color("&eArena closing in &f{0}s");
  private final String exitArena = TextUtils.color("&eExiting Arena!");

  public ArenaKickRunner(ArenaInstance instance) {
    this.instance = instance;
  }

  @Override
  public void run() {
    if (instance.getPlayer().isValid() && seconds > 0) {
      instance.getPlayer().sendTitle(blank,
          secondLeft.replace("{0}", String.valueOf(seconds)), 0, 30, 5);
      seconds--;
      return;
    }
    instance.getPlayer().sendTitle(blank, exitArena, 0, 30, 5);
    StrifeArenaPlugin.getInstance().getArenaManager().exitArena(instance.getPlayer(), true);
    cancel();
  }
}
