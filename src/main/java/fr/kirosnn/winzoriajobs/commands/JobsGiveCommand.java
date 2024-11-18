package fr.kirosnn.winzoriajobs.commands;

import fr.kirosnn.winzoriajobs.WinzoriaJobs;
import fr.kirosnn.winzoriajobs.items.FarmerArmor;
import fr.kirosnn.winzoriajobs.items.FarmerHoe;
import fr.kirosnn.winzoriajobs.items.Harvester;
import fr.kirosnn.winzoriajobs.items.HunterLame;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JobsGiveCommand implements CommandExecutor {

    private final WinzoriaJobs plugin;
    private final Harvester harvester;
    private final FarmerHoe farmerHoe;
    private final FarmerArmor farmerArmor;
    private final HunterLame hunterLame;

    public JobsGiveCommand(WinzoriaJobs plugin, Harvester harvester, FarmerHoe farmerHoe, FarmerArmor farmerArmor, HunterLame hunterLame) {
        this.plugin = plugin;
        this.harvester = harvester;
        this.farmerHoe = farmerHoe;
        this.farmerArmor = farmerArmor;
        this.hunterLame = hunterLame;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("winzoriajobs.give")) {
            sender.sendMessage(plugin.getLangManager().getMessage("commands.jobsgive.no-permission", "&cVous n'avez pas la permission d'exécuter cette commande."));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.getLangManager().getMessage("commands.jobsgive.usage", "&cUsage: /jobsgive <harvester|farmerhoe|farmerarmor|hunterlame> <nomdujoueur> [options]"));
            return true;
        }

        String itemType = args[0].toLowerCase();
        String playerName = args[1];

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage(plugin.getLangManager().getMessage("commands.jobsgive.player-not-found", "&cLe joueur &e{player} &cn'est pas en ligne.")
                    .replace("{player}", playerName));
            return true;
        }

        switch (itemType) {
            case "harvester":
                if (args.length != 3) {
                    sender.sendMessage(plugin.getLangManager().getMessage("commands.jobsgive.usage-harvester", "&cUsage: /jobsgive harvester <nomdujoueur> <level1|level2>"));
                    return true;
                }

                String level = args[2].toLowerCase();
                if (!level.equals("level1") && !level.equals("level2")) {
                    sender.sendMessage(plugin.getLangManager().getMessage("commands.jobsgive.invalid-level", "&cNiveau invalide. Utilisez 'level1' ou 'level2'."));
                    return true;
                }

                harvester.giveHarvester(target, level);
                sender.sendMessage(plugin.getLangManager().getMessage("commands.jobsgive.success", "&aHarvester de niveau {level} donné à &e{player}&a.")
                        .replace("{level}", level)
                        .replace("{player}", target.getName()));
                break;

            case "farmerhoe":
                farmerHoe.giveFarmerHoe(target);
                sender.sendMessage(plugin.getLangManager().getMessage("commands.jobsgive.success", "&aHoue du Farmer donnée à &e{player}&a.")
                        .replace("{player}", target.getName()));
                break;

            case "farmerarmor":
                farmerArmor.giveFullFarmerArmor(target);
                sender.sendMessage(plugin.getLangManager().getMessage("commands.jobsgive.success", "&aArmure du Farmer donnée à &e{player}&a.")
                        .replace("{player}", target.getName()));
                break;

            case "hunterlame":
                int killCount = 0;
                if (args.length >= 3) {
                    try {
                        killCount = Integer.parseInt(args[2]);
                        if (killCount < 0) throw new NumberFormatException();
                    } catch (NumberFormatException e) {
                        sender.sendMessage(plugin.getLangManager().getMessage("commands.jobsgive.invalid-killcount", "&cLe compteur de kills doit être un nombre entier positif."));
                        return true;
                    }
                }

                hunterLame.giveHunterLame(target, killCount);
                sender.sendMessage(plugin.getLangManager().getMessage("commands.jobsgive.success", "&aHunterLame donnée à &e{player}&a avec &e{kills}&a kills.")
                        .replace("{player}", target.getName())
                        .replace("{kills}", String.valueOf(killCount)));
                break;

            default:
                sender.sendMessage(plugin.getLangManager().getMessage("commands.jobsgive.invalid-item", "&cType d'objet invalide. Utilisez 'harvester', 'farmerhoe', 'farmerarmor' ou 'hunterlame'."));
                break;
        }

        return true;
    }
}
