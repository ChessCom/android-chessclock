package com.chess.ui.activities;

import actionbarcompat.ActionBarActivityHome;
import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.*;
import com.chess.backend.tasks.CheckUpdateTask2;
import com.chess.lcc.android.LccHolder2;
import com.chess.lcc.android.interfaces.LiveChessClientEventListenerFace;
import com.chess.model.PopupItem;
import com.chess.ui.fragments.PopupDialogFragment;
import com.chess.ui.fragments.PopupProgressFragment;
import com.chess.ui.interfaces.PopupDialogFace;
import com.chess.utilities.MyProgressDialog;
import com.flurry.android.FlurryAgent;

import java.util.ArrayList;
import java.util.List;

public abstract class CoreActivityHome2 extends ActionBarActivityHome implements PopupDialogFace, LiveChessClientEventListenerFace {

	private static final String TAG = "CoreActivityHome";
	private static final String CHECK_UPDATE_TAG = "check update";
	private static final String CONNECT_FAILED_TAG = "connect_failed";
	public static final String OBSOLETE_VERSION_TAG = "obsolete version";

    private static final String INFO_POPUP_TAG = "information popup";
    private static final String PROGRESS_TAG = "progress dialog popup";

	protected final static int INIT_ACTIVITY = -1;
	protected final static int ERROR_SERVER_RESPONSE = -2;

	//	protected MainApp mainApp;
	protected Bundle extras;
	protected DisplayMetrics metrics;
	protected MyProgressDialog progressDialog;
	//	protected LccHolder lccHolder;
	protected String response = StaticData.SYMBOL_EMPTY;
	protected String responseRepeatable = StaticData.SYMBOL_EMPTY;

	protected Context context;
	protected boolean isPaused;
//	private Handler handler;
//	public boolean mIsBound;
//	public WebService appService = null;

	protected PopupDialogFragment popupDialogFragment;
	protected PopupItem popupItem;
	protected PopupItem popupProgressItem;
	protected PopupProgressFragment popupProgressDialogFragment;
	protected List<PopupDialogFragment> popupManager;
	protected SharedPreferences preferences;
	protected SharedPreferences.Editor preferencesEditor;

	public abstract void update(int code);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean(StaticData.SAVED_STATE)) {
				checkUserTokenAndStartActivity();
			}
		}

		context = this;
//		handler = new Handler();

//		mainApp = (MainApp) getApplication();
		extras = getIntent().getExtras();

		// get global Shared Preferences
		preferences = AppData.getPreferences(this);
		preferencesEditor = preferences.edit();

		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

//		lccHolder = mainApp.getLccHolder();
//		lccHolder.setExternalConnectionListener(this);

		popupItem = new PopupItem();
		popupDialogFragment = PopupDialogFragment.newInstance(popupItem, this);
		popupProgressItem = new PopupItem();
		popupProgressDialogFragment = PopupProgressFragment.newInstance(popupProgressItem);

		popupManager = new ArrayList<PopupDialogFragment>();

		LccHolder2.getInstance(this).setLiveChessClientEventListener(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(StaticData.SAVED_STATE, true);
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

//		final MyProgressDialog reconnectingIndicator = lccHolder.getAndroidStuff().getReconnectingIndicator();
//		if (!lccHolder.isConnectingInProgress() && reconnectingIndicator != null) {
//			reconnectingIndicator.dismiss();
//			lccHolder.getAndroidStuff().setReconnectingIndicator(null);
//		}

//		if (DataHolder.getInstance().isLiveChess() && !lccHolder.isConnected() && !lccHolder.isConnectingInProgress()) {
//			// lccHolder.getAndroidStuff().showConnectingIndicator();
//			manageConnectingIndicator(true, "Loading Live Chess");
//
//			new ReconnectTask().execute();
//		}
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


//        LccHolder.getInstance(this).checkAndPerformReconnect();

	}

