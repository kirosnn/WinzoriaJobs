package fr.kirosnn.winzoriajobs.items;

import fr.kirosnn.winzoriajobs.WinzoriaJobs;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class FarmerHoe implements Listener {

    private final WinzoriaJobs plugin;
    private final HashMap<UUID, UUID> protectedItems = new HashMap<>();

    public FarmerHoe(WinzoriaJobs plugin) {
        this.plugin = plugin;
    }

    public ItemStack createFarmerHoe() {
        String path = "items.farmerhoe";
        String materialName = plugin.getConfig().getString(path + ".type", "WOOD_HOE");
        Material material;

        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            material = Material.GOLD_HOE;
            plugin.getLogger().warning("Matériau invalide dans la configuration pour " + path + ".type: " + materialName + ". Utilisation de WOOD_HOE par défaut.");
        }

        String name = plugin.getConfig().getString(path + ".name", "&6Houe du Farmer");
        List<String> lore = plugin.getConfig().getStringList(path + ".lore");
        ConfigurationSection enchantmentsSection = plugin.getConfig().getConfigurationSection(path + ".enchantments");
        boolean unbreakable = plugin.getConfig().getBoolean(path + ".unbreakable", false);

        ItemStack hoe = new ItemStack(material);
        ItemMeta meta = hoe.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            meta.setLore(lore.stream()
                    .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                    .collect(Collectors.toList()));

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
            hoe.setItemMeta(meta);
        }

        return hoe;
    }

    public void giveFarmerHoe(Player player) {
        ItemStack farmerHoe = createFarmerHoe();

        HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(farmerHoe);
        if (!leftovers.isEmpty()) {
            Item droppedItem = player.getWorld().dropItemNaturally(player.getLocation(), farmerHoe);
            protectedItems.put(droppedItem.getUniqueId(), player.getUniqueId());
            player.sendMessage("§aVotre inventaire est plein ! La houe a été droppée à vos pieds.");
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Item item = event.getItem();
        Player player = event.getPlayer();

        UUID itemUUID = item.getUniqueId();
        if (protectedItems.containsKey(itemUUID)) {
            UUID ownerUUID = protectedItems.get(itemUUID);

            if (!player.getUniqueId().equals(ownerUUID)) {
                event.setCancelled(true);
            } else {
                protectedItems.remove(itemUUID);
            }
        }
    }
}
