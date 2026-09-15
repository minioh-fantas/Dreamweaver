package me.minioh.firstPlugin.commands;

import me.minioh.firstPlugin.FirstPlugin;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TpSpawn implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
        FirstPlugin plugin = FirstPlugin.getInstance();
        if (sender instanceof Player p){
            Location location = plugin.getConfig().getLocation("spawn");
            if (location != null){
                p.teleport(location);
                p.sendMessage("You have teleport to the spawn");
            }
            else{
                p.sendMessage("Spawnpoint haven't set before");
            }
        }
        return true;
    }
}
