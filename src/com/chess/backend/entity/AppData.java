package com.chess.backend.entity;

import android.content.Context;
import android.content.SharedPreferences;
import com.chess.backend.statics.StaticData;
import com.chess.ui.core.AppConstants;

/**
 * AppData class
 *
 * @author alien_roger
 * @created at: 15.04.12 21:36
 */
public class AppData {
	private static AppData instance;

	public static AppData getInstance() {
		if(instance == null){
			instance = new AppData();
		}
		return instance;
	}

	private AppData() {
	}

	public String getUserToken(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(StaticData.SHARED_DATA_NAME, Context.MODE_PRIVATE);
		return preferences.getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY);
	}

    public int getAfterMoveAction(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(StaticData.SHARED_DATA_NAME, Context.MODE_PRIVATE);
        String userName = preferences.getString(AppConstants.USERNAME, AppConstants.SYMBOL_EMPTY);
        preferences.getInt(userName + AppConstants.PREF_ACTION_AFTER_MY_MOVE, StaticData.AFTER_MOVE_GO_TO_NEXT_GAME);
        return 0;
    }
    
    public String getUserName(Context context){
        SharedPreferences preferences = context.getSharedPreferences(StaticData.SHARED_DATA_NAME, Context.MODE_PRIVATE);
        return preferences.getString(AppConstants.USERNAME, AppConstants.SYMBOL_EMPTY);
    }
    
}
