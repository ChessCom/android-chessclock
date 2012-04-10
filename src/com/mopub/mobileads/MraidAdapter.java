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

import com.mopub.mobileads.MraidView.ViewState;

import android.view.Gravity;
import android.widget.FrameLayout;

public class MraidAdapter extends BaseAdapter {
    
    private MraidView mMraidView;
    private boolean mPreviousAutorefreshSetting;
    
    public void init(MoPubView view, String jsonParams) {
        super.init(view, jsonParams);
        mPreviousAutorefreshSetting = false;
    }
    
    @Override
    public void loadAd() {
        if (isInvalidated()) return;

        mMraidView = new MraidView(mMoPubView.getContext());
        mMraidView.loadHtmlData(mJsonParams);
        initMraidListeners();
        
        mMoPubView.removeAllViews();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.FILL_PARENT, 
                FrameLayout.LayoutParams.FILL_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        mMoPubView.addView(mMraidView, layoutParams);
    }

    @Override
    public void invalidate() {
        mMoPubView = null;
        if (mMraidView != null) mMraidView.destroy();
        super.invalidate();
    }
    
    private void initMraidListeners() {
        mMraidView.setOnReadyListener(new MraidView.OnReadyListener() {
            public void onReady(MraidView view) {
                if (!isInvalidated()) {
                    mMoPubView.nativeAdLoaded();
                    mMoPubView.trackNativeImpression();
                }
            }
        });
        
        mMraidView.setOnExpandListener(new MraidView.OnExpandListener() {
            public void onExpand(MraidView view) {
                if (!isInvalidated()) {
                    mPreviousAutorefreshSetting = mMoPubView.getAutorefreshEnabled();
                    mMoPubView.setAutorefreshEnabled(false);
                    mMoPubView.adPresentedOverlay();
                    mMoPubView.registerClick();
                }
            }
        });
        
        mMraidView.setOnCloseListener(new MraidView.OnCloseListener() {
            public void onClose(MraidView view, ViewState newViewState) {
                if (!isInvalidated()) {
                    mMoPubView.setAutorefreshEnabled(mPreviousAutorefreshSetting);
                    mMoPubView.adClosed();
                }
            }
        });

        mMraidView.setOnFailureListener(new MraidView.OnFailureListener() {
           public void onFailure(MraidView view) {
               if (!isInvalidated()) mMoPubView.loadFailUrl();
           } 
        });
    }
}
