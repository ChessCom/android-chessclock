package com.chess.ui.activities;

import actionbarcompat.ActionBarActivity;
import actionbarcompat.ActionBarHelper;
import android.app.AlertDialog;
import android.content.*;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.entity.SoundPlayer;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.IntentConstants;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.CheckUpdateTask2;
import com.chess.backend.tasks.ConnectLiveChessTask;
import com.chess.lcc.android.LccHolder;
import com.chess.lcc.android.LccHolder2;
import com.chess.lcc.android.interfaces.LiveChessClientEventListenerFace;
import com.chess.model.PopupItem;
import com.chess.ui.fragments.PopupDialogFragment;
import com.chess.ui.fragments.PopupProgressFragment;
import com.chess.ui.interfaces.ActiveFragmentInterface;
import com.chess.ui.interfaces.PopupDialogFace;
import com.chess.ui.views.BackgroundChessDrawable;
import com.chess.utilities.AppUtils;
import com.chess.utilities.MyProgressDialog;
import com.flurry.android.FlurryAgent;
import com.mopub.mobileads.MoPubView;

import java.util.ArrayList;
import java.util.List;

public abstract class CoreActivityActionBar2 extends ActionBarActivity implements View.OnClickListener
		, ActiveFragmentInterface, PopupDialogFace, LiveChessClientEventListenerFace {

	private static final String TAG = "CoreActivityActionBar";
	private static final String CHECK_UPDATE_TAG = "check update";
	private static final String CONNECT_FAILED_TAG = "connect_failed";
	protected static final String OBSOLETE_VERSION_TAG = "obsolete version";
//	protected static final String ERROR_TAG = "error happend";
	private static final String INFO_POPUP_TAG = "information popup";
	private static final String INFO_MSG_TAG = "info message popup";
	private static final String PROGRESS_TAG = "progress dialog popup";


	protected Bundle extras;
	protected DisplayMetrics metrics;
	protected MyProgressDialog progressDialog;
	protected BackgroundChessDrawable backgroundChessDrawable;
	protected PopupItem popupItem;
	protected PopupDialogFragment popupDialogFragment;
	protected PopupItem popupProgressItem;
	protected PopupProgressFragment popupProgressDialogFragment;
	protected List<PopupDialogFragment> popupManager;

	protected Handler handler;
	protected SharedPreferences preferences;
	protected SharedPreferences.Editor preferencesEditor;


	// we may have this add on every screen, so control it on the lowest level
	protected MoPubView moPubView;
	private Boolean forceFlag;
	protected boolean isPaused;
	private LccConnectUpdateListener lccConnectUpdateListener;

	public void setFullScreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		if(savedInstanceState != null){
//			if(savedInstanceState.getBoolean(StaticData.SAVED_STATE)){
//				checkUserTokenAndStartActivity();
//			}
//		}

		handler = new Handler();
		backgroundChessDrawable = new BackgroundChessDrawable(this);

		popupItem = new PopupItem();
		popupDialogFragment = PopupDialogFragment.newInstance(popupItem, this);
		popupProgressItem = new PopupItem();
		popupProgressDialogFragment = PopupProgressFragment.newInstance(popupProgressItem);

		popupManager = new ArrayList<PopupDialogFragment>();

		extras = getIntent().getExtras();

		// get global Shared Preferences
		preferences = AppData.getPreferences(this);
		preferencesEditor = preferences.edit();

		AppUtils.changeLocale(this);

		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		lccConnectUpdateListener = new LccConnectUpdateListener();

        LccHolder2.getInstance(this).setLiveChessClientEventListener(this);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		View mainView = findViewById(R.id.mainView);
		if (mainView != null)
			mainView.setBackgroundDrawable(backgroundChessDrawable);
	}

	protected void widgetsInit() {

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(StaticData.SAVED_STATE, true);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		backgroundChessDrawable.updateConfig();
		super.onConfigurationChanged(newConfig);
	}


	@Override
	public void switchFragment(Fragment fragment) {

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        ft.replace(R.id.activeContent, fragment);
		ft.addToBackStack(null);
		ft.commit();
	}

	public void showProgress(boolean show) {
		getActionBarHelper().setRefreshActionItemState(show);
	}


	/*
		 * public boolean isConnected(){ ConnectivityManager cm =
		 * (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE); NetworkInfo
		 * NI = cm.getActiveNetworkInfo(); if(NI == null) return false; else return
		 * NI.isConnectedOrConnecting(); }
		 */


//	public boolean doBindService() {
//		mIsBound = getApplicationContext().bindService(new Intent(this, WebService.class), onService,
//				Context.BIND_AUTO_CREATE);
//		return mIsBound;
//	}
//
//	public void doUnbindService() {
//		if (mIsBound) {
//			getApplicationContext().unbindService(onService);
//			mIsBound = false;
//		}
//	}

//	public ServiceConnection onService = new ServiceConnection() {
//		@Override
//		public void onServiceConnected(ComponentName className, IBinder rawBinder) {
//			appService = ((WebService.LocalBinder) rawBinder).getService();
//			update(INIT_ACTIVITY); // TODO send broadcast or call local method, but with readable arguments
//		}
//
//		@Override
//		public void onServiceDisconnected(ComponentName className) {
//			appService = null;
//		}
//	};


	@Override
	protected void onResume() {
		super.onResume();
		isPaused = false;

//		final MyProgressDialog reconnectingIndicator = getLccHolder().getAndroidStuff().getReconnectingIndicator();
//		if (!lccHolder.isConnectingInProgress() && reconnectingIndicator != null) {
//			reconnectingIndicator.dismiss();
//			lccHolder.getAndroidStuff().setReconnectingIndicator(null);
//		}

		if (LccHolder2.getInstance(this).isNotConnectedToLive()) {
			new ConnectLiveChessTask(lccConnectUpdateListener).executeTask();
//			LccHolder.getInstance(this).performConnect();
		}
        
//		doBindService();
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
		/*
		 * if (mainApp.isNetworkChangedNotification()) {
		 * showNetworkChangeNotification(); }
		 */

	}

	private class LccConnectUpdateListener extends AbstractUpdateListener<Void> {

		public LccConnectUpdateListener() {
			super(getContext());
		}

		@Override
		public void showProgress(boolean show) {
//			getActionBarHelper().setRefreshActionItemState(show);
//            Log.d("TEST", "showing progress bar " + show);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
//			onLccConnectFail(resultCode);
//            Log.d("TEST", "Lcc onLccConnectFail");
		}
	}

	protected void onLccConnectFail(Integer resultCode) {
        getActionBarHelper().showMenuItemById(R.id.menu_singOut, false);
	}

	@Override
	protected void onPause() {
		super.onPause();
		isPaused = true;

		// try to destroy ad here as Mopub team suggested
		if (moPubView != null) {
			moPubView.destroy();
		}

//		doUnbindService();
//		if (appService != null && appService.getRepeatableTimer() != null) {
//			appService.stopSelf();
//			appService.getRepeatableTimer().cancel();
//			appService = null;
//		}
		unRegisterReceivers();

		// todo: how to logout user when he/she is switching to another
// activity?
		/*
		 * if (DataHolder.getInstance().isLiveChess() && lccHolder.isConnected()) {
		 * lccHolder.logout(); }
		 */


		preferencesEditor.putLong(AppConstants.LAST_ACTIVITY_PAUSED_TIME, System.currentTimeMillis());
		preferencesEditor.commit();


		//mainApp.setForceBannerAdOnFailedLoad(false);

		if (progressDialog != null)
			progressDialog.dismiss();
	}

	private void registerReceivers() {
//		registerReceiver(receiver, new IntentFilter(WebService.BROADCAST_ACTION));
//		registerReceiver(lccLoggingInInfoReceiver, new IntentFilter(IntentConstants.FILTER_LOGINING_INFO));
//		registerReceiver(lccReconnectingInfoReceiver, new IntentFilter(IntentConstants.FILTER_RECONNECT_INFO));
//		registerReceiver(informAndExitReceiver, new IntentFilter(IntentConstants.FILTER_EXIT_INFO));
//		registerReceiver(obsoleteProtocolVersionReceiver, new IntentFilter(IntentConstants.FILTER_PROTOCOL_VERSION));
		registerReceiver(infoMessageReceiver, new IntentFilter(IntentConstants.FILTER_INFO));
	}

	private void unRegisterReceivers() {
//		unregisterReceiver(receiver);
//		unregisterReceiver(lccLoggingInInfoReceiver);
//		unregisterReceiver(lccReconnectingInfoReceiver);      // TODO
//		unregisterReceiver(informAndExitReceiver);
//		unregisterReceiver(obsoleteProtocolVersionReceiver);
		unregisterReceiver(infoMessageReceiver);
	}

	protected void backToHomeActivity() {
		Intent intent = new Intent(this, HomeScreenActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	protected void backToLoginActivity() {
		Intent intent = new Intent(this, LoginScreenActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

	public void dismissAllPopups() {
		for (PopupDialogFragment fragment : popupManager) {
			fragment.getDialog().dismiss();
		}
		popupManager.clear();
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		if(fragment.getTag().equals(CONNECT_FAILED_TAG)){
			if (DataHolder.getInstance().isLiveChess()) {
				LccHolder.getInstance(getContext()).logout();
			}
			backToHomeActivity();
		}else if(fragment.getTag().equals(OBSOLETE_VERSION_TAG)){
			// Show site and
			final Handler handler = new Handler();
			handler.post(new Runnable() {
				@Override
				public void run() {
					DataHolder.getInstance().setLiveChess(false);
					LccHolder.getInstance(getContext()).setConnected(false);
					startActivity(new Intent(Intent.ACTION_VIEW, Uri
							.parse(RestHelper.PLAY_ANDROID_HTML)));
				}
			});

			backToHomeActivity();
		}else if(fragment.getTag().equals(INFO_MSG_TAG)){

		}else if(fragment.getTag().equals(CHECK_UPDATE_TAG)){
			if (forceFlag) {
				// drop start day
				preferencesEditor.putLong(AppConstants.START_DAY, 0);
				preferencesEditor.commit();

				backToLoginActivity();
			}
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.GOOGLE_PLAY_URI));
			startActivity(intent);
		}


		fragment.getDialog().dismiss();
	}

	@Override
	public void onNeutralBtnCLick(DialogFragment fragment) {
		fragment.getDialog().dismiss();
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		fragment.getDialog().dismiss();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.sign_out, menu);
        getActionBarHelper().showMenuItemById(R.id.menu_singOut, LccHolder.getInstance(this).isConnected(), menu);
        return super.onCreateOptionsMenu(menu);
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
		if (!AppData.getUserToken(this).equals(StaticData.SYMBOL_EMPTY)) {
			Intent intent = new Intent(this, HomeScreenActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
		} else {
			startActivity(new Intent(this, LoginScreenActivity.class));
		}
	}

	// ---------- LiveChessClientEventListenerFace ----------------
	@Override
	public void onConnecting() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.d("TEST", "show progress from CoreAcb");

				getActionBarHelper().showMenuItemById(R.id.menu_singOut, false);
				getActionBarHelper().setRefreshActionItemState(true);
			}
		});
	}

	@Override
	public void onConnectionEstablished() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.d("TEST", "HIDE progress from CoreAcb");

				getActionBarHelper().setRefreshActionItemState(false);
				getActionBarHelper().showMenuItemById(R.id.menu_singOut, true);
			}
		});
	}

	@Override
	public void onConnectionFailure(String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Log.d("TEST", "onConnectionFailure/ another login");
				getActionBarHelper().setRefreshActionItemState(false);
				getActionBarHelper().showMenuItemById(R.id.menu_singOut, false);
			}
		});

		showPopupDialog(R.string.error, message, CONNECT_FAILED_TAG);
		popupDialogFragment.setButtons(1);
	}

	@Override
	public void onObsoleteProtocolVersion() {
		showPopupDialog(R.string.version_check, R.string.version_is_obsolete_update,
				OBSOLETE_VERSION_TAG);
		popupDialogFragment.setButtons(1);
		popupDialogFragment.getDialog().setCancelable(false);
	}

	// -----------------------------------------------------




