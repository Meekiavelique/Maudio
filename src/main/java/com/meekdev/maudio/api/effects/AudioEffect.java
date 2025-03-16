package com.meekdev.maudio.api.effects;

import com.meekdev.maudio.api.SoundLookup;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AudioEffect {
    private Sound sound;
    private String customSound;
    private SoundCategory category;
    private float volume;
    private float pitch;
    private final Set<UUID> targetPlayerIds;
    private final boolean global;
    private final Location location;
    private float fadeIn;
    private float fadeOut;
    private final float duration;
    private boolean looping;
    private int loopInterval;

    private AudioEffect(Builder builder) {
        this.sound = builder.sound;
        this.customSound = builder.customSound;
        this.category = builder.category;
        this.volume = builder.volume;
        this.pitch = builder.pitch;
        this.targetPlayerIds = builder.targetPlayerIds;
        this.global = builder.global;
        this.location = builder.location;
        this.fadeIn = builder.fadeIn;
        this.fadeOut = builder.fadeOut;
        this.duration = builder.duration;
        this.looping = builder.looping;
        this.loopInterval = builder.loopInterval;
    }

    public Sound getSound() {
        return sound;
    }

    public String getCustomSound() {
        return customSound;
    }

    public SoundCategory getCategory() {
        return category;
    }

    public float getVolume() {
        return volume;
    }

    public float getPitch() {
        return pitch;
    }

    public Set<UUID> getTargetPlayerIds() {
        return Collections.unmodifiableSet(targetPlayerIds);
    }

    public boolean isGlobal() {
        return global;
    }

    public Location getLocation() {
        return location != null ? location.clone() : null;
    }

    public float getFadeIn() {
        return fadeIn;
    }

    public float getFadeOut() {
        return fadeOut;
    }

    public float getDuration() {
        return duration;
    }

    public boolean isLooping() {
        return looping;
    }

    public int getLoopInterval() {
        return loopInterval;
    }

    public static class Builder {
        private Sound sound;
        private String customSound;
        private SoundCategory category = SoundCategory.MASTER;
        private float volume = 1.0f;
        private float pitch = 1.0f;
        private Set<UUID> targetPlayerIds = new HashSet<>();
        private boolean global = false;
        private Location location;
        private float fadeIn = 0.0f;
        private float fadeOut = 0.0f;
        private float duration = 0.0f;
        private boolean looping = false;
        private int loopInterval = 20;

        public Builder sound(Sound sound) {
            this.sound = sound;
            return this;
        }

        public Builder customSound(String customSound) {
            this.customSound = customSound;
            return this;
        }

        public Builder category(SoundCategory category) {
            this.category = category;
            return this;
        }

        public Builder volume(float volume) {
            this.volume = volume;
            return this;
        }

        public Builder pitch(float pitch) {
            this.pitch = pitch;
            return this;
        }

        public Builder addPlayer(Player player) {
            if (player != null) {
                this.targetPlayerIds.add(player.getUniqueId());
            }
            return this;
        }

        public Builder addPlayers(Iterable<Player> players) {
            if (players != null) {
                for (Player player : players) {
                    if (player != null) {
                        this.targetPlayerIds.add(player.getUniqueId());
                    }
                }
            }
            return this;
        }

        public Builder global() {
            this.global = true;
            return this;
        }

        public Builder location(Location location) {
            this.location = location != null ? location.clone() : null;
            return this;
        }

        public Builder fadeIn(float fadeIn) {
            this.fadeIn = Math.max(0, fadeIn);
            return this;
        }

        public Builder fadeOut(float fadeOut) {
            this.fadeOut = Math.max(0, fadeOut);
            return this;
        }

        public Builder duration(float duration) {
            this.duration = Math.max(0, duration);
            return this;
        }

        public Builder looping(boolean looping) {
            this.looping = looping;
            return this;
        }

        public Builder loopInterval(int loopInterval) {
            this.loopInterval = Math.max(1, loopInterval);
            return this;
        }

        public AudioEffect build() {
            return new AudioEffect(this);
        }
    }

    public AudioEffect soundLookup(SoundLookup lookup) {
        if (lookup != null) {
            this.sound = lookup.getSound();
            this.customSound = null;
            this.category = lookup.getCategory();
            this.volume = lookup.getVolume();
            this.pitch = lookup.getPitch();
            this.fadeIn = lookup.getFadeIn();
            this.fadeOut = lookup.getFadeOut();

            if (lookup.isLooping()) {
                this.looping = true;
                this.loopInterval = lookup.getLoopInterval();
            }
        }
        return this;
    }
}