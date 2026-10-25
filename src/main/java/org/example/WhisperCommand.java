package org.example;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.example.Msg.ph;

/** /msg (tell, w, whisper, m) и /r — личные сообщения в стиле сервера вместо ванильного шёпота. */
public class WhisperCommand implements TabExecutor {

    private static final Map<UUID, UUID> lastPartner = new ConcurrentHashMap<>();

    private final Main plugin;
    private final boolean reply;

    public WhisperCommand(Main plugin, boolean reply) {
        this.plugin = plugin;
        this.reply = reply;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player target;
        String text;

        if (reply) {
            if (!(sender instanceof Player p)) return true;
            if (args.length < 1) {
                Msg.send(sender, "whisper.reply-usage");
                return true;
            }
            UUID partner = lastPartner.get(p.getUniqueId());
            target = partner == null ? null : Bukkit.getPlayer(partner);
            if (target == null) {
                Msg.send(sender, "whisper.no-reply");
                return true;
            }
            text = String.join(" ", args);
        } else {
            if (args.length < 2) {
                Msg.send(sender, "whisper.usage");
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                Msg.send(sender, "whisper.not-found", ph("name", args[0]));
                return true;
            }
            text = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        }

        if (target.equals(sender)) {
            Msg.send(sender, "whisper.self");
            return true;
        }

        String fromName = sender instanceof Player p ? p.getName() : Msg.raw("whisper.console-name");
        sender.sendMessage(Msg.get("whisper.to", ph("name", target.getName()), ph("message", text)));
        target.sendMessage(Msg.get("whisper.from", ph("name", fromName), ph("message", text)));
        target.playSound(target, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.35f, 1.7f);
        plugin.getLogger().info("[PM] " + fromName + " -> " + target.getName() + ": " + text);

        if (sender instanceof Player p) {
            lastPartner.put(p.getUniqueId(), target.getUniqueId());
            lastPartner.put(target.getUniqueId(), p.getUniqueId());
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (reply || args.length != 1) return List.of();
        return null; // стандартная подсказка ников онлайн-игроков
    }
}
