package com.meekdev.maudio.api;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;

public interface SoundLookup {
    Sound getSound();

    default SoundCategory getCategory() {
        return SoundCategory.MASTER;
    }

    default float getVolume() {
        return 1.0f;
    }

    default float getPitch() {
        return 1.0f;
    }

    default int getLoopInterval() {
        return 0;
    }

    default boolean isLooping() {
        return getLoopInterval() > 0;
    }

    default float getFadeIn() {
        return 0.0f;
    }

    default float getFadeOut() {
        return 0.0f;
    }
}