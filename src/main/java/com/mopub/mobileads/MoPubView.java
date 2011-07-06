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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebViewDatabase;
import android.widget.FrameLayout;

import org.apache.http.HttpResponse;

import java.util.HashMap;

public class MoPubView extends FrameLayout {

    public interface OnAdWillLoadListener {
        public void OnAdWillLoad(MoPubView m, String url);
    }

    public interface OnAdLoadedListener {
        public void OnAdLoaded(MoPubView m);
    }

    public interface OnAdFailedListener {
        public void OnAdFailed(MoPubView m);
    }

    public interface OnAdClosedListener {
        public void OnAdClosed(MoPubView m);
    }

    public interface OnAdClickedListener {
        public void OnAdClicked(MoPubView m);
    }

    public static String HOST = "ads.mopub.com";
    public static String AD_HANDLER = "/m/ad";

    protected AdView mAdView;
    private Activity mActivity;
    protected BaseAdapter mAdapter;
    private Context mContext;
    private BroadcastReceiver mScreenStateReceiver;
    private boolean mIsInForeground;

    private OnAdWillLoadListener mOnAdWillLoadListener;
    private OnAdLoadedListener mOnAdLoadedListener;
    private OnAdFailedListener mOnAdFailedListener;
    private OnAdClosedListener mOnAdClosedListener;
    private OnAdClickedListener mOnAdClickedListener;

    public MoPubView(Context context) {
        this(context, null);
    }

    public MoPubView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mIsInForeground = (getVisibility() == VISIBLE);
        
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        
        // There is a rare bug in Froyo/2.2 where creation of a WebView causes a
        // NullPointerException. (http://code.google.com/p/android/issues/detail?id=10789)
        // It happens when the WebView can't access the local file store to make a cache file.
        // Here, we'll work around it by trying to create a file store and then just go inert
        // if it's not accessible.
        if (WebViewDatabase.getInstance(context) == null) {
            Log.e("MoPub", "Disabling MoPub. Local cache file is inaccessible so MoPub will " +
                    "fail if we try to create a WebView. Details of this Android bug found at:" +
                    "http://code.google.com/p/android/issues/detail?id=10789");
            return;
        }

        // The AdView doesn't need to be in the view hierarchy until an ad is loaded
        mAdView = new AdView(context, this);
        
        registerScreenStateBroadcastReceiver();

