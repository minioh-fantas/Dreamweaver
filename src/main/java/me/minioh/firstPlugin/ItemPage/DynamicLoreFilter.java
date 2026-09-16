package me.minioh.firstPlugin.ItemPage;

import net.Indyuce.mmoitems.ItemStats;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DynamicLoreFilter {

    /**
     * Dynamically identifies the exact lore lines injected by the ABILITIES stat.
     * Uses a Difference Engine approach to bypass the LoreBuilder's #abilities# 
     * injection obfuscation without breaking Revision ID /mi updates.
     */
    public static List<String> extractAbilityLines(LiveMMOItem liveMmo) {
        if (!liveMmo.hasData(ItemStats.ABILITIES)) return Collections.emptyList();

        // 1. Build the item WITH abilities
        ItemStack fullItem = new ItemStackBuilder(liveMmo).buildSilently();
        if (!fullItem.hasItemMeta() || !fullItem.getItemMeta().hasLore()) return Collections.emptyList();
        List<String> fullLore = fullItem.getItemMeta().getLore();

        // 2. Build the item WITHOUT abilities
        MMOItem clone = liveMmo.clone();
        clone.removeData(ItemStats.ABILITIES);
        ItemStack itemWithout = new ItemStackBuilder(clone).buildSilently();
        List<String> loreWithout = itemWithout.hasItemMeta() && itemWithout.getItemMeta().hasLore() 
                ? itemWithout.getItemMeta().getLore() : new ArrayList<>();

        // 3. Extract the difference (the injected ability block)
        List<String> abilityLines = new ArrayList<>();
        int withIdx = 0, withoutIdx = 0;
        
        while (withIdx < fullLore.size()) {
            if (withoutIdx < loreWithout.size() && fullLore.get(withIdx).equals(loreWithout.get(withoutIdx))) {
                withIdx++;
                withoutIdx++;
            } else {
                abilityLines.add(fullLore.get(withIdx));
                withIdx++;
            }
        }
        
        return abilityLines;
    }
}