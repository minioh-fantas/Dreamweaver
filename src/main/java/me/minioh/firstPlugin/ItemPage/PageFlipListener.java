package me.minioh.firstPlugin.ItemPage;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.kyori.adventure.text.Component;
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

import java.util.List;
import java.util.Map;

public class PageFlipListener implements Listener {

    public static final NamespacedKey PAGE_KEY = new NamespacedKey("mmoaddon", "current_page");

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        if (handlePageCycle(event.getMainHandItem())) event.setCancelled(true);
    }

    @EventHandler
    public void onInventorySwap(InventoryClickEvent event) {
        if (event.getClick() == ClickType.SWAP_OFFHAND)
            if (handlePageCycle(event.getCurrentItem())) event.setCancelled(true);
    }

    private boolean handlePageCycle(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        NBTItem nbtItem = NBTItem.get(item);
        if (!nbtItem.hasType()) return false;

        LiveMMOItem liveMmo = new LiveMMOItem(nbtItem);
        Map<String, List<String>> manualPages = LorePagesStat.getManualPages(nbtItem);

        int maxAutoPages = ConfigManager.getMaxAutoPages();
        int maxManualPage = manualPages.keySet().stream()
                .map(k -> Integer.parseInt(k.replace("page_", "")))
                .max(Integer::compareTo).orElse(0);
        
        int totalPages = Math.max(maxAutoPages, maxManualPage);
        if (totalPages <= 1) return false;

        ItemMeta meta = item.getItemMeta();
        int currentPage = meta.getPersistentDataContainer().getOrDefault(PAGE_KEY, PersistentDataType.INTEGER, 1);
        int startPage = currentPage;

        while (true) {
            currentPage = (currentPage % totalPages) + 1;
            if (MultiPageLoreBuilder.hasPageContent(liveMmo, currentPage, manualPages) || currentPage == 1) break;
            if (currentPage == startPage) return false;
        }

        meta.getPersistentDataContainer().set(PAGE_KEY, PersistentDataType.INTEGER, currentPage);
        
        // Adventure Component support mapping
        List<Component> pageLore = MultiPageLoreBuilder.buildPage(liveMmo, currentPage, nbtItem);
        meta.lore(pageLore);
        
        item.setItemMeta(meta);
        
        return true;
    }
}