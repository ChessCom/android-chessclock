package com.chess.clock.statics;

import static com.chess.clock.statics.AppConstants.PREF_THEME;

import android.content.Context;
import android.content.SharedPreferences;

import com.chess.clock.entities.AppTheme;

/**
 * Created with Android Studio.
 * User: pedroteixeira pmcteixeira@gmail.com
 * Date: 17/09/14
 * Time: 14:15
 */
public class AppData {

    private final SharedPreferences preferences;

    public AppData(Context context) {
        this.preferences = context.getSharedPreferences(StaticData.SHARED_DATA_NAME, Context.MODE_PRIVATE);
    }

    public boolean getClockFullScreen() {
        return preferences.getBoolean(AppConstants.PREF_CLOCK_FULL_SCREEN, true);
    }

    public boolean areSoundsEnabled() {
        return preferences.getBoolean(AppConstants.PREF_CLOCK_SOUNDS_ON, true);
    }

    public void setSoundsEnabled(Boolean enabled) {
        preferences.edit().putBoolean(AppConstants.PREF_CLOCK_SOUNDS_ON, enabled).apply();
    }

    public AppTheme getSelectedTheme() {
        int themeOrdinal = preferences.getInt(PREF_THEME, AppTheme.GREEN.ordinal());
        return AppTheme.fromInt(themeOrdinal);
    }

    public void saveAppSetup(AppTheme theme, boolean fullScreen, boolean soundsEnabled) {
        preferences.edit()
                .putInt(PREF_THEME, theme.ordinal())
                .putBoolean(AppConstants.PREF_CLOCK_SOUNDS_ON, soundsEnabled)
                .putBoolean(AppConstants.PREF_CLOCK_FULL_SCREEN, fullScreen)
                .apply();
    }
}