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

import static org.bukkit.attribute.Attribute.GENERIC_FOLLOW_RANGE;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import land.face.arena.StrifeArenaPlugin;
import land.face.arena.data.ArenaInstance;
import land.face.arena.data.ArenaSpawn;
import land.face.arena.data.ArenaWave;
import land.face.arena.data.BasicLocation;
import land.face.arena.listeners.MobDropListener;
import land.face.strife.StrifePlugin;
import land.face.strife.data.StrifeMob;
import org.bukkit.Location;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

public class WaveRunner extends BukkitRunnable {

  private ArenaInstance instance;
  private ArenaWave wave;
  private Location location;
  private int index = 0;
  private int tasksExecuted = 0;
  private List<LivingEntity> summons = new CopyOnWriteArrayList<>();

  public WaveRunner(ArenaInstance instance, ArenaWave wave, BasicLocation location) {
    this.instance = instance;
    this.location = location.asLocation();
    this.wave = wave;
  }

  @Override
  public void run() {
    if (wave.getArenaTask().size() <= tasksExecuted) {
      clearSummons();
      if (summons.isEmpty()) {
        instance.doWaveEnd(instance.getPlayer());
        cancel();
      }
      return;
    }
    if (!wave.getArenaTask().containsKey(index)) {
      index++;
      return;
    }
    for (ArenaSpawn a : wave.getArenaTask().get(index)) {
      Location loc = location.clone().add(a.getOffset());
      for (int i = 0; i < a.getAmount(); i++) {
        StrifeMob mob = StrifePlugin.getInstance().getUniqueEntityManager()
            .spawnUnique(a.getMobId(), loc);

        AttributeInstance followRange = mob.getEntity().getAttribute(GENERIC_FOLLOW_RANGE);
        if (followRange != null) {
          double newVal = Math.max(Math.max(followRange.getBaseValue(),
              followRange.getDefaultValue()), 32);
          followRange.setBaseValue(newVal);
        }
        mob.getEntity().setMetadata(MobDropListener.ARENA_META, new FixedMetadataValue(
            StrifeArenaPlugin.getInstance(), true));
        if (mob.getEntity() instanceof Mob) {
          ((Mob) mob.getEntity()).setTarget(instance.getPlayer());
        }
        if (mob.getEntity().isValid()) {
          summons.add(mob.getEntity());
        }
      }
    }
    tasksExecuted++;
    index++;
  }

  private void clearSummons() {
    for (LivingEntity le : summons) {
      if (le == null || !le.isValid()) {
        summons.remove(le);
      }
    }
  }
}
