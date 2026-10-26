package org.example;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.util.function.Consumer;

/** Подписка на вход в AuthMe (LoginEvent) без зависимости при компиляции. */
public final class AuthMeHook {

    private AuthMeHook() {
    }

    /** @return true, если AuthMe есть и подписка удалась */
    @SuppressWarnings("unchecked")
    public static boolean onLogin(Main plugin, Consumer<Player> action) {
        if (Bukkit.getPluginManager().getPlugin("AuthMe") == null) return false;
        try {
            Class<? extends Event> login = (Class<? extends Event>) Class.forName("fr.xephi.authme.events.LoginEvent");
            Listener owner = new Listener() { };
            EventExecutor exec = (listener, event) -> {
                if (!login.isInstance(event)) return;
                try {
                    Player p = (Player) event.getClass().getMethod("getPlayer").invoke(event);
                    if (p != null) action.accept(p);
                } catch (ReflectiveOperationException ex) {
                    plugin.getLogger().warning("AuthMe LoginEvent: " + ex.getMessage());
                }
            };
            Bukkit.getPluginManager().registerEvent(login, owner, EventPriority.MONITOR, exec, plugin, true);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
}
