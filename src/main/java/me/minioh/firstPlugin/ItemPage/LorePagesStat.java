package me.minioh.firstPlugin.ItemPage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.lumine.mythic.lib.api.item.ItemTag;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LorePagesStat extends ItemStat<StringData, StringData> {

    private static final Gson GSON = new Gson();
    private static final Type LIST_TYPE = new TypeToken<List<String>>(){}.getType();

    public LorePagesStat() {
        super("LORE_PAGES", Material.WRITABLE_BOOK, "Lore Pages", 
              new String[] { "Adds cyclable lore pages to your item." }, 
              new String[] { "all" });
    }

    @Override
    public StringData whenInitialized(Object object) {
        return new StringData(object.toString());
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
        item.addItemTag(getAppliedNBT(data));
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull StringData data) {
        ArrayList<ItemTag> ret = new ArrayList<>();
        ret.add(new ItemTag(getNBTPath(), data.toString())); // Save JSON to NBT[cite: 2]
        return ret;
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        // Open the custom Multiple Lores Menu
        new LoreGUIHandler(inv).open();
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        // Handled asynchronously via ChatInputListener to avoid blocking
    }

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        ArrayList<ItemTag> relevantTags = new ArrayList<>();
        if (mmoitem.getNBT().hasTag(getNBTPath())) {
            relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING)); //[cite: 2]
        }
        StringData bakedData = getLoadedNBT(relevantTags);
        if (bakedData != null) {
            mmoitem.setData(this, bakedData);
        }
    }

    @Nullable
    @Override
    public StringData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
        ItemTag tg = ItemTag.getTagAtPath(getNBTPath(), storedTags); //[cite: 2]
        if (tg != null) {
            return new StringData((String) tg.getValue());
        }
        return null;
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<StringData> statData) {
        if (statData.isPresent()) {
            List<String> pages = GSON.fromJson(statData.get().toString(), LIST_TYPE);
            lore.add(ChatColor.GRAY + "Current Pages: " + ChatColor.GREEN + pages.size());
        } else {
            lore.add(ChatColor.GRAY + "Current Pages: " + ChatColor.RED + "None");
        }
        lore.add("");
        lore.add(ChatColor.YELLOW + "► Click to edit pages.");
    }

    @NotNull
    @Override
    public StringData getClearStatData() {
        return new StringData("[]");
    }
}