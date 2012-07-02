/*
 * AndroidStuff.java
 */

package com.chess.lcc.android;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.interfaces.LccEventListener;

import java.io.Serializable;

public class AndroidStuff {
	private Context context;
	//	private SharedPreferences sharedData;
//	private SharedPreferences.Editor sharedDataEditor;
//	private LccHolder lccHolder;
//	private GameBaseActivity lccEventListener;
	private LccEventListener lccEventListener;
	//	private MyProgressDialog connectingIndicator;
//	private MyProgressDialog reconnectingIndicator;
	private static final String TAG = "AndroidStuff";

	public AndroidStuff(Context context) {
		this.context = context;
	}

	public Context getContext() {
		return context;
	}

//	public void setCurrentProgressDialog(MyProgressDialog currentProgressDialog) {
//		this.currentProgressDialog = currentProgressDialog;
//	}

//	public SharedPreferences getSharedData() {
//		if (sharedData == null) {
//			sharedData = context.getSharedPreferences(StaticData.SHARED_DATA_NAME, 0);
//		}
//		return sharedData;
//	}
//
//	public SharedPreferences.Editor getSharedDataEditor() {
//		if (sharedDataEditor == null) {
//			sharedDataEditor = getSharedData().edit();
//		}
//		return sharedDataEditor;
//	}

//	public LccEventListener getLccEventListener() {
//		return lccEventListener;
//	}
//
//	public void setLccEventListener(LccEventListener lccEventListener) {
//		this.lccEventListener = lccEventListener;
//	}

	/*public Handler getUpdateBoardHandler() {
		return updateBoardHandler;
	}*/


    /**
     * Use LccEventListener instead
     * @param code
     * @param broadcastAction
     * @param object
     */
    @Deprecated
	public void sendBroadcastObjectIntent(int code, String broadcastAction, Serializable object) {
		Log.d(TAG, AppConstants.LCCLOG_ANDROID_SEND_BROADCAST_OBJECT_INTENT_ACTION + broadcastAction);
		context.sendBroadcast(
				new Intent(broadcastAction)
						.putExtra(AppConstants.CALLBACK_CODE, code)
						.putExtra(AppConstants.OBJECT, object)
		);
	}

    /**
     * Use LccEventListener instead
     * @param code
     * @param broadcastAction
     * @param title
     * @param message
     */
    @Deprecated
	public void sendBroadcastMessageIntent(int code, String broadcastAction, String title, String message) {
		Log.d(TAG, AppConstants.LCCLOG_ANDROID_SEND_BROADCAST_OBJECT_INTENT_ACTION + broadcastAction);
		context.sendBroadcast(
				new Intent(broadcastAction)
						.putExtra(AppConstants.CALLBACK_CODE, code)
						.putExtra(AppConstants.TITLE, title)
						.putExtra(AppConstants.MESSAGE, message)
		);
	}

    /**
     * Use LccEventListener instead
     * @param code
     * @param broadcastAction
     */
    @Deprecated
	public void sendBroadcastIntent(int code, String broadcastAction) {
		Log.d(TAG, AppConstants.LCCLOG_ANDROID_SEND_BROADCAST_OBJECT_INTENT_ACTION + broadcastAction);
		context.sendBroadcast(
				new Intent(broadcastAction)
						.putExtra(AppConstants.CALLBACK_CODE, code)
		);
	}

	public void processDrawOffered(String offererUsername) {
//		sendBroadcastMessageIntent(0, IntentConstants.FILTER_DRAW_OFFERED, context.getString(R.string.draw_game),
//				offererUsername + StaticData.SYMBOL_SPACE + context.getResources().getString(R.string.has_offered_draw));
	}

	public void processGameEnd(String message) {
//		sendBroadcastMessageIntent(0, IntentConstants.ACTION_GAME_END, context.getString(R.string.game_over), message);
	}



//	@Deprecated
//	public void informAndExit(String title, String message) {
//		informAndExit(IntentConstants.FILTER_EXIT_INFO, title, message);
//	}

//	public void processOtherClientEntered() {
//		informAndExit(IntentConstants.FILTER_EXIT_INFO, StaticData.SYMBOL_EMPTY,
//				context.getString(R.string.another_login_detected));
//	}

//	public void informAndExit(String broadcastAction, String title, String message) {
//		Log.d(TAG, AppConstants.LCCLOG_ANDROID_SEND_BROADCAST_OBJECT_INTENT_ACTION + broadcastAction);
//		context.sendBroadcast(
//				new Intent(broadcastAction)
//						.putExtra(AppConstants.TITLE, title)
//						.putExtra(AppConstants.MESSAGE, message)
//		);
//	}

//	public void processObsoleteProtocolVersion() {
//		context.sendBroadcast(new Intent(IntentConstants.FILTER_PROTOCOL_VERSION));
//	}

}

