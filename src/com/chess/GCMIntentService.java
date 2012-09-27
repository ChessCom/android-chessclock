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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.util.Log;
import com.chess.backend.GcmHelper;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.GSMServerResponseItem;
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

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

	@SuppressWarnings("hiding")
	private static final String TAG = "GCMIntentService";
	private Context context;
	private SharedPreferences preferences;


	public GCMIntentService() {
        super(GcmHelper.SENDER_ID);
		context = this;
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.d(TAG, "Device registered: regId = " + registrationId);

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.GCM_REGISTER);
		loadItem.addRequestParams(RestHelper.GCM_P_ID, AppData.getUserToken(context));
		loadItem.addRequestParams(RestHelper.GCM_P_REGISTER_ID, registrationId);

		new PostJsonDataTask(new PostUpdateListener(GcmHelper.REQUEST_REGISTER)).execute(loadItem);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.d(TAG, "Device unregistered");

        if (GCMRegistrar.isRegisteredOnServer(context)) {
			preferences = AppData.getPreferences(this);
			String token = preferences.getString(AppConstants.PREF_TEMP_TOKEN_GCM, StaticData.SYMBOL_EMPTY);

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.GCM_UNREGISTER);
			loadItem.addRequestParams(RestHelper.GCM_P_ID, token);
			loadItem.addRequestParams(RestHelper.GCM_P_REGISTER_ID, registrationId);

			new PostJsonDataTask(new PostUpdateListener(GcmHelper.REQUEST_UNREGISTER)).execute(loadItem);
        } else {
            // This callback results from the call to unregister made on
            // GcmHelper when the registration to the server failed.
            Log.d(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.d(TAG, "Received message");


		String type = intent.getStringExtra("type");

		if (type.equals(GcmHelper.NOTIFICATION_YOUR_MOVE)){
			Log.d(TAG, "received move notification, notifications enabled = " + AppData.isNotificationsEnabled(context));
			if (!AppData.isNotificationsEnabled(context))   // we check it here because we will use GCM for lists update, so it need to be registered.
				return;


			String lastMoveSan = intent.getStringExtra("last_move_san");
//			String opponentUserId = intent.getStringExtra("opponent_user_id");
//			String collapseKey = intent.getStringExtra("collapse_key");
			String opponentUsername = intent.getStringExtra("opponent_username");
			long gameTimeLeft = Long.parseLong(intent.getStringExtra("game_time_left"));
			String gameId = intent.getStringExtra("game_id");
			Log.d("TEST", " receinved game info -> gameId = " + gameId);

			Log.d(TAG, "is inOnlineGame = " + DataHolder.getInstance().inOnlineGame(Long.parseLong(gameId)));
			if (DataHolder.getInstance().inOnlineGame(Long.parseLong(gameId))) { // update board
				context.sendBroadcast(new Intent(IntentConstants.USER_MOVE_UPDATE));
				return;
			}


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
	}

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.d(TAG, "Received deleted messages notification, cnt = "+ total);
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.d(TAG, "Received error: " + errorId);
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
