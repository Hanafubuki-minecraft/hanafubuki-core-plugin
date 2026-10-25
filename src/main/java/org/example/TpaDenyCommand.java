package org.example;

import org.bukkit.command.*;
import org.bukkit.entity.Player;

import static org.example.Msg.ph;

public class TpaDenyCommand implements CommandExecutor {

    private final Main plugin;

    public TpaDenyCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player target)) return true;

        var req = TeleportRequestManager.requests.remove(target);
        if (req == null) {
            Msg.send(target, "tpa.none");
            return true;
        }
        Msg.send(target, "tpa.denied-target", ph("name", req.from.getName()));
        if (req.from.isOnline()) Msg.send(req.from, "tpa.denied-sender", ph("name", target.getName()));
        return true;
    }
}
