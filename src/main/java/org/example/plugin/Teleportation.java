package org.example.plugin;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.example.Msg;

public class Teleportation {

    public void teleport(Player player, Location location) {
        player.teleport(location);
        player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 1.3f);
        Msg.send(player, "teleport.done");
    }
}
