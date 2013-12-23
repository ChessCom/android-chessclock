/*
 * Copyright (c) 2010-2013, MoPub Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *  Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 *  Neither the name of 'MoPub Inc.' nor the names of its contributors
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
import android.os.Handler;

import static com.mopub.mobileads.CustomEventInterstitial.CustomEventInterstitialListener;

public class HtmlInterstitialWebView extends BaseHtmlWebView {
    private Handler mHandler;

    interface MoPubUriJavascriptFireFinishLoadListener {
        abstract void onInterstitialLoaded();
    }

    public HtmlInterstitialWebView(Context context, AdConfiguration adConfiguration) {
        super(context, adConfiguration);

        mHandler = new Handler();
    }

    public void init(final CustomEventInterstitialListener customEventInterstitialListener, boolean isScrollable, String redirectUrl, String clickthroughUrl) {
        super.init(isScrollable);

        HtmlInterstitialWebViewListener htmlInterstitialWebViewListener = new HtmlInterstitialWebViewListener(customEventInterstitialListener);
        HtmlWebViewClient htmlWebViewClient = new HtmlWebViewClient(htmlInterstitialWebViewListener, this, clickthroughUrl, redirectUrl);
        setWebViewClient(htmlWebViewClient);

        addMoPubUriJavascriptInterface(new MoPubUriJavascriptFireFinishLoadListener() {
            @Override
            public void onInterstitialLoaded() {
                customEventInterstitialListener.onInterstitialLoaded();
            }
        });
    }

    private void postHandlerRunnable(Runnable r) {
        mHandler.post(r);
    }

    /*
     * XXX (2/15/12): This is a workaround for a problem on ICS devices where
     * WebViews with layout height WRAP_CONTENT can mysteriously render with
     * zero height. This seems to happen when calling loadData() with HTML that
     * sets window.location during its "onload" event. We use loadData() when
     * displaying interstitials, and our creatives use window.location to
     * communicate ad loading status to AdViews. This results in zero-height
     * interstitials. We counteract this by using a Javascript interface object
     * to signal loading status, rather than modifying window.location.
     */
    void addMoPubUriJavascriptInterface(final MoPubUriJavascriptFireFinishLoadListener moPubUriJavascriptFireFinishLoadListener) {
        final class MoPubUriJavascriptInterface {
            // This method appears to be unused, since it will only be called from JavaScript.
            @SuppressWarnings("unused")
            public boolean fireFinishLoad() {
                HtmlInterstitialWebView.this.postHandlerRunnable(new Runnable() {
                    @Override
                    public void run() {
                        moPubUriJavascriptFireFinishLoadListener.onInterstitialLoaded();
                    }
                });
                return true;
            }
        }

        addJavascriptInterface(new MoPubUriJavascriptInterface(), "mopubUriInterface");
    }

    static class HtmlInterstitialWebViewListener implements HtmlWebViewListener {
        private final CustomEventInterstitialListener mCustomEventInterstitialListener;

        public HtmlInterstitialWebViewListener(CustomEventInterstitialListener customEventInterstitialListener) {
            mCustomEventInterstitialListener = customEventInterstitialListener;
        }

        @Override
        public void onLoaded(BaseHtmlWebView mHtmlWebView) {
            mCustomEventInterstitialListener.onInterstitialLoaded();
        }

        @Override
        public void onFailed(MoPubErrorCode errorCode) {
            mCustomEventInterstitialListener.onInterstitialFailed(errorCode);
        }

        @Override
        public void onClicked() {
            mCustomEventInterstitialListener.onInterstitialClicked();
        }

        @Override
        public void onCollapsed() {
            // Ignored
        }
    }
}
