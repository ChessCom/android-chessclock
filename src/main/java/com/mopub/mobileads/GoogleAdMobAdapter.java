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

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.util.Log;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdSize;

public class GoogleAdMobAdapter extends BaseAdapter implements AdListener {
    
    private com.google.ads.AdView mAdView;
    private MoPubView mMoPubView;
    private String mParams;

    public GoogleAdMobAdapter(MoPubView view, String params) {
        mMoPubView = view;
        mParams = params;
    }

    @Override
    public void loadAd() {
        if (mMoPubView == null) {
            return;
        }

        // Get the parameters to pass to AdMob
        JSONObject object; 
        String adUnitId; 
        try { 
            object = (JSONObject) new JSONTokener(mParams).nextValue(); 
            adUnitId = object.getString("adUnitID"); 
        } catch (JSONException e) { 
            mMoPubView.adFailed(); 
            return; 
        }

        mAdView = new com.google.ads.AdView(mMoPubView.getActivity(), AdSize.BANNER, adUnitId);

        // Listen for callbacks
        mAdView.setAdListener(this);

        // Start loading the ad in the background.
        AdRequest request = new AdRequest();
        // request.addTestDevice(AdRequest.TEST_EMULATOR);
        // Uncomment the line above to enable test ads on the emulator.
        mAdView.loadAd(request);
    }

    @Override
    public void invalidate() {
        mMoPubView = null;
    }

    @Override
    public void onDismissScreen(Ad ad) {
    }

    @Override
    public void onFailedToReceiveAd(Ad ad, ErrorCode error) {
        if (mMoPubView != null) { 
            Log.d("MoPub", "Google AdMob failed. Trying another"); 
            mMoPubView.loadFailUrl(); 
        } 
    }

    @Override
    public void onLeaveApplication(Ad ad) {
    }

    @Override
    public void onPresentScreen(Ad ad) {
        Log.d("MoPub", "Google AdMob clicked"); 
        if (mMoPubView != null) { 
            mMoPubView.registerClick(); 
        } 
    }

    @Override
    public void onReceiveAd(Ad ad) {
        if (mMoPubView != null) {
            Log.d("MoPub", "Google AdMob load succeeded. Showing ad..."); 
            mMoPubView.removeAllViews();
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.FILL_PARENT, 
                    FrameLayout.LayoutParams.FILL_PARENT);
            layoutParams.gravity = Gravity.CENTER;
            mMoPubView.addView(mAdView, layoutParams);

            mMoPubView.nativeAdLoaded();
            mMoPubView.trackNativeImpression();
        } 
    }
}
