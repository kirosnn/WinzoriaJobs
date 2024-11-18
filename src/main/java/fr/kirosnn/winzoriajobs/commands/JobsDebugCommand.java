package fr.kirosnn.winzoriajobs.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class JobsDebugCommand implements CommandExecutor {

    private final HashMap<UUID, Integer> taskMap = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Seul un joueur peut exécuter cette commande.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length < 5) {
            player.sendMessage("§cUsage: /jobsdebug <mobType> <world> <x> <y> <z>");
            return true;
        }

        String mobTypeName = args[0].toUpperCase();
        EntityType mobType = EntityType.fromName(mobTypeName);

        if (mobType == null || !mobType.isAlive()) {
            player.sendMessage("§cType de mob invalide ou non supporté : " + mobTypeName);
            return true;
        }

        World world = Bukkit.getWorld(args[1]);
        if (world == null) {
            player.sendMessage("§cMonde invalide : " + args[1]);
            return true;
        }

        double x, y, z;
        try {
            x = Double.parseDouble(args[2]);
            y = Double.parseDouble(args[3]);
            z = Double.parseDouble(args[4]);
        } catch (NumberFormatException e) {
            player.sendMessage("§cLes coordonnées doivent être des nombres valides.");
            return true;
        }

        Location spawnLocation = new Location(world, x, y, z);

        UUID playerUUID = player.getUniqueId();
        if (taskMap.containsKey(playerUUID)) {
            int taskId = taskMap.get(playerUUID);
            Bukkit.getScheduler().cancelTask(taskId);
            taskMap.remove(playerUUID);
            player.sendMessage("§aSpawn des mobs arrêté.");
        } else {
            int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
                    Bukkit.getPluginManager().getPlugin("WinzoriaJobs"),
                    () -> {
                        LivingEntity entity = (LivingEntity) world.spawnEntity(spawnLocation, mobType);
                        entity.setCustomName("§eMob de test");
                        entity.setCustomNameVisible(true);
                    },
                    0L,
                    5L
            );

            taskMap.put(playerUUID, taskId);
            player.sendMessage("§aSpawn des mobs démarré à " + spawnLocation + " pour le type " + mobType.name());
        }

        return true;
    }
}
