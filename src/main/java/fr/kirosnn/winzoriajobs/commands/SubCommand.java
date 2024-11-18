package fr.kirosnn.winzoriajobs.commands;

import org.bukkit.command.CommandSender;

public interface SubCommand {
    void execute(CommandSender sender, String[] args);
}
