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

import android.view.View;

import com.mopub.mobileads.MoPubView.OnAdLoadedListener;

public class MoPubActivity extends BaseActivity implements OnAdLoadedListener {
    public static final int MOPUB_ACTIVITY_NO_AD = 1234;

    private MoPubView mMoPubView;
    
    @Override
    public View getAdView() {
        String adUnitId = getIntent().getStringExtra("com.mopub.mobileads.AdUnitId");
        String keywords = getIntent().getStringExtra("com.mopub.mobileads.Keywords");
        String clickthroughUrl = getIntent().getStringExtra("com.mopub.mobileads.ClickthroughUrl");
        int timeout = getIntent().getIntExtra("com.mopub.mobileads.Timeout", 0);
        
        if (adUnitId == null) {
            throw new RuntimeException("AdUnitId isn't set in com.mopub.mobileads.MoPubActivity");
        }
        
        mMoPubView = new MoPubView(this);
        mMoPubView.setAdUnitId(adUnitId);
        mMoPubView.setKeywords(keywords);
        mMoPubView.setClickthroughUrl(clickthroughUrl);
        mMoPubView.setTimeout(timeout);
        mMoPubView.setOnAdLoadedListener(this); 
        
        String source = getIntent().getStringExtra("com.mopub.mobileads.Source");
        if (source != null) {
            source = sourceWithImpressionTrackingDisabled(source);
            mMoPubView.loadHtmlString(source);
        }
        
        return mMoPubView;
    }
    
    @Override
    protected void onDestroy() {
        mMoPubView.destroy();
        super.onDestroy();
    }
    
    private String sourceWithImpressionTrackingDisabled(String source) {
        // TODO: Temporary fix. Disables impression tracking by renaming the pixel tracker's URL.
        return source.replaceAll("http://ads.mopub.com/m/imp", "mopub://null");
    }

    public void OnAdLoaded(MoPubView m) {
        m.adAppeared();
    }
}
