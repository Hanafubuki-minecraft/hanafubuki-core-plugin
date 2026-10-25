package org.example;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

import static org.example.Msg.ph;

public class TpaCommand implements TabExecutor {

    private final Main plugin;

    public TpaCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length < 1) {
            Msg.send(player, "tpa.usage");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            Msg.send(player, "tpa.not-found", ph("name", args[0]));
            return true;
        }
        if (target.equals(player)) {
            Msg.send(player, "tpa.self");
            return true;
        }

        TeleportRequestManager.requests.put(target, new TeleportRequestManager.Request(player, target));

        Msg.send(player, "tpa.sent", ph("name", target.getName()));
        Msg.send(target, "tpa.request", ph("name", player.getName()));
        Msg.line(target, "tpa.request-hint");
        target.playSound(target, org.bukkit.Sound.BLOCK_NOTE_BLOCK_CHIME, 0.6f, 1.4f);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1) return List.of();
        String p = args[0].toLowerCase(Locale.ROOT);
        return Bukkit.getOnlinePlayers().stream().map(Player::getName)
                .filter(n -> !n.equals(sender.getName()) && n.toLowerCase(Locale.ROOT).startsWith(p)).toList();
    }
}
