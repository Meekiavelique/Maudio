package com.meekdev.maudio.internal.processor;

import com.meekdev.maudio.internal.AudioManager;
import com.meekdev.maudio.internal.SpatialManager;
import com.meekdev.maudio.internal.model.SoundInstanceImpl;
import com.meekdev.maudio.internal.model.ZoneInstanceImpl;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class AudioProcessor implements Runnable {
    private final AudioManager manager;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private int spatialCleanupCounter = 0;
    private static final int SPATIAL_CLEANUP_INTERVAL = 100;

    public AudioProcessor(AudioManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        if (isRunning.get()) return;
        isRunning.set(true);

        try {
            processSounds();
            processZones();

            spatialCleanupCounter++;
            if (spatialCleanupCounter >= SPATIAL_CLEANUP_INTERVAL) {
                spatialCleanupCounter = 0;
                manager.getSpatialManager().cleanup();
            }
        } catch (Exception e) {
            manager.getPlugin().getLogger().severe("Error in audio processing: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isRunning.set(false);
        }
    }

    private void processSounds() {
        Set<SoundInstanceImpl> sounds = manager.getAllSounds();

        for (Iterator<SoundInstanceImpl> it = sounds.iterator(); it.hasNext();) {
            SoundInstanceImpl sound = it.next();

            if (sound.getState() == SoundInstanceImpl.State.STOPPED) {
                manager.removeSoundInstance(sound.getId());
                continue;
            }

            if (sound.isFading()) {
                updateFadingSound(sound);
            }

            if (sound.isLooping() && sound.isPlaying()) {
                sound.incrementTick();
            }
        }
    }

    private void updateFadingSound(SoundInstanceImpl sound) {
        float newVolume = sound.updateFading();

        if (sound.isFading()) {
            Player player = sound.getPlayer().orElse(null);
            Location location = sound.getLocation().orElse(null);

            if (player != null && player.isOnline()) {
                playSoundToPlayer(player, sound, newVolume);
            } else if (location != null && location.getWorld() != null) {
                playLocationSound(location, sound, newVolume);
            }
        }
    }

    private void playSoundToPlayer(Player player, SoundInstanceImpl sound, float volume) {
        float adjustedVolume = manager.calculatePlayerVolume(player, volume);

        if (adjustedVolume < 0.01f) return;

        if (sound.getSound() != null) {
            player.playSound(player.getLocation(), sound.getSound(), sound.getCategory(), adjustedVolume, sound.getPitch());
        } else if (sound.getCustomSound().isPresent()) {
            player.playSound(player.getLocation(), sound.getCustomSound().get(), sound.getCategory(), adjustedVolume, sound.getPitch());
        }
    }

    private void playLocationSound(Location location, SoundInstanceImpl sound, float volume) {
        float adjustedVolume = manager.calculateVolume(volume);

        if (adjustedVolume < 0.01f) return;

        if (sound.getSound() != null) {
            location.getWorld().playSound(location, sound.getSound(), sound.getCategory(), adjustedVolume, sound.getPitch());
        } else if (sound.getCustomSound().isPresent()) {
            location.getWorld().playSound(location, sound.getCustomSound().get(), sound.getCategory(), adjustedVolume, sound.getPitch());
        }
    }

    private void processZones() {
        SpatialManager spatialManager = manager.getSpatialManager();
        Set<ZoneInstanceImpl> zones = manager.getAllZones();

        for (ZoneInstanceImpl zone : zones) {
            if (!zone.isActive()) continue;

            zone.incrementTick();

            if (!zone.shouldPlayThisTick()) {
                continue;
            }

            for (Player player : manager.getPlugin().getServer().getOnlinePlayers()) {
                if (player.getWorld().getUID().equals(zone.getWorldId())) {
                    double distanceSq = spatialManager.getCachedDistanceSquared(player, zone);
                    double radiusSq = zone.getRadius() * zone.getRadius();

                    if (distanceSq <= radiusSq) {
                        playZoneSound(player, zone, calculateDistanceVolume(zone, distanceSq, radiusSq));
                    }
                }
            }
        }
    }

    private float calculateDistanceVolume(ZoneInstanceImpl zone, double distanceSq, double radiusSq) {
        float volumeFactor = 1.0f;

        if (distanceSq > 0 && radiusSq > 0) {
            volumeFactor = 1.0f - (float) Math.sqrt(distanceSq / radiusSq);
            volumeFactor = volumeFactor * volumeFactor;
        }

        return zone.getVolume() * volumeFactor;
    }

    private void playZoneSound(Player player, ZoneInstanceImpl zone, float volume) {
        float adjustedVolume = manager.calculatePlayerVolume(player, volume);

        if (adjustedVolume < 0.01f) return;

        if (zone.getSound() != null) {
            player.playSound(player.getLocation(), zone.getSound(), zone.getCategory(), adjustedVolume, zone.getPitch());
        } else if (zone.getCustomSound().isPresent()) {
            player.playSound(player.getLocation(), zone.getCustomSound().get(), zone.getCategory(), adjustedVolume, zone.getPitch());
        }
    }
}