package com.meekdev.maudio.internal.events;

import com.meekdev.maudio.api.events.AudioEvent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerAudioEventImpl implements AudioEvent {
    private final UUID id;
    private final String name;
    private final Player sourcePlayer;
    private final Set<UUID> targetPlayerIds;
    private final boolean global;
    private final Sound sound;
    private final String customSound;
    private SoundCategory category;
    private float volume;
    private float pitch;
    private final Location location;
    private boolean cancelled;
    private final long timestamp;
    private final Priority priority;

    public PlayerAudioEventImpl(String name, Player sourcePlayer, boolean global,
                                Sound sound, String customSound, SoundCategory category,
                                float volume, float pitch, Location location,
                                long timestamp, Priority priority) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.sourcePlayer = sourcePlayer;
        this.targetPlayerIds = new HashSet<>();
        if (sourcePlayer != null) {
            this.targetPlayerIds.add(sourcePlayer.getUniqueId());
        }
        this.global = global;
        this.sound = sound;
        this.customSound = customSound;
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
        this.location = location != null ? location.clone() : null;
        this.timestamp = timestamp;
        this.priority = priority;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Set<UUID> getTargetPlayerIds() {
        return Collections.unmodifiableSet(targetPlayerIds);
    }

    @Override
    public Set<Player> getTargetPlayers() {
        Set<Player> players = new HashSet<>();
        for (UUID playerId : targetPlayerIds) {
            Player player = org.bukkit.Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }
        return players;
    }

    @Override
    public boolean isGlobal() {
        return global;
    }

    @Override
    public Player getSourcePlayer() {
        return sourcePlayer;
    }

    @Override
    public Location getLocation() {
        return location != null ? location.clone() : null;
    }

    @Override
    public Sound getSound() {
        return sound;
    }

    @Override
    public String getCustomSound() {
        return customSound;
    }

    @Override
    public SoundCategory getCategory() {
        return category;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public Priority getPriority() {
        return priority;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public void setCategory(SoundCategory category) {
        this.category = category;
    }

    public void addTargetPlayer(Player player) {
        if (player != null) {
            targetPlayerIds.add(player.getUniqueId());
        }
    }

    public void removeTargetPlayer(Player player) {
        if (player != null) {
            targetPlayerIds.remove(player.getUniqueId());
        }
    }
}