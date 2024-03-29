package land.face.arena;

import com.tealcube.minecraft.bukkit.facecore.utilities.MessageUtils;
import com.tealcube.minecraft.bukkit.shade.acf.PaperCommandManager;
import io.pixeloutlaw.minecraft.spigot.config.MasterConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import land.face.arena.commands.ArenaCommand;
import land.face.arena.data.ArenaInstance;
import land.face.arena.listeners.ArenaChestListener;
import land.face.arena.listeners.ArenaExitListener;
import land.face.arena.listeners.MobDropListener;
import land.face.arena.managers.ArenaManager;
import land.face.arena.managers.LootManager;
import land.face.arena.managers.RecordManager;
import land.face.arena.menu.ArenaRewardsMenu;
import land.face.strife.commands.InspectCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class StrifeArenaPlugin extends JavaPlugin {

  private static StrifeArenaPlugin instance;
  public static final DecimalFormat INT_FORMAT = new DecimalFormat("###,###,###,###");

  private ArenaManager arenaManager;
  private LootManager lootManager;
  private RecordManager recordManager;

  private ArenaRewardsMenu rewardsMenu;

  private MasterConfiguration settings;
  private PaperCommandManager commandManager;

  public static StrifeArenaPlugin getInstance() {
    return instance;
  }

  public StrifeArenaPlugin() {
    instance = this;
  }

  public void onEnable() {
    List<VersionedSmartYamlConfiguration> configurations = new ArrayList<>();
    VersionedSmartYamlConfiguration configYAML;
    configurations.add(configYAML = defaultSettingsLoad("config.yml"));

    for (VersionedSmartYamlConfiguration config : configurations) {
      if (config.update()) {
        getLogger().info("Updating " + config.getFileName());
      }
    }

    settings = MasterConfiguration.loadFromFiles(configYAML);

    arenaManager = new ArenaManager(this);
    lootManager = new LootManager(this);
    recordManager = new RecordManager(this);

    Bukkit.getPluginManager().registerEvents(new ArenaExitListener(this), this);
    Bukkit.getPluginManager().registerEvents(new ArenaChestListener(this), this);
    Bukkit.getPluginManager().registerEvents(new MobDropListener(this), this);

    rewardsMenu = new ArenaRewardsMenu();

    arenaManager.loadArenas();
    arenaManager.updateRecordUsernames();

    commandManager = new PaperCommandManager(this);

    commandManager.registerCommand(new ArenaCommand(this));
    commandManager.getCommandCompletions()
        .registerCompletion("arenas", c -> arenaManager.getArenaIds());

    Bukkit.getServer().getLogger().info("StrifeArena Enabled!");
  }

  public void onDisable() {
    commandManager.unregisterCommands();
    for (Player p : Bukkit.getOnlinePlayers()) {
      ArenaInstance instance = arenaManager.getInstance(p);
      if (instance != null) {
        p.teleport(instance.getArena().getExitLocation().asLocation());
        MessageUtils.sendMessage(p, "&e&oThe arena plugin was reloaded!! You were moved to the arena exit.");
      }
    }
    arenaManager.saveArenas();
    HandlerList.unregisterAll(this);
    Bukkit.getServer().getScheduler().cancelTasks(this);
    Bukkit.getServer().getLogger().info("StrifeArena Disabled!");
  }

  public MasterConfiguration getSettings() {
    return settings;
  }

  public ArenaManager getArenaManager() {
    return arenaManager;
  }

  public LootManager getLootManager() {
    return lootManager;
  }

  public RecordManager getRecordManager() {
    return recordManager;
  }

  public ArenaRewardsMenu getRewardsMenu() {
    return rewardsMenu;
  }

  private VersionedSmartYamlConfiguration defaultSettingsLoad(String name) {
    return new VersionedSmartYamlConfiguration(new File(getDataFolder(), name),
        getResource(name), VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
  }
}