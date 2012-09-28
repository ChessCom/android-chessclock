package com.chess.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import com.chess.R;
import com.chess.backend.GcmHelper;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.GSMServerResponseItem;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.*;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.backend.tasks.PostJsonDataTask;
import com.chess.model.GameListCurrentItem;
import com.chess.ui.views.BackgroundChessDrawable;
import com.chess.utilities.AppUtils;
import com.chess.utilities.ChessComApiParser;
import com.flurry.android.FlurryAgent;
import com.google.android.gcm.GCMRegistrar;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * CommonLogicActivity class
 *
 * @author alien_roger
 * @created at: 23.09.12 8:10
 */
public abstract class CommonLogicActivity extends BaseFragmentActivity {

	protected static final int REQUEST_REGISTER = 11;
	private static final int REQUEST_UNREGISTER = 22;

	protected BackgroundChessDrawable backgroundChessDrawable;
	private String currentLocale;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		backgroundChessDrawable = new BackgroundChessDrawable(this);

		currentLocale = preferences.getString(AppConstants.CURRENT_LOCALE, StaticData.LOCALE_EN);
		setLocale();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (preferences.getLong(AppConstants.FIRST_TIME_START, 0) == 0) {
			preferencesEditor.putLong(AppConstants.FIRST_TIME_START, System.currentTimeMillis());
			preferencesEditor.putInt(AppConstants.ADS_SHOW_COUNTER, 0);
			preferencesEditor.commit();
		}

		if(!currentLocale.equals(getResources().getConfiguration().locale.getLanguage())){
			restartActivity();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, FlurryData.API_KEY);
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	protected void setLocale(){
		String prevLang = getResources().getConfiguration().locale.getLanguage();
		String[] languageCodes = getResources().getStringArray(R.array.languages_codes);

		String setLocale = languageCodes[AppData.getLanguageCode(this)];
		if(!prevLang.equals(setLocale)) {
			Locale locale = new Locale(setLocale);
			Locale.setDefault(locale);
			Configuration config = new Configuration();
			config.locale = locale;
			getResources().updateConfiguration(config, getResources().getDisplayMetrics());

			preferencesEditor.putString(AppConstants.CURRENT_LOCALE, setLocale);
			preferencesEditor.commit();

			currentLocale = setLocale;

			restartActivity();
		}
	}

	protected void restartActivity(){
		Intent intent = getIntent();
		finish();
		startActivity(intent);
		Log.d("TEST", "___restartActivity___");
	}

	protected List<String> getItemsFromEntries(int entries){
		String[] array = getResources().getStringArray(entries);
		return getItemsFromArray(array);
	}

	protected List<String> getItemsFromArray(String[] array){
		List<String> items = new ArrayList<String>();
		items.addAll(Arrays.asList(array));
		return items;
	}

