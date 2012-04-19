package com.chess.backend.entity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.chess.ui.core.AppConstants;

/**
 * AppData class
 *
 * @author alien_roger
 * @created at: 15.04.12 21:36
 */
public class AppData {
	private static AppData instance;
	private SharedPreferences preferences;

	public static AppData getInstance(Context context) {
		if(instance == null){
			instance = new AppData(context);
		}
		return instance;
	}

	private AppData(Context context) {
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public String getUserToken() {
		return preferences.getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY);
	}
}
