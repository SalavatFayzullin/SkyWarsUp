package bash.reactioner;

import bash.reactioner.cache.TemporalCache;
import bash.reactioner.command.CommandsManager;
import bash.reactioner.game.Game;
import bash.reactioner.items.ChestItem;
import bash.reactioner.items.ItemEnchantment;
import bash.reactioner.managers.GamesManager;
import bash.reactioner.managers.ItemsManager;
import bash.reactioner.managers.KitsManager;
import bash.reactioner.managers.PlayersManager;
import bash.reactioner.menu.MenusManager;
import bash.reactioner.model.GameModel;
import bash.reactioner.model.PlayerRepository;
import bash.reactioner.model.PostgreSqlPlayerRepository;
import bash.reactioner.model.SwKit;
import bash.reactioner.papi.PlaceholdersManager;
import bash.reactioner.scoreboards.ScoreboardsManager;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.*;

public class SkyWarsPlugin extends JavaPlugin {
    private static SkyWarsPlugin instance;

    public static SkyWarsPlugin getInstance() {
        return instance;
    }

    private Location spawn;
    private int secondsBeforeStart;
    private int firstPhaseDuration = 20;
    private int secondPhaseDuration = 20;
    private int thirdPhaseDuration = 20;
    private int deathmatchDuration = 20;
    private Map<String, GameModel> models;
    private CommandsManager commandsManager;
    private ItemsManager itemsManager;
    private GamesManager gamesManager;
    private PlayersManager playersManager;
    private int restartDuration = 10;
    private Map<String, ItemEnchantment> enchantments;
    private Map<String, ChestItem> items;
    private String pluginMessagesPrefix;
    private String joinToArenaMessage;
    private String quitFromArenaMessage;
    private String joinToServerMessage;
    private String quitFromServerMessage;
    private String playerDiedMessage;
    private String playerKilledByPlayerMessage;
    private String teleportToGameMessage;
    private String teleportToSpawnMessage;
    private String startIn30;
    private String startIn15;
    private String startIn10;
    private String startIn5;
    private String startIn3;
    private String startIn2;
    private String startIn1;
    private String missedValueForPlaceholderDummy;
    private List<String> winMessage;
    private PlaceholdersManager placeholdersManager;
    private Plugin placeholderApi;
    private String createTableIfNotExists;
    private String createPlayer;
    private String readPlayer;
    private String updatePlayer;
    private String deletePlayer;
    private PlayerRepository repository;
    private String url;
    private String username;
    private String password;
    private boolean shouldCreateTable;
    private Map<String, SwKit> kits = new HashMap<>();
    private KitsManager kitsManager;
    private ScoreboardsManager scoreboardsManager;
    private List<String> lobbyScoreboard;
    private List<String> waitingScoreboard;
    private List<String> startingScoreboard;
    private List<String> activeScoreboard;
    private TemporalCache cache;
    private Economy economy;
    private MenusManager menusManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        ConfigurationSerialization.registerClass(GameModel.class);
        ConfigurationSerialization.registerClass(ItemEnchantment.class);
        ConfigurationSerialization.registerClass(ChestItem.class);
        ConfigurationSerialization.registerClass(SwKit.class);
        spawn = getConfig().getLocation("spawn");
        models = new HashMap<>();
        ConfigurationSection arenas = getConfig().getConfigurationSection("arenas");
        if (arenas != null) arenas.getKeys(false).forEach(key -> models.put(key, arenas.getSerializable(key, GameModel.class)));
        secondsBeforeStart = 10;
        commandsManager = new CommandsManager();
        getCommand("swcreate").setExecutor(commandsManager);
        getCommand("swsetcorner").setExecutor(commandsManager);
        getCommand("swaddcabin").setExecutor(commandsManager);
        getCommand("swresetcabins").setExecutor(commandsManager);
        getCommand("swsetglobalspawn").setExecutor(commandsManager);
        getCommand("swcreateenchant").setExecutor(commandsManager);
        getCommand("swcreateitem").setExecutor(commandsManager);
        getCommand("swkitcreate").setExecutor(commandsManager);
        getCommand("swkitsave").setExecutor(commandsManager);
        getCommand("swkitremove").setExecutor(commandsManager);
        getCommand("swarenaremove").setExecutor(commandsManager);
        getCommand("switemremove").setExecutor(commandsManager);
        itemsManager = new ItemsManager();
        gamesManager = new GamesManager(models);
        playersManager = new PlayersManager();
        getServer().getPluginManager().registerEvents(itemsManager, this);
        getServer().getPluginManager().registerEvents(playersManager, this);
        getServer().getPluginManager().registerEvents(gamesManager, this);
        enchantments = new HashMap<>();
        ConfigurationSection configurationSection = getConfig().getConfigurationSection("enchantments");
        if (configurationSection != null) configurationSection.getKeys(false).forEach(key -> this.enchantments.put(key, configurationSection.getSerializable(key, ItemEnchantment.class)));
        items = new HashMap<>();
        ConfigurationSection itemsSection = getConfig().getConfigurationSection("items");
        if (itemsSection != null) itemsSection.getKeys(false).forEach(key -> items.put(key, itemsSection.getSerializable(key, ChestItem.class)));
        pluginMessagesPrefix = getConfig().getString("plugin-messages-prefix");
        joinToArenaMessage = getConfig().getString("join-to-arena-message");
        quitFromArenaMessage = getConfig().getString("quit-from-arena-message");
        joinToServerMessage = getConfig().getString("join-to-server-message");
        quitFromServerMessage = getConfig().getString("quit-from-server-message");
        playerDiedMessage = getConfig().getString("player-died-message");
        playerKilledByPlayerMessage = getConfig().getString("player-killed-by-player-message");
        teleportToGameMessage = getConfig().getString("teleport-to-game-message");
        teleportToSpawnMessage = getConfig().getString("teleport-to-spawn-message");
        startIn30 = getConfig().getString("start-messages.start-in-30");
        startIn15 = getConfig().getString("start-messages.start-in-15");
        startIn10 = getConfig().getString("start-messages.start-in-10");
        startIn5 = getConfig().getString("start-messages.start-in-5");
        startIn3 = getConfig().getString("start-messages.start-in-3");
        startIn2 = getConfig().getString("start-messages.start-in-2");
        startIn1 = getConfig().getString("start-messages.start-in-1");
        missedValueForPlaceholderDummy = getConfig().getString("missed-value-for-placeholder-dummy");
        winMessage = (List<String>) getConfig().getList("win-message", new ArrayList<>());
        placeholderApi = getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (placeholderApi != null) {
            placeholdersManager = new PlaceholdersManager();
            placeholdersManager.register();
        }
        createTableIfNotExists = getConfig().getString("sql.create-table-if-not-exists");
        createPlayer = getConfig().getString("sql.create-player");
        readPlayer = getConfig().getString("sql.read-player");
        updatePlayer = getConfig().getString("sql.update-player");
        deletePlayer = getConfig().getString("sql.delete-player");
        url = getConfig().getString("db.url");
        username = getConfig().getString("db.username");
        password = getConfig().getString("db.password");
        shouldCreateTable = getConfig().getBoolean("db.create-table-if-not-exists");
        try {
            repository = new PostgreSqlPlayerRepository(url, username, password);
            if (shouldCreateTable) repository.createTableIfDoesNotExist();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        kits = new HashMap<>();
        ConfigurationSection kitsSection = getConfig().getConfigurationSection("kits");
        if (kitsSection != null) kitsSection.getKeys(false).forEach(key -> kits.put(key, kitsSection.getSerializable(key, SwKit.class)));
        kitsManager = new KitsManager();
        scoreboardsManager = new ScoreboardsManager();
        lobbyScoreboard = (List<String>) getConfig().getList("scoreboards.lobby");
        waitingScoreboard = (List<String>) getConfig().getList("scoreboards.waiting");
        startingScoreboard = (List<String>) getConfig().getList("scoreboards.starting");
        activeScoreboard = (List<String>) getConfig().getList("scoreboards.active");
        cache = new TemporalCache();
        Plugin vault = getServer().getPluginManager().getPlugin("Vault");
        if (vault != null) {
            RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
            if (economyProvider != null) economy = economyProvider.getProvider();
            System.out.println(getServer().getServicesManager().getKnownServices());
        }
        menusManager = new MenusManager();
    }

