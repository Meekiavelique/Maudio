package com.meekdev.maudio.impl;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Set;

class AudioProcessor implements Runnable {
    private final AudioManager manager;
    private final ThreadLocal<Boolean> isRunning = ThreadLocal.withInitial(() -> false);

    AudioProcessor(AudioManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        if (isRunning.get()) return;
        isRunning.set(true);

        try {
            processSounds();
            processZones();
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
        Set<ZoneInstanceImpl> zones = manager.getAllZones();

        for (ZoneInstanceImpl zone : zones) {
            if (!zone.isActive()) continue;

            zone.updatePlayerPositions();
            zone.incrementTick();
        }
    }
}