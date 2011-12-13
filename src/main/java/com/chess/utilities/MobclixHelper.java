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

	private static java.util.Timer adTimer;

	public static void initializeBannerAdView(Activity activity, MainApp app)
	{
		if (!isShowAds(app))
		{
			return;
		}

		MobclixAdView bannerAdview = app.getBannerAdview();
		LinearLayout bannerAdviewWrapper = app.getBannerAdviewWrapper();
		if (bannerAdview == null)
		{
			bannerAdview = new MobclixMMABannerXLAdView(activity);
			app.setForceBannerAdFirstLoad(true);
			bannerAdview.setRefreshTime(-1);
			MobclixHelper.resumeAdview(bannerAdview, app);
			bannerAdview.addMobclixAdViewListener(new MobclixAdViewListenerImpl(false, app));
		}
		if (bannerAdviewWrapper != null)
		{
			bannerAdviewWrapper.removeView(bannerAdview);
		}
		bannerAdviewWrapper = (LinearLayout) activity.findViewById(R.id.adview_wrapper);
		bannerAdviewWrapper.addView(bannerAdview);
		bannerAdviewWrapper.setVisibility(View.VISIBLE);
		app.setBannerAdview(bannerAdview);
		app.setBannerAdviewWrapper(bannerAdviewWrapper);
	}

	public static void showBannerAd(LinearLayout adviewWrapper, TextView removeAds, Activity activity, MainApp app)
	{
		if (!isShowAds(app))
		{
			return;
		}

		MobclixAdView bannerAdview = app.getBannerAdview();
		LinearLayout bannerAdviewWrapper = app.getBannerAdviewWrapper();

		if (System.currentTimeMillis() - app.sharedData.getLong("lastActivityPauseTime", 0) > 30000 || app.isForceBannerAdOnFailedLoad())
		{
			if (bannerAdviewWrapper != null) {
				bannerAdviewWrapper.removeView(bannerAdview);
			}
			System.out.println("MOBCLIX: FORCE getAd by 30 seconds pause");
			pauseAdview(bannerAdview, app);
			app.setBannerAdview(null);
			app.setBannerAdviewWrapper(null);
		}

		int adsShowCounter = app.sharedData.getInt("com.chess.adsShowCounter", 0);
		if (adviewWrapper == null || bannerAdview == null)
		{
			initializeBannerAdView(activity, app);
		}
		else
		{
			if (bannerAdviewWrapper != null) {
				bannerAdviewWrapper.removeView(bannerAdview);
			}
			bannerAdviewWrapper = (LinearLayout) activity.findViewById(R.id.adview_wrapper);
			bannerAdviewWrapper.addView(bannerAdview);
			bannerAdviewWrapper.setVisibility(View.VISIBLE);
			app.setBannerAdview(bannerAdview);
			app.setBannerAdviewWrapper(bannerAdviewWrapper);
		}

		if (app.isAdviewPaused())
		{
			resumeAdview(bannerAdview, app);
		}

		if (adsShowCounter == 10) {
			/*if (!app.adviewPaused) {
				pauseAdview(bannerAdview, app);
			}*/
			//adviewWrapper.setVisibility(View.GONE);
			removeAds.setVisibility(View.VISIBLE);
			app.SDeditor.putInt("com.chess.adsShowCounter", 0);
			app.SDeditor.commit();
		}
		else 
		{
			removeAds.setVisibility(View.GONE);
			adviewWrapper.setVisibility(View.VISIBLE);
			app.SDeditor.putInt("com.chess.adsShowCounter", adsShowCounter + 1);
			app.SDeditor.commit();
		}
	}

	public static void resumeAdview(MobclixAdView adview, MainApp app) {
		System.out.println("Mobclix: RESUME");
		if (adview != null)
		{
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

	public static void hideBannerAd(MainApp app, TextView removeAds)
	{
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

	private static void startTimer(final MobclixAdView adview, final MainApp mainApp)
	{
		if (!isShowAds(mainApp))
		{
			return;
		}
		adTimer = new java.util.Timer();
		adTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				if (!mainApp.isAdviewPaused())
				{
					getAd(adview);
				}
			}
		}, 0, 15000);
	}

	private static void stopTimer(MobclixAdView adview)
	{
		if (adview != null)
		{
			adview.cancelAd();
		}
		if (adTimer != null)
		{
			adTimer.cancel();
		}
	}

	public static void getAd(MobclixAdView adview)
	{
		System.out.println("MOBCLIX: getAd");
		adview.getAd();
	}

	public static Timer getAdTimer()
	{
		return adTimer;
	}
}