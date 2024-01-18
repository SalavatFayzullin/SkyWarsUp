package bash.reactioner.model;

import java.sql.SQLException;
import java.util.Optional;

public interface PlayerRepository {
    void createTableIfDoesNotExist();
    SwPlayer create(String name);
    Optional<SwPlayer> read(String name);
    void update(String name, SwPlayer newStats);
    void delete(String name);
    void close() throws SQLException;
}
