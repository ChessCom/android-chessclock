package com.chess.utilities;

import com.chess.ui.core.MainApp;
import com.mobclix.android.sdk.MobclixAdView;
import com.mobclix.android.sdk.MobclixAdViewListener;

public class MobclixAdViewListenerImpl implements MobclixAdViewListener {

	private boolean isRectangle;
	private MainApp mainApp;

	public MobclixAdViewListenerImpl(boolean isRectangle, MainApp mainApp) {
		this.isRectangle = isRectangle;
		this.mainApp = mainApp;
	}

	public void onSuccessfulLoad(MobclixAdView view) {
		//view.setVisibility(View.VISIBLE);
		System.out.println("MobclixAdViewListener: onSuccessfulLoad" + (isRectangle ? " Rectangle ad" : ""));
		if (!isRectangle && mainApp.isForceBannerAdFirstLoad()) {
			mainApp.setForceBannerAdFirstLoad(false);
			MobclixHelper.pauseAdview(view, mainApp);
			MobclixHelper.resumeAdview(view, mainApp);
		}
	}

	public void onFailedLoad(MobclixAdView view, int errorCode) {
		System.out.println("MobclixAdViewListener: onFailedLoad errorCode=" + errorCode + (isRectangle ? " Rectangle ad" : ""));

		if (!mainApp.isAdviewPaused() && errorCode == MobclixAdViewListener.APP_NOT_IN_FOREGROUND) {
			mainApp.setForceBannerAdOnFailedLoad(true);
			/*new Handler().postDelayed(new Runnable()
				{
					public void run()
					{
						MobclixHelper.showBannerAd();
					}
				}, 3000);*/
		}
	}

	public boolean onOpenAllocationLoad(MobclixAdView adView,
										int openAllocationCode) {

		System.out.println("MobclixAdViewListener: onOpenAllocationLoad openAllocationCode="
				+ openAllocationCode + (isRectangle ? " Rectangle ad" : ""));
		/*
		if (openAllocationCode == MobclixAdViewListener.SUBALLOCATION_ADMOB
				|| openAllocationCode == MobclixAdViewListener.SUBALLOCATION_GOOGLE
				|| openAllocationCode == MobclixAdViewListener.SUBALLOCATION_MILLENNIAL) {
			return false;
		}
		if (openAllocationCode == MobclixAdViewListener.SUBALLOCATION_OTHER) {
			//adView.setVisibility(View.GONE);

			return true;
		}*/
		return false;
	}

	public void onAdClick(MobclixAdView adView) {
		System.out.println("MobclixAdViewListener: onAdClick");
		if (isRectangle) {
			mainApp.setForceRectangleAd(true);
		}
	}

	public void onCustomAdTouchThrough(MobclixAdView adView, String string) {
		//System.out.println("MobclixAdViewListener: onCustomAdTouchThrough");
	}

	public String keywords() {
		return null;
	}

	public String query() {
		return null;
	}
}
