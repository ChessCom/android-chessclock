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

import java.util.HashMap;

import com.mopub.mobileads.MoPubView.OnAdClickedListener;
import com.mopub.mobileads.MoPubView.OnAdFailedListener;
import com.mopub.mobileads.MoPubView.OnAdLoadedListener;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MoPubInterstitial {
    
    private MoPubInterstitialView mInterstitialView;
    private MoPubInterstitialListener mListener;
    private Activity mActivity;
    private String mAdUnitId;
    
    public interface MoPubInterstitialListener {
        public void OnInterstitialLoaded();
        public void OnInterstitialFailed();
        public void OnInterstitialClosed();
    }
    
    public class MoPubInterstitialView extends MoPubView {
        
        public MoPubInterstitialView(Context context) {
            super(context);
        }

        @Override
        protected void loadNativeSDK(HashMap<String, String> paramsHash) {
            MoPubInterstitial parent = MoPubInterstitial.this;
            String type = paramsHash.get("X-Adtype");

            if (type != null && type.equals("interstitial")) {
                Log.i("MoPub", "Loading native adapter for type: "+type);
                BaseInterstitialAdapter adapter =
                        BaseInterstitialAdapter.getAdapterForType(parent, type, paramsHash);
                if (adapter != null) {
                    adapter.loadInterstitial();
                    return;
                }
            }
            
            Log.i("MoPub", "Couldn't load native adapter. Trying next ad...");
            parent.interstitialFailed();
        }
    }
    
    public MoPubInterstitial(Activity activity, String id) {
        mActivity = activity;
        mAdUnitId = id;
        
        mInterstitialView = new MoPubInterstitialView(mActivity);
        mInterstitialView.setAdUnitId(mAdUnitId);
        mInterstitialView.setOnAdLoadedListener(new OnAdLoadedListener() {
            public void OnAdLoaded(MoPubView m) {
                if (mListener != null) {
                    mListener.OnInterstitialLoaded();
                }
                
                if (mActivity != null) {
                    String responseString = mInterstitialView.getResponseString();
                    Intent i = new Intent(mActivity, MoPubActivity.class);
                    i.putExtra("com.mopub.mobileads.AdUnitId", mAdUnitId);
                    i.putExtra("com.mopub.mobileads.Source", responseString);
                    mActivity.startActivity(i);
                }
            }
        });
        mInterstitialView.setOnAdFailedListener(new OnAdFailedListener() {
            public void OnAdFailed(MoPubView m) {
                if (mListener != null) {
                    mListener.OnInterstitialFailed();
                }
            }
        });
        mInterstitialView.setOnAdClickedListener(new OnAdClickedListener() {
            public void OnAdClicked(MoPubView m) {
                if (mInterstitialView != null) {
                    mInterstitialView.registerClick();
                }
            }
        });
    }
    
    public Activity getActivity() {
        return mActivity;
    }
    
    public void showAd() {
        mInterstitialView.loadAd();
    }
/* TODO:
    public void prefetchAd() {
    
    }
    
    public void showPrefetchedAd() {
        
    }
*/
    public void setListener(MoPubInterstitialListener listener) {
        mListener = listener;
    }
    
    public MoPubInterstitialListener getListener() {
        return mListener;
    }
    
    protected void interstitialLoaded() {
        if (mListener != null) {
            mListener.OnInterstitialLoaded();
        }
    }
    
    protected void interstitialFailed() {
        if (mInterstitialView != null) {
            mInterstitialView.loadFailUrl();
        }
    }
    
    protected void interstitialClicked() {
        if (mInterstitialView != null) {
            mInterstitialView.registerClick();
        }
    }
    
    protected void interstitialClosed() {
        if (mListener != null) {
            mListener.OnInterstitialClosed();
        }
    }
}
