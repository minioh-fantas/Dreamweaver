package me.minioh.firstPlugin.ItemPage;

import net.Indyuce.mmoitems.api.event.MMOItemReforgeEvent;
import net.Indyuce.mmoitems.api.event.MMOItemReforgeFinishEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ReforgeListener implements Listener {

    private final LorePagesStat lorePagesStat;

    public ReforgeListener(LorePagesStat lorePagesStat) {
        this.lorePagesStat = lorePagesStat;
    }

    @EventHandler
    public void onReforge(MMOItemReforgeEvent event) {
        MMOItem oldMmo = event.getReforger().getOldMMOItem();
        MMOItem freshMmo = event.getReforger().getFreshMMOItem();

        // Carry over the custom manual lore pages to the fresh MMOItem[cite: 5]
        if (oldMmo.hasData(lorePagesStat)) {
            freshMmo.setData(lorePagesStat, oldMmo.getData(lorePagesStat));
        }
    }

    @EventHandler
    public void onReforgeFinish(MMOItemReforgeFinishEvent event) {
        ItemStack oldItem = event.getReforger().getStack();
        ItemStack freshItem = event.getFinishedItem();

        // Carry over the PDC current reading page marker so the player's view doesn't jump
        if (oldItem.hasItemMeta() && freshItem.hasItemMeta()) {
            Integer page = oldItem.getItemMeta().getPersistentDataContainer().get(PageFlipListener.PAGE_KEY, PersistentDataType.INTEGER);
            if (page != null) {
                ItemMeta freshMeta = freshItem.getItemMeta();
                freshMeta.getPersistentDataContainer().set(PageFlipListener.PAGE_KEY, PersistentDataType.INTEGER, page);
                freshItem.setItemMeta(freshMeta);
            }
        }
    }
}