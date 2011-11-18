package com.chess.utilities;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chess.R;
import com.chess.core.MainApp;
import com.chess.live.client.User;
import com.mobclix.android.sdk.MobclixAdView;
import com.mobclix.android.sdk.MobclixMMABannerXLAdView;

/**
 * Created by IntelliJ IDEA.
 * User: vm
 * Date: 15.11.11
 * Time: 0:58
 * To change this template use File | Settings | File Templates.
 */
public class MobclixHelper {

	public static void initializeBannerAdView(Activity activity, MainApp app) {
		MobclixAdView bannerAdview = app.getBannerAdview();
		LinearLayout bannerAdviewWrapper = app.bannerAdviewWrapper;
		if (bannerAdview == null) {
			bannerAdview = new MobclixMMABannerXLAdView(activity);
			bannerAdview.addMobclixAdViewListener(new MobclixAdViewListenerImpl());
		}
		if (bannerAdviewWrapper != null) {
			bannerAdviewWrapper.removeView(bannerAdview);
		}
		bannerAdviewWrapper = (LinearLayout) activity.findViewById(R.id.adview_wrapper);
		bannerAdviewWrapper.addView(bannerAdview);
		bannerAdviewWrapper.setVisibility(View.VISIBLE);
		app.setBannerAdview(bannerAdview);
		app.bannerAdviewWrapper = bannerAdviewWrapper;
	}

	public static void showBannerAd(LinearLayout adviewWrapper, TextView removeAds, Activity activity, MainApp app) {
		MobclixAdView bannerAdview = app.getBannerAdview();
		LinearLayout bannerAdviewWrapper = app.bannerAdviewWrapper;
		int adsShowCounter = app.sharedData.getInt("com.chess.adsShowCounter", 0);
		if (adviewWrapper == null || bannerAdview == null) {
			initializeBannerAdView(activity, app);
		} else {
			if (bannerAdviewWrapper != null) {
				bannerAdviewWrapper.removeView(bannerAdview);
			}
			bannerAdviewWrapper = (LinearLayout) activity.findViewById(R.id.adview_wrapper);
			bannerAdviewWrapper.addView(bannerAdview);
			bannerAdviewWrapper.setVisibility(View.VISIBLE);
			app.setBannerAdview(bannerAdview);
			app.bannerAdviewWrapper = bannerAdviewWrapper;
		}

		if (adsShowCounter == 10) {
			if (!app.adviewPaused) {
				pauseAdview(bannerAdview, app);
			}
			//adviewWrapper.setVisibility(View.GONE);
			removeAds.setVisibility(View.VISIBLE);
			app.SDeditor.putInt("com.chess.adsShowCounter", 0);
			app.SDeditor.commit();
		} else {
			if (app.adviewPaused) {
				resumeAdview(bannerAdview, app);
			}
			removeAds.setVisibility(View.GONE);
			adviewWrapper.setVisibility(View.VISIBLE);
			app.SDeditor.putInt("com.chess.adsShowCounter", adsShowCounter + 1);
			app.SDeditor.commit();
		}
	}

	public static void resumeAdview(MobclixAdView adview, MainApp app) {
		System.out.println("Mobclix: RESUME");
		if (adview != null) {
			//adview.getAd();
			adview.resume();
		}
		app.adviewPaused = false;
	}

	public static void pauseAdview(MobclixAdView adview, MainApp app) {
		System.out.println("Mobclix: PAUSE");

		if (adview != null) {
			adview.pause();
		}
		app.adviewPaused = true;
	}

	public static boolean isShowAds(MainApp app) {
		boolean result = false;
		User lccUser = null;
		try {

		lccUser = app.getLccHolder().getUser();
		boolean liveMembershipLevel =
				lccUser != null ? app.isLiveChess() && (lccUser.getMembershipLevel() < 30) : false;

		result = /*((System.currentTimeMillis() - App.sharedData.getLong("com.chess.firstTimeStart", 0)) >
            (7 * 24 * 60 * 60 * 1000)) && */(liveMembershipLevel || (!app.isLiveChess() && Integer.parseInt(
				app.sharedData.getString("premium_status", "0")) < 1));
		}
		catch (Exception e)
		{
			throw new NullPointerException("app.getLccHolder() " + app.getLccHolder() + ", app.getLccHolder().getUser() " + app.getLccHolder().getUser() + ", lccUser.getMembershipLevel() " + lccUser.getMembershipLevel() + ", app.sharedData " + app.sharedData + ", app.sharedData.getString(\"premium_status\", \"0\") " + app.sharedData.getString("premium_status", "0"));
		}
		return result;
	}

	public static LinearLayout getBannerAdviewWrapper(MainApp app) {
		return app.bannerAdviewWrapper;
	}

	public static void setBannerAdviewWrapper(LinearLayout bannerAdviewWrapper, MainApp app) {
		app.bannerAdviewWrapper = bannerAdviewWrapper;
	}

	public static MobclixAdView getBannerAdview(MainApp app) {
		return app.getBannerAdview();
	}

	public static void setBannerAdview(MobclixAdView bannerAdview, MainApp app) {
		app.setBannerAdview(bannerAdview);
	}
}
