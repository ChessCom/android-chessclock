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
import android.widget.Toast;
import com.chess.backend.GcmHelper;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.LastMoveInfoItem;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.GcmItem;
import com.chess.backend.exceptions.InternalErrorException;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.model.BaseGameItem;
import com.chess.model.GameListCurrentItem;
import com.chess.utilities.AppUtils;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

import java.util.Random;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

	@SuppressWarnings("hiding")
	private static final String TAG = "GCMIntentService";
	private static final String TOKEN = Long.toBinaryString(new Random().nextLong());
	public static final String OBJECT_SYMBOL = "{";
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
//		loadItem.setLoadPath(RestHelper.GCM_REGISTER);
		loadItem.setLoadPath(RestHelper.CMD_GCM);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(context));
		loadItem.addRequestParams(RestHelper.GCM_P_REGISTER_ID, registrationId);

		Log.d(TAG, "Registering to server, registrationId = " + registrationId
				+ " \ntoken = " + AppData.getUserToken(context));

//		String url = RestHelper.formPostRequest(loadItem);
//		postData(url, loadItem, GcmHelper.REQUEST_REGISTER);

		GcmItem item = null;
		try {
			item = RestHelper.requestData(loadItem, GcmItem.class);
		} catch (InternalErrorException e) {
			e.logMe();
		}

		if (item != null && item.getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
			GCMRegistrar.setRegisteredOnServer(context, true);
			AppData.registerOnChessGCM(context, AppData.getUserToken(context));
		} else {
			if (context != null) {
				Toast.makeText(context, R.string.gcm_not_registered, Toast.LENGTH_SHORT).show();
			}
		}
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
//				loadItem.setLoadPath(RestHelper.GCM_UNREGISTER);
				loadItem.setLoadPath(RestHelper.CMD_GCM);
				loadItem.setRequestMethod(RestHelper.DELETE);
				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, token);
//				loadItem.addRequestParams(RestHelper.GCM_P_REGISTER_ID, registrationId);

//				String url = RestHelper.formPostRequest(loadItem); // TODO check
//				postData(url, loadItem, GcmHelper.REQUEST_UNREGISTER);

				GcmItem item = null;
				try {
					item = RestHelper.requestData(loadItem, GcmItem.class);
				} catch (InternalErrorException e) {
					e.logMe();
				}

				if (item != null && item.getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
					GCMRegistrar.setRegisteredOnServer(context, false);
					AppData.unRegisterOnChessGCM(context);
					// remove saved token
					SharedPreferences.Editor editor = preferences.edit();
					editor.putString(AppConstants.PREF_TEMP_TOKEN_GCM, StaticData.SYMBOL_EMPTY);
					editor.commit();
				}

