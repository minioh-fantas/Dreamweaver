package me.minioh.firstPlugin.ItemPage;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LoreGUIHandler implements InventoryHolder {

    private final Inventory inventory;
    private final EditionInventory editionInv;
    private final Map<String, List<String>> pages;

    public LoreGUIHandler(EditionInventory editionInv) {
        this.editionInv = editionInv;
        this.inventory = Bukkit.createInventory(this, 54, "Lore Pages Menu");
        this.pages = LorePagesStat.parseJson(editionInv.getEditedSection().getString("lore-pages", "{}"));
        setupGUI();
    }

    private void setupGUI() {
        ItemStack createBtn = new ItemStack(Material.EMERALD);
        ItemMeta createMeta = createBtn.getItemMeta();
        createMeta.setDisplayName(ChatColor.GREEN + "Create New Custom Page");
        createBtn.setItemMeta(createMeta);
        inventory.setItem(0, createBtn);

        ItemStack backBtn = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backBtn.getItemMeta();
        backMeta.setDisplayName(ChatColor.RED + "Back to Main Menu");
        backBtn.setItemMeta(backMeta);
        inventory.setItem(8, backBtn);

        int maxAutoPages = ConfigManager.getMaxAutoPages();
        int maxManualPage = pages.keySet().stream().map(k -> Integer.parseInt(k.replace("page_", ""))).max(Integer::compareTo).orElse(0);
        int totalDisplayPages = Math.max(maxAutoPages, maxManualPage);

        for (int i = 1; i <= totalDisplayPages; i++) {
            ItemStack pageBtn = new ItemStack(Material.PAPER);
            ItemMeta pageMeta = pageBtn.getItemMeta();
            pageMeta.setDisplayName(ChatColor.YELLOW + "Page " + i + (i <= maxAutoPages ? " (Format + Custom)" : " (Custom Only)"));
            
            List<String> lore = new ArrayList<>();
            List<String> customLines = pages.getOrDefault("page_" + i, new ArrayList<>());
            
            if (customLines.isEmpty()) lore.add(ChatColor.GRAY + "No custom lines appended.");
            else for(String line : customLines) lore.add(ChatColor.GRAY + line);
            
            lore.add("");
            lore.add(ChatColor.GREEN + "Left-Click to append line.");
            lore.add(ChatColor.RED + "Shift-Right-Click to clear custom lines.");
            pageMeta.setLore(lore);
            pageBtn.setItemMeta(pageMeta);
            inventory.setItem(8 + i, pageBtn);
        }
    }

    public void open() { editionInv.getPlayer().openInventory(inventory); }

    @NotNull
    @Override
    public Inventory getInventory() { return inventory; }

    public static class GUIListener implements Listener {
        @EventHandler
        public void onClick(InventoryClickEvent event) {
            if (!(event.getInventory().getHolder() instanceof LoreGUIHandler handler)) return;
            event.setCancelled(true);

            Player player = (Player) event.getWhoClicked();
            int slot = event.getRawSlot();
            EditionInventory editionInv = handler.editionInv;
            Map<String, List<String>> pages = handler.pages;

            if (slot == 8) {
                editionInv.open(); return;
            }

            int maxAutoPages = ConfigManager.getMaxAutoPages();
            int maxManualPage = pages.keySet().stream().map(k -> Integer.parseInt(k.replace("page_", ""))).max(Integer::compareTo).orElse(0);
            
            if (slot == 0) {
                int newPage = Math.max(maxAutoPages, maxManualPage) + 1;
                player.closeInventory();
                MultiLorePlugin.getInstance().getPendingInputs().put(player.getUniqueId(), new MultiLorePlugin.PendingInput(editionInv, newPage));
                player.sendMessage(ChatColor.YELLOW + "Type the lore for Page " + newPage + " in chat. Use \\n for multiple lines.");
                return;
            }

            if (slot > 8 && slot <= 8 + Math.max(maxAutoPages, maxManualPage)) {
                int pageIndex = slot - 8;
                String key = "page_" + pageIndex;
                
                if (event.isShiftClick() && event.isRightClick()) {
                    pages.remove(key);
                    editionInv.getEditedSection().set("lore-pages", LorePagesStat.GSON.toJson(pages));
                    editionInv.registerTemplateEdition(); 
                    new LoreGUIHandler(editionInv).open();
                } else if (event.isLeftClick()) {
                    player.closeInventory();
                    MultiLorePlugin.getInstance().getPendingInputs().put(player.getUniqueId(), new MultiLorePlugin.PendingInput(editionInv, pageIndex));
                    player.sendMessage(ChatColor.YELLOW + "Type the lore to append to Page " + pageIndex + " in chat."); 
                }
            }
        }
    }
}