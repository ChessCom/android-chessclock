/*
 * Copyright (c) 2010, MoPub Inc.
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
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.mopub.mobileads.util.Views;

import java.util.*;

import static com.google.ads.AdSize.BANNER;
import static com.google.ads.AdSize.IAB_BANNER;
import static com.google.ads.AdSize.IAB_LEADERBOARD;
import static com.google.ads.AdSize.IAB_MRECT;
import static com.mopub.mobileads.MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_NO_FILL;

/*
 * Compatible with version 6.4.1 of the Google AdMob Ads SDK.
 */

class GoogleAdMobBanner extends CustomEventBanner implements AdListener {
    /*
     * These keys are intended for MoPub internal use. Do not modify.
     */
    public static final String AD_UNIT_ID_KEY = "adUnitID";
    public static final String AD_WIDTH_KEY = "adWidth";
    public static final String AD_HEIGHT_KEY = "adHeight";
    public static final String LOCATION_KEY = "location";

    private AdView mAdMobView;
    private CustomEventBannerListener mBannerListener;

    @Override
    protected void loadBanner(Context context,
                              CustomEventBannerListener customEventBannerListener,
                              Map<String, Object> localExtras,
                              Map<String, String> serverExtras) {
        mBannerListener = customEventBannerListener;

        String adUnitId;
        int adWidth;
        int adHeight;

        if (!(context instanceof Activity)) {
            mBannerListener.onBannerFailed(ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        if (extrasAreValid(serverExtras)) {
            adUnitId = serverExtras.get(AD_UNIT_ID_KEY);
            adWidth = Integer.parseInt(serverExtras.get(AD_WIDTH_KEY));
            adHeight = Integer.parseInt(serverExtras.get(AD_HEIGHT_KEY));
        } else {
            mBannerListener.onBannerFailed(ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        AdSize adSize = calculateAdSize(adWidth, adHeight);
        if (adSize == null) {
            Log.d("MoPub", "Unsupported AdMob ad size: " + adWidth + "x" + adHeight);
            mBannerListener.onBannerFailed(ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        mAdMobView = new AdView((Activity) context, adSize, adUnitId);
        mAdMobView.setAdListener(this);

        AdRequest request = new AdRequest();
        Location location = extractLocation(localExtras);
        if (location != null) request.setLocation(location);

        mAdMobView.loadAd(request);
    }

    @Override
    protected void onInvalidate() {
        mAdMobView.setAdListener(null);
        Views.removeFromParent(mAdMobView);
        mAdMobView.destroy();
    }

    private Location extractLocation(Map<String, Object> localExtras) {
        Object location = localExtras.get(LOCATION_KEY);
        if (location instanceof Location) {
            return (Location) location;
        }
        return null;
    }

    private AdSize calculateAdSize(int width, int height) {
        // Use the smallest AdMob AdSize that will properly contain the adView
        if (width <= BANNER.getWidth() && height <= BANNER.getHeight()) {
            return BANNER;
        } else if (width <= IAB_MRECT.getWidth() && height <= IAB_MRECT.getHeight()) {
            return IAB_MRECT;
        } else if (width <= IAB_BANNER.getWidth() && height <= IAB_BANNER.getHeight()) {
            return IAB_BANNER;
        } else if (width <= IAB_LEADERBOARD.getWidth() && height <= IAB_LEADERBOARD.getHeight()) {
            return IAB_LEADERBOARD;
        } else {
            return null;
        }
    }

    private boolean extrasAreValid(Map<String, String> serverExtras) {
        try {
            Integer.parseInt(serverExtras.get(AD_WIDTH_KEY));
            Integer.parseInt(serverExtras.get(AD_HEIGHT_KEY));
        } catch (NumberFormatException e) {
            return false;
        }

        return serverExtras.containsKey(AD_UNIT_ID_KEY);
    }

    @Deprecated // for testing
    AdView getAdMobView() {
        return mAdMobView;
    }

    /**
     * AdMob AdListener implementation
     */
    @Override
    public void onFailedToReceiveAd(Ad ad, ErrorCode error) {
        Log.d("MoPub", "Google AdMob banner ad failed to load.");
        mBannerListener.onBannerFailed(NETWORK_NO_FILL);
    }

    @Override
    public void onPresentScreen(Ad ad) {
        Log.d("MoPub", "Google AdMob banner ad clicked.");
        mBannerListener.onBannerClicked();
    }

    @Override
    public void onReceiveAd(Ad ad) {
        Log.d("MoPub", "Google AdMob banner ad loaded successfully. Showing ad...");
        mBannerListener.onBannerLoaded(mAdMobView);
    }

    @Override
    public void onLeaveApplication(Ad ad) {
    }

    @Override
    public void onDismissScreen(Ad ad) {
    }
}
