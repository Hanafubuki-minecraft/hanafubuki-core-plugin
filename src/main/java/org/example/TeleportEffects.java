package org.example;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.example.plugin.Teleportation;

public class TeleportEffects {

    /** Отсчёт с боссбаром и частицами; сошёл с места больше чем на 3 блока — отмена. */
    public static void start(Player player, Main plugin, Location target, String titleKey, TagResolver... title) {

        if (target.getWorld() != null && !plugin.dimensions().allowed(player, target.getWorld())) {
            plugin.dimensions().sendClosed(player, target.getWorld().getEnvironment());
            return;
        }

        Location start = player.getLocation().clone();
        Teleportation tp = new Teleportation();

        BossBar bar = BossBar.bossBar(Msg.get(titleKey, title), 1f, BossBar.Color.PINK, BossBar.Overlay.PROGRESS);
        player.showBossBar(bar);

        int totalTicks = 40;

        new BukkitRunnable() {

            int tick = 0;

            @Override
            public void run() {

                if (!player.isOnline()) {
                    player.hideBossBar(bar);
                    cancel();
                    return;
                }

                double dx = Math.abs(player.getLocation().getX() - start.getX());
                double dz = Math.abs(player.getLocation().getZ() - start.getZ());

                if (dx > 3 || dz > 3 || !player.getWorld().equals(start.getWorld())) {
                    player.hideBossBar(bar);
                    Msg.send(player, "teleport.cancelled");
                    player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 0.5f);
                    cancel();
                    return;
                }

                spawnMagicSphere(player, tick);

                if (tick % 5 == 0) {
                    spawnBorder(player, start);
                }

                tick++;

                bar.progress((float) Math.max(0, 1.0 - (double) tick / totalTicks));

                if (tick >= totalTicks) {
                    player.hideBossBar(bar);
                    tp.teleport(player, target);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static void spawnMagicSphere(Player player, int tick) {

        double radius = 1.4;
        Location center = player.getLocation().add(0, 1, 0);

        double pulse = 0.2 * Math.sin(tick * 0.25);
        double r = radius + pulse;

        for (int i = 0; i < 45; i++) {

            double theta = Math.random() * Math.PI * 2;
            double phi = Math.acos(2 * Math.random() - 1);

            double x = r * Math.sin(phi) * Math.cos(theta);
            double y = r * Math.cos(phi);
            double z = r * Math.sin(phi) * Math.sin(theta);

            player.getWorld().spawnParticle(
                    Particle.ENCHANT,
                    center.clone().add(x, y, z),
                    1,
                    0, 0, 0,
                    0
            );
        }
    }

    private static void spawnBorder(Player player, Location center) {

        World w = player.getWorld();

        double x = center.getX();
        double y = center.getY();
        double z = center.getZ();

        double r = 3;

        double[][] points = {
                {x - r, y, z - r},
                {x - r, y, z + r},
                {x + r, y, z - r},
                {x + r, y, z + r},

                {x - r, y, z},
                {x + r, y, z},
                {x, y, z - r},
                {x, y, z + r}
        };

        for (double[] p : points) {
            w.spawnParticle(Particle.END_ROD, p[0], p[1] + 0.2, p[2], 2, 0, 0, 0, 0);
        }
    }
}
