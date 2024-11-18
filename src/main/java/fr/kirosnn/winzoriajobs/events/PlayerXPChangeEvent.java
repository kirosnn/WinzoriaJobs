package fr.kirosnn.winzoriajobs.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerXPChangeEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final String job;
    private final int oldLevel;
    private final int newLevel;
    private final int oldTier;
    private final int newTier;

    public PlayerXPChangeEvent(Player player, String job, int oldLevel, int newLevel, int oldTier, int newTier) {
        this.player = player;
        this.job = job;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
        this.oldTier = oldTier;
        this.newTier = newTier;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    public Player getPlayer() {
        return player;
    }

    public String getJob() {
        return job;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public int getOldTier() {
        return oldTier;
    }

    public int getNewTier() {
        return newTier;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }
}
