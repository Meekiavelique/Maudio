# MAudio

[![ko-fi](https://ko-fi.com/img/githubbutton_sm.svg)](https://ko-fi.com/T6T1X1T51)

**MAudio** is a audio management API for Minecraft plugins, providing advanced sound capabilities beyond what the base Bukkit API offers. This library enables dynamic audio experiences in your Minecraft server with features like sound zones, music management, looping sounds, and event-driven audio.
(For plugins developpers, not for server owners)
## Features

- **Location-based Sound Systems**: Play sounds at specific locations
- **Player-targeted Audio**: Send sounds directly to specific players
- **Music Management**: Seamlessly play and control background music with fade effects (Dosen't work due to changing volume of sound restart it)
- **Looping Sounds**: Create ambient and repeating audio experiences
- **Sound Zones**: Define areas that play sounds to players who enter them
- **Volume Control**: Manage global and per-player volume settings
- **Audio Effects**: Create complex sound effects with fading and sequencing
- **Event System**: Respond to audio events with a custom event bus

## Installation

### Maven

Add the repository and dependency to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>repsy</id>
        <name>MeekDev Maven Repository</name>
        <url>https://repo.repsy.io/mvn/meek/maudio</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.meekdev</groupId>
        <artifactId>maudio</artifactId>
        <version>1.0.0</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

### Gradle

Add the repository and dependency to your `build.gradle`:

```groovy
repositories {
    maven {
        url 'https://repo.repsy.io/mvn/meek/maudio'
    }
}

dependencies {
    implementation 'com.meekdev:maudio:1.0.0'
}
```

## Getting Started

### Initialization

Initialize the MAudio API in your plugin's `onEnable()` method:

```java
import com.meekdev.maudio.MaudioLib;
import com.meekdev.maudio.Maudio;

public class YourPlugin extends JavaPlugin {
    private Maudio audioManager;

    @Override
    public void onEnable() {
        // Initialize MAudio
        this.audioManager = MaudioLib.init(this);
        
        // Your plugin initialization code...
    }
    
    @Override
    public void onDisable() {
        // Properly clean up audio resources
        MaudioLib.shutdown();
    }
}
```

### Basic Usage Examples

#### Playing Simple Sounds

```java
// Play a sound at a location
audioManager.playSound(location, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 1.0f, 1.0f);

// Play a sound to a specific player
audioManager.playSoundToPlayer(player, Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.PLAYERS, 1.0f, 1.0f);

// Play a custom sound (resource pack sound)
audioManager.playSound(location, "mycustom.sound.effect", SoundCategory.AMBIENT, 1.0f, 1.0f);
```

#### Music Management

```java
// Play music with a 3-second fade-in
SoundInstance music = audioManager.playMusic(player, Sound.MUSIC_DISC_CAT, 0.5f, 1.0f, 3.0f);


// Later, stop the music with a 2-second fade-out
audioManager.stopMusic(player, 2.0f);
```
(Dosen't work due to changing volume of sound restart it)

#### Looping Sounds

```java
// Create a looping sound that repeats every 40 ticks (2 seconds)
SoundInstance ambience = audioManager.playLoopingSound(
    location, 
    Sound.AMBIENT_CAVE, 
    SoundCategory.AMBIENT, 
    0.3f, 
    1.0f, 
    40
);

// Stop the looping sound when needed
audioManager.stopLoopingSound(ambience.getId());
```

#### Sound Zones

```java
// Create a sound zone with 20-block radius that plays a sound every 100 ticks
ZoneInstance zone = audioManager.createSoundZone(
    centerLocation,  // The center point
    20.0,           // Radius in blocks
    Sound.AMBIENT_UNDERWATER_LOOP,
    SoundCategory.AMBIENT,
    0.5f,           // Volume
    1.0f,           // Pitch
    100             // Interval in ticks (5 seconds)
);

// Remove the zone when no longer needed
audioManager.removeSoundZone(zone.getId());
```

#### Volume Control

```java
// Set global volume (affects all sounds)
audioManager.setGlobalVolume(0.7f);

// Set volume for a specific player
audioManager.setPlayerVolume(player, 0.5f);

// Get a player's volume setting
float playerVolume = audioManager.getPlayerVolume(player);
```

### Advanced Features

#### Sound Lookup

Create predefined sound profiles:

```java
public class MySounds implements SoundLookup {
    
    @Override
    public Sound getSound() {
        return Sound.ENTITY_GHAST_SCREAM;
    }
    
    @Override
    public SoundCategory getCategory() {
        return SoundCategory.HOSTILE;
    }
    
    @Override
    public float getVolume() {
        return 0.8f;
    }
    
    @Override
    public float getPitch() {
        return 0.6f;
    }
}

// Then use it:
audioManager.playSound(location, new MySounds());
```

#### Audio Effects

Creating complex audio effects:

```java
// Create a thunder effect with fade
AudioEffect thunderEffect = new AudioEffect.Builder()
    .sound(Sound.ENTITY_LIGHTNING_BOLT_THUNDER)
    .category(SoundCategory.WEATHER)
    .volume(1.0f)
    .pitch(0.8f)
    .global()  // Heard by all players
    .fadeIn(0.5f) // (Dosen't work due to changing volume of sound restart it)
    .build();

// Play the effect
audioManager.playEffect(thunderEffect);
```


#### Audio Sequences

Chain multiple effects together:

```java
// Create a sequence of effects
AudioSequence sequence = new AudioSequence.Builder()
    .addEffect(thunderEffect)
    .then(rainEffect)
    .then(windEffect)
    .concurrent(false)  // Play one after another
    .build();

// Play the sequence
audioManager.playSequence(sequence);
```

#### Event System

Listen for audio events:

```java
public class MyAudioListener {
    
    @AudioListener({"block_step", "item_use"})
    public void onAudioEvent(AudioEvent event) {
        // Handle the audio event
        if (event.getName().equals("block_step")) {
            // Do something when a player steps on a block
        }
    }
}

// Register the listener
audioManager.registerEvents(new MyAudioListener());
```

### Working with Sound and Zone Instances

Both `SoundInstance` and `ZoneInstance` provide methods to modify properties after creation:

```java
// Get a sound instance by ID
Optional<SoundInstance> soundOpt = audioManager.getSoundInstance(soundId);
soundOpt.ifPresent(sound -> {
    sound.setVolume(0.8f);
    sound.setPitch(1.2f);
    
    if (sound.isPaused()) {
        sound.play();
    }
});

// Get a zone instance by ID
Optional<ZoneInstance> zoneOpt = audioManager.getZoneInstance(zoneId);
zoneOpt.ifPresent(zone -> {
    zone.setRadius(25.0);
    zone.setVolume(0.6f);
    
    // Check if a specific player is in the zone
    if (zone.isPlayerInZone(player)) {
        // Player-specific logic
    }
});
```


Always call `MaudioLib.shutdown()` in your plugin's `onDisable()` method btw.


## License

[Need to put one]

## Support

For issues, feature requests, or questions, please [open an issue](https://github.com/yourusername/maudio/issues) on GitHub.
