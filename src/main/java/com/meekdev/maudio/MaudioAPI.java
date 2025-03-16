package com.meekdev.maudio;

import com.meekdev.maudio.api.effects.AudioEffect;
import com.meekdev.maudio.api.effects.AudioSequence;
import com.meekdev.maudio.api.events.AudioEvent;
import com.meekdev.maudio.internal.AudioManager;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.UUID;

public class MaudioAPI implements Maudio {
    private final AudioManager manager;

    public MaudioAPI(JavaPlugin plugin) {
        this.manager = new AudioManager(plugin);
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
    public void registerEvents(Object listener) {
        manager.registerEvents(listener);
    }

    @Override
    public void unregisterEvents(Object listener) {
        manager.unregisterEvents(listener);
    }

    @Override
    public boolean triggerEvent(AudioEvent event) {
        return manager.triggerEvent(event);
    }

    @Override
    public void playEffect(AudioEffect effect) {
        manager.playEffect(effect);
    }

    @Override
    public void playSequence(AudioSequence sequence) {
        manager.playSequence(sequence);
    }

    @Override
    public void dispose() {
        manager.dispose();
    }
}