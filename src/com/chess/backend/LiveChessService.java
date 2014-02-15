package com.chess.backend;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import com.chess.R;
import com.chess.lcc.android.LiveConnectionHelper;
import com.chess.model.DataHolder;
import com.chess.statics.IntentConstants;
import com.chess.ui.activities.MainFragmentFaceActivity;
import com.chess.utilities.LogMe;

public class LiveChessService extends Service {

	private static final String TAG = "LCCLOG-LiveChessService";
	private static final int GO_TO_LIVE = 11;

	private ServiceBinder serviceBinder = new ServiceBinder();
	private LiveConnectionHelper liveConnectionHelper;

	public class ServiceBinder extends Binder {
		public LiveChessService getService() {
			LogMe.dl(TAG, "SERVICE: getService called");
			return LiveChessService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		LogMe.dl(TAG, "SERVICE: onBind @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		if (liveConnectionHelper == null) {
			liveConnectionHelper = new LiveConnectionHelper(this);
		}
		return serviceBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		LogMe.dl(TAG, "SERVICE: onUnbind, this service have no binders anymore");

		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		LogMe.dl(TAG, "SERVICE: onStartCommand");

		if (DataHolder.getInstance().isLiveChess()) {

			if (liveConnectionHelper == null) {
				liveConnectionHelper = new LiveConnectionHelper(this);
			}
			if (liveConnectionHelper.isLiveChessEventListenerSet()) {
				liveConnectionHelper.checkAndConnectLiveClient();
			}
		} else {

			// lets try this way
			stop(); // todo: to vm: What does that mean?
		}

		// try to use START_NOT_STICKY as main mode,
		// because system will keep service started when app is in foreground,
		// and we anyway kill service in 30sec if it's in the background

		// todo: to vm: system shouldn't re-create service which has no binders. So when we shutdown service that means that activity is no longer exist.
		// todo: to vm: if we do leave when app is in foreground, we only do leave for live chess client, and do not shutdown service itself. Please review this logic as it might be incorrect.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		LogMe.dl(TAG, "SERVICE: onDestroy");
		if (liveConnectionHelper != null) {
			liveConnectionHelper.leave();
			liveConnectionHelper = null;
		}
		//stopForeground(true);
		//unregisterReceiver(networkChangeReceiver);
	}


	@Override
	public void onRebind(Intent intent) {
		super.onRebind(intent);
		LogMe.dl(TAG, "SERVICE: onRebind @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		LogMe.dl(TAG, "SERVICE: onStart @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
	}

	public void stop() {
		stopSelf();
		stopForeground(true);
	}

	public void onLiveConnected() {

		// Show status bar for ongoing notification
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);

		Intent notifyIntent = new Intent(getContext(), MainFragmentFaceActivity.class);
		notifyIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		notifyIntent.putExtra(IntentConstants.LIVE_CHESS, true);

		// Creates the PendingIntent
		PendingIntent pendingIntent = PendingIntent.getActivity(this, GO_TO_LIVE, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		Bitmap bigImage = ((BitmapDrawable) getResources().getDrawable(R.drawable.ic_stat_chess)).getBitmap();
		String title = getString(R.string.live_chess_connection);
		String body = getString(R.string.live_chess_connected_description);

		notificationBuilder.setContentTitle(title)
				.setTicker(title)
				.setContentText(body)
				.setSmallIcon(R.drawable.ic_stat_live)
				.setLargeIcon(bigImage);

		// Puts the PendingIntent into the notification builder
		notificationBuilder.setContentIntent(pendingIntent);
		notificationBuilder.setOngoing(true);

		startForeground(R.drawable.ic_stat_live, notificationBuilder.build());
	}

	private Context getContext() {
		return this;
	}

	public LiveConnectionHelper getLiveConnectionHelper() {
		return liveConnectionHelper;
	}

//	private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {
//
//		@Override
//		public void onReceive(final Context context, final Intent intent) {
//
//			// todo: improve and refactor, just used old code.
//			// OtherClientEntered problem is here
//
//			if (!appData.isLiveChess()) {
//				return;
//			}
//
//			//LccHelper lccHolder = LccHelper.getInstance(context);
//
//			boolean failOver = intent.getBooleanExtra("FAIL_OVER_CONNECTION", false);
//			LogMe.dl(TAG, "NetworkChangeReceiver failOver=" + failOver);
//
//			final ConnectivityManager connectivityManager = (ConnectivityManager)
//					context.getSystemService(Context.CONNECTIVITY_SERVICE);
//
//			final NetworkInfo wifi =
//					connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
//
//			final NetworkInfo mobile =
//					connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
//
//			if (wifi != null && mobile != null) {
//				LogMe.dl(TAG, "NetworkChangeReceiver failOver wifi=" + wifi.isFailover() + ", mobile=" + mobile.isFailover());
//			}
//
//			NetworkInfo[] networkInfo
//					= connectivityManager.getAllNetworkInfo();
//
//			if (networkInfo != null) {
//				for (NetworkInfo aNetworkInfo : networkInfo) {
//					if (aNetworkInfo.isConnected()) {
//						LogMe.dl(TAG, "NetworkChangeReceiver isConnected " + aNetworkInfo.getTypeName());
//
//						// todo: check NPE
//						if (lccHelper.getNetworkTypeName() != null && !aNetworkInfo.getTypeName().equals(lccHelper.getNetworkTypeName())) {
//
//						/*((LiveChessClientImpl) lccHelper.getClient()).leave();
//						lccHelper.runConnectTask();*/
//
//							//setNetworkChangedNotification(true);
//							lccHelper.getContext().sendBroadcast(new Intent("com.chess.lcc.android-network-change"));
//						} else {
//							lccHelper.setNetworkTypeName(aNetworkInfo.getTypeName());
//						}
//					}
//				}
//
//			}
//		}
}