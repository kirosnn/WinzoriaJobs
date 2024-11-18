package fr.kirosnn.winzoriajobs.listeners;

import fr.kirosnn.winzoriajobs.WinzoriaJobs;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public class ItemProtectionListener implements Listener {

    private final WinzoriaJobs plugin;

    public ItemProtectionListener(WinzoriaJobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        Item item = event.getItem();

        if (item.hasMetadata("owner")) {
            List<MetadataValue> metadataValues = item.getMetadata("owner");
            for (MetadataValue value : metadataValues) {
                if (value.getOwningPlugin().equals(plugin)) {
                    String ownerUUID = value.asString();
                    if (!player.getUniqueId().toString().equals(ownerUUID)) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }
}
