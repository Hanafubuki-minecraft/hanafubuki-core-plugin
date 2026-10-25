package org.example;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

import static org.example.Msg.ph;

public class TpaAcceptCommand implements CommandExecutor {

    private final Main plugin;

    public TpaAcceptCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player target)) return true;

        var req = TeleportRequestManager.requests.remove(target);
        if (req == null || !req.from.isOnline()) {
            Msg.send(target, "tpa.none");
            return true;
        }
        Player from = req.from;

        Msg.send(target, "tpa.accepted-target", ph("name", from.getName()));
        Msg.send(from, "tpa.accepted-sender", ph("name", target.getName()));

        TeleportEffects.start(from, plugin, target.getLocation(), "teleport.bar-tpa", ph("name", target.getName()));
        return true;
    }
}
