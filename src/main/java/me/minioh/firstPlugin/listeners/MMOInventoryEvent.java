package me.minioh.firstPlugin.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;

public class MMOInventoryEvent implements Listener {
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if(!(event.getWhoClicked() instanceof Player player)){
            return;
        }
        if(event.getInventory().getType()!= InventoryType.CRAFTING){
            return;
        }
        int slot = event.getSlot();
        if (slot >=1 && slot<=4){
            player.sendMessage("YEAH");
        }
    }
}
