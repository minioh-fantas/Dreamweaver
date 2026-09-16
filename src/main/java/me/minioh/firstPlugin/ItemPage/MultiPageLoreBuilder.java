package me.minioh.firstPlugin.ItemPage;

import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.LiveMMOItem;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultiPageLoreBuilder {

    public static List<String> buildPage(LiveMMOItem liveMmo, int page, NBTItem nbtItem) {
        List<String> format = ConfigManager.getPageFormat(page);
        List<String> finalLore = new ArrayList<>();

        // 1. Native Auto-Generated Lore
        if (format != null && !format.isEmpty()) {
            ItemStackBuilder builder = new ItemStackBuilder(liveMmo);
            
            // INJECT custom page format to completely override native lore-format.yml
            builder.getLore().setLore(new ArrayList<>(format));

            // buildSilently() natively resolves all #tags# (including Abilities/Elements/Sets) 
            // and securely processes {bar} without triggering an infinite ItemBuildEvent loop.
            ItemStack builtPage = builder.buildSilently();
            if (builtPage.hasItemMeta() && builtPage.getItemMeta().hasLore())
                finalLore.addAll(builtPage.getItemMeta().getLore());
        }

        // 2. Append Manual Lore Lines
        Map<String, List<String>> manualPages = LorePagesStat.getManualPages(nbtItem);
        List<String> customLines = manualPages.get("page_" + page);
        if (customLines != null && !customLines.isEmpty()) {
            for (String line : customLines) {
                Component formattedLine = line.contains("&")
                        ? LegacyComponentSerializer.builder().character('&').hexColors().build().deserialize(line)
                        : MiniMessage.miniMessage().deserialize(line);
                finalLore.add(LegacyComponentSerializer.legacySection().serialize(formattedLine.decoration(TextDecoration.ITALIC, false)));
            }
        }

        return finalLore;
    }

    public static boolean hasPageContent(LiveMMOItem mmo, int page, Map<String, List<String>> manualPages) {
        if (manualPages.containsKey("page_" + page) && !manualPages.get("page_" + page).isEmpty()) return true;

        List<String> format = ConfigManager.getPageFormat(page);
        if (format == null || format.isEmpty()) return false;

        for (String line : format)
            if (line.contains("%")) return true; // Keep if it has PAPI placeholders

        // Validate if the MMOItem possesses any stat required by the format
        for (ItemStat stat : mmo.getStats()) {
            String path = stat.getPath();
            if (stat.getId().equals("ABILITY")) path = "abilities";
            else if (stat.getId().equals("ELEMENT")) path = "elements";
            else if (stat.getId().equals("PERM_EFFECTS")) path = "perm-effects";
            else if (stat.getId().equals("EFFECTS")) path = "effects";
            else if (stat.getId().equals("SET")) path = "set";

            for (String line : format)
                if (line.contains("#" + path + "#")) return true;
        }
        
        return false;
    }
}