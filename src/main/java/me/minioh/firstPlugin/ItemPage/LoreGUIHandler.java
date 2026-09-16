package me.minioh.firstPlugin.ItemPage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.Indyuce.mmoitems.gui.edition.EditionInventory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LoreGUIHandler implements InventoryHolder {

    private final Inventory inventory;
    private final EditionInventory editionInv;
    private final List<String> pages;
    
    private static final Gson GSON = new Gson();
    private static final Type LIST_TYPE = new TypeToken<List<String>>(){}.getType();

    public LoreGUIHandler(EditionInventory editionInv) {
        this.editionInv = editionInv;
        this.inventory = Bukkit.createInventory(this, 54, "Multiple Lores Menu");
        
        String json = editionInv.getEditedSection().getString("lore-pages", "[]");
        this.pages = GSON.fromJson(json, LIST_TYPE);
        
        setupGUI();
    }

    private void setupGUI() {
        ItemStack createBtn = new ItemStack(Material.EMERALD);
        ItemMeta createMeta = createBtn.getItemMeta();
        createMeta.setDisplayName(ChatColor.GREEN + "Create New Page");
        createBtn.setItemMeta(createMeta);
        inventory.setItem(0, createBtn);

        ItemStack backBtn = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backBtn.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back to Main Menu");
        backBtn.setItemMeta(backMeta);
        inventory.setItem(8, backBtn);

        for (int i = 0; i < pages.size(); i++) {
            ItemStack pageBtn = new ItemStack(Material.PAPER);
            ItemMeta pageMeta = pageBtn.getItemMeta();
            pageMeta.setDisplayName(ChatColor.YELLOW + "Page " + (i + 2)); 
            List<String> lore = new ArrayList<>();
            
            for(String line : pages.get(i).split("\n")) {
                lore.add(ChatColor.GRAY + line);
            }
            lore.add("");
            lore.add(ChatColor.GREEN + "Left-Click to add a new line."); // Text updated
            lore.add(ChatColor.RED + "Shift-Right-Click to delete.");
            pageMeta.setLore(lore);
            pageBtn.setItemMeta(pageMeta);
            
            inventory.setItem(9 + i, pageBtn);
        }
    }

    public void open() {
        editionInv.getPlayer().openInventory(inventory);
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public static class GUIListener implements Listener {
        @EventHandler
        public void onClick(InventoryClickEvent event) {
            if (!(event.getInventory().getHolder() instanceof LoreGUIHandler handler)) return;
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            int slot = event.getRawSlot();
            EditionInventory editionInv = handler.editionInv;
            List<String> pages = handler.pages;

            if (slot == 0) {
                player.closeInventory();
                MultiLorePlugin.getInstance().getPendingInputs().put(player.getUniqueId(), new MultiLorePlugin.PendingInput(editionInv, pages.size()));
                player.sendMessage(ChatColor.YELLOW + "Type the lore for the new page in chat. Use \\n for multiple lines.");
                return;
            }

            if (slot == 8) {
                editionInv.open(); 
                return;
            }

            if (slot >= 9 && slot < 9 + pages.size()) {
                int pageIndex = slot - 9;
                if (event.isShiftClick() && event.isRightClick()) {
                    pages.remove(pageIndex);
                    editionInv.getEditedSection().set("lore-pages", GSON.toJson(pages));
                    editionInv.registerTemplateEdition(); 
                    new LoreGUIHandler(editionInv).open();
                } else if (event.isLeftClick()) {
                    player.closeInventory();
                    MultiLorePlugin.getInstance().getPendingInputs().put(player.getUniqueId(), new MultiLorePlugin.PendingInput(editionInv, pageIndex));
                    // Prompt updated
                    player.sendMessage(ChatColor.YELLOW + "Type the lore to append to this page in chat. Use \\n for multiple lines."); 
                }
            }
        }
    }
}