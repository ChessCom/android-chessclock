package com.chess.core;

import actionbarcompat.ActionBarActivityHome;
import android.app.AlertDialog;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;
import com.chess.R;
import com.chess.activities.HomeScreenActivity;
import com.chess.activities.LoginScreenActivity;
import com.chess.backend.tasks.CheckUpdateTask;
import com.chess.core.interfaces.CoreActivityFace;
import com.chess.lcc.android.LccHolder;
import com.chess.utilities.*;
import com.flurry.android.FlurryAgent;
import com.mobclix.android.sdk.MobclixAdView;

public abstract class CoreActivityHome extends ActionBarActivityHome implements CoreActivityFace {

	protected final static int INIT_ACTIVITY = -1;
	protected final static int ERROR_SERVER_RESPONSE = -2;

	protected MainApp mainApp;
	protected Bundle extras;
	protected DisplayMetrics metrics;
	protected MyProgressDialog progressDialog;
	protected LccHolder lccHolder;
	private PowerManager.WakeLock wakeLock;
	protected String response = "";
	protected String responseRepeatable = "";


	public abstract void Update(int code);

	protected Context context;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = this;
		mainApp = (MainApp) getApplication();
		extras = getIntent().getExtras();

		// get global Shared Preferences
		if (mainApp.getSharedData() == null) {
			mainApp.setSharedData(getSharedPreferences("sharedData", 0));
			mainApp.setSharedDataEditor(mainApp.getSharedData().edit());
		}

		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		lccHolder = mainApp.getLccHolder();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		doUnbindService();
	}

	/*
	 * public boolean isConnected(){ ConnectivityManager cm =
	 * (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE); NetworkInfo
	 * NI = cm.getActiveNetworkInfo(); if(NI == null) return false; else return
	 * NI.isConnectedOrConnecting(); }
	 */

	public boolean mIsBound;
	public WebService appService = null;

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
			Update(INIT_ACTIVITY); // TODO send broadcast or call local method, but with readable arguments
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
			lccHolder.getClient().connect(mainApp.getSharedData().getString(AppConstants.USER_SESSION_ID, ""),
					lccHolder.getConnectionListener());
			/*
								 * appService.RunRepeatble(0, 0, 120000, progressDialog =
								 * MyProgressDialog.show(this, null,
								 * getString(R.string.updatinggameslist), true));
								 */
			return null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mainApp.getBoardBitmap() == null || mainApp.getPiecesBitmap() == null) {
			new Handler().post(new Runnable() {
				@Override
				public void run() {
					mainApp.LoadBoard(mainApp.res_boards[mainApp.getSharedData().getInt(
							mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_BOARD_TYPE, 8)]);
					mainApp.LoadPieces(mainApp.res_pieces[mainApp.getSharedData().getInt(
							mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_PIECES_SET, 0)]);
					mainApp.loadCapturedPieces();
				}
			});
			if (!mainApp.getSharedData().getString(AppConstants.USERNAME, "").equals("")) {
				final Intent intent = new Intent(mainApp, HomeScreenActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mainApp.startActivity(intent);
			} else {
				startActivity(new Intent(mainApp, LoginScreenActivity.class));
			}
		}

		final MyProgressDialog reconnectingIndicator = lccHolder.getAndroid().getReconnectingIndicator();
		if (!lccHolder.isConnectingInProgress() && reconnectingIndicator != null) {
			reconnectingIndicator.dismiss();
			lccHolder.getAndroid().setReconnectingIndicator(null);
		}

		if (mainApp.isLiveChess() && !lccHolder.isConnected() && !lccHolder.isConnectingInProgress()) {
			// lccHolder.getAndroid().showConnectingIndicator();
			manageConnectingIndicator(true, "Loading Live Chess");

			// startService(new Intent(getApplicationContext(),
// NetworkChangeService.class));

			new ReconnectTask().execute();
		}
		doBindService();
		registerReceiver(receiver, new IntentFilter(WebService.BROADCAST_ACTION));
		/*
		 * if (mainApp.isLiveChess()) {
		 */

		registerReceiver(lccLoggingInInfoReceiver, new IntentFilter(IntentConstants.FILTER_LOGINING_INFO));
		registerReceiver(lccReconnectingInfoReceiver, new IntentFilter(IntentConstants.FILTER_RECONNECT_INFO));
		registerReceiver(drawOfferedMessageReceiver, new IntentFilter(IntentConstants.FILTER_DRAW_OFFERED));
		registerReceiver(informAndExitReceiver, new IntentFilter(IntentConstants.FILTER_EXIT_INFO));
		registerReceiver(obsoleteProtocolVersionReceiver, new IntentFilter(IntentConstants.FILTER_PROTOCOL_VERSION));
		registerReceiver(infoMessageReceiver, new IntentFilter(IntentConstants.FILTER_INFO));

		// registerReceiver(networkChangeNotificationReceiver, new
// IntentFilter("com.chess.lcc.android-network-change"));
		/* } */
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
		/*
		 * if (mainApp.isNetworkChangedNotification()) {
		 * showNetworkChangeNotification(); }
		 */
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
		unregisterReceiver(receiver);
		unregisterReceiver(drawOfferedMessageReceiver);
		unregisterReceiver(lccLoggingInInfoReceiver);
		unregisterReceiver(lccReconnectingInfoReceiver);
		unregisterReceiver(informAndExitReceiver);
		unregisterReceiver(obsoleteProtocolVersionReceiver);
		unregisterReceiver(infoMessageReceiver);
		// unregisterReceiver(networkChangeNotificationReceiver);

		// todo: how to logout user when he/she is switching to another
// activity?
		/*
		 * if (mainApp.isLiveChess() && lccHolder.isConnected()) {
		 * lccHolder.logout(); }
		 */

		mainApp.getSharedDataEditor().putLong(AppConstants.LAST_ACTIVITY_PAUSED_TIME, System.currentTimeMillis());
		mainApp.getSharedDataEditor().commit();
		mainApp.setForceBannerAdOnFailedLoad(false);

		if (progressDialog != null)
			progressDialog.dismiss();
	}

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// getting extras
			Bundle rExtras = intent.getExtras();
			boolean repeatable;
			String resp = "";
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
				Update(ERROR_SERVER_RESPONSE);
				return;
			}

			if (Web.getStatusCode() == -1)
				mainApp.noInternet = true;
			else {
				if (mainApp.noInternet) { /* mainApp.ShowMessage("Online mode!"); */
					mainApp.offline = false;
				}
				mainApp.noInternet = false;
			}

			if (resp.contains("Success"))
				Update(retCode);
			else {
				if (mainApp.getTabHost() != null && mainApp.getTabHost().getCurrentTab() == 3) {
					Update(ERROR_SERVER_RESPONSE);
					return;
				}
				if (resp.length() == 0) {
					Update(ERROR_SERVER_RESPONSE);
					return;
				}
				String title = getString(R.string.error);
				String message = resp;
				if (resp.contains("Error+")) {
					message = resp.split("[+]")[1];
				} else {
					Update(ERROR_SERVER_RESPONSE);
					return;
				}
				if (message == null || message.trim().equals("")) {
					Update(ERROR_SERVER_RESPONSE);
					return;
				}
				new AlertDialog.Builder(CoreActivityHome.this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(title)
						.setMessage(message)
						.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int whichButton) {
								Update(ERROR_SERVER_RESPONSE);
							}
						}).create().show();
			}
		}
	};

	private final BroadcastReceiver drawOfferedMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction());
			final com.chess.live.client.Game game = mainApp.getLccHolder().getGame(mainApp.getGameId());
			final AlertDialog alertDialog = new AlertDialog.Builder(CoreActivityHome.this)
					// .setTitle(intent.getExtras().getString(AppConstants.TITLE))
					.setMessage(intent.getExtras().getString(AppConstants.MESSAGE))
					.setPositiveButton(getString(R.string.accept), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							mainApp.getLccHolder().getAndroid().runMakeDrawTask(game);
						}
					}).setNeutralButton(getString(R.string.decline), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							lccHolder.getAndroid().runRejectDrawTask(game);
						}
					})
							/*
												 * .setNegativeButton(getString(R.string.cancel), new
												 * DialogInterface.OnClickListener() { public void
												 * onClick(DialogInterface dialog, int whichButton) {
												 * startActivity(new Intent(CoreActivity.this, Game.class).
												 * putExtra(AppConstants.GAME_MODE, 4).
												 * putExtra(AppConstants.GAME_ID,
												 * el.values.get(AppConstants.GAME_ID))); } })
												 */
					.create();
			alertDialog.setCanceledOnTouchOutside(true);
			alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialogInterface) {
					lccHolder.getAndroid().runRejectDrawTask(game);
				}
			});
			alertDialog.getWindow().setGravity(Gravity.BOTTOM);
			alertDialog.show();
		}
	};

	protected BroadcastReceiver challengesListUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mainApp.isLiveChess()) {
				LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction());
				Update(intent.getExtras().getInt(AppConstants.CALLBACK_CODE));
			}
		}
	};

	public BroadcastReceiver lccReconnectingInfoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (mainApp.isLiveChess()) {
				LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction()
						+ ", enable=" + intent.getExtras().getBoolean(AppConstants.ENABLE_LIVE_CONNECTING_INDICATOR));
				MyProgressDialog reconnectingIndicator = lccHolder.getAndroid().getReconnectingIndicator();
				boolean enable = intent.getExtras().getBoolean(AppConstants.ENABLE_LIVE_CONNECTING_INDICATOR);

				if (reconnectingIndicator != null) {
					reconnectingIndicator.dismiss();
					lccHolder.getAndroid().setReconnectingIndicator(null);
				}
				/* else */
				if (enable) {
					if (MobclixHelper.isShowAds(mainApp) && MobclixHelper.getBannerAdview(mainApp) != null
							&& !mainApp.isAdviewPaused()) {
						MobclixHelper.pauseAdview(MobclixHelper.getBannerAdview(mainApp), mainApp);
					}
					reconnectingIndicator = new MyProgressDialog(context);
					reconnectingIndicator.setMessage(intent.getExtras().getString(AppConstants.MESSAGE));
					reconnectingIndicator.setOnCancelListener(new DialogInterface.OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							lccHolder.logout();
							final Intent intent = new Intent(CoreActivityHome.this, HomeScreenActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							// reconnectingIndicator.dismiss();
							mainApp.startActivity(intent);
						}
					});
					reconnectingIndicator.setCancelable(true);
					reconnectingIndicator.setIndeterminate(true);
					try {
						reconnectingIndicator.show();
						lccHolder.getAndroid().setReconnectingIndicator(reconnectingIndicator);
					} catch (Exception e) {
						lccHolder.logout();
						intent = new Intent(CoreActivityHome.this, HomeScreenActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						// reconnectingIndicator.dismiss();
						mainApp.startActivity(intent);
					}
				} else {
					if (MobclixHelper.isShowAds(mainApp) && MobclixHelper.getBannerAdview(mainApp) != null
							&& mainApp.isAdviewPaused()) {
						MobclixHelper.resumeAdview(MobclixHelper.getBannerAdview(mainApp), mainApp);
					}
				}
			}
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
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							if (mainApp.isLiveChess()/*
													 * &&
													 * lccHolder.isConnected()
													 */) {
								lccHolder.logout();
							}
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
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							final Handler handler = new Handler();
							handler.post(new Runnable() {
								@Override
								public void run() {
									mainApp.setLiveChess(false);
									lccHolder.setConnected(false);
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
			LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction());
			final TextView messageView = new TextView(context);
			messageView.setMovementMethod(LinkMovementMethod.getInstance());
			messageView.setText(Html.fromHtml(intent.getExtras().getString(AppConstants.MESSAGE)));
			messageView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
			messageView.setGravity(Gravity.CENTER);

			new AlertDialog.Builder(context).setIcon(android.R.drawable.ic_dialog_alert).setCancelable(true)
					.setTitle(intent.getExtras().getString(AppConstants.TITLE)).setView(messageView)
					.setPositiveButton("OK", new DialogInterface.OnClickListener() {
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
			LccHolder.LOG.info("LCCLOG ANDROID: receive broadcast intent, action=" + intent.getAction());
			boolean enable = intent.getExtras().getBoolean(AppConstants.ENABLE_LIVE_CONNECTING_INDICATOR);
			manageConnectingIndicator(enable, intent.getExtras().getString(AppConstants.MESSAGE));
		}
	};

	private void manageConnectingIndicator(boolean enable, String message) {
		if (mainApp.isLiveChess()) {
			MyProgressDialog connectingIndicator = lccHolder.getAndroid().getConnectingIndicator();
			if (connectingIndicator != null) {
				connectingIndicator.dismiss();
				lccHolder.getAndroid().setConnectingIndicator(null);
			} else if (enable) {
				connectingIndicator = new MyProgressDialog(this);
				connectingIndicator.setMessage(message);
				/*
				 * connectingIndicator.setOnCancelListener(new
				 * DialogInterface.OnCancelListener() { public void
				 * onCancel(DialogInterface dialog) {
				 * 
				 * final Intent intent = new Intent(mainApp, Singin.class);
				 * intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				 * //connectingIndicator.dismiss(); lccHolder.logout();
				 * mainApp.startActivity(intent); } });
				 */
				connectingIndicator.setCancelable(true);
				connectingIndicator.setIndeterminate(true);
				connectingIndicator.show();
				lccHolder.getAndroid().setConnectingIndicator(connectingIndicator);
			}
		}
	}

	protected void disableScreenLock() {
		final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "CoreActivity");
		wakeLock.setReferenceCounted(false);
		wakeLock.acquire();
	}

	protected void enableScreenLock() {
		if (wakeLock != null) {
			wakeLock.release();
		}
	}

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

	@Override
	public Boolean isUserColorWhite() {
		try {
			return mainApp.getCurrentGame().values.get(AppConstants.WHITE_USERNAME).toLowerCase()
					.equals(mainApp.getSharedData().getString(AppConstants.USERNAME, ""));
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public SoundPlayer getSoundPlayer() {
		return mainApp.getSoundPlayer();
	}

	public LccHolder getLccHolder() {
		return lccHolder;
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

	public MobclixAdView getRectangleAdview() {
		return mainApp.getRectangleAdview();
	}

	public void setRectangleAdview(MobclixAdView rectangleAdview) {
		mainApp.setRectangleAdview(rectangleAdview);
	}

	private void checkUpdate() {
		new CheckUpdateTask(this, mainApp).execute("http://www.chess.com/api/get_android_version");
	}

	private void showNetworkChangeNotification() {
		new AlertDialog.Builder(CoreActivityHome.this).setIcon(android.R.drawable.ic_dialog_alert).setCancelable(false)
				.setTitle("Logout").setMessage("Network was changed. Please relogin to Live")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						// mainApp.setNetworkChangedNotification(false);
						startActivity(new Intent(CoreActivityHome.this, Tabs.class));
					}
				}).create().show();
	}

	/*
	 * private BroadcastReceiver networkChangeNotificationReceiver = new
	 * BroadcastReceiver() {
	 * 
	 * @Override public void onReceive(Context coreContext, Intent intent) { if
	 * (mainApp.isNetworkChangedNotification()) {
	 * showNetworkChangeNotification(); } } };
	 */

	public MainApp getMainApp() {
		return mainApp;
	}

	@Override
	public com.chess.model.Game getCurrentGame() {
		return mainApp.getCurrentGame();
	}
}