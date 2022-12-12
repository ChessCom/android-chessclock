package com.chess.clock.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.chess.clock.BuildConfig;
import com.chess.clock.R;
import com.chess.clock.adapters.ThemesAdapter;
import com.chess.clock.entities.AppTheme;

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
        RecyclerView recycler = findViewById(R.id.themesRecycler);
        SwitchCompat fullScreenSwitch = findViewById(R.id.fullscreenSwitch);
        SwitchCompat soundsSwitch = findViewById(R.id.soundSwitch);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.app_settings);
        }

        AppTheme selectedTheme = appData.getSelectedTheme();
        adapter = new ThemesAdapter(selectedTheme, theme -> {
            ColorStateList tintChecked = theme.switchColorStateList(this);
            DrawableCompat.setTintList(fullScreenSwitch.getThumbDrawable(), tintChecked);
            DrawableCompat.setTintList(soundsSwitch.getThumbDrawable(), tintChecked);
        });
        recycler.setAdapter(adapter);

        fullScreenSwitch.setChecked(fullScreenMode);
        soundsSwitch.setChecked(soundsEnabled);
        ColorStateList tintChecked = selectedTheme.switchColorStateList(this);
        DrawableCompat.setTintList(fullScreenSwitch.getThumbDrawable(), tintChecked);
        DrawableCompat.setTintList(soundsSwitch.getThumbDrawable(), tintChecked);

        fullScreenSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            fullScreenMode = isChecked;
            updateUiState();
        });
        MediaPlayer enableSounds = MediaPlayer.create(this, R.raw.chess_clock_pause);
        soundsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            soundsEnabled = isChecked;
            updateUiState();
            if (isChecked) {
                enableSounds.start();
            }
        });

        findViewById(R.id.restoreBtn).setOnClickListener(v -> {
            TextView titleView = (TextView) View.inflate(this, R.layout.dialog_title_text_view, null);
            titleView.setText(R.string.restore_default_controls);
            new AlertDialog.Builder(this, R.style.WhiteButtonsDialogTheme)
                    .setMessage(R.string.restore_default_controls_confirmation_message)
                    .setCustomTitle(titleView)
                    .setPositiveButton(R.string.action_confirm, (dialog, id) -> {
                        BaseActivity activity = AppSettingsActivity.this;
                        activity.setResult(Activity.RESULT_OK);
                        activity.finishWithAnimation();
                    })
                    .setNegativeButton(R.string.action_cancel, (dialog, id) -> {
                        // dismiss
                    })
                    .create()
                    .show();
        });

        updateUiState();
        setVersionInfo();
    }

    private void setVersionInfo() {
        TextView versionTv = (TextView) findViewById(R.id.versionTv);
        String version = getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
        versionTv.setText(version);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finishWithAnimation();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finishWithAnimation();
    }

}