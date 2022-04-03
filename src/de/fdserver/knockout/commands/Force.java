package de.fdserver.knockout.commands;

import de.fdserver.knockout.Kit;
import de.fdserver.knockout.KnockOut;
import de.myfdweb.minecraft.itemsapi.ItemBuilder;
import de.myfdweb.minecraft.itemsapi.Pages;
import joptsimple.internal.Strings;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Force implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String s, @NonNull String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                Player p = (Player) sender;
                Pages pages = new Pages();
                if (command.getName().equals("forcekit")) {
                    if (!sender.hasPermission(KnockOut.PERMISSION_FORCE_KIT)) {
                        sender.sendMessage(KnockOut.PERMISSION_ERROR);
                        return true;
                    }
                    pages.setTitle("Kit auswählen");
                    for (Kit kit : KnockOut.getAllKits())
                        pages.addContent(new Pages.Item(new ItemBuilder(kit.getMaterial()).setDisplayName(kit.getDisplayName()).build(), (p1, item) -> {
                            KnockOut.setCurrentKit(kit);
                            p1.sendMessage("§aDu hast das Kit zu " + kit.getName() + " geändert.");
                        }));
                } else if (command.getName().equals("forcemap")) {
                    if (!sender.hasPermission(KnockOut.PERMISSION_FORCE_MAP)) {
                        sender.sendMessage(KnockOut.PERMISSION_ERROR);
                        return true;
                    }
                    pages.setTitle("Map auswählen");
                    for (String map : KnockOut.getAllMaps())
                        pages.addContent(new Pages.Item(new ItemBuilder(Material.FILLED_MAP).setDisplayName("§a" + map).setLore("§7von " + KnockOut.getMapBuilder(map)).build(), (p1, item) -> {
                            KnockOut.setCurrentMap(map);
                            p1.sendMessage("§aDie hast die Map zu " + map + " geändert.");
                        }));
                }
                pages.open(p);
            } else {
                if (command.getName().equals("forcekit")) {
                    if (!sender.hasPermission(KnockOut.PERMISSION_FORCE_KIT)) {
                        sender.sendMessage(KnockOut.PERMISSION_ERROR);
                        return true;
                    }
                    sender.sendMessage("§cSyntax: §a/forcekit <Kit>");
                    List<String> kits = new ArrayList<>();
                    for (Kit k : KnockOut.getAllKits())
                        kits.add(k.getName());
                    sender.sendMessage("§aEs gibt folgende Kits zur Auswahl: " + Strings.join(kits, ", "));
                } else if (command.getName().equals("forcemap")) {
                    if (!sender.hasPermission(KnockOut.PERMISSION_FORCE_MAP)) {
                        sender.sendMessage(KnockOut.PERMISSION_ERROR);
                        return true;
                    }
                    sender.sendMessage("§cSyntax: §a/forcekit <Kit>");
                    sender.sendMessage("§aEs gibt folgende Kits zur Auswahl: " + Strings.join(KnockOut.getAllMaps(), ", "));
                }
            }
        } else {
            if (command.getName().equals("forcekit")) {
                if (!sender.hasPermission(KnockOut.PERMISSION_FORCE_KIT)) {
                    sender.sendMessage(KnockOut.PERMISSION_ERROR);
                    return true;
                }
                String kit = Strings.join(args, " ");
                for (Kit k : KnockOut.getAllKits())
                    if (k.getName().equalsIgnoreCase(kit)) {
                        KnockOut.setCurrentKit(k);
                        sender.sendMessage("§aDu hast das Kit zu " + k.getName() + " geändert.");
                        return true;
                    }
                sender.sendMessage("§cEs wurde kein Kit mit diesem Namen gefunden.");
            } else if (command.getName().equals("forcemap")) {
                if (!sender.hasPermission(KnockOut.PERMISSION_FORCE_MAP)) {
                    sender.sendMessage(KnockOut.PERMISSION_ERROR);
                    return true;
                }
                String map = Strings.join(args, " ");
                for (String m : KnockOut.getAllMaps())
                    if (m.equalsIgnoreCase(map)) {
                        KnockOut.setCurrentMap(map);
                        sender.sendMessage("§aDu hast die Map zu " + m + " geändert.");
                        return true;
                    }
                sender.sendMessage("§cEs wurde keine Map mit diesem Namen gefunden.");
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        if (command.getName().equals("forcekit")) {
            if (!sender.hasPermission(KnockOut.PERMISSION_FORCE_KIT))
                return new ArrayList<>();
            ArrayList<String> completions = new ArrayList<>();
            String kit = Strings.join(args, " ");
            for (Kit k : KnockOut.getAllKits())
                if (k.getName().toLowerCase().startsWith(kit.toLowerCase()))
                    completions.add(k.getName());
            return completions;
        } else if (command.getName().equals("forcemap")) {
            if (!sender.hasPermission(KnockOut.PERMISSION_FORCE_MAP))
                return new ArrayList<>();
            ArrayList<String> completions = new ArrayList<>();
            String map = Strings.join(args, " ");
            for (String m : KnockOut.getAllMaps())
                if (m.toLowerCase().startsWith(map.toLowerCase()))
                    completions.add(m);
            return completions;
        }
        return new ArrayList<>();
    }

}
