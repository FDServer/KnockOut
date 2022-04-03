package de.fdserver.knockout.events;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class SpecialKits implements Listener {

    private static boolean pay(Player p, Material m, int count) {
        for (int i = 0; i < p.getInventory().getContents().length && count > 0; i++) {
            ItemStack is = p.getInventory().getContents()[i];
            if (is != null && is.getType().equals(m))
                if (is.getAmount() > 1) {
                    int j = Math.min(is.getAmount(), count);
                    is.setAmount(is.getAmount() - j);
                    count -= j;
                } else {
                    p.getInventory().setItem(i, new ItemStack(Material.AIR));
                    count--;
                }
        }
        return count == 0;
    }

    @EventHandler
    public void onEnterhaken(PlayerFishEvent e) {
        if (e.getState().equals(PlayerFishEvent.State.CAUGHT_ENTITY))
            e.setCancelled(true);
        else if (!e.getState().equals(PlayerFishEvent.State.IN_GROUND) && !e.getState().equals(PlayerFishEvent.State.FAILED_ATTEMPT))
            return;
        e.getHook().remove();
        Block block = e.getHook().getLocation().clone().subtract(0, 1, 0).getBlock();
        Block blockhere = e.getHook().getLocation().getBlock();
        if (!block.getType().equals(Material.AIR) || !blockhere.getType().equals(Material.AIR)) {
            Player player = e.getPlayer();
            Location lc = player.getLocation();
            lc.setY(lc.getY() + 0.5D);
            player.teleport(lc);
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0F, 1.0F);

            Location to = e.getHook().getLocation();
            double d = to.distance(lc);

            Vector v = player.getVelocity();
            v.setX((1.5D + 0.07D * d) * (to.getX() - lc.getX()) / d);
            v.setY((1.5D + 0.03D * d) * (to.getY() - lc.getY()) / d - 0.5D * -0.08D * d);
            v.setZ((1.5D + 0.07D * d) * (to.getZ() - lc.getZ()) / d);
            player.setVelocity(v);
        }
    }

    @EventHandler
    public void onJetPack(PlayerInteractEvent e) {
        if (e.getItem() == null || !e.getItem().getType().equals(Material.GLASS_BOTTLE))
            return;
        e.setCancelled(true);
        Player p = e.getPlayer();
        boolean notpayed = true;
        //if (!Troll.isTrolling(p))
        pay(p, Material.GLASS_BOTTLE, 1);
        p.setVelocity(p.getLocation().getDirection().multiply(3D).setY(1D));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0F, 1.0F);
    }

    @EventHandler
    public void onFallschirm(PlayerInteractEvent e) {
        if (e.getItem() == null || !e.getItem().getType().equals(Material.FEATHER))
            return;
        e.setCancelled(true);
        Player p = e.getPlayer();
        //if (!Troll.isTrolling(p))
        pay(p, Material.FEATHER, 1);
        p.setVelocity(p.getLocation().getDirection().setY(3D));
        if (p.hasPotionEffect(PotionEffectType.LEVITATION))
            p.removePotionEffect(PotionEffectType.LEVITATION);
        p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 10 * 20, 253, true, false));
        p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1.0F, 1.0F);
    }

    @EventHandler
    public void onGranate(ProjectileHitEvent e) {
        if (e.getEntity().getType().equals(EntityType.EGG)) {
            TNTPrimed tnt = (TNTPrimed) e.getEntity().getWorld().spawnEntity(e.getEntity().getLocation(),
                    EntityType.PRIMED_TNT);
            tnt.setFuseTicks(1);
            tnt.setIsIncendiary(false);
        }
    }

    @EventHandler
    public void onSpawnChicken(EntitySpawnEvent e) {
        if (e.getEntity().getType().equals(EntityType.CHICKEN))
            e.setCancelled(true);
    }

    @EventHandler
    public void onFireball(PlayerInteractEvent e) {
        if (e.getItem() != null && e.getItem().getType().equals(Material.FIRE_CHARGE)
                && (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR))) {
            e.setCancelled(true);
            Player p = e.getPlayer();
            pay(p, Material.FIRE_CHARGE, 1);
            p.launchProjectile(Fireball.class);
        }
    }

}
