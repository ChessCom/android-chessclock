package com.chess.backend.entity;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
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

	public static SharedPreferences getPreferences(Context context){
		return context.getSharedPreferences(StaticData.SHARED_DATA_NAME, Context.MODE_PRIVATE);
	}


	public static String getUserToken(Context context) {
		SharedPreferences preferences = getPreferences(context);
		return preferences.getString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY);
	}

    public int getAfterMoveAction(Context context) {
		SharedPreferences preferences = getPreferences(context);
        String userName = preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
        return preferences.getInt(userName + AppConstants.PREF_ACTION_AFTER_MY_MOVE, StaticData.AFTER_MOVE_GO_TO_NEXT_GAME);
    }
    
    public String getUserName(Context context){
		SharedPreferences preferences = getPreferences(context);
        return preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY);
    }
    
}
