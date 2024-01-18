package bash.reactioner.game;

import bash.reactioner.SkyWarsPlugin;
import bash.reactioner.game.state.RestartingState;
import bash.reactioner.game.state.StartingState;
import bash.reactioner.game.state.State;
import bash.reactioner.game.state.WaitingState;
import bash.reactioner.items.ChestItem;
import bash.reactioner.managers.PlayersManager;
import bash.reactioner.model.GameModel;
import bash.reactioner.model.SwKit;
import bash.reactioner.model.SwPlayer;
import com.google.common.collect.Sets;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Game implements Listener {
    private Set<Player> players;
    private Set<Player> spectators;
    private BlockData[] arenaData;
    private BossBar info;
    private State state;
    private List<Inventory> openedChests;
    private GameModel model;
    private boolean[] occupiedCabins;
    private Map<String, Integer> playersPlaces;
    private WorldBorder border;
    private boolean damageResistence;
    private String winner;
    private Map<String, Integer> topKillers;
    private List<String> topNames;
    private List<Integer> topKills;
    private Map<String, SwPlayer> statsChanges;
    private String name;

    public Game(String name, GameModel model) {
        this.name = name;
        this.model = model;
        save();
        resetState();
    }

    public int getSecondsBeforeStart() {
        if (state instanceof StartingState startingState) return startingState.getSecondsLeft();
        return 0;
    }

    public String getName() {
        return name;
    }

    public void resetArena() {
        load();
    }

    public void resetState() {
        if (players != null) players.forEach(p -> {
            SkyWarsPlugin.getInstance().teleportOnSpawn(p);
            p.setWorldBorder(p.getWorld().getWorldBorder());
        });
        if (spectators != null) spectators.forEach(p -> {
            SkyWarsPlugin.getInstance().teleportOnSpawn(p);
            p.setWorldBorder(p.getWorld().getWorldBorder());
        });
        players = Sets.newConcurrentHashSet();
        spectators = Sets.newConcurrentHashSet();
        if (info != null) info.removeAll();
        info = Bukkit.createBossBar("Waiting %d/%d", BarColor.BLUE, BarStyle.SOLID);
        openedChests = new ArrayList<>();
        if (model.getCabins() != null) occupiedCabins = new boolean[model.getCabins().size()];
        playersPlaces = new HashMap<>();
        setState(new WaitingState());
        int minX = Math.min(model.getFirstCorner().getBlockX(), model.getSecondCorner().getBlockX()), maxX = Math.max(model.getFirstCorner().getBlockX(), model.getSecondCorner().getBlockX());
        int minZ = Math.min(model.getFirstCorner().getBlockZ(), model.getSecondCorner().getBlockZ()), maxZ = Math.max(model.getFirstCorner().getBlockZ(), model.getSecondCorner().getBlockZ());
        double diameter = Math.max(maxZ - minZ, maxX - minX);
        double midX = minX + diameter / 2, midZ = minZ + diameter / 2;
        border = Bukkit.createWorldBorder();
        border.setCenter(midX, midZ);
        border.setSize(diameter);
        winner = null;
        topKillers = new HashMap<>();
        topNames = null;
        topKills = null;
        statsChanges = new HashMap<>();
    }

    public void updateStats() {
        if (statsChanges != null) statsChanges.forEach((name, change) -> {
            SwPlayer origin = SkyWarsPlugin.getInstance().getPlayersManager().getStats(name);
            origin.addGame();
            origin.merge(change);
            SkyWarsPlugin.getInstance().getRepository().update(name, origin);
        });
    }

    public void giveKits() {
        players.forEach(p -> {
            SwKit kit = SkyWarsPlugin.getInstance().getGamesManager().getKit(p);
            if (kit != null) kit.giveItems(p);
        });
    }

    public boolean onPlayerJoin(Player p) {
        if (players.contains(p)) {
            SkyWarsPlugin.getInstance().broadcast(ChatColor.RED + "You are already in this game!", p, p);
            return false;
        }
        players.add(p);
        teleportOnLocalSpawn(p);
        state.onJoin(p, this);
        p.getInventory().setItem(0, SkyWarsPlugin.getInstance().getItemsManager().getQuitGame().getItem());
        p.getInventory().setItem(1, SkyWarsPlugin.getInstance().getKitsManager().getSelectKit().getItem());
        p.setWorldBorder(border);
        SkyWarsPlugin.getInstance().broadcast(SkyWarsPlugin.getInstance().getJoinToArenaMessage(), p, p);
        statsChanges.put(p.getName(), new SwPlayer());
        getPlayers().forEach(player -> SkyWarsPlugin.getInstance().getScoreboardsManager().showScoreboard(player, state.getScoreboard()));
        return true;
    }

    public SwPlayer getLocalStats(Player p) {
        return statsChanges.get(p.getName());
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public GameModel getModel() {
        return model;
    }

    public void onKitSelect(Player p, SwKit kit) {
        SkyWarsPlugin.getInstance().getGamesManager().onKitSelect(p, kit);
    }

    public void onPlayerKilled(Player killed, Optional<Player> killer) {
        spectators.add(killed);
        players.remove(killed);
        if (killer.isEmpty()) broadcast(SkyWarsPlugin.getInstance().getPlayerDiedMessage(), killed);
        else {
            statsChanges.get(killer.get().getName()).addKill();
            topKillers.put(killer.get().getName(), topKillers.getOrDefault(killer.get().getName(), 0) + 1);
            broadcast(SkyWarsPlugin.getInstance().getPlayerKilledByPlayerMessage(), killed);
        }
        checkForWinner();
        killed.setGameMode(GameMode.SPECTATOR);
        killed.teleport(model.getCabins().get(playersPlaces.get(killed.getName())));
        statsChanges.get(killed.getName()).addDeath();
        getPlayers().forEach(player -> SkyWarsPlugin.getInstance().getScoreboardsManager().showScoreboard(player, state.getScoreboard()));
    }

    public List<String> getTopNames() {
        if (topNames != null) return topNames;
        if (topKillers == null) return null;
        topNames = new ArrayList<>(topKillers.size());
        topKills = new ArrayList<>(topKillers.size());
        topKillers.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).forEach(x -> {
            topNames.add(x.getKey());
            topKills.add(x.getValue());
        });
        return topNames;
    }

    public List<Integer> getTopKills() {
        if (topKills != null) return topKills;
        if (topKillers == null) return null;
        topNames = new ArrayList<>(topKillers.size());
        topKills = new ArrayList<>(topKillers.size());
        topKillers.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).forEach(x -> {
            topNames.add(x.getKey());
            topKills.add(x.getValue());
        });
        return topKills;
    }

    public boolean onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p) {
            if (players.contains(p)) {
                if (damageResistence || !state.canDamage()) e.setCancelled(true);
                else if (e.getCause() == EntityDamageEvent.DamageCause.VOID) p.setHealth(0.0);
                return true;
            }
        }
        return false;
    }

    @EventHandler
    private void onDeath(PlayerDeathEvent e) {
        if (players.contains(e.getPlayer())) {
            if (e.getEntity().getKiller() == null) onPlayerKilled(e.getPlayer(), Optional.empty());
            else onPlayerKilled(e.getPlayer(), Optional.of(e.getEntity().getKiller()));
            e.getPlayer().spigot().respawn();
            e.deathMessage(Component.empty());
        }
    }

    public void checkForWinner() {
        if (players.size() == 1) onPlayerWin();
    }

    private void onPlayerWin() {
        Optional<Player> winner = players.stream().findFirst();
        if (winner.isEmpty()) return;
        this.winner = winner.get().getName();
        SkyWarsPlugin.getInstance().getWinMessage().forEach(x -> broadcast(x, winner.get()));
        setState(new RestartingState());
        statsChanges.get(winner.get().getName()).addWin();
    }

    public String getWinner() {
        return winner;
    }

    public void broadcast(String message, Player target) {
        players.forEach(p -> {
            if (target == null)SkyWarsPlugin.getInstance().broadcast(message, p, p);
            else SkyWarsPlugin.getInstance().broadcast(message, p, target);
        });
        spectators.forEach(p -> {
            if (target == null)SkyWarsPlugin.getInstance().broadcast(message, p, p);
            else SkyWarsPlugin.getInstance().broadcast(message, p, target);
        });
    }

    public void onPlayerQuit(Player p) {
        players.remove(p);
        spectators.remove(p);
        info.removePlayer(p);
        state.onQuit(p, this);
        SkyWarsPlugin.getInstance().teleportOnSpawn(p);
        broadcast(SkyWarsPlugin.getInstance().getQuitFromArenaMessage(), p);
        p.setWorldBorder(p.getWorld().getWorldBorder());
        int index = playersPlaces.get(p.getName());
        occupiedCabins[index] = false;
        playersPlaces.remove(p.getName());
        fillCabin(model.getCabins().get(index), Material.AIR);
    }

    public WorldBorder getBorder() {
        return border;
    }

    private void fillCabin(Location l, Material type) {
        int x = l.getBlockX(), y = l.getBlockY(), z = l.getBlockZ();
        l.getWorld().setType(x, y - 1, z, type);
        l.getWorld().setType(x - 1, y, z, type);
        l.getWorld().setType(x + 1, y, z, type);
        l.getWorld().setType(x, y, z - 1, type);
        l.getWorld().setType(x, y, z + 1, type);
        l.getWorld().setType(x - 1, y + 1, z, type);
        l.getWorld().setType(x + 1, y + 1, z, type);
        l.getWorld().setType(x, y + 1, z - 1, type);
        l.getWorld().setType(x, y + 1, z + 1, type);
        l.getWorld().setType(x - 1, y + 2, z, type);
        l.getWorld().setType(x + 1, y + 2, z, type);
        l.getWorld().setType(x, y + 2, z - 1, type);
        l.getWorld().setType(x, y + 2, z + 1, type);
        l.getWorld().setType(x, y + 3, z, type);
    }

    public void breakCabins() {
        model.getCabins().forEach(l -> fillCabin(l, Material.AIR));
    }

    private void teleportOnLocalSpawn(Player p) {
        for (int i = 0; i < occupiedCabins.length; i++) {
            if (!occupiedCabins[i]) {
                occupiedCabins[i] = true;
                p.teleport(model.getCabins().get(i));
                playersPlaces.put(p.getName(), i);
                fillCabin(model.getCabins().get(i), Material.GLASS);
                break;
            }
        }
        p.getInventory().clear();
        p.setHealth(20.0);
        p.setFoodLevel(20);
        p.setGameMode(GameMode.ADVENTURE);
        p.setLevel(0);
        p.setExp(0f);
        SkyWarsPlugin.getInstance().broadcast(SkyWarsPlugin.getInstance().getTeleportToGameMessage(), p, p);
        info.addPlayer(p);
    }

    public void save() {
        int minX = Math.min(model.getFirstCorner().getBlockX(), model.getSecondCorner().getBlockX()), maxX = Math.max(model.getFirstCorner().getBlockX(), model.getSecondCorner().getBlockX());
        int minY = Math.min(model.getFirstCorner().getBlockY(), model.getSecondCorner().getBlockY()), maxY = Math.max(model.getFirstCorner().getBlockY(), model.getSecondCorner().getBlockY());
        int minZ = Math.min(model.getFirstCorner().getBlockZ(), model.getSecondCorner().getBlockZ()), maxZ = Math.max(model.getFirstCorner().getBlockZ(), model.getSecondCorner().getBlockZ());
        arenaData = new BlockData[(maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1)];
        int ptr = 0;
        World w = model.getFirstCorner().getWorld();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    arenaData[ptr++] = w.getBlockData(x, y, z);
                }
            }
        }
    }

    public void load() {
        int minX = Math.min(model.getFirstCorner().getBlockX(), model.getSecondCorner().getBlockX()), maxX = Math.max(model.getFirstCorner().getBlockX(), model.getSecondCorner().getBlockX());
        int minY = Math.min(model.getFirstCorner().getBlockY(), model.getSecondCorner().getBlockY()), maxY = Math.max(model.getFirstCorner().getBlockY(), model.getSecondCorner().getBlockY());
        int minZ = Math.min(model.getFirstCorner().getBlockZ(), model.getSecondCorner().getBlockZ()), maxZ = Math.max(model.getFirstCorner().getBlockZ(), model.getSecondCorner().getBlockZ());
        int ptr = 0;
        World w = model.getFirstCorner().getWorld();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    w.setBlockData(x, y, z, arenaData[ptr++]);
                }
            }
        }
    }

    @EventHandler
    private void onChestOpen(InventoryOpenEvent e) {
        if (!players.contains(e.getPlayer())) return;
        if (e.getInventory().getHolder() instanceof Chest chestState && e.getPlayer() instanceof Player p) {
            if (openedChests.contains(e.getInventory())) return;
            int totalWeight = SkyWarsPlugin.getInstance().getItems().stream().map(x -> x.getWeight()).reduce(0, Integer::sum);
            Random random = new Random();
            int i = random.nextInt(0, 9);
            while (i < 27) {
                int cur = 0;
                ItemStack itemStack = null;
                int rand = random.nextInt(0, totalWeight + 1);
                for (ChestItem item : SkyWarsPlugin.getInstance().getItems()) {
                    if (cur <= rand && rand <= cur + item.getWeight()) {
                        itemStack = item.getItem();
                        break;
                    }
                    cur += item.getWeight();
                }
                e.getInventory().setItem(i, itemStack);
                i += random.nextInt(0, 9);
            }
            openedChests.add(e.getInventory());
            statsChanges.get(e.getPlayer().getName()).addChestLooten();
            SkyWarsPlugin.getInstance().getScoreboardsManager().showScoreboard(p, state.getScoreboard());
        }
    }

    public Set<Player> getSpectators() {
        return spectators;
    }

    public void resetOpenedChests() {
        openedChests.clear();
    }

    public void setState(State state) {
        if (this.state != null) this.state.cancel(this);
        this.state = state;
        this.state.start(this);
    }

    public BossBar getInfo() {
        return info;
    }

    public int getOnline() {
        return players.size();
    }

    public int getMaxPlayers() {
        return model.getMaxPlayers();
    }

    public int getMinPlayers() {
        return model.getMinPlayers();
    }

    public World getWorld() {
        return model.getFirstCorner().getWorld();
    }

    public Location getFirstCorner() {
        return model.getFirstCorner();
    }

    public Location getSecondCorner() {
        return model.getSecondCorner();
    }

    public boolean isDamageResistence() {
        return damageResistence;
    }

    public void setDamageResistence(boolean damageResistence) {
        this.damageResistence = damageResistence;
    }
}
