package de.fdserver.knockout.events;

import de.fdserver.knockout.KnockOut;
import org.bukkit.GameMode;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;

import java.util.HashMap;

public class Events implements Listener {

    private static HashMap<Player, Long> lastDamage = new HashMap<>();
    private static HashMap<Player, Player> lastDamager = new HashMap<>();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onSpawnFireball(EntitySpawnEvent e) {
        if (e.getEntityType().equals(EntityType.FIREBALL)) {
            ((Fireball) e.getEntity()).setIsIncendiary(false);
            ((Fireball) e.getEntity()).setYield(10);
        }
    }

    @EventHandler
    public void onIgnition(BlockBurnEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onExplode(BlockExplodeEvent e) {
        e.blockList().clear();
        ((Explosive) e.getBlock()).setIsIncendiary(false);
        for (Entity en : e.getBlock().getWorld().getNearbyEntities(e.getBlock().getLocation(), 2.5, 2.5, 2.5))
            en.setVelocity(en.getVelocity().setY(1.5D));
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        e.blockList().clear();
        ((Explosive) e.getEntity()).setIsIncendiary(false);
        for (Entity en : e.getEntity().getNearbyEntities(2.5, 2.5, 2.5))
            en.setVelocity(en.getVelocity().setY(1.5D));
    }

    @EventHandler
    public void onKillEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager().getType().equals(EntityType.PLAYER) && !e.getEntity().getType().equals(EntityType.PLAYER)) {
            if (!((Player) e.getDamager()).getGameMode().equals(GameMode.CREATIVE))
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteractFrame(PlayerInteractAtEntityEvent e) {
        if (e.getRightClicked().getType().equals(EntityType.ITEM_FRAME) && !e.getPlayer().getGameMode().equals(GameMode.CREATIVE))
            e.setCancelled(true);
    }

    public static void resetLastDamage(Player p) {
        lastDamage.remove(p);
        lastDamager.remove(p);
    }

    public static Player getLastDamager(Player p) {
        if (lastDamage.getOrDefault(p, System.currentTimeMillis()) < System.currentTimeMillis() - 15 * 1000) {
            lastDamage.remove(p);
            lastDamager.remove(p);
        }
        return lastDamager.getOrDefault(p, null);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.setGameMode(GameMode.ADVENTURE);
        KnockOut.respawn(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        KnockOut.streak.remove(p);
        lastDamage.remove(p);
        lastDamager.remove(p);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo() != null && e.getTo().getY() < 0)
            KnockOut.die(e.getPlayer());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        e.getEntity().spigot().respawn();
        KnockOut.die(e.getEntity());
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (e.getCause().equals(EntityDamageEvent.DamageCause.DROWNING) || e.getCause().equals(EntityDamageEvent.DamageCause.FIRE) ||
                e.getCause().equals(EntityDamageEvent.DamageCause.FIRE_TICK) || e.getCause().equals(EntityDamageEvent.DamageCause.FALL) ||
                e.getCause().equals(EntityDamageEvent.DamageCause.HOT_FLOOR) || e.getCause().equals(EntityDamageEvent.DamageCause.LAVA))
            e.setCancelled(true);
        if (e.getEntity() instanceof Player && e.getEntity().getLocation().getY() > 90)
            e.setCancelled(true);
        e.setDamage(0);
        if (e.getEntity() instanceof Player p && e.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK) && ((EntityDamageByEntityEvent) e).getDamager() instanceof Player p2) {
            lastDamage.put(p, System.currentTimeMillis());
            lastDamager.put(p, p2);
        }
    }

    @EventHandler
    public void onHunger(FoodLevelChangeEvent e) {
        if (e.getFoodLevel() != 20)
            e.setFoodLevel(20);
    }

}
