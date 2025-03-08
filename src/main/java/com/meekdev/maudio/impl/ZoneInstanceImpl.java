package com.meekdev.maudio.impl;

import com.meekdev.maudio.ZoneInstance;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

class ZoneInstanceImpl implements ZoneInstance {
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
    private final Set<UUID> playersInZone = new HashSet<>();

    ZoneInstanceImpl(UUID id, Location center, double radius, Sound sound, String customSound,
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
        playersInZone.clear();
        return this;
    }

    @Override
    public Set<Player> getPlayersInZone() {
        Set<Player> players = new HashSet<>();

        for (UUID playerId : playersInZone) {
            Player player = manager.getPlugin().getServer().getPlayer(playerId);
            if (player != null && player.isOnline()) {
                players.add(player);
            }
        }

        return players;
    }

    @Override
    public boolean isPlayerInZone(Player player) {
        return player != null && playersInZone.contains(player.getUniqueId());
    }

    void updatePlayerPositions() {
        if (!active || center.getWorld() == null) return;

        playersInZone.clear();
        double radiusSq = radius * radius;

        for (Player player : center.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(center) <= radiusSq) {
                playersInZone.add(player.getUniqueId());
            }
        }
    }

    void incrementTick() {
        if (!active) return;

        currentTick++;
        if (currentTick >= intervalTicks) {
            currentTick = 0;
            playSound();
        }
    }

    private void playSound() {
        if (!active || center.getWorld() == null) return;

        for (UUID playerId : playersInZone) {
            Player player = manager.getPlugin().getServer().getPlayer(playerId);
            if (player == null || !player.isOnline()) continue;

            float distance = (float) player.getLocation().distance(center);
            float adjustedVolume = calculateVolumeByDistance(distance);

            if (sound != null) {
                player.playSound(player.getLocation(), sound, category, adjustedVolume, pitch);
            } else if (customSound != null) {
                player.playSound(player.getLocation(), customSound, category, adjustedVolume, pitch);
            }
        }
    }

    private float calculateVolumeByDistance(float distance) {
        if (distance >= radius) return 0;

        float volumeFactor = 1.0f - (distance / (float) radius);
        return volume * volumeFactor * volumeFactor;
    }
}