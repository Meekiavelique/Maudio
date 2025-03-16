package com.meekdev.maudio.api.effects;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AudioSequence {
    private final List<AudioEffect> effects;
    private final Set<UUID> targetPlayerIds;
    private final boolean concurrent;

    private AudioSequence(Builder builder) {
        this.effects = new ArrayList<>(builder.effects);
        this.targetPlayerIds = new HashSet<>(builder.targetPlayerIds);
        this.concurrent = builder.concurrent;
    }

    public List<AudioEffect> getEffects() {
        return Collections.unmodifiableList(effects);
    }

    public Set<UUID> getTargetPlayerIds() {
        return Collections.unmodifiableSet(targetPlayerIds);
    }

    public boolean isConcurrent() {
        return concurrent;
    }

    public static class Builder {
        private final List<AudioEffect> effects = new ArrayList<>();
        private final Set<UUID> targetPlayerIds = new HashSet<>();
        private boolean concurrent = false;

        public Builder addEffect(AudioEffect effect) {
            if (effect != null) {
                effects.add(effect);
            }
            return this;
        }

        public Builder then(AudioEffect effect) {
            return addEffect(effect);
        }

        public Builder addEffects(List<AudioEffect> effectList) {
            if (effectList != null) {
                for (AudioEffect effect : effectList) {
                    if (effect != null) {
                        effects.add(effect);
                    }
                }
            }
            return this;
        }

        public Builder addPlayer(Player player) {
            if (player != null) {
                targetPlayerIds.add(player.getUniqueId());
            }
            return this;
        }

        public Builder addPlayers(Iterable<Player> players) {
            if (players != null) {
                for (Player player : players) {
                    if (player != null) {
                        targetPlayerIds.add(player.getUniqueId());
                    }
                }
            }
            return this;
        }

        public Builder concurrent(boolean concurrent) {
            this.concurrent = concurrent;
            return this;
        }

        public AudioSequence build() {
            return new AudioSequence(this);
        }
    }
}