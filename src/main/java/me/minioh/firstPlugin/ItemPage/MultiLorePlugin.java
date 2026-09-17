package me.minioh.firstPlugin.ItemPage;

import lombok.Getter;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MultiLorePlugin extends JavaPlugin {

    @Getter private static MultiLorePlugin instance;
    @Getter private final Map<UUID, PendingInput> pendingInputs = new ConcurrentHashMap<>();
    LorePagesStat lorePagesStat = new LorePagesStat();
    @Override
    public void onLoad(){
        MMOItems.plugin.getStats().register(lorePagesStat);
        getLogger().info("Registered custom stat: LORE_PAGES");
    }
    @Override
    public void onEnable() {
        instance = this;

        ConfigManager.load(this);

        if (getCommand("multilores") != null) 
            getCommand("multilores").setExecutor(new ReloadCommand(this));


        Bukkit.getPluginManager().registerEvents(new PageFlipListener(), this);
        Bukkit.getPluginManager().registerEvents(new ChatInputListener(), this);
        Bukkit.getPluginManager().registerEvents(new LoreGUIHandler.GUIListener(), this);
        Bukkit.getPluginManager().registerEvents(new ItemCreationListener(), this);

        getLogger().info("MultiLorePlugin has been enabled successfully.");
    }

    public record PendingInput(net.Indyuce.mmoitems.gui.edition.EditionInventory inv, int targetPageIndex) {}
}