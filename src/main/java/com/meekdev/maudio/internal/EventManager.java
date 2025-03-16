package com.meekdev.maudio.internal;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

public class EventManager implements Listener {
    private final Plugin plugin;
    private final AudioManager audioManager;
    private boolean registered = false;

    public EventManager(Plugin plugin, AudioManager audioManager) {
        this.plugin = plugin;
        this.audioManager = audioManager;
        register();
    }

    private void register() {
        if (!registered) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            registered = true;
        }
    }

    public void unregister() {
        if (registered) {
            HandlerList.unregisterAll(this);
            registered = false;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        audioManager.handlePlayerQuit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        String fromWorld = event.getFrom().getName();
        String toWorld = player.getWorld().getName();

        audioManager.handleWorldChange(player, fromWorld, toWorld);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled() || event.getFrom().getWorld() == null ||
                event.getTo() == null || event.getTo().getWorld() == null) {
            return;
        }

        if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            Player player = event.getPlayer();
            String fromWorld = event.getFrom().getWorld().getName();
            String toWorld = event.getTo().getWorld().getName();

            audioManager.handleWorldChange(player, fromWorld, toWorld);
        }
    }
}