package bash.reactioner.managers;

import bash.reactioner.SkyWarsPlugin;
import bash.reactioner.game.Game;
import bash.reactioner.items.InteractItem;
import bash.reactioner.model.GameModel;
import bash.reactioner.model.SwKit;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GamesManager implements Listener {
    private List<Game> games;
    private Inventory gamesMenu;
    private List<InteractItem> items;
    private Map<Player, SwKit> kits;

    public GamesManager(Map<String, GameModel> models) {
        gamesMenu = Bukkit.createInventory(null, 27, Component.text("Choose a game"));
        games = new ArrayList<>(models.size());
        items = new ArrayList<>(models.size());
        models.forEach((name, model) -> addGame(name, model));
        kits = new HashMap<>();
    }

    public SwKit getKit(Player p) {
        return kits.get(p);
    }

    public void onKitSelect(Player p, SwKit kit) {
        kits.put(p, kit);
    }

    public void addGame(String name, GameModel model) {
        Game game = new Game(name, model);
        Bukkit.getServer().getPluginManager().registerEvents(game, SkyWarsPlugin.getInstance());
        games.add(game);
        game.save();
        InteractItem item = new InteractItem(game::onPlayerJoin, Material.SLIME_BALL, Component.text("Join to " + name));
        items.add(item);
        SkyWarsPlugin.getInstance().getItemsManager().addItem(item);
    }

    public void removeGame(String name) {
        int i = 0;
        for (; i < games.size(); i++) {
            if (games.get(i).getName().equals(name)) break;
        }
        Game game = games.get(i);
        PlayerDeathEvent.getHandlerList().unregister(game);
        InventoryOpenEvent.getHandlerList().unregister(game);
        if (game != null) game.load();
        games.remove(i);
        InteractItem item = items.remove(i);
        SkyWarsPlugin.getInstance().getItemsManager().removeItem(item);
    }

    public void openMenu(Player p) {
        for (int i = 0; i < games.size(); i++) {
            items.get(i).changeLore(Component.text("Online: %d/%d".formatted(games.get(i).getOnline(), games.get(i).getMaxPlayers())));
            gamesMenu.setItem(i, items.get(i).getItem());
        }
        p.openInventory(gamesMenu);
    }

    public void onPlayerQuit(Player p) {
        for (Game game : games) {
            if (game.getPlayers().contains(p)) {
                game.onPlayerQuit(p);
                break;
            }
        }
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent e) {
        kits.remove(e.getPlayer());
    }

    public void resetAllArenas() {
        games.forEach(game -> game.resetArena());
    }

    @EventHandler
    private void onDamage(EntityDamageEvent e) {
        boolean f = false;
        for (Game game : games) {
            if (game.onDamage(e)) {
                f = true;
                break;
            }
        }
        if (!f) e.setCancelled(true);
    }

    public Game getGameByPlayer(Player p) {
        for (Game game : games) {
            if (game.getPlayers().contains(p)) return game;
            else if (game.getSpectators().contains(p)) return game;
        }
        return null;
    }
}
