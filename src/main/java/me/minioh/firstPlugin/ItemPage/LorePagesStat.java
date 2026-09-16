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
        super("LORE_PAGES", Material.WRITABLE_BOOK, "Manual Lore Pages", 
              new String[] { "Adds manual pages after auto-pages." }, 
              new String[] { "all" });
    }

    @Override
    public StringData whenInitialized(Object object) {
        return new StringData(object.toString());
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringData data) {
        item.addItemTag(getAppliedNBT(data));
        // Note: We intentionally do NOT insert anything into `item.getLore()` here!
        // This ensures the manual pages do not render on Auto Page 1 when the item is rebuilt natively.
    }

    @NotNull
    @Override
    public ArrayList<ItemTag> getAppliedNBT(@NotNull StringData data) {
        ArrayList<ItemTag> ret = new ArrayList<>();
        ret.add(new ItemTag(getNBTPath(), data.toString())); 
        return ret;
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        new LoreGUIHandler(inv).open();
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {}

    @Override
    public void whenLoaded(@NotNull ReadMMOItem mmoitem) {
        ArrayList<ItemTag> relevantTags = new ArrayList<>();
        if (mmoitem.getNBT().hasTag(getNBTPath())) {
            relevantTags.add(ItemTag.getTagAtPath(getNBTPath(), mmoitem.getNBT(), SupportedNBTTagValues.STRING)); 
        }
        StringData bakedData = getLoadedNBT(relevantTags);
        if (bakedData != null) {
            mmoitem.setData(this, bakedData);
        }
    }

    @Nullable
    @Override
    public StringData getLoadedNBT(@NotNull ArrayList<ItemTag> storedTags) {
        ItemTag tg = ItemTag.getTagAtPath(getNBTPath(), storedTags); 
        if (tg != null) {
            return new StringData((String) tg.getValue());
        }
        return null;
    }

    @Override
    public void whenDisplayed(List<String> lore, Optional<StringData> statData) {
        if (statData.isPresent()) {
            List<String> pages = GSON.fromJson(statData.get().toString(), LIST_TYPE);
            lore.add(ChatColor.GRAY + "Manual Pages: " + ChatColor.GREEN + pages.size());
        } else {
            lore.add(ChatColor.GRAY + "Manual Pages: " + ChatColor.RED + "None");
        }
        lore.add("");
        lore.add(ChatColor.YELLOW + "► Click to edit manual pages.");
    }

    @NotNull
    @Override
    public StringData getClearStatData() {
        return new StringData("[]");
    }
}