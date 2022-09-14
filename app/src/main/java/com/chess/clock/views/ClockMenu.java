package com.chess.clock.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.chess.clock.R;

public class ClockMenu extends ConstraintLayout {

    private final ImageView settingsButton;
    private final ImageView playPauseButton;
    private final ImageView resetButton;
    private final ImageView soundButton;

    public ClockMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.view_clock_menu, this, true);
        view.setBackgroundResource(R.color.toolbar_gray);

        settingsButton = view.findViewById(R.id.settingsBtn);
        playPauseButton = view.findViewById(R.id.playPauseBtn);
        resetButton = view.findViewById(R.id.resetBtn);
        soundButton = view.findViewById(R.id.soundBtn);
    }

    public void setListener(MenuClickListener listener) {
        settingsButton.setOnClickListener(v -> listener.timeSettingsClicked());
        playPauseButton.setOnClickListener(v -> listener.playPauseClicked());
        resetButton.setOnClickListener(v -> listener.resetClicked());
        soundButton.setOnClickListener(v -> listener.soundClicked());
    }

    public void showPause() {
        playPauseButton.setImageResource(R.drawable.ic_pause);
        playPauseButton.setVisibility(View.VISIBLE);
    }

    public void showPlay() {
        playPauseButton.setImageResource(R.drawable.ic_play);
        playPauseButton.setVisibility(View.VISIBLE);
    }

    public void hidePlayPauseBtn() {
        playPauseButton.setVisibility(View.INVISIBLE);
    }

    public void updateSoundIcon(boolean soundsEnabled) {
        if (soundsEnabled) {
            soundButton.setImageResource(R.drawable.ic_sound);
        } else {
            soundButton.setImageResource(R.drawable.ic_sound_off);
        }
    }

    public interface MenuClickListener {
        void timeSettingsClicked();

        void playPauseClicked();

        void resetClicked();

        void soundClicked();
    }
}