//	private final BroadcastReceiver receiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			// getting extras
//			Bundle rExtras = intent.getExtras();
//			boolean repeatable;
//			String resp;
//			int retCode;
//			try {
//				repeatable = rExtras.getBoolean(AppConstants.REPEATABLE_TASK);
//				resp = rExtras.getString(AppConstants.REQUEST_RESULT);
//				if (repeatable) {
//					responseRepeatable = resp;
//				} else {
//					response = resp;
//				}
//
//				retCode = rExtras.getInt(AppConstants.CALLBACK_CODE);
//			} catch (Exception e) {
//				update(ERROR_SERVER_RESPONSE);
//				return;
//			}
//
//			if (Web.getStatusCode() == -1)
//				mainApp.noInternet = true;
//			else {
//				if (mainApp.noInternet) {
//					mainApp.offline = false;
//				}
//				mainApp.noInternet = false;
//			}
//
//			if (resp.contains(RestHelper.R_SUCCESS))
//				update(retCode);
//			else {
//				if (resp.length() == 0) {
//					update(ERROR_SERVER_RESPONSE);
//					return;
//				}
//				String title = getString(R.string.error);
//				String message = resp;
//				if (resp.contains(RestHelper.R_ERROR)) {
//					message = resp.split("[+]")[1];
//				} else {
//					update(ERROR_SERVER_RESPONSE);
//					return;
//				}
//				if (message == null || message.trim().equals(StaticData.SYMBOL_EMPTY)) {
//					update(ERROR_SERVER_RESPONSE);
//					return;
//				}
//				new AlertDialog.Builder(CoreActivityActionBar.this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(title)
//						.setMessage(message)
//						.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
//							@Override
//							public void onClick(DialogInterface dialog, int whichButton) {
//								update(ERROR_SERVER_RESPONSE);
//							}
//						}).create().show();
//			}
//		}
//	};

	protected LccHolder2 getLccHolder() {
		return LccHolder2.getInstance(this);
	}


