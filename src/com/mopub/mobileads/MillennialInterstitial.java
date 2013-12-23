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

import android.content.Context;
import android.location.Location;
import android.util.Log;
import com.millennialmedia.android.*;

import java.util.Map;

import static com.mopub.mobileads.MoPubErrorCode.NETWORK_INVALID_STATE;
import static com.mopub.mobileads.MoPubErrorCode.NETWORK_NO_FILL;

/**
 * Compatible with version 5.1.0 of the Millennial Media SDK.
 */

class MillennialInterstitial extends CustomEventInterstitial {
    private MMInterstitial mMillennialInterstitial;
    private CustomEventInterstitialListener mInterstitialListener;
    public static final String APID_KEY = "adUnitID";
    private MillennialBroadcastReceiver mBroadcastReceiver;

    @Override
    protected void loadInterstitial(Context context, CustomEventInterstitialListener customEventInterstitialListener,
                                    Map<String, Object> localExtras, Map<String, String> serverExtras) {
        mInterstitialListener = customEventInterstitialListener;

        String apid;
        if (extrasAreValid(serverExtras)) {
            apid = serverExtras.get(APID_KEY);
        } else {
            mInterstitialListener.onInterstitialFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        MMSDK.initialize(context);
        MMSDK.setBroadcastEvents(true);

        mBroadcastReceiver = new MillennialBroadcastReceiver();
        mBroadcastReceiver.register(context);

        Location location = (Location) localExtras.get("location");
        if (location != null) MMRequest.setUserLocation(location);

        mMillennialInterstitial = new MMInterstitial(context);
        if (mMillennialInterstitial.isAdAvailable()) {
            Log.d("MoPub", "Millennial interstitial ad already loaded.");
            mInterstitialListener.onInterstitialLoaded();
        } else {
            mMillennialInterstitial.setMMRequest(new MMRequest());
            mMillennialInterstitial.setApid(apid);
            mMillennialInterstitial.fetch();
        }
    }

    @Override
    protected void showInterstitial() {
        if (mMillennialInterstitial.isAdAvailable()) {
            mMillennialInterstitial.display();
        } else {
            Log.d("MoPub", "Tried to show a Millennial interstitial ad before it finished loading. Please try again.");
        }
    }

    @Override
    protected void onInvalidate() {
        mMillennialInterstitial.setListener(null);
        mBroadcastReceiver.unregister();
    }

    private boolean extrasAreValid(Map<String, String> serverExtras) {
        return serverExtras.containsKey(APID_KEY);
    }

    class MillennialBroadcastReceiver extends MMBroadcastReceiver {
        private Context mContext;

        @Override
        public void fetchFinishedCaching(MMAd ad) {
            super.fetchFinishedCaching(ad);
            fetchFinished(NETWORK_INVALID_STATE);
        }

        @Override
        public void getAdFailure(MMAd ad) {
            super.getAdFailure(ad);
            Log.d("MoPub", "Millennial interstitial ad failed to load.");
            mInterstitialListener.onInterstitialFailed(NETWORK_NO_FILL);
        }

        @Override
        public void intentStarted(MMAd ad, String intent) {
            super.intentStarted(ad, intent);
            Log.d("MoPub", "Millennial interstitial ad clicked.");
            mInterstitialListener.onInterstitialClicked();
        }

        @Override
        public void fetchFailure(MMAd ad) {
            super.fetchFailure(ad);
            fetchFinished(NETWORK_NO_FILL);
        }

        @Override
        public void displayStarted(MMAd ad) {
            super.displayStarted(ad);
            Log.d("MoPub", "Showing Millennial interstitial ad.");
            mInterstitialListener.onInterstitialShown();
        }

        @Override
        public void overlayClosed(MMAd ad) {
            super.overlayClosed(ad);
            Log.d("MoPub", "Millennial interstitial ad dismissed.");
            mInterstitialListener.onInterstitialDismissed();
        }

        void register(Context context) {
            mContext = context;
            context.registerReceiver(this, MMBroadcastReceiver.createIntentFilter());
        }

        void unregister() {
            try {
                mContext.unregisterReceiver(this);
            } catch (Exception exception) {
                Log.d("MoPub", "Unable to unregister MMBroadcastReceiver", exception);
            } finally {
                mContext = null;
            }
        }

        private void fetchFinished(MoPubErrorCode errorToReport) {
            if (mMillennialInterstitial.isAdAvailable()) {
                Log.d("MoPub", "Millennial interstitial ad loaded successfully.");
                mInterstitialListener.onInterstitialLoaded();
            } else {
                Log.d("MoPub", "Millennial interstitial ad failed to load.");
                mInterstitialListener.onInterstitialFailed(errorToReport);
            }
        }
    }
}
