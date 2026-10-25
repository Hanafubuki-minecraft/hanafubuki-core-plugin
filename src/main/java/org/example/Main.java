package org.example;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {

        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();
        Msg.load(this);

        getServer().getPluginManager().registerEvents(new CommandVisibility(this), this);
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);

        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("home").setExecutor(new HomeCommand(this));

        TpaCommand tpa = new TpaCommand(this);
        getCommand("tpa").setExecutor(tpa);
        getCommand("tpa").setTabCompleter(tpa);
        getCommand("tpaaccept").setExecutor(new TpaAcceptCommand(this));
        getCommand("tpadeny").setExecutor(new TpaDenyCommand(this));

        getCommand("resources").setExecutor(new ResourcesCommand(this));

        WhisperCommand msg = new WhisperCommand(this, false);
        getCommand("msg").setExecutor(msg);
        getCommand("msg").setTabCompleter(msg);
        getCommand("reply").setExecutor(new WhisperCommand(this, true));

        getLogger().info("Plugin enabled");
    }
}
