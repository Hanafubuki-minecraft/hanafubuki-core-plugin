package org.example;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.List;
import java.util.Locale;

import static org.example.Msg.ph;

/** /hanafubuki reload | version — только для опов (hanafubuki.admin). */
public class AdminCommand implements TabExecutor {

    private final Main plugin;

    public AdminCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String sub = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload" -> {
                plugin.reloadAll();
                Msg.send(sender, "admin.reloaded");
            }
            case "nether", "end", "eyes" -> {
                String key = sub.equals("eyes") ? "end-eyes" : sub;
                if (args.length >= 2) {
                    boolean on = args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true");
                    plugin.getConfig().set("dimensions." + key, on);
                    plugin.saveConfig();
                }
                boolean now = plugin.getConfig().getBoolean("dimensions." + key, true);
                Msg.send(sender, "dimensions.status-" + sub, ph("state", Msg.raw(now ? "dimensions.on" : "dimensions.off")));
            }
            case "version" -> Msg.send(sender, "admin.version", ph("version", plugin.getPluginMeta().getVersion()));
            default -> Msg.send(sender, "admin.usage", ph("label", label));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            String p = args[0].toLowerCase(Locale.ROOT);
            return List.of("reload", "version", "nether", "end", "eyes").stream().filter(s -> s.startsWith(p)).toList();
        }
        if (args.length == 2 && List.of("nether", "end", "eyes").contains(args[0].toLowerCase(Locale.ROOT))) {
            return List.of("on", "off").stream().filter(s -> s.startsWith(args[1].toLowerCase(Locale.ROOT))).toList();
        }
        return List.of();
    }
}
