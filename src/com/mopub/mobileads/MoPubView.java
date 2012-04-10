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
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.WebViewDatabase;
import android.widget.FrameLayout;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
    
    public interface OnAdPresentedOverlayListener {
        public void OnAdPresentedOverlay(MoPubView m);
    }
    
    public enum LocationAwareness {
        LOCATION_AWARENESS_NORMAL, LOCATION_AWARENESS_TRUNCATED, LOCATION_AWARENESS_DISABLED
    }

    public static final String HOST = "ads.mopub.com";
    public static final String AD_HANDLER = "/m/ad";
    public static final int DEFAULT_LOCATION_PRECISION = 6;

    protected AdView mAdView;
    protected BaseAdapter mAdapter;
    
    private Context mContext;
    private BroadcastReceiver mScreenStateReceiver;
    private boolean mIsInForeground;
    private LocationAwareness mLocationAwareness;
    private int mLocationPrecision;
    private boolean mPreviousAutorefreshSetting = false;

    private OnAdWillLoadListener mOnAdWillLoadListener;
    private OnAdLoadedListener mOnAdLoadedListener;
    private OnAdFailedListener mOnAdFailedListener;
    private OnAdPresentedOverlayListener mOnAdPresentedOverlayListener;
    private OnAdClosedListener mOnAdClosedListener;
    private OnAdClickedListener mOnAdClickedListener;

    public MoPubView(Context context) {
        this(context, null);
    }

    public MoPubView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mIsInForeground = (getVisibility() == VISIBLE);
        mLocationAwareness = LocationAwareness.LOCATION_AWARENESS_NORMAL;
        mLocationPrecision = DEFAULT_LOCATION_PRECISION;
        
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
        
        initVersionDependentAdView(context);
        registerScreenStateBroadcastReceiver();
    }
    
    private void initVersionDependentAdView(Context context) {
        int sdkVersion = (new Integer(Build.VERSION.SDK)).intValue();
        if (sdkVersion < 7) {
        	mAdView = new AdView(context, this);
        } else {
            // On Android 2.1 (Eclair) and up, try to load our HTML5-enabled AdView class.
            Class<?> HTML5AdViewClass = null;
            try {
                HTML5AdViewClass = (Class<?>) Class.forName("com.mopub.mobileads.HTML5AdView");
            } catch (ClassNotFoundException e) {
                mAdView = new AdView(context, this);
                return;
            } 

            Class<?>[] parameterTypes = new Class[2];
            parameterTypes[0] = Context.class;
            parameterTypes[1] = MoPubView.class;

            Object[] args = new Object[2];
            args[0] = context;
            args[1] = this;

            try {
                Constructor<?> constructor = HTML5AdViewClass.getConstructor(parameterTypes);
                mAdView = (AdView) constructor.newInstance(args);
            } catch (SecurityException e) {
                Log.e("MoPub", "Could not load HTML5AdView.");
            } catch (NoSuchMethodException e) {
                Log.e("MoPub", "Could not load HTML5AdView.");
            } catch (IllegalArgumentException e) {
                Log.e("MoPub", "Could not load HTML5AdView.");
            } catch (InstantiationException e) {
                Log.e("MoPub", "Could not load HTML5AdView.");
            } catch (IllegalAccessException e) {
                Log.e("MoPub", "Could not load HTML5AdView.");
            } catch (InvocationTargetException e) {
                Log.e("MoPub", "Could not load HTML5AdView.");
            }

            if (mAdView == null) mAdView = new AdView(context, this);
        }
    }

    private void registerScreenStateBroadcastReceiver() {
        if (mAdView == null) return;
        
        mScreenStateReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    if (mIsInForeground) {
                        Log.d("MoPub", "Screen sleep with ad in foreground, disable refresh");
                        if (mAdView != null) {
                            mPreviousAutorefreshSetting = mAdView.getAutorefreshEnabled();
                            mAdView.setAutorefreshEnabled(false);
                        }
                    } else {
                        Log.d("MoPub", "Screen sleep but ad in background; " + 
                                "refresh should already be disabled");
                    }
                } else if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
                    if (mIsInForeground) {
                        Log.d("MoPub", "Screen wake / ad in foreground, reset refresh");
                        if (mAdView != null) {
                            mAdView.setAutorefreshEnabled(mPreviousAutorefreshSetting);
                        }
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
        try {
            mContext.unregisterReceiver(mScreenStateReceiver);
        } catch (Exception IllegalArgumentException) {
            Log.d("MoPub", "Failed to unregister screen state broadcast receiver (never registered).");
        }
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
        
        if (mAdapter != null) {
            mAdapter.invalidate();
            mAdapter = null;
        }
    }

    protected void loadFailUrl() {
        if (mAdView != null) mAdView.loadFailUrl();
    }

    protected void loadNativeSDK(HashMap<String, String> paramsHash) {
        if (mAdapter != null) mAdapter.invalidate();

        String type = paramsHash.get("X-Adtype");
        mAdapter = BaseAdapter.getAdapterForType(type);

        if (mAdapter != null) {
            Log.i("MoPub", "Loading native adapter for type: " + type);
            String jsonParams = paramsHash.get("X-Nativeparams");
            mAdapter.init(this, jsonParams);
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
        if (mOnAdWillLoadListener != null) mOnAdWillLoadListener.OnAdWillLoad(this, url);
    }

    protected void adLoaded() {
        Log.d("MoPub", "adLoaded");
        if (mOnAdLoadedListener != null) mOnAdLoadedListener.OnAdLoaded(this);
    }

    protected void adFailed() {
        if (mOnAdFailedListener != null) mOnAdFailedListener.OnAdFailed(this);
    }

    protected void adPresentedOverlay() {
        if (mOnAdPresentedOverlayListener != null) {
            mOnAdPresentedOverlayListener.OnAdPresentedOverlay(this);
        }
    }
    
    protected void adClosed() {
        if (mOnAdClosedListener != null) mOnAdClosedListener.OnAdClosed(this);
    }

    protected void adClicked() {
        if (mOnAdClickedListener != null) mOnAdClickedListener.OnAdClicked(this);
    }
    
    protected void nativeAdLoaded() {
        if (mAdView != null) mAdView.scheduleRefreshTimerIfEnabled();
        adLoaded();
    }
    
    protected void adAppeared() {
        if (mAdView != null) mAdView.adAppeared();
    }

    public void customEventDidLoadAd() {
        if (mAdView != null) mAdView.customEventDidLoadAd();
    }
    
    public void customEventDidFailToLoadAd() {
        if (mAdView != null) mAdView.customEventDidFailToLoadAd();
    }
    
    public void customEventActionWillBegin() {
        if (mAdView != null) mAdView.customEventActionWillBegin();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void setAdUnitId(String adUnitId) {
        if (mAdView != null) mAdView.setAdUnitId(adUnitId);
    }
    
    public void setKeywords(String keywords) {
        if (mAdView != null) mAdView.setKeywords(keywords);
    }

    public String getKeywords() {
        return (mAdView != null) ? mAdView.getKeywords() : null;
    }

    public void setLocation(Location location) {
        if (mAdView != null) mAdView.setLocation(location);
    }

    public Location getLocation() {
        return (mAdView != null) ? mAdView.getLocation() : null;
    }

    public void setTimeout(int milliseconds) {
        if (mAdView != null) mAdView.setTimeout(milliseconds);
    }

    public int getAdWidth() {
        return (mAdView != null) ? mAdView.getAdWidth() : 0;
    }

    public int getAdHeight() {
        return (mAdView != null) ? mAdView.getAdHeight() : 0;
    }

    public String getResponseString() {
        return (mAdView != null) ? mAdView.getResponseString() : null;
    }
    
    public void setClickthroughUrl(String url) {
        if (mAdView != null) mAdView.setClickthroughUrl(url);
    }
    
    public String getClickthroughUrl() {
        return (mAdView != null) ? mAdView.getClickthroughUrl() : null;
    }

    public Activity getActivity() {
        return (Activity) mContext;
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

    public void setOnAdPresentedOverlayListener(OnAdPresentedOverlayListener listener) {
        mOnAdPresentedOverlayListener = listener;
    }
    
    public void setOnAdClosedListener(OnAdClosedListener listener) {
        mOnAdClosedListener = listener;
    }

    public void setOnAdClickedListener(OnAdClickedListener listener) {
        mOnAdClickedListener = listener;
    }
    
    public void setLocationAwareness(LocationAwareness awareness) {
        mLocationAwareness = awareness;
    }

    public LocationAwareness getLocationAwareness() {
        return mLocationAwareness;
    }

    public void setLocationPrecision(int precision) {
        mLocationPrecision = (precision >= 0) ? precision : 0;
    }

    public int getLocationPrecision() {
        return mLocationPrecision;
    }
    
    public void setAutorefreshEnabled(boolean enabled) {
        if (mAdView != null) mAdView.setAutorefreshEnabled(enabled);
    }
    
    public boolean getAutorefreshEnabled() {
        if (mAdView != null) return mAdView.getAutorefreshEnabled();
        else {
            Log.d("MoPub", "Can't get autorefresh status for destroyed MoPubView. " + 
                    "Returning false.");
            return false;
        }
    }
    
    public void setAdContentView(View view) {
        if (mAdView != null) mAdView.setAdContentView(view);
    }
}