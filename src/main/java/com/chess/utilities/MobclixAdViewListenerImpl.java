package com.chess.utilities;

import com.mobclix.android.sdk.MobclixAdView;
import com.mobclix.android.sdk.MobclixAdViewListener;

public class MobclixAdViewListenerImpl implements MobclixAdViewListener {
	public void onSuccessfulLoad(MobclixAdView view) {
		//view.setVisibility(View.VISIBLE);
		System.out.println("MobclixAdViewListener: onSuccessfulLoad");
	}

	public void onFailedLoad(MobclixAdView view, int errorCode) {
		System.out.println("MobclixAdViewListener: onFailedLoad errorCode=" + errorCode);
	}

	public boolean onOpenAllocationLoad(MobclixAdView adView,
			int openAllocationCode) {

		System.out.println("MobclixAdViewListener: onOpenAllocationLoad openAllocationCode="
						+ openAllocationCode);
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
		//System.out.println("MobclixAdViewListener: onAdClick");
		return;
	}

	public void onCustomAdTouchThrough(MobclixAdView adView, String string) {
		//System.out.println("MobclixAdViewListener: onCustomAdTouchThrough");
		return;
	}

	public String keywords() {
		return null;
	}

	public String query() {
		return null;
	}
}
