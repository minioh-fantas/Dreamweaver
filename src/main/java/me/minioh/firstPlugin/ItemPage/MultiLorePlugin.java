package me.minioh.firstPlugin.ItemPage;

import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MultiLorePlugin extends JavaPlugin {

    private static MultiLorePlugin instance;
    private final Map<UUID, PendingInput> pendingInputs = new ConcurrentHashMap<>();

    @Override
    public void onLoad(){
        MMOItems.plugin.getStats().register(new LorePagesStat());
        getLogger().info("Registered custom stat: LORE_PAGES");
    }
    @Override
    public void onEnable() {
        instance = this;
        // Register listeners
        Bukkit.getPluginManager().registerEvents(new PageFlipListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChatInputListener(), this);
        Bukkit.getPluginManager().registerEvents(new LoreGUIHandler.GUIListener(), this);

        getLogger().info("MultiLorePlugin has been enabled successfully.");
    }

    public static MultiLorePlugin getInstance() {
        return instance;
    }

    public Map<UUID, PendingInput> getPendingInputs() {
        return pendingInputs;
    }

    public record PendingInput(net.Indyuce.mmoitems.gui.edition.EditionInventory inv, int targetPageIndex) {}
}