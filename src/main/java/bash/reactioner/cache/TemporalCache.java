package bash.reactioner.cache;

import bash.reactioner.model.SwKit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class TemporalCache {
    private Map<Player, SwKit> wantedKitsToBuy;

    {
        wantedKitsToBuy = new HashMap<>();
    }

    public void setWantedKitToPlayer(Player p, SwKit kit) {
        wantedKitsToBuy.put(p, kit);
    }

    public void removeWantedKitFromPlayer(Player p) {
        wantedKitsToBuy.remove(p);
    }

    public SwKit getWantedKit(Player p) {
        return wantedKitsToBuy.get(p);
    }
}
