/*
 * Copyright (c) 2011, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'MoPub Inc.' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.mopub.mobileads;

import android.app.Activity;
import android.location.Location;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMAdView.MMAdListener;
import com.millennialmedia.android.MMAdViewSDK;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.lang.ref.WeakReference;

public class MillennialInterstitialAdapter extends BaseInterstitialAdapter implements MMAdListener {

	private MMAdView mMillennialAdView;

	private boolean mHasAlreadyRegisteredClick;

	// MMAdListener should use a WeakReference to the activity.
	// From: http://wiki.millennialmedia.com/index.php/Android#Listening_for_Ad_Events
	private WeakReference<Activity> mActivityReference;

	// To avoid races between MMAdListener's asynchronous callbacks and our adapter code
	// (e.g. invalidate()), we'll "convert" asynchronous calls to synchronous ones via a Handler.
	private final Handler mHandler = new Handler();

	@Override
	public void init(MoPubInterstitial interstitial, String jsonParams) {
		super.init(interstitial, jsonParams);
		mActivityReference = new WeakReference<Activity>(interstitial.getActivity());

		// The following parameters are required. Fail if they aren't set.
		JSONObject object;
		String pubId;
		try {
			object = (JSONObject) new JSONTokener(mJsonParams).nextValue();
			pubId = object.getString("adUnitID");
		} catch (JSONException e) {
			if (mAdapterListener != null) mAdapterListener.onNativeInterstitialFailed(this);
			return;
		}

		mMillennialAdView = new MMAdView(mActivityReference.get(), pubId,
				MMAdView.FULLSCREEN_AD_TRANSITION, MMAdView.REFRESH_INTERVAL_OFF);
		mMillennialAdView.setId(MMAdViewSDK.DEFAULT_VIEWID);
		mMillennialAdView.setListener(this);
	}

	@Override
	public void invalidate() {
		mMillennialAdView.removeAllViews();
		mActivityReference = null;
		super.invalidate();
	}

	@Override
	public boolean isInvalidated() {
		if (mActivityReference == null) return true;
		else if (mActivityReference.get() == null) return true;
		else return super.isInvalidated();
	}

	@Override
	public void loadInterstitial() {
		if (isInvalidated()) return;

		Log.d("MoPub", "Fetching Millennial ad...");

		Location location = mInterstitial.getLocation();
		if (location != null) mMillennialAdView.updateUserLocation(location);

		mMillennialAdView.setVisibility(View.INVISIBLE);
		mHasAlreadyRegisteredClick = false;
		mMillennialAdView.fetch();
	}

	@Override
	public void showInterstitial() {
		if (isInvalidated()) return;

		if (mMillennialAdView.check()) mMillennialAdView.display();
		else Log.d("MoPub", "Tried to show a Millennial interstitial before it finished caching. " +
				"Please try again.");
	}

	private void recordClickIfNecessary() {
		if (!mHasAlreadyRegisteredClick) {
			mHasAlreadyRegisteredClick = true;
			if (mAdapterListener != null) mAdapterListener.onNativeInterstitialClicked(this);
		}
	}

	@Override
	public void MMAdFailed(MMAdView adview) {
		mHandler.post(new Runnable() {
			public void run() {
				if (isInvalidated()) return;

				Log.d("MoPub", "Millennial interstitial failed. Trying another.");
				if (mAdapterListener != null) {
					mAdapterListener.onNativeInterstitialFailed(MillennialInterstitialAdapter.this);
				}
			}
		});
	}

	@Override
	public void MMAdReturned(MMAdView adview) {
		mHandler.post(new Runnable() {
			public void run() {
				if (isInvalidated()) return;

				Log.d("MoPub", "Millennial interstitial returned an ad.");
				if (mAdapterListener != null) {
					//mAdapterListener.onNativeInterstitialLoaded(MillennialInterstitialAdapter.this);
				}
			}
		});
	}

	@Override
	public void MMAdClickedToNewBrowser(MMAdView adview) {
		mHandler.post(new Runnable() {
			public void run() {
				if (isInvalidated()) return;

				Log.d("MoPub", "Millennial interstitial clicked to new browser");
				recordClickIfNecessary();
			}
		});
	}

	@Override
	public void MMAdClickedToOverlay(MMAdView adview) {
		mHandler.post(new Runnable() {
			public void run() {
				if (isInvalidated()) return;

				Log.d("MoPub", "Millennial interstitial clicked to overlay");
				recordClickIfNecessary();
			}
		});
	}

	@Override
	public void MMAdOverlayLaunched(MMAdView adview) {
		mHandler.post(new Runnable() {
			public void run() {
				if (isInvalidated()) return;

				Log.d("MoPub", "Millennial interstitial launched overlay");
				if (mAdapterListener != null) {
					mAdapterListener.onNativeInterstitialExpired(MillennialInterstitialAdapter.this);
				}
			}
		});
	}

	@Override
	public void MMAdCachingCompleted(MMAdView adview, boolean success) {
		mHandler.post(new Runnable() {
			public void run() {
				if (isInvalidated()) return;

				if (mAdapterListener != null) {
					if (mMillennialAdView.check()) {
						Log.d("MoPub", "Millennial interstitial caching completed.");
						mAdapterListener.onNativeInterstitialLoaded(MillennialInterstitialAdapter.this);
					} else {
						Log.d("MoPub", "Millennial interstitial caching failed. Trying another.");
						mAdapterListener.onNativeInterstitialFailed(MillennialInterstitialAdapter.this);
					}
				}
			}
		});
	}

	@Override
	public void MMAdRequestIsCaching(MMAdView adview) {
		// Nothing needs to happen.
		Log.d("MoPub", "Millennial interstitial is caching.");
	}
}
