package com.chess.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.inneractive.api.ads.InneractiveAd;
import com.inneractive.api.ads.InneractiveAdListener;

public class InneractiveAdHelper {

	public static final boolean IS_SHOW_BANNER_ADS = true;

	public static void showBannerAd(Button upgradeBtn, InneractiveAd inneractiveAd, /*View adLayout,*/ Context context) {

		inneractiveAd.setInneractiveListener(new InneractiveAdListener());

		SharedPreferences preferences = AppData.getPreferences(context);
		SharedPreferences.Editor preferencesEditor = preferences.edit();

		if (!AppUtils.isNeedToUpgrade(context)) {
			return;
		}

		int adsShowCounter = preferences.getInt(AppConstants.ADS_SHOW_COUNTER, 0);

		if (adsShowCounter != AppConstants.UPGRADE_SHOW_COUNTER) {
			upgradeBtn.setVisibility(View.GONE);
			inneractiveAd.setVisibility(View.VISIBLE);
			// todo: initialize inneractiveAd object here, if necessary
			preferencesEditor.putInt(AppConstants.ADS_SHOW_COUNTER, adsShowCounter + 1);
			preferencesEditor.commit();
		} else {
			inneractiveAd.setVisibility(View.GONE);
			upgradeBtn.setVisibility(View.VISIBLE);
			preferencesEditor.putInt(AppConstants.ADS_SHOW_COUNTER, 0);
			preferencesEditor.commit();
		}
	}

	public static class InneractiveAdListener implements com.inneractive.api.ads.InneractiveAdListener {

		public void onIaAdClicked() {
			Log.d("InneractiveAd", "onIaAdClicked");
		}

		public void onIaAdExpand() {
			Log.d("InneractiveAd", "onIaAdExpand");
		}

		public void onIaAdExpandClosed() {
			Log.d("InneractiveAd", "onIaAdExpandClosed");
		}

		public void onIaAdFailed() {
			Log.d("InneractiveAd", "onIaAdFailed");
		}

		public void onIaAdReceived() {
			Log.d("InneractiveAd", "onIaAdReceived");
		}

		public void onIaAdResize() {
			Log.d("InneractiveAd", "onIaAdResize");
		}

		public void onIaAdResizeClosed() {
			Log.d("InneractiveAd", "onIaAdResizeClosed");
		}

		public void onIaDefaultAdReceived() {
			Log.d("InneractiveAd", "onIaDefaultAdReceived");
		}
	}
}