//    private class LiveChessClientManager implements ChatListener,
//            ConnectionListener, ChallengeListener, FriendStatusListener, GameListener {
//
//
//    }

//	public BroadcastReceiver lccReconnectingInfoReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			if (DataHolder.getInstance().isLiveChess()) {
//				Log.i(TAG, AppConstants.LCCLOG_ANDROID_RECEIVE_BROADCAST_INTENT_ACTION + intent.getAction()
//                        + ", enable=" + intent.getExtras().getBoolean(AppConstants.ENABLE_LIVE_CONNECTING_INDICATOR));
//				MyProgressDialog reconnectingIndicator = lccHolder.getAndroid().getReconnectingIndicator();
//				boolean enable = intent.getExtras().getBoolean(AppConstants.ENABLE_LIVE_CONNECTING_INDICATOR);
//
//				if (reconnectingIndicator != null) {
//					reconnectingIndicator.dismiss();
////					lccHolder.getAndroidStuff().setReconnectingIndicator(null);
//				}
//				if (enable) {
//					/*if (MobclixHelper.isShowAds(mainApp) && MobclixHelper.getBannerAdview(mainApp) != null
//							&& !mainApp.isAdviewPaused()) {
//						MobclixHelper.pauseAdview(MobclixHelper.getBannerAdview(mainApp), mainApp);
//					}*/
//					reconnectingIndicator = new MyProgressDialog(context);
//					reconnectingIndicator.setMessage(intent.getExtras().getString(AppConstants.MESSAGE));
//					reconnectingIndicator.setOnCancelListener(new DialogInterface.OnCancelListener() {
//						@Override
//						public void onCancel(DialogInterface dialog) {
//							LccHolder.getInstance(getContext()).logout();
//							final Intent intent = new Intent(CoreActivityActionBar.this, HomeScreenActivity.class);
//							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//							// reconnectingIndicator.dismiss();
//							startActivity(intent);
//						}
//					});
//					reconnectingIndicator.setCancelable(true);
//					reconnectingIndicator.setIndeterminate(true);
//					try {
//						reconnectingIndicator.show();
////						lccHolder.getAndroidStuff().setReconnectingIndicator(reconnectingIndicator); // TODO
//					} catch (Exception e) {
//						LccHolder.getInstance(getContext()).logout();
//						intent = new Intent(CoreActivityActionBar.this, HomeScreenActivity.class);
//						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//						// reconnectingIndicator.dismiss();
//						startActivity(intent);
//					}
//				} else {
//					/*if (MobclixHelper.isShowAds(mainApp) && MobclixHelper.getBannerAdview(mainApp) != null
//							&& mainApp.isAdviewPaused()) {
//						MobclixHelper.resumeAdview(MobclixHelper.getBannerAdview(mainApp), mainApp);
//					}*/
//				}
//			}
//		}
//	};

