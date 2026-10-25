package org.example;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.HashSet;
import java.util.List;
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
    private Set<String> hiddenPlugins = new HashSet<>();
    private Set<String> bedrockAllowed = new HashSet<>();
    private Set<String> bedrockAllowed = new HashSet<>();
    private boolean customHelp = true;

    public CommandVisibility(Main plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        Set<String> set = new HashSet<>();
        // свой список из config.yml + встроенный список по умолчанию (новые версии плагина могут его дополнять)
        List<String> mine = plugin.getConfig().getStringList("hide-from-non-ops.commands");
        List<String> defaults = plugin.getConfig().getDefaults() == null ? List.of()
                : plugin.getConfig().getDefaults().getStringList("hide-from-non-ops.commands");
        for (String s : mine) set.add(s.toLowerCase(Locale.ROOT).replace("/", ""));
        if (plugin.getConfig().getBoolean("hide-from-non-ops.include-defaults", true)) {
            for (String s : defaults) set.add(s.toLowerCase(Locale.ROOT).replace("/", ""));
        }
        hidden = set;
        hideNamespaced = plugin.getConfig().getBoolean("hide-from-non-ops.namespaced", true);
        Set<String> plugins = new HashSet<>();
        for (String s : plugin.getConfig().getStringList("hide-from-non-ops.plugins")) plugins.add(s.toLowerCase(Locale.ROOT));
        if (plugin.getConfig().getBoolean("hide-from-non-ops.include-defaults", true) && plugin.getConfig().getDefaults() != null) {
            for (String s : plugin.getConfig().getDefaults().getStringList("hide-from-non-ops.plugins")) plugins.add(s.toLowerCase(Locale.ROOT));
        }
        hiddenPlugins = plugins;
        Set<String> br = new HashSet<>();
        for (String s : plugin.getConfig().getStringList("hide-from-non-ops.bedrock-allowed")) br.add(s.toLowerCase(Locale.ROOT));
        bedrockAllowed = br;
        Set<String> br = new HashSet<>();
        for (String s : plugin.getConfig().getStringList("hide-from-non-ops.bedrock-allowed")) br.add(s.toLowerCase(Locale.ROOT));
        bedrockAllowed = br;
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

    /** Скрыта ли команда для этого игрока (Bedrock-игрокам часть команд оставляем — например, /geyser). */
    public boolean isHiddenFor(Player p, String label) {
        String l = label.toLowerCase(Locale.ROOT);
        if (l.contains(":")) l = l.substring(l.indexOf(':') + 1);
        if (ChatStyle.isBedrock(p) && bedrockAllowed.contains(l)) return false;
        return isHidden(label);
    }

    public boolean isHidden(String label) {
        if (customHelp && isHelp(label)) return false;   // свой /help виден всем
        String l = label.toLowerCase(Locale.ROOT);
        if (hidden.contains(l) || (hideNamespaced && l.contains(":"))) return true;
        return fromHiddenPlugin(l);
    }

    /** Команда принадлежит плагину из hide-from-non-ops.plugins (WorldEdit, CoreProtect…). */
    private boolean fromHiddenPlugin(String label) {
        if (hiddenPlugins.isEmpty()) return false;
        Command c = Bukkit.getCommandMap().getCommand(label);
        if (c instanceof PluginIdentifiableCommand pic) {
            return hiddenPlugins.contains(pic.getPlugin().getName().toLowerCase(Locale.ROOT));
        }
        return false;
    }

    /** Скрыта ли команда для этого игрока (Bedrock-игрокам можно то, что в bedrock-allowed). */
    public boolean isHiddenFor(Player p, String label) {
        if (ChatStyle.isBedrock(p) && bedrockAllowed.contains(label.toLowerCase(Locale.ROOT))) return false;
        return isHidden(label);
    }

    /** Список команд для Tab: убираем скрытые и те, что игроку на деле недоступны. */
    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent e) {
        Player p = e.getPlayer();
        if (p.isOp()) return;
        e.getCommands().removeIf(label -> isHiddenFor(p, label) || !availableTo(p, label));
    }

    /**
     * Доступна ли команда игроку на самом деле. Если у команды задано право — решает оно.
     * Если нет (плагин проверяет права внутри команды), ищем среди прав этого плагина
     * соответствующее команде (…capes, ….command.capes, ….capes.…) и проверяем его.
     */
    public boolean availableTo(Player p, String label) {
        String l = label.toLowerCase(Locale.ROOT);
        if (l.contains(":")) l = l.substring(l.indexOf(':') + 1);
        Command c = Bukkit.getCommandMap().getCommand(l);
        if (c == null) return true;
        if (c.getPermission() != null && !c.getPermission().isBlank()) return c.testPermissionSilent(p);
        if (!(c instanceof PluginIdentifiableCommand pic)) return true;
        String prefix = pic.getPlugin().getName().toLowerCase(Locale.ROOT) + ".";
        String name = c.getName().toLowerCase(Locale.ROOT);
        for (Permission perm : Bukkit.getPluginManager().getPermissions()) {
            String n = perm.getName().toLowerCase(Locale.ROOT);
            if (!n.startsWith(prefix)) continue;
            boolean matches = n.endsWith("." + name) || n.contains("." + name + ".");
            if (!matches || perm.getDefault() == PermissionDefault.TRUE) continue;
            if (!p.hasPermission(perm)) return false;
        }
        return true;
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
        if (!isHiddenFor(p, label)) return;
        e.setCancelled(true);
        Msg.line(p, "unknown-command");
    }
}
