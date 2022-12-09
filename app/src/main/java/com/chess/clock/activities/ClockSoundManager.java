package com.chess.clock.activities;

import android.content.Context;
import android.media.MediaPlayer;

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

    void playSound(ClockSound sound);

    void toggleSound();
}

class ClockSoundManagerImpl implements ClockSoundManager {
    public boolean soundsEnabled = true;
    private MediaPlayer playerOneMoveSound;
    private MediaPlayer playerTwoMoveSound;
    private MediaPlayer gameFinished;
    private MediaPlayer clockReset;
    private MediaPlayer menuAction;

    private final HashSet<String> preparedSounds = new HashSet();

    @Override
    public void init(Context context) {
        playerOneMoveSound = MediaPlayer.create(context, R.raw.chess_clock_switch1);
        playerOneMoveSound.setOnPreparedListener(mediaPlayer -> preparedSounds.add(ClockSound.PLAYER_ONE_MOVE.name()));
        playerTwoMoveSound = MediaPlayer.create(context, R.raw.chess_clock_switch2);
        playerTwoMoveSound.setOnPreparedListener(mediaPlayer -> preparedSounds.add(ClockSound.PLAYER_TWO_MOVE.name()));
        gameFinished = MediaPlayer.create(context, R.raw.chess_clock_time_ended);
        gameFinished.setOnPreparedListener(mediaPlayer -> preparedSounds.add(ClockSound.GAME_FINISHED.name()));
        clockReset = MediaPlayer.create(context, R.raw.chess_clock_reset);
        clockReset.setOnPreparedListener(mediaPlayer -> preparedSounds.add(ClockSound.RESET_CLOCK.name()));
        menuAction = MediaPlayer.create(context, R.raw.chess_clock_pause);
        menuAction.setOnPreparedListener(mediaPlayer -> preparedSounds.add(ClockSound.MENU_ACTION.name()));
    }

    @Override
    public void releaseSounds() {
        playerOneMoveSound.release();
        playerTwoMoveSound.release();
        gameFinished.release();
        clockReset.release();
        menuAction.release();
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
        if (!preparedSounds.contains(sound.name())) return;

        switch (sound) {
            case PLAYER_ONE_MOVE:
                playerOneMoveSound.start();
                break;
            case PLAYER_TWO_MOVE:
                playerTwoMoveSound.start();
                break;
            case GAME_FINISHED:
                gameFinished.start();
                break;
            case RESET_CLOCK:
                clockReset.start();
                break;
            case MENU_ACTION:
                menuAction.start();
                break;
        }
    }

    @Override
    public void toggleSound() {
        soundsEnabled = !soundsEnabled;
    }
}
