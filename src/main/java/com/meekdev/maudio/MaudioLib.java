package com.meekdev.maudio;

import org.bukkit.plugin.java.JavaPlugin;
import java.util.concurrent.atomic.AtomicReference;

public final class MaudioLib {
    private static final AtomicReference<MaudioAPI> instance = new AtomicReference<>(null);

    private MaudioLib() {}

    public static MaudioAPI init(JavaPlugin plugin) {
        MaudioAPI existing = instance.get();
        if (existing != null) {
            return existing;
        }

        MaudioAPI newInstance = new MaudioAPI(plugin);
        if (instance.compareAndSet(null, newInstance)) {
            return newInstance;
        } else {
            return instance.get();
        }
    }

    public static MaudioAPI getInstance() {
        MaudioAPI api = instance.get();
        if (api == null) {
            throw new IllegalStateException("MaudioLib has not been initialized. Call init() first.");
        }
        return api;
    }

    public static boolean isInitialized() {
        return instance.get() != null;
    }

    public static void shutdown() {
        MaudioAPI api = instance.getAndSet(null);
        if (api != null) {
            api.dispose();
        }
    }
}