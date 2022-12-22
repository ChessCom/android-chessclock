package com.chess.clock.activities;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.chess.clock.R;

import java.util.HashSet;

enum ClockSound {
    PLAYER_ONE_MOVE, PLAYER_TWO_MOVE, GAME_FINISHED, RESET_CLOCK, MENU_ACTION
}

interface ClockSoundManager {
    void init(Context context);

    void releaseSounds();

    void setSoundsEnabled(boolean enabled);

    boolean areSoundsEnabled();

    void playSound(ClockSound sound, AudioManager manager);

    void toggleSound();
}

class ClockSoundManagerImpl implements ClockSoundManager {
    private static final int NO_LOOP = 0;
    private static final int NO_SOUND_ID = 0;
    private static final float PLAYBACK_RATE = 1f;
    private static final int SOUND_PRIORITY = 1;

    public boolean soundsEnabled = true;

    private SoundPool soundPool;
    private final HashSet<Integer> preparedSoundsIds = new HashSet();

    private int menuActionId = NO_SOUND_ID;
    private int playerOneMoveId = NO_SOUND_ID;
    private int playerTwoMoveId = NO_SOUND_ID;
    private int gameFinishedId = NO_SOUND_ID;
    private int clockResetId = NO_SOUND_ID;

    @Override
    public void init(Context context) {
        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            preparedSoundsIds.add(sampleId);
        });
        menuActionId = soundPool.load(context, R.raw.chess_clock_pause, 1);
        playerOneMoveId = soundPool.load(context, R.raw.chess_clock_switch1, 1);
        playerTwoMoveId = soundPool.load(context, R.raw.chess_clock_switch2, 1);
        gameFinishedId = soundPool.load(context, R.raw.chess_clock_time_ended, 1);
        clockResetId = soundPool.load(context, R.raw.chess_clock_reset, 1);
    }

    @Override
    public void releaseSounds() {
        soundPool.release();
    }

    @Override
    public void setSoundsEnabled(boolean soundsEnabled) {
        this.soundsEnabled = soundsEnabled;
    }

    @Override
    public boolean areSoundsEnabled() {
        return soundsEnabled;
    }

    @Override
    public void playSound(ClockSound sound, AudioManager manager) {
        if (!soundsEnabled) return;
        int soundId = clockSoundId(sound);

        if (soundId == NO_SOUND_ID) return; // sound failed to load
        if (!preparedSoundsIds.contains(soundId)) return; // sound not prepared

        float actualVolume = (float) manager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float soundVolume = actualVolume / maxVolume;

        soundPool.play(soundId, soundVolume, soundVolume, SOUND_PRIORITY, NO_LOOP, PLAYBACK_RATE);
    }

    @Override
    public void toggleSound() {
        soundsEnabled = !soundsEnabled;
    }

    private Integer clockSoundId(ClockSound sound) {
        int soundId = NO_SOUND_ID;
        switch (sound) {
            case PLAYER_ONE_MOVE:
                soundId = playerOneMoveId;
                break;
            case PLAYER_TWO_MOVE:
                soundId = playerTwoMoveId;
                break;
            case GAME_FINISHED:
                soundId = gameFinishedId;
                break;
            case RESET_CLOCK:
                soundId = clockResetId;
                break;
            case MENU_ACTION:
                soundId = menuActionId;
                break;
        }
        return soundId;
    }
}
