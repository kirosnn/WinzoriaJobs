package fr.kirosnn.winzoriajobs.utils;

import fr.kirosnn.winzoriajobs.WinzoriaJobs;
import fr.kirosnn.winzoriajobs.files.DatabaseManager;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class BossbarManager {

    private static final long BOSSBAR_TIMEOUT = 2000;
    private final WinzoriaJobs plugin;
    private final Map<Player, BossBar> playerBossBars = new HashMap<>();
    private final Map<Player, Long> lastUpdateTimes = new HashMap<>();

    public BossbarManager(WinzoriaJobs plugin) {
        this.plugin = plugin;
        startCleanupTask();
    }

    public void createOrUpdateBossBar(Player player, String jobType, int playerLevel, int currentXP, int nextXP, int tier) {
        String playerName = player.getName();
        DatabaseManager.PlayerJobData jobData = plugin.getDatabaseManager().getPlayerJobData(playerName, jobType);

        if (jobData == null) {
            plugin.getLogger().warning("Impossible de récupérer les données de job pour " + playerName);
            return;
        }

        String langKey = jobType.equalsIgnoreCase("hunter") ? "hunter-bossbar" : "farmer-bossbar";
        String barTextTemplate = plugin.getLangManager().getMessage(langKey,
                "{job} | Niveau: {level} | XP: {current_xp}/{next_xp} | Tier: {tier}");

        String barText = barTextTemplate
                .replace("{job}", jobType.equalsIgnoreCase("hunter") ? "Hunter" : "Farmer")
                .replace("{level}", String.valueOf(jobData.getLevel()))
                .replace("{current_xp}", String.valueOf(jobData.getCurrentXP()))
                .replace("{next_xp}", String.valueOf(jobData.getNextXP()))
                .replace("{tier}", String.valueOf(jobData.getTier()));

        TextComponent text = Component.text(barText)
                .color(NamedTextColor.WHITE)
                .decoration(TextDecoration.ITALIC, false);

        float progress = (float) jobData.getCurrentXP() / jobData.getNextXP();
        progress = Math.min(progress, 1.0f);

        BossBar bossBar = playerBossBars.get(player);
        if (bossBar == null) {
            bossBar = BossBar.bossBar(text,
                    progress,
                    jobType.equalsIgnoreCase("hunter") ? BossBar.Color.RED : BossBar.Color.GREEN,
                    BossBar.Overlay.PROGRESS);
            playerBossBars.put(player, bossBar);
            BossBar finalBossBar = bossBar;
            plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getAdventure().player(player).showBossBar(finalBossBar));
        } else {
            bossBar.name(text);
            bossBar.progress(progress);
            bossBar.color(jobType.equalsIgnoreCase("hunter") ? BossBar.Color.RED : BossBar.Color.GREEN);
        }

        lastUpdateTimes.put(player, System.currentTimeMillis());
    }

    public void removeBossBar(Player player) {
        BossBar bossBar = playerBossBars.remove(player);
        lastUpdateTimes.remove(player);
        if (bossBar != null) {
            plugin.getAdventure().player(player).hideBossBar(bossBar);
        }
    }

    private void startCleanupTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();

            for (Map.Entry<Player, Long> entry : new HashMap<>(lastUpdateTimes).entrySet()) {
                Player player = entry.getKey();
                long lastUpdateTime = entry.getValue();

                if ((currentTime - lastUpdateTime) > BOSSBAR_TIMEOUT) {
                    removeBossBar(player);
                    lastUpdateTimes.remove(player);
                }
            }
        }, 20L, 20L);
    }
}