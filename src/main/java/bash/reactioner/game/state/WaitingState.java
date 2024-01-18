package bash.reactioner.game.state;

import bash.reactioner.SkyWarsPlugin;
import bash.reactioner.game.Game;
import org.bukkit.boss.BarColor;
import org.bukkit.entity.Player;

import java.util.List;

public class WaitingState implements State {
    @Override
    public void start(Game game) {
        updateBar(game);
    }

    @Override
    public void cancel(Game game) {

    }

    @Override
    public void onJoin(Player p, Game game) {
        if (game.getOnline() == game.getMinPlayers()) game.setState(new StartingState());
        updateBar(game);
    }

    @Override
    public void onQuit(Player p, Game game) {

    }

    @Override
    public List<String> getScoreboard() {
        return SkyWarsPlugin.getInstance().getWaitingScoreboard();
    }

    @Override
    public boolean canDamage() {
        return false;
    }

    private void updateBar(Game game) {
        game.getInfo().setProgress((double) game.getOnline() / game.getMaxPlayers());
        game.getInfo().setColor(BarColor.BLUE);
        game.getInfo().setTitle("Waiting %d/%d".formatted(game.getOnline(), game.getMaxPlayers()));
    }
}
