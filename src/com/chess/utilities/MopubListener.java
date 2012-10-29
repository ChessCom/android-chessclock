package com.chess.utilities;

import android.util.Log;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.FlurryData;
import com.flurry.android.FlurryAgent;
import com.mopub.mobileads.MoPubView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 22.08.12
 * Time: 0:34
 * To change this template use File | Settings | File Templates.
 */
public class MopubListener implements MoPubView.OnAdWillLoadListener, MoPubView.OnAdLoadedListener, MoPubView.OnAdFailedListener,
		MoPubView.OnAdPresentedOverlayListener, MoPubView.OnAdClosedListener, MoPubView.OnAdClickedListener {

	public void OnAdClicked(MoPubView moPubView) {
		Log.d("MOPUB TEST", "OnAdClicked url=" + moPubView.getClickthroughUrl());
	}

	public void OnAdClosed(MoPubView moPubView) {
		Log.d("MOPUB TEST", "OnAdClosed url=" + moPubView.getClickthroughUrl());
	}

	public void OnAdFailed(MoPubView moPubView) {
		String response = moPubView.getResponseString();
		Log.d("MOPUB TEST", "OnAdFailed url=" + moPubView.getClickthroughUrl());
		if (response != null && response.contains(AppConstants.MATOMY_AD)) {
			Map<String, String> params = new HashMap<String, String>();
			params.put(AppConstants.RESPONSE, response);
			FlurryAgent.logEvent(FlurryData.MATOMY_AD_FAILED, params);
		}
	}

	public void OnAdLoaded(MoPubView moPubView) {
		String response = moPubView.getResponseString();
		Log.d("MOPUB TEST", "OnAdLoaded url=" + moPubView.getClickthroughUrl() + ",\n response=" + response);
		if (response != null && response.contains(AppConstants.MATOMY_AD)) {
			Map<String, String> params = new HashMap<String, String>();
			params.put(AppConstants.RESPONSE, response);
			FlurryAgent.logEvent(FlurryData.MATOMY_AD_LOADED, params);
		}
	}

	public void OnAdPresentedOverlay(MoPubView moPubView) {
		Log.d("MOPUB TEST", "OnAdPresentedOverlay url=" + moPubView.getClickthroughUrl());
	}

	public void OnAdWillLoad(MoPubView moPubView, String url) {
		Log.d("MOPUB TEST", "OnAdWillLoad url=" + url);
	}
}
