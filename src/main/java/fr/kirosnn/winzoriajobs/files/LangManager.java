package fr.kirosnn.winzoriajobs.files;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class LangManager {

    private final JavaPlugin plugin;
    private FileConfiguration langConfig;
    private final File langFile;

    public LangManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.langFile = new File(plugin.getDataFolder(), "lang.yml");
        loadLang();
    }

    public void loadLang() {
        if (!langFile.exists()) {
            plugin.getLogger().info("Le fichier lang.yml n'existe pas, création d'un nouveau fichier...");
            plugin.saveResource("lang.yml", false);
        }
        langConfig = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getMessage(String path, String defaultValue) {
        String message = langConfig.getString(path, defaultValue);
        return ChatColor.translateAlternateColorCodes('&', message != null ? message : defaultValue);
    }

    public void reloadLang() {
        loadLang();
        plugin.getLogger().info(ChatColor.GREEN + "Lang.yml rechargé !");
    }
}
