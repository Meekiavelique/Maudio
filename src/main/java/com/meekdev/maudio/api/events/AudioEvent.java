package com.meekdev.maudio.api.events;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

public interface AudioEvent {
    UUID getId();

    String getName();

    Set<UUID> getTargetPlayerIds();

    Set<Player> getTargetPlayers();

    boolean isGlobal();

    Player getSourcePlayer();

    Location getLocation();

    Sound getSound();

    String getCustomSound();

    SoundCategory getCategory();

    float getVolume();

    float getPitch();

    boolean isCancelled();

    void setCancelled(boolean cancelled);

    long getTimestamp();

    Priority getPriority();

    enum Priority {
        LOW(0),
        NORMAL(1),
        HIGH(2),
        CRITICAL(3);

        private final int value;

        Priority(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}