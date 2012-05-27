package com.chess.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.mopub.mobileads.MoPubView;

public class MopubHelper {

	private static final String MOPUB_AD_BANNER_ID = "agltb3B1Yi1pbmNyDQsSBFNpdGUYlvOBEww";
	private static final String MOPUB_AD_RECTANGLE_ID = "agltb3B1Yi1pbmNyDQsSBFNpdGUYtfH_Egw";


	private static LinearLayout rectangleAdWrapper;
	private static MoPubView rectangleAdView;

	//	public static void showBannerAd(Button upgradeBtn, MoPubView moPubAdView, MainApp app) {
	public static void showBannerAd(Button upgradeBtn, MoPubView moPubAdView, Context context) {
		SharedPreferences preferences = AppData.getPreferences(context);
		SharedPreferences.Editor preferencesEditor = preferences.edit();

		if (!isShowAds(context)) {
			return;
		}

		int adsShowCounter = preferences.getInt(AppConstants.ADS_SHOW_COUNTER, 0);

		if (adsShowCounter != 10) {
			upgradeBtn.setVisibility(View.GONE);
			moPubAdView.setVisibility(View.VISIBLE);
			moPubAdView.setAdUnitId(MOPUB_AD_BANNER_ID);
			moPubAdView.loadAd();
			preferencesEditor.putInt(AppConstants.ADS_SHOW_COUNTER, adsShowCounter + 1);
			preferencesEditor.commit();
		} else {
			moPubAdView.setVisibility(View.GONE);
			upgradeBtn.setVisibility(View.VISIBLE);
			preferencesEditor.putInt(AppConstants.ADS_SHOW_COUNTER, 0);
			preferencesEditor.commit();
		}
	}

	public static void createRectangleAd(Context context) {
		rectangleAdView = new MoPubView(context);
		rectangleAdView.setAdUnitId(MOPUB_AD_RECTANGLE_ID);
	}

    public static void destroyRectangleAd(){
        if(rectangleAdView != null)
            rectangleAdView.destroy();
    }

//	public static void showRectangleAd(LinearLayout wrapper, MainApp app) {
	public static void showRectangleAd(LinearLayout wrapper, Context context) {
		SharedPreferences preferences = AppData.getPreferences(context);
		SharedPreferences.Editor preferencesEditor = preferences.edit();

		if (!isShowAds(context) || rectangleAdView == null) {
			return;
		}

		/*if (rectangleAdView == null) {
			createRectangleAd(app);
		}*/

		if (rectangleAdWrapper != null && rectangleAdView != null) {
			rectangleAdWrapper.removeView(rectangleAdView);
		}
		rectangleAdWrapper = wrapper;

		//moPubAdView.setVisibility(View.VISIBLE);
		wrapper.addView(rectangleAdView);
		rectangleAdView.loadAd();

		int adsShowCounter = preferences.getInt(AppConstants.ADS_SHOW_COUNTER, 0);
		preferencesEditor.putInt(AppConstants.ADS_SHOW_COUNTER, adsShowCounter + 1);
		preferencesEditor.commit();

	}

//	public static boolean isShowAds(MainApp app) {
	public static boolean isShowAds(Context context) {
		boolean result;
		return true;
//			result = AppUtils.isNeedToUpgrade(context);


/*
		// -------------  DO NOT USE ANYMORE --------------------------------------
		User lccUser = null;
		try {
			lccUser = LccHolder.getInstance(context).getUser();

			boolean liveMembershipLevel =
					lccUser != null ? app.isLiveChess() && (lccUser.getMembershipLevel() < 30) && !app.getLccHolder().isConnectingInProgress() : false;

			boolean echessMembershipLevel = !app.isLiveChess() && Integer.parseInt(
					app.getSharedData().getString(AppConstants.USER_PREMIUM_STATUS, "0")) < 1;

			result = liveMembershipLevel || echessMembershipLevel*/
/*((System.currentTimeMillis() - mainApp.getSharedData().getLong(AppConstants.FIRST_TIME_START, 0)) >
				(7 * 24 * 60 * 60 * 1000)) && *//*
;
		} catch (Exception e) {
			throw new NullPointerException("app.getLccHolder() " + app.getLccHolder() + ", app.getLccHolder().getUser() " + app.getLccHolder().getUser() + ", lccUser.getMembershipLevel() " + lccUser.getMembershipLevel() + ", app.getSharedData() " + app.getSharedData() + ", app.getSharedData().getString(\"premium_status\", \"0\") " + app.getSharedData().getString(AppConstants.USER_PREMIUM_STATUS, "0"));
		}
		return result;
*/
	}
}