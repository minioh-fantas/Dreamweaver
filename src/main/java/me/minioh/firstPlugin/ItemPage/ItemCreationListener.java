package me.minioh.firstPlugin.ItemPage;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.event.ItemBuildEvent;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ItemCreationListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemBuild(ItemBuildEvent event) {
        ItemStack item = event.getItemStack();
        if (item == null || !item.hasItemMeta()) return;

        NBTItem nbt = NBTItem.get(item);
        if (!nbt.hasType()) return;

        LiveMMOItem liveMmo = new LiveMMOItem(nbt);
        ItemMeta meta = item.getItemMeta();
        
        meta.setLore(MultiPageLoreBuilder.buildPage(liveMmo, 1, nbt));
        meta.getPersistentDataContainer().set(PageFlipListener.PAGE_KEY, PersistentDataType.INTEGER, 1);
        item.setItemMeta(meta);
    }
}