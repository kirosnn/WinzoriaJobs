package fr.kirosnn.winzoriajobs.items;

import fr.kirosnn.winzoriajobs.WinzoriaJobs;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.stream.Collectors;

public class HunterLame {

    private final WinzoriaJobs plugin;

    public HunterLame(WinzoriaJobs plugin) {
        this.plugin = plugin;
    }

    public ItemStack createHunterLame(Player player, int killCount) {
        String path = "items.hunterlame";
        String materialName = plugin.getConfig().getString(path + ".type", "GOLD_SWORD");
        Material material;

        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            material = Material.GOLD_SWORD;
            plugin.getLogger().warning("Matériau invalide dans la configuration pour " + path + ".type: " + materialName + ". Utilisation de GOLD_SWORD par défaut.");
        }

        String name = plugin.getConfig().getString(path + ".name", "&6Lame de %player%")
                .replace("%player%", player.getName())
                .replace("{kill}", String.valueOf(killCount));
        List<String> lore = plugin.getConfig().getStringList(path + ".lore").stream()
                .map(line -> line.replace("{kill}", String.valueOf(killCount)))
                .collect(Collectors.toList());
        boolean unbreakable = plugin.getConfig().getBoolean(path + ".unbreakable", true);

        ItemStack sword = new ItemStack(material);
        ItemMeta meta = sword.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            meta.setLore(lore.stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList()));

            ConfigurationSection enchantmentsSection = plugin.getConfig().getConfigurationSection(path + ".enchantments");
            if (enchantmentsSection != null) {
                for (String key : enchantmentsSection.getKeys(false)) {
                    Enchantment enchantment = Enchantment.getByName(key.toUpperCase());
                    if (enchantment != null) {
                        int level = enchantmentsSection.getInt(key);
                        meta.addEnchant(enchantment, level, true);
                    } else {
                        plugin.getLogger().warning("Enchantement invalide dans la configuration: " + key);
                    }
                }
            }

            meta.spigot().setUnbreakable(unbreakable);

            sword.setItemMeta(meta);
        }

        return sword;
    }

    public void giveHunterLame(Player player, int killCount) {
        ItemStack hunterLame = createHunterLame(player, killCount);
        player.getInventory().addItem(hunterLame);
    }

    public void handlePlayerKill(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        ItemStack itemInHand = killer.getItemInHand();
        if (itemInHand != null && itemInHand.hasItemMeta()) {
            ItemMeta meta = itemInHand.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.getLore();
                if (lore != null && lore.stream().anyMatch(line -> ChatColor.stripColor(line).contains("{kill}"))) {
                    int kills = getKillCounter(lore);
                    kills++;

                    ItemStack updatedSword = createHunterLame(killer, kills);
                    killer.setItemInHand(updatedSword);
                }
            }
        }
    }

    private int getKillCounter(List<String> lore) {
        for (String line : lore) {
            String stripped = ChatColor.stripColor(line);
            if (stripped.contains("{kill}")) {
                try {
                    return Integer.parseInt(stripped.replaceAll("[^0-9]", ""));
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }
}
