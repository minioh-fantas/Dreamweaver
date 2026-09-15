package me.minioh.firstPlugin.commands;

import me.minioh.firstPlugin.FirstPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RepeatCommand implements CommandExecutor {
    @Override
    public boolean onCommand (CommandSender sender, Command command, String label, String[] args){
        if (sender instanceof Player){
            Player p = (Player) sender;

            if (args.length == 0){
                p.sendMessage(ChatColor.RED + "Misssing agruments when running this command, make sure you use the right syntax");
                p.sendMessage(ChatColor.RED + "try /kc <agruments>");
            }
            else if (args.length == 1){
                String name = args[0];
                p.sendMessage("Message" + name);
            }
            else{
                StringBuilder buidler = new StringBuilder();
                for(int i = 0; i< args.length; i++){
                    buidler.append(args[i]);
                    buidler.append(" ");
                }
                String finalbuilder = buidler.toString();
                finalbuilder = finalbuilder.stripTrailing();
                p.sendMessage("Message"+ finalbuilder);
            }
        }

        return true;
    }
}
