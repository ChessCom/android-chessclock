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

	// todo: TESTING
	public static final boolean IS_SHOW_BANNER_ADS = false;
	public static final boolean IS_SHOW_FULLSCREEN_ADS = false;

	public static final String FULLSCREEN_APP_ID = "Android_IA_Test";

	public static void showBannerAd(Button upgradeBtn, InneractiveAd inneractiveAd, /*View adLayout,*/ Context context) {

		inneractiveAd.setInneractiveListener(new InneractiveAdListenerImpl(InneractiveAd.IaAdType.Banner));

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

	public static class InneractiveAdListenerImpl implements com.inneractive.api.ads.InneractiveAdListener {

		private SharedPreferences.Editor preferencesEditor;
		private String adType;
		private boolean isInterstitial;

		public InneractiveAdListenerImpl(InneractiveAd.IaAdType adType) {
			this.adType = adType.toString();
			isInterstitial = adType == InneractiveAd.IaAdType.Interstitial;
		}

		public InneractiveAdListenerImpl(InneractiveAd.IaAdType adType, SharedPreferences.Editor preferencesEditor) {
			this(adType);
			this.preferencesEditor = preferencesEditor;
		}

		private void log(String message) {
			Log.d("InneractiveAd", adType + ": " + message);
		}

		public void onIaAdClicked() {
			log("onIaAdClicked");
		}

		public void onIaAdExpand() {
			log("onIaAdExpand");
		}

		public void onIaAdExpandClosed() {
			log("onIaAdExpandClosed");
		}

		public void onIaAdFailed() {
			log("onIaAdFailed");
		}

		public void onIaAdReceived() {
			log("onIaAdReceived");
			processInterstitialAdReceived();
		}

		public void onIaAdResize() {
			log("onIaAdResize");
		}

		public void onIaAdResizeClosed() {
			log("onIaAdResizeClosed");
		}

		public void onIaDefaultAdReceived() {
			log("onIaDefaultAdReceived");
			processInterstitialAdReceived();
		}

		private void processInterstitialAdReceived() {
			if (isInterstitial) {
				preferencesEditor.putBoolean(AppConstants.FULLSCREEN_AD_ALREADY_SHOWED, true);
				preferencesEditor.commit();
			}
		}
	}
}
