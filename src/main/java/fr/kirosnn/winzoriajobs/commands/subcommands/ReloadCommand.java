package fr.kirosnn.winzoriajobs.commands.subcommands;

import fr.kirosnn.winzoriajobs.WinzoriaJobs;
import fr.kirosnn.winzoriajobs.commands.SubCommand;
import fr.kirosnn.winzoriajobs.files.LangManager;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements SubCommand {

    private final WinzoriaJobs plugin;
    private final LangManager langManager;

    public ReloadCommand(WinzoriaJobs plugin) {
        this.plugin = plugin;
        this.langManager = new LangManager(plugin);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        plugin.reloadConfig();
        langManager.reloadLang();
        sender.sendMessage(plugin.getLangManager().getMessage("commands.jobs.reload", "Erreur, message non défini dans le lang.yml"));
    }
}
