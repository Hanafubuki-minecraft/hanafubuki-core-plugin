package org.example;

import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

public class HomeCommand implements CommandExecutor {

    private final Main plugin;

    public HomeCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        Location home = player.getRespawnLocation();
        if (home == null) {
            Msg.send(player, "home.no-bed");
            return true;
        }
        TeleportEffects.start(player, plugin, home, "teleport.bar-home");
        return true;
    }
}
