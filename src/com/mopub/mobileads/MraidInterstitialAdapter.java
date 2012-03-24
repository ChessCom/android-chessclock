package com.mopub.mobileads;


import android.app.Activity;
import android.content.Intent;

public class MraidInterstitialAdapter extends BaseInterstitialAdapter {
	public static final String COM_MOPUB_MOBILEADS_SOURCE = "com.mopub.mobileads.Source";

	@Override
	public void loadInterstitial() {
		if (mAdapterListener != null) mAdapterListener.onNativeInterstitialLoaded(this);
	}

	@Override
	public void showInterstitial() {
		Activity activity = mInterstitial.getActivity();
		Intent i = new Intent(activity, MraidActivity.class);
		i.putExtra(COM_MOPUB_MOBILEADS_SOURCE, mJsonParams);
		activity.startActivity(i);
	}
}
