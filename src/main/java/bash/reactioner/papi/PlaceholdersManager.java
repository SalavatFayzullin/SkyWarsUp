package bash.reactioner.papi;

import bash.reactioner.SkyWarsPlugin;
import bash.reactioner.game.Game;
import bash.reactioner.model.SwPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholdersManager extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "sw";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Reactioner";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        String[] args = params.split("_");
        String dummy = SkyWarsPlugin.getInstance().getMissedValueForPlaceholderDummy();
        if (player == null) return dummy;
        if (args.length == 2 && args[0].equals("game") && args[1].equals("online")) {
            Game game = SkyWarsPlugin.getInstance().getGameByPlayer(player);
            if (game == null) return dummy;
            return String.valueOf(game.getPlayers().size());
        } else if (args.length == 2 && args[0].equals("game") && args[1].equals("maxplayers")) {
            Game game = SkyWarsPlugin.getInstance().getGameByPlayer(player);
            if (game == null) return dummy;
            return String.valueOf(game.getMaxPlayers());
        } else if (args.length == 1 && args[0].equals("killer")) {
            if (player.getKiller() == null) return dummy;
            return player.getKiller().getName();
        } else if (args.length == 2 && args[0].equals("game") && args[1].equals("winner")) {
            Game game = SkyWarsPlugin.getInstance().getGameByPlayer(player);
            if (game == null) return dummy;
            return game.getWinner();
        } else if (args.length == 4 && args[0].equals("game") && args[1].equals("top") && args[2].equals("name")) {
            int place = Integer.parseInt(args[3]);
            Game game = SkyWarsPlugin.getInstance().getGameByPlayer(player);
            if (game == null) return dummy;
            if (0 < place && place <= game.getTopNames().size()) return game.getTopNames().get(place - 1);
            return dummy;
        } else if (args.length == 4 && args[0].equals("game") && args[1].equals("top") && args[2].equals("kills")) {
            int place = Integer.parseInt(args[3]);
            Game game = SkyWarsPlugin.getInstance().getGameByPlayer(player);
            if (game == null) return dummy;
            if (0 < place && place <= game.getTopNames().size())  return String.valueOf(game.getTopKills().get(place - 1));
            return dummy; // %sw_stats_wins%
        } else if (args.length == 2 && args[0].equals("stats")) {
            SwPlayer stats = SkyWarsPlugin.getInstance().getPlayersManager().getStats(player.getName());
            if (args[1].equals("wins")) return String.valueOf(stats.getWins());
            else if (args[1].equals("chestslooten")) return String.valueOf(stats.getChestsLooten());
            else if (args[1].equals("deaths")) return String.valueOf(stats.getDeaths());
            else if (args[1].equals("games")) return String.valueOf(stats.getGames());
            else if (args[1].equals("kills")) return String.valueOf(stats.getKills());
        } else if (args.length == 2 && args[0].equals("game")) {
            if (args[1].equals("wins")) return String.valueOf(SkyWarsPlugin.getInstance().getGameByPlayer(player).getLocalStats(player).getWins());
            else if (args[1].equals("chestslooten")) return String.valueOf(SkyWarsPlugin.getInstance().getGameByPlayer(player).getLocalStats(player).getChestsLooten());
            else if (args[1].equals("deaths")) return String.valueOf(SkyWarsPlugin.getInstance().getGameByPlayer(player).getLocalStats(player).getDeaths());
            else if (args[1].equals("games")) return String.valueOf(SkyWarsPlugin.getInstance().getGameByPlayer(player).getLocalStats(player).getGames());
            else if (args[1].equals("kills")) return String.valueOf(SkyWarsPlugin.getInstance().getGameByPlayer(player).getLocalStats(player).getKills());
            else if (args[1].equals("name")) return SkyWarsPlugin.getInstance().getGameByPlayer(player).getName();
            else if (args[1].equals("secondsbeforestart")) return String.valueOf(SkyWarsPlugin.getInstance().getGameByPlayer(player).getSecondsBeforeStart());
        }
        return null;
    }
}
