package com.meekdev.maudio;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;

/**
 * API interface for audio operations - implements Maudio interface
 */
public class MaudioAPI implements Maudio {
    private final com.meekdev.maudio.impl.AudioManager manager;

    public MaudioAPI(JavaPlugin plugin) {
        this.manager = new com.meekdev.maudio.impl.AudioManager(plugin);
    }

    @Override
    public void playSound(Location location, Sound sound, SoundCategory category, float volume, float pitch) {
        manager.playSound(location, sound, category, volume, pitch);
    }

    @Override
    public void playSound(Location location, String customSound, SoundCategory category, float volume, float pitch) {
        manager.playSound(location, customSound, category, volume, pitch);
    }

    @Override
    public void playSoundToPlayer(Player player, Sound sound, SoundCategory category, float volume, float pitch) {
        manager.playSoundToPlayer(player, sound, category, volume, pitch);
    }

    @Override
    public void playSoundToPlayer(Player player, String customSound, SoundCategory category, float volume, float pitch) {
        manager.playSoundToPlayer(player, customSound, category, volume, pitch);
    }

    @Override
    public void stopSound(Player player, Sound sound, SoundCategory category) {
        manager.stopSound(player, sound, category);
    }

    @Override
    public void stopSound(Player player, String customSound, SoundCategory category) {
        manager.stopSound(player, customSound, category);
    }

    @Override
    public SoundInstance playMusic(Player player, Sound music, float volume, float pitch, float fadeInSeconds) {
        return manager.playMusic(player, music, volume, pitch, fadeInSeconds);
    }

    @Override
    public SoundInstance playMusic(Player player, String customMusic, float volume, float pitch, float fadeInSeconds) {
        return manager.playMusic(player, customMusic, volume, pitch, fadeInSeconds);
    }

    @Override
    public void stopMusic(Player player, float fadeOutSeconds) {
        manager.stopMusic(player, fadeOutSeconds);
    }

    @Override
    public SoundInstance playLoopingSound(Location location, Sound sound, SoundCategory category, float volume, float pitch, int intervalTicks) {
        return manager.playLoopingSound(location, sound, category, volume, pitch, intervalTicks);
    }

    @Override
    public SoundInstance playLoopingSound(Player player, Sound sound, SoundCategory category, float volume, float pitch, int intervalTicks) {
        return manager.playLoopingSound(player, sound, category, volume, pitch, intervalTicks);
    }

    @Override
    public void stopLoopingSound(UUID soundId) {
        manager.stopLoopingSound(soundId);
    }

    @Override
    public ZoneInstance createSoundZone(Location center, double radius, Sound sound, SoundCategory category, float volume, float pitch, int intervalTicks) {
        return manager.createSoundZone(center, radius, sound, category, volume, pitch, intervalTicks);
    }

    @Override
    public ZoneInstance createSoundZone(Location center, double radius, String customSound, SoundCategory category, float volume, float pitch, int intervalTicks) {
        return manager.createSoundZone(center, radius, customSound, category, volume, pitch, intervalTicks);
    }

    @Override
    public void removeSoundZone(UUID zoneId) {
        manager.removeSoundZone(zoneId);
    }

    @Override
    public void stopAllSounds(Player player) {
        manager.stopAllSounds(player);
    }

    @Override
    public void stopAllSounds() {
        manager.stopAllSounds();
    }

    @Override
    public void setGlobalVolume(float volume) {
        manager.setGlobalVolume(volume);
    }

    @Override
    public float getGlobalVolume() {
        return manager.getGlobalVolume();
    }

    @Override
    public void setPlayerVolume(Player player, float volume) {
        manager.setPlayerVolume(player, volume);
    }

    @Override
    public float getPlayerVolume(Player player) {
        return manager.getPlayerVolume(player);
    }

    @Override
    public Optional<SoundInstance> getSoundInstance(UUID soundId) {
        return manager.getSoundInstance(soundId);
    }

    @Override
    public Optional<ZoneInstance> getZoneInstance(UUID zoneId) {
        return manager.getZoneInstance(zoneId);
    }

    @Override
    public void dispose() {
        manager.dispose();
    }
}