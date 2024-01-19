package bash.reactioner.command;

import bash.reactioner.SkyWarsPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandsManager implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (sender instanceof Player p) {
            if (!p.isOp()) return false;
            Location loc = cleanLocation(p.getLocation());
            if (s.equalsIgnoreCase("swcreate")) {
                if (args.length != 3) return false;
                try {
                    SkyWarsPlugin.getInstance().createArena(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                } catch (NumberFormatException e) {
                    SkyWarsPlugin.getInstance().broadcast("Last two arguments should be integer!", p, p);
                }
                return true;
            } else if (s.equalsIgnoreCase("swsetcorner")) {
                if (args.length != 2) return false;
                int corner;
                try {
                    corner = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    SkyWarsPlugin.getInstance().broadcast("Corner's number should be integer!", p, p);
                    return true;
                }

                if (corner == 1) {
                    if (SkyWarsPlugin.getInstance().setCorner(args[0], corner, loc)) {
                        SkyWarsPlugin.getInstance().broadcast("Successfully set the corner №1 for %s arena!".formatted(args[0]), p, p);
                    }
                    else {
                        SkyWarsPlugin.getInstance().broadcast("No such arena with name %s".formatted(args[0]), p, p);
                    }
                } else {
                    if (SkyWarsPlugin.getInstance().setCorner(args[0], corner, loc)) {
                        SkyWarsPlugin.getInstance().broadcast("Successfully set the corner №2 for %s arena!".formatted(args[0]), p, p);
                    }
                    else {
                        SkyWarsPlugin.getInstance().broadcast("No such arena with name %s".formatted(args[0]), p, p);
                    }
                }
                return true;
            } else if (s.equalsIgnoreCase("swaddcabin")) {
                if (args.length != 1) return false;
                int num = SkyWarsPlugin.getInstance().addCabin(args[0], loc);
                if (num != -1) {
                    SkyWarsPlugin.getInstance().broadcast("Successfully set the cabin №%d!".formatted(num), p, p);
                }
                else {
                    SkyWarsPlugin.getInstance().broadcast("No such arena with name %s".formatted(args[0]), p, p);
                }
                return true;
            } else if (s.equalsIgnoreCase("swresetcabins")) {
                if (args.length != 1) return false;
                if (SkyWarsPlugin.getInstance().resetCabins(args[0])) {
                    SkyWarsPlugin.getInstance().broadcast("Successfully reset the cabins for %s arena!".formatted(args[0]), p, p);
                }
                else {
                    SkyWarsPlugin.getInstance().broadcast("No such arena with name %s".formatted(args[0]), p, p);
                }
                return true;
            } else if (s.equalsIgnoreCase("swsetglobalspawn")) {
                SkyWarsPlugin.getInstance().setGlobalSpawn(loc);
                SkyWarsPlugin.getInstance().broadcast("You successfully set the global spawn!", p, p);
                return true;
            } else if (s.equalsIgnoreCase("swcreateenchant")) {
                if (args.length < 3) return false;
                Map<Integer, Double> levels = new HashMap<>();
                for (int i = 2; i < args.length; i++) {
                    String[] split = args[i].split(":");
                    levels.put(Integer.parseInt(split[0]), Double.parseDouble(split[1]));
                }
                SkyWarsPlugin.getInstance().createEnchantment(args[0], args[1], levels);
                SkyWarsPlugin.getInstance().broadcast("You successfully created the enchantment " + args[0], p, p);
                return true;
            } else if (s.equalsIgnoreCase("swcreateitem")) {
                if (args.length < 5) return false;
                int weight = Integer.parseInt(args[1]), minCount = Integer.parseInt(args[3]), maxCount = Integer.parseInt(args[4]);
                Material type = Material.valueOf(args[2]);
                List<String> enchantments = new ArrayList<>();
                for (int i = 5; i < args.length; i++) enchantments.add(args[i]);
                SkyWarsPlugin.getInstance().createItem(args[0], weight, type, minCount, maxCount, enchantments);
                SkyWarsPlugin.getInstance().broadcast("You successfully created the item " + args[0], p, p);
                return true;
            } else if (s.equalsIgnoreCase("swkitcreate")) {
                if (args.length < 1) return false;
                else if (p.getInventory().getItemInMainHand().getType() == Material.AIR) {
                    SkyWarsPlugin.getInstance().broadcast("You must hold the kit icon in the hand while calling /swkitcreate", p, p);
                    return true;
                }
                List<String> lore = new ArrayList<>();
                for (int i = 1; i < args.length; i++) lore.add(args[i].replace('_', ' '));
                SkyWarsPlugin.getInstance().createKit(args[0], lore, p.getInventory().getItemInMainHand());
                SkyWarsPlugin.getInstance().broadcast("You successfully created the kit " + args[0], p, p);
                return true;
            } else if (s.equalsIgnoreCase("swkitsave")) {
                if (args.length != 2) return false;
                if (SkyWarsPlugin.getInstance().saveKit(args[0], p, Double.parseDouble(args[1]))) SkyWarsPlugin.getInstance().broadcast("You successfully saved the kit " + args[0], p, p);
                else SkyWarsPlugin.getInstance().broadcast("There is no such kit with name " + args[0], p, p);
                return true;
            } else if (s.equalsIgnoreCase("swkitremove")) {
                if (args.length != 1) return false;
                if (SkyWarsPlugin.getInstance().removeKit(args[0])) SkyWarsPlugin.getInstance().broadcast("You successfully deleted the kit " + args[0], p, p);
                else SkyWarsPlugin.getInstance().broadcast("There is no such kit with name " + args[0], p, p);
                return true;
            } else if (s.equalsIgnoreCase("swarenaremove")) {
                if (args.length != 1) return false;
                if (SkyWarsPlugin.getInstance().removeArena(args[0])) SkyWarsPlugin.getInstance().broadcast("You successfully deleted the arena " + args[0], p, p);
                else SkyWarsPlugin.getInstance().broadcast("There is no such arena with name " + args[0], p, p);
                return true;
            } else if (s.equalsIgnoreCase("swenchantremove")) {
                if (args.length != 1) return false;
                if (SkyWarsPlugin.getInstance().removeEnchantment(args[0])) SkyWarsPlugin.getInstance().broadcast("You successfully deleted the enchantment " + args[0], p, p);
                else SkyWarsPlugin.getInstance().broadcast("There is no such enchantment with name " + args[0], p, p);
                return true;
            } else if (s.equalsIgnoreCase("switemremove")) {
                if (args.length != 1) return false;
                if (SkyWarsPlugin.getInstance().removeItem(args[0])) SkyWarsPlugin.getInstance().broadcast("You successfully deleted the item " + args[0], p, p);
                else SkyWarsPlugin.getInstance().broadcast("There is no such item with name " + args[0], p, p);
                return true;
            }
        }
        return false;
    }

    private Location cleanLocation(Location init) {
        init.setX(init.getBlockX() + 0.5);
        init.setY(init.getBlockY());
        init.setZ(init.getBlockZ() + 0.5);
        init.setYaw(0f);
        init.setPitch(0f);
        return init;
    }
}
