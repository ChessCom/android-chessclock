package com.chess.utilities;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import com.chess.statics.AppConstants;
import com.chess.statics.AppData;
import com.mopub.mobileads.MoPubView;

public class MopubHelper {

	// test ad ids
//	private static final String MOPUB_AD_BANNER_ID = "agltb3B1Yi1pbmNyDAsSBFNpdGUY8fgRDA"; // test sample
//	private static final String MOPUB_AD_RECTANGLE_ID = "agltb3B1Yi1pbmNyDQsSBFNpdGUYqKO5CAw"; // test sample
	//public static final String MOPUB_AD_INTERSTITIAL_ID = "agltb3B1Yi1pbmNyDAsSBFNpdGUY6tERDA"; // test sample

	// todo: uncomment for prod ads
	// production ad ids
	private static final String MOPUB_AD_BANNER_ID = "agltb3B1Yi1pbmNyDQsSBFNpdGUYlvOBEww";
	private static final String MOPUB_AD_RECTANGLE_ID = "agltb3B1Yi1pbmNyDQsSBFNpdGUYtfH_Egw";
//	private static final String MOPUB_AD_300x250_ID = "agltb3B1Yi1pbmNyDQsSBFNpdGUY6Y2BEww";
//	private static final String MOPUB_AD_728x90_ID = "agltb3B1Yi1pbmNyDQsSBFNpdGUYt8eAEww";
	//public static final String MOPUB_AD_INTERSTITIAL_ID = "agltb3B1Yi1pbmNyDQsSBFNpdGUYwLyBEww";


	public static MoPubView showBannerAd(Button upgradeBtn, LinearLayout mopubAdLayout, Context context) {

		AppData appData = new AppData(context);
		int adsShowCounter = appData.getAdsShowCounter();

		MoPubView moPubAdView = null;

		if (adsShowCounter != AppConstants.UPGRADE_SHOW_COUNTER) {
			moPubAdView = new MoPubView(context);
			mopubAdLayout.addView(moPubAdView);

			upgradeBtn.setVisibility(View.GONE);
			setListener(moPubAdView, new MopubListener());
			mopubAdLayout.setVisibility(View.VISIBLE);
//			if (AppUtils.isTablet(context)) {
//				moPubAdView.setAdUnitId(MOPUB_AD_728x90_ID);
//			} else {
				moPubAdView.setAdUnitId(MOPUB_AD_BANNER_ID);
//			}

			moPubAdView.loadAd();

			appData.setAdsShowCounter(adsShowCounter + 1);
		} else {
			mopubAdLayout.setVisibility(View.GONE);
			upgradeBtn.setVisibility(View.VISIBLE);

			appData.setAdsShowCounter(0);
		}

		return moPubAdView;
	}

	public static void showRectangleAd(MoPubView moPubView, Context context) {

		setListener(moPubView, new MopubListener());
//		if (AppUtils.isSmallScreen(context)) { // doesn't work because of  Not enough space to show ad! Wants: <320, 50>, Has: <304, 50>. Let's try with next SDK, maybe will be solved
//			moPubView.setAdUnitId(MOPUB_AD_300x250_ID);
//		} else {
			moPubView.setAdUnitId(MOPUB_AD_RECTANGLE_ID);
//		}

		moPubView.loadAd();

		AppData appData = new AppData(context);
		appData.setAdsShowCounter(appData.getAdsShowCounter() + 1);
	}

	public static void setListener(MoPubView mopPubView, MoPubView.BannerAdListener bannerAdListener) {
		mopPubView.setBannerAdListener(bannerAdListener);
	}

}