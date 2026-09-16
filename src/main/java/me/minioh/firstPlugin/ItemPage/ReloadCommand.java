package me.minioh.firstPlugin.ItemPage;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {

    private final MultiLorePlugin plugin;

    public ReloadCommand(MultiLorePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("multilores.admin")) {
                sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>You lack the required permissions to use this command."));
                return true;
            }

            ConfigManager.load(plugin);
            sender.sendMessage(MiniMessage.miniMessage().deserialize("<green>Successfully reloaded <white>mmomultilores.yml</white> configuration!"));
            return true;
        }

        sender.sendMessage(MiniMessage.miniMessage().deserialize("<red>Usage: /multilores reload"));
        return true;
    }
}