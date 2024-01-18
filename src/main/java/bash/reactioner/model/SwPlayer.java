package bash.reactioner.model;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SwPlayer {
    private int wins;
    private int kills;
    private int chestsLooten;
    private int games;
    private int deaths;
    private List<String> kits;

    public SwPlayer(int wins, int kills, int chestsLooten, int games, int deaths, @NotNull String kits) {
        this.wins = wins;
        this.kills = kills;
        this.chestsLooten = chestsLooten;
        this.games = games;
        this.deaths = deaths;
        this.kits = Arrays.stream(kits.trim().split(" ")).filter(x -> !x.isEmpty()).toList();
    }

    public SwPlayer() {
        this(0, 0, 0, 0, 0, "");
    }

    public void addKit(String name) {
        kits = new ArrayList<>(kits);
        kits.add(name);
    }

    public String getKitsString() {
        return kits.stream().collect(Collectors.joining(" "));
    }

    public List<String> getKits() {
        return kits;
    }

    public void merge(SwPlayer other) {
        wins += other.getWins();
        kills += other.getKills();
        chestsLooten += other.getChestsLooten();
        games += other.getGames();
        deaths += other.getDeaths();
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public void setChestsLooten(int chestsLooten) {
        this.chestsLooten = chestsLooten;
    }

    public void setGames(int games) {
        this.games = games;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void addKill() {
        kills++;
    }

    public void addChestLooten() {
        chestsLooten++;
    }

    public void addGame() {
        games++;
    }

    public void addDeath() {
        deaths++;
    }

    public void addWin() {
        wins++;
    }

    public int getWins() {
        return wins;
    }

    public int getKills() {
        return kills;
    }

    public int getChestsLooten() {
        return chestsLooten;
    }

    public int getGames() {
        return games;
    }

    public int getDeaths() {
        return deaths;
    }
}
