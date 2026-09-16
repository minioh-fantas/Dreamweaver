package me.minioh.firstPlugin.ItemPage;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static final Map<String, Integer> statPages = new HashMap<>();
    @Getter private static int maxAutoPages = 1;

    public static void load(Plugin plugin) {
        statPages.clear();
        maxAutoPages = 1;

        File file = new File(plugin.getDataFolder(), "mmomultilores.yml");
        if (!file.exists()) plugin.saveResource("mmomultilores.yml", false);

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection pages = config.getConfigurationSection("auto-pages");

        if (pages == null) return;

        for (String key : pages.getKeys(false)) {
            try {
                int pageNum = Integer.parseInt(key.replace("Page_", ""));
                maxAutoPages = Math.max(maxAutoPages, pageNum);

                for (String stat : pages.getStringList(key))
                    statPages.put(stat.toUpperCase(), pageNum);
            } catch (NumberFormatException ignored) {}
        }
    }

    public static int getStatPage(String statId) {
        return statPages.getOrDefault(statId.toUpperCase(), -1);
    }
}