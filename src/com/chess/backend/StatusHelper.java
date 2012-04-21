package com.chess.backend;

import android.content.Context;
import com.chess.backend.tasks.UpdateStatusTask;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.MainApp;

/**
 * StatusHelper class
 *
 * @author alien_roger
 * @created at: 21.04.12 7:06
 */
public class StatusHelper {

	public static void checkStatusUpdate(MainApp mainApp, Context context){
		String userToken = mainApp.getSharedData().getString(AppConstants.USER_TOKEN, AppConstants.SYMBOL_EMPTY);
		if(!mainApp.guest && !userToken.equals(AppConstants.SYMBOL_EMPTY)){
			new UpdateStatusTask(context).execute(userToken);
		}
	}
}
