package lol.sillyapps.WCTDR;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import java.util.*;

public class AudioEngine {
    private final ObjectMap<String, ManagedMusic> musicInstances = new ObjectMap<>();
    private final ObjectMap<String, Sound> soundInstances = new ObjectMap<>();
    private final Array<VolumeAnimation> volumeAnimations = new Array<>();
    private final Array<PitchAnimation> pitchAnimations = new Array<>();
    private final Array<Runnable> pendingTasks = new Array<>();

    public static void Debug(String message) {
        System.out.println("[AUDIO] " + message);
    }

    public String loadMusic(String filePath) {
        String uuid = UUID.randomUUID().toString();
        Music music = Gdx.audio.newMusic(Gdx.files.internal(filePath));
        musicInstances.put(uuid, new ManagedMusic(music));
        Debug("Loaded: " + uuid);
        return uuid;
    }

    public void playMusic(String uuid) {
        playMusic(uuid, false);
    }

    public void playMusic(String uuid, boolean loop) {
        ManagedMusic mm = musicInstances.get(uuid);
        if (mm != null) {
            mm.music.setLooping(loop);
            mm.isLooping = loop;
            mm.music.play();
            mm.isPlaying = true;
            Debug("Started: " + uuid + " (loop: " + loop + ")");
        }
    }

    public void stopMusic(String uuid) {
        ManagedMusic mm = musicInstances.get(uuid);
        if (mm != null) {
            mm.music.stop();
            mm.isPlaying = false;
            Debug("Stopped: " + uuid);
        }
    }

    public void pauseMusic(String uuid) {
        ManagedMusic mm = musicInstances.get(uuid);
        if (mm != null) {
            mm.music.pause();
            mm.isPlaying = false;
            Debug("Paused: " + uuid);
        }
    }

    public void resumeMusic(String uuid) {
        ManagedMusic mm = musicInstances.get(uuid);
        if (mm != null && !mm.isPlaying) {
            mm.music.play();
            mm.isPlaying = true;
            Debug("Resumed: " + uuid);
        }
    }

    public void setVolume(String uuid, float volume) {
        ManagedMusic mm = musicInstances.get(uuid);
        if (mm != null) {
            mm.music.setVolume(volume);
            mm.currentVolume = volume;
            //Debug("Volume for " + uuid + " is " + volume);
        }
    }

    public float getVolume(String uuid) {
        ManagedMusic mm = musicInstances.get(uuid);
        return mm != null ? mm.currentVolume : 0;
    }

    // Установка режима повтора без остановки/запуска
    public void setLooping(String uuid, boolean loop) {
        ManagedMusic mm = musicInstances.get(uuid);
        if (mm != null) {
            mm.music.setLooping(loop);
            mm.isLooping = loop;
            Debug("Looping for " + uuid + " is " + loop);
        }
    }

    public boolean isLooping(String uuid) {
        ManagedMusic mm = musicInstances.get(uuid);
        return mm != null ? mm.isLooping : false;
    }

    public void fadeIn(String uuid, float duration, Runnable onComplete) {
        ManagedMusic mm = musicInstances.get(uuid);
        if (mm != null) {
            float startVolume = mm.currentVolume;
            VolumeAnimation anim = new VolumeAnimation(uuid, startVolume, 1.0f, duration, onComplete);
            volumeAnimations.add(anim);
            Debug("FadeIn started for " + uuid + ", duration: " + duration);
        }
    }

    public void fadeOut(String uuid, float duration, Runnable onComplete) {
        ManagedMusic mm = musicInstances.get(uuid);
        if (mm != null) {
            float startVolume = mm.currentVolume;
            VolumeAnimation anim = new VolumeAnimation(uuid, startVolume, 0.0f, duration, onComplete);
            volumeAnimations.add(anim);
            Debug("FadeOut started for " + uuid + ", duration: " + duration);
        }
    }

