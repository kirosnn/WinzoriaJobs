package fr.kirosnn.winzoriajobs.listeners;

import fr.kirosnn.winzoriajobs.WinzoriaJobs;
import fr.kirosnn.winzoriajobs.items.Harvester;
import fr.kirosnn.winzoriajobs.utils.JobBonus;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class FarmerJobListener implements Listener {

    private final WinzoriaJobs plugin;
    private final Economy economy;
    private final Harvester harvester;
    private final HashMap<UUID, HashMap<Location, Material>> placedBlocks;

    public FarmerJobListener(WinzoriaJobs plugin, Economy economy, Harvester harvester) { // correction
        this.plugin = plugin;
        this.economy = economy;
        this.harvester = harvester; // addition
        this.placedBlocks = new HashMap<>();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack itemInHand = player.getItemInHand();

        if (harvester.isCustomHarvester(itemInHand, "level1") || harvester.isCustomHarvester(itemInHand, "level2")) { // correction
            harvester.applyHarvesterEffect(block, determineRange(itemInHand), player, false); // correction
            harvester.reduceHarvesterDurability(itemInHand, player); // correction
            event.setCancelled(true);
        } else if (isMaturePlant(block)) {
            block.breakNaturally();
            handleBlockBreakReward(player, block);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack itemInHand = player.getItemInHand();

        if (harvester.isCustomHarvester(itemInHand, "level1") || harvester.isCustomHarvester(itemInHand, "level2")) { // correction
            String level = harvester.isCustomHarvester(itemInHand, "level2") ? "level2" : "level1"; // correction
            int range = determineRange(itemInHand); // correction

            harvester.applyHarvesterEffect(block, range, player, true); // correction
            harvester.reduceHarvesterDurability(itemInHand, player); // correction
            return;
        }


        if (isSeedMaterial(block.getType())) { // correction
            placedBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                    .put(block.getLocation(), block.getType());
            handleBlockPlaceReward(player); // correction
        }
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getRecipe().getResult();
            Material material = item.getType();

            double baseXP = 0;
            double baseMoney = 0;

            if (material == Material.CAKE || material == Material.MUSHROOM_SOUP || material == Material.GOLDEN_APPLE) {
                baseXP = 10;
                baseMoney = 10;
            } else if (material == Material.BREAD) {
                baseXP = 6;
                baseMoney = 6;
            } else if (material == Material.GOLDEN_CARROT) {
                baseXP = 7;
                baseMoney = 7;
            }

            if (baseXP > 0 && baseMoney > 0) {
                int playerLevel = plugin.getDatabaseManager().getPlayerLevel(player.getName(), "farmer");
                int currentXP = plugin.getDatabaseManager().getCurrentXP(player.getName(), "farmer");
                int nextXP = plugin.getDatabaseManager().getXPForNextLevel(playerLevel);
                int tier = plugin.getDatabaseManager().getPlayerTier(player.getName(), "farmer");

                double finalXP = JobBonus.applyBonus(baseXP, playerLevel);
                double finalMoney = JobBonus.applyBonus(baseMoney, playerLevel);

                plugin.getDatabaseManager().addXP(player.getName(), "farmer_xp", (int) finalXP);
                economy.depositPlayer(player, finalMoney);
                plugin.getBossbarManager().createOrUpdateBossBar(player, "farmer", playerLevel, currentXP, nextXP, tier);
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (event.getRightClicked().getType() == EntityType.COW || event.getRightClicked().getType() == EntityType.SHEEP) {
            int playerLevel = plugin.getDatabaseManager().getPlayerLevel(player.getName(), "farmer");
            int currentXP = plugin.getDatabaseManager().getCurrentXP(player.getName(), "farmer");
            int nextXP = plugin.getDatabaseManager().getXPForNextLevel(playerLevel);
            int tier = plugin.getDatabaseManager().getPlayerTier(player.getName(), "farmer");

            double baseXP = 10;
            double baseMoney = 4;

            double finalXP = JobBonus.applyBonus(baseXP, playerLevel);
            double finalMoney = JobBonus.applyBonus(baseMoney, playerLevel);

            plugin.getDatabaseManager().addXP(player.getName(), "farmer_xp", (int) finalXP);
            economy.depositPlayer(player, finalMoney);
            plugin.getBossbarManager().createOrUpdateBossBar(player, "farmer", playerLevel, currentXP, nextXP, tier);
        }
    }

    private boolean isSeedMaterial(Material material) {
        return material == Material.SEEDS || material == Material.CARROT_ITEM ||
                material == Material.POTATO_ITEM || material == Material.MELON_SEEDS ||
                material == Material.PUMPKIN_SEEDS || material == Material.NETHER_STALK;
    }

    private boolean isMaturePlant(Block block) {
        if (block.getType() == Material.CROPS) {
            return block.getData() == 7;
        } else if (block.getType() == Material.CARROT || block.getType() == Material.POTATO) {
            return block.getData() == 7;
        } else if (block.getType() == Material.MELON_BLOCK || block.getType() == Material.PUMPKIN) {
            return true;
        } else if (block.getType() == Material.NETHER_WARTS) {
            return block.getData() == 3;
        }
        return false;
    }

    private void handleBlockBreakReward(Player player, Block block) {
        int playerLevel = plugin.getDatabaseManager().getPlayerLevel(player.getName(), "farmer");
        int currentXP = plugin.getDatabaseManager().getCurrentXP(player.getName(), "farmer");
        int nextXP = plugin.getDatabaseManager().getXPForNextLevel(playerLevel);
        int tier = plugin.getDatabaseManager().getPlayerTier(player.getName(), "farmer");

        double baseXP = 2;
        double baseMoney = 2.5;

        double finalXP = JobBonus.applyBonus(baseXP, playerLevel);
        double finalMoney = JobBonus.applyBonus(baseMoney, playerLevel);

        plugin.getDatabaseManager().addXP(player.getName(), "farmer_xp", (int) finalXP);
        economy.depositPlayer(player, finalMoney);
        plugin.getBossbarManager().createOrUpdateBossBar(player, "farmer", playerLevel, currentXP, nextXP, tier);
    }

    private void handleBlockPlaceReward(Player player) {
        int playerLevel = plugin.getDatabaseManager().getPlayerLevel(player.getName(), "farmer");
        int currentXP = plugin.getDatabaseManager().getCurrentXP(player.getName(), "farmer");
        int nextXP = plugin.getDatabaseManager().getXPForNextLevel(playerLevel);
        int tier = plugin.getDatabaseManager().getPlayerTier(player.getName(), "farmer");

        double baseXP = 1.0;
        double baseMoney = 1.0;

        double finalXP = JobBonus.applyBonus(baseXP, playerLevel);
        double finalMoney = JobBonus.applyBonus(baseMoney, playerLevel);

        plugin.getDatabaseManager().addXP(player.getName(), "farmer_xp", (int) finalXP);
        economy.depositPlayer(player, finalMoney);

        plugin.getBossbarManager().createOrUpdateBossBar(player, "farmer", playerLevel, currentXP, nextXP, tier);
    }

    private int determineRange(ItemStack item) { // addition
        String level = harvester.getHarvesterLevel(item.getItemMeta()); // addition
        if (level != null) {
            return level.equals("level1") ? 1 : 2; // addition
        }
        return 1; // addition
    }
}