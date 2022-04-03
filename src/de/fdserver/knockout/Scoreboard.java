package de.fdserver.knockout;

import com.google.common.io.Files;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.UUID;

public class Scoreboard {

    public static void setScoreboard(Player p) {
        org.bukkit.scoreboard.Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective obj = sb.registerNewObjective("sidebar", "dummy", "sidebar");
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        obj.setDisplayName("§bKnockOut");
        int score = 8;
        obj.getScore(" ").setScore(score--);
        obj.getScore("§6Dein Spiel").setScore(score--);
        obj.getScore("§a► §fKnockOut").setScore(score--);
        obj.getScore("  ").setScore(score--);
        obj.getScore("   ").setScore(score--);
        obj.getScore("§eDie Karte").setScore(score--);
        obj.getScore("§a► §f" + KnockOut.getCurrentMap().getName()).setScore(score--);
        obj.getScore("    ").setScore(score--);
        obj.getScore("     ").setScore(score--);
        obj.getScore("§bDer Erbauer:").setScore(score--);
        obj.getScore("§a► §f" + KnockOut.getMapBuilder(KnockOut.getCurrentMap().getName())).setScore(score--);
        obj.getScore("      ").setScore(score--);
        obj.getScore("       ").setScore(score--);
        obj.getScore("§aDas Kit").setScore(score--);
        obj.getScore("§a► §f" + KnockOut.getCurrentKit().getName()).setScore(score--);
        p.setScoreboard(sb);
    }

}
