package me.minioh.firstPlugin.listeners;

import me.minioh.firstPlugin.FirstPlugin;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinLeaveListener implements Listener {
    FirstPlugin plugin = FirstPlugin.getInstance();
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        String joinmessage = plugin.getConfig().getString("join-message");
        joinmessage = joinmessage.replace("%player_name%", player.getName());
        if (joinmessage != null) {
            if (!player.hasPlayedBefore()) {
                player.sendMessage(ChatColor.DARK_GREEN + "Welcome back, traveller.");
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', joinmessage));
            }
        }
    }
    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event){
        Player player = event.getPlayer();
        event.setQuitMessage(ChatColor.RED + player.getName() + ChatColor.RED + "has gone to The Dream");
    }
}
