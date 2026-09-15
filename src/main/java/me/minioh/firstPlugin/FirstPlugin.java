package me.minioh.firstPlugin;

import me.minioh.firstPlugin.commands.*;
import me.minioh.firstPlugin.listeners.MMOInventoryEvent;
import me.minioh.firstPlugin.listeners.OPSnowball;
import me.minioh.firstPlugin.listeners.PlayerDeathListener;
import me.minioh.firstPlugin.listeners.PlayerJoinLeaveListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class FirstPlugin extends JavaPlugin implements Listener {

    private static FirstPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        PluginManager pm = getServer().getPluginManager();
        System.out.println("Dreamweaver Core 1.0 has started.");

        saveDefaultConfig();
        getConfig().options().copyDefaults(true);

        pm.registerEvents(new OPSnowball(), this);                      //register event
        pm.registerEvents(new PlayerJoinLeaveListener(), this);
        pm.registerEvents(new MMOInventoryEvent(), this);
        pm.registerEvents(new PlayerDeathListener(), this);

        getCommand("hjhj").setExecutor(new MiniohPluginCommands());     //register command
        getCommand("repeat").setExecutor(new RepeatCommand());
        getCommand("sm").setExecutor(new SuperStrongEffect());
        getCommand("setspawnn").setExecutor(new SetSpawn());
        getCommand("spawnn").setExecutor(new TpSpawn());
        getCommand("dreamreload").setExecutor(new Dreamreload());
        getCommand("zspawn").setExecutor(new SummonZombieCooldown());
    }

    @Override
    public void onDisable() {
        instance = null;
        System.out.println(" Dreamweaver Core 1.0 has stopped, see ya!");
    }

    public static FirstPlugin getInstance() {
        return instance;
    }
}
