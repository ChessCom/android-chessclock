package com.chess.clock.activities;

import android.content.Context;
import android.media.MediaPlayer;

import com.chess.clock.R;

enum ClockSound {
    PLAYER_ONE_MOVE, PLAYER_TWO_MOVE, GAME_FINISHED, RESET_CLOCK
}

interface ClockSoundManager {
    void init(Context context);

    void setSoundsEnabled(boolean enabled);

    boolean areSoundsEnabled();

    void playSound(ClockSound sound);

    void toggleSound();
}

class ClockSoundManagerImpl implements ClockSoundManager {
    public boolean soundsEnabled = true;
    private MediaPlayer playerOneMoveSound;
    private MediaPlayer playerTwoMoveSound;
    private MediaPlayer clockFinished;
    private MediaPlayer clockReset;

    @Override
    public void init(Context context) {
        playerOneMoveSound = MediaPlayer.create(context, R.raw.chess_clock_switch1);
        playerTwoMoveSound = MediaPlayer.create(context, R.raw.chess_clock_switch2);
        clockFinished = MediaPlayer.create(context, R.raw.chess_clock_time_ended);
        clockReset = MediaPlayer.create(context, R.raw.chess_clock_reset);
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
    public void playSound(ClockSound sound) {
        if (!soundsEnabled) return;
        switch (sound) {
            case PLAYER_ONE_MOVE:
                playerOneMoveSound.start();
                break;
            case PLAYER_TWO_MOVE:
                playerTwoMoveSound.start();
                break;
            case GAME_FINISHED:
                clockFinished.start();
                break;
            case RESET_CLOCK:
                clockReset.start();
                break;
        }
    }

    @Override
    public void toggleSound() {
        soundsEnabled = !soundsEnabled;
    }
}
