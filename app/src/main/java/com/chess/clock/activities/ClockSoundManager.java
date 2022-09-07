package com.chess.clock.activities;

import android.content.Context;
import android.media.MediaPlayer;

import com.chess.clock.R;

interface ClockSoundManager {
    void init(Context context, Boolean soundsEnabled);
    void playSound(ClockSound sound);
    boolean areSoundsEnabled();
    void toggleSound();
}

class ClockSoundManagerImpl implements ClockSoundManager {
    private MediaPlayer playerOneMoveSound;
    private MediaPlayer playerTwoMoveSound;
    private MediaPlayer clockFinished;

    public boolean soundsEnabled = true;

    @Override
    public void init(Context context, Boolean soundsEnabled) {
        playerOneMoveSound = MediaPlayer.create(context, R.raw.chess_clock_switch1);
        playerTwoMoveSound = MediaPlayer.create(context, R.raw.chess_clock_switch2);
        clockFinished = MediaPlayer.create(context, R.raw.chess_clock_time_ended);
        this.soundsEnabled = soundsEnabled;
    }

    @Override
    public void playSound(ClockSound sound) {
        if(!soundsEnabled) return;
        switch (sound){
            case PLAYER_ONE_MOVE:
                playerOneMoveSound.start();
                break;
            case PLAYER_TWO_MOVE:
                playerTwoMoveSound.start();
                break;
            case GAME_FINISHED:
                clockFinished.start();
                break;
        }
    }

    @Override
    public boolean areSoundsEnabled() {
        return soundsEnabled;
    }

    @Override
    public void toggleSound() {
        soundsEnabled = !soundsEnabled;
    }
}

enum ClockSound {
    PLAYER_ONE_MOVE, PLAYER_TWO_MOVE, GAME_FINISHED
}
