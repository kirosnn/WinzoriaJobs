package fr.kirosnn.winzoriajobs.files;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class ConfigManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private final File configFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        loadConfig();
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            plugin.getLogger().info("Le fichier config.yml n'existe pas, création d'un nouveau fichier...");
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public String getString(String path, String defaultValue) {
        String message = config.getString(path, defaultValue);
        return ChatColor.translateAlternateColorCodes('&', message != null ? message : defaultValue);
    }

    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }

    public void set(String path, Object value) {
        config.set(path, value);
        saveConfig();
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Erreur lors de la sauvegarde du fichier config.yml : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        plugin.getLogger().info("Le fichier config.yml a été rechargé.");
    }

    public FileConfiguration getConfig() {
        return config;
    }
}
