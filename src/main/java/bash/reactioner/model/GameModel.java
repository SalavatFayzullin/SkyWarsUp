package bash.reactioner.model;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameModel implements ConfigurationSerializable {
    public static GameModel deserialize(Map<String, Object> map) {
        GameModel model = new GameModel();
        model.firstCorner = (Location) map.get("first-corner");
        model.secondCorner = (Location) map.get("second-corner");
        model.cabins = (List<Location>) map.get("cabins");
        model.minPlayers = (Integer) map.get("min-players");
        model.maxPlayers = (Integer) map.get("max-players");
        return model;
    }

    private Location firstCorner;
    private Location secondCorner;
    private List<Location> cabins;
    private int minPlayers;
    private int maxPlayers;

    public GameModel() {
    }

    public GameModel(int minPlayers, int maxPlayers) {
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("first-corner", firstCorner);
        map.put("second-corner", secondCorner);
        map.put("cabins", cabins);
        map.put("min-players", minPlayers);
        map.put("max-players", maxPlayers);
        return map;
    }

    public Location getFirstCorner() {
        return firstCorner;
    }

    public void setFirstCorner(Location firstCorner) {
        this.firstCorner = firstCorner;
    }

    public Location getSecondCorner() {
        return secondCorner;
    }

    public void setSecondCorner(Location secondCorner) {
        this.secondCorner = secondCorner;
    }

    public List<Location> getCabins() {
        return cabins;
    }

    public void setCabins(List<Location> cabins) {
        this.cabins = cabins;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
}
