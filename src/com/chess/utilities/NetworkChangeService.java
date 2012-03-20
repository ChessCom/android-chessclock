package com.chess.utilities;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import com.chess.ui.core.MainApp;
import com.chess.lcc.android.LccHolder;

public class NetworkChangeService extends Service {

	public MainApp mainApp;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onCreate() {
		super.onCreate();
		mainApp = (MainApp) getApplication();
		registerReceiver(networkChangeReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
	}

	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(networkChangeReceiver);
	}

	private BroadcastReceiver networkChangeReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {

			if (!mainApp.isLiveChess()) {
				return;
			}

			LccHolder lccHolder = mainApp.getLccHolder();

			/*boolean failover = intent.getBooleanExtra("FAILOVER_CONNECTION", false);
					System.out.println("!!!!!!!! NetworkChangeReceiver failover=" + failover);*/

			final ConnectivityManager connectivityManager = (ConnectivityManager)
					context.getSystemService(Context.CONNECTIVITY_SERVICE);

			/*final android.net.NetworkInfo wifi =
						connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

					final android.net.NetworkInfo mobile =
						connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

					System.out.println("!!!!!!!! NetworkChangeReceiver failover wifi=" + wifi.isFailover() + ", mobile=" + mobile.isFailover());*/

			NetworkInfo[] networkInfo
					= connectivityManager.getAllNetworkInfo();

			for (int i = 0; i < networkInfo.length; i++) {
				if (networkInfo[i].isConnected()) {
					System.out.println("LCCLOG: NetworkChangeReceiver isConnected " + networkInfo[i].getTypeName());
					if (lccHolder.getNetworkTypeName() != null && !networkInfo[i].getTypeName().equals(lccHolder.getNetworkTypeName())) {
						lccHolder.logout();
						//mainApp.setNetworkChangedNotification(true);
						lccHolder.getAndroid().getContext().sendBroadcast(new Intent("com.chess.lcc.android-network-change"));
					} else {
						lccHolder.setNetworkTypeName(networkInfo[i].getTypeName());
					}
				}
			}
		}
	};
}