	protected void registerGcmService(){
		if (!AppData.isNotificationsEnabled(this)) // no need to register if user turned off notifications
			return;

		// Make sure the device has the proper dependencies.
//		GCMRegistrar.checkDevice(this); // don't check for emulator
		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
//		GCMRegistrar.checkManifest(this);

		/* When an application is updated, it should invalidate its existing registration ID.
		The best way to achieve this validation is by storing the current
		 application version when a registration ID is stored.
		 Then when the application is started, compare the stored value
		 with the current application version.
		 If they do not match, invalidate the stored data and start the registration process again.
		 */

		final String registrationId = GCMRegistrar.getRegistrationId(this);
		if (registrationId.equals("")) {
			// Automatically registers application on startup.
			GCMRegistrar.register(this, GcmHelper.SENDER_ID);
			Log.d("TEST", " no regId - > GCMRegistrar.register");
		} else {
			// Device is already registered on GCM, check server.
			if (GCMRegistrar.isRegisteredOnServer(this)) {
				// Skips registration.
				Log.d("TEST", "already registered");
			} else {
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.GCM_REGISTER);
				loadItem.addRequestParams(RestHelper.GCM_P_ID, AppData.getUserToken(this));
				loadItem.addRequestParams(RestHelper.GCM_P_REGISTER_ID, registrationId);

				new PostJsonDataTask(new PostUpdateListener(REQUEST_REGISTER)).execute(loadItem);
				Log.d("TEST", " manual register -> PostJsonDataTask");
			}
		}
	}

	protected void unregisterGcmService(){
		// save token to unregister from server
		preferencesEditor.putString(AppConstants.PREF_TEMP_TOKEN_GCM, AppData.getUserToken(this));
		preferencesEditor.commit();
		GCMRegistrar.unregister(this);
	}

	protected class PostUpdateListener extends AbstractUpdateListener<String> {
		private int requestCode;

		public PostUpdateListener(int requestCode) {
			super(CommonLogicActivity.this);
			this.requestCode = requestCode;
		}

		@Override
		public void updateData(String returnedObj) {
			super.updateData(returnedObj);
			Log.d("TEST", "PostUpdateListener -> updateDate = " + returnedObj);

			GSMServerResponseItem responseItem = parseJson(returnedObj);

			if(responseItem.getCode() < 400){
				switch (requestCode){
					case REQUEST_REGISTER:
						GCMRegistrar.setRegisteredOnServer(getContext(), true);
						break;
					case REQUEST_UNREGISTER:
						GCMRegistrar.setRegisteredOnServer(getContext(), false);
						break;
				}
			}
		}

		GSMServerResponseItem parseJson(String jRespString) {
			Gson gson = new Gson();
			return gson.fromJson(jRespString, GSMServerResponseItem.class);
		}
	}


	protected void checkMove(){
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.ECHESS_CURRENT_GAMES);
		loadItem.addRequestParams(RestHelper.P_ID, AppData.getUserToken(this));
		loadItem.addRequestParams(RestHelper.P_ALL, RestHelper.V_ONLY_USER_TURN);
		new GetStringObjTask(new CheckMoveUpdateListener()).executeTask(loadItem);
	}

	private class CheckMoveUpdateListener extends AbstractUpdateListener<String> {
		public CheckMoveUpdateListener() {
			super(getContext());
		}

		@Override
		public void updateData(String returnedObj) {
			if (returnedObj.contains(RestHelper.R_SUCCESS)) {
				int haveMoves = 0;
				List<GameListCurrentItem> itemList = ChessComApiParser.getCurrentOnlineGames(returnedObj);

				if(itemList.size() == 1) { // only one icon
					GameListCurrentItem gameListItem = itemList.get(0);

					AppUtils.showNewMoveStatusNotification(getMeContext(),
							getMeContext().getString(R.string.your_move),
							getMeContext().getString(R.string.your_turn_in_game_with,
									gameListItem.getOpponentUsername(),
									gameListItem.getLastMoveFromSquare() + gameListItem.getLastMoveToSquare()),
							StaticData.MOVE_REQUEST_CODE,
							gameListItem);
					haveMoves++;
				} else if(itemList.size() > 0) {
					for (GameListCurrentItem currentItem : itemList) {
						AppUtils.cancelNotification(getMeContext(), (int) currentItem.getGameId());
					}

					AppUtils.showMoveStatusNotification(getMeContext(),
							getMeContext().getString(R.string.your_turn),
							getMeContext().getString(R.string.your_move) + StaticData.SYMBOL_SPACE
									+ StaticData.SYMBOL_LEFT_PAR + itemList.size() + StaticData.SYMBOL_RIGHT_PAR,
							0, OnlineScreenActivity.class);
					getMeContext().sendBroadcast(new Intent(IntentConstants.USER_MOVE_UPDATE));
				}

				if(haveMoves == 1){ // play for one
					SharedPreferences preferences = AppData.getPreferences(getMeContext());
					boolean playSounds = preferences.getBoolean(AppData.getUserName(getMeContext()) + AppConstants.PREF_SOUNDS, false);
					if(playSounds){
						final MediaPlayer player = MediaPlayer.create(getMeContext(), R.raw.move_opponent);
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


					getMeContext().sendBroadcast(new Intent(IntentConstants.USER_MOVE_UPDATE));
				}
			}
		}
	}
}