//	public BroadcastReceiver informAndExitReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			final String message = intent.getExtras().getString(AppConstants.MESSAGE);
//			if (message == null || message.trim().equals(StaticData.SYMBOL_EMPTY)) {
//				return;
//			}
//			new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert).setCancelable(false)
//					.setTitle(intent.getExtras().getString(AppConstants.TITLE)).setMessage(message)
//					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int whichButton) {
//							if (DataHolder.getInstance().isLiveChess()) {
//								LccHolder.getInstance(getContext()).logout();
//							}
//							final Intent intent = new Intent(coreContext, HomeScreenActivity.class);
//							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//							startActivity(intent);
//						}
//					}).create().show();
//		}
//	};

//	public BroadcastReceiver obsoleteProtocolVersionReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert)
//					.setCancelable(false)
//					.setTitle(R.string.version_check)
//					.setMessage(R.string.version_is_obsolete_update)
//					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int whichButton) {
//							final Handler handler = new Handler();
//							handler.post(new Runnable() {
//								@Override
//								public void run() {
//									DataHolder.getInstance().setLiveChess(false);
//									LccHolder.getInstance(getContext()).setConnected(false);
//									startActivity(new Intent(Intent.ACTION_VIEW, Uri
//											.parse(RestHelper.PLAY_ANDROID_HTML)));
//								}
//							});
//
//							final Intent intent = new Intent(getContext(), HomeScreenActivity.class);
//							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//							startActivity(intent);
//						}
//					}).create().show();
//		}
//	};


	private final BroadcastReceiver infoMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(TAG, AppConstants.LCCLOG_ANDROID_RECEIVE_BROADCAST_INTENT_ACTION + intent.getAction());
			Spanned message = Html.fromHtml(intent.getExtras().getString(AppConstants.MESSAGE));
