package bash.reactioner.game.state;

import bash.reactioner.SkyWarsPlugin;
import bash.reactioner.game.Game;
import org.bukkit.entity.Player;

import java.util.List;

public interface State {
    void start(Game game);
    void cancel(Game game);
    void onJoin(Player p, Game game);
    void onQuit(Player p, Game game);
    boolean canDamage();
    default List<String> getScoreboard() {
        return SkyWarsPlugin.getInstance().getActiveScoreboard();
    }
}
