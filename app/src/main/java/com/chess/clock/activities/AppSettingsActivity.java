package com.chess.clock.activities;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chess.clock.R;
import com.chess.clock.adapters.ThemesAdapter;

public class AppSettingsActivity extends BaseActivity {

    private boolean soundsEnabled;
    private boolean fullScreenMode;
    private ThemesAdapter adapter;

    private ImageView soundImg;
    private ImageView fullScreenImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        soundsEnabled = appData.areSoundsEnabled();
        fullScreenMode = appData.getClockFullScreen();

        soundImg = findViewById(R.id.soundImg);
        fullScreenImg = findViewById(R.id.fullscreenImg);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.app_settings);
        }

        RecyclerView recycler = findViewById(R.id.themesRecycler);
        adapter = new ThemesAdapter(appData.getSelectedTheme());
        recycler.setAdapter(adapter);

        SwitchCompat fullScreenSwitch = findViewById(R.id.fullscreenSwitch);
        SwitchCompat soundsSwitch = findViewById(R.id.soundSwitch);

        fullScreenSwitch.setChecked(fullScreenMode);
        soundsSwitch.setChecked(soundsEnabled);

        fullScreenSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            fullScreenMode = isChecked;
            updateUiState();
        });
        soundsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundsEnabled = isChecked;
            updateUiState();
        });

        updateUiState();
    }

    private void updateUiState() {
        soundImg.setImageResource(soundsEnabled ? R.drawable.ic_settings_sound_on : R.drawable.ic_settings_sound_off);
        fullScreenImg.setImageResource(fullScreenMode ? R.drawable.ic_fullscreen : R.drawable.ic_fullscreen_exit);
    }

    @Override
    protected void onPause() {
        super.onPause();
        appData.saveAppSetup(
                adapter.selectedTheme,
                fullScreenMode,
                soundsEnabled
        );
    }
}