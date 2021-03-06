package de.fdserver.knockout;

import com.google.common.io.Files;
import de.fdserver.knockout.commands.Force;
import de.fdserver.knockout.events.Events;
import de.fdserver.knockout.events.SpecialKits;
import de.fdserver.worldprotect.WorldProtect;
import de.myfdweb.minecraft.api.CoreAPI;
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

        WorldProtect.getWorldProtectConfig().setDamageAllowed(true);
        WorldProtect.getWorldProtectConfig().setExplodeAllowed(true);

        Bukkit.getPluginManager().registerEvents(new Events(), this);
        Bukkit.getPluginManager().registerEvents(new SpecialKits(), this);
        Bukkit.getPluginManager().registerEvents(new Vote(), this);
        getCommand("forcekit").setExecutor(new Force());
        getCommand("forcemap").setExecutor(new Force());
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (--countdown <= 0)
                if (Bukkit.getOnlinePlayers().size() == 0)
                    countdown = defaultCountdown;
                else {
                    if (kitVote.isEmpty())
                        setCurrentKit(getRandomKit());
                    else {
                        ArrayList<Kit> kits = new ArrayList<>(getAllKits());
                        Collections.shuffle(kits);
                        Kit kit = null;
                        for (Kit k : kits)
                            if (kit == null || getKitVotes(kit) < getKitVotes(k))
                                kit = k;
                        setCurrentKit(kit);
                    }
                    if (mapVote.isEmpty())
                        setCurrentMap(getRandomMap());
                    else {
                        ArrayList<String> maps = new ArrayList<>(getAllMaps());
                        Collections.shuffle(maps);
                        String map = null;
                        for (String m : maps)
                            if (map == null || getMapVotes(map) < getMapVotes(m))
                                map = m;
                        setCurrentMap(map);
                    }
                }
            String time = (countdown > 60 ? " " + countdown / 60 + " Minuten" : "") + (countdown % 60 == 0 ? "" : " " + countdown % 60 + " Sekunden");
            for (Player p : Bukkit.getOnlinePlayers())
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("§aKit-/Mapwechsel in" + time));
        }, 0, 20);
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
        currentMap.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        currentMap.setGameRule(GameRule.DISABLE_RAIDS, true);
        currentMap.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        currentMap.setGameRule(GameRule.COMMAND_BLOCK_OUTPUT, false);
        currentMap.setGameRule(GameRule.DO_ENTITY_DROPS, false);
        currentMap.setGameRule(GameRule.DO_FIRE_TICK, false);
        currentMap.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        currentMap.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        currentMap.setGameRule(GameRule.DROWNING_DAMAGE, false);
        currentMap.setGameRule(GameRule.FALL_DAMAGE, false);
        currentMap.setGameRule(GameRule.FIRE_DAMAGE, false);
        currentMap.setGameRule(GameRule.FREEZE_DAMAGE, false);
        currentMap.setGameRule(GameRule.DO_TILE_DROPS, false);
        currentMap.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        currentMap.setGameRule(GameRule.MOB_GRIEFING, false);
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
        for (Player p : mapVote.keySet())
            if (mapVote.get(p).equals(map))
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
        for (Player p : kitVote.keySet())
            if (kitVote.get(p).equals(kit))
                votes++;
        return votes;
    }

    public static void die(Player p) {
        Player p2 = Events.getLastDamager(p);
        Bukkit.broadcastMessage("§c☠ " + p.getDisplayName() + (p2 == null ? "" : " §c\uD83D\uDDE1 " + p2.getDisplayName()));
        streak.put(p, 0);
        Events.resetLastDamage(p);
        if (p2 != null) {
            p2.playSound(p2.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
            if (!getCurrentKit().isInfinite())
                getCurrentKit().addKitItem(p2);
            streak.put(p2, streak.getOrDefault(p2, 0) + 1);
            if (streak.get(p2) % 5 == 0)
                Bukkit.broadcastMessage(CoreAPI.getPrefix("KnockOut") + p2.getDisplayName() + " §ahat nun eine " + streak.get(p2) + "er Kill-Streak!");
        }
        respawn(p);
    }

    public static void respawn(Player p) {
        p.teleport(getCurrentMap().getSpawnLocation().add(0.5, 0, 0.5));
        getCurrentKit().apply(p);
        Scoreboard.setScoreboard(p);
    }
}
