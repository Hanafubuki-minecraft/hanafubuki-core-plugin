package org.example;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.EventExecutor;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Книга новичка «Добро пожаловать на Hanafubuki». Выдаётся при первом входе — после входа в AuthMe,
 * если он стоит (иначе AuthMe спрячет инвентарь до логина), и сразу открывается.
 * Снова получить: /guide. Тексты — messages.yml → guide, настройки — config.yml → guide.
 */
public class Guide implements Listener, CommandExecutor {

    private final Main plugin;
    private final Set<UUID> pending = ConcurrentHashMap.newKeySet();
    private boolean authMe;

    public Guide(Main plugin) {
        this.plugin = plugin;
    }

    /** Подписаться на LoginEvent AuthMe без зависимости при компиляции. */
    @SuppressWarnings("unchecked")
    public void hookAuthMe() {
        if (Bukkit.getPluginManager().getPlugin("AuthMe") == null) return;
        try {
            Class<? extends Event> login = (Class<? extends Event>) Class.forName("fr.xephi.authme.events.LoginEvent");
            EventExecutor exec = (listener, event) -> {
                if (!login.isInstance(event)) return;
                try {
                    Player p = (Player) event.getClass().getMethod("getPlayer").invoke(event);
                    if (p != null && pending.remove(p.getUniqueId())) {
                        Bukkit.getScheduler().runTaskLater(plugin, () -> give(p, true), 20L);
                    }
                } catch (ReflectiveOperationException ex) {
                    plugin.getLogger().warning("AuthMe LoginEvent: " + ex.getMessage());
                }
            };
            Bukkit.getPluginManager().registerEvent(login, this, EventPriority.MONITOR, exec, plugin, true);
            authMe = true;
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().warning("AuthMe found, but its LoginEvent isn't — the guide is given on join");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!plugin.getConfig().getBoolean("guide.give-on-first-join", true)) return;
        Player p = e.getPlayer();
        if (p.hasPlayedBefore()) return;
        if (authMe) {
            pending.add(p.getUniqueId());
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, () -> give(p, true), 40L);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player p) give(p, false);
        return true;
    }

    public ItemStack book() {
        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) item.getItemMeta();
        meta.setTitle(Msg.raw("guide.title"));
        meta.setAuthor(Msg.raw("guide.author"));
        // страницы книги бежевые — у них своя, тёмная палитра (config.yml → guide.style)
        var palette = Style.resolver(plugin.getConfig().getConfigurationSection("guide.style"));
        for (String page : Msg.list("guide.pages")) meta.addPages(Msg.parseWithStyle(page, palette));
        item.setItemMeta(meta);
        return item;
    }

    private void give(Player p, boolean firstJoin) {
        if (!p.isOnline()) return;
        ItemStack book = book();
        if (firstJoin && plugin.getConfig().getBoolean("guide.open-on-first-join", true)) {
            p.openBook(book);
        }
        if (!firstJoin || plugin.getConfig().getBoolean("guide.put-in-inventory", true)) {
            var left = p.getInventory().addItem(book);
            left.values().forEach(i -> p.getWorld().dropItemNaturally(p.getLocation(), i));
            Msg.send(p, firstJoin ? "guide.given-first" : "guide.given");
        }
    }
}
