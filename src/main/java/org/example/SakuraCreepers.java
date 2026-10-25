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
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Сакура-криперы: с шансом крипер вместо взрыва рассыпается облаком лепестков — без урона и разрушений.
 * config.yml → sakura-creepers.
 */
public class SakuraCreepers implements Listener {

    private final Main plugin;

    public SakuraCreepers(Main plugin) {
        this.plugin = plugin;
    }

    /** Момент, когда крипер дошипел и вот-вот взорвётся — ловим до самого взрыва. */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPrime(ExplosionPrimeEvent e) {
        if (!(e.getEntity() instanceof Creeper creeper)) return;
        if (!roll(creeper)) return;
        e.setCancelled(true);
        e.setRadius(0);
        Location at = creeper.getLocation().add(0, 1, 0);
        creeper.remove();
        burst(at);
        log(at);
    }

    /** Подстраховка: если взрыв всё же случился мимо prime-события — то же решение здесь. */
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExplode(EntityExplodeEvent e) {
        if (!(e.getEntity() instanceof Creeper creeper) || !creeper.isValid()) return;
        if (!roll(creeper)) return;
        e.setCancelled(true);
        e.blockList().clear();
        Location at = creeper.getLocation().add(0, 1, 0);
        creeper.remove();
        burst(at);
        log(at);
    }

    private boolean roll(Creeper creeper) {
        var c = plugin.getConfig();
        if (!c.getBoolean("sakura-creepers.enabled", true)) return false;
        if (creeper.isPowered() && !c.getBoolean("sakura-creepers.charged-too", false)) return false;
        return ThreadLocalRandom.current().nextDouble() < c.getDouble("sakura-creepers.chance", 0.25);
    }

    private void log(Location at) {
        if (plugin.getConfig().getBoolean("sakura-creepers.log", true)) {
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
