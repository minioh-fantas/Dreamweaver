package me.minioh.firstPlugin.ItemPage;

import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.api.item.mmoitem.MMOItem;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DynamicLoreFilter {

    public static List<String> generatePageLore(LiveMMOItem liveMmo, int targetPage) {
        MMOItem filtered = new MMOItem(liveMmo.getType(), liveMmo.getId());

        for (ItemStat stat : liveMmo.getStats()) {
            int page = getStatPage(stat);
            
            // STRICT ISOLATION: Unlisted stats (-1) MUST default to Page 1 only.
            if (page == -1) page = 1;

            if (page == targetPage)
                filtered.setData(stat, liveMmo.getData(stat));
        }

        ItemStack builtPage = new ItemStackBuilder(filtered).buildSilently();
        if (builtPage.hasItemMeta() && builtPage.getItemMeta().hasLore())
            return builtPage.getItemMeta().getLore();
        
        return new ArrayList<>();
    }

    private static int getStatPage(ItemStat stat) {
        String path = stat.getPath();
        
        if (stat.getId().equals("ABILITY")) path = "abilities";
        if (stat.getId().equals("ELEMENT")) path = "elements";
        if (stat.getId().equals("PERM_EFFECTS")) path = "perm-effects";
        if (stat.getId().equals("EFFECTS")) path = "effects";
        if (stat.getId().equals("SET")) path = "set";

        String placeholder = "#" + path + "#";

        for (int i = 1; i <= ConfigManager.getMaxAutoPages(); i++) {
            List<String> format = ConfigManager.getPageFormat(i);
            if (format != null) {
                for (String line : format)
                    if (line.contains(placeholder)) return i;
            }
        }
        
        return -1;
    }
}