//			String message = intent.getExtras().getString(AppConstants.MESSAGE);
			String title = intent.getExtras().getString(AppConstants.TITLE);
//
//			showPopupDialog(title, message);
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

			new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert).setCancelable(true)
					.setTitle(intent.getExtras().getString(AppConstants.TITLE)).setView(messageView)
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

//	private final BroadcastReceiver lccLoggingInInfoReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			getActionBarHelper().showMenuItemById(R.id.menu_singOut, true);
//		}
//	};

	public ActionBarHelper provideActionBarHelper() {
		return getActionBarHelper();
	}

	protected CoreActivityActionBar2 getInstance() {
		return this;
	}

	// TODO handle connection indicator if needed
//	private void manageConnectingIndicator(boolean enable, String message) {
//		if (DataHolder.getInstance().isLiveChess()) {
//			MyProgressDialog connectingIndicator = lccHolder.getAndroidStuff().getConnectingIndicator();
//			if (connectingIndicator != null) {
//				connectingIndicator.dismiss();
//				lccHolder.getAndroidStuff().setConnectingIndicator(null);
//				lccHolder.updateConnectionState();
//			} else if (enable) {
//				connectingIndicator = new MyProgressDialog(this);
//				connectingIndicator.setMessage(message);
//				/*
//				 * connectingIndicator.setOnCancelListener(new
//				 * DialogInterface.OnCancelListener() { public void
//				 * onCancel(DialogInterface dialog) {
//				 *
//				 * final Intent intent = new Intent(mainApp, Singin.class);
//				 * intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				 * //connectingIndicator.dismiss(); lccHolder.logout();
//				 * mainApp.startActivity(intent); } });
//				 */
//				connectingIndicator.setCancelable(true);
//				connectingIndicator.setIndeterminate(true);
//				connectingIndicator.show();
//				lccHolder.getAndroidStuff().setConnectingIndicator(connectingIndicator);
//			}
//		}
//	}

	@Override
	public void unregisterReceiver(BroadcastReceiver receiver) {// TODO don't do any hacks
		try {
			super.unregisterReceiver(receiver);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			// hack for Android's IllegalArgumentException: Receiver not registered
		}
	}

	public SoundPlayer getSoundPlayer() {
		return SoundPlayer.getInstance(this);
	}

	protected String getTextFromField(EditText editText) {
		return editText.getText().toString().trim();
	}

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, "M5ID55IB7UP9SAC88D3M");
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();


