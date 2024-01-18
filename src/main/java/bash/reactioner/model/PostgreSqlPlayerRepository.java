package bash.reactioner.model;

import bash.reactioner.SkyWarsPlugin;

import java.sql.*;
import java.util.Optional;

public class PostgreSqlPlayerRepository implements PlayerRepository {
    private Connection connection;

    public PostgreSqlPlayerRepository(String url, String username, String password) throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        connection = DriverManager.getConnection(url, username, password);
    }

    @Override
    public void createTableIfDoesNotExist() {
        try (Statement statement = connection.createStatement()) {
            statement.execute(SkyWarsPlugin.getInstance().getCreateTableIfNotExists());
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public SwPlayer create(String name) {
        try (PreparedStatement statement = connection.prepareStatement(SkyWarsPlugin.getInstance().getCreatePlayer())) {
            statement.setString(1, name);
            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return new SwPlayer();
    }

    @Override
    public Optional<SwPlayer> read(String name) {
        try (PreparedStatement statement = connection.prepareStatement(SkyWarsPlugin.getInstance().getReadPlayer())) {
            statement.setString(1, name);
            ResultSet set = statement.executeQuery();
            if (set.next()) {
                // wins, kills, chests_looten, games, deaths
                int wins = set.getInt(1);
                int kills = set.getInt(2);
                int chestsLooten = set.getInt(3);
                int games = set.getInt(4);
                int deaths = set.getInt(5);
                String kits = set.getString(6);
                return Optional.of(new SwPlayer(wins, kills, chestsLooten, games, deaths, kits));
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public void update(String name, SwPlayer newStats) {
        try (PreparedStatement statement = connection.prepareStatement(SkyWarsPlugin.getInstance().getUpdatePlayer())) {
            // UPDATE sw_player SET wins=?, kills=?, chests_looten=?, games=?, deaths=? WHERE name = ?;
            statement.setInt(1, newStats.getWins());
            statement.setInt(2, newStats.getKills());
            statement.setInt(3, newStats.getChestsLooten());
            statement.setInt(4, newStats.getGames());
            statement.setInt(5, newStats.getDeaths());
            statement.setString(6, newStats.getKitsString());
            statement.setString(7, name);
            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void delete(String name) {
        try (PreparedStatement statement = connection.prepareStatement(SkyWarsPlugin.getInstance().getDeletePlayer())) {
            // DELETE FROM sw_player WHERE name = ?;
            statement.setString(1, name);
            statement.execute();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) connection.close();
    }
}
