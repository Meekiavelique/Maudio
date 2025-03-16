package com.meekdev.maudio;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;


public interface ZoneInstance {
    UUID getId();

    Location getCenter();

    double getRadius();

    Sound getSound();

    Optional<String> getCustomSound();

    SoundCategory getCategory();

    float getVolume();

    float getPitch();

    int getIntervalTicks();

    boolean isActive();

    ZoneInstance setVolume(float volume);

    ZoneInstance setPitch(float pitch);

    ZoneInstance setRadius(double radius);

    ZoneInstance setIntervalTicks(int ticks);

    ZoneInstance activate();

    ZoneInstance deactivate();

    Set<Player> getPlayersInZone();

    boolean isPlayerInZone(Player player);
}