package bash.reactioner.menu;

import bash.reactioner.SkyWarsPlugin;
import bash.reactioner.command.CommandsManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class MenusManager {
    public void openKitBuyMenu(Player p) {
        Inventory inventory = Bukkit.createInventory(null, 9, Component.text("Buy kit"));
        inventory.setItem(2, SkyWarsPlugin.getInstance().getItemsManager().getBuyKit().getItem());
        p.openInventory(inventory);
    }
}
