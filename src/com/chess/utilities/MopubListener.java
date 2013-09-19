package com.chess.utilities;

import com.chess.statics.AppConstants;
import com.chess.statics.FlurryData;
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

	@Override
	public void OnAdClicked(MoPubView moPubView) {
	}

	@Override
	public void OnAdClosed(MoPubView moPubView) {
	}

	@Override
	public void OnAdFailed(MoPubView moPubView) {
		String response = moPubView.getResponseString();
		if (response != null && response.contains(AppConstants.MATOMY_AD)) {
			Map<String, String> params = new HashMap<String, String>();
			params.put(AppConstants.RESPONSE, response);
			FlurryAgent.logEvent(FlurryData.MATOMY_AD_FAILED, params);
		}
	}

	@Override
	public void OnAdLoaded(MoPubView moPubView) {
		String response = moPubView.getResponseString();
		if (response != null && response.contains(AppConstants.MATOMY_AD)) {
			Map<String, String> params = new HashMap<String, String>();
			params.put(AppConstants.RESPONSE, response);
			FlurryAgent.logEvent(FlurryData.MATOMY_AD_LOADED, params);
		}
	}

	@Override
	public void OnAdPresentedOverlay(MoPubView moPubView) {
	}

	@Override
	public void OnAdWillLoad(MoPubView moPubView, String url) {
	}
}
