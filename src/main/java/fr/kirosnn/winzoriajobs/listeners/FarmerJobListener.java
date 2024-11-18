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

    public FarmerJobListener(WinzoriaJobs plugin, Economy economy, Harvester harvester) {
        this.plugin = plugin;
        this.economy = economy;
        this.harvester = harvester;
        this.placedBlocks = new HashMap<>();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack itemInHand = player.getItemInHand();

        // Debug: Log when the event starts
        plugin.getLogger().info("[DEBUG] BlockBreakEvent triggered by: " + player.getName() + " on block: " + block.getType());


        if (harvester.isCustomHarvester(itemInHand, "level1") || harvester.isCustomHarvester(itemInHand, "level2")) {
            // Determine the range of the Harvester
            int range = determineRange(itemInHand);

            // Debug: Log the detected range and Harvester level
            plugin.getLogger().info("[DEBUG] Using Harvester with range: " + range);

            harvester.applyHarvesterEffect(block, range, player, false);

            harvester.reduceHarvesterDurability(itemInHand, player);

            if (isMaturePlant(block)) {
                handleBlockBreakReward(player, block);

                // Debug: Log that rewards were granted for a mature plant
                plugin.getLogger().info("[DEBUG] Rewards granted for breaking mature plant: " + block.getType());
            }


            event.setCancelled(true);

            // Debug: Log that the event was canceled
            plugin.getLogger().info("[DEBUG] BlockBreakEvent canceled for Harvester usage.");
        } else if (isMaturePlant(block)) {

            block.breakNaturally();
            handleBlockBreakReward(player, block);

            // Debug: Log that rewards were granted for a non-Harvester break
            plugin.getLogger().info("[DEBUG] Rewards granted for breaking mature plant without Harvester: " + block.getType());
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        ItemStack itemInHand = player.getItemInHand();

        player.sendMessage("Bloc placé :" + event.getBlock()); // debug

        if (harvester.isCustomHarvester(itemInHand, "level1") || harvester.isCustomHarvester(itemInHand, "level2")) {
            String level = harvester.isCustomHarvester(itemInHand, "level2") ? "level2" : "level1";
            int range = determineRange(itemInHand);

            harvester.applyHarvesterEffect(block, range, player, true);
            harvester.reduceHarvesterDurability(itemInHand, player);
            player.sendMessage("Utilisation d'un harvester personnalisée pour placer le bloc."); // debug
            return;
        }

        if (isSeedMaterial(block.getType())) {
            placedBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                    .put(block.getLocation(), block.getType());
            handleBlockPlaceReward(player);
            player.sendMessage("Bloc de graine placé, récompenses appliquées."); // debug
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
                player.sendMessage("Craft réussi : récompenses appliquées !"); // debug
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
            player.sendMessage("Interaction réussie avec une entité, récompenses appliquées."); // debug
        }
    }

    private boolean isSeedMaterial(Material material) {
        return material == Material.SEEDS || material == Material.CARROT ||
                material == Material.POTATO || material == Material.MELON_SEEDS ||
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
