package org.example;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Сакура-криперы: с шансом крипер вместо взрыва рассыпается облаком лепестков — без урона и разрушений.
 * Ловим момент перед взрывом (ExplosionPrimeEvent), чтобы взрыв вообще не начинался.
 * config.yml → sakura-creepers.
 */
public class SakuraCreepers implements Listener {

    private final Main plugin;

    public SakuraCreepers(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrime(ExplosionPrimeEvent e) {
        if (!(e.getEntity() instanceof Creeper creeper)) return;
        var c = plugin.getConfig();
        if (!c.getBoolean("sakura-creepers.enabled", true)) return;
        if (creeper.isPowered() && !c.getBoolean("sakura-creepers.charged-too", false)) return;
        double chance = c.getDouble("sakura-creepers.chance", 0.25);
        if (ThreadLocalRandom.current().nextDouble() >= chance) return;

        e.setCancelled(true);
        Location at = creeper.getLocation().add(0, 1, 0);
        creeper.remove();
        burst(at);
        if (c.getBoolean("sakura-creepers.log", false)) {
            plugin.getLogger().info("Sakura creeper at " + at.getWorld().getName() + " "
                    + at.getBlockX() + " " + at.getBlockY() + " " + at.getBlockZ());
        }
    }

    private static void burst(Location at) {
        World w = at.getWorld();
        if (w == null) return;
        w.spawnParticle(Particle.CHERRY_LEAVES, at, 260, 1.8, 1.4, 1.8, 0);
        Particle.DustOptions pink = new Particle.DustOptions(Color.fromRGB(0xF7A1C0), 1.6f);
        Particle.DustOptions light = new Particle.DustOptions(Color.fromRGB(0xFFE1EC), 1.2f);
        w.spawnParticle(Particle.DUST, at, 80, 1.2, 1.0, 1.2, 0, pink);
        w.spawnParticle(Particle.DUST, at, 60, 1.6, 1.2, 1.6, 0, light);
        w.playSound(at, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.8f, 1.6f);
        w.playSound(at, Sound.BLOCK_CHERRY_LEAVES_BREAK, 1.0f, 0.9f);
        w.playSound(at, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.7f, 1.4f);
    }
}
