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
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.millennialmedia.android.MMAdView;
import com.millennialmedia.android.MMAdView.MMAdListener;
import com.millennialmedia.android.MMAdViewSDK;
import com.mopub.mobileads.MoPubView;

import java.lang.ref.WeakReference;

public class MillennialAdapter extends BaseAdapter implements MMAdListener {

    private MMAdView mMillennialAdView;
    
    // TODO: Temporary fix. MMAdView often calls MMAdReturned multiple times for a single 
    // successful ad load, so we need a way to dedupe these calls.
    private boolean mHasAlreadyRegisteredImpression;

    // MMAdListener should use a WeakReference to the activity.
    // From: http://wiki.millennialmedia.com/index.php/Android#Listening_for_Ad_Events
    private WeakReference<Activity>	mActivityReference;
    
    // To avoid races between MMAdListener's asynchronous callbacks and our adapter code 
    // (e.g. invalidate()), we'll "convert" asynchronous calls to synchronous ones via a Handler.
    private final Handler mHandler = new Handler();
    
    @Override
    public void init(MoPubView view, String jsonParams) {
        super.init(view, jsonParams);
        mActivityReference = new WeakReference<Activity>((Activity)view.getContext());
    }

    @Override
    public void loadAd() {
        if (isInvalidated()) return;
        
        // The following parameters are required. Fail if they aren't set.
        JSONObject object; 
        String pubId;
        int adWidth, adHeight;
        try { 
            object = (JSONObject) new JSONTokener(mJsonParams).nextValue(); 
            pubId = object.getString("adUnitID");
            adWidth = object.getInt("adWidth");
            adHeight = object.getInt("adHeight");
        } catch (JSONException e) { 
            mMoPubView.adFailed(); 
            return; 
        }
        
        String mmAdType = MMAdView.BANNER_AD_TOP;
        String widthString = "320";
        String heightString = "53";
        
        if (adWidth == 300 && adHeight == 250) {
            mmAdType = MMAdView.BANNER_AD_RECTANGLE;
            widthString = Integer.toString(adWidth);
            heightString = Integer.toString(adHeight);
        }
        
        Activity activity = mActivityReference.get();
        mMillennialAdView = new MMAdView(activity, pubId, mmAdType, MMAdView.REFRESH_INTERVAL_OFF);
        mMillennialAdView.setId(MMAdViewSDK.DEFAULT_VIEWID);
        mMillennialAdView.setListener(this);
        mMillennialAdView.setVisibility(View.INVISIBLE);
        
        mMillennialAdView.setWidth(widthString);
        mMillennialAdView.setHeight(heightString);
        
        Location location = mMoPubView.getLocation();
        if (location != null) mMillennialAdView.updateUserLocation(location);

        mHasAlreadyRegisteredImpression = false;
        
        Log.d("MoPub", "Loading Millennial ad...");
        mMillennialAdView.callForAd();
    }

    @Override
    public void invalidate() {
    	mMillennialAdView.removeAllViews();
        mMoPubView.removeView(mMillennialAdView);
        mActivityReference = null;
        super.invalidate();
    }
    
    @Override
    public boolean isInvalidated() {
        if (mActivityReference == null) return true;
        else if (mActivityReference.get() == null) return true;
        else return super.isInvalidated();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    
    @Override
    public void MMAdFailed(MMAdView adview)	{
        mHandler.post(new Runnable() {
            public void run() {
                if (isInvalidated()) return;
                
                Log.d("MoPub", "Millennial failed. Trying another");
                mMoPubView.loadFailUrl();
            }
        });   
    }

    @Override
    public void MMAdReturned(MMAdView adview) {
        mHandler.post(new Runnable() {
            public void run() {
                if (isInvalidated()) return;
                
                mMoPubView.removeAllViews();
                mMillennialAdView.setVisibility(View.VISIBLE);
                mMillennialAdView.setHorizontalScrollBarEnabled(false);
                mMillennialAdView.setVerticalScrollBarEnabled(false);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.FILL_PARENT, 
                        FrameLayout.LayoutParams.FILL_PARENT);
                layoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL;
                mMoPubView.addView(mMillennialAdView, layoutParams);
                
                if (!mHasAlreadyRegisteredImpression) {
                    mHasAlreadyRegisteredImpression = true;
                    mMoPubView.nativeAdLoaded();
                    mMoPubView.trackNativeImpression();
                }
            }
        });
    }

    @Override
    public void MMAdClickedToNewBrowser(MMAdView adview) {
        mHandler.post(new Runnable() {
            public void run() {
                if (isInvalidated()) return;
                
                Log.d("MoPub", "Millennial clicked");
                mMoPubView.registerClick();
            }
        }); 
    }

    @Override
    public void MMAdClickedToOverlay(MMAdView adview) {
        mHandler.post(new Runnable() {
            public void run() {
                if (isInvalidated()) return;
                
                Log.d("MoPub", "Millennial clicked");
                mMoPubView.registerClick();
            }
        });
    }

    @Override
    public void MMAdOverlayLaunched(MMAdView adview) {
        // Nothing needs to happen.
    }

    @Override
    public void MMAdRequestIsCaching(MMAdView adview) {
        // Nothing needs to happen -- caching is only relevant for interstitial ads.
    }
    
    @Override
    public void MMAdCachingCompleted(MMAdView adview, boolean success) {
        // Nothing needs to happen -- caching is only relevant for interstitial ads.
    }
}