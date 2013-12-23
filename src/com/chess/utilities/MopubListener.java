package com.chess.utilities;

import android.util.Log;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;

/**
 * Created with IntelliJ IDEA.
 * User: vm
 * Date: 22.08.12
 * Time: 0:34
 * To change this template use File | Settings | File Templates.
 */
public class MopubListener implements MoPubView.BannerAdListener {

	private static final String TAG = "MopubBannerLog";

	@Override
	public void onBannerLoaded(MoPubView banner) {
		Log.d(TAG, "onBannerLoaded: " + banner);
		/*String response = moPubBannerView.getResponseString();
		if (response != null && response.contains(AppConstants.MATOMY_AD)) {
			Map<String, String> params = new HashMap<String, String>();
			params.put(AppConstants.RESPONSE, response);
			FlurryAgent.logEvent(FlurryData.MATOMY_AD_LOADED, params);
		}*/
	}

	@Override
	public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
		Log.d(TAG, "onBannerFailed: " + banner + ", error: " + errorCode);
		/*String response = moPubBannerView.getResponseString();
		if (response != null && response.contains(AppConstants.MATOMY_AD)) {
			Map<String, String> params = new HashMap<String, String>();
			params.put(AppConstants.RESPONSE, response);
			FlurryAgent.logEvent(FlurryData.MATOMY_AD_FAILED, params);
		}*/
	}

	@Override
	public void onBannerClicked(MoPubView banner) {
		Log.d(TAG, "onBannerClicked: " + banner);
	}

	@Override
	public void onBannerExpanded(MoPubView banner) {
		Log.d(TAG, "onBannerExpanded: " + banner);
	}

	@Override
	public void onBannerCollapsed(MoPubView banner) {
		Log.d(TAG, "onBannerCollapsed: " + banner);
	}
}
