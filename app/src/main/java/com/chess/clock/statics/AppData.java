package com.chess.clock.statics;

import android.content.Context;
import android.content.SharedPreferences;

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

    public void setClockFullScreen(boolean value) {
        preferences.edit().putBoolean(AppConstants.PREF_CLOCK_FULL_SCREEN, value).apply();
    }

    public boolean getClockFullScreen() {
        return preferences.getBoolean(AppConstants.PREF_CLOCK_FULL_SCREEN, true);
    }

}