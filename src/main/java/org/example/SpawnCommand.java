package org.example;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.boss.BarColor;

public class SpawnCommand implements CommandExecutor {

    private final Main plugin;

    public SpawnCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        TeleportEffects.start(
                player,
                plugin,
                Bukkit.getWorlds().get(0).getSpawnLocation(),
                "Телепортация на спавн",
                BarColor.GREEN
        );

        return true;
    }
}