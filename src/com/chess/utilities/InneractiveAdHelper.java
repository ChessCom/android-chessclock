package com.chess.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.inneractive.api.ads.InneractiveAd;

public class InneractiveAdHelper {

	// todo: flags should be removed
	public static final boolean IS_SHOW_BANNER_ADS = true;
	public static final boolean IS_SHOW_FULLSCREEN_ADS = true;

	/*private static LinearLayout rectangleAdWrapper;
	private static InneractiveAd rectangleAdView;*/

	public static void showBannerAd(Button upgradeBtn, InneractiveAd bannerAd, Context context) {

		if (!AppUtils.isNeedToUpgrade(context)) {
			return;
		}

		bannerAd.setInneractiveListener(new InneractiveAdListenerImpl(AppConstants.AD_BANNER));

		SharedPreferences preferences = AppData.getPreferences(context);
		SharedPreferences.Editor preferencesEditor = preferences.edit();

		int adsShowCounter = preferences.getInt(AppConstants.ADS_SHOW_COUNTER, 0);

		if (adsShowCounter != AppConstants.UPGRADE_SHOW_COUNTER) {
			upgradeBtn.setVisibility(View.GONE);
			if (bannerAd.getVisibility() != View.VISIBLE) {
				bannerAd.setVisibility(View.VISIBLE);
			}
			// todo: initialize inneractiveAd object here, if necessary
			preferencesEditor.putInt(AppConstants.ADS_SHOW_COUNTER, adsShowCounter + 1);
			preferencesEditor.commit();
		} else {
			// todo: try to do not load ad when upgrade button is showing
			bannerAd.setVisibility(View.GONE);
			upgradeBtn.setVisibility(View.VISIBLE);
			preferencesEditor.putInt(AppConstants.ADS_SHOW_COUNTER, 0);
			preferencesEditor.commit();
		}
	}

	public static void showRectangleAd(InneractiveAd rectangleAd, Context context) {
		if (!AppUtils.isNeedToUpgrade(context) || rectangleAd == null) {
			return;
		}

		rectangleAd.setInneractiveListener(new InneractiveAdListenerImpl(AppConstants.AD_RECTANGLE));

		SharedPreferences preferences = AppData.getPreferences(context);
		SharedPreferences.Editor preferencesEditor = preferences.edit();

		if (rectangleAd.getVisibility() != View.VISIBLE) {
			rectangleAd.setVisibility(View.VISIBLE);
		}

		int adsShowCounter = preferences.getInt(AppConstants.ADS_SHOW_COUNTER, 0);
		preferencesEditor.putInt(AppConstants.ADS_SHOW_COUNTER, adsShowCounter + 1);
		preferencesEditor.commit();
	}

	public static class InneractiveAdListenerImpl implements com.inneractive.api.ads.InneractiveAdListener {

		private SharedPreferences.Editor preferencesEditor;
		private String adType;

		public InneractiveAdListenerImpl(String adType) {
			this.adType = adType;
		}

		public InneractiveAdListenerImpl(String adType, SharedPreferences.Editor preferencesEditor) {
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

		public void onIaDismissScreen() {
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
			if (adType.equals(AppConstants.AD_FULLSCREEN)) {
				preferencesEditor.putBoolean(AppConstants.FULLSCREEN_AD_ALREADY_SHOWED, true);
				preferencesEditor.commit();
			}
		}
	}
}
