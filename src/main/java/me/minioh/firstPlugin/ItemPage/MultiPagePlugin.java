package me.minioh.firstPlugin.ItemPage;

import io.lumine.mythic.lib.api.item.ItemTag;
import net.Indyuce.mmoitems.MMOItems;

import net.Indyuce.mmoitems.api.edition.StatEdition;
import net.Indyuce.mmoitems.api.item.build.ItemStackBuilder;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import net.Indyuce.mmoitems.stat.data.StringListData;
import net.Indyuce.mmoitems.stat.type.StringListStat;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.*;
import java.util.stream.Collectors;

public final class MultiPagePlugin extends JavaPlugin implements Listener {

    public static NamespacedKey PAGES_KEY;
    public static NamespacedKey INDEX_KEY;
    public static NamespacedKey ORIGIN_LORE_KEY;
    public static final Map<UUID, Integer> editingPage = new HashMap<>();

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.builder()
            .character('&')
            .hexCharacter('#')
            .hexColors()
            .build();

    @Override
    public void onEnable() {
        PAGES_KEY = new NamespacedKey(this, "pages");
        INDEX_KEY = new NamespacedKey(this, "page_index");
        ORIGIN_LORE_KEY = new NamespacedKey(this, "origin_lore");

        MMOItems.plugin.getStats().register(new LorePagesStat());
        getServer().getPluginManager().registerEvents(this, this);
    }

    public static Component parseColor(String text) {
        if (text == null) return Component.empty();
        Component legacy = LEGACY_SERIALIZER.deserialize(text);
        String mm = MiniMessage.miniMessage().serialize(legacy);
        return MiniMessage.miniMessage().deserialize(mm).decoration(TextDecoration.ITALIC, false);
    }

    @EventHandler
    public void onSwap(InventoryClickEvent e) {
        if (e.getClick() != ClickType.SWAP_OFFHAND) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(PAGES_KEY, PersistentDataType.STRING)) return;

        e.setCancelled(true);

        if (!meta.getPersistentDataContainer().has(ORIGIN_LORE_KEY, PersistentDataType.STRING)) {
            List<Component> orig = meta.lore();
            if (orig != null)
                meta.getPersistentDataContainer().set(ORIGIN_LORE_KEY, PersistentDataType.STRING, orig.stream().map(GsonComponentSerializer.gson()::serialize).collect(Collectors.joining(";;;")));
        }

        String[] pages = meta.getPersistentDataContainer().get(PAGES_KEY, PersistentDataType.STRING).split(";;PAGE;;");
        int index = meta.getPersistentDataContainer().getOrDefault(INDEX_KEY, PersistentDataType.INTEGER, 0);

        index = (index + 1) % (pages.length + 1);
        meta.getPersistentDataContainer().set(INDEX_KEY, PersistentDataType.INTEGER, index);

        if (index == 0) {
            String json = meta.getPersistentDataContainer().get(ORIGIN_LORE_KEY, PersistentDataType.STRING);
            if (json != null && !json.isEmpty())
                meta.lore(Arrays.stream(json.split(";;;")).map(GsonComponentSerializer.gson()::deserialize).toList());
            else
                meta.lore(null);
        } else {
            String pageData = pages[index - 1];
            meta.lore(Arrays.stream(pageData.split("\\\\n")).map(MultiPagePlugin::parseColor).toList());
        }
        item.setItemMeta(meta);
    }

    @EventHandler
    public void onGUIClick(InventoryClickEvent e) {
        if (!(e.getInventory().getHolder() instanceof LorePagesGUI gui)) return;

        e.setCancelled(true);

        if (e.getRawSlot() == 4) {
            editingPage.put(e.getWhoClicked().getUniqueId(), -1);
            e.getWhoClicked().closeInventory();
            new StatEdition(gui.getEditionInv(), MMOItems.plugin.getStats().get("LORE_PAGES")).enable("Type your new page in chat. Use \\n for new lines. Supports MiniMessage & Hex/Legacy.");
        } else if (e.getRawSlot() == 8)
            gui.getEditionInv().open();
        else if (e.getRawSlot() >= 18 && e.getCurrentItem() != null && e.getCurrentItem().getType() == Material.PAPER) {
            int index = e.getRawSlot() - 18;
            List<String> pages = gui.getEditionInv().getEditedSection().getStringList("LORE_PAGES");
            if (index >= pages.size()) return;

            if (e.getClick().isShiftClick() && e.getClick().isRightClick()) {
                pages.remove(index);
                gui.getEditionInv().getEditedSection().set("LORE_PAGES", pages);
                gui.getEditionInv().registerTemplateEdition();
                gui.open();
            } else if (e.getClick().isLeftClick()) {
                editingPage.put(e.getWhoClicked().getUniqueId(), index);
                e.getWhoClicked().closeInventory();
                new StatEdition(gui.getEditionInv(), MMOItems.plugin.getStats().get("LORE_PAGES")).enable("Type to add a line to Page " + (index + 2) + ". Use \\n for new lines.");
            }
        }
    }
}

