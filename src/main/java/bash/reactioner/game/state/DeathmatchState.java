package bash.reactioner.game.state;

import bash.reactioner.SkyWarsPlugin;
import bash.reactioner.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class DeathmatchState implements State {
    private BukkitTask borderTask;
    private int secondsLeft;
    private BukkitTask borderDamageTask;
    private BukkitTask backgroundDamageTask;
    private double continuousDamage;

    @Override
    public void start(Game game) {
        int minX = Math.min(game.getFirstCorner().getBlockX(), game.getSecondCorner().getBlockX()), maxX = Math.max(game.getFirstCorner().getBlockX(), game.getSecondCorner().getBlockX());
        int minZ = Math.min(game.getFirstCorner().getBlockZ(), game.getSecondCorner().getBlockZ()), maxZ = Math.max(game.getFirstCorner().getBlockZ(), game.getSecondCorner().getBlockZ());
        int diameter = Math.max(maxX - minX, maxZ - minZ);
        game.getBorder().setCenter((double) (maxX + minX) / 2, (double) (maxZ + minZ) / 2);
        secondsLeft = SkyWarsPlugin.getInstance().getDeathmatchDuration();
        continuousDamage = 0.3;
        borderTask = Bukkit.getScheduler().runTaskTimer(SkyWarsPlugin.getInstance(), () -> {
            secondsLeft--;
            if (secondsLeft <= 0) borderTask.cancel();
            game.getBorder().setSize(diameter * ((double) secondsLeft / SkyWarsPlugin.getInstance().getDeathmatchDuration()));
        }, 0, 20);
        borderDamageTask = Bukkit.getScheduler().runTaskTimer(SkyWarsPlugin.getInstance(), () -> {

            game.getPlayers().forEach(p -> {
                if (!p.getWorldBorder().isInside(p.getLocation())) p.damage(1);
            });
        }, 0, 10);
        backgroundDamageTask = Bukkit.getScheduler().runTaskTimer(SkyWarsPlugin.getInstance(), () -> {
            game.getPlayers().forEach(p -> {
                p.damage(continuousDamage);
                continuousDamage += 0.2;
            });
        }, 0, 20 * 3);
    }

    @Override
    public void cancel(Game game) {
        if (borderTask != null && !borderTask.isCancelled()) borderTask.cancel();
        if (borderDamageTask != null && !borderDamageTask.isCancelled()) borderDamageTask.cancel();
        if (backgroundDamageTask != null && !backgroundDamageTask.isCancelled()) backgroundDamageTask.cancel();
    }

    @Override
    public void onJoin(Player p, Game game) {

    }

    @Override
    public void onQuit(Player p, Game game) {
        game.checkForWinner();
    }

    @Override
    public boolean canDamage() {
        return true;
    }
}
