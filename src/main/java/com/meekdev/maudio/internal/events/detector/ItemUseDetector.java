package com.meekdev.maudio.internal.events.detector;

import com.meekdev.maudio.api.effects.AudioEffect;
import com.meekdev.maudio.internal.AudioManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ItemUseDetector implements Listener {
    private final AudioManager audioManager;
    private final JavaPlugin plugin;
    private final Map<UUID, Long> interactCooldowns = new ConcurrentHashMap<>();
    private static final long INTERACT_COOLDOWN_MS = 250;

    public ItemUseDetector(AudioManager audioManager, JavaPlugin plugin) {
        this.audioManager = audioManager;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        Action action = event.getAction();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        if (!shouldProcessInteract(player)) {
            return;
        }

        interactCooldowns.put(player.getUniqueId(), System.currentTimeMillis());

        AudioEffect effect = audioManager.createItemUseEffect(player, item, action);
        if (effect != null) {
            audioManager.playEffect(effect);
        }
    }

    private boolean shouldProcessInteract(Player player) {
        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long lastInteract = interactCooldowns.get(playerId);

        if (lastInteract == null) {
            return true;
        }

        return now - lastInteract >= INTERACT_COOLDOWN_MS;
    }
}