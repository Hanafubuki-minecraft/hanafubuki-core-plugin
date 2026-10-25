package org.example;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Прячет от не-опов служебные команды, открытые по умолчанию всем (/plugins, /version, /help…),
 * и команды с префиксом (minecraft:give, bukkit:plugins…): их нет в Tab, а при вводе — «неизвестная команда».
 */
public class CommandVisibility implements Listener {

    private final Main plugin;
    private Set<String> hidden = new HashSet<>();
    private boolean hideNamespaced = true;
    private boolean customHelp = true;

    public CommandVisibility(Main plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        Set<String> set = new HashSet<>();
        for (String s : plugin.getConfig().getStringList("hide-from-non-ops.commands")) {
            set.add(s.toLowerCase(Locale.ROOT).replace("/", ""));
        }
        hidden = set;
        hideNamespaced = plugin.getConfig().getBoolean("hide-from-non-ops.namespaced", true);
        customHelp = plugin.getConfig().getBoolean("help.enabled", true);
    }

    private Help help;

    public void setHelp(Help help) {
        this.help = help;
    }

    private static boolean isHelp(String label) {
        String l = label.toLowerCase(Locale.ROOT);
        return l.equals(Commands.name("help")) || l.equals("help") || l.equals("?");
    }

    public boolean isHidden(String label) {
        if (customHelp && isHelp(label)) return false;   // свой /help виден всем
        String l = label.toLowerCase(Locale.ROOT);
        return hidden.contains(l) || (hideNamespaced && l.contains(":"));
    }

    /** Список команд для Tab: убираем скрытые. */
    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent e) {
        if (e.getPlayer().isOp()) return;
        e.getCommands().removeIf(this::isHidden);
    }

    /** /help — свой список команд для игроков; скрытые команды — «неизвестная команда». */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        String msg = e.getMessage();
        if (msg.length() < 2) return;
        String label = msg.substring(1).split(" ", 2)[0];

        if (customHelp && isHelp(label)) {
            e.setCancelled(true);
            String[] parts = msg.substring(1).trim().split("\\s+");
            int page = 1;
            if (parts.length > 1) try { page = Integer.parseInt(parts[1]); } catch (NumberFormatException ignored) { }
            help.show(p, page);
            return;
        }

        if (p.isOp()) return;
        if (!isHidden(label)) return;
        e.setCancelled(true);
        Msg.line(p, "unknown-command");
    }
}
