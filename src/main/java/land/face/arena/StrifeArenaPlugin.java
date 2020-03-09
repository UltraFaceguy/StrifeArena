package land.face.arena;

import io.pixeloutlaw.minecraft.spigot.config.MasterConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedConfiguration;
import io.pixeloutlaw.minecraft.spigot.config.VersionedSmartYamlConfiguration;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import land.face.arena.commands.BaseCommand;
import land.face.arena.listeners.ArenaChestListener;
import land.face.arena.listeners.ArenaExitListener;
import land.face.arena.listeners.MobDropListener;
import land.face.arena.managers.ArenaManager;
import land.face.arena.managers.LootManager;
import land.face.arena.menu.ArenaRewardsMenu;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import se.ranzdo.bukkit.methodcommand.CommandHandler;

public class StrifeArenaPlugin extends JavaPlugin {

  private static StrifeArenaPlugin instance;
  public static final DecimalFormat INT_FORMAT = new DecimalFormat("#");
  public static final DecimalFormat ONE_DECIMAL = new DecimalFormat("#.#");

  private ArenaManager arenaManager;
  private LootManager lootManager;

  private ArenaRewardsMenu rewardsMenu;

  private MasterConfiguration settings;
  private VersionedSmartYamlConfiguration configYAML;

  private CommandHandler commandHandler;

  public static StrifeArenaPlugin getInstance() {
    return instance;
  }

  public void onEnable() {
    instance = this;

    List<VersionedSmartYamlConfiguration> configurations = new ArrayList<>();
    configurations.add(configYAML = defaultSettingsLoad("config.yml"));

    for (VersionedSmartYamlConfiguration config : configurations) {
      if (config.update()) {
        getLogger().info("Updating " + config.getFileName());
      }
    }

    settings = MasterConfiguration.loadFromFiles(configYAML);

    arenaManager = new ArenaManager();
    lootManager = new LootManager();

    Bukkit.getPluginManager().registerEvents(new ArenaExitListener(this), this);
    Bukkit.getPluginManager().registerEvents(new ArenaChestListener(this), this);
    Bukkit.getPluginManager().registerEvents(new MobDropListener(), this);

    rewardsMenu = new ArenaRewardsMenu();

    commandHandler = new CommandHandler(this);
    commandHandler.registerCommands(new BaseCommand(this));

    arenaManager.loadArenas();

    Bukkit.getServer().getLogger().info("StrifeArena Enabled!");
  }

  public void onDisable() {
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

  public ArenaRewardsMenu getRewardsMenu() {
    return rewardsMenu;
  }

  private VersionedSmartYamlConfiguration defaultSettingsLoad(String name) {
    return new VersionedSmartYamlConfiguration(new File(getDataFolder(), name),
        getResource(name), VersionedConfiguration.VersionUpdateType.BACKUP_AND_UPDATE);
  }
}