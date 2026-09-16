package me.minioh.firstPlugin.ItemPage;

import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ChatInputListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        MultiLorePlugin.PendingInput input = MultiLorePlugin.getInstance().getPendingInputs().remove(player.getUniqueId());
        
        if (input == null) return;
        event.setCancelled(true);

        EditionInventory inv = input.inv();
        String json = inv.getEditedSection().getString("lore-pages", "{}");
        Map<String, List<String>> pages = LorePagesStat.parseJson(json);

        String newMessage = event.getMessage().replace("\\n", "\n"); 
        String key = "page_" + input.targetPageIndex();
        
        pages.computeIfAbsent(key, k -> new ArrayList<>()).add(newMessage);

        Bukkit.getScheduler().runTask(MultiLorePlugin.getInstance(), () -> {
            inv.getEditedSection().set("lore-pages", LorePagesStat.GSON.toJson(pages));
            inv.registerTemplateEdition(); 
            new LoreGUIHandler(inv).open();
        });
    }
}