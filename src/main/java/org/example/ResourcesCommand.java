package org.example;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

/**
 * /resources — телепорт в мир ресурсов в случайную безопасную точку
 * с тем же отсчётом и проверкой движения, что у /spawn и /home.
 * Сам мир (создание, сброс, правила) — плагин hanafubuki-resource-world.
 */
public class ResourcesCommand implements CommandExecutor {

    private final Main plugin;

    public ResourcesCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!(sender instanceof Player player)) return true;

        String worldName = plugin.getConfig().getString("resource-world.world", "resources");
        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            Msg.send(player, "resources.unavailable");
            return true;
        }

        Msg.send(player, "resources.searching");

        SafeLocationFinder.find(
                world,
                plugin.getConfig().getInt("resource-world.min-radius", 200),
                plugin.getConfig().getInt("resource-world.radius", 3000),
                plugin.getConfig().getInt("resource-world.max-attempts", 12)
        ).whenComplete((target, err) -> Bukkit.getScheduler().runTask(plugin, () -> {

            if (!player.isOnline()) return;

            if (target == null) {
                Msg.send(player, "resources.failed");
                return;
            }

            TeleportEffects.start(player, plugin, target, "teleport.bar-resources");
        }));

        return true;
    }
}
