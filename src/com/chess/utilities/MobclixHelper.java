package com.chess.utilities;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.live.client.User;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.MainApp;
import com.mobclix.android.sdk.MobclixAdView;
import com.mobclix.android.sdk.MobclixMMABannerXLAdView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by IntelliJ IDEA.
 * User: vm
 * Date: 15.11.11
 * Time: 0:58
 * To change this template use File | Settings | File Templates.
 */
public class MobclixHelper {

/*	private static java.util.Timer adTimer;

	public static void initializeBannerAdView(Activity activity, MainApp app) {
		if (true) {
//		if (!isShowAds(app)) {
			return;
		}

		MobclixAdView bannerAdview = app.getBannerAdview();
		LinearLayout bannerAdviewWrapper = app.getBannerAdviewWrapper();
		if (bannerAdview == null) {
			bannerAdview = new MobclixMMABannerXLAdView(activity);
			app.setForceBannerAdFirstLoad(true);
			bannerAdview.setRefreshTime(-1);
			MobclixHelper.resumeAdview(bannerAdview, app);
			bannerAdview.addMobclixAdViewListener(new MobclixAdViewListenerImpl(false, app));
		}
		if (bannerAdviewWrapper != null) {
			bannerAdviewWrapper.removeView(bannerAdview);
		}
		bannerAdviewWrapper = (LinearLayout) activity.findViewById(R.id.adview_wrapper);
		bannerAdviewWrapper.addView(bannerAdview);
		bannerAdviewWrapper.setVisibility(View.VISIBLE);
		app.setBannerAdview(bannerAdview);
		app.setBannerAdviewWrapper(bannerAdviewWrapper);
	}

	public static void showBannerAd(Button upgradeBtn, Activity activity, MainApp app) {
		if (true) { //TODO  restore
//		if (!isShowAds(app)) {
			return;
		}

		MobclixAdView bannerAdview = app.getBannerAdview();
		LinearLayout bannerAdviewWrapper = app.getBannerAdviewWrapper();

		if (System.currentTimeMillis() - app.getSharedData().getLong(AppConstants.LAST_ACTIVITY_PAUSED_TIME, 0) > 30000 || app.isForceBannerAdOnFailedLoad()) {
			if (bannerAdviewWrapper != null) {
				bannerAdviewWrapper.removeView(bannerAdview);
			}
			System.out.println("MOBCLIX: FORCE getAd by 30 seconds pause");
			pauseAdview(bannerAdview, app);
			app.setBannerAdview(null);
			app.setBannerAdviewWrapper(null);
		}

		int adsShowCounter = app.getSharedData().getInt(AppConstants.ADS_SHOW_COUNTER, 0);
		if (bannerAdviewWrapper == null || bannerAdview == null) {
			initializeBannerAdView(activity, app);
		} else {
			if (bannerAdviewWrapper != null) {
				bannerAdviewWrapper.removeView(bannerAdview);
			}
			bannerAdviewWrapper = (LinearLayout) activity.findViewById(R.id.adview_wrapper);
			bannerAdviewWrapper.addView(bannerAdview);
			bannerAdviewWrapper.setVisibility(View.VISIBLE);
			app.setBannerAdview(bannerAdview);
			app.setBannerAdviewWrapper(bannerAdviewWrapper);
		}

		if (app.isAdviewPaused()) {
			resumeAdview(bannerAdview, app);
		}

		if (adsShowCounter == 10) {
			//if (!app.adviewPaused) {
			//	pauseAdview(bannerAdview, app);
			//}
			//adviewWrapper.setVisibility(View.GONE);
			upgradeBtn.setVisibility(View.VISIBLE);
			app.getSharedDataEditor().putInt(AppConstants.ADS_SHOW_COUNTER, 0);
			app.getSharedDataEditor().commit();
		} else {
			upgradeBtn.setVisibility(View.GONE);
			bannerAdviewWrapper.setVisibility(View.VISIBLE);
			app.getSharedDataEditor().putInt(AppConstants.ADS_SHOW_COUNTER, adsShowCounter + 1);
			app.getSharedDataEditor().commit();
		}
	}

	public static void resumeAdview(MobclixAdView adview, MainApp app) {
		System.out.println("Mobclix: RESUME");
		if (adview != null) {
			//adview.resume();
			startTimer(adview, app);
		}
		app.setAdviewPaused(false);
	}

	public static void pauseAdview(MobclixAdView adview, MainApp app) {
		System.out.println("Mobclix: PAUSE");
		stopTimer(adview);
		app.setAdviewPaused(true);
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

			result = liveMembershipLevel || echessMembershipLevel
			//((System.currentTimeMillis() - mainApp.getSharedData().getLong(AppConstants.FIRST_TIME_START, 0)) >
			//	(7 * 24 * 60 * 60 * 1000))
		} catch (Exception e) {
			throw new NullPointerException("app.getLccHolder() " + app.getLccHolder() + ", app.getLccHolder().getUser() " + app.getLccHolder().getUser() + ", lccUser.getMembershipLevel() " + lccUser.getMembershipLevel() + ", app.getSharedData() " + app.getSharedData() + ", app.getSharedData().getString(\"premium_status\", \"0\") " + app.getSharedData().getString(AppConstants.USER_PREMIUM_STATUS, "0"));
		}
		return result;
	}

	public static void hideBannerAd(MainApp app, TextView removeAds) {
		getBannerAdviewWrapper(app).setVisibility(View.GONE);
		removeAds.setVisibility(View.GONE);
		pauseAdview(getBannerAdview(app), app);
	}

	public static LinearLayout getBannerAdviewWrapper(MainApp app) {
		return app.getBannerAdviewWrapper();
	}

	public static void setBannerAdviewWrapper(LinearLayout bannerAdviewWrapper, MainApp app) {
		app.setBannerAdviewWrapper(bannerAdviewWrapper);
	}

	public static MobclixAdView getBannerAdview(MainApp app) {
		return app.getBannerAdview();
	}

	public static void setBannerAdview(MobclixAdView bannerAdview, MainApp app) {
		app.setBannerAdview(bannerAdview);
	}

	private static void startTimer(final MobclixAdView adview, final MainApp mainApp) {
		if (!isShowAds(mainApp)) {
			return;
		}
		adTimer = new java.util.Timer();
		adTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (!mainApp.isAdviewPaused()) {
					getAd(adview);
				}
			}
		}, 0, 20000);
	}

	private static void stopTimer(MobclixAdView adview) {
		if (adview != null) {
			adview.cancelAd();
		}
		if (adTimer != null) {
			adTimer.cancel();
		}
	}

	public static void getAd(MobclixAdView adview) {
		System.out.println("MOBCLIX: getAd");
		adview.getAd();
	}

	public static Timer getAdTimer() {
		return adTimer;
	}*/
}
