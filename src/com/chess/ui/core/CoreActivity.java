package com.chess.ui.core;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.Web;
import com.chess.backend.WebService;
import com.chess.backend.tasks.CheckUpdateTask;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Game;
import com.chess.model.GameItem;
import com.chess.ui.activities.HomeScreenActivity;
import com.chess.ui.activities.LoginScreenActivity;
import com.chess.ui.interfaces.CoreActivityFace;
import com.chess.ui.views.BackgroundChessDrawable;
import com.chess.utilities.MyProgressDialog;
import com.chess.utilities.SoundPlayer;
import com.flurry.android.FlurryAgent;

public abstract class CoreActivity extends Activity implements CoreActivityFace {

	protected final static int INIT_ACTIVITY = -1;
	protected final static int ERROR_SERVER_RESPONSE = -2;

	protected MainApp mainApp;
	protected Bundle extras;
	protected DisplayMetrics metrics;
	protected MyProgressDialog progressDialog;
	protected String response = "";
	protected String responseRepeatable = "";
	protected BackgroundChessDrawable backgroundChessDrawable;

	protected Context context;
    public boolean mIsBound;
	public WebService appService = null;

	public abstract void update(int code);

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DITHER);
		getWindow().setFormat(PixelFormat.RGBA_8888);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;
		backgroundChessDrawable =  new BackgroundChessDrawable(this);
		mainApp = (MainApp) getApplication();
		extras = getIntent().getExtras();

		// get global Shared Preferences
		if (mainApp.getSharedData() == null) {
			mainApp.setSharedData(getSharedPreferences("sharedData", 0));
			mainApp.setSharedDataEditor(mainApp.getSharedData().edit());
		}

		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

