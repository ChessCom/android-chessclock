package com.chess.ui.core;

import actionbarcompat.ActionBarActivity;
import actionbarcompat.ActionBarHelper;
import android.app.AlertDialog;
import android.content.*;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.TextView;
import android.widget.Toast;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.Web;
import com.chess.backend.WebService;
import com.chess.backend.entity.SoundPlayer;
import com.chess.backend.statics.*;
import com.chess.backend.tasks.CheckUpdateTask;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Game;
import com.chess.model.GameItem;
import com.chess.model.PopupItem;
import com.chess.ui.activities.HomeScreenActivity;
import com.chess.ui.activities.LoginScreenActivity;
import com.chess.ui.fragments.PopupDialogFragment;
import com.chess.ui.interfaces.ActiveFragmentInterface;
import com.chess.ui.interfaces.BoardToGameActivityFace;
import com.chess.ui.interfaces.LccConnectionListener;
import com.chess.ui.interfaces.PopupDialogFace;
import com.chess.ui.views.BackgroundChessDrawable;
import com.chess.utilities.MyProgressDialog;
import com.flurry.android.FlurryAgent;
import com.mopub.mobileads.MoPubView;

import java.util.ArrayList;
import java.util.List;

public abstract class CoreActivityActionBar extends ActionBarActivity implements BoardToGameActivityFace,
		ActiveFragmentInterface, PopupDialogFace,LccConnectionListener {

	protected final static int INIT_ACTIVITY = -1;
	protected final static int ERROR_SERVER_RESPONSE = -2;
	private static final String INFO_MSG_TAG = "info message";

	/**
	 * Use context instead
	 */
//	@Deprecated
	protected MainApp mainApp;
	protected Bundle extras;
	protected DisplayMetrics metrics;
	protected MyProgressDialog progressDialog;
	protected LccHolder lccHolder;
	protected String response = StaticData.SYMBOL_EMPTY; // TODO remove
	protected String responseRepeatable = StaticData.SYMBOL_EMPTY; // TODO remove
	protected BackgroundChessDrawable backgroundChessDrawable;
	protected PopupItem popupItem;
	protected List<PopupDialogFragment> popupManager;

	protected Handler handler;
	protected SharedPreferences preferences;
	protected SharedPreferences.Editor preferencesEditor;

	public boolean mIsBound;
	public WebService appService = null;

    // we may have this add on every screen, so control it on the lowest level
    protected MoPubView moPubView;

	public abstract void update(int code);

	public void setFullScreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		handler = new Handler();
		backgroundChessDrawable = new BackgroundChessDrawable(this);

		popupItem = new PopupItem();
		popupManager = new ArrayList<PopupDialogFragment>();

		mainApp = (MainApp) getApplication();
		extras = getIntent().getExtras();

		// get global Shared Preferences
//		if (preferences == null) {
//			mainApp.setSharedData(AppData.getPreferences(this));
//			mainApp.setSharedDataEditor(preferences.edit());
//		}

		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		lccHolder = mainApp.getLccHolder();
		lccHolder.setExternalConnectionListener(this);

		preferences = AppData.getPreferences(this);
		preferencesEditor = preferences.edit();
	}

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        View mainView = findViewById(R.id.mainView);
        if(mainView != null)
            mainView.setBackgroundDrawable(backgroundChessDrawable);
    }

    protected void widgetsInit(){

    }

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		backgroundChessDrawable.updateConfig();
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// try to destroy ad here as Mopub team suggested
		if (moPubView != null) {
			moPubView.destroy();
		}

		doUnbindService();
	}

	@Override
	public void switchFragment(Fragment fragment) {

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.replace(R.id.activeContent, fragment);
		ft.addToBackStack(null);
		ft.commit();
	}

	/*
	 * public boolean isConnected(){ ConnectivityManager cm =
	 * (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE); NetworkInfo
	 * NI = cm.getActiveNetworkInfo(); if(NI == null) return false; else return
	 * NI.isConnectedOrConnecting(); }
	 */
	public boolean doBindService() {
		mIsBound = bindService(new Intent(this, WebService.class), onService,
				Context.BIND_AUTO_CREATE);
		return mIsBound;
	}

	public void doUnbindService() {
		if (mIsBound) {
			unbindService(onService);
			mIsBound = false;
		}
	}

	public ServiceConnection onService = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder rawBinder) {
			appService = ((WebService.LocalBinder) rawBinder).getService();
			update(INIT_ACTIVITY);
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			appService = null;
		}
	};

	private class ReconnectTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			final LccHolder lccHolder = mainApp.getLccHolder();
			// lccHolder.setConnectingInProgress(true);
			lccHolder.getClient().disconnect();
			lccHolder.setNetworkTypeName(null);
			lccHolder.setConnectingInProgress(true);

			final String password = preferences.getString(AppConstants.PASSWORD, StaticData.SYMBOL_EMPTY);
			if (password.equals(StaticData.SYMBOL_EMPTY)) {
				lccHolder.getClient().connect(
					preferences.getString(AppConstants.USER_SESSION_ID, StaticData.SYMBOL_EMPTY),
					lccHolder.getConnectionListener());
			}else {
				lccHolder.getClient().connect(
						AppData.getUserName(getContext()),
								AppData.getPassword(getContext())

					/*preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY),
					preferences.getString(AppConstants.PASSWORD, StaticData.SYMBOL_EMPTY)*/,
					lccHolder.getConnectionListener());
			}
			/*
								 * appService.RunRepeatble(0, 0, 120000, progressDialog =
								 * MyProgressDialog.show(this, null,
								 * getString(R.string.updatinggameslist), true));
								 */
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			lccHolder.updateConnectionState();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

