package bash.reactioner.game.state;

import bash.reactioner.SkyWarsPlugin;
import bash.reactioner.game.Game;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class ActiveState implements State {
    private int phase;
    private BukkitTask task;
    private int timer;

    @Override
    public void start(Game game) {
        game.setDamageResistence(true);
        Bukkit.getScheduler().runTaskLater(SkyWarsPlugin.getInstance(), () -> game.setDamageResistence(false), 20 * 5);
        phase = 1;
        timer = SkyWarsPlugin.getInstance().getFirstPhaseDuration();
        game.breakCabins();
        game.getPlayers().forEach(p -> {
            p.setGameMode(GameMode.SURVIVAL);
            p.getInventory().clear();
            p.setLevel(0);
            p.setExp(0f);
            SkyWarsPlugin.getInstance().getScoreboardsManager().showScoreboard(p, SkyWarsPlugin.getInstance().getActiveScoreboard());
        });
        updateProgress(game);
        updateTitle(game);
        game.giveKits();
        task = Bukkit.getScheduler().runTaskTimer(SkyWarsPlugin.getInstance(), () -> {
            timer--;
            if (timer <= 0) {
                if (phase == 1) timer = SkyWarsPlugin.getInstance().getSecondPhaseDuration();
                else if (phase == 2) timer = SkyWarsPlugin.getInstance().getThirdPhaseDuration();
                else {
                    game.setState(new DeathmatchState());
                    return;
                }
                game.resetOpenedChests();
                phase++;
            }
            updateProgress(game);
            updateTitle(game);
        }, 0, 20);
        if (game.getMaxPlayers() > game.getModel().getCabins().size()) {
            Bukkit.broadcast(Component.text(ChatColor.RED + "!!! MAX PLAYERS COUNT IS NOT EQUAL TO CABINS COUNT !!! PLEASE CONFIGURE CABINS PROPERLY !!!"));
        }
    }

    private void updateTitle(Game game) {
        game.getInfo().setTitle("Next refill in %d:%d seconds".formatted(timer / 60, timer % 60));
    }

    private void updateProgress(Game game) {
        if (phase == 1) game.getInfo().setProgress((double) timer / SkyWarsPlugin.getInstance().getFirstPhaseDuration());
        else if (phase == 2) game.getInfo().setProgress((double) timer / SkyWarsPlugin.getInstance().getSecondPhaseDuration());
        else game.getInfo().setProgress((double) timer / SkyWarsPlugin.getInstance().getThirdPhaseDuration());
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
        game.checkForWinner();
    }

    @Override
    public boolean canDamage() {
        return true;
    }
}
