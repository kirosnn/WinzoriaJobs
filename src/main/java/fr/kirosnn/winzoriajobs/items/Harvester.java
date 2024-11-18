package fr.kirosnn.winzoriajobs.items;

import fr.kirosnn.winzoriajobs.WinzoriaJobs;
import fr.kirosnn.winzoriajobs.files.DatabaseManager;
import fr.kirosnn.winzoriajobs.files.LangManager;
import fr.kirosnn.winzoriajobs.listeners.FarmerJobListener;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Harvester implements Listener {

    private final WinzoriaJobs plugin;
    private final LangManager langManager;
    private final DatabaseManager databaseManager;

    public Harvester(WinzoriaJobs plugin, LangManager langManager, DatabaseManager databaseManager, FarmerJobListener farmerJobListener) {
        this.plugin = plugin;
        this.langManager = langManager;
        this.databaseManager = databaseManager;
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

    public void giveHarvester(Player player, String level) {
        ItemStack harvester = createHarvester(level);
        player.getInventory().addItem(harvester);
    }

    @EventHandler
    public void onPlayerUseHarvester(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.WOOD_HOE) return;

        if (!isCustomHarvester(item, "level1") && !isCustomHarvester(item, "level2")) {
            return;
        }

        Player player = event.getPlayer();
        int farmerLevel = databaseManager.getPlayerLevel(player.getName(), "farmer");

        if (farmerLevel < 5) {
            event.setCancelled(true);
            String noLevelMessage = langManager.getMessage("harvester.no-level", "&cVous devez être au moins niveau 5 Farmer pour utiliser cet outil !");
            player.sendMessage(noLevelMessage);
            return;
        }

        Action action = event.getAction();
        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null) return;

        String level = getHarvesterLevel(item.getItemMeta());
        if (level == null) return;

        int range = level.equals("level1") ? 1 : (level.equals("level2") ? 2 : 1);

        if (action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            applyHarvesterEffect(clickedBlock, range, player, true);
            reduceHarvesterDurability(item, player);
        } else if (action == Action.LEFT_CLICK_BLOCK) {
            if (isPlantation(clickedBlock.getType())) {
                event.setCancelled(true);
                applyHarvesterEffect(clickedBlock, range, player, false);
                reduceHarvesterDurability(item, player);
            }
        }
    }

    private void reduceHarvesterDurability(ItemStack item, Player player) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return;

        List<String> lore = meta.getLore();
        if (lore == null || lore.isEmpty()) return;

        int index = -1;
        int remainingUses = 0;

        String durabilityMessage = ChatColor.stripColor(langManager.getMessage("harvester.durability", "Utilisations restantes : "));
        for (int i = 0; i < lore.size(); i++) {
            String line = ChatColor.stripColor(lore.get(i));
            if (line.startsWith(durabilityMessage)) {
                index = i;
                try {
                    remainingUses = Integer.parseInt(line.replace(durabilityMessage, "").trim());
                } catch (NumberFormatException e) {
                    return;
                }
                break;
            }
        }

        if (index == -1) return;

        remainingUses--;

        if (remainingUses <= 0) {
            player.getInventory().remove(item);
            String brokeMessage = langManager.getMessage("harvester.broke", "&cVotre Harvester s'est cassé !");
            player.sendMessage(brokeMessage);
            return;
        }

        String updatedDurability = langManager.getMessage("harvester.durability", "Utilisations restantes : ")
                + ChatColor.GREEN + remainingUses;
        lore.set(index, ChatColor.GRAY + updatedDurability);
        meta.setLore(lore);
        item.setItemMeta(meta);
    }

    private boolean canBeTilled(Material type) {
        return type == Material.DIRT || type == Material.GRASS;
    }

    private boolean isPlantation(Material type) {
        return type == Material.CROPS || type == Material.POTATO || type == Material.CARROT;
    }

    private ItemStack getSeedForPlantation(Material plantation) {
        switch (plantation) {
            case CROPS:
                return new ItemStack(Material.SEEDS, 1);
            case POTATO:
                return new ItemStack(Material.POTATO_ITEM, 1);
            case CARROT:
                return new ItemStack(Material.CARROT_ITEM, 1);
            default:
                return null;
        }
    }

    private String getHarvesterLevel(ItemMeta meta) {
        if (meta == null || !meta.hasLore()) {
            return null;
        }

        List<String> lore = meta.getLore();
        if (lore == null) {
            return null;
        }

        String levelPrefix = ChatColor.stripColor(langManager.getMessage("harvester.level", "Niveau: "));

        for (String line : lore) {
            String strippedLine = ChatColor.stripColor(line);
            if (strippedLine.startsWith(levelPrefix)) {
                String level = strippedLine.replace(levelPrefix, "").trim();
                return "lvl" + level;
            }
        }
        return null;
    }

    public void applyHarvesterEffect(Block centerBlock, int range, Player player, boolean isRightClick) {
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                Block targetBlock = centerBlock.getRelative(x, 0, z);

                if (isRightClick) {
                    if (canBeTilled(targetBlock.getType())) {
                        targetBlock.setType(Material.SOIL);
                    }
                } else if (isPlantation(targetBlock.getType())) {
                    Material cropType = targetBlock.getType();
                    targetBlock.breakNaturally(player.getInventory().getItemInHand());

                    ItemStack requiredSeed = getSeedForPlantation(cropType);
                    if (requiredSeed != null && player.getInventory().containsAtLeast(requiredSeed, 1)) {
                        player.getInventory().removeItem(requiredSeed);
                        targetBlock.setType(cropType);
                    }
                }
            }
        }
    }

    private boolean isCustomHarvester(ItemStack item, String level) {
        if (item == null || item.getType() != Material.WOOD_HOE) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.hasLore()) return false;

        String expectedName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("harvesters." + level + ".name", "&aHarvester"));
        List<String> expectedLoreConfig = plugin.getConfig().getStringList("harvesters." + level + ".lore");
        List<String> expectedLore = new ArrayList<>();
        for (String line : expectedLoreConfig) {
            expectedLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }

        if (!meta.getDisplayName().equals(expectedName)) return false;

        List<String> itemLore = meta.getLore();
        if (itemLore == null || itemLore.size() < expectedLore.size()) return false;
        for (int i = 0; i < expectedLore.size(); i++) {
            if (!itemLore.get(i).equals(expectedLore.get(i))) {
                return false;
            }
        }

        return true;
    }
}
