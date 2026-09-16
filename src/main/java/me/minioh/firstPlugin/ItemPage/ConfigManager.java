package me.minioh.firstPlugin.ItemPage;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    private static final Map<Integer, List<String>> pageFormats = new HashMap<>();
    @Getter private static int maxAutoPages = 1;

    public static void load(Plugin plugin) {
        pageFormats.clear();
        maxAutoPages = 1;

        File file = new File(plugin.getDataFolder(), "mmomultilores.yml");
        if (!file.exists()) plugin.saveResource("mmomultilores.yml", false);

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection pages = config.getConfigurationSection("pages");

        if (pages == null) return;

        for (String key : pages.getKeys(false)) {
            try {
                int pageNum = Integer.parseInt(key);
                maxAutoPages = Math.max(maxAutoPages, pageNum);
                pageFormats.put(pageNum, pages.getStringList(key));
            } catch (NumberFormatException ignored) {}
        }
    }

    public static List<String> getPageFormat(int page) {
        return pageFormats.get(page);
    }
}