//		doUnbindService();
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

	private void checkUpdate() {  // TODO restore
//		new CheckUpdateTask(new CheckUpdateListener()).execute(RestHelper.GET_ANDROID_VERSION);
		new CheckUpdateTask2(new CheckUpdateListener()).executeTask(RestHelper.GET_ANDROID_VERSION);
//		new CheckUpdateTaskHttp(new CheckUpdateListener()).executeTask(RestHelper.GET_ANDROID_VERSION);
	}

	private class CheckUpdateListener extends AbstractUpdateListener<Boolean> {


		public CheckUpdateListener() {
			super(getContext());
		}

		@Override
		public void showProgress(boolean show) {
		}

		@Override
		public void updateData(Boolean returnedObj) {
			showToast("ActionBar check update finished");

			forceFlag = returnedObj;
			if (isPaused)
				return;

			showPopupDialog(R.string.update_check, R.string.update_available_please_update,
					CHECK_UPDATE_TAG);
			popupDialogFragment.setButtons(1);
		}
	}


	private DialogInterface.OnClickListener updateClickListener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int whichButton) {
			if (forceFlag) {

				preferencesEditor.putLong(AppConstants.START_DAY, 0);
				preferencesEditor.commit();

				startActivity(new Intent(CoreActivityActionBar2.this, LoginScreenActivity.class));
				finish();
			}
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.chess"));
			startActivity(intent);
		}
	};

	protected void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	protected void showToast(int msgId) {
		Toast.makeText(this, msgId, Toast.LENGTH_SHORT).show();
	}
    // Single button no callback dialogs
    protected void showSinglePopupDialog(int titleId, int messageId) {
        showPopupDialog(titleId, messageId, INFO_POPUP_TAG);
        popupDialogFragment.setButtons(1);
    }

    protected void showSinglePopupDialog(String title, String message) {
        showPopupDialog(title, message, INFO_POPUP_TAG);
        popupDialogFragment.setButtons(1);
    }

    protected void showSinglePopupDialog(int titleId, String message) {
        showPopupDialog(titleId, message, INFO_POPUP_TAG);
        popupDialogFragment.setButtons(1);
    }

    protected void showSinglePopupDialog(String message) {
        showPopupDialog(message, INFO_POPUP_TAG);
        popupDialogFragment.setButtons(1);
    }

    protected void showSinglePopupDialog(int messageId) {
        showPopupDialog(messageId, INFO_POPUP_TAG);
        popupDialogFragment.setButtons(1);
    }

    // Default Dialogs
    protected void showPopupDialog(int titleId, int messageId, String tag) {
        popupItem.setTitle(titleId);
        popupItem.setMessage(messageId);
        popupDialogFragment.show(getSupportFragmentManager(), tag);
    }

    protected void showPopupDialog(int titleId, String messageId, String tag) {
        popupItem.setTitle(titleId);
        popupItem.setMessage(messageId);
        popupDialogFragment.show(getSupportFragmentManager(), tag);
    }


    protected void showPopupDialog(String title, String message, String tag) {
        popupItem.setTitle(title);
        popupItem.setMessage(message);
        popupDialogFragment.show(getSupportFragmentManager(), tag);
    }

    protected void showPopupDialog(int titleId, String tag) {
        popupItem.setTitle(titleId);
        popupDialogFragment.show(getSupportFragmentManager(), tag);
    }

    protected void showPopupDialog(String title, String tag) {
        popupItem.setTitle(title);
        popupDialogFragment.show(getSupportFragmentManager(), tag);
    }

    // Progress Dialogs
    protected void showPopupProgressDialog(String title) {
        popupProgressItem.setTitle(title);
        popupProgressDialogFragment.show(getSupportFragmentManager(), PROGRESS_TAG);
    }

    protected void showPopupProgressDialog(String title, String message) {
        popupProgressItem.setTitle(title);
        popupProgressItem.setMessage(message);
        popupProgressDialogFragment.show(getSupportFragmentManager(), PROGRESS_TAG);
    }

    protected void showPopupProgressDialog(int title) {
        popupProgressItem.setTitle(title);
        popupProgressDialogFragment.show(getSupportFragmentManager(), PROGRESS_TAG);
    }

    protected void showPopupProgressDialog(int titleId, int messageId) {
        popupProgressItem.setTitle(titleId);
        popupProgressItem.setMessage(messageId);
        popupProgressDialogFragment.show(getSupportFragmentManager(), PROGRESS_TAG);
    }

    protected void dismissProgressDialog(){
        if (popupProgressDialogFragment != null && popupProgressDialogFragment.getDialog() != null)
            popupProgressDialogFragment.getDialog().dismiss();
    }

	protected Context getContext() {
		return this;
	}

}
