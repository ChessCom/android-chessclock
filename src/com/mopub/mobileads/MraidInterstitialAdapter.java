package com.mopub.mobileads;


import android.app.Activity;
import android.content.Intent;

public class MraidInterstitialAdapter extends BaseInterstitialAdapter {
    @Override
    public void loadInterstitial() {
        if (mAdapterListener != null) mAdapterListener.onNativeInterstitialLoaded(this);
    }

    @Override
    public void showInterstitial() {
        Activity activity = mInterstitial.getActivity();
        Intent i = new Intent(activity, MraidActivity.class);
        i.putExtra("com.mopub.mobileads.Source", mJsonParams);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(i);
    }
}
