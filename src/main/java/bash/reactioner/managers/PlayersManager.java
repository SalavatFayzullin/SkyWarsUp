package bash.reactioner.managers;

import bash.reactioner.SkyWarsPlugin;
import bash.reactioner.model.SwPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PlayersManager implements Listener {
    private Map<String, SwPlayer> stats;

    {
        stats = new HashMap<>();
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent e) {
        e.joinMessage(Component.empty());
        Optional<SwPlayer> statsOptional = SkyWarsPlugin.getInstance().getRepository().read(e.getPlayer().getName());
        if (statsOptional.isEmpty()) stats.put(e.getPlayer().getName(), SkyWarsPlugin.getInstance().getRepository().create(e.getPlayer().getName()));
        else stats.put(e.getPlayer().getName(), statsOptional.get());
        SkyWarsPlugin.getInstance().teleportOnSpawn(e.getPlayer());
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent e) {
        SkyWarsPlugin.getInstance().getGamesManager().onPlayerQuit(e.getPlayer());
        e.quitMessage(Component.empty());
        stats.remove(e.getPlayer().getName());
    }

    public SwPlayer getStats(String name) {
        return stats.get(name);
    }
}