    public MenusManager getMenusManager() {
        return menusManager;
    }

    public Economy getEconomy() {
        return economy;
    }

    public TemporalCache getCache() {
        return cache;
    }

    public ScoreboardsManager getScoreboardsManager() {
        return scoreboardsManager;
    }

    public List<String> getLobbyScoreboard() {
        return lobbyScoreboard;
    }

    public List<String> getWaitingScoreboard() {
        return waitingScoreboard;
    }

    public List<String> getStartingScoreboard() {
        return startingScoreboard;
    }

    public List<String> getActiveScoreboard() {
        return activeScoreboard;
    }

    public KitsManager getKitsManager() {
        return kitsManager;
    }

    public boolean saveKit(String name, Player p, double price) {
        SwKit kit = kits.get(name);
        if (kit == null) return false;
        kit.takeSnapshot(p, price);
        getConfig().set("kits." + name, kit);
        saveConfig();
        return true;
    }

    public boolean removeKit(String name) {
        SwKit kit = kits.get(name);
        if (kit == null) return false;
        getConfig().set("kits." + name, null);
        saveConfig();
        kits.remove(name);
        return true;
    }

    public Map<String, SwKit> getKits() {
        return kits;
    }

    public void createKit(String name, List<String> lore, ItemStack icon) {
        SwKit kit = new SwKit(icon, name, lore);
        kits.put(name, kit);
        getConfig().set("kits." + name, kit);
        saveConfig();
    }

