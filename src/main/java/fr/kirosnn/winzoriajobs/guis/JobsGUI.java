package fr.kirosnn.winzoriajobs.guis;

import fr.kirosnn.winzoriajobs.WinzoriaJobs;
import fr.kirosnn.winzoriajobs.files.DatabaseManager;
import fr.kirosnn.winzoriajobs.utils.LevelCalculator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class JobsGUI implements Listener {

    private final WinzoriaJobs plugin;

    public JobsGUI(WinzoriaJobs plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openJobsGUI(Player player, String jobType) {
        String title = plugin.getConfig().getString("jobs-gui.title", "Jobs Menu");
        title = color(title);
        int size = plugin.getConfig().getInt("jobs-gui.size", 27);
        Inventory gui = Bukkit.createInventory(null, size, title);

        Map<String, Object> itemsConfig = plugin.getConfig().getConfigurationSection("jobs-gui.items").getValues(false);
        for (Map.Entry<String, Object> entry : itemsConfig.entrySet()) {
            String itemKey = entry.getKey();
            String path = "jobs-gui.items." + itemKey;

            int slot = plugin.getConfig().getInt(path + ".slot", -1);
            if (slot < 0 || slot >= size) continue;

            String materialName = plugin.getConfig().getString(path + ".material", "STONE");
            Material material = Material.matchMaterial(materialName);
            if (material == null) continue;

            String itemName = plugin.getConfig().getString(path + ".name", "&fDefault Item");
            List<String> itemLore = plugin.getConfig().getStringList(path + ".lore");

            DatabaseManager.PlayerJobData jobData = plugin.getDatabaseManager().getPlayerJobData(player.getName(), itemKey);
            int level = jobData != null ? jobData.getLevel() : 0;
            int xp = jobData != null ? jobData.getCurrentXP() : 0;
            int xpLevel = LevelCalculator.getXPForNextLevel(level);
            int tier = jobData != null ? jobData.getTier() : 0;

            itemName = replacePlaceholders(itemName, level, xp, xpLevel, tier);
            itemName = color(itemName);

            for (int i = 0; i < itemLore.size(); i++) {
                itemLore.set(i, replacePlaceholders(itemLore.get(i), level, xp, xpLevel, tier));
                itemLore.set(i, color(itemLore.get(i)));
            }

            ItemStack itemStack = new ItemStack(material);
            ItemMeta meta = itemStack.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(itemName);
                meta.setLore(itemLore);
                itemStack.setItemMeta(meta);
            }

            gui.setItem(slot, itemStack);
        }

        player.openInventory(gui);
    }

    private String replacePlaceholders(String text, int level, int xp, int xpLevel, int tier) {
        return text.replace("{level}", String.valueOf(level))
                .replace("{xp}", String.valueOf(xp))
                .replace("{xp_level}", String.valueOf(xpLevel))
                .replace("{tier}", String.valueOf(tier));
    }

    private String color(String text) {
        return text.replace("&", "§");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryView view = event.getView();
        String guiTitle = color(plugin.getConfig().getString("jobs-gui.title", "Jobs Menu"));

        if (view.getTitle().equals(guiTitle)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String guiTitle = color(plugin.getConfig().getString("jobs-gui.title", "Jobs Menu"));

        if (event.getView().getTitle().equals(guiTitle)) {
            event.setCancelled(true);
        }
    }
}
