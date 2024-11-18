package fr.kirosnn.winzoriajobs.commands;

import fr.kirosnn.winzoriajobs.WinzoriaJobs;
import fr.kirosnn.winzoriajobs.files.LangManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class UpgradeHarvesterCommand implements CommandExecutor {

    private final WinzoriaJobs plugin;
    private final LangManager langManager;
    private final Economy economy;

    public UpgradeHarvesterCommand(WinzoriaJobs plugin, LangManager langManager, Economy economy) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.economy = economy;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Seuls les joueurs peuvent utiliser cette commande.");
            return true;
        }

        Player player = (Player) sender;

        ItemStack item = player.getInventory().getItemInHand();
        if (item == null || item.getType() != Material.WOOD_HOE) {
            player.sendMessage(langManager.getMessage("upgradeharvester.not-holding", "&cVous devez tenir un Harvester pour l'améliorer !"));
            return true;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) {
            player.sendMessage(langManager.getMessage("upgradeharvester.not-holding", "&cVous devez tenir un Harvester pour l'améliorer !"));
            return true;
        }

        String currentLevel = getHarvesterLevel(item);
        if (currentLevel == null || !currentLevel.equals("level1")) {
            player.sendMessage(langManager.getMessage("upgradeharvester.invalid-item", "&cCet objet n'est pas un Harvester valide."));
            return true;
        }

        String nextLevel = getNextHarvesterLevel(currentLevel);
        if (nextLevel == null) {
            player.sendMessage(langManager.getMessage("upgradeharvester.max-level", "&aVotre Harvester est déjà au niveau maximum."));
            return true;
        }

        double cost = plugin.getConfig().getDouble("upgradeharvester.cost." + nextLevel, 1000.0);
        if (!economy.has(player, cost)) {
            player.sendMessage(langManager.getMessage("upgradeharvester.not-enough-money", "&cVous n'avez pas assez d'argent pour cette amélioration. Coût : &e" + cost + " &c!"));
            return true;
        }

        economy.withdrawPlayer(player, cost);
        upgradeHarvester(player, item, nextLevel);

        player.sendMessage(langManager.getMessage("upgradeharvester.success", "&aVotre Harvester a été amélioré au niveau &e" + nextLevel + "&a !"));
        return true;
    }

    private String getHarvesterLevel(ItemStack item) {
        if (isLoreMatching(item, "level1")) {
            return "level1";
        }
        if (isLoreMatching(item, "level2")) {
            return "level2";
        }
        return null;
    }


    private boolean isLoreMatching(ItemStack item, String level) {
        List<String> configuredLore = plugin.getConfig().getStringList("harvesters." + level + ".lore");
        List<String> translatedLore = new ArrayList<>();
        for (String line : configuredLore) {
            translatedLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return false;

        List<String> itemLore = meta.getLore();
        if (itemLore == null || itemLore.isEmpty()) return false;

        for (String loreLine : translatedLore) {
            if (!itemLore.contains(loreLine)) {
                return false;
            }
        }
        return true;
    }


    private String getNextHarvesterLevel(String currentLevel) {
        if ("level1".equalsIgnoreCase(currentLevel)) {
            return "level2";
        }
        return null;
    }

    private void upgradeHarvester(Player player, ItemStack oldItem, String nextLevel) {
        player.getInventory().remove(oldItem);

        ItemStack newHarvester = createHarvester(nextLevel);
        player.getInventory().addItem(newHarvester);
    }

    public ItemStack createHarvester(String level) {
        String basePath = "harvesters." + level;

        String name = plugin.getConfig().getString(basePath + ".name", "&aHarvester");
        List<String> loreConfig = plugin.getConfig().getStringList(basePath + ".lore");
        int maxUses = plugin.getConfig().getInt(basePath + ".max-uses", 100);

        List<String> lore = new ArrayList<>();
        for (String line : loreConfig) {
            lore.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        String durabilityMessage = langManager.getMessage("harvester.durability", "&7Utilisations restantes : &a{uses}");
        lore.add(durabilityMessage.replace("{uses}", String.valueOf(maxUses)));

        ItemStack harvester = new ItemStack(Material.WOOD_HOE);
        ItemMeta meta = harvester.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            meta.setLore(lore);

            List<String> enchantments = plugin.getConfig().getStringList(basePath + ".enchantments");
            for (String enchant : enchantments) {
                String[] parts = enchant.split(":");
                if (parts.length == 2) {
                    try {
                        Enchantment enchantment = Enchantment.getByName(parts[0].toUpperCase());
                        int levelValue = Integer.parseInt(parts[1]);
                        if (enchantment != null) {
                            meta.addEnchant(enchantment, levelValue, true);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Enchantement invalide: " + enchant);
                    }
                }
            }
            harvester.setItemMeta(meta);
        }

        return harvester;
    }
}