    public SwKit getKit(String name) {
        return kits.get(name);
    }

    public PlayerRepository getRepository() {
        return repository;
    }

    public String getUpdatePlayer() {
        return updatePlayer;
    }

    public String getDeletePlayer() {
        return deletePlayer;
    }

    public String getReadPlayer() {
        return readPlayer;
    }

    public String getCreateTableIfNotExists() {
        return createTableIfNotExists;
    }

    public String getCreatePlayer() {
        return createPlayer;
    }

    public String getPluginMessagesPrefix() {
        return pluginMessagesPrefix;
    }

    public String getJoinToArenaMessage() {
        return joinToArenaMessage;
    }

    public String getQuitFromArenaMessage() {
        return quitFromArenaMessage;
    }

    public String getJoinToServerMessage() {
        return joinToServerMessage;
    }

    public String getQuitFromServerMessage() {
        return quitFromServerMessage;
    }

    public String getPlayerDiedMessage() {
        return playerDiedMessage;
    }

    public String getPlayerKilledByPlayerMessage() {
        return playerKilledByPlayerMessage;
    }

    public String getTeleportToGameMessage() {
        return teleportToGameMessage;
    }

    public String getTeleportToSpawnMessage() {
        return teleportToSpawnMessage;
    }

    public String getStartIn30() {
        return startIn30;
    }

    public String getStartIn15() {
        return startIn15;
    }

    public String getStartIn10() {
        return startIn10;
    }

    public String getStartIn5() {
        return startIn5;
    }

    public String getStartIn3() {
        return startIn3;
    }

    public String getStartIn2() {
        return startIn2;
    }

    public String getStartIn1() {
        return startIn1;
    }

    public String getMissedValueForPlaceholderDummy() {
        return missedValueForPlaceholderDummy;
    }

    public List<String> getWinMessage() {
        return winMessage;
    }

