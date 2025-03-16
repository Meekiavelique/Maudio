package com.meekdev.maudio.internal;

import com.meekdev.maudio.Maudio;
import com.meekdev.maudio.SoundInstance;
import com.meekdev.maudio.ZoneInstance;
import com.meekdev.maudio.api.SoundLookup;
import com.meekdev.maudio.api.effects.AudioEffect;
import com.meekdev.maudio.api.effects.AudioSequence;
import com.meekdev.maudio.api.events.AudioEvent;
import com.meekdev.maudio.internal.events.EventBus;
import com.meekdev.maudio.internal.events.PlayerAudioEventImpl;
import com.meekdev.maudio.internal.events.detector.BlockStepDetector;
import com.meekdev.maudio.internal.events.detector.ItemUseDetector;
import com.meekdev.maudio.internal.model.SoundInstanceImpl;
import com.meekdev.maudio.internal.model.ZoneInstanceImpl;
import com.meekdev.maudio.internal.processor.AudioProcessor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.ArrayList;
import java.util.List;

public class AudioManager implements Maudio {
    private final JavaPlugin plugin;
    private final AudioProcessor processor;
    private final SoundPool soundPool;
    private final SpatialManager spatialManager;
    private final EventBus eventBus;
    private final Map<String, Listener> detectors = new HashMap<>();

    private final Map<UUID, SoundInstanceImpl> activeSounds = new ConcurrentHashMap<>();
    private final Map<UUID, ZoneInstanceImpl> activeZones = new ConcurrentHashMap<>();
    private final Map<UUID, Float> playerVolumes = new ConcurrentHashMap<>();
    private final Map<UUID, Map<UUID, Long>> sequenceTimers = new ConcurrentHashMap<>();

    private float globalVolume = 1.0f;
    private BukkitTask processorTask;
    private boolean initialized = false;

