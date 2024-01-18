package bash.reactioner.game.state;

import bash.reactioner.SkyWarsPlugin;
import bash.reactioner.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class StartingState implements State {
    private int secondsLeft;
    private BukkitTask task;

    @Override
    public void start(Game game) {
        secondsLeft = SkyWarsPlugin.getInstance().getSecondsBeforeStart();
        task = Bukkit.getScheduler().runTaskTimer(SkyWarsPlugin.getInstance(), () -> {
            secondsLeft--;
            game.getPlayers().forEach(p -> {
                SkyWarsPlugin.getInstance().getScoreboardsManager().showScoreboard(p, SkyWarsPlugin.getInstance().getStartingScoreboard());
                p.setLevel(secondsLeft);
                p.setExp((float) secondsLeft / SkyWarsPlugin.getInstance().getSecondsBeforeStart());
            });
            if (secondsLeft <= 0) game.setState(new ActiveState());
            else if (secondsLeft == 30) game.broadcast(SkyWarsPlugin.getInstance().getStartIn30(), null);
            else if (secondsLeft == 15) game.broadcast(SkyWarsPlugin.getInstance().getStartIn15(), null);
            else if (secondsLeft == 10) game.broadcast(SkyWarsPlugin.getInstance().getStartIn10(), null);
            else if (secondsLeft == 5) game.broadcast(SkyWarsPlugin.getInstance().getStartIn5(), null);
            else if (secondsLeft == 3) game.broadcast(SkyWarsPlugin.getInstance().getStartIn3(), null);
            else if (secondsLeft == 2) game.broadcast(SkyWarsPlugin.getInstance().getStartIn2(), null);
            else if (secondsLeft == 1) game.broadcast(SkyWarsPlugin.getInstance().getStartIn1(), null);
        }, 0, 20);
    }

    public int getSecondsLeft() {
        return secondsLeft;
    }

    @Override
    public List<String> getScoreboard() {
        return SkyWarsPlugin.getInstance().getStartingScoreboard();
    }

    @Override
    public void cancel(Game game) {
        if (task != null && !task.isCancelled()) task.cancel();
    }

    @Override
    public void onJoin(Player p, Game game) {

    }

    @Override
    public void onQuit(Player p, Game game) {
        if (game.getOnline() < game.getMinPlayers()) game.setState(new WaitingState());
    }

    @Override
    public boolean canDamage() {
        return false;
    }
}
