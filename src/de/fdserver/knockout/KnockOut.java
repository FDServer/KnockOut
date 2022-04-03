package de.fdserver.knockout;

import com.google.common.io.Files;
import de.fdserver.knockout.commands.Force;
import de.fdserver.knockout.commands.Troll;
import de.fdserver.knockout.events.Events;
import de.fdserver.knockout.events.SpecialKits;
import de.myfdweb.minecraft.itemsapi.Pages;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.io.FileUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class KnockOut extends JavaPlugin {

    public static final String PERMISSION_ERROR = "§cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this in error.";
    public static final String PERMISSION_FORCE_KIT = "fdserver.knockout.forcekit";
    public static final String PERMISSION_FORCE_MAP = "fdserver.knockout.forcemap";
    public static final String PERMISSION_TROLL = "fdserver.troll";
    private static final File mapFolder = new File("maps");
    private static World currentMap;
    private static Kit currentKit;
    private static int defaultCountdown, countdown = 0;
    public static HashMap<Player, Integer> streak = new HashMap<>();
    public static HashMap<Player, String> mapVote = new HashMap<>();
    public static HashMap<Player, Kit> kitVote = new HashMap<>();

    @Override
    public void onEnable() {
        reloadConfig();
        getConfig().addDefault("roundTime", 10 * 60);
        getConfig().options().copyDefaults(true);
        saveConfig();

        defaultCountdown = getConfig().getInt("roundTime");
        setCurrentKit(getRandomKit());
        setCurrentMap(getRandomMap());
        for (World w : Bukkit.getWorlds())
            if (!w.getName().equals("world") && !w.equals(getCurrentMap())) {
                Bukkit.unloadWorld(w, false);
                try {
                    FileUtils.deleteDirectory(new File(w.getName()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        Bukkit.getPluginManager().registerEvents(new Events(), this);
        Bukkit.getPluginManager().registerEvents(new SpecialKits(), this);
        Bukkit.getPluginManager().registerEvents(new Vote(), this);
        getCommand("forcekit").setExecutor(new Force());
        getCommand("forcemap").setExecutor(new Force());
        getCommand("troll").setExecutor(new Troll());
        getCommand("notroll").setExecutor(new Troll());
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (--countdown <= 0) {
                if (kitVote.isEmpty())
                    setCurrentKit(getRandomKit());
                else {
                    ArrayList<Kit> kits = new ArrayList<>(getAllKits());
                    Collections.shuffle(kits);
                    Kit kit = null;
                    for(Kit k : kits)
                        if(kit == null || getKitVotes(kit) < getKitVotes(k))
                            kit = k;
                    setCurrentKit(kit);
                }
                if (mapVote.isEmpty())
                    setCurrentMap(getRandomMap());
                else {
                    ArrayList<String> maps = new ArrayList<>(getAllMaps());
                    Collections.shuffle(maps);
                    String map = null;
                    for(String m : maps)
                        if(map == null || getMapVotes(map) < getMapVotes(m))
                            map = m;
                    setCurrentMap(map);
                }
            }
            String time = (countdown > 60 ? " " + countdown / 60 + " Minuten" : "") + (countdown % 60 == 0 ? "" : " " + countdown % 60 + " Sekunden");
            for (Player p : Bukkit.getOnlinePlayers())
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§aKit-/Mapwechsel in" + time));
        }, 0, 20);
    }

    @Override
    public void onDisable() {

    }

    public static World getCurrentMap() {
        return currentMap;
    }

    public static void setCurrentMap(String map) {
        World lastMap = currentMap;
        File from = new File("maps/" + map), to = new File(map);
        if (!from.exists() || !from.isDirectory())
            return;
        try {
            if (to.exists())
                FileUtils.deleteDirectory(to);
            FileUtils.copyDirectory(from, to);
        } catch (IOException e) {
            e.printStackTrace();
        }
        currentMap = Bukkit.createWorld(new WorldCreator(map));
        for (Player p : Bukkit.getOnlinePlayers())
            respawn(p);
        if (lastMap != null) {
            Bukkit.unloadWorld(lastMap, false);
            try {
                FileUtils.deleteDirectory(new File(lastMap.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        countdown = defaultCountdown;
    }

    public static List<String> getAllMaps() {
        return Arrays.asList(Objects.requireNonNull(mapFolder.list()));
    }

    public static String getRandomMap() {
        List<String> maps = new ArrayList<>();
        for (String map : getAllMaps())
            if (getCurrentMap() == null || !getCurrentMap().getName().equals(map))
                maps.add(map);
        return maps.get((int) (Math.random() * maps.size()));
    }

    public static String getMapBuilder(String map) {
        try {
            File f = new File("maps/" + map + "/builder.txt");
            if (!f.exists())
                return "unbekannt";
            OfflinePlayer op = Bukkit.getOfflinePlayer(UUID.fromString(Files.readLines(f, Charset.defaultCharset()).get(0)));
            return op.getName() == null ? "unbekannt" : op.getName();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "unbekannt";
    }

    public static int getMapVotes(String map) {
        int votes = 0;
        for(Player p : mapVote.keySet())
            if(mapVote.get(p).equals(map))
                votes++;
        return votes;
    }

    public static Kit getCurrentKit() {
        return currentKit;
    }

    public static void setCurrentKit(Kit kit) {
        currentKit = kit;
        for (Player p : Bukkit.getOnlinePlayers())
            getCurrentKit().apply(p);
        countdown = defaultCountdown;
    }

    public static List<Kit> getAllKits() {
        return Arrays.asList(Kit.values());
    }

    public static Kit getRandomKit() {
        List<Kit> kits = new ArrayList<>();
        for (Kit kit : getAllKits())
            if (getCurrentKit() == null || !getCurrentKit().equals(kit))
                kits.add(kit);
        return kits.get((int) (Math.random() * kits.size()));
    }

    public static int getKitVotes(Kit kit) {
        int votes = 0;
        for(Player p : kitVote.keySet())
            if(kitVote.get(p).equals(kit))
                votes++;
        return votes;
    }

    public static void die(Player p) {
        Player p2 = Events.getLastDamager(p);
        Bukkit.broadcastMessage("§c☠ " + p.getDisplayName() + (p2 == null ? "" : " \uD83D\uDDE1 " + p2.getName()));
        streak.put(p, 0);
        Events.resetLastDamage(p);
        if (p2 != null) {
            p2.playSound(p2.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            if (!getCurrentKit().isInfinite())
                getCurrentKit().addKitItem(p2);
            streak.put(p2, streak.getOrDefault(p2, 0) + 1);
            if (streak.get(p2) % 5 == 0)
                Bukkit.broadcastMessage(p2.getDisplayName() + " §ahat nun eine " + streak.get(p2) + "er Kill-Streak!");
        }
        respawn(p);
    }

    public static void respawn(Player p) {
        p.teleport(getCurrentMap().getSpawnLocation().add(0.5, 0, 0.5));
        getCurrentKit().apply(p);
        Scoreboard.setScoreboard(p);
    }
}