    @Override
    public void onDisable() {
        gamesManager.resetAllArenas();
        if (repository != null) {
            try {
                repository.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Game getGameByPlayer(Player p) {
        return gamesManager.getGameByPlayer(p);
    }

    public void broadcast(String message, Player receiver, Player placeholdersTarget) {
        if (placeholderApi != null) {
            if (placeholdersTarget != null) message = PlaceholderAPI.setPlaceholders(placeholdersTarget, message);
            else message = PlaceholderAPI.setPlaceholders(receiver, message);
        }
        receiver.sendMessage(Component.text(pluginMessagesPrefix + message));
    }

    public void createItem(String name, int weight, Material type, int minCount, int maxCount, List<String> enchantments) {
        ChestItem item = new ChestItem(type, minCount, maxCount, enchantments, weight);
        items.put(name, item);
        getConfig().set("items." + name, item);
        saveConfig();
    }

    public boolean removeItem(String name) {
        ChestItem item = items.get(name);
        if (item == null) return false;
        items.remove(name);
        getConfig().set("items." + name, null);
        saveConfig();
        return true;
    }

    public void createEnchantment(String name, String type, Map<Integer, Double> levels) {
        ItemEnchantment enchantment = new ItemEnchantment(Enchantment.getByKey(NamespacedKey.fromString(type)), levels);
        enchantments.put(name, enchantment);
        getConfig().set("enchantments." + name, enchantment);
        saveConfig();
    }

    public boolean removeEnchantment(String name) {
        ItemEnchantment enchantment = enchantments.get(name);
        if (enchantment == null) return false;
        enchantments.remove(name);
        getConfig().set("enchantments." + name, null);
        saveConfig();
        return true;
    }

    public int getRestartDuration() {
        return restartDuration;
    }

    public void setGlobalSpawn(Location location) {
        spawn = location;
        getConfig().set("spawn", location);
        saveConfig();
    }

    public Location getSpawn() {
        return spawn;
    }

    public ItemsManager getItemsManager() {
        return itemsManager;
    }

    public GamesManager getGamesManager() {
        return gamesManager;
    }

    public PlayersManager getPlayersManager() {
        return playersManager;
    }

    public boolean containsArena(String name) {
        return models.containsKey(name);
    }

    public boolean resetCabins(String name) {
        GameModel model = models.get(name);
        if (model == null) return false;
        model.getCabins().clear();
        getConfig().set("arenas." + name, model);
        saveConfig();
        return true;
    }

    public ItemEnchantment getEnchantment(String name) {
        return enchantments.get(name);
    }

    public Collection<ChestItem> getItems() {
        return items.values();
    }

    public int addCabin(String name, Location localSpawn) {
        GameModel model = models.get(name);
        if (model == null) return 0;
        if (model.getCabins() == null) model.setCabins(new ArrayList<>());
        model.getCabins().add(localSpawn);
        getConfig().set("arenas." + name, model);
        saveConfig();
        return model.getCabins().size();
    }

    public boolean setCorner(String name, int corner, Location location) {
        GameModel model = models.get(name);
        if (model == null) return false;
        if (corner == 1) model.setFirstCorner(location);
        else model.setSecondCorner(location);
        getConfig().set("arenas." + name, model);
        saveConfig();
        return true;
    }

    public void createArena(String name, int minPlayers, int maxPlayers) {
        GameModel model = new GameModel(minPlayers, maxPlayers);
        models.put(name, model);
        getConfig().set("arenas." + name, model);
        saveConfig();
        gamesManager.addGame(name, model);
    }

    public boolean removeArena(String name) {
        if (!models.containsKey(name)) return false;
        models.remove(name);
        getConfig().set("arenas." + name, null);
        saveConfig();
        gamesManager.removeGame(name);
        return true;
    }

    public void teleportOnSpawn(Player p) {
        if (SkyWarsPlugin.getInstance().getSpawn() != null) p.teleport(SkyWarsPlugin.getInstance().getSpawn());
        else Bukkit.broadcast(Component.text("Oh, shit, I'm sorry. But there is no spawn defined"));
        p.getInventory().clear();
        p.setHealth(20.0);
        p.setFoodLevel(20);
        p.setGameMode(GameMode.ADVENTURE);
        p.setLevel(0);
        p.setExp(0f);
        scoreboardsManager.showScoreboard(p, getLobbyScoreboard());
        p.getInventory().setItem(0, SkyWarsPlugin.getInstance().getItemsManager().getJoinGame().getItem());
    }

    public int getSecondsBeforeStart() {
        return secondsBeforeStart;
    }

    public int getFirstPhaseDuration() {
        return firstPhaseDuration;
    }

    public int getSecondPhaseDuration() {
        return secondPhaseDuration;
    }

    public int getThirdPhaseDuration() {
        return thirdPhaseDuration;
    }

    public int getDeathmatchDuration() {
        return deathmatchDuration;
    }
}
