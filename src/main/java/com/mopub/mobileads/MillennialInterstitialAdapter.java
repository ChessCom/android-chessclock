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

import java.lang.ref.WeakReference;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMAdView.MMAdListener;
import com.millennialmedia.android.MMAdViewSDK;

import android.app.Activity;
import android.util.Log;
import android.view.View;

public class MillennialInterstitialAdapter extends BaseInterstitialAdapter implements MMAdListener {

    private MMAdView mAdView;
    private String mParams;
    private boolean mHasAlreadyRegisteredClick;

    // MMAdListener should use a WeakReference to the activity.
    // From: http://wiki.millennialmedia.com/index.php/Android#Listening_for_Ad_Events
    private WeakReference<Activity> mActivityReference;

    public MillennialInterstitialAdapter(MoPubInterstitial interstitial, String params) {
        super(interstitial);
        mActivityReference = new WeakReference<Activity>(interstitial.getActivity());
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

        mAdView = new MMAdView(mActivityReference.get(), pubId, MMAdView.FULLSCREEN_AD_TRANSITION, 
                MMAdView.REFRESH_INTERVAL_OFF);
        mAdView.setId(MMAdViewSDK.DEFAULT_VIEWID);
        mAdView.setListener(this);
    }

    @Override
    public void loadInterstitial() {
        if (mInterstitial == null) {
            return;
        }

        Log.d("MoPub", "Showing Millennial ad...");

        mAdView.setVisibility(View.INVISIBLE);
        mHasAlreadyRegisteredClick = false;
        mAdView.callForAd();
    }

    @Override
    public void invalidate() {
        mInterstitial = null;
    }

    @Override
    public void showInterstitial() {
        // Not supported.
    }

    @Override
    public void MMAdFailed(MMAdView adview)	{
        Log.d("MoPub", "Millennial interstitial failed. Trying another");
        if (mInterstitial != null) { 
            mInterstitial.interstitialFailed(); 
        }
    }

    @Override
    public void MMAdReturned(MMAdView adview) {
        Log.d("MoPub", "Millennial interstitial returned an ad.");
        if (mInterstitial != null) { 
            Activity activity = mActivityReference.get();
            if (activity != null) {
                activity.runOnUiThread(new MMRunnable());
            }
        }
    }

    @Override
    public void MMAdClickedToNewBrowser(MMAdView adview) {
        Log.d("MoPub", "Millennial interstitial clicked to new browser");
        if (mInterstitial != null && !mHasAlreadyRegisteredClick) {
            mHasAlreadyRegisteredClick = true;
            mInterstitial.interstitialClicked(); 
        } 
    }

    @Override
    public void MMAdClickedToOverlay(MMAdView adview) {
        Log.d("MoPub", "Millennial interstitial clicked to overlay");
        if (mInterstitial != null && !mHasAlreadyRegisteredClick) { 
            mHasAlreadyRegisteredClick = true;
            mInterstitial.interstitialClicked(); 
        } 
    }

    @Override
    public void MMAdOverlayLaunched(MMAdView adview) {
        Log.d("MoPub", "Millennial interstitial launched overlay");
        if (mInterstitial != null && !mHasAlreadyRegisteredClick) { 
            mHasAlreadyRegisteredClick = true;
            mInterstitial.interstitialClicked(); 
        } 
    }

    @Override
    public void MMAdRequestIsCaching(MMAdView adview) {
        // Nothing needs to happen.
    }
    
    protected class MMRunnable implements Runnable {
        public void run() {
            if (mInterstitial != null) {
                mInterstitial.interstitialLoaded();
            }
        }
    }
}
