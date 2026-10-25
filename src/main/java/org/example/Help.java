package org.example;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.*;

import static org.example.Msg.ph;

/**
 * /help — все команды, которые этот игрок реально может выполнить, по разделам и со страницами.
 * Опы видят всё, обычные игроки — только доступное им. Разделы и описания — в messages.yml → help.
 */
public final class Help {

    private static final String VANILLA_DESC = "A Mojang provided command.";

    private final Main plugin;
    private final CommandVisibility visibility;

    public Help(Main plugin, CommandVisibility visibility) {
        this.plugin = plugin;
        this.visibility = visibility;
    }

    private record Group(String title, List<Command> commands) {
    }

    public void show(Player p, int page) {
        List<Component> lines = new ArrayList<>();
        for (Group g : collect(p)) {
            if (g.commands().isEmpty()) continue;
            lines.add(Msg.get("help.group", ph("title", g.title())));
            for (Command c : g.commands()) lines.add(entry(c));
        }

        int perPage = Math.max(5, plugin.getConfig().getInt("help.per-page", 14));
        int pages = Math.max(1, (lines.size() + perPage - 1) / perPage);
        page = Math.min(Math.max(1, page), pages);

        p.sendMessage(Component.empty());
        p.sendMessage(Msg.get("help.header", ph("page", page), ph("pages", pages)));
        for (Component l : lines.subList((page - 1) * perPage, Math.min(lines.size(), page * perPage))) p.sendMessage(l);

        String label = Commands.name("help");
        String prev = page > 1 ? Msg.raw("help.prev").replace("<target>", "/" + label + " " + (page - 1)) : Msg.raw("help.prev-off");
        String next = page < pages ? Msg.raw("help.next").replace("<target>", "/" + label + " " + (page + 1)) : Msg.raw("help.next-off");
        if (pages > 1) p.sendMessage(Msg.parse(Msg.raw("help.nav").replace("<prev>", prev).replace("<next>", next)));
        Msg.line(p, "help.hint");
        if (p.isOp()) Msg.line(p, "help.op-hint");
    }

    private List<Group> collect(Player p) {
        // все команды, доступные игроку: основное имя, без префиксов и дублей-алиасов
        Map<String, Command> available = new TreeMap<>();
        Set<String> hide = new HashSet<>();
        for (String s : plugin.getConfig().getStringList("help.hide")) hide.add(s.toLowerCase(Locale.ROOT));
        for (Map.Entry<String, Command> e : Bukkit.getCommandMap().getKnownCommands().entrySet()) {
            String label = e.getKey().toLowerCase(Locale.ROOT);
            Command c = e.getValue();
            if (label.contains(":") || !label.equalsIgnoreCase(c.getName())) continue;
            if (hide.contains(label) || label.equals(Commands.name("help"))) continue;
            if (!c.testPermissionSilent(p)) continue;
            if (!p.isOp() && (visibility.isHidden(label) || !visibility.availableTo(p, label))) continue;
            available.put(label, c);
        }

        List<Group> groups = new ArrayList<>();
        ConfigurationSection gs = msgSection("help.groups");
        if (gs != null) {
            for (String key : gs.getKeys(false)) {
                List<Command> list = new ArrayList<>();
                for (String id : gs.getStringList(key + ".commands")) {
                    String label = (Commands.has(id) ? Commands.name(id) : id).toLowerCase(Locale.ROOT);
                    Command c = available.remove(label);
                    if (c != null) list.add(c);
                }
                groups.add(new Group(gs.getString(key + ".title", key), list));
            }
        }
        groups.add(new Group(Msg.raw("help.other"), new ArrayList<>(available.values())));
        return groups;
    }

    private Component entry(Command c) {
        String name = c.getName().toLowerCase(Locale.ROOT);
        String usage = Msg.has("help.usage." + name) ? Msg.raw("help.usage." + name) : "/" + name;
        String desc = Msg.has("help.desc." + name) ? Msg.raw("help.desc." + name) : c.getDescription();
        if (desc == null || desc.isBlank() || desc.equals(VANILLA_DESC)) desc = "";
        String suggest = "/" + name + (usage.contains("[") ? " " : "");

        String tpl = Msg.raw(desc.isEmpty() ? "help.entry-plain" : "help.entry")
                .replace("<suggest>", suggest.replace("'", ""))
                .replace("<usage>", Msg.escape(usage))
                .replace("<desc>", Msg.escape(desc));
        return Msg.parse(tpl);
    }

    private static ConfigurationSection msgSection(String path) {
        return Msg.section(path);
    }
}
