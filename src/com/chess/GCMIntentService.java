/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chess;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.util.Log;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.GcmItem;
import com.chess.backend.entity.api.YourTurnItem;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.backend.gcm.*;
import com.chess.db.DbDataManager;
import com.chess.model.BaseGameItem;
import com.chess.model.DataHolder;
import com.chess.statics.*;
import com.chess.utilities.AppUtils;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.google.gson.Gson;

import java.util.List;
import java.util.Random;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCMIntentService";
	private static final String TOKEN = Long.toBinaryString(new Random().nextLong());
	private SharedPreferences preferences;

	public GCMIntentService() {
		super(GcmHelper.SENDER_ID);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		AppData appData = new AppData(context);
		Log.d(TAG, "User = " + appData.getUsername() + " Device registered: regId = " + registrationId);

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_GCM);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, appData.getUserToken());
		loadItem.addRequestParams(RestHelper.GCM_P_REGISTER_ID, registrationId);

		Log.d(TAG, "Registering to server, registrationId = " + registrationId
				+ " \ntoken = " + appData.getUserToken());

		GcmItem item = null;
		try {
			item = RestHelper.getInstance().requestData(loadItem, GcmItem.class, context);
		} catch (InternalErrorException e) {
			int resultCode = e.getCode();
			if (RestHelper.containsServerCode(resultCode)) {
				int serverCode = RestHelper.decodeServerCode(resultCode);
				if (serverCode == ServerErrorCodes.YOUR_GCM_ID_ALREADY_REGISTERED) {
					GCMRegistrar.setRegisteredOnServer(context, true);
					appData.registerOnChessGCM(appData.getUserToken());
					Log.d(TAG, "Already registered on server -> Re-registering GCM");
				}
			}

			e.logMe();
		}

		if (item != null && item.getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
			GCMRegistrar.setRegisteredOnServer(context, true);
			appData.registerOnChessGCM(appData.getUserToken());
		}
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {  // Do nothing here
//		AppData appData = new AppData(context);
//		Log.d(TAG, "User = " + appData.getUsername() + " Device unregistered, registrationId = " + registrationId);
//
//		if (GCMRegistrar.isRegisteredOnServer(context)) {
//			preferences = appData.getPreferences();
//			// TODO temporary unregister only if user loged out
//			String realToken = preferences.getString(AppConstants.USER_TOKEN, Symbol.EMPTY);
//			if (realToken.equals(Symbol.EMPTY)) {
//
//				String token = preferences.getString(AppConstants.PREF_TEMP_TOKEN_GCM, Symbol.EMPTY);
//
//				LoadItem loadItem = new LoadItem();
//				loadItem.setLoadPath(RestHelper.getInstance().CMD_GCM);
//				loadItem.setRequestMethod(RestHelper.DELETE);
//				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, token);
//
//				GcmItem item = null;
//				try {
//					item = RestHelper.getInstance().requestData(loadItem, GcmItem.class, context);
//				} catch (InternalErrorException e) {
//					e.logMe();
//				}
//
//				if (item != null && item.getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
//					GCMRegistrar.setRegisteredOnServer(context, false);
//					appData.unRegisterOnChessGCM();
//					// remove saved token
//					SharedPreferences.Editor editor = preferences.edit();
//					editor.putString(AppConstants.PREF_TEMP_TOKEN_GCM, Symbol.EMPTY);
//					editor.commit();
//				}
//
//				Log.d(TAG, "Unregistering from server, registrationId = " + registrationId + "token = " + token);
//			}
//		} else {
//			// This callback results from the call to unregister made on
//			// GcmHelper when the registration to the server failed.
//			Log.d(TAG, "Ignoring unregister callback");
//		}
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		AppData appData = new AppData(context);
		Log.d(TAG, "User = " + appData.getUsername() + " Received message");

		String type = intent.getStringExtra("type");
		Log.d(TAG, "type = " + type + " intent = " + intent);
		if (BuildConfig.DEBUG && intent.hasExtra("message")) {
			Log.d(TAG, "type = " + type + " message = " + intent.getStringExtra("message"));

			String message = intent.getStringExtra("message");          // TODO remove after debug
			if (message.length() >= 20) {
				message = message.substring(0, 20);
			}
			AppUtils.showStatusBarNotification(context, type, message);
		}

		if (type.equals(GcmHelper.NOTIFICATION_YOUR_MOVE)) {
			Log.d(TAG, "received move notification, notifications enabled = " + appData.isNotificationsEnabled());

			showYouTurnNotification(intent, context);
		} else if (type.equals(GcmHelper.NOTIFICATION_NEW_FRIEND_REQUEST)) {

			showNewFriendRequest(intent, context);
			sendNotificationBroadcast(context, type);

			if (!DataHolder.getInstance().isMainActivityVisible() && appData.isNotificationsEnabled()) {
				String title = context.getString(R.string.you_have_new_friend_request);
				String body = context.getString(R.string.you_have_new_friend_request);
				AppUtils.showStatusBarNotification(context, title, body);
			}
		} else if (type.equals(GcmHelper.NOTIFICATION_NEW_MESSAGE)) {

//			showNewMessage(intent, context);
			sendNotificationBroadcast(context, type);
			if (!DataHolder.getInstance().isMainActivityVisible() && appData.isNotificationsEnabled()) {
				String title = context.getString(R.string.you_have_new_message);
				String body = context.getString(R.string.you_have_new_message);
				AppUtils.showStatusBarNotification(context, title, body);
			}
		} else if (type.equals(GcmHelper.NOTIFICATION_NEW_CHAT_MESSAGE)) {

			showNewChatMessage(intent, context);
			sendNotificationBroadcast(context, type);
			if (!DataHolder.getInstance().isMainActivityVisible() && appData.isNotificationsEnabled()) {
				String title = context.getString(R.string.you_have_new_chat_message);
				String body = context.getString(R.string.you_have_new_chat_message);
				AppUtils.showStatusBarNotification(context, title, body);
			}
		} else if (type.equals(GcmHelper.NOTIFICATION_MOVE_MADE)) {
			context.sendBroadcast(new Intent(IntentConstants.USER_MOVE_UPDATE));
		} else if (type.equals(GcmHelper.NOTIFICATION_GAME_OVER)) {

			showGameOver(intent, context);
			sendNotificationBroadcast(context, type);
			if (!DataHolder.getInstance().isMainActivityVisible() && appData.isNotificationsEnabled()) {
				String title = context.getString(R.string.your_game_is_over);
				String body = context.getString(R.string.your_game_is_over);
				AppUtils.showStatusBarNotification(context, title, body);
			}
		} else if (type.equals(GcmHelper.NOTIFICATION_NEW_CHALLENGE)) {

			showNewChallenge(intent, context);
			sendNotificationBroadcast(context, type);
			if (!DataHolder.getInstance().isMainActivityVisible() && appData.isNotificationsEnabled()) {
				String title = context.getString(R.string.you_have_new_challenge);
				String body = context.getString(R.string.you_have_new_challenge);
				AppUtils.showStatusBarNotification(context, title, body);
			}
		}
	}

	private void sendNotificationBroadcast(Context context, String type) {
		Intent notifyIntent = new Intent(IntentConstants.NOTIFICATIONS_UPDATE);
		notifyIntent.putExtra(IntentConstants.TYPE, type);
		context.sendBroadcast(notifyIntent);
	}

	private synchronized void showNewFriendRequest(Intent intent, Context context) {
		FriendRequestItem friendRequestItem = new FriendRequestItem();

		friendRequestItem.setMessage(intent.getStringExtra("message"));
		friendRequestItem.setUsername(intent.getStringExtra("sender"));
		friendRequestItem.setCreatedAt(Long.parseLong(intent.getStringExtra("created_at")));
		friendRequestItem.setAvatar(intent.getStringExtra("avatar_url"));
		friendRequestItem.setRequestId(Long.parseLong(intent.getStringExtra("request_id")));
		Log.d(TAG, " _________________________________");
		Log.d(TAG, " FriendRequestItem = " + new Gson().toJson(friendRequestItem));

		ContentResolver contentResolver = context.getContentResolver();
		String username = new AppData(context).getUsername();

		DbDataManager.saveNewFriendRequest(contentResolver, friendRequestItem, username);
	}

	private synchronized void showNewChatMessage(Intent intent, Context context) {
		NewChatNotificationItem chatNotificationItem = new NewChatNotificationItem();

		chatNotificationItem.setMessage(intent.getStringExtra("message"));
		chatNotificationItem.setUsername(intent.getStringExtra("sender"));
		chatNotificationItem.setGameId(Long.parseLong(intent.getStringExtra("game_id")));
		chatNotificationItem.setCreatedAt(Long.parseLong(intent.getStringExtra("created_at")));
		chatNotificationItem.setAvatar(intent.getStringExtra("avatar_url"));
		Log.d(TAG, " _________________________________");
		Log.d(TAG, " NewChatNotificationItem = " + new Gson().toJson(chatNotificationItem));

		ContentResolver contentResolver = context.getContentResolver();
		String username = new AppData(context).getUsername();

		DbDataManager.saveNewChatNotification(contentResolver, chatNotificationItem, username);
	}

