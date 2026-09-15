package me.minioh.firstPlugin.commands;

import me.minioh.firstPlugin.FirstPlugin;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetSpawn implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
        FirstPlugin plugin = FirstPlugin.getInstance();
        if(sender instanceof Player p){
            Location location = p.getLocation();
            plugin.getConfig().set("spawn", location);
            plugin.saveConfig();
            p.sendMessage("Spawn set, you can type /spawn to teleport directly to spawn.");
        }

        return true;
    }
}