//		boolean resetDetected = false;
		if (mainApp.getBoardBitmap() == null) {
			mainApp.loadBoard(mainApp.res_boards[preferences.getInt(
					AppData.getUserName(getContext())
							+ AppConstants.PREF_BOARD_TYPE, 0)]);

//			resetDetected = true;
		}

		if (mainApp.getPiecesBitmaps() == null) {
			mainApp.loadPieces(preferences.getInt(AppData.getUserName(getContext())
					+ AppConstants.PREF_PIECES_SET, 0));

//			resetDetected = true;
		}

//		if (resetDetected) {
//			checkUserTokenAndStartActivity();
//		}

		final MyProgressDialog reconnectingIndicator = lccHolder.getAndroidStuff().getReconnectingIndicator();
		if (!lccHolder.isConnectingInProgress() && reconnectingIndicator != null) {
			reconnectingIndicator.dismiss();
			lccHolder.getAndroidStuff().setReconnectingIndicator(null);
		}

		if (mainApp.isLiveChess() && !lccHolder.isConnected() && !lccHolder.isConnectingInProgress()) {
			// lccHolder.getAndroidStuff().showConnectingIndicator();
			manageConnectingIndicator(true, "Loading Live Chess");

			new ReconnectTask().execute();
		}
		doBindService();
		registerReceivers();

		if (preferences.getLong(AppConstants.FIRST_TIME_START, 0) == 0) {
			preferencesEditor.putLong(AppConstants.FIRST_TIME_START, System.currentTimeMillis());
			preferencesEditor.putInt(AppConstants.ADS_SHOW_COUNTER, 0);
			preferencesEditor.commit();
		}
		long startDay = preferences.getLong(AppConstants.START_DAY, 0);
		if (startDay == 0 || !DateUtils.isToday(startDay)) {
			checkUpdate();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		doUnbindService();
		if (appService != null && appService.getRepeatableTimer() != null) {
			appService.stopSelf();
			appService.getRepeatableTimer().cancel();
			appService = null;
		}
		unRegisterReceivers();

		// todo: how to logout user when he/she is switching to another
// activity?
		/*
		 * if (mainApp.isLiveChess() && lccHolder.isConnected()) {
		 * lccHolder.logout(); }
		 */

		preferencesEditor.putLong(AppConstants.LAST_ACTIVITY_PAUSED_TIME, System.currentTimeMillis());
		preferencesEditor.commit();

		//mainApp.setForceBannerAdOnFailedLoad(false);

		if (progressDialog != null)
			progressDialog.dismiss();
	}

	private void registerReceivers(){
		registerReceiver(receiver, new IntentFilter(IntentConstants.BROADCAST_ACTION));
		registerReceiver(lccLoggingInInfoReceiver, new IntentFilter(IntentConstants.FILTER_LOGINING_INFO));
		registerReceiver(lccReconnectingInfoReceiver, new IntentFilter(IntentConstants.FILTER_RECONNECT_INFO));
		registerReceiver(drawOfferedMessageReceiver, new IntentFilter(IntentConstants.FILTER_DRAW_OFFERED));
		registerReceiver(informAndExitReceiver, new IntentFilter(IntentConstants.FILTER_EXIT_INFO));
		registerReceiver(obsoleteProtocolVersionReceiver, new IntentFilter(IntentConstants.FILTER_PROTOCOL_VERSION));
		registerReceiver(infoMessageReceiver, new IntentFilter(IntentConstants.FILTER_INFO));
	}

	private void unRegisterReceivers(){
		unregisterReceiver(receiver);
		unregisterReceiver(drawOfferedMessageReceiver);
		unregisterReceiver(lccLoggingInInfoReceiver);
		unregisterReceiver(lccReconnectingInfoReceiver);
		unregisterReceiver(informAndExitReceiver);
		unregisterReceiver(obsoleteProtocolVersionReceiver);
		unregisterReceiver(infoMessageReceiver);
	}

	protected void backToHomeActivity(){
		Intent intent = new Intent(this, HomeScreenActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	public void dismissAllPopups(){
		for (PopupDialogFragment fragment : popupManager) {
			fragment.getDialog().dismiss();
		}
		popupManager.clear();
	}

	@Override
	public void onConnected(boolean connected) {
		Log.d("TEST", "Live Screen onLccConnected, lcc connect state = "
				+ LccHolder.getInstance(this).isConnected());
		getActionBarHelper().showMenuItemById(R.id.menu_singOut, connected);
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		fragment.getDialog().dismiss();
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		fragment.getDialog().dismiss();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				backToHomeActivity();
				break;
		}
		return super.onOptionsItemSelected(item);
	}	
	
	private void checkUserTokenAndStartActivity() {
		Log.d("TEST", "Reset detected!");
		if (!AppData.getUserName(getContext()).equals(StaticData.SYMBOL_EMPTY)) {
			Intent intent = new Intent(this, HomeScreenActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} else {
			startActivity(new Intent(this, LoginScreenActivity.class));
		}
	}

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// getting extras
			Bundle rExtras = intent.getExtras();
			boolean repeatable;
			String resp;
			int retCode;
			try {
				repeatable = rExtras.getBoolean(AppConstants.REPEATABLE_TASK);
				resp = rExtras.getString(AppConstants.REQUEST_RESULT);
				if (repeatable) {
					responseRepeatable = resp;
				} else {
					response = resp;
				}

				retCode = rExtras.getInt(AppConstants.CALLBACK_CODE);
			} catch (Exception e) {
				update(ERROR_SERVER_RESPONSE);
				return;
			}

			if (Web.getStatusCode() == -1)
				mainApp.noInternet = true;
			else {
				if (mainApp.noInternet) {
					mainApp.offline = false;
				}
				mainApp.noInternet = false;
			}

			if (resp.contains(RestHelper.R_SUCCESS))
				update(retCode);
			else {
				if (resp.length() == 0) {
					update(ERROR_SERVER_RESPONSE);
					return;
				}
				String title = getString(R.string.error);
				String message;
				if (resp.contains(RestHelper.R_ERROR)) {
					message = resp.split("[+]")[1];
				} else {
					update(ERROR_SERVER_RESPONSE);
					return;
				}
				if (message == null || message.trim().equals(StaticData.SYMBOL_EMPTY)) {
					update(ERROR_SERVER_RESPONSE);
					return;
				}
				new AlertDialog.Builder(CoreActivityActionBar.this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(title)
						.setMessage(message)
						.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int whichButton) {
								update(ERROR_SERVER_RESPONSE);
							}
						}).create().show();
			}
		}
	};

	private final BroadcastReceiver drawOfferedMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LccHolder.LOG.info(AppConstants.LCCLOG_ANDROID_RECEIVE_BROADCAST_INTENT_ACTION + intent.getAction());
			final Game game = mainApp.getLccHolder().getGame(mainApp.getGameId()); // TODO remove final and pass like argument
			// TODO make popUpFragmentDialog
			final AlertDialog alertDialog = new AlertDialog.Builder(CoreActivityActionBar.this)
					// .setTitle(intent.getExtras().getString(AppConstants.TITLE))
					.setMessage(intent.getExtras().getString(AppConstants.MESSAGE))
					.setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							mainApp.getLccHolder().getAndroidStuff().runMakeDrawTask(game);
						}
					}).setNeutralButton(getString(R.string.decline), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							lccHolder.getAndroidStuff().runRejectDrawTask(game);
						}
					})
							/*
												 * .setNegativeButton(getString(R.string.cancel), new
												 * DialogInterface.OnClickListener() { public void
												 * onClick(DialogInterface dialog, int whichButton) {
												 * startActivity(new Intent(CoreActivity.this, Game.class).
												 * putExtra(AppConstants.GAME_MODE, 4).
												 * putExtra(GameListItem.GAME_ID,
												 * el.values.get(GameListItem.GAME_ID))); } })
												 */
					.create();
			alertDialog.setCanceledOnTouchOutside(true);
			alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialogInterface) {
					lccHolder.getAndroidStuff().runRejectDrawTask(game);
				}
			});
			alertDialog.getWindow().setGravity(Gravity.BOTTOM);
			alertDialog.show();
		}
	};

	public BroadcastReceiver lccReconnectingInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mainApp.isLiveChess()) {
				LccHolder.LOG.info(AppConstants.LCCLOG_ANDROID_RECEIVE_BROADCAST_INTENT_ACTION + intent.getAction()
						+ ", enable=" + intent.getExtras().getBoolean(AppConstants.ENABLE_LIVE_CONNECTING_INDICATOR));
				MyProgressDialog reconnectingIndicator = lccHolder.getAndroidStuff().getReconnectingIndicator();
				boolean enable = intent.getExtras().getBoolean(AppConstants.ENABLE_LIVE_CONNECTING_INDICATOR);

				if (reconnectingIndicator != null) {
					reconnectingIndicator.dismiss();
					lccHolder.getAndroidStuff().setReconnectingIndicator(null);
				}
				if (enable) {
					/*if (MobclixHelper.isShowAds(mainApp) && MobclixHelper.getBannerAdview(mainApp) != null
							&& !mainApp.isAdviewPaused()) {
						MobclixHelper.pauseAdview(MobclixHelper.getBannerAdview(mainApp), mainApp);
					}*/
					reconnectingIndicator = new MyProgressDialog(context);
					reconnectingIndicator.setMessage(intent.getExtras().getString(AppConstants.MESSAGE));
					reconnectingIndicator.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							lccHolder.logout();
							final Intent intent = new Intent(CoreActivityActionBar.this, HomeScreenActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							// reconnectingIndicator.dismiss();
							mainApp.startActivity(intent);
						}
					});
					reconnectingIndicator.setCancelable(true);
					reconnectingIndicator.setIndeterminate(true);
					try {
						if(!isFinishing()) // TODO check
							reconnectingIndicator.show();
						lccHolder.getAndroidStuff().setReconnectingIndicator(reconnectingIndicator);
					} catch (Exception e) {
						lccHolder.logout();
						intent = new Intent(CoreActivityActionBar.this, HomeScreenActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						// reconnectingIndicator.dismiss();
						mainApp.startActivity(intent);
					}
				} else {
					/*if (MobclixHelper.isShowAds(mainApp) && MobclixHelper.getBannerAdview(mainApp) != null
							&& mainApp.isAdviewPaused()) {
						MobclixHelper.resumeAdview(MobclixHelper.getBannerAdview(mainApp), mainApp);
					}*/
				}
			}
		}
	};

	public BroadcastReceiver informAndExitReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String message = intent.getExtras().getString(AppConstants.MESSAGE);
			if (message == null || message.trim().equals(StaticData.SYMBOL_EMPTY)) {
				return;
			}
			new AlertDialog.Builder(context)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setCancelable(false)
					.setTitle(intent.getExtras().getString(AppConstants.TITLE))
					.setMessage(message)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							if (mainApp.isLiveChess()) {
								lccHolder.logout();
							}
							final String password = preferences.getString(AppConstants.PASSWORD, StaticData.SYMBOL_EMPTY);
							final Class clazz = (password == null || password.equals(StaticData.SYMBOL_EMPTY)) ? LoginScreenActivity.class : HomeScreenActivity.class;
							final Intent intent = new Intent(getContext(), clazz);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							mainApp.startActivity(intent);
						}
					}).create().show();
		}
	};

	public BroadcastReceiver obsoleteProtocolVersionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			new AlertDialog.Builder(context)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setCancelable(false)
					.setTitle(R.string.version_check)
					.setMessage(R.string.version_is_obsolete_update)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					final Handler handler = new Handler();
					handler.post(new Runnable() {
						@Override
						public void run() {
							mainApp.setLiveChess(false);
							lccHolder.setConnected(false);
							startActivity(new Intent(Intent.ACTION_VIEW, Uri
									.parse(RestHelper.PLAY_ANDROID_HTML)));
						}
					});
					final Intent intent = new Intent(getContext(), HomeScreenActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					mainApp.startActivity(intent);
				}
			}).create().show();
		}
	};

	private final BroadcastReceiver infoMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LccHolder.LOG.info(AppConstants.LCCLOG_ANDROID_RECEIVE_BROADCAST_INTENT_ACTION + intent.getAction());

