package bash.reactioner.model;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SwKit implements ConfigurationSerializable {
    public static SwKit deserialize(Map<String, Object> map) {
        SwKit kit = new SwKit();
        kit.price = (double) map.get("price");
        kit.icon = (ItemStack) map.get("icon");
        kit.name = (String) map.get("name");
        kit.kitLore = (List<String>) map.get("kit-lore");
        kit.inventory = (List<ItemStack>) map.get("inventory");
        kit.applyNameAndLore();
        return kit;
    }

    private ItemStack icon;
    private String name;
    private List<String> kitLore;
    private List<ItemStack> inventory;
    private double price;

    public SwKit(ItemStack icon, String name, List<String> kitLore) {
        this.icon = icon.clone();
        this.name = name;
        this.kitLore = kitLore;
        applyNameAndLore();
    }

    public SwKit() {
    }

    private void applyNameAndLore() {
        ItemMeta meta = this.icon.getItemMeta();
        meta.displayName(Component.text(name));
        meta.lore(kitLore.stream().map(Component::text).collect(Collectors.toList()));
        this.icon.setItemMeta(meta);
    }

    public ItemStack getIcon() {
        return icon;
    }

    public void takeSnapshot(Player p, double price) {
        inventory = new ArrayList<>();
        this.price = price;
        for (ItemStack item : p.getInventory()) inventory.add(item);
    }

    public void giveItems(Player p) {
        if (inventory != null) {
            for (int i = 0; i < inventory.size(); i++) p.getInventory().setItem(i, inventory.get(i));
        }
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("icon", icon);
        map.put("name", name);
        map.put("kit-lore", kitLore);
        map.put("inventory", inventory);
        map.put("price", price);
        return map;
    }

    public String getName() {
        return name;
    }

    public List<String> getKitLore() {
        return kitLore;
    }

    public double getPrice() {
        return price;
    }

    public List<ItemStack> getInventory() {
        return inventory;
    }
}
