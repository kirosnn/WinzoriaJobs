package fr.kirosnn.winzoriajobs.listeners;

import fr.kirosnn.winzoriajobs.WinzoriaJobs;
import fr.kirosnn.winzoriajobs.events.PlayerXPChangeEvent;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.ArrayList;
import java.util.List;

public class PlayerProgressionListener implements Listener {

    private final WinzoriaJobs plugin;
    private final Economy economy;

    public PlayerProgressionListener(WinzoriaJobs plugin) {
        this.plugin = plugin;
        this.economy = WinzoriaJobs.getEconomy();
    }

    @EventHandler
    public void onPlayerXPChange(PlayerXPChangeEvent event) {
        Player player = event.getPlayer();
        String job = event.getJob();
        int newLevel = event.getNewLevel();

        if (plugin.getConfig().contains("rewards." + job)) {
            handleLevelReward(player, job, newLevel);
        }
    }

    private void handleLevelReward(Player player, String job, int level) {
        ConfigurationSection rewardsSection = plugin.getConfig().getConfigurationSection("rewards." + job + ".level_" + level);
        if (rewardsSection == null) return;

        int money = rewardsSection.getInt("money", 0);
        if (money > 0) {
            economy.depositPlayer(player, money);
        }

        List<String> commands = rewardsSection.getStringList("commands");
        for (String command : commands) {
            String formattedCommand = command.replace("%player%", player.getName());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), formattedCommand);
        }

        List<ItemStack> rewards = new ArrayList<>();
        ConfigurationSection itemSection = rewardsSection.getConfigurationSection("item");
        if (itemSection != null) {
            ItemStack item = createItemFromConfig(itemSection);
            if (item != null) rewards.add(item);
        }

        ConfigurationSection itemsSection = rewardsSection.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection individualItemSection = itemsSection.getConfigurationSection(key);
                if (individualItemSection != null) {
                    ItemStack item = createItemFromConfig(individualItemSection);
                    if (item != null) rewards.add(item);
                }
            }
        }

        giveOrDropRewards(player, rewards);

        String rewardMessage = plugin.getLangManager().getMessage(
                "messages." + job + "_level_" + level + "_reward",
                "Erreur, message non défini dans le lang.yml"
        );
        player.sendMessage(rewardMessage);
    }

    private ItemStack createItemFromConfig(ConfigurationSection itemSection) {
        String materialName = itemSection.getString("type", "AIR");
        Material material = Material.getMaterial(materialName.toUpperCase());
        if (material == null) return null;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (itemSection.contains("name")) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemSection.getString("name")));
            }

            if (itemSection.contains("lore")) {
                List<String> lore = new ArrayList<>();
                for (String line : itemSection.getStringList("lore")) {
                    lore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(lore);
            }

            if (itemSection.contains("enchantments")) {
                ConfigurationSection enchantmentsSection = itemSection.getConfigurationSection("enchantments");
                for (String enchantName : enchantmentsSection.getKeys(false)) {
                    Enchantment enchantment = Enchantment.getByName(enchantName.toUpperCase());
                    if (enchantment != null) {
                        int level = enchantmentsSection.getInt(enchantName, 1);
                        meta.addEnchant(enchantment, level, true);
                    }
                }
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    @EventHandler
    public void onPlayerTierChange(PlayerXPChangeEvent event) {
        Player player = event.getPlayer();
        String job = event.getJob();
        int newTier = event.getNewTier();

        if (plugin.getConfig().contains("tiers." + job)) {
            handleTierReward(player, job, newTier);
        }
    }

    private void handleTierReward(Player player, String job, int tier) {
        ConfigurationSection tierSection = plugin.getConfig().getConfigurationSection("tiers." + job + ".tier_" + tier);
        if (tierSection == null) return;

        int money = tierSection.getInt("money", 0);
        if (money > 0) {
            economy.depositPlayer(player, money);
        }

        List<String> commands = tierSection.getStringList("commands");
        for (String command : commands) {
            String formattedCommand = command.replace("%player%", player.getName());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), formattedCommand);
        }

        List<ItemStack> rewards = new ArrayList<>();
        ConfigurationSection itemsSection = tierSection.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ConfigurationSection individualItemSection = itemsSection.getConfigurationSection(key);
                if (individualItemSection != null) {
                    ItemStack item = createItemFromConfig(individualItemSection);
                    if (item != null) rewards.add(item);
                }
            }
        }

        giveOrDropRewards(player, rewards);

        String rewardMessage = plugin.getLangManager().getMessage(
                "messages." + job + "_tier_" + tier + "_reward",
                "Erreur, message non défini dans le lang.yml"
        );
        player.sendMessage(rewardMessage);
    }

    private void giveOrDropRewards(Player player, List<ItemStack> rewards) {
        for (ItemStack reward : rewards) {
            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(reward);
            } else {
                Location dropLocation = player.getLocation();
                Item droppedItem = dropLocation.getWorld().dropItemNaturally(dropLocation, reward);
                droppedItem.setMetadata("owner", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
                droppedItem.setPickupDelay(0);
                String dropMessage = plugin.getLangManager().getMessage(
                        "messages.inventory_full_drop",
                        "Erreur, message non défini dans le lang.yml"
                );
                player.sendMessage(dropMessage);
            }
        }
    }
}
