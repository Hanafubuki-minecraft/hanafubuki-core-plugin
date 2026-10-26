package org.example;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;

/**
 * Закрыть Незер / Энд и запретить вставлять Око Края. Переключается опами: /hf nether|end|eyes on|off.
 * config.yml → dimensions.
 */
public class Dimensions implements Listener {

    private final Main plugin;

    public Dimensions(Main plugin) {
        this.plugin = plugin;
    }

    public boolean open(World.Environment env) {
        return switch (env) {
            case NETHER -> plugin.getConfig().getBoolean("dimensions.nether", true);
            case THE_END -> plugin.getConfig().getBoolean("dimensions.end", true);
            default -> true;
        };
    }

    private boolean bypass(Player p) {
        return p.isOp() && plugin.getConfig().getBoolean("dimensions.ops-bypass", true);
    }

    /** Можно ли игроку оказаться в этом мире (порталы, /tpa, /home). */
    public boolean allowed(Player p, World target) {
        return target == null || bypass(p) || open(target.getEnvironment());
    }

    public void sendClosed(Player p, World.Environment env) {
        Msg.send(p, env == World.Environment.NETHER ? "dimensions.nether-closed" : "dimensions.end-closed");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPortal(PlayerPortalEvent e) {
        World.Environment target = switch (e.getCause()) {
            case NETHER_PORTAL -> World.Environment.NETHER;
            case END_PORTAL -> World.Environment.THE_END;
            default -> null;
        };
        if (target == null || bypass(e.getPlayer())) return;
        // из закрытого измерения выйти можно всегда — закрываем только вход
        if (e.getFrom().getWorld() != null && e.getFrom().getWorld().getEnvironment() == target) return;
        if (!open(target)) {
            e.setCancelled(true);
            sendClosed(e.getPlayer(), target);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent e) {
        if (e.getTo() == null || e.getTo().getWorld() == null) return;
        World.Environment env = e.getTo().getWorld().getEnvironment();
        if (e.getFrom().getWorld() != null && e.getFrom().getWorld().getEnvironment() == env) return;
        if (!open(env)) e.setCancelled(true);
    }

    /** Подстраховка: любой другой телепорт игрока в закрытое измерение (кроме опов). */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getTo() == null || e.getTo().getWorld() == null) return;
        World to = e.getTo().getWorld();
        if (e.getFrom().getWorld() != null && e.getFrom().getWorld().equals(to)) return;
        if (!allowed(e.getPlayer(), to)) {
            e.setCancelled(true);
            sendClosed(e.getPlayer(), to.getEnvironment());
        }
    }

    /** Око Края в рамку портала. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEye(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getHand() != EquipmentSlot.HAND && e.getHand() != EquipmentSlot.OFF_HAND) return;
        Block b = e.getClickedBlock();
        if (b == null || b.getType() != Material.END_PORTAL_FRAME) return;
        if (e.getItem() == null || e.getItem().getType() != Material.ENDER_EYE) return;
        if (plugin.getConfig().getBoolean("dimensions.end-eyes", true) || bypass(e.getPlayer())) return;
        e.setCancelled(true);
        Msg.send(e.getPlayer(), "dimensions.eyes-closed");
    }
}