    public AudioManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.soundPool = new SoundPool(128, 64);
        this.spatialManager = new SpatialManager(16, 500);
        this.processor = new AudioProcessor(this);
        this.eventBus = new EventBus(this, plugin);
        init();
    }

    private void init() {
        if (initialized) return;

        initProcessor();
        registerDetectors();
        initialized = true;
    }

    private void initProcessor() {
        processorTask = plugin.getServer().getScheduler().runTaskTimer(
                plugin,
                processor,
                1L,
                1L
        );
    }

    private void registerDetectors() {
        BlockStepDetector blockDetector = new BlockStepDetector(this, plugin);
        ItemUseDetector itemDetector = new ItemUseDetector(this, plugin);

        plugin.getServer().getPluginManager().registerEvents(blockDetector, plugin);
        plugin.getServer().getPluginManager().registerEvents(itemDetector, plugin);

        detectors.put("blockStep", blockDetector);
        detectors.put("itemUse", itemDetector);
    }

    public void registerEvents(Object listener) {
        eventBus.registerListeners(listener);
    }

    public void unregisterEvents(Object listener) {
        eventBus.unregisterListeners(listener);
    }

    public boolean triggerEvent(AudioEvent event) {
        return eventBus.fireEvent(event);
    }

    public void playEffect(AudioEffect effect) {
        if (effect == null) return;

        Sound sound = effect.getSound();
        String customSound = effect.getCustomSound();

        if (sound == null && (customSound == null || customSound.isEmpty())) {
            return;
        }

        Set<UUID> targetPlayerIds = effect.getTargetPlayerIds();
        boolean global = effect.isGlobal();
        Location location = effect.getLocation();
        float volume = effect.getVolume();
        float pitch = effect.getPitch();
        SoundCategory category = effect.getCategory();

        if (effect.getFadeIn() > 0) {
            if (!targetPlayerIds.isEmpty()) {
                for (UUID playerId : targetPlayerIds) {
                    Player player = plugin.getServer().getPlayer(playerId);
                    if (player == null || !player.isOnline()) continue;

                    if (sound != null) {
                        playMusic(player, sound, volume, pitch, effect.getFadeIn());
                    } else if (customSound != null) {
                        playMusic(player, customSound, volume, pitch, effect.getFadeIn());
                    }
                }
            } else if (global && location != null) {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (sound != null) {
                        playMusic(player, sound, volume, pitch, effect.getFadeIn());
                    } else if (customSound != null) {
                        playMusic(player, customSound, volume, pitch, effect.getFadeIn());
                    }
                }
            }
        } else if (effect.isLooping()) {
            if (!targetPlayerIds.isEmpty()) {
                for (UUID playerId : targetPlayerIds) {
                    Player player = plugin.getServer().getPlayer(playerId);
                    if (player == null || !player.isOnline()) continue;

                    if (sound != null) {
                        playLoopingSound(player, sound, category, volume, pitch, effect.getLoopInterval());
                    }
                }
            } else if (location != null) {
                if (sound != null) {
                    playLoopingSound(location, sound, category, volume, pitch, effect.getLoopInterval());
                }
            }
        } else {
            if (!targetPlayerIds.isEmpty()) {
                for (UUID playerId : targetPlayerIds) {
                    Player player = plugin.getServer().getPlayer(playerId);
                    if (player == null || !player.isOnline()) continue;

                    if (sound != null) {
                        playSoundToPlayer(player, sound, category, volume, pitch);
                    } else if (customSound != null) {
                        playSoundToPlayer(player, customSound, category, volume, pitch);
                    }
                }
            } else if (global) {
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (sound != null) {
                        playSoundToPlayer(player, sound, category, volume, pitch);
                    } else if (customSound != null) {
                        playSoundToPlayer(player, customSound, category, volume, pitch);
                    }
                }
            } else if (location != null) {
                if (sound != null) {
                    playSound(location, sound, category, volume, pitch);
                } else if (customSound != null) {
                    playSound(location, customSound, category, volume, pitch);
                }
            }
        }
    }

    public void playSequence(AudioSequence sequence) {
        if (sequence == null) return;

        List<AudioEffect> effects = sequence.getEffects();
        if (effects.isEmpty()) return;

        if (sequence.isConcurrent()) {
            for (AudioEffect effect : effects) {
                playEffect(effect);
            }
        } else {
            playSequentialEffects(sequence, effects, 0);
        }
    }

    private void playSequentialEffects(AudioSequence sequence, List<AudioEffect> effects, int index) {
        if (index >= effects.size()) return;

        AudioEffect effect = effects.get(index);
        playEffect(effect);

        float delay = calculateEffectDuration(effect);

        plugin.getServer().getScheduler().runTaskLater(
                plugin,
                () -> playSequentialEffects(sequence, effects, index + 1),
                Math.max(1, (long)(delay * 20))
        );
    }

    private float calculateEffectDuration(AudioEffect effect) {
        float baseDuration = 0.0f;

        if (effect.getDuration() > 0) {
            baseDuration = effect.getDuration();
        } else {
            baseDuration = 1.0f;
        }

        return baseDuration + effect.getFadeIn() + effect.getFadeOut();
    }

    public AudioEffect createBlockStepEffect(Player player, Block block) {
        AudioEvent event = new PlayerAudioEventImpl(
                "block_step",
                player,
                false,
                null,
                null,
                SoundCategory.BLOCKS,
                1.0f,
                1.0f,
                block.getLocation(),
                System.currentTimeMillis(),
                AudioEvent.Priority.NORMAL
        );

        if (triggerEvent(event)) {
            return null;
        }

        return null;
    }

    public AudioEffect createItemUseEffect(Player player, ItemStack item, Action action) {
        String eventName = "item_use";
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            eventName = "item_left_click";
        } else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            eventName = "item_right_click";
        }

        AudioEvent event = new PlayerAudioEventImpl(
                eventName,
                player,
                false,
                null,
                null,
                SoundCategory.PLAYERS,
                1.0f,
                1.0f,
                player.getLocation(),
                System.currentTimeMillis(),
                AudioEvent.Priority.NORMAL
        );

        if (triggerEvent(event)) {
            return null;
        }

        return null;
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

    public void playSound(Location location, SoundLookup soundLookup) {
        if (soundLookup == null) return;
        playSound(location, soundLookup.getSound(), soundLookup.getCategory(),
                soundLookup.getVolume(), soundLookup.getPitch());
    }

    public void playSoundToPlayer(Player player, SoundLookup soundLookup) {
        if (soundLookup == null || player == null) return;
        playSoundToPlayer(player, soundLookup.getSound(), soundLookup.getCategory(),
                soundLookup.getVolume(), soundLookup.getPitch());
    }

    public SoundInstance playMusic(Player player, SoundLookup soundLookup) {
        if (soundLookup == null || player == null) return null;
        return playMusic(player, soundLookup.getSound(), soundLookup.getVolume(),
                soundLookup.getPitch(), soundLookup.getFadeIn());
    }

    public SoundInstance playLoopingSound(Location location, SoundLookup soundLookup) {
        if (soundLookup == null || location == null) return null;
        return playLoopingSound(location, soundLookup.getSound(), soundLookup.getCategory(),
                soundLookup.getVolume(), soundLookup.getPitch(),
                soundLookup.isLooping() ? soundLookup.getLoopInterval() : 20);
    }

    public SoundInstance playLoopingSound(Player player, SoundLookup soundLookup) {
        if (soundLookup == null || player == null) return null;
        return playLoopingSound(player, soundLookup.getSound(), soundLookup.getCategory(),
                soundLookup.getVolume(), soundLookup.getPitch(),
                soundLookup.isLooping() ? soundLookup.getLoopInterval() : 20);
    }

    public ZoneInstance createSoundZone(Location center, double radius, SoundLookup soundLookup) {
        if (soundLookup == null || center == null) return null;
        return createSoundZone(center, radius, soundLookup.getSound(), soundLookup.getCategory(),
                soundLookup.getVolume(), soundLookup.getPitch(),
                soundLookup.isLooping() ? soundLookup.getLoopInterval() : 40);
    }

    @Override
    public SoundInstance playMusic(Player player, Sound music, float volume, float pitch, float fadeInSeconds) {
        if (player == null || !player.isOnline()) {
            return null;
        }

        stopPlayerMusicSounds(player);

        SoundInstanceImpl instance = soundPool.obtain(music, null, SoundCategory.MUSIC,
                volume, pitch, player, null, false, 0);
        if (instance == null) return null;

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

        SoundInstanceImpl instance = soundPool.obtain(null, customMusic, SoundCategory.MUSIC,
                volume, pitch, player, null, false, 0);
        if (instance == null) return null;

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

        SoundInstanceImpl instance = soundPool.obtain(sound, null, category,
                volume, pitch, null, location, true, intervalTicks);
        if (instance == null) return null;

        activeSounds.put(instance.getId(), instance);
        instance.play();

        return instance;
    }

    @Override
    public SoundInstance playLoopingSound(Player player, Sound sound, SoundCategory category, float volume, float pitch, int intervalTicks) {
        if (player == null || !player.isOnline()) {
            return null;
        }

        SoundInstanceImpl instance = soundPool.obtain(sound, null, category,
                volume, pitch, player, null, true, intervalTicks);
        if (instance == null) return null;

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
        spatialManager.addZone(zone);
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
        spatialManager.addZone(zone);
        zone.activate();

        return zone;
    }

    @Override
    public void removeSoundZone(UUID zoneId) {
        ZoneInstanceImpl zone = activeZones.get(zoneId);
        if (zone != null) {
            zone.deactivate();
            spatialManager.removeZone(zone);
            activeZones.remove(zoneId);
        }
    }

    @Override
    public void stopAllSounds(Player player) {
        if (player == null || !player.isOnline()) return;

        for (SoundCategory category : SoundCategory.values()) {
            player.stopSound(String.valueOf(category));
        }

        getSoundsByPlayer(player).forEach(sound -> {
            sound.stop();
            soundPool.release(sound);
        });
    }

    @Override
    public void stopAllSounds() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            stopAllSounds(player);
        }

        activeSounds.values().forEach(sound -> {
            sound.stop();
            soundPool.release(sound);
        });

        activeSounds.clear();
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
        spatialManager.clear();

        HandlerList.unregisterAll(plugin);

        if (processorTask != null && !processorTask.isCancelled()) {
            processorTask.cancel();
        }
    }

    void stopPlayerMusicSounds(Player player) {
        getSoundsByPlayer(player)
                .stream()
                .filter(sound -> sound.getCategory() == SoundCategory.MUSIC)
                .forEach(sound -> {
                    sound.stop();
                    soundPool.release(sound);
                });
    }

    public float calculateVolume(float baseVolume) {
        return baseVolume * globalVolume;
    }

    public float calculatePlayerVolume(Player player, float baseVolume) {
        if (player == null) return 0;
        return baseVolume * globalVolume * playerVolumes.getOrDefault(player.getUniqueId(), 1.0f);
    }

    public Set<SoundInstanceImpl> getAllSounds() {
        return Set.copyOf(activeSounds.values());
    }

    public Set<ZoneInstanceImpl> getAllZones() {
        return Set.copyOf(activeZones.values());
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public void removeSoundInstance(UUID soundId) {
        SoundInstanceImpl instance = activeSounds.remove(soundId);
        if (instance != null) {
            soundPool.release(instance);
        }
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

    public void handlePlayerQuit(Player player) {
        if (player == null) return;

        stopAllSounds(player);
        playerVolumes.remove(player.getUniqueId());
        spatialManager.clearPlayerCache(player.getUniqueId());
    }

    public void handleWorldChange(Player player, String fromWorld, String toWorld) {
        if (player == null || !player.isOnline()) return;

        getSoundsByPlayer(player)
                .stream()
                .filter(SoundInstanceImpl::isWorldSpecific)
                .forEach(sound -> {
                    sound.stop();
                    soundPool.release(sound);
                    removeSoundInstance(sound.getId());
                });
    }

    public SpatialManager getSpatialManager() {
        return spatialManager;
    }

    SoundPool getSoundPool() {
        return soundPool;
    }
}