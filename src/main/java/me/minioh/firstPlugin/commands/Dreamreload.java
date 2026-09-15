package me.minioh.firstPlugin.commands;

import me.minioh.firstPlugin.FirstPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Dreamreload implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        FirstPlugin plugin = FirstPlugin.getInstance();

        if (!sender.hasPermission("dreamweaver.reload")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }

        sender.sendMessage("§eReloading Dreamweaver Core...");

        long startTime = System.currentTimeMillis();

        try {
            plugin.reloadConfig();

            long endTime = System.currentTimeMillis();
            long reloadTime = endTime - startTime;

            sender.sendMessage("§a✓ Dreamweaver Core reloaded successfully!");
            sender.sendMessage("§7Reload time: " + reloadTime + "ms");

            if (sender instanceof Player) {
                plugin.getLogger().info("Config reloaded by player: " + sender.getName());
            } else {
                plugin.getLogger().info("Config reloaded by console");
            }

        } catch (Exception e) {
            sender.sendMessage("§c✗ Error reloading plugin: " + e.getMessage());
        }

        return true;
    }
}