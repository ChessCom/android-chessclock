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
import android.os.SystemClock;
import android.util.Log;
import com.chess.backend.GetAndSaveUserStats;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.ServerErrorCodes;
import com.chess.backend.entity.api.GcmItem;
import com.chess.backend.entity.api.YourTurnItem;
import com.chess.backend.entity.api.daily_games.DailyCurrentGameData;
import com.chess.backend.entity.api.daily_games.DailyCurrentGamesItem;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.backend.gcm.*;
import com.chess.db.DbDataManager;
import com.chess.model.BaseGameItem;
import com.chess.model.DataHolder;
import com.chess.statics.*;
import com.chess.ui.engine.SoundPlayer;
import com.chess.utilities.AppUtils;
import com.chess.utilities.LogMe;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.chess.backend.RestHelper.P_LOGIN_TOKEN;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCMIntentService";
	private static final String TOKEN = Long.toBinaryString(new Random().nextLong());

	public static final String SENDER = "sender";
	public static final String USERNAME = "username";
	public static final String MESSAGE = "message";
	public static final String CREATED_AT = "created_at";
	public static final String AVATAR_URL = "avatar_url";
	public static final String GAME_ID = "game_id";
	public static final String REQUEST_ID = "request_id";
	public static final String CHALLENGE_ID = "challenge_id";
	public static final String LAST_MOVE_SAN = "last_move_san";
	public static final String OPPONENT_USERNAME = "opponent_username";
	public static final String SENDER_USERNAME = "sender_username";
	private SharedPreferences preferences;

	public GCMIntentService() {
		super(GcmHelper.SENDER_ID);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		AppData appData = new AppData(context);
		LogMe.dl(TAG, "User = " + appData.getUsername() + " Device registered: regId = " + registrationId);

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_GCM);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, appData.getUserToken());
		loadItem.addRequestParams(RestHelper.GCM_P_REGISTER_ID, registrationId);

		LogMe.dl(TAG, "Registering to server, registrationId = " + registrationId
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
					LogMe.dl(TAG, "Already registered on server -> Re-registering GCM");
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
		String username = appData.getUsername();
		LogMe.dl(TAG, "User = " + username + " Received message");

		String type = intent.getStringExtra("type");
		if (type == null) {
			return;
		}

		LogMe.dl(TAG, "intent.hasExtra(\"owner\") = " + intent.hasExtra("owner"));

		if (intent.hasExtra("owner")) {
			String owner = intent.getStringExtra("owner");
			LogMe.dl(TAG, "owner = " + owner);
			if (owner != null && !owner.equals(username)) { // don't handle not our messages
				return;
			}
		}

		LogMe.dl(TAG, "type = " + type + " intent = " + intent);
		if (BuildConfig.DEBUG && intent.hasExtra(MESSAGE)) {
			Log.d(TAG, "type = " + type + " message = " + intent.getStringExtra(MESSAGE));
		}

		if (type.equals(GcmHelper.NOTIFICATION_YOUR_MOVE)) {
			LogMe.dl(TAG, "received move notification, notifications enabled = " + appData.isNotificationsEnabled());

			showYouTurnNotification(intent, context);
		} else if (type.equals(GcmHelper.NOTIFICATION_NEW_FRIEND_REQUEST)) {

			showNewFriendRequest(intent, context);
			sendNotificationBroadcast(context, type);

			if (appData.isNotificationsEnabled()) {
				String title = context.getString(R.string.new_friend_request);
				String body = context.getString(R.string.friend_request_from_arg, intent.getStringExtra(SENDER));
				AppUtils.showStatusBarNotification(context, title, body);
			}
		} else if (type.equals(GcmHelper.NOTIFICATION_NEW_MESSAGE)) {

			showNewMessage(intent, context);
			sendNotificationBroadcast(context, type);

			if (!DataHolder.getInstance().isMainActivityVisible() && appData.isNotificationsEnabled()) {
				String title = context.getString(R.string.new_message);
				String body = intent.getStringExtra(SENDER_USERNAME) + Symbol.COLON + Symbol.SPACE + intent.getStringExtra(MESSAGE);
				AppUtils.showStatusBarNotification(context, title, body);
			}
		} else if (type.equals(GcmHelper.NOTIFICATION_NEW_CHAT_MESSAGE)) {

			showNewChatMessage(intent, context);
			sendNotificationBroadcast(context, type);

			if (!DataHolder.getInstance().isMainActivityVisible() && appData.isNotificationsEnabled()) {
				String title = context.getString(R.string.new_chat_message);
				String body = intent.getStringExtra(SENDER) + Symbol.COLON + Symbol.SPACE + intent.getStringExtra(MESSAGE);
				AppUtils.showStatusBarNotification(context, title, body);
			}
		} else if (type.equals(GcmHelper.NOTIFICATION_GAME_OVER)) {  // Game over

			showGameOver(intent, context);
			sendNotificationBroadcast(context, type);

			if (appData.isNotificationsEnabled()) {
				String title = context.getString(R.string.game_is_over);
				String body = intent.getStringExtra(MESSAGE);
				AppUtils.showStatusBarNotification(context, title, body);
			}
		} else if (type.equals(GcmHelper.NOTIFICATION_NEW_CHALLENGE)) {

			showNewChallenge(intent, context);
			sendNotificationBroadcast(context, type);

			if (!DataHolder.getInstance().isMainActivityVisible() && appData.isNotificationsEnabled()) {
				String title = context.getString(R.string.new_daily_challenge);
				String body = context.getString(R.string.arg_wants_to_play, intent.getStringExtra(SENDER));
				AppUtils.showStatusBarNotification(context, title, body);

				// update user stats
				startService(new Intent(this, GetAndSaveUserStats.class));
			}
		} else if (type.equals(GcmHelper.NOTIFICATION_MOVE_MADE)) {
			List<YourTurnItem> moveNotifications = DbDataManager.getAllPlayMoveNotifications(getContentResolver(), username);

			moveNotifications = updateDailyGames(appData, username, getContentResolver(), moveNotifications);

			if (appData.isNotificationsEnabled()) {
				AppUtils.showNewMoveStatusNotification(context, moveNotifications, StaticData.MOVE_REQUEST_CODE);
			}

			context.sendBroadcast(new Intent(IntentConstants.USER_MOVE_UPDATE));
		}
	}

	private void sendNotificationBroadcast(Context context, String type) {
		Intent notifyIntent = new Intent(IntentConstants.NOTIFICATIONS_UPDATE);
		notifyIntent.putExtra(IntentConstants.TYPE, type);
		context.sendBroadcast(notifyIntent);
	}

	private synchronized void showNewFriendRequest(Intent intent, Context context) {
		NewFriendNotificationItem newFriendNotificationItem = new NewFriendNotificationItem();

		newFriendNotificationItem.setMessage(intent.getStringExtra(MESSAGE));
		newFriendNotificationItem.setUsername(intent.getStringExtra(SENDER));
		newFriendNotificationItem.setCreatedAt(Long.parseLong(intent.getStringExtra(CREATED_AT)));
		newFriendNotificationItem.setAvatar(intent.getStringExtra(AVATAR_URL));
		newFriendNotificationItem.setRequestId(Long.parseLong(intent.getStringExtra(REQUEST_ID)));
		LogMe.dl(TAG, " _________________________________");
		LogMe.dl(TAG, " NewFriendNotificationItem = " + new Gson().toJson(newFriendNotificationItem));

		ContentResolver contentResolver = context.getContentResolver();
		String username = new AppData(context).getUsername();

		DbDataManager.saveNewFriendRequest(contentResolver, newFriendNotificationItem, username);
	}

	private synchronized void showNewChatMessage(Intent intent, Context context) {
		NewChatNotificationItem chatNotificationItem = new NewChatNotificationItem();

		chatNotificationItem.setMessage(intent.getStringExtra(MESSAGE));
		chatNotificationItem.setUsername(intent.getStringExtra(SENDER));
		chatNotificationItem.setGameId(Long.parseLong(intent.getStringExtra(GAME_ID)));
		chatNotificationItem.setCreatedAt(Long.parseLong(intent.getStringExtra(CREATED_AT)));
		chatNotificationItem.setAvatar(intent.getStringExtra(AVATAR_URL));
		LogMe.dl(TAG, " _________________________________");
		LogMe.dl(TAG, " NewChatNotificationItem = " + new Gson().toJson(chatNotificationItem));

		ContentResolver contentResolver = context.getContentResolver();
		String username = new AppData(context).getUsername();

		DbDataManager.saveNewChatNotification(contentResolver, chatNotificationItem, username);
	}

	private synchronized void showNewMessage(Intent intent, Context context) {
		NewMessageNotificationItem messageNotificationItem = new NewMessageNotificationItem(intent);

		Log.d(TAG, " _________________________________");
		Log.d(TAG, " NewChatNotificationItem = " + new Gson().toJson(messageNotificationItem));

		ContentResolver contentResolver = context.getContentResolver();
		String username = new AppData(context).getUsername();
		DbDataManager.saveNewMessageNotification(contentResolver, messageNotificationItem, username);
	}

	private synchronized void showGameOver(Intent intent, Context context) {
		GameOverNotificationItem gameOverNotificationItem = new GameOverNotificationItem();

		long gameId = Long.parseLong(intent.getStringExtra(GAME_ID));
		gameOverNotificationItem.setMessage(intent.getStringExtra(MESSAGE));
		gameOverNotificationItem.setUsername(intent.getStringExtra(USERNAME));
		gameOverNotificationItem.setGameId(gameId);
		gameOverNotificationItem.setAvatar(intent.getStringExtra(AVATAR_URL));
		LogMe.dl(TAG, " _________________________________");
		LogMe.dl(TAG, " GameOverNotificationItem = " + new Gson().toJson(gameOverNotificationItem));
		ContentResolver contentResolver = context.getContentResolver();
		String username = new AppData(context).getUsername();

		DbDataManager.saveGameOverNotification(contentResolver, gameOverNotificationItem, username);
		// clear badge
		DbDataManager.deletePlayMoveNotification(getContentResolver(), username, gameId);
	}

	private synchronized void showNewChallenge(Intent intent, Context context) {
		NewChallengeNotificationItem challengeNotificationItem = new NewChallengeNotificationItem();

		challengeNotificationItem.setUsername(intent.getStringExtra(SENDER));
		challengeNotificationItem.setAvatar(intent.getStringExtra(AVATAR_URL));
		challengeNotificationItem.setChallengeId(Long.parseLong(intent.getStringExtra(CHALLENGE_ID)));
		LogMe.dl(TAG, " _________________________________");
		LogMe.dl(TAG, " NewChallengeNotificationItem = " + new Gson().toJson(challengeNotificationItem));
		ContentResolver contentResolver = context.getContentResolver();
		String username = new AppData(context).getUsername();
		DbDataManager.saveNewChallengeNotification(contentResolver, challengeNotificationItem, username);
	}

	private synchronized void showYouTurnNotification(Intent intent, Context context) {
		AppData appData = new AppData(context);
		String username = appData.getUsername();

		String lastMoveSan = intent.getStringExtra(LAST_MOVE_SAN);
		String opponentUsername = intent.getStringExtra(OPPONENT_USERNAME);
		String gameId = intent.getStringExtra(GAME_ID);

		LogMe.dl(TAG, " _________________________________");
		LogMe.dl(TAG, " LastMoveSan = " + lastMoveSan);
		LogMe.dl(TAG, " gameId = " + gameId);
		LogMe.dl(TAG, " opponentUsername = " + opponentUsername);
		LogMe.dl(TAG, " is inOnlineGame = " + DataHolder.getInstance().inOnlineGame(Long.parseLong(gameId)));

		// we use the same registerId for all users on a device, so check username to notify only the needed user
		if (opponentUsername.equalsIgnoreCase(username)) {
			return; // don't need notification of myself game
		}

		// Saving play move notification to DB
		ContentResolver contentResolver = context.getContentResolver();

		YourTurnItem yourTurnItem = new YourTurnItem(lastMoveSan, username, Long.parseLong(gameId));
		yourTurnItem.setOpponent(opponentUsername);

		DbDataManager.savePlayMoveNotification(contentResolver, username, yourTurnItem);

		// get all saved move notifications
		List<YourTurnItem> moveNotifications = DbDataManager.getAllPlayMoveNotifications(contentResolver, username);

		// update game states from server
		moveNotifications = updateDailyGames(appData, username, contentResolver, moveNotifications);

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
					new SoundPlayer(context).playMoveOpponent();
//					final MediaPlayer player = MediaPlayer.create(context, R.raw.move_opponent);
//					if (player != null) {
//						player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//							@Override
//							public void onCompletion(MediaPlayer mediaPlayer) {
//								player.stop();
//								player.release();
//							}
//						});
//						player.start();
//					}
				}
			}
		}
	}

	private List<YourTurnItem> updateDailyGames(AppData appData, String username, ContentResolver contentResolver,
												List<YourTurnItem> moveNotifications) {

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_GAMES_CURRENT);
		loadItem.addRequestParams(P_LOGIN_TOKEN, appData.getUserToken());

		DailyCurrentGamesItem item = null;
		List<DailyCurrentGameData> currentGamesList;
		try {
			item = RestHelper.getInstance().requestData(loadItem, DailyCurrentGamesItem.class, getApplicationContext());
		} catch (InternalErrorException e) {
			e.logMe();
		}

		if (item != null) {
			currentGamesList = item.getData();
			for (DailyCurrentGameData currentItem : currentGamesList) {
				DbDataManager.saveDailyGame(getContentResolver(), currentItem, username);
			}

			DbDataManager.checkAndDeleteNonExistCurrentGames(contentResolver, currentGamesList, username);
			List<YourTurnItem> notificationsToRemove = new ArrayList<YourTurnItem>();
			for (YourTurnItem turnItem : moveNotifications) {
				// compare with games list
				for (DailyCurrentGameData gameData : currentGamesList) {
					if (turnItem.getGameId() == gameData.getGameId()) {
						if (!gameData.isMyTurn()) {
							notificationsToRemove.add(turnItem);
							DbDataManager.deletePlayMoveNotification(contentResolver, username, gameData.getGameId());
						}
						break;
					}
				}
			}
			moveNotifications.removeAll(notificationsToRemove);
		}

		return moveNotifications;
	}

	@Override
	protected void onDeletedMessages(Context context, int total) {
		Log.d(TAG, "Received deleted messages notification, cnt = " + total);
	}

	@Override
	public void onError(Context context, String errorId) {
		LogMe.dl(TAG, "Received error: " + errorId);
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
				LogMe.dl(TAG, "Received error: " + errorId);
			}
		}
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		LogMe.dl(TAG, "Received recoverable error: " + errorId);
		return super.onRecoverableError(context, errorId);
	}

}
