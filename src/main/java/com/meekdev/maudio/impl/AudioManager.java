package com.meekdev.maudio.impl;

import com.meekdev.maudio.Maudio;
import com.meekdev.maudio.SoundInstance;
import com.meekdev.maudio.ZoneInstance;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class AudioManager implements Maudio {
    private final JavaPlugin plugin;
    private final AudioProcessor processor;
    private final Map<UUID, SoundInstanceImpl> activeSounds = new ConcurrentHashMap<>();
    private final Map<UUID, ZoneInstanceImpl> activeZones = new ConcurrentHashMap<>();
    private final Map<UUID, Float> playerVolumes = new ConcurrentHashMap<>();
    private float globalVolume = 1.0f;
    private BukkitTask processorTask;

    public AudioManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.processor = new AudioProcessor(this);
        initProcessor();
    }

    private void initProcessor() {
        processorTask = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                processor,
                1L,
                1L
        );
    }

    @Override
    public void playSound(Location location, Sound sound, SoundCategory category, float volume, float pitch) {
        if (location == null || location.getWorld() == null) return;
        location.getWorld().playSound(location, sound, category, calculateVolume(volume), pitch);
    }

    @Override
    public void playSound(Location location, String customSound, SoundCategory category, float volume, float pitch) {
        if (location == null || location.getWorld() == null) return;
        location.getWorld().playSound(location, customSound, category, calculateVolume(volume), pitch);
    }

    @Override
    public void playSoundToPlayer(Player player, Sound sound, SoundCategory category, float volume, float pitch) {
        if (player == null || !player.isOnline()) return;
        player.playSound(player.getLocation(), sound, category, calculatePlayerVolume(player, volume), pitch);
    }

    @Override
    public void playSoundToPlayer(Player player, String customSound, SoundCategory category, float volume, float pitch) {
        if (player == null || !player.isOnline()) return;
        player.playSound(player.getLocation(), customSound, category, calculatePlayerVolume(player, volume), pitch);
    }

    @Override
    public void stopSound(Player player, Sound sound, SoundCategory category) {
        if (player == null || !player.isOnline()) return;
        player.stopSound(sound, category);
    }

    @Override
    public void stopSound(Player player, String customSound, SoundCategory category) {
        if (player == null || !player.isOnline()) return;
        player.stopSound(customSound, category);
    }

    @Override
    public SoundInstance playMusic(Player player, Sound music, float volume, float pitch, float fadeInSeconds) {
        if (player == null || !player.isOnline()) {
            return null;
        }

        stopPlayerMusicSounds(player);

        SoundInstanceImpl instance = new SoundInstanceImpl(
                UUID.randomUUID(),
                music,
                null,
                SoundCategory.MUSIC,
                volume,
                pitch,
                player
        );

        activeSounds.put(instance.getId(), instance);

        if (fadeInSeconds > 0) {
            instance.fadeIn(fadeInSeconds);
        } else {
            playSoundToPlayer(player, music, SoundCategory.MUSIC, volume, pitch);
            instance.setState(SoundInstanceImpl.State.PLAYING);
        }

        return instance;
    }

    @Override
    public SoundInstance playMusic(Player player, String customMusic, float volume, float pitch, float fadeInSeconds) {
        if (player == null || !player.isOnline()) {
            return null;
        }

        stopPlayerMusicSounds(player);

        SoundInstanceImpl instance = new SoundInstanceImpl(
                UUID.randomUUID(),
                null,
                customMusic,
                SoundCategory.MUSIC,
                volume,
                pitch,
                player
        );

        activeSounds.put(instance.getId(), instance);

        if (fadeInSeconds > 0) {
            instance.fadeIn(fadeInSeconds);
        } else {
            playSoundToPlayer(player, customMusic, SoundCategory.MUSIC, volume, pitch);
            instance.setState(SoundInstanceImpl.State.PLAYING);
        }

        return instance;
    }

    @Override
    public void stopMusic(Player player, float fadeOutSeconds) {
        if (player == null) return;

        getSoundsByPlayer(player)
                .stream()
                .filter(sound -> sound.getCategory() == SoundCategory.MUSIC)
                .forEach(sound -> {
                    if (fadeOutSeconds > 0) {
                        sound.fadeOut(fadeOutSeconds);
                    } else {
                        sound.stop();
                    }
                });
    }

    @Override
    public SoundInstance playLoopingSound(Location location, Sound sound, SoundCategory category, float volume, float pitch, int intervalTicks) {
        if (location == null || location.getWorld() == null) {
            return null;
        }

        SoundInstanceImpl instance = new SoundInstanceImpl(
                UUID.randomUUID(),
                sound,
                null,
                category,
                volume,
                pitch,
                null,
                location,
                true,
                intervalTicks
        );

        activeSounds.put(instance.getId(), instance);
        instance.play();

        return instance;
    }

    @Override
    public SoundInstance playLoopingSound(Player player, Sound sound, SoundCategory category, float volume, float pitch, int intervalTicks) {
        if (player == null || !player.isOnline()) {
            return null;
        }

        SoundInstanceImpl instance = new SoundInstanceImpl(
                UUID.randomUUID(),
                sound,
                null,
                category,
                volume,
                pitch,
                player,
                null,
                true,
                intervalTicks
        );

        activeSounds.put(instance.getId(), instance);
        instance.play();

        return instance;
    }

    @Override
    public void stopLoopingSound(UUID soundId) {
        Optional.ofNullable(activeSounds.get(soundId)).ifPresent(SoundInstanceImpl::stop);
    }

    @Override
    public ZoneInstance createSoundZone(Location center, double radius, Sound sound, SoundCategory category, float volume, float pitch, int intervalTicks) {
        if (center == null || center.getWorld() == null) {
            return null;
        }

        ZoneInstanceImpl zone = new ZoneInstanceImpl(
                UUID.randomUUID(),
                center,
                radius,
                sound,
                null,
                category,
                volume,
                pitch,
                intervalTicks,
                this
        );

        activeZones.put(zone.getId(), zone);
        zone.activate();

        return zone;
    }

    @Override
    public ZoneInstance createSoundZone(Location center, double radius, String customSound, SoundCategory category, float volume, float pitch, int intervalTicks) {
        if (center == null || center.getWorld() == null) {
            return null;
        }

        ZoneInstanceImpl zone = new ZoneInstanceImpl(
                UUID.randomUUID(),
                center,
                radius,
                null,
                customSound,
                category,
                volume,
                pitch,
                intervalTicks,
                this
        );

        activeZones.put(zone.getId(), zone);
        zone.activate();

        return zone;
    }

    @Override
    public void removeSoundZone(UUID zoneId) {
        Optional.ofNullable(activeZones.get(zoneId)).ifPresent(zone -> {
            zone.deactivate();
            activeZones.remove(zoneId);
        });
    }

    @Override
    public void stopAllSounds(Player player) {
        if (player == null || !player.isOnline()) return;

        for (SoundCategory category : SoundCategory.values()) {
            player.stopSound(category);
        }

        getSoundsByPlayer(player).forEach(SoundInstanceImpl::stop);
    }

    @Override
    public void stopAllSounds() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            stopAllSounds(player);
        }

        activeSounds.values().forEach(SoundInstanceImpl::stop);
    }

    @Override
    public void setGlobalVolume(float volume) {
        this.globalVolume = Math.max(0, Math.min(1, volume));
    }

    @Override
    public float getGlobalVolume() {
        return globalVolume;
    }

    @Override
    public void setPlayerVolume(Player player, float volume) {
        if (player == null) return;
        playerVolumes.put(player.getUniqueId(), Math.max(0, Math.min(1, volume)));
    }

    @Override
    public float getPlayerVolume(Player player) {
        if (player == null) return 0;
        return playerVolumes.getOrDefault(player.getUniqueId(), 1.0f);
    }

    @Override
    public Optional<SoundInstance> getSoundInstance(UUID soundId) {
        return Optional.ofNullable(activeSounds.get(soundId));
    }

    @Override
    public Optional<ZoneInstance> getZoneInstance(UUID zoneId) {
        return Optional.ofNullable(activeZones.get(zoneId));
    }

    @Override
    public void dispose() {
        stopAllSounds();
        activeZones.values().forEach(ZoneInstanceImpl::deactivate);
        activeSounds.clear();
        activeZones.clear();

        if (processorTask != null && !processorTask.isCancelled()) {
            processorTask.cancel();
        }
    }

    private void stopPlayerMusicSounds(Player player) {
        getSoundsByPlayer(player)
                .stream()
                .filter(sound -> sound.getCategory() == SoundCategory.MUSIC)
                .forEach(SoundInstanceImpl::stop);
    }

    float calculateVolume(float baseVolume) {
        return baseVolume * globalVolume;
    }

    float calculatePlayerVolume(Player player, float baseVolume) {
        if (player == null) return 0;
        return baseVolume * globalVolume * playerVolumes.getOrDefault(player.getUniqueId(), 1.0f);
    }

    Set<SoundInstanceImpl> getAllSounds() {
        return Set.copyOf(activeSounds.values());
    }

    Set<ZoneInstanceImpl> getAllZones() {
        return Set.copyOf(activeZones.values());
    }

    JavaPlugin getPlugin() {
        return plugin;
    }

    void removeSoundInstance(UUID soundId) {
        activeSounds.remove(soundId);
    }

    Set<SoundInstanceImpl> getSoundsByPlayer(Player player) {
        return activeSounds.values().stream()
                .filter(sound -> sound.getPlayer().isPresent() && sound.getPlayer().get().equals(player))
                .collect(java.util.stream.Collectors.toSet());
    }

    Set<SoundInstanceImpl> getSoundsByPredicate(Predicate<SoundInstanceImpl> predicate) {
        return activeSounds.values().stream()
                .filter(predicate)
                .collect(java.util.stream.Collectors.toSet());
    }
}