package fr.kirosnn.winzoriajobs.listeners;

import fr.kirosnn.winzoriajobs.WinzoriaJobs;
import fr.kirosnn.winzoriajobs.items.HunterLame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class HunterLameListener implements Listener {

    private final WinzoriaJobs plugin;

    public HunterLameListener(WinzoriaJobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        new HunterLame(plugin).handlePlayerKill(event);
    }
}