        mActivity = (Activity) context;
    }

    private void registerScreenStateBroadcastReceiver() {
        if (mAdView == null) return;
        
        mScreenStateReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    if (mIsInForeground) {
                        Log.d("MoPub", "Screen sleep with ad in foreground, disable refresh");
                        mAdView.setAutorefreshEnabled(false);
                    } else {
                        Log.d("MoPub", "Screen sleep but ad in background; " + 
                                "refresh should already be disabled");
                    }
                } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                    if (mIsInForeground) {
                        Log.d("MoPub", "Screen wake / ad in foreground, enable refresh");
                        mAdView.setAutorefreshEnabled(true);
                    } else {
                        Log.d("MoPub", "Screen wake but ad in background; don't enable refresh");
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mContext.registerReceiver(mScreenStateReceiver, filter);
    }
    
    private void unregisterScreenStateBroadcastReceiver() {
        mContext.unregisterReceiver(mScreenStateReceiver);
    }
    
    public void loadAd() {
        if (mAdView != null) mAdView.loadAd();
    }
    
    /*
     * Tears down the ad view: no ads will be shown once this method executes. The parent
     * Activity's onDestroy implementation must include a call to this method.
     */
    public void destroy() {
        unregisterScreenStateBroadcastReceiver();
        
        if (mAdView != null) {
            mAdView.cleanup();
            mAdView = null;
        }
    }

    protected void loadFailUrl() {
        if (mAdView != null) mAdView.loadFailUrl();
    }

    protected void loadNativeSDK(HashMap<String, String> paramsHash) {
        if (mAdapter != null) mAdapter.invalidate();

        String type = paramsHash.get("X-Adtype");
        mAdapter = BaseAdapter.getAdapterForType(this, type, paramsHash);

        if (mAdapter != null) {
            Log.i("MoPub", "Loading native adapter for type: "+type);
            mAdapter.loadAd();
        } else {
            Log.i("MoPub", "Couldn't load native adapter. Trying next ad...");
            loadFailUrl();
        }
    }

    protected void registerClick() {
        if (mAdView != null) {
            mAdView.registerClick();

            // Let any listeners know that an ad was clicked
            adClicked();
        }
    }
    
    protected void loadHtmlString(String html) {
        if (mAdView != null) mAdView.loadResponseString(html);
    }
    
    protected void trackNativeImpression() {
        Log.d("MoPub", "Tracking impression for native adapter.");
        if (mAdView != null) mAdView.trackImpression();
    }

    // Getters and Setters

    public void setAdUnitId(String adUnitId) {
        if (mAdView != null) mAdView.setAdUnitId(adUnitId);
    }

    public void setKeywords(String keywords) {
        if (mAdView != null) mAdView.setKeywords(keywords);
    }

    public String getKeywords() {
        if (mAdView == null) {
            return null;
        }
        return mAdView.getKeywords();
    }

    public void setLocation(Location location) {
        if (mAdView == null) {
            return;
        }
        mAdView.setLocation(location);
    }

    public Location getLocation() {
        if (mAdView == null) {
            return null;
        }
        return mAdView.getLocation();
    }

    public void setTimeout(int milliseconds) {
        if (mAdView == null) {
            return;
        }
        mAdView.setTimeout(milliseconds);
    }

    public int getAdWidth() {
        if (mAdView == null) {
            return 0;
        }
        return mAdView.getAdWidth();
    }

    public int getAdHeight() {
        if (mAdView == null) {
            return 0;
        }
        return mAdView.getAdHeight();
    }

    public HttpResponse getResponse() {
        if (mAdView == null) {
            return null;
        }
        return mAdView.getResponse();
    }

    public String getResponseString() {
        if (mAdView == null) {
            return null;
        }
        return mAdView.getResponseString();
    }

    public Activity getActivity() {
        return mActivity;
    }
    
    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        if (mAdView == null) return;
        
        if (visibility == VISIBLE) {
            Log.d("MoPub", "Ad Unit ("+mAdView.getAdUnitId()+") going visible: enabling refresh");
            mIsInForeground = true;
            mAdView.setAutorefreshEnabled(true);
        }
        else {
            Log.d("MoPub", "Ad Unit ("+mAdView.getAdUnitId()+") going invisible: disabling refresh");
            mIsInForeground = false;
            mAdView.setAutorefreshEnabled(false);
        }
    }

    protected void adWillLoad(String url) {
        Log.d("MoPub", "adWillLoad: " + url);
        if (mOnAdWillLoadListener != null) {
            mOnAdWillLoadListener.OnAdWillLoad(this, url);
        }
    }

    protected void adLoaded() {
        Log.d("MoPub", "adLoaded");
        if (mOnAdLoadedListener != null) {
            mOnAdLoadedListener.OnAdLoaded(this);
        }
    }

    protected void adFailed() {
        if (mOnAdFailedListener != null) {
            mOnAdFailedListener.OnAdFailed(this);
        }
    }

    protected void adClosed() {
        if (mOnAdClosedListener != null) {
            mOnAdClosedListener.OnAdClosed(this);
        }
    }

    protected void adClicked() {
        if (mOnAdClickedListener != null) {
            mOnAdClickedListener.OnAdClicked(this);
        }
    }

    public void setOnAdWillLoadListener(OnAdWillLoadListener listener) {
        mOnAdWillLoadListener = listener;
    }

    public void setOnAdLoadedListener(OnAdLoadedListener listener) {
        mOnAdLoadedListener = listener;
    }

    public void setOnAdFailedListener(OnAdFailedListener listener) {
        mOnAdFailedListener = listener;
    }

    public void setOnAdClosedListener(OnAdClosedListener listener) {
        mOnAdClosedListener = listener;
    }

    public void setOnAdClickedListener(OnAdClickedListener listener) {
        mOnAdClickedListener = listener;
    }
}