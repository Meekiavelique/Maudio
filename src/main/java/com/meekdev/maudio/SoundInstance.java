package com.meekdev.maudio;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;


public interface SoundInstance {
    UUID getId();

    Sound getSound();

    Optional<String> getCustomSound();

    SoundCategory getCategory();

    float getVolume();

    float getPitch();

    boolean isPlaying();

    boolean isLooping();

    boolean isFading();

    boolean isFadingIn();

    boolean isFadingOut();

    SoundInstance setVolume(float volume);

    SoundInstance setPitch(float pitch);

    SoundInstance setLocation(Location location);

    SoundInstance play();

    SoundInstance pause();

    SoundInstance stop();

    SoundInstance fadeIn(float seconds);

    SoundInstance fadeOut(float seconds);

    Optional<Player> getPlayer();

    Optional<Location> getLocation();
}