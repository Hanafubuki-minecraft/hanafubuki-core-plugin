package org.example;

import io.papermc.paper.event.world.WorldGameRuleChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.example.Msg.ph;

/**
 * Одинаковые gamerules во всех мирах сервера.
 * Источник — основной мир (или gamerule-sync.source-world). Работает только по событиям: запуск,
 * загрузка мира, изменение правила командой — никакой нагрузки в фоне.
 */
public class GameRuleSync implements Listener {

    private final Main plugin;
    private boolean propagating;

    public GameRuleSync(Main plugin) {
        this.plugin = plugin;
    }

    private boolean enabled() {
        return plugin.getConfig().getBoolean("gamerule-sync.enabled", true);
    }

    private Set<String> excluded() {
        Set<String> s = new HashSet<>();
        for (String r : plugin.getConfig().getStringList("gamerule-sync.exclude")) s.add(r.toLowerCase(Locale.ROOT));
        return s;
    }

    private World source() {
        String name = plugin.getConfig().getString("gamerule-sync.source-world", "");
        World w = name == null || name.isBlank() ? null : Bukkit.getWorld(name);
        return w != null ? w : Bukkit.getWorlds().get(0);
    }

    /** Скопировать все правила из основного мира во все остальные. */
    public void syncAll() {
        if (!enabled()) return;
        World from = source();
        Set<String> skip = excluded();
        int changed = 0;
        for (World to : Bukkit.getWorlds()) {
            if (to.equals(from)) continue;
            changed += copyAll(from, to, skip);
        }
        if (changed > 0) plugin.getLogger().info("Gamerules synced from " + from.getName() + " (" + changed + " changes)");
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        if (!enabled()) return;
        World from = source();
        if (e.getWorld().equals(from)) return;
        int changed = copyAll(from, e.getWorld(), excluded());
        if (changed > 0) plugin.getLogger().info("Gamerules applied to new world " + e.getWorld().getName() + " (" + changed + ")");
    }

    /** Правило поменяли командой в любом мире — применяем это значение везде. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRuleChange(WorldGameRuleChangeEvent e) {
        if (!enabled() || propagating) return;
        GameRule<?> rule = e.getGameRule();
        if (excluded().contains(rule.getName().toLowerCase(Locale.ROOT))) return;
        World changedWorld = e.getWorld();
        CommandSender sender = e.getCommandSender();
        // значение применяется после события — распространяем на следующем тике
        Bukkit.getScheduler().runTask(plugin, () -> {
            propagating = true;
            int changed = 0;
            try {
                for (World to : Bukkit.getWorlds()) {
                    if (to.equals(changedWorld)) continue;
                    if (copy(changedWorld, to, rule)) changed++;
                }
            } finally {
                propagating = false;
            }
            Object value = changedWorld.getGameRuleValue(rule);
            // во скольких мирах правило теперь именно такое (а не сколько пришлось поменять)
            int worlds = 0;
            for (World w : Bukkit.getWorlds()) if (value != null && value.equals(w.getGameRuleValue(rule))) worlds++;
            int total = Bukkit.getWorlds().size();
            plugin.getLogger().info("Gamerule " + rule.getName() + "=" + value + ": " + worlds + "/" + total + " worlds (" + changed + " changed)");
            if (sender != null) {
                Msg.send(sender, "gamerules.synced", ph("rule", rule.getName()), ph("value", value),
                        ph("count", worlds), ph("total", total));
            }
        });
    }

    @SuppressWarnings("deprecation")
    private static int copyAll(World from, World to, Set<String> skip) {
        int n = 0;
        for (GameRule<?> rule : GameRule.values()) {
            if (skip.contains(rule.getName().toLowerCase(Locale.ROOT))) continue;
            if (copy(from, to, rule)) n++;
        }
        return n;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static boolean copy(World from, World to, GameRule rule) {
        Object value = from.getGameRuleValue(rule);
        if (value == null || value.equals(to.getGameRuleValue(rule))) return false;
        return to.setGameRule(rule, value);
    }
}
