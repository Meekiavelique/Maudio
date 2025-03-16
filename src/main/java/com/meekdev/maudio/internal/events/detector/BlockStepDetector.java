package com.meekdev.maudio.internal.events.detector;

import com.meekdev.maudio.api.effects.AudioEffect;
import com.meekdev.maudio.api.events.AudioEvent;
import com.meekdev.maudio.api.events.AudioTrigger;
import com.meekdev.maudio.internal.AudioManager;
import com.meekdev.maudio.internal.events.PlayerAudioEventImpl;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BlockStepDetector implements Listener {
    private final AudioManager audioManager;
    private final JavaPlugin plugin;
    private final Map<UUID, Location> lastBlockLocations = new ConcurrentHashMap<>();
    private final Map<Material, List<Method>> blockHandlers = new ConcurrentHashMap<>();
    private final Map<UUID, Long> playerStepCooldowns = new ConcurrentHashMap<>();
    private static final long STEP_COOLDOWN_MS = 10;

    public BlockStepDetector(AudioManager audioManager, JavaPlugin plugin) {
        this.audioManager = audioManager;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!shouldProcessStep(player)) {
            return;
        }

        Location to = event.getTo();
        if (to == null) return;

        Location lastLoc = lastBlockLocations.get(player.getUniqueId());
        if (lastLoc != null && isSameBlock(lastLoc, to)) {
            return;
        }

        Block block = to.getBlock().getRelative(0, -1, 0);
        if (block.getType() == Material.AIR) {
            return;
        }

        lastBlockLocations.put(player.getUniqueId(), to.clone());
        playerStepCooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        AudioEffect effect = audioManager.createBlockStepEffect(player, block);
        if (effect != null) {
            audioManager.playEffect(effect);
        }
    }

    private boolean shouldProcessStep(Player player) {
        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long lastStep = playerStepCooldowns.get(playerId);

        if (lastStep == null) {
            return true;
        }

        return now - lastStep >= STEP_COOLDOWN_MS;
    }

    private boolean isSameBlock(Location a, Location b) {
        return a.getBlockX() == b.getBlockX() &&
                a.getBlockY() == b.getBlockY() &&
                a.getBlockZ() == b.getBlockZ();
    }
}