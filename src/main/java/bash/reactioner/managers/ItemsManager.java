package bash.reactioner.managers;

import bash.reactioner.SkyWarsPlugin;
import bash.reactioner.items.InteractItem;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.LinkedList;
import java.util.List;

public class ItemsManager implements Listener {
    private InteractItem joinGame;
    private InteractItem quitGame;
    private List<InteractItem> items;
    private InteractItem buyKit;
    private InteractItem cancelBuying;

    public ItemsManager() {
        items = new LinkedList<>();
        joinGame = new InteractItem(p -> SkyWarsPlugin.getInstance().getGamesManager().openMenu(p), Material.NAME_TAG, Component.text("Click to select a game"));
        quitGame = new InteractItem(p -> SkyWarsPlugin.getInstance().getGamesManager().onPlayerQuit(p), Material.MAGMA_CREAM, Component.text("Leave the game"));
        items.add(joinGame);
        items.add(quitGame);
        buyKit = new InteractItem(p -> {
            if (!SkyWarsPlugin.getInstance().getKitsManager().onKitBuy(p)) {
                SkyWarsPlugin.getInstance().broadcast("Sorry, but you have not kit to buy", p, p);
            } else {
                SkyWarsPlugin.getInstance().broadcast("You successfully bought the kit", p, p);
                SkyWarsPlugin.getInstance().getKitsManager().openMenu(p);
            }
        }, Material.EMERALD_BLOCK, Component.text("Buy"));
        items.add(buyKit);
    }

    public void removeItem(InteractItem item) {
        items.remove(item);
    }

    public InteractItem getBuyKit() {
        return buyKit;
    }

    public InteractItem getCancelBuying() {
        return cancelBuying;
    }

    public InteractItem getQuitGame() {
        return quitGame;
    }

    public void addItem(InteractItem item) {
        items.add(item);
    }

    @EventHandler
    private void onClick(InventoryClickEvent e) {
        for (InteractItem item : items) {
            if (item.doAction(e.getCurrentItem(), (Player) e.getWhoClicked())) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    private void onDrop(PlayerDropItemEvent e) {
        for (InteractItem item : items) {
            if (item.identify(e.getItemDrop().getItemStack())) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler
    private void onInteract(PlayerInteractEvent e) {
        for (InteractItem item : items) {
            if (item.doAction(e.getItem(), e.getPlayer())) {
                e.setCancelled(true);
                break;
            }
        }
    }

    public InteractItem getJoinGame() {
        return joinGame;
    }
}
