package me.minioh.firstPlugin.ItemPage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.lumine.mythic.lib.api.item.ItemTag;
import io.lumine.mythic.lib.api.item.NBTItem;
import io.lumine.mythic.lib.api.item.SupportedNBTTagValues;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.api.item.mmoitem.ReadMMOItem;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringData;
import net.Indyuce.mmoitems.stat.type.ItemStat;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.*;

public class LorePagesStat extends ItemStat<StringData, StringData> {

    public static final Gson GSON = new Gson();
    public static final Type MAP_TYPE = new TypeToken<Map<String, List<String>>>(){}.getType();
    public static final Type LEGACY_LIST_TYPE = new TypeToken<List<String>>(){}.getType();

    public LorePagesStat() {
        super("LORE_PAGES", Material.WRITABLE_BOOK, "Custom Lore Pages", 
              new String[] { "Add custom manual lines to any page." }, new String[] { "all" });
    }

    public static Map<String, List<String>> getManualPages(NBTItem nbtItem) {
        if (!nbtItem.hasTag("MMOITEMS_LORE_PAGES")) return new HashMap<>();
        return parseJson(nbtItem.getString("MMOITEMS_LORE_PAGES"));
    }

    public static Map<String, List<String>> parseJson(String json) {
        if (json == null || json.isEmpty()) return new HashMap<>();
        
        try {
            return GSON.fromJson(json, MAP_TYPE);
        } catch (Exception e) {
            try {
                List<String> legacy = GSON.fromJson(json, LEGACY_LIST_TYPE);
                Map<String, List<String>> map = new HashMap<>();
                int start = ConfigManager.getMaxAutoPages() + 1;
                for (int i = 0; i < legacy.size(); i++) 
                    map.put("page_" + (start + i), new ArrayList<>(List.of(legacy.get(i))));
                return map;
            } catch (Exception ex) {
                return new HashMap<>();
            }
        }
    }

    @Override
    public StringData whenInitialized(Object object) { return new StringData(object.toString()); }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
        item.addItemTag(getAppliedNBT(data)); 
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull StringData data) {
        return new ArrayList<>(List.of(new ItemTag(getNBTPath(), data.toString())));
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        new LoreGUIHandler(inv).open();
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {}

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        if (mmoitem.getNBT().hasTag(getNBTPath())) {
            ItemTag tag = ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING);
            if (tag != null) mmoitem.setData(this, new StringData((String) tag.getValue()));
        }
    }

    @Nullable
    @Override
    public StringData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
        ItemTag tg = ItemTag.getTagAtPath(getNBTPath(), storedTags); 
        return tg != null ? new StringData((String) tg.getValue()) : null;
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<StringData> statData) {
        lore.add(ChatColor.GRAY + "Status: " + (statData.isPresent() ? ChatColor.GREEN + "Configured" : ChatColor.RED + "Empty"));
        lore.add("");
        lore.add(ChatColor.YELLOW + "► Click to edit custom manual lines.");
    }

    @NotNull
    @Override
    public StringData getClearStatData() { return new StringData("{}"); }
}