package bash.reactioner.items;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ItemEnchantment implements ConfigurationSerializable {
    public static ItemEnchantment deserialize(Map<String, Object> map) {
        ItemEnchantment enchantment = new ItemEnchantment();
        enchantment.enchantment = Enchantment.getByKey(NamespacedKey.fromString((String) map.get("enchantment")));
        enchantment.probabilities = new HashMap<>();
        Arrays.stream(((String) map.get("levels")).split(" ")).forEach(x -> {
            String[] split = x.split(":");
            enchantment.probabilities.put(Integer.parseInt(split[0]), Double.parseDouble(split[1]));
        });
        return enchantment;
    }

    private Enchantment enchantment;
    private Map<Integer, Double> probabilities;

    public ItemEnchantment(Enchantment enchantment, Map<Integer, Double> probabilities) {
        this.enchantment = enchantment;
        this.probabilities = probabilities;
    }

    public ItemEnchantment() {
    }

    public void enchant(ItemStack item) {
        double curProba = 0.0, rand = Math.random();
        int level = 0;
        for (Map.Entry<Integer, Double> entry : probabilities.entrySet()) {
            if (curProba <= rand && rand <= curProba + entry.getValue()) {
                level = entry.getKey();
                break;
            } else curProba += entry.getValue();
        }
        if (level == 0) return;
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(enchantment, level, true);
        item.setItemMeta(meta);
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("levels", probabilities.entrySet().stream().map(e -> e.getKey() + ":" + e.getValue()).collect(Collectors.joining(" ")));
        map.put("enchantment", enchantment.getKey().getKey());
        return map;
    }
}
