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

    private final SharedPreferences.Editor editor;
    private SharedPreferences preferences;
    private Context mContext;

    public AppData(Context context) {
        mContext = context;
        this.preferences = context.getSharedPreferences(StaticData.SHARED_DATA_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public SharedPreferences getPreferences() {
        return preferences;
    }

    public SharedPreferences.Editor getEditor() {
        return editor;
    }

    public void clearPreferences() {
        preferences.edit()
                .clear()
                .commit();
    }

    public void setClockFullScreen(boolean value) {
        preferences.edit().putBoolean(AppConstants.PREF_CLOCK_FULL_SCREEN, value).commit();
    }

    public boolean getClockFullScreen() {
        return preferences.getBoolean(AppConstants.PREF_CLOCK_FULL_SCREEN, true);
    }

}