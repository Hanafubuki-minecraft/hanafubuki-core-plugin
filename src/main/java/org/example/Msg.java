package org.example;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Тексты плагина из messages.yml (MiniMessage). Стиль — STYLE.md в hanafubuki-brand. */
public final class Msg {

    private static final MiniMessage MM = MiniMessage.miniMessage();
    private static YamlConfiguration lang = new YamlConfiguration();

    private Msg() {
    }

    public static void load(JavaPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) plugin.saveResource("messages.yml", false);
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        InputStream in = plugin.getResource("messages.yml");
        if (in != null) {
            y.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(in, StandardCharsets.UTF_8)));
        }
        lang = y;
    }

    public static String raw(String key) {
        return lang.getString(key, key);
    }

    public static List<String> list(String key) {
        return lang.getStringList(key);
    }

    public static Component get(String key, TagResolver... r) {
        return MM.deserialize(raw(key), r);
    }

    public static Component parse(String text, TagResolver... r) {
        return MM.deserialize(text, r);
    }

    /** С префиксом ❀. */
    public static void send(CommandSender to, String key, TagResolver... r) {
        to.sendMessage(MM.deserialize(raw("prefix") + raw(key), r));
    }

    /** Без префикса — подсказки, строки списков. */
    public static void line(CommandSender to, String key, TagResolver... r) {
        to.sendMessage(MM.deserialize(raw(key), r));
    }

    /** Плейсхолдер без разбора тегов — ник или текст игрока не может внедрить разметку. */
    public static TagResolver ph(String key, Object value) {
        return Placeholder.unparsed(key, String.valueOf(value));
    }
}
