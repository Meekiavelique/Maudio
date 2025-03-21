package com.meekdev.maudio.internal.model;

import com.meekdev.maudio.SoundInstance;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class SoundInstanceImpl implements SoundInstance {
    public enum State {
        CREATED,
        PLAYING,
        PAUSED,
        STOPPING,
        STOPPED,
        FADING_IN,
        FADING_OUT
    }

    private UUID id;
    private Sound sound;
    private String customSound;
    private SoundCategory category;
    private float volume;
    private float pitch;
    private Player player;
    private Location location;
    private boolean looping;
    private int intervalTicks;
    private long lastPlayTime;
    private int currentTick;
    private State state = State.CREATED;
    private UUID worldId;

    private float targetVolume;
    private float startVolume;
    private float fadeTime;
    private long fadeStartTime;

    public SoundInstanceImpl(UUID id, Sound sound, String customSound, SoundCategory category,
                             float volume, float pitch, Player player) {
        this(id, sound, customSound, category, volume, pitch, player, null, false, 0);
    }

    public SoundInstanceImpl(UUID id, Sound sound, String customSound, SoundCategory category,
                             float volume, float pitch, Player player, Location location,
                             boolean looping, int intervalTicks) {
        reset(id, sound, customSound, category, volume, pitch, player, location, looping, intervalTicks);
    }

    public void reset(UUID id, Sound sound, String customSound, SoundCategory category,
                      float volume, float pitch, Player player, Location location,
                      boolean looping, int intervalTicks) {
        this.id = id;
        this.sound = sound;
        this.customSound = customSound;
        this.category = category;
        this.volume = volume;
        this.targetVolume = volume;
        this.pitch = pitch;
        this.player = player;
        this.location = location;
        this.looping = looping;
        this.intervalTicks = Math.max(1, intervalTicks);
        this.lastPlayTime = System.currentTimeMillis();
        this.currentTick = 0;
        this.state = State.CREATED;

        if (location != null && location.getWorld() != null) {
            this.worldId = location.getWorld().getUID();
        } else if (player != null && player.getWorld() != null) {
            this.worldId = player.getWorld().getUID();
        } else {
            this.worldId = null;
        }
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Sound getSound() {
        return sound;
    }

    @Override
    public Optional<String> getCustomSound() {
        return Optional.ofNullable(customSound);
    }

    @Override
    public SoundCategory getCategory() {
        return category;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    @Override
    public boolean isPlaying() {
        return state == State.PLAYING || state == State.FADING_IN || state == State.FADING_OUT;
    }

    @Override
    public boolean isLooping() {
        return looping;
    }

    @Override
    public boolean isFading() {
        return state == State.FADING_IN || state == State.FADING_OUT;
    }

    @Override
    public boolean isFadingIn() {
        return state == State.FADING_IN;
    }

    @Override
    public boolean isFadingOut() {
        return state == State.FADING_OUT;
    }

    @Override
    public SoundInstance setVolume(float volume) {
        this.volume = volume;
        this.targetVolume = volume;
        return this;
    }

    @Override
    public SoundInstance setPitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    @Override
    public SoundInstance setLocation(Location location) {
        this.location = location;
        if (location != null && location.getWorld() != null) {
            this.worldId = location.getWorld().getUID();
        }
        return this;
    }

    @Override
    public SoundInstance play() {
        if (state == State.STOPPED) return this;

        if (player != null && player.isOnline()) {
            if (sound != null) {
                player.playSound(player.getLocation(), sound, category, volume, pitch);
            } else if (customSound != null) {
                player.playSound(player.getLocation(), customSound, category, volume, pitch);
            }
        } else if (location != null && location.getWorld() != null) {
            if (sound != null) {
                location.getWorld().playSound(location, sound, category, volume, pitch);
            } else if (customSound != null) {
                location.getWorld().playSound(location, customSound, category, volume, pitch);
            }
        }

        setState(State.PLAYING);
        lastPlayTime = System.currentTimeMillis();
        return this;
    }

    @Override
    public SoundInstance pause() {
        setState(State.PAUSED);
        return this;
    }

    @Override
    public SoundInstance stop() {
        if (state == State.STOPPED) return this;

        if (player != null && player.isOnline()) {
            if (sound != null) {
                player.stopSound(sound, category);
            } else if (customSound != null) {
                player.stopSound(customSound, category);
            }
        }

        setState(State.STOPPED);
        return this;
    }

    @Override
    public SoundInstance fadeIn(float seconds) {
        if (seconds <= 0 || state == State.STOPPED) return this;

        this.startVolume = 0.01f;
        this.fadeTime = seconds;
        this.fadeStartTime = System.currentTimeMillis();
        this.setState(State.FADING_IN);

        if (player != null && player.isOnline()) {
            if (sound != null) {
                player.playSound(player.getLocation(), sound, category, startVolume, pitch);
            } else if (customSound != null) {
                player.playSound(player.getLocation(), customSound, category, startVolume, pitch);
            }
        } else if (location != null && location.getWorld() != null) {
            if (sound != null) {
                location.getWorld().playSound(location, sound, category, startVolume, pitch);
            } else if (customSound != null) {
                location.getWorld().playSound(location, customSound, category, startVolume, pitch);
            }
        }

        return this;
    }

    @Override
    public SoundInstance fadeOut(float seconds) {
        if (seconds <= 0 || state == State.STOPPED) {
            stop();
            return this;
        }

        this.startVolume = this.volume;
        this.fadeTime = seconds;
        this.fadeStartTime = System.currentTimeMillis();
        this.setState(State.FADING_OUT);

        return this;
    }

    @Override
    public Optional<Player> getPlayer() {
        return Optional.ofNullable(player);
    }

    @Override
    public Optional<Location> getLocation() {
        return Optional.ofNullable(location);
    }

    public void setState(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

    public int getIntervalTicks() {
        return intervalTicks;
    }

    public void setIntervalTicks(int intervalTicks) {
        this.intervalTicks = Math.max(1, intervalTicks);
    }

    public void incrementTick() {
        if (!looping || state != State.PLAYING) return;

        currentTick++;
        if (currentTick >= intervalTicks) {
            currentTick = 0;
            play();
        }
    }

    public float updateFading() {
        if (state != State.FADING_IN && state != State.FADING_OUT) {
            return volume;
        }

        float elapsed = (System.currentTimeMillis() - fadeStartTime) / 1000f;
        float progress = Math.min(1.0f, elapsed / fadeTime);

        if (state == State.FADING_IN) {
            volume = startVolume + (targetVolume - startVolume) * progress;
            if (progress >= 1.0f) {
                volume = targetVolume;
                setState(State.PLAYING);
            }
        } else {
            volume = startVolume * (1.0f - progress);
            if (progress >= 1.0f) {
                stop();
            }
        }

        return volume;
    }

    public boolean isWorldSpecific() {
        return worldId != null;
    }

    public UUID getWorldId() {
        return worldId;
    }

    public boolean isInSameWorld(Player p) {
        return p != null && p.getWorld() != null &&
                worldId != null && p.getWorld().getUID().equals(worldId);
    }
}