//				new PostJsonDataTask(new PostUpdateListener(GcmHelper.REQUEST_UNREGISTER)).execute(loadItem);// don't need as we are on worker thread alreay
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

		if (type.equals(GcmHelper.NOTIFICATION_YOUR_MOVE)) {
			Log.d(TAG, "received move notification, notifications enabled = " + AppData.isNotificationsEnabled(context));
			if (!AppData.isNotificationsEnabled(context))   // we check it here because we will use GCM for lists update, so it need to be registered.
				return;

			showYouTurnNotification(intent);
		}
	}

	private synchronized void showYouTurnNotification(Intent intent) {

		String lastMoveSan = intent.getStringExtra("last_move_san");
//			String opponentUserId = intent.getStringExtra("opponent_user_id");
//			String collapseKey = intent.getStringExtra("collapse_key");
		String opponentUsername = intent.getStringExtra("opponent_username");
		String gameId = intent.getStringExtra("game_id");

		boolean gameInfoFound = false;
		Log.d(TAG, " _________________________________");
		Log.d(TAG, " LastMoveSan = " + lastMoveSan);
		Log.d(TAG, " gameId = " + gameId);
		Log.d(TAG, " opponentUsername = " + opponentUsername);
		Log.d(TAG, " is inOnlineGame = " + DataHolder.getInstance().inOnlineGame(Long.parseLong(gameId)));

		// we use the same registerId for all users on a device, so check username to notify only the needed user
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
				gameInfoFound = true;
			}
		}

		if (!gameInfoFound) { // if we have no info about this game, then add last move to list of objects
			Log.d(TAG, " adding new game info");
			LastMoveInfoItem lastMoveInfoItem = new LastMoveInfoItem();
			lastMoveInfoItem.setLastMoveSan(lastMoveSan);
			lastMoveInfoItem.setGameId(gameId);
			DataHolder.getInstance().addLastMoveInfo(lastMoveInfoItem);
		}

		if (DataHolder.getInstance().inOnlineGame(Long.parseLong(gameId))) { // don't show notification
			Log.d(TAG, " updating board");
			Intent gameUpdateIntent = new Intent(IntentConstants.BOARD_UPDATE);
			gameUpdateIntent.putExtra(BaseGameItem.GAME_ID, Long.parseLong(gameId));
			context.sendBroadcast(gameUpdateIntent);
		} else {
			context.sendBroadcast(new Intent(IntentConstants.USER_MOVE_UPDATE));

			long gameTimeLeft = Long.parseLong(intent.getStringExtra("game_time_left"));

			long minutes = gameTimeLeft / 60 % 60;
			long hours = gameTimeLeft / 3600 % 24;
			long days = gameTimeLeft / 86400;

			String remainingUnits;
			String remainingTime;

			if (days > 0) {
				remainingUnits = "d";
				remainingTime = String.valueOf(days);
			} else if (hours > 0) {
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
			if (playSounds) {
				final MediaPlayer player = MediaPlayer.create(context, R.raw.move_opponent);
				if (player != null) {
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

//	private void postData(String url, LoadItem loadItem, int requestCode) {
//		int result = StaticData.EMPTY_DATA;
//		GcmItem item = null;
//
////		String url = RestHelper.formCustomRequest(loadItem);
//		if (loadItem.getRequestMethod().equals(RestHelper.POST)){
//			url = RestHelper.formPostRequest(loadItem);
//		}
//		Log.d(TAG, "retrieving from url = " + url);
//
//		long tag = System.currentTimeMillis();
//		BugSenseHandler.addCrashExtraData(AppConstants.BUGSENSE_DEBUG_APP_API_REQUEST, "tag=" + tag + " " + url);
//
//		HttpURLConnection connection = null;
//		try {
//			URL urlObj = new URL(url);
//			connection = (HttpURLConnection) urlObj.openConnection();
//			connection.setRequestMethod(loadItem.getRequestMethod());
//
//			if (RestHelper.IS_TEST_SERVER_MODE) {
//				Authenticator.setDefault(new Authenticator() {
//					protected PasswordAuthentication getPasswordAuthentication() {
//						return new PasswordAuthentication(RestHelper.V_TEST_NAME, RestHelper.V_TEST_NAME2.toCharArray());
//					}
//				});
//			}
//
//			if (loadItem.getRequestMethod().equals(RestHelper.POST)){
//				submitPostData(connection, loadItem);
//			}
//
//			final int statusCode = connection.getResponseCode();
//			if (statusCode != HttpStatus.SC_OK) {
//				Log.e(TAG, "Error " + statusCode + " while retrieving data from " + url);
//
//				InputStream inputStream = connection.getErrorStream();
//				String resultString = AppUtils.convertStreamToString(inputStream);
//				BaseResponseItem baseResponse = parseJson(resultString, BaseResponseItem.class);
//				Log.d(TAG, "Code: " + baseResponse.getCode() + " Message: " + baseResponse.getMessage());
//				result =  RestHelper.encodeServerCode(baseResponse.getCode());
//			}
//
//			InputStream inputStream = null;
//			String resultString = null;
//			try {
//				inputStream = connection.getInputStream();
//
//				resultString = AppUtils.convertStreamToString(inputStream);
//				BaseResponseItem baseResponse = parseJson(resultString, BaseResponseItem.class);
//				if (baseResponse.getStatus().equals(RestHelper.R_STATUS_SUCCESS)) {
//					item = parseJson(resultString);
//					if(item != null) {
//						result = StaticData.RESULT_OK;
//					}
//
//				}
//			} finally {
//				if (inputStream != null) {
//					inputStream.closeBoard();
//				}
//			}
//
//			result = StaticData.RESULT_OK;
//			Log.d(TAG, "WebRequest SERVER RESPONSE: " + resultString);
//			BugSenseHandler.addCrashExtraData(AppConstants.BUGSENSE_DEBUG_APP_API_RESPONSE, "tag=" + tag + " " + resultString);
//
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//			result = StaticData.INTERNAL_ERROR;
//		} catch (JsonSyntaxException e) {
//			e.printStackTrace();
//			result = StaticData.INTERNAL_ERROR;
//		} catch (IOException e) {
//			Log.e(TAG, "I/O error while retrieving data from " + url, e);
//			result = StaticData.NO_NETWORK;
//		} catch (IllegalStateException e) {
//			Log.e(TAG, "Incorrect URL: " + url, e);
//			result = StaticData.UNKNOWN_ERROR;
//		} catch (Exception e) {
//			Log.e(TAG, "Error while retrieving data from " + url, e);
//			result = StaticData.UNKNOWN_ERROR;
//		} finally {
//			if (connection != null) {
//				connection.disconnect();
//			}
//		}
////		return result;
//
//		if (result == StaticData.RESULT_OK /*&& returnedObj.startsWith(OBJECT_SYMBOL)*/) {
////			GCMServerResponseItem responseItem = parseJson(returnedObj);
//
//		}
//	}

//	private void submitPostData(URLConnection connection, LoadItem loadItem) throws IOException {
//		String query = RestHelper.formPostData(loadItem);
//		String charset = HTTP.UTF_8;
//		connection.setDoOutput(true); // Triggers POST.
////		connection.setRequestProperty("Accept-Charset", charset);
//		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + charset);
//		OutputStream output = null;
//		try {
//			output = connection.getOutputStream();
//			output.write(query.getBytes(charset));
//		} finally {
//			if (output != null) try {
//				output.closeBoard();
//			} catch (IOException ex) {
//				Log.e(TAG, "Error while submiting POST data " + ex.toString());
//			}
//		}
//
//	}

//	private String formJsonData(List<NameValuePair> requestParams) {
//		StringBuilder data = new StringBuilder();
//		String separator = StaticData.SYMBOL_EMPTY;
//		data.append("{");
//		for (NameValuePair requestParam : requestParams) {
//
//			data.append(separator);
//			separator = StaticData.SYMBOL_COMMA;
//			data.append("\"")
//					.append(requestParam.getName()).append("\"")
//					.append(":")
//					.append("\"")
//					.append(requestParam.getValue())
//					.append("\"");
//		}
//		data.append("}");
//		return data.toString();
//	}

//	private GcmItem parseJson(String jRespString) {
//		Gson gson = new Gson();
//		return gson.fromJson(jRespString, GcmItem.class);
//	}
//
//	private <CustomType> CustomType parseJson(String jRespString, Class<CustomType> clazz) {
//		Gson gson = new Gson();
//		return gson.fromJson(jRespString, clazz);
//	}

//	GCMServerResponseItem parseJson(String jRespString) {
//		Gson gson = new Gson();
//		try {
//            return gson.fromJson(jRespString, GCMServerResponseItem.class);
//        }catch(JsonSyntaxException ex) {
//            ex.printStackTrace(); // in case you want to see the stacktrace in your log cat output
//            BugSenseHandler.addCrashExtraData("GCM Server Response Item", jRespString);
//            BugSenseHandler.sendException(ex);
//            return GCMServerResponseItem.createFailResponse();
//        }
//	}
}
