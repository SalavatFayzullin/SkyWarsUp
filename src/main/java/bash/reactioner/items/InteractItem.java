package bash.reactioner.items;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class InteractItem {
    private ItemStack item;
    private Consumer<Player> action;

    public InteractItem(Consumer<Player> action, Material type, Component name, Component... lore) {
        this.action = action;
        item = new ItemStack(type);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(List.of(lore));
        item.setItemMeta(meta);
    }

    public InteractItem(Consumer<Player> action, ItemStack item, Component name, List<Component> lore) {
        this.action = action;
        this.item = item;
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name);
        meta.lore(lore);
        item.setItemMeta(meta);
    }

    public void changeLore(Component... lore) {
        ItemMeta meta = item.getItemMeta();
        meta.lore(List.of(lore));
        item.setItemMeta(meta);
    }

    public boolean identify(ItemStack other) {
        if (other == null || other.getItemMeta() == null) return false;
        if (Objects.equals(item.getItemMeta().displayName(), other.getItemMeta().displayName())) return true;
        return false;
    }

    public boolean doAction(ItemStack other, Player p) {
        if (other == null || other.getItemMeta() == null) return false;
        if (Objects.equals(item.getItemMeta().displayName(), other.getItemMeta().displayName())) {
            action.accept(p);
            return true;
        }
        return false;
    }

    public ItemStack getItem() {
        return item;
    }
}
