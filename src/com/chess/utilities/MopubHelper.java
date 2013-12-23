package com.chess.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import com.chess.statics.AppConstants;
import com.chess.statics.AppData;
import com.mopub.mobileads.MoPubView;

public class MopubHelper {

	// test ad ids
	private static final String MOPUB_AD_BANNER_ID = "agltb3B1Yi1pbmNyDAsSBFNpdGUY8fgRDA"; // test sample
	private static final String MOPUB_AD_RECTANGLE_ID = "agltb3B1Yi1pbmNyDQsSBFNpdGUYqKO5CAw"; // test sample
	//public static final String MOPUB_AD_INTERSTITIAL_ID = "agltb3B1Yi1pbmNyDAsSBFNpdGUY6tERDA"; // test sample

	// todo: uncomment for prod ads
	// production ad ids
	/*private static final String MOPUB_AD_BANNER_ID = "agltb3B1Yi1pbmNyDQsSBFNpdGUYlvOBEww";
	private static final String MOPUB_AD_RECTANGLE_ID = "agltb3B1Yi1pbmNyDQsSBFNpdGUYtfH_Egw";
	//public static final String MOPUB_AD_INTERSTITIAL_ID = "agltb3B1Yi1pbmNyDQsSBFNpdGUYwLyBEww";*/

	public static final boolean IS_SHOW_FULLSCREEN_ADS = false;
	public static final String FULLSCREEN_TAG_LOG = "MopubFullscreenLog";

	//private static LinearLayout rectangleAdWrapper;
	private static MoPubView rectangle;

	public static MoPubView showBannerAd(Button upgradeBtn, LinearLayout mopubAdLayout, Context context) {
		/*if (!AppUtils.isNeedToUpgrade(context)) {
			return;
		}*/

		AppData appData = new AppData(context);

		SharedPreferences preferences = appData.getPreferences();
		SharedPreferences.Editor preferencesEditor = preferences.edit();

		int adsShowCounter = preferences.getInt(AppConstants.ADS_SHOW_COUNTER, 0);

		MoPubView moPubAdView = null;

		if (adsShowCounter != AppConstants.UPGRADE_SHOW_COUNTER) {

			moPubAdView = new MoPubView(context);
			mopubAdLayout.addView(moPubAdView);

			upgradeBtn.setVisibility(View.GONE);
			setListener(moPubAdView, new MopubListener());
			mopubAdLayout.setVisibility(View.VISIBLE);
			moPubAdView.setAdUnitId(MOPUB_AD_BANNER_ID);
			moPubAdView.loadAd();
			preferencesEditor.putInt(AppConstants.ADS_SHOW_COUNTER, adsShowCounter + 1);
			preferencesEditor.commit();
		} else {
			mopubAdLayout.setVisibility(View.GONE);
			upgradeBtn.setVisibility(View.VISIBLE);
			preferencesEditor.putInt(AppConstants.ADS_SHOW_COUNTER, 0);
			preferencesEditor.commit();
		}

		return moPubAdView;
	}

	/*public static void createRectangleAd(Context context) {
		rectangleAdView = new MoPubView(context);
		rectangleAdView.setAdUnitId(MOPUB_AD_RECTANGLE_ID);
		setListener(rectangleAdView, new MopubListener());
	}*/

	/*public static void destroyRectangleAd() {
		if (rectangle != null)
			rectangle.destroy();
	}*/

	public static void showRectangleAd(MoPubView rectangleAdView, Context context) {
		/*if (!AppUtils.isNeedToUpgrade(context) || rectangleAdView == null) {
			return;
		}*/

		rectangle = rectangleAdView;
		rectangleAdView.setAdUnitId(MOPUB_AD_RECTANGLE_ID);
		setListener(rectangleAdView, new MopubListener());

		AppData appData = new AppData(context);
		SharedPreferences preferences = appData.getPreferences();
		SharedPreferences.Editor preferencesEditor = preferences.edit();

		/*if (rectangleAdView == null) {
			createRectangleAd(app);
		}*/

		/*if (rectangleAdWrapper != null *//*&& rectangleAdView != null*//*) { // rectangleAdView != null always true
			rectangleAdWrapper.removeView(rectangleAdView);
		}
		rectangleAdWrapper = wrapper;*/

		//moPubAdView.setVisibility(View.VISIBLE);
		//wrapper.addView(rectangleAdView);
		rectangleAdView.loadAd();

		int adsShowCounter = preferences.getInt(AppConstants.ADS_SHOW_COUNTER, 0);
		preferencesEditor.putInt(AppConstants.ADS_SHOW_COUNTER, adsShowCounter + 1);
		preferencesEditor.commit();
	}

	public static void setListener(MoPubView mopPubView, MoPubView.BannerAdListener bannerAdListener) {
		mopPubView.setBannerAdListener(bannerAdListener);
	}

}