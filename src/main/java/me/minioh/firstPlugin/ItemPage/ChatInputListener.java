package me.minioh.firstPlugin.ItemPage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.lang.reflect.Type;
import java.util.List;

public class ChatInputListener implements Listener {

    private static final Gson GSON = new Gson();
    private static final Type LIST_TYPE = new TypeToken<List<String>>(){}.getType();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        MultiLorePlugin.PendingInput input = MultiLorePlugin.getInstance().getPendingInputs().remove(player.getUniqueId());
        
        if (input == null) return;
        event.setCancelled(true);

        EditionInventory inv = input.inv();
        String json = inv.getEditedSection().getString("lore-pages", "[]");
        List<String> pages = GSON.fromJson(json, LIST_TYPE);

        // Convert the literal string "\n" typed in chat into a proper newline
        String newMessage = event.getMessage().replace("\\n", "\n"); 
        
        if (input.targetPageIndex() >= pages.size()) {
            pages.add(newMessage);
        } else {
            // Append instead of rewriting
            String existing = pages.get(input.targetPageIndex());
            pages.set(input.targetPageIndex(), existing + "\n" + newMessage);
        }

        Bukkit.getScheduler().runTask(MultiLorePlugin.getInstance(), () -> {
            inv.getEditedSection().set("lore-pages", GSON.toJson(pages));
            inv.registerTemplateEdition();
            new LoreGUIHandler(inv).open();
        });
    }
}