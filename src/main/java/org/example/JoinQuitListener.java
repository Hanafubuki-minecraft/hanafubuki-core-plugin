package org.example;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Вход и выход в стиле сервера, для всех в игре.
 * С AuthMe «заходит» объявляется только после входа по паролю; «покидает» — только у тех, о ком объявили вход.
 * config.yml → chat.join-quit, тексты — messages.yml → join / first-join / quit.
 */
public class JoinQuitListener implements Listener {

    private final Main plugin;
    private final Map<UUID, Boolean> pending = new ConcurrentHashMap<>(); // uuid → первый ли вход
    private final Set<UUID> announced = ConcurrentHashMap.newKeySet();
    private boolean authMe;

    public JoinQuitListener(Main plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        authMe = AuthMeHook.onLogin(plugin, p -> {
            Boolean first = pending.remove(p.getUniqueId());
            if (first != null) announceJoin(p, first);
        });
    }

    private boolean on() {
        return plugin.getConfig().getBoolean("chat.join-quit", true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        if (!on()) return;
        e.joinMessage(null);
        Player p = e.getPlayer();
        boolean first = !p.hasPlayedBefore();
        if (authMe) pending.put(p.getUniqueId(), first);
        else announceJoin(p, first);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent e) {
        if (!on()) return;
        e.quitMessage(null);
        UUID id = e.getPlayer().getUniqueId();
        pending.remove(id);
        if (announced.remove(id)) {
            Bukkit.broadcast(Msg.get("quit", Msg.ph("name", e.getPlayer().getName())));
        }
    }

    private void announceJoin(Player p, boolean first) {
        if (!p.isOnline()) return;
        announced.add(p.getUniqueId());
        String tpl = Msg.raw(first ? "first-join" : "join").replace("<name>", Msg.escape(p.getName()));
        Bukkit.broadcast(Msg.parse(tpl));
    }
}