class LorePagesStat extends StringListStat {
    public LorePagesStat() {
        super("LORE_PAGES", Material.WRITABLE_BOOK, "Lore Pages",
                new String[]{"Add multiple pages of lore.", "Left click to edit pages."},
                new String[]{"all"});
    }

    @Override
    public void whenClicked(@NotNull EditionInventory inv, @NotNull InventoryClickEvent event) {
        new LorePagesGUI(inv).open();
    }

    @Override
    public void whenInput(@NotNull EditionInventory inv, @NotNull String message, Object... info) {
        int editIndex = MultiPagePlugin.editingPage.getOrDefault(inv.getPlayer().getUniqueId(), -1);
        List<String> pages = inv.getEditedSection().getStringList("LORE_PAGES");

        if (editIndex == -1)
            pages.add(message);
        else if (editIndex >= 0 && editIndex < pages.size())
            pages.set(editIndex, pages.get(editIndex) + "\\n" + message);

        inv.getEditedSection().set("LORE_PAGES", pages);
        inv.registerTemplateEdition();
        inv.getPlayer().sendMessage(ChatColor.GREEN + "Lore pages updated!");
    }

    @Override
    public void whenApplied(@NotNull ItemStackBuilder item, @NotNull StringListData data) {
        String combined = String.join(";;PAGE;;", data.getList());
        item.getMeta().getPersistentDataContainer().set(MultiPagePlugin.PAGES_KEY, PersistentDataType.STRING, combined);
        item.addItemTag(new ItemTag("pages", combined));
    }
}

class LorePagesGUI implements InventoryHolder {
    private final EditionInventory editionInv;
    private Inventory inv;

    public LorePagesGUI(EditionInventory editionInv) {
        this.editionInv = editionInv;
    }

    public EditionInventory getEditionInv() {
        return editionInv;
    }

    public void open() {
        inv = Bukkit.createInventory(this, 54, "Lore Pages");

        ItemStack addBtn = new ItemStack(Material.EMERALD);
        ItemMeta addMeta = addBtn.getItemMeta();
        addMeta.setDisplayName(ChatColor.GREEN + "Add New Page");
        addBtn.setItemMeta(addMeta);
        inv.setItem(4, addBtn);

        ItemStack backBtn = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backBtn.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back");
        backBtn.setItemMeta(backMeta);
        inv.setItem(8, backBtn);

        List<String> pages = editionInv.getEditedSection().getStringList("LORE_PAGES");
        for (int i = 0; i < pages.size(); i++) {
            ItemStack paper = new ItemStack(Material.PAPER);
            ItemMeta paperMeta = paper.getItemMeta();
            paperMeta.setDisplayName(ChatColor.YELLOW + "Page " + (i + 2));

            List<Component> lore = new ArrayList<>();
            lore.add(Component.empty());
            lore.addAll(Arrays.stream(pages.get(i).split("\\\\n")).map(MultiPagePlugin::parseColor).toList());
            lore.add(Component.empty());
            lore.add(Component.text("Left-Click to add a line.", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
            lore.add(Component.text("Shift-Right-Click to delete.", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));

            paperMeta.lore(lore);
            paper.setItemMeta(paperMeta);
            inv.setItem(18 + i, paper);
        }
        editionInv.getPlayer().openInventory(inv);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inv;
    }
}