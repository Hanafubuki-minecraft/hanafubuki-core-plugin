package org.example;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
    }

    private boolean isHidden(String label) {
        String l = label.toLowerCase(Locale.ROOT);
        return hidden.contains(l) || (hideNamespaced && l.contains(":"));
    }

    /** Список команд для Tab: убираем скрытые. */
    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent e) {
        if (e.getPlayer().isOp()) return;
        e.getCommands().removeIf(this::isHidden);
    }

    /** Ввод вручную: отвечаем как на несуществующую команду. */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        if (p.isOp()) return;
        String msg = e.getMessage();
        if (msg.length() < 2) return;
        String label = msg.substring(1).split(" ", 2)[0];
        if (!isHidden(label)) return;
        e.setCancelled(true);
        p.sendMessage(Component.translatable("command.unknown.command", NamedTextColor.RED));
    }
}
