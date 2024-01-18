package bash.reactioner.game.state;

import bash.reactioner.SkyWarsPlugin;
import bash.reactioner.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class RestartingState implements State {
    private BukkitTask task;
    private int timer;

    @Override
    public void start(Game game) {
        timer = SkyWarsPlugin.getInstance().getRestartDuration();
        task = Bukkit.getScheduler().runTaskTimer(SkyWarsPlugin.getInstance(), () -> {
            timer--;
            if (timer <= 0) {
                game.updateStats();
                game.resetState();
                game.resetArena();
            }
            game.getInfo().setTitle("Reset in %d seconds".formatted(timer));
            game.getInfo().setProgress((double) timer / SkyWarsPlugin.getInstance().getRestartDuration());
        }, 0, 20);
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

    }

    @Override
    public boolean canDamage() {
        return false;
    }
}
