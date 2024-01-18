package bash.reactioner.items;

import bash.reactioner.SkyWarsPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ChestItem implements ConfigurationSerializable {
    public static ChestItem deserialize(Map<String, Object> map) {
        ChestItem item = new ChestItem();
        item.type = Material.valueOf((String) map.get("type"));
        item.minCount = (int) map.get("min-count");
        item.maxCount = (int) map.get("max-count");
        item.weight = (int) map.get("weight");
        item.enchantmentsNames = (List<String>) map.get("enchantments");
        if (item.enchantmentsNames == null) item.enchantmentsNames = new ArrayList<>();
        return item;
    }

    private Material type;
    private int minCount;
    private int maxCount;
    private List<String> enchantmentsNames;
    private int weight;

    public ChestItem(Material type, int minCount, int maxCount, List<String> enchantmentsNames, int weight) {
        this.type = type;
        this.minCount = minCount;
        this.maxCount = maxCount;
        this.enchantmentsNames = enchantmentsNames;
        this.weight = weight;
    }

    public ChestItem() {
    }

    public int getWeight() {
        return weight;
    }

    public ItemStack getItem() {
        Random random = new Random();
        int count = random.nextInt(minCount, maxCount + 1);
        ItemStack item = new ItemStack(type, count);
        if (enchantmentsNames != null) enchantmentsNames.forEach(name -> {
            ItemEnchantment enchantment = SkyWarsPlugin.getInstance().getEnchantment(name);
            if (enchantment == null) return;
            enchantment.enchant(item);
        });
        return item;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type.name());
        map.put("min-count", minCount);
        map.put("max-count", maxCount);
        map.put("enchantments", enchantmentsNames);
        map.put("weight", weight);
        return map;
    }
}
