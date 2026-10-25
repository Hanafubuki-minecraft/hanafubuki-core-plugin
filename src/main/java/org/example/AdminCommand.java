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
            case "version" -> Msg.send(sender, "admin.version", ph("version", plugin.getPluginMeta().getVersion()));
            default -> Msg.send(sender, "admin.usage", ph("label", label));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1) return List.of();
        String p = args[0].toLowerCase(Locale.ROOT);
        return List.of("reload", "version").stream().filter(s -> s.startsWith(p)).toList();
    }
}
