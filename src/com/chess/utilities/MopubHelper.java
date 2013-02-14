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

	public static void showBannerAd(Button upgradeBtn, MoPubView moPubAdView, Context context) {
		/*if (!AppUtils.isNeedToUpgrade(context)) {
			return;
		}*/

		SharedPreferences preferences = AppData.getPreferences(context);
		SharedPreferences.Editor preferencesEditor = preferences.edit();

		int adsShowCounter = preferences.getInt(AppConstants.ADS_SHOW_COUNTER, 0);

		if (adsShowCounter != AppConstants.UPGRADE_SHOW_COUNTER) {
			upgradeBtn.setVisibility(View.GONE);
			setListener(moPubAdView, new MopubListener());
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
		setListener(rectangleAdView, new MopubListener());
	}

    public static void destroyRectangleAd(){
        if (rectangleAdView != null)
            rectangleAdView.destroy();
    }

	public static void showRectangleAd(LinearLayout wrapper, Context context) {
		/*if (!AppUtils.isNeedToUpgrade(context) || rectangleAdView == null) {
			return;
		}*/

		SharedPreferences preferences = AppData.getPreferences(context);
		SharedPreferences.Editor preferencesEditor = preferences.edit();

		/*if (rectangleAdView == null) {
			createRectangleAd(app);
		}*/

		if (rectangleAdWrapper != null /*&& rectangleAdView != null*/) { // rectangleAdView != null always true
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

	public static void setListener(MoPubView mopPubView, MopubListener mopubListener) {
		mopPubView.setOnAdClickedListener(mopubListener);
		mopPubView.setOnAdWillLoadListener(mopubListener);
		mopPubView.setOnAdLoadedListener(mopubListener);
		mopPubView.setOnAdFailedListener(mopubListener);
		mopPubView.setOnAdPresentedOverlayListener(mopubListener);
		mopPubView.setOnAdClosedListener(mopubListener);
	}

}