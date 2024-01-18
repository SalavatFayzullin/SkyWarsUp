package bash.reactioner.scoreboards;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

public class ScoreboardsManager {
    public void showScoreboard(Player p, List<String> content) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.getObjective("SW");
        if (objective == null) objective = scoreboard.registerNewObjective("SW", "dummy", Component.text("SW stats"));
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        for (int i = 0; i < content.size(); i++) {
            objective.getScore(PlaceholderAPI.setPlaceholders(p, content.get(i))).setScore(content.size() - i - 1);
        }
        p.setScoreboard(scoreboard);
    }
}
