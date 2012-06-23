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

public class AndroidStuff2 {
	private Context context;
//	private SharedPreferences sharedData;
//	private SharedPreferences.Editor sharedDataEditor;
//	private LccHolder lccHolder;
//	private GameBaseActivity lccEventListener;
	private LccEventListener lccEventListener;
//	private MyProgressDialog connectingIndicator;
//	private MyProgressDialog reconnectingIndicator;
    private static final String TAG = "AndroidStuff";

    public AndroidStuff2(Context context) {
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

//	public GameBaseActivity getLccEventListener() {
	public LccEventListener getLccEventListener() {
		return lccEventListener;
	}

//	public void setGameActivity(GameBaseActivity gameActivity) {
	public void setLccEventListener(LccEventListener lccEventListener) {
		this.lccEventListener = lccEventListener;
	}

	/*public Handler getUpdateBoardHandler() {
		return updateBoardHandler;
	}*/

	/*public void sendConnectionBroadcastIntent(boolean result, int code, String... errorMessage) {
		context.sendBroadcast(new Intent(WebService.BROADCAST_ACTION)
				.putExtra(AppConstants.REPEATABLE_TASK, false)
				.putExtra(AppConstants.CALLBACK_CODE, code)
				.putExtra(AppConstants.REQUEST_RESULT,
						result ? RestHelper.R_SUCCESS : RestHelper.R_ERROR + errorMessage[0])
		);
	}*/

	public void sendBroadcastObjectIntent(int code, String broadcastAction, Serializable object) {
		Log.d(TAG, AppConstants.LCCLOG_ANDROID_SEND_BROADCAST_OBJECT_INTENT_ACTION + broadcastAction);
		context.sendBroadcast(
				new Intent(broadcastAction)
						.putExtra(AppConstants.CALLBACK_CODE, code)
						.putExtra(AppConstants.OBJECT, object)
		);
//		if (currentProgressDialog != null) {
//			currentProgressDialog.dismiss();
//		}
	}

	public void sendBroadcastMessageIntent(int code, String broadcastAction, String title, String message) {
		Log.d(TAG, AppConstants.LCCLOG_ANDROID_SEND_BROADCAST_OBJECT_INTENT_ACTION + broadcastAction);
		context.sendBroadcast(
				new Intent(broadcastAction)
						.putExtra(AppConstants.CALLBACK_CODE, code)
						.putExtra(AppConstants.TITLE, title)
						.putExtra(AppConstants.MESSAGE, message)
		);
//		if (currentProgressDialog != null) {
//			currentProgressDialog.dismiss();
//		}
	}

	public void sendBroadcastIntent(int code, String broadcastAction) {
		Log.d(TAG, AppConstants.LCCLOG_ANDROID_SEND_BROADCAST_OBJECT_INTENT_ACTION + broadcastAction);
		context.sendBroadcast(
				new Intent(broadcastAction)
						.putExtra(AppConstants.CALLBACK_CODE, code)
		);
//		if (currentProgressDialog != null) {
//			currentProgressDialog.dismiss();
//		}
	}

	public void processDrawOffered(String offererUsername) {
		sendBroadcastMessageIntent(0, IntentConstants.FILTER_DRAW_OFFERED, context.getString(R.string.draw_game),
				offererUsername + StaticData.SYMBOL_SPACE  + context.getResources().getString(R.string.has_offered_draw));
	}

	public void processGameEnd(String message) {
		sendBroadcastMessageIntent(0, IntentConstants.ACTION_GAME_END, context.getString(R.string.game_over), message);
	}

//	public void setConnectingIndicator(MyProgressDialog connectingIndicator) {
//		this.connectingIndicator = connectingIndicator;
//	}
//
//	public MyProgressDialog getConnectingIndicator() {
//		return connectingIndicator;
//	}
//
//	public void setReconnectingIndicator(MyProgressDialog reconnectingIndicator) {
//		this.reconnectingIndicator = reconnectingIndicator;
//	}
//
//	public MyProgressDialog getReconnectingIndicator() {
//		return reconnectingIndicator;
//	}

//	public void manageProgressDialog(String broadcastAction, boolean enable, String message) {
//		Log.d(TAG, AppConstants.LCCLOG_ANDROID_SEND_BROADCAST_OBJECT_INTENT_ACTION + broadcastAction);
//		context.sendBroadcast(
//				new Intent(broadcastAction)
//						.putExtra(AppConstants.ENABLE_LIVE_CONNECTING_INDICATOR, enable)
//						.putExtra(AppConstants.MESSAGE, message)
//		);
//	}

	/*public void showLoggingInIndicator()
	  {
		manageProgressDialog("com.chess.lcc.android-logging-in-info", true, "Loading Live Chess");
	  }*/

//	@Deprecated
//	public void closeLoggingInIndicator() {
//		manageProgressDialog(IntentConstants.FILTER_LOGINING_INFO, false, StaticData.SYMBOL_EMPTY);
//	}

//	@Deprecated
//	public void showReconnectingIndicator() {
//		manageProgressDialog(IntentConstants.FILTER_RECONNECT_INFO, true, context.getString(R.string.reconnecting));
//	}
//
//	@Deprecated
//	public void closeReconnectingIndicator() {
//		manageProgressDialog(IntentConstants.FILTER_RECONNECT_INFO, false, StaticData.SYMBOL_EMPTY);
//	}

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

	/*public void startSigninActivity()
	  {
		coreContext.getSharedDataEditor().putString("password", StaticData.SYMBOL_EMPTY);
		coreContext.getSharedDataEditor().putString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY);
		coreContext.getSharedDataEditor().commit();
		final Intent intent = new Intent(mainApp, Singin.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		coreContext.startActivity(intent);
	  }*/



}

