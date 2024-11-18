package fr.kirosnn.winzoriajobs.listeners;

import fr.kirosnn.winzoriajobs.files.DatabaseManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoin implements Listener {

    private final DatabaseManager databaseManager;

    public PlayerJoin(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        String playerName = event.getPlayer().getName();

        databaseManager.addPlayerIfNotExists(playerName);
    }
}
