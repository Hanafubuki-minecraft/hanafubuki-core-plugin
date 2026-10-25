package org.example;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static org.example.Msg.ph;

/** Сообщения о входе и выходе в стиле сервера. Выключается в config.yml → chat.join-quit. */
public class JoinQuitListener implements Listener {

    private final Main plugin;

    public JoinQuitListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!plugin.getConfig().getBoolean("chat.join-quit", true)) return;
        String key = e.getPlayer().hasPlayedBefore() ? "join" : "first-join";
        e.joinMessage(Msg.get(key, ph("name", e.getPlayer().getName())));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (!plugin.getConfig().getBoolean("chat.join-quit", true)) return;
        e.quitMessage(Msg.get("quit", ph("name", e.getPlayer().getName())));
    }
}
