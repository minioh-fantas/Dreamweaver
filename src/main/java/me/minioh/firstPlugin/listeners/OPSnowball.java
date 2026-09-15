package me.minioh.firstPlugin.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class OPSnowball implements Listener {
    @EventHandler
    public void whenThrowSnowball(ProjectileHitEvent event){
        EntityType projectile = event.getEntityType();
        if(projectile == EntityType.SNOWBALL){
            Block block = event.getHitBlock();
            if (block != null) block.breakNaturally();
        }
    }
}