//			Spanned message = Html.fromHtml(intent.getExtras().getString(AppConstants.MESSAGE));
			String message = intent.getExtras().getString(AppConstants.MESSAGE);
			String title = intent.getExtras().getString(AppConstants.TITLE);
//
//			PopupItem popupItem = new PopupItem();
//			popupItem.setTitle(title);
//			popupItem.setMessage(message);
//
//			PopupDialogFragment popupFragment = PopupDialogFragment.newInstance(popupItem, CoreActivityActionBar.this);
//			popupFragment.show(getSupportFragmentManager(), INFO_MSG_TAG);

			final TextView messageView = new TextView(context);
			messageView.setMovementMethod(LinkMovementMethod.getInstance());

//			messageView.setText(Html.fromHtml(intent.getExtras().getString(AppConstants.MESSAGE)));
			messageView.setText(message);
			messageView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			messageView.setGravity(Gravity.CENTER);

			new AlertDialog.Builder(context)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setCancelable(true)
					.setTitle(title)
					.setView(messageView)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(final DialogInterface dialog, int whichButton) {
							final Handler handler = new Handler();
							handler.post(new Runnable() {
								@Override
								public void run() {
									dialog.dismiss();
								}
							});
						}
					}).create().show();
		}
	};



	private final BroadcastReceiver lccLoggingInInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LccHolder.LOG.info(AppConstants.LCCLOG_ANDROID_RECEIVE_BROADCAST_INTENT_ACTION + intent.getAction());
			boolean enable = intent.getExtras().getBoolean(AppConstants.ENABLE_LIVE_CONNECTING_INDICATOR);
			manageConnectingIndicator(enable, intent.getExtras().getString(AppConstants.MESSAGE));
		}
	};

	public ActionBarHelper provideActionBarHelper(){
		return getActionBarHelper();
	}

	protected CoreActivityActionBar getInstance(){
		return this;
	}

	private void manageConnectingIndicator(boolean enable, String message) {
		if (mainApp.isLiveChess()) {
			MyProgressDialog connectingIndicator = lccHolder.getAndroidStuff().getConnectingIndicator();
			if (connectingIndicator != null) {
				connectingIndicator.dismiss();
				lccHolder.getAndroidStuff().setConnectingIndicator(null);
				lccHolder.updateConnectionState();
			} else if (enable) {
				connectingIndicator = new MyProgressDialog(this);
				connectingIndicator.setMessage(message);
				/*
				 * connectingIndicator.setOnCancelListener(new
				 * DialogInterface.OnCancelListener() { public void
				 * onCancel(DialogInterface dialog) {
				 * 
				 * final Intent intent = new Intent(this, Singin.class);
				 * intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				 * //connectingIndicator.dismiss(); lccHolder.logout();
				 * mainApp.startActivity(intent); } });
				 */
				connectingIndicator.setCancelable(true);
				connectingIndicator.setIndeterminate(true);
				connectingIndicator.show();
				lccHolder.getAndroidStuff().setConnectingIndicator(connectingIndicator);
			}
		}
	}

	@Override
	public void unregisterReceiver(BroadcastReceiver receiver) {// TODO don't do any hacks
		try {
			super.unregisterReceiver(receiver);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			// hack for Android's IllegalArgumentException: Receiver not registered
		}
	}

	public Boolean isUserColorWhite() {
		try {
			return mainApp.getCurrentGame().values.get(AppConstants.WHITE_USERNAME).toLowerCase()
					.equals(AppData.getUserName(getContext()));
		} catch (Exception e) {     // TODO remove NPE check
			return null;
		}
	}

	public SoundPlayer getSoundPlayer() {
		return SoundPlayer.getInstance(this);
	}

	public LccHolder getLccHolder() {
		return lccHolder;
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

	/*
	 * protected void showGameEndAds(LinearLayout adviewWrapper) { if
	 * (mainApp.isAdviewPaused()) {
	 * MobclixHelper.resumeAdview(getRectangleAdview(), mainApp); } }
	 */

	/*
	 * protected void showAds(MobclixMMABannerXLAdView adview) {
	 * if(!isShowAds()) { adview.setVisibility(View.GONE); } else {
	 * adview.setVisibility(View.VISIBLE);
	 * //adview.setAdUnitId("agltb3B1Yi1pbmNyDQsSBFNpdGUYmrqmAgw");
	 * //adview.setAdUnitId("agltb3B1Yi1pbmNyDAsSBFNpdGUYkaoMDA"); //test
	 * //adview.loadAd(); } }
	 */

	private void checkUpdate() {
		new CheckUpdateTask(this).execute(AppConstants.URL_GET_ANDROID_VERSION);
	}

	protected void showToast(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	protected void showToast(int msgId){
		Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show();
	}

	public MainApp getMainApp() {
		return mainApp;
	}

	@Override
	public GameItem getCurrentGame() {
		return mainApp.getCurrentGame();
	}

	protected Context getContext(){
		return this;
	}

}