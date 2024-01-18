package bash.reactioner.managers;

import bash.reactioner.SkyWarsPlugin;
import bash.reactioner.game.Game;
import bash.reactioner.items.InteractItem;
import bash.reactioner.model.SwKit;
import bash.reactioner.model.SwPlayer;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KitsManager {
    private Map<InteractItem, SwKit> kitsIcons;
    private InteractItem selectKit;

    {
        kitsIcons = new HashMap<>();
        SkyWarsPlugin.getInstance().getKits().forEach((k, v) -> {
            InteractItem item = new InteractItem(p -> {
                if (isKitAvailableForPlayer(p, v)) {
                    SkyWarsPlugin.getInstance().getGamesManager().onKitSelect(p, v);
                    SkyWarsPlugin.getInstance().broadcast("You selected the %s kit".formatted(k), p, p);
                    openMenu(p);
                } else {
                    if (SkyWarsPlugin.getInstance().getEconomy().getBalance(p) >= v.getPrice()) {
                        SkyWarsPlugin.getInstance().getCache().setWantedKitToPlayer(p, v);
                        SkyWarsPlugin.getInstance().getMenusManager().openKitBuyMenu(p);
                    } else SkyWarsPlugin.getInstance().broadcast(ChatColor.RED + "You can not select the %s kit".formatted(k), p, p);
                }
            }, v.getIcon(), Component.text(k), v.getKitLore().stream().map(Component::text).collect(Collectors.toList()));
            kitsIcons.put(item, v);
            SkyWarsPlugin.getInstance().getItemsManager().addItem(item);
        });
        selectKit = new InteractItem(this::openMenu, Material.FEATHER, Component.text("Select a kit"));
        SkyWarsPlugin.getInstance().getItemsManager().addItem(selectKit);
    }

    public boolean onKitBuy(Player p) {
        SwKit kit = SkyWarsPlugin.getInstance().getCache().getWantedKit(p);
        if (kit == null) return false;
        SwPlayer stats = SkyWarsPlugin.getInstance().getPlayersManager().getStats(p.getName());
        if (stats.getKits().contains(kit.getName())) return true;
        SkyWarsPlugin.getInstance().getEconomy().withdrawPlayer(p, kit.getPrice());
        stats.addKit(kit.getName());
        SkyWarsPlugin.getInstance().getRepository().update(p.getName(), stats);
        return true;
    }

    public boolean isKitAvailableForPlayer(Player p, SwKit kit) {
        return SkyWarsPlugin.getInstance().getPlayersManager().getStats(p.getName()).getKits().contains(kit.getName());
    }

    public void openMenu(Player p) {
        Inventory inventory = Bukkit.createInventory(null, 27, Component.text("Select a kit"));
        int i = 0;
        SwKit selected = SkyWarsPlugin.getInstance().getGamesManager().getKit(p);
        for (Map.Entry<InteractItem, SwKit> entry : kitsIcons.entrySet()) {
            ItemStack copy = entry.getKey().getItem().clone();
            ItemMeta meta = copy.getItemMeta();
            List<Component> lore = new ArrayList<>(meta.lore());
            if (selected == entry.getValue()) lore.add(Component.text(ChatColor.YELLOW + "SELECTED"));
            else if (isKitAvailableForPlayer(p, entry.getValue())) lore.add(Component.text(ChatColor.GREEN + "AVAILABLE"));
            else lore.add(Component.text(ChatColor.RED + "NOT AVAILABLE"));
            meta.lore(lore);
            copy.setItemMeta(meta);
            inventory.setItem(i++, copy);
        }
        p.openInventory(inventory);
    }

    public InteractItem getSelectKit() {
        return selectKit;
    }
}
