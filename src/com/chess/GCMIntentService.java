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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.SystemClock;
import android.util.Log;
import com.chess.backend.GcmHelper;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.GSMServerResponseItem;
import com.chess.backend.entity.LastMoveInfoItem;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.PostJsonDataTask;
import com.chess.model.GameListCurrentItem;
import com.chess.utilities.AppUtils;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.google.gson.Gson;

import java.util.Random;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

	@SuppressWarnings("hiding")
	private static final String TAG = "GCMIntentService";
	private static final String TOKEN = Long.toBinaryString(new Random().nextLong());
	private Context context;
	private SharedPreferences preferences;

	public GCMIntentService() {
        super(GcmHelper.SENDER_ID);
		context = this;
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.d(TAG, "User = " + AppData.getUserName(context) + " Device registered: regId = " + registrationId);

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.GCM_REGISTER);
		loadItem.addRequestParams(RestHelper.GCM_P_ID, AppData.getUserToken(context));
		loadItem.addRequestParams(RestHelper.GCM_P_REGISTER_ID, registrationId);

		Log.d(TAG, "Registering to server, registrationId = " + registrationId
				+ " \ntoken = " + AppData.getUserToken(context));

		new PostJsonDataTask(new PostUpdateListener(GcmHelper.REQUEST_REGISTER)).execute(loadItem);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.d(TAG, "User = " + AppData.getUserName(context) + " Device unregistered, registrationId = " + registrationId);

        if (GCMRegistrar.isRegisteredOnServer(context)) {
			preferences = AppData.getPreferences(this);
			// TODO temporary unregister only if user loged out
			String realToken = preferences.getString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY);
			if (realToken.equals(StaticData.SYMBOL_EMPTY)) {

				String token = preferences.getString(AppConstants.PREF_TEMP_TOKEN_GCM, StaticData.SYMBOL_EMPTY);

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.GCM_UNREGISTER);
				loadItem.addRequestParams(RestHelper.GCM_P_ID, token);
				loadItem.addRequestParams(RestHelper.GCM_P_REGISTER_ID, registrationId);

				new PostJsonDataTask(new PostUpdateListener(GcmHelper.REQUEST_UNREGISTER)).execute(loadItem);
				Log.d(TAG, "Unregistering from server, registrationId = " + registrationId + "token = " + token);
			}
        } else {
            // This callback results from the call to unregister made on
            // GcmHelper when the registration to the server failed.
            Log.d(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.d(TAG, "User = " + AppData.getUserName(context) + " Received message");

		String type = intent.getStringExtra("type");

		if (type.equals(GcmHelper.NOTIFICATION_YOUR_MOVE)){
			Log.d(TAG, "received move notification, notifications enabled = " + AppData.isNotificationsEnabled(context));
			if (!AppData.isNotificationsEnabled(context))   // we check it here because we will use GCM for lists update, so it need to be registered.
				return;

			String gameId = intent.getStringExtra("game_id");
			Log.d("TEST", " received game info -> gameId = " + gameId);

			Log.d(TAG, "is inOnlineGame = " + DataHolder.getInstance().inOnlineGame(Long.parseLong(gameId)));
			context.sendBroadcast(new Intent(IntentConstants.USER_MOVE_UPDATE));
			if (!DataHolder.getInstance().inOnlineGame(Long.parseLong(gameId))) { // don't show notification
				showYouTurnNotification(intent);
			}
		}
	}

	private synchronized void showYouTurnNotification(Intent intent) {


		String lastMoveSan = intent.getStringExtra("last_move_san");
//			String opponentUserId = intent.getStringExtra("opponent_user_id");
//			String collapseKey = intent.getStringExtra("collapse_key");
		String opponentUsername = intent.getStringExtra("opponent_username");
		String gameId = intent.getStringExtra("game_id");

		boolean gameInfoWasFound = false;
		Log.d(TAG, " _________________________________");
		Log.d(TAG, " LastmoveSan = " + lastMoveSan);
		Log.d(TAG, " gameId = " + gameId);
		Log.d(TAG, " opponentUsername = " + opponentUsername);

		// we use the same registerId for all users on a device, so check username to notify the needed user
		if (opponentUsername.equalsIgnoreCase(AppData.getUserName(this))) {
			return; // don't need notificaion of myself game
		}
//		Log.d("TEST", " lastMoveInfoItems.size() = " + DataHolder.getInstance().getLastMoveInfoItems().size());

		// check if we already received that notification
		for (LastMoveInfoItem lastMoveInfoItem : DataHolder.getInstance().getLastMoveInfoItems()) {
			if (lastMoveInfoItem.getGameId().equals(gameId)) { // if have info about this game
				Log.d(TAG, " lastMoveInfoItem.getLastMoveSan().equals(lastMoveSan) = " + lastMoveInfoItem.getLastMoveSan().equals(lastMoveSan));
				if (lastMoveInfoItem.getLastMoveSan().equals(lastMoveSan)) { // if this game info already contains the same move update
					return; // no need to update
				} else { // if move info is different
					lastMoveInfoItem.setLastMoveSan(lastMoveSan);
				}
				gameInfoWasFound = true;
			}
		}

		if (!gameInfoWasFound) { // if we have no info about this game, then add last move to list of objects
			Log.d(TAG, " adding new game info" );
			LastMoveInfoItem lastMoveInfoItem = new LastMoveInfoItem();
			lastMoveInfoItem.setLastMoveSan(lastMoveSan);
			lastMoveInfoItem.setGameId(gameId);
		    DataHolder.getInstance().addLastMoveInfo(lastMoveInfoItem);
		}

		long gameTimeLeft = Long.parseLong(intent.getStringExtra("game_time_left"));

		long minutes = gameTimeLeft /60%60;
		long hours = gameTimeLeft /3600%24;
		long days = gameTimeLeft /86400;

		String remainingUnits;
		String remainingTime;

		if(days > 0){
			remainingUnits = "d";
			remainingTime = String.valueOf(days);
		} else if(hours > 0){
			remainingUnits = "h";
			remainingTime = String.valueOf(hours);
		} else {
			remainingUnits = "m";
			remainingTime = String.valueOf(minutes);
		}
		// compose gameInfoItem
		String[] gameInfoValues = new String[]{
				gameId,
				remainingTime,
				remainingUnits
		};

		GameListCurrentItem gameListItem = GameListCurrentItem.newInstance(gameInfoValues);

		AppUtils.showNewMoveStatusNotification(context,
				context.getString(R.string.your_move),
				context.getString(R.string.your_turn_in_game_with,
						opponentUsername,
						lastMoveSan),
				StaticData.MOVE_REQUEST_CODE,
				gameListItem);

		SharedPreferences preferences = AppData.getPreferences(context);
		boolean playSounds = preferences.getBoolean(AppData.getUserName(context) + AppConstants.PREF_SOUNDS, false);
		if(playSounds){
			final MediaPlayer player = MediaPlayer.create(context, R.raw.move_opponent);
			if(player != null){
				player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
					@Override
					public void onCompletion(MediaPlayer mediaPlayer) {
						player.release();
					}
				});
				player.start();
			}
		}
	}

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.d(TAG, "Received deleted messages notification, cnt = "+ total);
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

	private class PostUpdateListener extends AbstractUpdateListener<String>{
		private int requestCode;

		public PostUpdateListener(int requestCode) {
			super(GCMIntentService.this);
			this.requestCode = requestCode;
		}

		@Override
		public void updateData(String returnedObj) {
			super.updateData(returnedObj);
			GSMServerResponseItem responseItem = parseJson(returnedObj);
			String reqCode = requestCode == GcmHelper.REQUEST_REGISTER? "REGISTER": "UNREGISTER";
			Log.d(TAG, "REQUEST_" + reqCode + " \nResult = " + returnedObj );

			if(responseItem.getCode() < 400){
				switch (requestCode){
					case GcmHelper.REQUEST_REGISTER:
						GCMRegistrar.setRegisteredOnServer(context, true);
						break;
					case GcmHelper.REQUEST_UNREGISTER:
						GCMRegistrar.setRegisteredOnServer(context, false);
						// remove saved token
						SharedPreferences.Editor editor = preferences.edit();
						editor.putString(AppConstants.PREF_TEMP_TOKEN_GCM, StaticData.SYMBOL_EMPTY);
						editor.commit();
						break;
				}
			}
		}

		GSMServerResponseItem parseJson(String jRespString) {
			Gson gson = new Gson();
			return gson.fromJson(jRespString, GSMServerResponseItem.class);
		}
	}
}
