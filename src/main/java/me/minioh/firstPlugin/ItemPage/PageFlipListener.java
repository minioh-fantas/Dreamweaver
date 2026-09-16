package me.minioh.firstPlugin.ItemPage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
    public static final NamespacedKey PAGE_KEY = new NamespacedKey("mmoaddon", "current_page");

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
        if (!nbtItem.hasType()) return false; 

        List<String> manualPages = new ArrayList<>();
        if (nbtItem.hasTag("MMOITEMS_LORE_PAGES")) {
            manualPages = GSON.fromJson(nbtItem.getString("MMOITEMS_LORE_PAGES"), LIST_TYPE);
        }

        int maxAutoPages = ConfigManager.getMaxAutoPages();
        int totalPages = maxAutoPages + manualPages.size();
        if (totalPages <= 1) return false;

        ItemMeta meta = item.getItemMeta();
        int currentPage = meta.getPersistentDataContainer().getOrDefault(PAGE_KEY, PersistentDataType.INTEGER, 1);
        currentPage = (currentPage % totalPages) + 1;
        meta.getPersistentDataContainer().set(PAGE_KEY, PersistentDataType.INTEGER, currentPage);

        List<String> newLoreLegacy = new ArrayList<>();

        if (currentPage <= maxAutoPages) {
            LiveMMOItem liveMmo = new LiveMMOItem(nbtItem);
            
            // Rebuilding with filtered stats automatically clears unused placeholder tags (like #abilities#).
            // You can still call DynamicLoreFilter.extractAbilityLines(liveMmo) here if your architecture requires strictly returning line strings.
            MMOItem filteredMmo = new MMOItem(liveMmo.getType(), liveMmo.getId());
            
            for (ItemStat stat : liveMmo.getStats()) {
                // CRITICAL FIX: Map the internal "ABILITY" ID to the user-facing "ABILITIES" config key
                String configId = stat.getId().equals("ABILITY") ? "ABILITIES" : stat.getId();
                int targetPage = ConfigManager.getStatPage(configId);
                
                if (targetPage == -1 || targetPage == currentPage) {
                    filteredMmo.setData(stat, liveMmo.getData(stat));
                }
            }

            ItemStack builtPage = new ItemStackBuilder(filteredMmo).buildSilently();
            if (builtPage.hasItemMeta() && builtPage.getItemMeta().hasLore()) {
                newLoreLegacy = builtPage.getItemMeta().getLore();
            }
            
        } else {
            // Render manual pages using Adventure API
            String pageRawText = manualPages.get(currentPage - maxAutoPages - 1);
            for (String line : pageRawText.split("\n")) {
                Component formattedLine;
                if (line.contains("&")) {
                    formattedLine = LegacyComponentSerializer.builder().character('&').hexColors().build().deserialize(line);
                } else {
                    formattedLine = MiniMessage.miniMessage().deserialize(line);
                }
                formattedLine = formattedLine.decoration(TextDecoration.ITALIC, false);
                newLoreLegacy.add(LegacyComponentSerializer.legacySection().serialize(formattedLine));
            }
        }

        meta.setLore(newLoreLegacy);
        item.setItemMeta(meta);

        return true;
    }
}