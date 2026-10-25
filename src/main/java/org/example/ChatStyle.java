package org.example;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Общий чат в стиле сервера: «Ник › текст». Клик по нику подставляет /msg,
 * у Bedrock-игроков — отметка. Формат — messages.yml → chat; выключение — config.yml → chat.style.
 */
public class ChatStyle implements Listener {

    private final Main plugin;

    public ChatStyle(Main plugin) {
        this.plugin = plugin;
    }

    /** Floodgate выдаёт Bedrock-игрокам UUID вида 00000000-0000-0000-…. */
    public static boolean isBedrock(Player p) {
        return p.getUniqueId().getMostSignificantBits() == 0;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChat(AsyncChatEvent e) {
        if (!plugin.getConfig().getBoolean("chat.style", true)) return;
        e.renderer(ChatRenderer.viewerUnaware((source, displayName, message) -> {
            String name = Msg.escape(source.getName());
            String tpl = Msg.raw("chat.format").replace("<name>", name);
            String mark = isBedrock(source) ? Msg.raw("chat.bedrock-mark") : "";
            return Msg.parse(tpl,
                    Placeholder.parsed("bedrock", mark),
                    Placeholder.component("message", message));
        }));
    }
}
