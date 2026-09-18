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

    public static List<Component> buildPage(LiveMMOItem liveMmo, int page, NBTItem nbtItem) {
        List<String> format = ConfigManager.getPageFormat(page);
        List<String> combinedFormat = new ArrayList<>();

        // 1. Base Configured Format
        if (format != null && !format.isEmpty()) {
            combinedFormat.addAll(format);
        }

        // 2. Append Manual Lore Lines
        Map<String, List<String>> manualPages = LorePagesStat.getManualPages(nbtItem);
        List<String> customLines = manualPages.get("page_" + page);
        
        if (customLines != null && !customLines.isEmpty()) {
            for (String line : customLines) {
                // Parse MiniMessage or Legacy into native section symbols BEFORE passing to MMOItems.
                // This allows MMOItems' LoreBuilder to natively wrap these lines inside Custom Tooltips 
                // and perfectly evaluate raw placeholders like #attack-damage# or {bar}.
                Component comp;
                if (line.contains("&")) {
                    comp = LegacyComponentSerializer.builder().character('&').hexColors().build().deserialize(line);
                } else {
                    comp = MiniMessage.miniMessage().deserialize(line);
                }
                comp = comp.decoration(TextDecoration.ITALIC, false);
                combinedFormat.add(LegacyComponentSerializer.legacySection().serialize(comp));
            }
        }

        if (combinedFormat.isEmpty()) return new ArrayList<>();

        // 3. Inject into MMOItems Engine
        ItemStackBuilder builder = new ItemStackBuilder(liveMmo);
        builder.getLore().setLore(combinedFormat);

        // buildSilently() applies all stats, evaluates {bar}, parses placeholders,
        // and inherently applies the TooltipTexture Prefix/Suffix to our combined block.
        ItemStack builtPage = builder.buildSilently();
        
        // Extract the beautifully formatted Adventure Components directly from the built meta
        if (builtPage.hasItemMeta() && builtPage.getItemMeta().hasLore()) {
            return builtPage.getItemMeta().lore();
        }

        return new ArrayList<>();
    }

    public static boolean hasPageContent(LiveMMOItem mmo, int page, Map<String, List<String>> manualPages) {
        if (manualPages.containsKey("page_" + page) && !manualPages.get("page_" + page).isEmpty()) return true;

        List<String> format = ConfigManager.getPageFormat(page);
        if (format == null || format.isEmpty()) return false;

        for (String line : format)
            if (line.contains("%")) return true; 

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