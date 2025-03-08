package com.meekdev.maudio;

import com.meekdev.maudio.impl.AudioManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main entry point for the MaudioLib library
 */
public final class MaudioLib {
    private static volatile MaudioAPI instance;

    private MaudioLib() {}

    /**
     * Initialize the Maudio library with the given plugin
     */
    public static MaudioAPI init(JavaPlugin plugin) {
        if (instance == null) {
            synchronized (MaudioLib.class) {
                if (instance == null) {
                    instance = new MaudioAPI(plugin);
                }
            }
        }
        return instance;
    }

    /**
     * Get the MaudioAPI instance
     */
    public static MaudioAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("MaudioLib has not been initialized. Call init() first.");
        }
        return instance;
    }

    /**
     * Shutdown the Maudio library and clean up resources
     */
    public static void shutdown() {
        if (instance != null) {
            instance.dispose();
            instance = null;
        }
    }
}