    /*
    public void changePitch(String uuid, float startPitch, float endPitch, float duration, Runnable onComplete) {
        ManagedMusic mm = musicInstances.get(uuid);
        if (mm != null) {
            PitchAnimation anim = new PitchAnimation(uuid, startPitch, endPitch, duration, onComplete);
            pitchAnimations.add(anim);
            Debug("Изменение тона начато для " + uuid +
                ", от " + startPitch + " до " + endPitch);
        }
    }
    */

    public String loadSound(String filePath) {
        String uuid = UUID.randomUUID().toString();
        Sound sound = Gdx.audio.newSound(Gdx.files.internal(filePath));
        soundInstances.put(uuid, sound);
        Debug("Loaded sound: " + uuid);
        return uuid;
    }

    public void playSound(String uuid, float volume) {
        Sound sound = soundInstances.get(uuid);
        if (sound != null) {
            sound.play(volume);
            Debug("Played sound: " + uuid);
        }
    }

    public void update(float deltaTime) {
        updateVolumeAnimations(deltaTime);
        updatePitchAnimations(deltaTime);

        for (Runnable task : pendingTasks) {
            task.run();
        }
        pendingTasks.clear();
    }

    private void updateVolumeAnimations(float deltaTime) {
        Array<VolumeAnimation> finished = new Array<>();

        for (VolumeAnimation anim : volumeAnimations) {
            anim.elapsed += deltaTime;
            float progress = Math.min(anim.elapsed / anim.duration, 1.0f);

            float eased = (float) (progress < 0.5 ?
                2 * Math.pow(progress, 2) :
                1 - 2 * Math.pow(1 - progress, 2));

            float currentVolume = anim.start + (anim.end - anim.start) * eased;
            setVolume(anim.uuid, currentVolume);

            if (progress >= 1.0) {
                finished.add(anim);
                if (anim.onComplete != null) {
                    pendingTasks.add(anim.onComplete);
                }
            }
        }

        volumeAnimations.removeAll(finished, true);
    }

    private void updatePitchAnimations(float deltaTime) {
        Array<PitchAnimation> finished = new Array<>();

        for (PitchAnimation anim : pitchAnimations) {
            anim.elapsed += deltaTime;
            float progress = Math.min(anim.elapsed / anim.duration, 1.0f);

            float currentPitch = anim.start + (anim.end - anim.start) * progress;
            try {
                //musicInstances.get(anim.uuid).music.setPitch(currentPitch);
            } catch (Exception e) {
                Debug("Error pitch change for " + anim.uuid + ": " + e.getMessage());
            }

            if (progress >= 1.0) {
                finished.add(anim);
                if (anim.onComplete != null) {
                    pendingTasks.add(anim.onComplete);
                }
            }
        }

        pitchAnimations.removeAll(finished, true);
    }

    public void dispose() {
        for (ManagedMusic mm : musicInstances.values()) {
            mm.music.dispose();
        }
        musicInstances.clear();

        for (Sound sound : soundInstances.values()) {
            sound.dispose();
        }
        soundInstances.clear();

        volumeAnimations.clear();
        pitchAnimations.clear();

        Debug("Cleared");
    }

    private static class ManagedMusic {
        Music music;
        float currentVolume;
        boolean isPlaying;
        boolean isLooping;

        ManagedMusic(Music music) {
            this.music = music;
            this.currentVolume = 1.0f;
            this.isPlaying = false;
            this.isLooping = false;
        }
    }

    private static class VolumeAnimation {
        String uuid;
        float start;
        float end;
        float duration;
        float elapsed;
        Runnable onComplete;

        VolumeAnimation(String uuid, float start, float end, float duration, Runnable onComplete) {
            this.uuid = uuid;
            this.start = start;
            this.end = end;
            this.duration = duration;
            this.elapsed = 0;
            this.onComplete = onComplete;
        }
    }

    private static class PitchAnimation {
        String uuid;
        float start;
        float end;
        float duration;
        float elapsed;
        Runnable onComplete;

        PitchAnimation(String uuid, float start, float end, float duration, Runnable onComplete) {
            this.uuid = uuid;
            this.start = start;
            this.end = end;
            this.duration = duration;
            this.elapsed = 0;
            this.onComplete = onComplete;
        }
    }
}
