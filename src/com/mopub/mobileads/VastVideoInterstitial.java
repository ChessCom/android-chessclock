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

import android.net.Uri;
import android.util.Log;
import com.mopub.mobileads.factories.VastManagerFactory;
import com.mopub.mobileads.factories.VastVideoDownloadTaskFactory;
import com.mopub.mobileads.util.Lists;
import com.mopub.mobileads.util.vast.VastManager;

import java.util.*;

class VastVideoInterstitial extends ResponseBodyInterstitial implements VastManager.VastManagerListener, VastVideoDownloadTask.OnDownloadCompleteListener {
    public static final int CACHE_MAX_SIZE = 100 * 1000 * 1000;
    public static final String VIDEO_CACHE_DIRECTORY_NAME = "mopub_vast_video_cache";
    private CustomEventInterstitialListener mCustomEventInterstitialListener;
    private VastVideoDownloadTask mVastVideoDownloadTask;
    private DiskLruCache mVideoCache;
    private String mVastResponse;
    private String mVideoUrl;
    private VastManager mVastManager;
    private ArrayList<String> mVideoStartTrackers;
    private ArrayList<String> mVideoFirstQuartileTrackers;
    private ArrayList<String> mVideoMidpointTrackers;
    private ArrayList<String> mVideoThirdQuartileTrackers;
    private ArrayList<String> mVideoCompleteTrackers;
    private ArrayList<String> mImpressionTrackers;
    private String mClickThroughUrl;
    private ArrayList<String> mClickTrackers;

    @Override
    protected void extractExtras(Map<String, String> serverExtras) {
        mVastResponse = Uri.decode(serverExtras.get(AdFetcher.HTML_RESPONSE_BODY_KEY));
    }

    @Override
    protected void preRenderHtml(CustomEventInterstitialListener customEventInterstitialListener) {
        mCustomEventInterstitialListener = customEventInterstitialListener;

        if (mVideoCache == null) {
            try {
                mVideoCache = new DiskLruCache(mContext, VIDEO_CACHE_DIRECTORY_NAME, CACHE_MAX_SIZE);
            } catch (Exception e) {
                Log.d("MoPub", "Unable to create VAST video cache.");
                mCustomEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.VIDEO_CACHE_ERROR);
                return;
            }
        }

        mVastManager = VastManagerFactory.create();
        mVastManager.processVast(mVastResponse, this);
    }

    @Override
    protected void showInterstitial() {
        MraidVideoPlayerActivity.startVast(mContext,
                mVideoUrl,
                mVideoStartTrackers,
                mVideoFirstQuartileTrackers,
                mVideoMidpointTrackers,
                mVideoThirdQuartileTrackers,
                mVideoCompleteTrackers,
                mImpressionTrackers,
                mClickThroughUrl,
                mClickTrackers
        );
    }

    @Override
    protected void onInvalidate() {
        if (mVastManager != null) {
            mVastManager.cancel();
        }

        super.onInvalidate();
    }

    /*
     * VastManager.VastManagerListener implementation
     */

    @Override
    public void onComplete(VastManager vastManager) {
        mVideoUrl = vastManager.getMediaFileUrl();

        Uri uri = mVideoCache.getUri(mVideoUrl);
        if (uri != null) {
            onDownloadSuccess();
        } else {
            mVastVideoDownloadTask = VastVideoDownloadTaskFactory.create(this, mVideoCache);
            mVastVideoDownloadTask.execute(mVideoUrl);
        }
    }

    /*
     * VastVideoDownloadTask.OnDownloadCompleteListener implementation
     */

    @Override
    public void onDownloadSuccess() {
        mVideoStartTrackers = Lists.asStringArrayList(mVastManager.getVideoStartTrackers());
        mVideoFirstQuartileTrackers = Lists.asStringArrayList(mVastManager.getVideoFirstQuartileTrackers());
        mVideoMidpointTrackers = Lists.asStringArrayList(mVastManager.getVideoMidpointTrackers());
        mVideoThirdQuartileTrackers = Lists.asStringArrayList(mVastManager.getVideoThirdQuartileTrackers());
        mVideoCompleteTrackers = Lists.asStringArrayList(mVastManager.getVideoCompleteTrackers());

        mImpressionTrackers = Lists.asStringArrayList(mVastManager.getImpressionTrackers());

        mClickThroughUrl = mVastManager.getClickThroughUrl();
        mClickTrackers = Lists.asStringArrayList(mVastManager.getClickTrackers());

        mCustomEventInterstitialListener.onInterstitialLoaded();
    }

    @Override
    public void onDownloadFailed() {
        mCustomEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.VIDEO_DOWNLOAD_ERROR);
    }

    @Deprecated // for testing
    DiskLruCache getVideoCache() {
        return mVideoCache;
    }

    @Deprecated // for testing
    String getVastResponse() {
        return mVastResponse;
    }

    @Deprecated // for testing
    void setVastManager(VastManager vastManager) {
        mVastManager = vastManager;
    }
}
