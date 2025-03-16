package com.meekdev.maudio.internal.model;

import com.meekdev.maudio.ZoneInstance;
import com.meekdev.maudio.internal.AudioManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ZoneInstanceImpl implements ZoneInstance {
    private final UUID id;
    private final Location center;
    private double radius;
    private final Sound sound;
    private final String customSound;
    private final SoundCategory category;
    private float volume;
    private float pitch;
    private int intervalTicks;
    private boolean active;
    private final AudioManager manager;
    private int currentTick;
    private final UUID worldId;

    public ZoneInstanceImpl(UUID id, Location center, double radius, Sound sound, String customSound,
                            SoundCategory category, float volume, float pitch, int intervalTicks,
                            AudioManager manager) {
        this.id = id;
        this.center = center.clone();
        this.radius = radius;
        this.sound = sound;
        this.customSound = customSound;
        this.category = category;
        this.volume = volume;
        this.pitch = pitch;
        this.intervalTicks = Math.max(1, intervalTicks);
        this.manager = manager;
        this.worldId = center.getWorld() != null ? center.getWorld().getUID() : null;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Location getCenter() {
        return center.clone();
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public Sound getSound() {
        return sound;
    }

    @Override
    public Optional<String> getCustomSound() {
        return Optional.ofNullable(customSound);
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
    public int getIntervalTicks() {
        return intervalTicks;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public ZoneInstance setVolume(float volume) {
        this.volume = volume;
        return this;
    }

    @Override
    public ZoneInstance setPitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    @Override
    public ZoneInstance setRadius(double radius) {
        this.radius = radius;
        manager.getSpatialManager().updateZone(this);
        return this;
    }

    @Override
    public ZoneInstance setIntervalTicks(int ticks) {
        this.intervalTicks = Math.max(1, ticks);
        return this;
    }

    @Override
    public ZoneInstance activate() {
        this.active = true;
        return this;
    }

    @Override
    public ZoneInstance deactivate() {
        this.active = false;
        return this;
    }

    @Override
    public Set<Player> getPlayersInZone() {
        if (!active || worldId == null) {
            return Set.of();
        }

        World world = center.getWorld();
        if (world == null) {
            return Set.of();
        }

        double radiusSq = radius * radius;
        return world.getPlayers().stream()
                .filter(player -> player.getLocation().distanceSquared(center) <= radiusSq)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isPlayerInZone(Player player) {
        if (!active || player == null || !player.isOnline() || worldId == null) {
            return false;
        }

        World playerWorld = player.getWorld();
        if (playerWorld == null || !playerWorld.getUID().equals(worldId)) {
            return false;
        }

        return player.getLocation().distanceSquared(center) <= (radius * radius);
    }

    public void incrementTick() {
        if (!active) return;

        currentTick++;
        if (currentTick >= intervalTicks) {
            currentTick = 0;
        }
    }

    public boolean shouldPlayThisTick() {
        return active && currentTick == 0;
    }

    public UUID getWorldId() {
        return worldId;
    }
}