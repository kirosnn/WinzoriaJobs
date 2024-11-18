package fr.kirosnn.winzoriajobs.listeners;

import fr.kirosnn.winzoriajobs.WinzoriaJobs;
import fr.kirosnn.winzoriajobs.utils.JobBonus;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerFishEvent;

public class HunterJobListener implements Listener {

    private final WinzoriaJobs plugin;
    private final Economy economy;

    public HunterJobListener(WinzoriaJobs plugin, Economy economy) {
        this.plugin = plugin;
        this.economy = economy;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();
            EntityType entityType = event.getEntity().getType();

            double baseXP = 0;
            double baseMoney = 0;

            if (entityType == EntityType.ZOMBIE ||
                    entityType == EntityType.SKELETON ||
                    entityType == EntityType.SPIDER ||
                    entityType == EntityType.CREEPER ||
                    entityType == EntityType.ENDERMAN ||
                    entityType == EntityType.SLIME ||
                    entityType == EntityType.WITCH ||
                    entityType == EntityType.GHAST ||
                    entityType == EntityType.BLAZE ||
                    entityType == EntityType.MAGMA_CUBE ||
                    entityType == EntityType.PIG_ZOMBIE ||
                    entityType == EntityType.SILVERFISH ||
                    entityType == EntityType.ENDERMITE ||
                    entityType == EntityType.HORSE ||
                    entityType == EntityType.VILLAGER ||
                    entityType == EntityType.COW ||
                    entityType == EntityType.PIG ||
                    entityType == EntityType.SHEEP ||
                    entityType == EntityType.CHICKEN ||
                    entityType == EntityType.WOLF ||
                    entityType == EntityType.OCELOT ||
                    entityType == EntityType.BAT ||
                    entityType == EntityType.IRON_GOLEM ||
                    entityType == EntityType.SNOWMAN) {

                baseXP = 5;
                baseMoney = 2;
            } else if (entityType == EntityType.ENDER_DRAGON || entityType == EntityType.WITHER || entityType == EntityType.GUARDIAN) {
                baseXP = 5000;
                baseMoney = 10000;
            }

            if (baseXP > 0 && baseMoney > 0) {
                int playerLevel = plugin.getDatabaseManager().getPlayerLevel(player.getName(), "hunter");
                int currentXP = plugin.getDatabaseManager().getCurrentXP(player.getName(), "hunter");
                int nextXP = plugin.getDatabaseManager().getXPForNextLevel(playerLevel);
                int tier = plugin.getDatabaseManager().getPlayerTier(player.getName(), "hunter");

                double finalXP = JobBonus.applyBonus(baseXP, playerLevel);
                double finalMoney = JobBonus.applyBonus(baseMoney, playerLevel);

                plugin.getDatabaseManager().addXP(player.getName(), "hunter_xp", (int) finalXP);
                economy.depositPlayer(player, finalMoney);
                plugin.getBossbarManager().createOrUpdateBossBar(player, "hunter", playerLevel, currentXP, nextXP, tier);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (event.getEntity().getKiller() instanceof Player) {
            Player killer = event.getEntity().getKiller();

            double baseXP = 250;
            double baseMoney = 400;

            int playerLevel = plugin.getDatabaseManager().getPlayerLevel(killer.getName(), "hunter");
            int currentXP = plugin.getDatabaseManager().getCurrentXP(killer.getName(), "hunter");
            int nextXP = plugin.getDatabaseManager().getXPForNextLevel(playerLevel);
            int tier = plugin.getDatabaseManager().getPlayerTier(killer.getName(), "hunter");

            double finalXP = JobBonus.applyBonus(baseXP, playerLevel);
            double finalMoney = JobBonus.applyBonus(baseMoney, playerLevel);

            plugin.getDatabaseManager().addXP(killer.getName(), "hunter_xp", (int) finalXP);
            economy.depositPlayer(killer, finalMoney);
            plugin.getBossbarManager().createOrUpdateBossBar(killer, "hunter", playerLevel, currentXP, nextXP, tier);
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            Player player = event.getPlayer();

            double baseXP = 17;
            double baseMoney = 10;

            int playerLevel = plugin.getDatabaseManager().getPlayerLevel(player.getName(), "hunter");
            int currentXP = plugin.getDatabaseManager().getCurrentXP(player.getName(), "hunter");
            int nextXP = plugin.getDatabaseManager().getXPForNextLevel(playerLevel);
            int tier = plugin.getDatabaseManager().getPlayerTier(player.getName(), "hunter");

            double finalXP = JobBonus.applyBonus(baseXP, playerLevel);
            double finalMoney = JobBonus.applyBonus(baseMoney, playerLevel);

            plugin.getDatabaseManager().addXP(player.getName(), "hunter_xp", (int) finalXP);
            economy.depositPlayer(player, finalMoney);
            plugin.getBossbarManager().createOrUpdateBossBar(player, "hunter", playerLevel, currentXP, nextXP, tier);
        }
    }

    @EventHandler
    public void onFurnaceExtract(FurnaceExtractEvent event) {
        Player player = event.getPlayer();
        Material material = event.getItemType();

        if (material != Material.POTATO_ITEM &&
                material != Material.COOKED_BEEF &&
                material != Material.COOKED_CHICKEN &&
                material != Material.COOKED_MUTTON &&
                material != Material.COOKED_FISH &&
                material != Material.COOKED_RABBIT &&
                material != Material.BAKED_POTATO &&
                material != Material.COOKIE &&
                material != Material.CAKE &&
                material != Material.PUMPKIN_PIE) {

            double baseXP = 3;
            double baseMoney = 2;

            int playerLevel = plugin.getDatabaseManager().getPlayerLevel(player.getName(), "hunter");
            int currentXP = plugin.getDatabaseManager().getCurrentXP(player.getName(), "hunter");
            int nextXP = plugin.getDatabaseManager().getXPForNextLevel(playerLevel);
            int tier = plugin.getDatabaseManager().getPlayerTier(player.getName(), "hunter");

            double finalXP = JobBonus.applyBonus(baseXP, playerLevel);
            double finalMoney = JobBonus.applyBonus(baseMoney, playerLevel);

            plugin.getDatabaseManager().addXP(player.getName(), "hunter_xp", (int) finalXP);
            economy.depositPlayer(player, finalMoney);
            plugin.getBossbarManager().createOrUpdateBossBar(player, "hunter", playerLevel, currentXP, nextXP, tier);
        }
    }
}
