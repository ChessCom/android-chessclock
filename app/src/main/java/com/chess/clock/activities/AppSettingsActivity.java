package com.chess.clock.activities;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chess.clock.R;
import com.chess.clock.adapters.ThemesAdapter;

public class AppSettingsActivity extends BaseActivity {

    private boolean soundsEnabled;
    private boolean fullScreenMode;
    private ThemesAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        soundsEnabled = appData.areSoundsEnabled();
        fullScreenMode = appData.getClockFullScreen();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
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
        });
        soundsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundsEnabled = isChecked;
        });
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