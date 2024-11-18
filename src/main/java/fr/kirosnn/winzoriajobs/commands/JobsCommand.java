package fr.kirosnn.winzoriajobs.commands;

import fr.kirosnn.winzoriajobs.WinzoriaJobs;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JobsCommand implements CommandExecutor {

    private final WinzoriaJobs plugin;

    public JobsCommand(WinzoriaJobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                plugin.getJobsGUI().openJobsGUI(player, "default");
            } else {
                sender.sendMessage(plugin.getLangManager().getMessage("commands.jobs.player-only", "Erreur, message non défini dans le lang.yml"));
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("winzoriajobs.reload")) {
                sender.sendMessage(plugin.getLangManager().getMessage("commands.jobs.no-permission", "Erreur, message non défini dans le lang.yml"));
                return true;
            }

            plugin.reloadConfig();
            sender.sendMessage(plugin.getLangManager().getMessage("commands.jobs.reload", "Erreur, message non défini dans le lang.yml"));
            return true;
        }

        sender.sendMessage(plugin.getLangManager().getMessage("commands.jobs.invalid", "Erreur, message non défini dans le lang.yml"));
        return true;
    }
}
