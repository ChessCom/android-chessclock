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

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.google.ads.*;
import com.google.ads.AdRequest.ErrorCode;

public class GoogleAdMobInterstitialAdapter extends BaseInterstitialAdapter implements 
        AdListener {

    private InterstitialAd mInterstitialAd;
    private String mParams;
    private boolean mHasAlreadyRegisteredClick;

    public GoogleAdMobInterstitialAdapter(MoPubInterstitial interstitial, String params) {
        super(interstitial);
        
        mParams = params;

        // The following parameters are required. Fail if they aren't set. 
        JSONObject object; 
        String pubId;
        try { 
            object = (JSONObject) new JSONTokener(mParams).nextValue(); 
            pubId = object.getString("adUnitID");
        } catch (JSONException e) { 
            mInterstitial.interstitialFailed(); 
            return; 
        }

        mInterstitialAd = new InterstitialAd(mInterstitial.getActivity(), pubId);
        mInterstitialAd.setAdListener(this);
    }

    @Override
    public void loadInterstitial() {
        AdRequest adRequest = new AdRequest();
        // adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
        // Uncomment the line above to enable test ads on the emulator.
        mInterstitialAd.loadAd(adRequest);
    }

    @Override
    public void invalidate() {
        mInterstitial = null;
    }

    @Override
    public void showInterstitial() {
        mInterstitialAd.show();
    }

    @Override
    public void onDismissScreen(Ad arg0) {
        // TODO: Forward this message.
    }

    @Override
    public void onFailedToReceiveAd(Ad arg0, ErrorCode arg1) {
        Log.d("MoPub", "Google AdMob interstitial failed. Trying another");
        if (mInterstitial != null) { 
            mInterstitial.interstitialFailed(); 
        }
    }

    @Override
    public void onLeaveApplication(Ad arg0) {
        // TODO: This only tracks clicks accurately if all clicks result in leaving the app.
        Log.d("MoPub", "Google AdMob interstitial was clicked, leaving application");
        if (mInterstitial != null && !mHasAlreadyRegisteredClick) { 
            mHasAlreadyRegisteredClick = true;
            mInterstitial.interstitialClicked(); 
        }
    }

    @Override
    public void onPresentScreen(Ad arg0) {
        // Not relevant for interstitials.
    }

    @Override
    public void onReceiveAd(Ad arg0) {
        Log.d("MoPub", "Google AdMob interstitial received an ad successfully.");
        if (mInterstitial != null) {
            mInterstitial.interstitialLoaded();
            mInterstitialAd.show();
        }
    }
}
