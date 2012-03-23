package com.chess.utilities;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.chess.live.client.User;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.MainApp;
import com.mopub.mobileads.MoPubView;

public class MopubHelper {

	public static void showBannerAd(Button upgradeBtn, MoPubView moPubAdView, MainApp app) {
		if (!isShowAds(app)) {
			return;
		}

		/*upgradeBtn.setVisibility(View.VISIBLE);
		moPubAdView.setVisibility(View.VISIBLE);*/

		int adsShowCounter = app.getSharedData().getInt(AppConstants.ADS_SHOW_COUNTER, 0);

		if (adsShowCounter != 10) {
			upgradeBtn.setVisibility(View.GONE);
			moPubAdView.setVisibility(View.VISIBLE);
			moPubAdView.setAdUnitId("agltb3B1Yi1pbmNyDQsSBFNpdGUYlvOBEww");
			moPubAdView.loadAd();
			app.getSharedDataEditor().putInt(AppConstants.ADS_SHOW_COUNTER, adsShowCounter + 1);
			app.getSharedDataEditor().commit();
		} else {
			moPubAdView.setVisibility(View.GONE);
			upgradeBtn.setVisibility(View.VISIBLE);
			app.getSharedDataEditor().putInt(AppConstants.ADS_SHOW_COUNTER, 0);
			app.getSharedDataEditor().commit();
		}
	}

	public static boolean isShowAds(MainApp app) {
		boolean result;
		User lccUser = null;
		try {
			lccUser = app.getLccHolder().getUser();
			boolean liveMembershipLevel =
					lccUser != null ? app.isLiveChess() && (lccUser.getMembershipLevel() < 30) && !app.getLccHolder().isConnectingInProgress() : false;

			boolean echessMembershipLevel = !app.isLiveChess() && Integer.parseInt(
					app.getSharedData().getString(AppConstants.USER_PREMIUM_STATUS, "0")) < 1;

			result = liveMembershipLevel || echessMembershipLevel/*((System.currentTimeMillis() - mainApp.getSharedData().getLong(AppConstants.FIRST_TIME_START, 0)) >
				(7 * 24 * 60 * 60 * 1000)) && */;
		} catch (Exception e) {
			throw new NullPointerException("app.getLccHolder() " + app.getLccHolder() + ", app.getLccHolder().getUser() " + app.getLccHolder().getUser() + ", lccUser.getMembershipLevel() " + lccUser.getMembershipLevel() + ", app.getSharedData() " + app.getSharedData() + ", app.getSharedData().getString(\"premium_status\", \"0\") " + app.getSharedData().getString(AppConstants.USER_PREMIUM_STATUS, "0"));
		}
		return result;
	}
}