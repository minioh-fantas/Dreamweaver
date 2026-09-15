package me.minioh.firstPlugin.listeners;

import me.minioh.firstPlugin.FirstPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;

public class PlayerDeathListener implements Listener {
    FirstPlugin plugin = FirstPlugin.getInstance();
    private final HashMap<UUID, Integer> corruptionLevels = new HashMap<>();
    private final HashMap<UUID, Integer> corruptionTask = new HashMap<>();
    @EventHandler
    public void onPlayerDeath (PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        String playerName = player.getName();
        if(corruptionTask.containsKey(playerUuid)){
            Bukkit.getScheduler().cancelTask(corruptionTask.get(playerUuid));
            corruptionTask.remove(playerUuid);
        }

        int currentCorruption = corruptionLevels.getOrDefault(playerUuid, 0);
        if(currentCorruption<10){
            currentCorruption++;
        }
        corruptionLevels.put(playerUuid, currentCorruption);
        int duration = 3*60;
        int amplifier = currentCorruption-1;
        player.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, duration, amplifier));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, duration/3, amplifier));
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "goop sudop " + playerName + " ml tempstat add " + playerName + " DEFENSE -10% 3600");
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "goop sudop " + playerName + " ml tempstat add " + playerName + " MAX_HEALTH -10% 3600");
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "goop sudop " + playerName + " ml tempstat add " + playerName + " BLOCK_BREAK -10% 3600");
        int taskID = Bukkit.getScheduler().runTaskLater(plugin, () ->{
            corruptionLevels.remove(playerUuid);
            corruptionTask.remove(playerUuid);
            player.removePotionEffect(PotionEffectType.UNLUCK);
            player.removePotionEffect(PotionEffectType.DARKNESS);
        }, duration).getTaskId();
        corruptionTask.put(playerUuid, taskID);
    }
}
