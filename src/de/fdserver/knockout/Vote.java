package de.fdserver.knockout;

import de.myfdweb.minecraft.itemsapi.ItemBuilder;
import de.myfdweb.minecraft.itemsapi.Pages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;

public class Vote implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getItem() != null && e.getItem().getItemMeta() != null && e.getItem().getItemMeta().getDisplayName().equals("§aMap-/Kitvote")) {
            e.setCancelled(true);
            Inventory inv = Bukkit.createInventory(null, 27, "§aMap-/Kitvote");
            inv.setItem(11, new ItemBuilder(Material.FILLED_MAP).setDisplayName("§aMap-Vote").build());
            inv.setItem(15, new ItemBuilder(Material.STICK).setDisplayName("§aKit-Vote").build());
            e.getPlayer().openInventory(inv);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (e.getView().getTitle().equals("§aMap-/Kitvote")) {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            Pages pages = new Pages();
            if (e.getCurrentItem() != null && e.getCurrentItem().getItemMeta() != null) {
                if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§aMap-Vote")) {
                    pages.setTitle("Für Map voten");
                    pages.setItem2(new Pages.Item(new ItemBuilder(Material.BARRIER).setDisplayName("§cStimme zurückziehen").build(), (p1, item) -> {
                        KnockOut.mapVote.remove(p);
                        p1.sendMessage("§cDu hast deine Stimme zurückgezogen.");
                    }));
                    for (String map : KnockOut.getAllMaps()) {
                        ArrayList<String> lore = new ArrayList<>();
                        lore.add("§7von " + KnockOut.getMapBuilder(map));
                        if (KnockOut.getMapVotes(map) > 0)
                            lore.add("§a" + KnockOut.getMapVotes(map) + "x dafür gestimmt");
                        if (map.equals(KnockOut.mapVote.getOrDefault(p, null)))
                            lore.add("§aDeine Wahl");
                        pages.addContent(new Pages.Item(new ItemBuilder(Material.FILLED_MAP).setDisplayName("§a" + map).setLore(lore).build(), (p1, item) -> {
                            KnockOut.mapVote.put(p, map);
                            p1.sendMessage("§aDu hast dafür gestimmt die Map " + map + " zu spielen.");
                        }));
                    }
                    pages.open(p);
                } else if (e.getCurrentItem().getItemMeta().getDisplayName().equals("§aKit-Vote")) {
                    pages.setTitle("Für Kit voten");
                    pages.setItem2(new Pages.Item(new ItemBuilder(Material.BARRIER).setDisplayName("§cStimme zurückziehen").build(), (p1, item) -> {
                        KnockOut.kitVote.remove(p);
                        p1.sendMessage("§cDu hast deine Stimme zurückgezogen.");
                    }));
                    for (Kit kit : KnockOut.getAllKits()) {
                        ArrayList<String> lore = new ArrayList<>();
                        if (KnockOut.getKitVotes(kit) > 0)
                            lore.add("§a" + KnockOut.getKitVotes(kit) + "x dafür gestimmt");
                        if (kit.equals(KnockOut.kitVote.getOrDefault(p, null)))
                            lore.add("§aDeine Wahl");
                        pages.addContent(new Pages.Item(new ItemBuilder(kit.getMaterial()).setDisplayName(kit.getDisplayName()).setLore(lore).build(), (p1, item) -> {
                            KnockOut.kitVote.put(p, kit);
                            p1.sendMessage("§aDu hast dafür gestimmt das Kit " + kit.getName() + " zu spielen.");
                        }));
                    }
                    pages.open(p);
                }

            }
        }
    }

}