//	private class ReconnectTask extends AsyncTask<Void, Void, Void> {
//		@Override
//		protected Void doInBackground(Void... voids) {
//			final LccHolder lccHolder = mainApp.getLccHolder();
//			// lccHolder.setConnectingInProgress(true);
//			lccHolder.getClient().disconnect();
//			lccHolder.setNetworkTypeName(null);
//			lccHolder.setConnectingInProgress(true);
//			lccHolder.getClient().connect(
//					preferences.getString(AppConstants.USERNAME, StaticData.SYMBOL_EMPTY),
//					preferences.getString(AppConstants.PASSWORD, StaticData.SYMBOL_EMPTY),
//					lccHolder.getConnectionListener());
//
//			return null;
//		}
//
//		@Override
//		protected void onPostExecute(Void aVoid) {
//			super.onPostExecute(aVoid);
//			lccHolder.updateConnectionState();
//		}
//	}

	@Override
	protected void onPause() {
		super.onPause();
		isPaused = true;
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
		registerReceiver(receiver, new IntentFilter(IntentConstants.BROADCAST_ACTION));
//		registerReceiver(lccLoggingInInfoReceiver, new IntentFilter(IntentConstants.FILTER_LOGINING_INFO));
//		registerReceiver(lccReconnectingInfoReceiver, new IntentFilter(IntentConstants.FILTER_RECONNECT_INFO));
//		registerReceiver(drawOfferedMessageReceiver, new IntentFilter(IntentConstants.FILTER_DRAW_OFFERED));
//		registerReceiver(informAndExitReceiver, new IntentFilter(IntentConstants.FILTER_EXIT_INFO));
//		registerReceiver(obsoleteProtocolVersionReceiver, new IntentFilter(IntentConstants.FILTER_PROTOCOL_VERSION));
		registerReceiver(infoMessageReceiver, new IntentFilter(IntentConstants.FILTER_INFO));
	}

	private void unRegisterReceivers() {
		unregisterReceiver(receiver);
//		unregisterReceiver(drawOfferedMessageReceiver);
//		unregisterReceiver(lccLoggingInInfoReceiver);
//		unregisterReceiver(lccReconnectingInfoReceiver);
//		unregisterReceiver(informAndExitReceiver);
//		unregisterReceiver(obsoleteProtocolVersionReceiver);
		unregisterReceiver(infoMessageReceiver);
	}

	private void checkUserTokenAndStartActivity() {
		if (!AppData.getUserName(this).equals(StaticData.SYMBOL_EMPTY)) {
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
			String resp = StaticData.SYMBOL_EMPTY;
			int retCode = ERROR_SERVER_RESPONSE;
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

//			if (Web.getStatusCode() == -1)
//				mainApp.noInternet = true;
//			else {
//				if (mainApp.noInternet) { /* mainApp.showToast("Online mode!"); */
//					mainApp.offline = false;
//				}
//				mainApp.noInternet = false;
//			}

			if (resp.contains(RestHelper.R_SUCCESS))
				update(retCode);
			else {
				/*if (mainApp.getTabHost() != null && mainApp.getTabHost().getCurrentTab() == 3) {
					update(ERROR_SERVER_RESPONSE);
					return;
				}*/
				if (resp.length() == 0) {
					update(ERROR_SERVER_RESPONSE);
					return;
				}
				String title = getString(R.string.error);
				String message = resp;
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
				new AlertDialog.Builder(CoreActivityHome2.this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(title)
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

	protected LccHolder2 getLccHolder() {
		return LccHolder2.getInstance(this);
	}


//	public BroadcastReceiver lccReconnectingInfoReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			if (DataHolder.getInstance().isLiveChess()) {
//				Log.i(TAG, AppConstants.LCCLOG_ANDROID_RECEIVE_BROADCAST_INTENT_ACTION + intent.getAction()
//                        + ", enable=" + intent.getExtras().getBoolean(AppConstants.ENABLE_LIVE_CONNECTING_INDICATOR));
//				MyProgressDialog reconnectingIndicator = lccHolder.getAndroid().getReconnectingIndicator(); // TODO
//				boolean enable = intent.getExtras().getBoolean(AppConstants.ENABLE_LIVE_CONNECTING_INDICATOR);
//
////				if (reconnectingIndicator != null) {
////					reconnectingIndicator.dismiss();
////					lccHolder.getAndroidStuff().setReconnectingIndicator(null);
////				}
//				/* else */
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
//							lccHolder.logout();
//							final Intent intent = new Intent(CoreActivityHome.this, HomeScreenActivity.class);
//							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//							// reconnectingIndicator.dismiss();
//							startActivity(intent);
//						}
//					});
//					reconnectingIndicator.setCancelable(true);
//					reconnectingIndicator.setIndeterminate(true);
//					try {
//						reconnectingIndicator.show();
//						lccHolder.getAndroid().setReconnectingIndicator(reconnectingIndicator);
//					} catch (Exception e) {
//						lccHolder.logout();
//						intent = new Intent(CoreActivityHome.this, HomeScreenActivity.class);
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
//							if (DataHolder.getInstance().isLiveChess()/*
//													 * &&
//													 * lccHolder.isConnected()
//													 */) {
//								getLccHolder().logout();
//							}
//							final Intent intent = new Intent(getContext(), HomeScreenActivity.class);
//							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//							startActivity(intent);
//						}
//					}).create().show();
//		}
//	};

//	public BroadcastReceiver obsoleteProtocolVersionReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert).setCancelable(false)
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
//											.parse("http://www.chess.com/play/android.html")));
//								}
//							});
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
			final TextView messageView = new TextView(context);
			messageView.setMovementMethod(LinkMovementMethod.getInstance());
			messageView.setText(Html.fromHtml(intent.getExtras().getString(AppConstants.MESSAGE)));
			messageView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			messageView.setGravity(Gravity.CENTER);

			new AlertDialog.Builder(context)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setCancelable(true)
					.setTitle(intent.getExtras().getString(AppConstants.TITLE))
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

//	private final BroadcastReceiver lccLoggingInInfoReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			Log.i(TAG, AppConstants.LCCLOG_ANDROID_RECEIVE_BROADCAST_INTENT_ACTION + intent.getAction());
//			boolean enable = intent.getExtras().getBoolean(AppConstants.ENABLE_LIVE_CONNECTING_INDICATOR);
////			manageConnectingIndicator(enable, intent.getExtras().getString(AppConstants.MESSAGE));
//		}
//	};


	@Override
	public void unregisterReceiver(BroadcastReceiver receiver) {
		try {
			super.unregisterReceiver(receiver);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			// hack for Android's IllegalArgumentException: Receiver not
// registered
		}
	}

	public void dismissAllPopups() {
		for (PopupDialogFragment fragment : popupManager) {
			fragment.getDialog().dismiss();
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		if (fragment.getTag().equals(CHECK_UPDATE_TAG)) {
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
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, FlurryData.API_KEY);
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	// ---------- LiveChessClientEventListenerFace ----------------
	@Override
	public void onConnecting() {
		getActionBarHelper().showMenuItemById(R.id.menu_singOut, false);
		getActionBarHelper().setRefreshActionItemState(true);
	}

	@Override
	public void onConnectionEstablished() {
		getActionBarHelper().setRefreshActionItemState(false);
		getActionBarHelper().showMenuItemById(R.id.menu_singOut, true);
	}

	@Override
	public void onConnectionFailure(String message) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getActionBarHelper().setRefreshActionItemState(false);
				getActionBarHelper().showMenuItemById(R.id.menu_singOut, false);
			}
		});

		showPopupDialog(R.string.warning, message, CONNECT_FAILED_TAG);
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
		new CheckUpdateTask2(new CheckUpdateListener()).executeTask(RestHelper.GET_ANDROID_VERSION);
	}

	private boolean forceFlag;


	private class CheckUpdateListener extends AbstractUpdateListener<Boolean> {
		public CheckUpdateListener() {
			super(getContext());
		}

		@Override
		public void showProgress(boolean show) {
		}

		@Override
		public void updateData(Boolean returnedObj) {
			showToast("Home check update finished");

			forceFlag = returnedObj;
			if (isPaused)
				return;

			showPopupDialog(R.string.update_check, R.string.update_available_please_update,
					CHECK_UPDATE_TAG);
			popupDialogFragment.setButtons(1);
		}
	}

	protected void backToLoginActivity() {
		Intent intent = new Intent(this, LoginScreenActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
	}

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