//		lccHolder = mainApp.getLccHolder();
	}

	public boolean doBindService() {
		mIsBound = getApplicationContext().bindService(new Intent(this, WebService.class), onService,
				Context.BIND_AUTO_CREATE);
		return mIsBound;
	}

	public void doUnbindService() {
		if (mIsBound) {
			getApplicationContext().unbindService(onService);
			mIsBound = false;
		}
	}

	public ServiceConnection onService = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder rawBinder) {
			appService = ((WebService.LocalBinder) rawBinder).getService();
			update(INIT_ACTIVITY); // TODO send broadcast or call local method, but with readable arguments
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			appService = null;
		}
	};

	@Override
	protected void onResume() {
		super.onResume();

		boolean resetDetected = false;

		if (resetDetected) {
			checkUserTokenAndStartActivity();
		}

		doBindService();
		registerReceivers();

		if (mainApp.getSharedData().getLong(AppConstants.FIRST_TIME_START, 0) == 0) {
			mainApp.getSharedDataEditor().putLong(AppConstants.FIRST_TIME_START, System.currentTimeMillis());
			mainApp.getSharedDataEditor().putInt(AppConstants.ADS_SHOW_COUNTER, 0);
			mainApp.getSharedDataEditor().commit();
		}
		long startDay = mainApp.getSharedData().getLong(AppConstants.START_DAY, 0);
		if (mainApp.getSharedData().getLong(AppConstants.START_DAY, 0) == 0 || !DateUtils.isToday(startDay)) {
			mainApp.getSharedDataEditor().putLong(AppConstants.START_DAY, System.currentTimeMillis());
			mainApp.getSharedDataEditor().putBoolean(AppConstants.FULLSCREEN_AD_ALREADY_SHOWED, false);
			mainApp.getSharedDataEditor().commit();
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

		mainApp.getSharedDataEditor().putLong(AppConstants.LAST_ACTIVITY_PAUSED_TIME, System.currentTimeMillis());
		mainApp.getSharedDataEditor().commit();

		//mainApp.setForceBannerAdOnFailedLoad(false);

		if (progressDialog != null)
			progressDialog.dismiss();
	}


	private void registerReceivers(){
		registerReceiver(receiver, new IntentFilter(WebService.BROADCAST_ACTION));
		registerReceiver(lccLoggingInInfoReceiver, new IntentFilter(IntentConstants.FILTER_LOGINING_INFO));
		registerReceiver(lccReconnectingInfoReceiver, new IntentFilter(IntentConstants.FILTER_RECONNECT_INFO));
 		registerReceiver(informAndExitReceiver, new IntentFilter(IntentConstants.FILTER_EXIT_INFO));
		registerReceiver(obsoleteProtocolVersionReceiver, new IntentFilter(IntentConstants.FILTER_PROTOCOL_VERSION));
		registerReceiver(infoMessageReceiver, new IntentFilter(IntentConstants.FILTER_INFO));
	}

	private void unRegisterReceivers(){
		unregisterReceiver(receiver);
		unregisterReceiver(lccLoggingInInfoReceiver);
		unregisterReceiver(lccReconnectingInfoReceiver);
		unregisterReceiver(informAndExitReceiver);
		unregisterReceiver(obsoleteProtocolVersionReceiver);
		unregisterReceiver(infoMessageReceiver);
	}

	private void checkUserTokenAndStartActivity() {
		if (!mainApp.getUserName().equals("")) {
			final Intent intent = new Intent(mainApp, HomeScreenActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} else {
			startActivity(new Intent(mainApp, LoginScreenActivity.class));
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
				if (mainApp.noInternet) { /* mainApp.showToast("Online mode!"); */
					mainApp.offline = false;
				}
				mainApp.noInternet = false;
			}

			if (resp.contains(AppConstants.SUCCESS))
				update(retCode);
			else {
				if (resp.length() == 0) {
					update(ERROR_SERVER_RESPONSE);
					return;
				}
				String title = getString(R.string.error);
				String message;
				if (resp.contains(AppConstants.ERROR_PLUS)) {
					message = resp.split("[+]")[1];
				} else {
					update(ERROR_SERVER_RESPONSE);
					return;
				}
				if (message == null || message.trim().equals("")) {
					update(ERROR_SERVER_RESPONSE);
					return;
				}
				new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert).setTitle(title)
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


	public BroadcastReceiver lccReconnectingInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
		}
	};

	public BroadcastReceiver informAndExitReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String message = intent.getExtras().getString(AppConstants.MESSAGE);
			if (message == null || message.trim().equals("")) {
				return;
			}
			new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert).setCancelable(false)
					.setTitle(intent.getExtras().getString(AppConstants.TITLE)).setMessage(message)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							final Intent intent = new Intent(mainApp, LoginScreenActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							mainApp.startActivity(intent);
						}
					}).create().show();
		}
	};

	public BroadcastReceiver obsoleteProtocolVersionReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert).setCancelable(false)
					.setTitle("Version Check").setMessage("The client version is obsolete. Please update")
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							final Handler handler = new Handler();
							handler.post(new Runnable() {
								@Override
								public void run() {
									startActivity(new Intent(Intent.ACTION_VIEW, Uri
											.parse("http://www.chess.com/play/android.html")));
								}
							});
							final Intent intent = new Intent(mainApp, HomeScreenActivity.class);
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
			final TextView messageView = new TextView(context);
			messageView.setMovementMethod(LinkMovementMethod.getInstance());
			messageView.setText(Html.fromHtml(intent.getExtras().getString(AppConstants.MESSAGE)));
			messageView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			messageView.setGravity(Gravity.CENTER);

			new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert).setCancelable(true)
					.setTitle(intent.getExtras().getString(AppConstants.TITLE)).setView(messageView)
					.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
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

	private void manageConnectingIndicator(boolean enable, String message) {

	}

	@Override
	public void unregisterReceiver(BroadcastReceiver receiver) { // TODO handle unregister receiver correctly
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
					.equals(mainApp.getUserName());
		} catch (Exception e) {
			return null;
		}
	}

	public SoundPlayer getSoundPlayer() {
		return mainApp.getSoundPlayer();
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
		doUnbindService();
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

	private void checkUpdate() {  // TODO show progress
        new CheckUpdateTask(this, mainApp).execute(AppConstants.URL_GET_ANDROID_VERSION);
	}

	@Override
	public GameItem getCurrentGame() {
		return mainApp.getCurrentGame();
	}
}