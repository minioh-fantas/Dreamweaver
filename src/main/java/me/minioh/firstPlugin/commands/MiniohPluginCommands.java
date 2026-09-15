package me.minioh.firstPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class MiniohPluginCommands implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){

        if(command.getName().equalsIgnoreCase("hjhj")){
            if (sender instanceof Player){
                Player p = (Player) sender;
                p.sendMessage(ChatColor.LIGHT_PURPLE + "OCCAK VCL");
                p.setHealth(0.0);
            } else if (sender instanceof ConsoleCommandSender) {
                System.out.println("The OCCHIM command cant run by the console!");
            } else if (sender instanceof BlockCommandSender) {
                System.out.println("The unknow player send the OCCHIM command by command block");
            }
        }

        return true;
    }
}
