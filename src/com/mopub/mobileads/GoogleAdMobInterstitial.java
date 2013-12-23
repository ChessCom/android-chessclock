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
import android.content.Context;
import android.location.Location;
import android.util.Log;
import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.InterstitialAd;

import java.util.*;

import static com.mopub.mobileads.MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_NO_FILL;

/*
 * Compatible with version 6.4.1 of the Google AdMob Ads SDK.
 */

class GoogleAdMobInterstitial extends CustomEventInterstitial implements AdListener {
    /*
     * These keys are intended for MoPub internal use. Do not modify.
     */
    public static final String AD_UNIT_ID_KEY = "adUnitID";
    public static final String LOCATION_KEY = "location";

    private InterstitialAd mInterstitialAd;
    private boolean mHasAlreadyRegisteredClick;
    private CustomEventInterstitialListener mInterstitialListener;

    @Override
    protected void loadInterstitial(Context context,
                                    CustomEventInterstitialListener customEventInterstitialListener,
                                    Map<String, Object> localExtras,
                                    Map<String, String> serverExtras) {
        mInterstitialListener = customEventInterstitialListener;

        if (!(context instanceof Activity)) {
            mInterstitialListener.onInterstitialFailed(ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        String pubId;
        if (extrasAreValid(serverExtras)) {
            pubId = serverExtras.get(AD_UNIT_ID_KEY);
        } else {
            mInterstitialListener.onInterstitialFailed(ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        mInterstitialAd = new InterstitialAd((Activity) context, pubId);
        mInterstitialAd.setAdListener(this);

        AdRequest adRequest = new AdRequest();
        Location location = extractLocation(localExtras);
        if (location != null) adRequest.setLocation(location);
        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    protected void showInterstitial() {
        if (mInterstitialAd.isReady()) {
            mInterstitialAd.show();
        } else {
            Log.d("MoPub", "Tried to show a Google AdMob interstitial ad before it finished loading. Please try again.");
        }
    }

    @Override
    protected void onInvalidate() {
        if (mInterstitialAd != null) {
            mInterstitialAd.setAdListener(null);
        }
    }

    private Location extractLocation(Map<String, Object> localExtras) {
        Object location = localExtras.get(LOCATION_KEY);
        if (location instanceof Location) {
            return (Location) location;
        }
        return null;
    }

    private boolean extrasAreValid(Map<String, String> serverExtras) {
        return serverExtras.containsKey(AD_UNIT_ID_KEY);
    }

    @Deprecated // for testing
    InterstitialAd getAdMobInterstitial() {
        return mInterstitialAd;
    }

    /*
     * AdMob AdListener implementation
     */

    @Override
    public void onDismissScreen(Ad ad) {
        Log.d("MoPub", "Google AdMob interstitial ad dismissed.");
        mInterstitialListener.onInterstitialDismissed();
    }

    @Override
    public void onFailedToReceiveAd(Ad ad, ErrorCode error) {
        Log.d("MoPub", "Google AdMob interstitial ad failed to load.");
        mInterstitialListener.onInterstitialFailed(NETWORK_NO_FILL);
    }

    @Override
    public void onLeaveApplication(Ad ad) {
        if (!mHasAlreadyRegisteredClick) {
            Log.d("MoPub", "Google AdMob interstitial ad clicked.");
            mHasAlreadyRegisteredClick = true;
            mInterstitialListener.onInterstitialClicked();
        }
    }

    @Override
    public void onPresentScreen(Ad ad) {
        Log.d("MoPub", "Showing Google AdMob interstitial ad.");
        mInterstitialListener.onInterstitialShown();
    }

    @Override
    public void onReceiveAd(Ad ad) {
        Log.d("MoPub", "Google AdMob interstitial ad loaded successfully.");
        mInterstitialListener.onInterstitialLoaded();
    }
}
