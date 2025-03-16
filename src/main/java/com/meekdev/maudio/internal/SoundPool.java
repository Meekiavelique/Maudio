package com.meekdev.maudio.internal;

import com.meekdev.maudio.internal.model.SoundInstanceImpl;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SoundPool {
    private final Queue<SoundInstanceImpl> pool = new ConcurrentLinkedQueue<>();
    private final AtomicInteger activeSounds = new AtomicInteger(0);
    private final int maxPoolSize;
    private final int maxActiveSounds;

    public SoundPool(int poolSize, int maxSounds) {
        this.maxPoolSize = poolSize;
        this.maxActiveSounds = maxSounds;

        for (int i = 0; i < poolSize / 4; i++) {
            pool.add(createEmptyInstance());
        }
    }

    public SoundInstanceImpl obtain(Sound sound, String customSound, SoundCategory category,
                                    float volume, float pitch, Player player, Location location,
                                    boolean looping, int intervalTicks) {

        if (activeSounds.get() >= maxActiveSounds) {
            return null;
        }

        SoundInstanceImpl instance = pool.poll();
        if (instance == null) {
            if (activeSounds.get() + pool.size() < maxPoolSize) {
                instance = createEmptyInstance();
            } else {
                return null;
            }
        }

        UUID id = UUID.randomUUID();
        instance.reset(id, sound, customSound, category,
                volume, pitch, player, location, looping, intervalTicks);

        activeSounds.incrementAndGet();
        return instance;
    }

    public void release(SoundInstanceImpl instance) {
        if (instance == null) return;

        instance.reset(null, null, null, null, 0, 0, null, null, false, 0);

        if (pool.size() < maxPoolSize) {
            pool.add(instance);
        }

        activeSounds.decrementAndGet();
    }

    private SoundInstanceImpl createEmptyInstance() {
        return new SoundInstanceImpl(null, null, null, null, 0, 0, null);
    }

    public int getActiveCount() {
        return activeSounds.get();
    }

    public int getPooledCount() {
        return pool.size();
    }

    public int getMaxSize() {
        return maxPoolSize;
    }
}