//	private synchronized void showNewMessage(Intent intent, Context context){
//		NewChatNotificationItem chatNotificationItem = new NewChatNotificationItem();
//
//		chatNotificationItem.setMessage(intent.getStringExtra("message"));
//		chatNotificationItem.setUsername(intent.getStringExtra("sender"));
//		chatNotificationItem.setGameId(Long.parseLong(intent.getStringExtra("game_id")));
//		chatNotificationItem.setCreatedAt(Long.parseLong(intent.getStringExtra("created_at")));
//		chatNotificationItem.setAvatar(intent.getStringExtra("avatar_url"));
//		Log.d(TAG, " _________________________________");
//		Log.d(TAG, " NewChatNotificationItem = " + new Gson().toJson(chatNotificationItem));
//
//		ContentResolver contentResolver = context.getContentResolver();
//		String username = new AppData(context).getUsername();
//		DbDataManager.saveNewChatNotification(contentResolver, chatNotificationItem, username);
//	}

	private synchronized void showGameOver(Intent intent, Context context) {
		GameOverNotificationItem gameOverNotificationItem = new GameOverNotificationItem();

		gameOverNotificationItem.setMessage(intent.getStringExtra("message"));
		gameOverNotificationItem.setGameId(Long.parseLong(intent.getStringExtra("game_id")));
		gameOverNotificationItem.setAvatar(intent.getStringExtra("avatar_url"));
		Log.d(TAG, " _________________________________");
		Log.d(TAG, " GameOverNotificationItem = " + new Gson().toJson(gameOverNotificationItem));
		ContentResolver contentResolver = context.getContentResolver();
		String username = new AppData(context).getUsername();

		DbDataManager.saveGameOverNotification(contentResolver, gameOverNotificationItem, username);
	}

	private synchronized void showNewChallenge(Intent intent, Context context) {
		NewChallengeNotificationItem challengeNotificationItem = new NewChallengeNotificationItem();

		challengeNotificationItem.setUsername(intent.getStringExtra("sender"));
		challengeNotificationItem.setAvatar(intent.getStringExtra("avatar_url"));
		challengeNotificationItem.setChallengeId(Long.parseLong(intent.getStringExtra("challenge_id")));
		Log.d(TAG, " _________________________________");
		Log.d(TAG, " NewChallengeNotificationItem = " + new Gson().toJson(challengeNotificationItem));
		ContentResolver contentResolver = context.getContentResolver();
		String username = new AppData(context).getUsername();
		DbDataManager.saveNewChallengeNotification(contentResolver, challengeNotificationItem, username);
	}

	private synchronized void showYouTurnNotification(Intent intent, Context context) {
		AppData appData = new AppData(context);
		String username = appData.getUsername();

		String lastMoveSan = intent.getStringExtra("last_move_san");
//			String opponentUserId = intent.getStringExtra("opponent_user_id");
//			String collapseKey = intent.getStringExtra("collapse_key");
		String opponentUsername = intent.getStringExtra("opponent_username");
		String gameId = intent.getStringExtra("game_id");

//		boolean gameInfoFound = false;
		Log.d(TAG, " _________________________________");
		Log.d(TAG, " LastMoveSan = " + lastMoveSan);
		Log.d(TAG, " gameId = " + gameId);
		Log.d(TAG, " opponentUsername = " + opponentUsername);
		Log.d(TAG, " is inOnlineGame = " + DataHolder.getInstance().inOnlineGame(Long.parseLong(gameId)));

		// we use the same registerId for all users on a device, so check username to notify only the needed user
		if (opponentUsername.equalsIgnoreCase(username)) {
			return; // don't need notification of myself game
		}
//		Log.d("TEST", " lastMoveInfoItems.size() = " + DataHolder.getInstance().getLastMoveInfoItems().size());

		// check if we already received that notification
//		for (LastMoveInfoItem lastMoveInfoItem : DataHolder.getInstance().getLastMoveInfoItems()) {
//			if (lastMoveInfoItem.getGameId().equals(gameId)) { // if have info about this game
//				Log.d(TAG, " lastMoveInfoItem.getLastMoveSan().equals(lastMoveSan) = " + lastMoveInfoItem.getLastMoveSan().equals(lastMoveSan));
//				if (lastMoveInfoItem.getLastMoveSan().equals(lastMoveSan)) { // if this game info already contains the same move update
//					return; // no need to update
//				} else { // if move info is different
//					lastMoveInfoItem.setLastMoveSan(lastMoveSan);
//				}
//				gameInfoFound = true;
//			}
//		}

//		if (!gameInfoFound) { // if we have no info about this game, then add last move to list of objects
//			Log.d(TAG, " adding new game info");
//			LastMoveInfoItem lastMoveInfoItem = new LastMoveInfoItem();
//			lastMoveInfoItem.setLastMoveSan(lastMoveSan);
//			lastMoveInfoItem.setGameId(gameId);
//			DataHolder.getInstance().addLastMoveInfo(lastMoveInfoItem);
//		}

		// Saving play move notification to DB
		ContentResolver contentResolver = context.getContentResolver();

		YourTurnItem yourTurnItem = new YourTurnItem(lastMoveSan, username, Long.parseLong(gameId));
		yourTurnItem.setOpponent(opponentUsername);

		DbDataManager.savePlayMoveNotification(contentResolver, username, yourTurnItem);


		List<YourTurnItem> moveNotifications = DbDataManager.getAllPlayMoveNotifications(contentResolver, username);

		if (DataHolder.getInstance().inOnlineGame(Long.parseLong(gameId))) { // don't show notification

			Intent gameUpdateIntent = new Intent(IntentConstants.BOARD_UPDATE);
			gameUpdateIntent.putExtra(BaseGameItem.GAME_ID, Long.parseLong(gameId));
			context.sendBroadcast(gameUpdateIntent);
		} else {
			context.sendBroadcast(new Intent(IntentConstants.USER_MOVE_UPDATE));

			if (appData.isNotificationsEnabled()) {  // we check it here because we will use GCM for lists update, so it need to be registered.

				AppUtils.showNewMoveStatusNotification(context, moveNotifications, StaticData.MOVE_REQUEST_CODE);

				boolean playSoundsFlag = AppUtils.getSoundsPlayFlag(context);
				if (playSoundsFlag) {
					final MediaPlayer player = MediaPlayer.create(context, R.raw.move_opponent);
					if (player != null) {
						player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
							@Override
							public void onCompletion(MediaPlayer mediaPlayer) {
								player.stop();
								player.release();
							}
						});
						player.start();
					}
				}
			}
		}
	}

	@Override
	protected void onDeletedMessages(Context context, int total) {
		Log.d(TAG, "Received deleted messages notification, cnt = " + total);
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.d(TAG, "Received error: " + errorId);
		if (errorId != null) {
			if ("SERVICE_NOT_AVAILABLE".equals(errorId)) {
				long backoffTimeMs = preferences.getLong(AppConstants.GCM_RETRY_TIME, 100);// get back-off time from shared preferences
				long nextAttempt = SystemClock.elapsedRealtime() + backoffTimeMs;
				Intent retryIntent = new Intent("com.example.gcm.intent.RETRY");
				retryIntent.putExtra("token", TOKEN);
				PendingIntent retryPendingIntent = PendingIntent.getBroadcast(context, 0, retryIntent, 0);
				AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
				am.set(AlarmManager.ELAPSED_REALTIME, nextAttempt, retryPendingIntent);
				backoffTimeMs *= 2; // Next retry should wait longer.
				// update back-off time on shared preferences
				SharedPreferences.Editor editor = preferences.edit();
				editor.putLong(AppConstants.GCM_RETRY_TIME, backoffTimeMs);
				editor.commit();
			} else {
				// Unrecoverable error, log it
				Log.i(TAG, "Received error: " + errorId);
			}
		}
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		Log.d(TAG, "Received recoverable error: " + errorId);
		return super.onRecoverableError(context, errorId);
	}

}
