package me.rothes.protocolstringreplacer.scheduler;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.concurrent.TimeUnit;

public class PsrScheduler {

    private PsrScheduler() {

    }
    private static final ProtocolStringReplacer PLUGIN = ProtocolStringReplacer.getInstance();
    private static final boolean FOLIA = PLUGIN.isFolia();

    public static PsrTask runTaskTimerAsynchronously(Runnable runnable, long delay, long period) {
        if (FOLIA) {
            return new PsrTask(Bukkit.getServer().getAsyncScheduler().runAtFixedRate(PLUGIN, val -> runnable.run(), delay * 50, period * 50, TimeUnit.MILLISECONDS));
        } else {
            return new PsrTask(Bukkit.getScheduler().runTaskTimerAsynchronously(PLUGIN, runnable, delay, period));
        }
    }
    public static PsrTask runTaskLaterAsynchronously(Runnable runnable, long delay) {
        if (FOLIA) {
            return new PsrTask(Bukkit.getServer().getAsyncScheduler().runDelayed(PLUGIN, val -> runnable.run(), delay * 50, TimeUnit.MILLISECONDS));
        } else {
            return new PsrTask(Bukkit.getScheduler().runTaskLaterAsynchronously(PLUGIN, runnable, delay));
        }
    }

    public static PsrTask runTaskAsynchronously(Runnable runnable) {
        if (FOLIA) {
            return new PsrTask(Bukkit.getServer().getAsyncScheduler().runNow(PLUGIN, val -> runnable.run()));
        } else {
            return new PsrTask(Bukkit.getScheduler().runTaskAsynchronously(PLUGIN, runnable));
        }
    }

    public static void runTask(Runnable runnable) {
        if (FOLIA) {
            Bukkit.getServer().getGlobalRegionScheduler().run(PLUGIN, val -> runnable.run());
        } else {
            Bukkit.getScheduler().runTask(PLUGIN, runnable);
        }
    }

    public static void run(Runnable runnable, Location location) {
        if (FOLIA) {
            Bukkit.getServer().getRegionScheduler().execute(PLUGIN, location, runnable);
        } else {
            runnable.run();
        }
    }

    public static void runTaskLater(Runnable runnable, long delay) {
        if (FOLIA) {
            Bukkit.getServer().getGlobalRegionScheduler().runDelayed(PLUGIN, val -> runnable.run(), delay);
        } else {
            Bukkit.getScheduler().runTaskLater(PLUGIN, runnable, delay);
        }
    }

    public static void runTaskLater(Runnable runnable, Location location, long delay) {
        if (FOLIA) {
            Bukkit.getServer().getRegionScheduler().runDelayed(PLUGIN, location, val -> runnable.run(), delay);
        } else {
            Bukkit.getScheduler().runTaskLater(PLUGIN, runnable, delay);
        }
    }

    public static void cancelTasks() {
        if (FOLIA) {
            Bukkit.getServer().getGlobalRegionScheduler().cancelTasks(PLUGIN);
            Bukkit.getServer().getAsyncScheduler().cancelTasks(PLUGIN);
        } else {
            Bukkit.getScheduler().cancelTasks(PLUGIN);
        }
    }

}
