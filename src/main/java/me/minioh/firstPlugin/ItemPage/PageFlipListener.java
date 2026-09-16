package me.minioh.firstPlugin.ItemPage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PageFlipListener implements Listener {

    private static final Gson GSON = new Gson();
    private static final Type LIST_TYPE = new TypeToken<List<String>>(){}.getType();
    private final NamespacedKey pageKey = new NamespacedKey(MultiLorePlugin.getInstance(), "current_page");
    private final NamespacedKey originalLoreKey = new NamespacedKey(MultiLorePlugin.getInstance(), "original_lore");

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        if (handlePageCycle(event.getPlayer(), event.getMainHandItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventorySwap(InventoryClickEvent event) {
        if (event.getClick() == ClickType.SWAP_OFFHAND) {
            if (handlePageCycle((Player) event.getWhoClicked(), event.getCurrentItem())) {
                event.setCancelled(true);
            }
        }
    }

    private boolean handlePageCycle(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        NBTItem nbtItem = NBTItem.get(item);
        if (!nbtItem.hasTag("MMOITEMS_LORE_PAGES")) return false; 

        String pagesJson = nbtItem.getString("MMOITEMS_LORE_PAGES");
        List<String> customPages = GSON.fromJson(pagesJson, LIST_TYPE);
        if (customPages == null || customPages.isEmpty()) return false;

        ItemMeta meta = item.getItemMeta();
        int totalPages = customPages.size() + 1; 
        int currentPage = meta.getPersistentDataContainer().getOrDefault(pageKey, PersistentDataType.INTEGER, 1);

        // Fix: Save original lore properly line-by-line as a JSON array of components
        if (currentPage == 1 && meta.hasLore()) {
            List<String> serializedLines = new ArrayList<>();
            for (Component comp : meta.lore()) {
                serializedLines.add(GsonComponentSerializer.gson().serialize(comp));
            }
            meta.getPersistentDataContainer().set(originalLoreKey, PersistentDataType.STRING, GSON.toJson(serializedLines));
        }

        currentPage = (currentPage % totalPages) + 1;
        meta.getPersistentDataContainer().set(pageKey, PersistentDataType.INTEGER, currentPage);

        if (currentPage == 1) {
            // Fix: Restore Original Lore from the saved JSON string array
            String serializedLore = meta.getPersistentDataContainer().get(originalLoreKey, PersistentDataType.STRING);
            if (serializedLore != null) {
                List<String> jsonLines = GSON.fromJson(serializedLore, LIST_TYPE);
                List<Component> deserialized = new ArrayList<>();
                for (String jsonLine : jsonLines) {
                    deserialized.add(GsonComponentSerializer.gson().deserialize(jsonLine));
                }
                meta.lore(deserialized); 
            }
        } else {
            String pageRawText = customPages.get(currentPage - 2);
            List<Component> newLore = new ArrayList<>();
            
            for (String line : pageRawText.split("\n")) {
                Component formattedLine;
                if (line.contains("&")) {
                    formattedLine = LegacyComponentSerializer.builder().character('&').hexColors().build().deserialize(line);
                } else {
                    formattedLine = MiniMessage.miniMessage().deserialize(line);
                }
                newLore.add(formattedLine.decoration(TextDecoration.ITALIC, false));
            }
            meta.lore(newLore);
        }

        item.setItemMeta(meta);
        return true;
    }
}