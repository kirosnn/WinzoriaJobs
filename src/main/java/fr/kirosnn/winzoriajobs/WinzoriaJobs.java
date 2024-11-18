package fr.kirosnn.winzoriajobs;

import fr.kirosnn.winzoriajobs.commands.JobsCommand;
import fr.kirosnn.winzoriajobs.commands.JobsDebugCommand;
import fr.kirosnn.winzoriajobs.commands.JobsGiveCommand;
import fr.kirosnn.winzoriajobs.commands.UpgradeHarvesterCommand;
import fr.kirosnn.winzoriajobs.files.ConfigManager;
import fr.kirosnn.winzoriajobs.files.DatabaseManager;
import fr.kirosnn.winzoriajobs.files.LangManager;
import fr.kirosnn.winzoriajobs.guis.JobsGUI;
import fr.kirosnn.winzoriajobs.items.FarmerArmor;
import fr.kirosnn.winzoriajobs.items.FarmerHoe;
import fr.kirosnn.winzoriajobs.items.Harvester;
import fr.kirosnn.winzoriajobs.items.HunterLame;
import fr.kirosnn.winzoriajobs.listeners.*;
import fr.kirosnn.winzoriajobs.utils.BossbarManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class WinzoriaJobs extends JavaPlugin {

    private static Economy economy = null;
    private ConfigManager configManager;
    private LangManager langManager;
    private DatabaseManager databaseManager;
    private BossbarManager bossbarManager;
    private BukkitAudiences adventure;
    private JobsGUI jobsGUI;
    FarmerJobListener farmerJobListener;

    public static Economy getEconomy() {
        return economy;
    }

    @Override
    public void onEnable() {
        this.adventure = BukkitAudiences.create(this);
        this.jobsGUI = new JobsGUI(this);

        configManager = new ConfigManager(this);
        langManager = new LangManager(this);
        langManager = new LangManager(this);
        bossbarManager = new BossbarManager(this);
        databaseManager = new DatabaseManager(this);
        FarmerHoe farmerHoe = new FarmerHoe(this);
        FarmerArmor farmerAmor = new FarmerArmor(this);
        HunterLame hunterLame = new HunterLame(this);
        Harvester harvester = new Harvester(this, langManager, databaseManager, farmerJobListener);
        FarmerJobListener farmerJobListener = new FarmerJobListener(this, economy, harvester);

        databaseManager.setupSQLiteDatabase();

        if (!setupEconomy()) {
            getLogger().severe("Vault n'est pas configuré ! Désactivation du plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(new HunterJobListener(this, economy), this);
        Bukkit.getPluginManager().registerEvents(new FarmerJobListener(this, economy, harvester), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoin(databaseManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerProgressionListener(this), this);
        getServer().getPluginManager().registerEvents(new HunterLameListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ItemProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this, farmerHoe, hunterLame), this);
        getServer().getPluginManager().registerEvents(new Harvester(this, langManager, databaseManager, farmerJobListener), this);
        getServer().getPluginManager().registerEvents(new FarmerArmor(this), this);
        getServer().getPluginManager().registerEvents(jobsGUI, this);

        getCommand("jobs").setExecutor(new JobsCommand(this));
        getCommand("jobsgive").setExecutor(new JobsGiveCommand(this, harvester, farmerHoe, farmerAmor, hunterLame));
        getCommand("upgradeharvester").setExecutor(new UpgradeHarvesterCommand(this, langManager, economy));
        getCommand("jobsdebug").setExecutor(new JobsDebugCommand());

        getServer().getConsoleSender().sendMessage("");
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "▐▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▌");
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "▐██╗    ██╗██╗███╗   ██╗███████╗ ██████╗ ██████╗ ██╗ █████╗ ▌");
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "▐██║    ██║██║████╗  ██║╚══███╔╝██╔═══██╗██╔══██╗██║██╔══██╗▌");
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "▐██║ █╗ ██║██║██╔██╗ ██║  ███╔╝ ██║   ██║██████╔╝██║███████║▌");
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "▐██║███╗██║██║██║╚██╗██║ ███╔╝  ██║   ██║██╔══██╗██║██╔══██║▌");
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "▐╚███╔███╔╝██║██║ ╚████║███████╗╚██████╔╝██║  ██║██║██║  ██║▌");
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "▐ ╚══╝╚══╝ ╚═╝╚═╝  ╚═══╝╚══════╝ ╚═════╝ ╚═╝  ╚═╝╚═╝╚═╝  ╚═╝▌");
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "▐▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▌");
        getServer().getConsoleSender().sendMessage("");

    }

    @Override
    public void onDisable() {
        getServer().getOnlinePlayers().forEach(bossbarManager::removeBossBar);
        if (adventure != null) {
            adventure.close();
        }

        databaseManager.closeConnection();

        getServer().getConsoleSender().sendMessage("");
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "▐▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▌");
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "▐██╗    ██╗██╗███╗   ██╗███████╗ ██████╗ ██████╗ ██╗ █████╗ ▌");
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "▐██║    ██║██║████╗  ██║╚══███╔╝██╔═══██╗██╔══██╗██║██╔══██╗▌");
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "▐██║ █╗ ██║██║██╔██╗ ██║  ███╔╝ ██║   ██║██████╔╝██║███████║▌");
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "▐██║███╗██║██║██║╚██╗██║ ███╔╝  ██║   ██║██╔══██╗██║██╔══██║▌");
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "▐╚███╔███╔╝██║██║ ╚████║███████╗╚██████╔╝██║  ██║██║██║  ██║▌");
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "▐ ╚══╝╚══╝ ╚═╝╚═╝  ╚═══╝╚══════╝ ╚═════╝ ╚═╝  ╚═╝╚═╝╚═╝  ╚═╝▌");
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "▐▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▌");
        getServer().getConsoleSender().sendMessage("");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public LangManager getLangManager() {
        return langManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public BossbarManager getBossbarManager() {
        return bossbarManager;
    }

    public BukkitAudiences getAdventure() {
        return adventure;
    }

    public JobsGUI getJobsGUI() {
        return jobsGUI;
    }
}
