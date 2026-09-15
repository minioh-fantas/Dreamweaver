package me.minioh.firstPlugin.commands;

import me.minioh.firstPlugin.FirstPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SuperStrongEffect implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String args[]){
        FirstPlugin plugin = FirstPlugin.getInstance();
        if (sender instanceof Player p){
            if (args.length == 0){
                p.sendMessage(ChatColor.GOLD + "You was blessed by your own");
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 2000, 2000));
            }
            else{
                String playername = args[0];
                Player target = plugin.getServer().getPlayerExact(playername);
                if (target == null){
                    p.sendMessage(ChatColor.RED + "This player is not online");
                }
                else{
                    target.sendMessage(ChatColor.GOLD + "Feel the absolutely power!!");
                    target.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 2000, 2000));
                    p.sendMessage(ChatColor.LIGHT_PURPLE + "You have grant absolutely streng to" + ChatColor.GREEN + " " + playername);
                }
            }
        }

        return true;
    }
}
