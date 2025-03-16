package com.meekdev.maudio;

import com.meekdev.maudio.api.effects.AudioEffect;
import com.meekdev.maudio.api.effects.AudioSequence;
import com.meekdev.maudio.api.events.AudioEvent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public interface Maudio {
    void playSound(Location location, Sound sound, SoundCategory category, float volume, float pitch);

    void playSound(Location location, String customSound, SoundCategory category, float volume, float pitch);

    void playSoundToPlayer(Player player, Sound sound, SoundCategory category, float volume, float pitch);

    void playSoundToPlayer(Player player, String customSound, SoundCategory category, float volume, float pitch);

    void stopSound(Player player, Sound sound, SoundCategory category);

    void stopSound(Player player, String customSound, SoundCategory category);

    SoundInstance playMusic(Player player, Sound music, float volume, float pitch, float fadeInSeconds);

    SoundInstance playMusic(Player player, String customMusic, float volume, float pitch, float fadeInSeconds);

    void stopMusic(Player player, float fadeOutSeconds);

    SoundInstance playLoopingSound(Location location, Sound sound, SoundCategory category, float volume, float pitch, int intervalTicks);

    SoundInstance playLoopingSound(Player player, Sound sound, SoundCategory category, float volume, float pitch, int intervalTicks);

    void stopLoopingSound(UUID soundId);

    ZoneInstance createSoundZone(Location center, double radius, Sound sound, SoundCategory category, float volume, float pitch, int intervalTicks);

    ZoneInstance createSoundZone(Location center, double radius, String customSound, SoundCategory category, float volume, float pitch, int intervalTicks);

    void removeSoundZone(UUID zoneId);

    void stopAllSounds(Player player);

    void stopAllSounds();

    void setGlobalVolume(float volume);

    float getGlobalVolume();

    void setPlayerVolume(Player player, float volume);

    float getPlayerVolume(Player player);

    Optional<SoundInstance> getSoundInstance(UUID soundId);

    Optional<ZoneInstance> getZoneInstance(UUID zoneId);

    void registerEvents(Object listener);

    void unregisterEvents(Object listener);

    boolean triggerEvent(AudioEvent event);

    void playEffect(AudioEffect effect);

    void playSequence(AudioSequence sequence);

    void dispose();
}