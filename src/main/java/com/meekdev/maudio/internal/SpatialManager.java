package com.meekdev.maudio.internal;

import com.meekdev.maudio.internal.model.ZoneInstanceImpl;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SpatialManager {
    private final Map<UUID, List<ZoneInstanceImpl>> worldZones = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, CachedDistance>> distanceCache = new ConcurrentHashMap<>();
    private final int gridSize;
    private final int cacheDuration;

    public SpatialManager(int gridSize, int cacheDuration) {
        this.gridSize = gridSize;
        this.cacheDuration = cacheDuration;
    }

    public void addZone(ZoneInstanceImpl zone) {
        Location center = zone.getCenter();
        World world = center.getWorld();

        if (world == null) {
            return;
        }

        UUID worldId = world.getUID();
        List<ZoneInstanceImpl> zones = worldZones.computeIfAbsent(worldId, k -> new ArrayList<>());

        zones.add(zone);
    }

    public void removeZone(ZoneInstanceImpl zone) {
        UUID worldId = zone.getWorldId();
        if (worldId == null) {
            return;
        }

        List<ZoneInstanceImpl> zones = worldZones.get(worldId);
        if (zones != null) {
            zones.removeIf(z -> z.getId().equals(zone.getId()));
        }
    }

    public void updateZone(ZoneInstanceImpl zone) {
        removeZone(zone);
        addZone(zone);
    }

    public List<ZoneInstanceImpl> getZonesInRange(Player player) {
        if (player == null || !player.isOnline()) {
            return new ArrayList<>();
        }

        Location loc = player.getLocation();
        World world = loc.getWorld();

        if (world == null) {
            return new ArrayList<>();
        }

        UUID worldId = world.getUID();
        List<ZoneInstanceImpl> zones = worldZones.get(worldId);

        if (zones == null || zones.isEmpty()) {
            return new ArrayList<>();
        }

        List<ZoneInstanceImpl> result = new ArrayList<>();
        for (ZoneInstanceImpl zone : zones) {
            if (!zone.isActive()) continue;

            double distanceSq = getCachedDistanceSquared(player, zone);
            double radiusSq = zone.getRadius() * zone.getRadius();

            if (distanceSq <= radiusSq) {
                result.add(zone);
            }
        }

        return result;
    }

    public double getCachedDistanceSquared(Player player, ZoneInstanceImpl zone) {
        UUID playerId = player.getUniqueId();
        UUID zoneId = zone.getId();

        Map<UUID, CachedDistance> playerCache = distanceCache.computeIfAbsent(
                playerId, k -> new ConcurrentHashMap<>()
        );

        CachedDistance cached = playerCache.get(zoneId);
        long now = System.currentTimeMillis();

        if (cached != null && now - cached.timestamp < cacheDuration) {
            return cached.distanceSquared;
        }

        Location playerLoc = player.getLocation();
        Location zoneCenter = zone.getCenter();
        double distSq = playerLoc.distanceSquared(zoneCenter);

        playerCache.put(zoneId, new CachedDistance(distSq, now));
        return distSq;
    }

    public void clearPlayerCache(UUID playerId) {
        distanceCache.remove(playerId);
    }

    public void clear() {
        worldZones.clear();
        distanceCache.clear();
    }

    public void cleanup() {
        long now = System.currentTimeMillis();
        long expiry = cacheDuration * 3;

        for (Map<UUID, CachedDistance> playerCache : distanceCache.values()) {
            playerCache.entrySet().removeIf(entry -> now - entry.getValue().timestamp > expiry);
        }

        distanceCache.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    private static class CachedDistance {
        final double distanceSquared;
        final long timestamp;

        CachedDistance(double distanceSquared, long timestamp) {
            this.distanceSquared = distanceSquared;
            this.timestamp = timestamp;
        }
    }
}