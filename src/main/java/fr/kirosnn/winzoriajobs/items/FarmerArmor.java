package fr.kirosnn.winzoriajobs.items;

import fr.kirosnn.winzoriajobs.WinzoriaJobs;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class FarmerArmor implements Listener {

    private final WinzoriaJobs plugin;
    private final HashMap<UUID, UUID> protectedItems = new HashMap<>();

    public FarmerArmor(WinzoriaJobs plugin) {
        this.plugin = plugin;
    }

    public ItemStack createArmorPiece(String type) {
        String path = "items.farmer_armor." + type.toLowerCase();

        String materialName = plugin.getConfig().getString(path + ".type", "LEATHER_HELMET");
        Material material;
        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            material = Material.LEATHER_HELMET;
            plugin.getLogger().warning("Matériau invalide dans la configuration pour " + path + ".type: " + materialName + ". Utilisation de LEATHER_HELMET par défaut.");
        }

        String name = plugin.getConfig().getString(path + ".name", "&6Armure du Farmer");
        List<String> lore = plugin.getConfig().getStringList(path + ".lore");
        boolean unbreakable = plugin.getConfig().getBoolean(path + ".unbreakable", true);
        List<String> effectsConfig = plugin.getConfig().getStringList(path + ".effects");

        ItemStack armorPiece = new ItemStack(material);
        ItemMeta meta = armorPiece.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            List<String> loreWithColor = new ArrayList<>();
            for (String line : lore) {
                loreWithColor.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            meta.setLore(loreWithColor);

            for (String effectConfig : effectsConfig) {
                String[] parts = effectConfig.split(":");
                if (parts.length == 2) {
                    Enchantment enchantment = Enchantment.getByName(parts[0].toUpperCase());
                    if (enchantment != null) {
                        try {
                            int level = Integer.parseInt(parts[1]);
                            meta.addEnchant(enchantment, level, true);
                        } catch (NumberFormatException e) {
                            plugin.getLogger().warning("Effet invalide : " + effectConfig);
                        }
                    }
                }
            }

            meta.spigot().setUnbreakable(unbreakable);
            armorPiece.setItemMeta(meta);
        }

        return armorPiece;
    }

    public void giveFullFarmerArmor(Player player) {
        giveArmorPiece(player, "helmet");
        giveArmorPiece(player, "chestplate");
        giveArmorPiece(player, "leggings");
        giveArmorPiece(player, "boots");
    }

    private void giveArmorPiece(Player player, String type) {
        ItemStack armorPiece = createArmorPiece(type);

        HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(armorPiece);
        if (!leftovers.isEmpty()) {
            Item droppedItem = player.getWorld().dropItemNaturally(player.getLocation(), armorPiece);
            protectedItems.put(droppedItem.getUniqueId(), player.getUniqueId());
            player.sendMessage("§aVotre inventaire est plein ! L'armure a été droppée à vos pieds.");
        }
    }

    @EventHandler
    public void onPlayerPickupItem(org.bukkit.event.player.PlayerPickupItemEvent event) {
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
