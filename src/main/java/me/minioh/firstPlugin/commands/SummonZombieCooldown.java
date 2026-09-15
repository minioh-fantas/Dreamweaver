package me.minioh.firstPlugin.commands;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

import java.util.HashMap;
import java.util.UUID;

public class SummonZombieCooldown implements CommandExecutor {
    private final HashMap<UUID, Long> cooldown;
    public SummonZombieCooldown(){
        this.cooldown = new HashMap<>();
    }
    @Override
    public boolean onCommand (CommandSender sender, Command command, String label, String args[]){
        if (sender instanceof Player p){
            if (!this.cooldown.containsKey(p.getUniqueId())){
                this.cooldown.put(p.getUniqueId(), System.currentTimeMillis());
                p.sendMessage(ChatColor.GREEN + "You have summoned a Undead Soldier");
                Location spawnloc = p.getLocation();
                spawnloc.getWorld().spawnEntity(spawnloc, EntityType.ZOMBIE);
            }
            else {
                long timeToWait = System.currentTimeMillis() - cooldown.get(p.getUniqueId());
                if (timeToWait>=10000){
                    this.cooldown.put(p.getUniqueId(), System.currentTimeMillis());
                    p.sendMessage(ChatColor.GREEN + "You have summoned a Undead Soldier");
                    Location spawnloc = p.getLocation();
                    spawnloc.getWorld().spawnEntity(spawnloc, EntityType.ZOMBIE);
                }
                else {
                    p.sendMessage(ChatColor.RED + "You can summon a Zombie again in "+ (10000-timeToWait)+ "millisecond");
                }
            }

        }
        return true;
    }
}
