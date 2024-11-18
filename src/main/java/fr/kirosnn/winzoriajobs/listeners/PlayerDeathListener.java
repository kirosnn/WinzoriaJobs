package fr.kirosnn.winzoriajobs.listeners;

import fr.kirosnn.winzoriajobs.WinzoriaJobs;
import fr.kirosnn.winzoriajobs.items.FarmerHoe;
import fr.kirosnn.winzoriajobs.items.HunterLame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDeathListener implements Listener {

    private final WinzoriaJobs plugin;
    private final FarmerHoe farmerHoe;
    private final HunterLame hunterLame;
    private final Map<UUID, Map<String, Integer>> playerItemsCount = new HashMap<>();

    public PlayerDeathListener(WinzoriaJobs plugin, FarmerHoe farmerHoe, HunterLame hunterLame) {
        this.plugin = plugin;
        this.farmerHoe = farmerHoe;
        this.hunterLame = hunterLame;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        Map<String, Integer> itemCounts = new HashMap<>();
        itemCounts.put("farmerhoe", 0);
        itemCounts.put("hunterlame", 0);

        ItemStack expectedHoe = farmerHoe.createFarmerHoe();
        ItemStack expectedLame = hunterLame.createHunterLame(player, 0);

        for (ItemStack item : player.getInventory()) {
            if (item != null) {
                if (item.isSimilar(expectedHoe)) {
                    itemCounts.put("farmerhoe", itemCounts.get("farmerhoe") + item.getAmount());
                    event.getDrops().remove(item);
                } else if (item.isSimilar(expectedLame)) {
                    itemCounts.put("hunterlame", itemCounts.get("hunterlame") + item.getAmount());
                    event.getDrops().remove(item);
                }
            }
        }

        if (itemCounts.get("farmerhoe") > 0 || itemCounts.get("hunterlame") > 0) {
            playerItemsCount.put(player.getUniqueId(), itemCounts);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (playerItemsCount.containsKey(playerId)) {
            Map<String, Integer> itemCounts = playerItemsCount.get(playerId);

            if (itemCounts.get("farmerhoe") > 0) {
                ItemStack hoe = farmerHoe.createFarmerHoe();
                hoe.setAmount(itemCounts.get("farmerhoe"));
                player.getInventory().addItem(hoe);
            }

            if (itemCounts.get("hunterlame") > 0) {
                int killCount = 0;
                ItemStack lame = hunterLame.createHunterLame(player, killCount);
                lame.setAmount(itemCounts.get("hunterlame"));
                player.getInventory().addItem(lame);
            }

            playerItemsCount.remove(playerId);
        }
    }
}
