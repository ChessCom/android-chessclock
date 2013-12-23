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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.util.*;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MraidVideoPlayerActivity extends BaseInterstitialActivity implements BaseVideoView.BaseVideoViewListener {
    static final String VIDEO_URL = "video_url";
    private static final String VIDEO_CLASS_EXTRAS_KEY = "video_view_class_name";

    private BaseVideoView mVideoView;

    static void startMraid(Context context, String videoUrl) {
        Intent intentVideoPlayerActivity = createIntentMraid(context, videoUrl);
        try {
            context.startActivity(intentVideoPlayerActivity);
        } catch (ActivityNotFoundException e) {
            Log.d("MraidVideoPlayerActivity", "Activity MraidVideoPlayerActivity not found. Did you declare it in your AndroidManifest.xml?");
        }
    }

    static Intent createIntentMraid(Context context, String videoUrl) {
        Intent intentVideoPlayerActivity = new Intent(context, MraidVideoPlayerActivity.class);
        intentVideoPlayerActivity.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intentVideoPlayerActivity.putExtra(VIDEO_CLASS_EXTRAS_KEY, "mraid");
        intentVideoPlayerActivity.putExtra(VIDEO_URL, videoUrl);
        return intentVideoPlayerActivity;
    }

    static void startVast(
            Context context,
            String videoUrl,
            ArrayList<String> videoStartTrackers,
            ArrayList<String> videoFirstQuartileTrackers,
            ArrayList<String> videoMidpointTrackers,
            ArrayList<String> videoThirdQuartileTrackers,
            ArrayList<String> videoCompleteTrackers,
            ArrayList<String> impressionTrackers,
            String clickThroughUrl,
            ArrayList<String> clickThroughTrackers) {

        if (videoUrl == null) {
            return;
        }

        Intent intentVideoPlayerActivity = createIntentVast(
                context,
                videoUrl,
                videoStartTrackers,
                videoFirstQuartileTrackers,
                videoMidpointTrackers,
                videoThirdQuartileTrackers,
                videoCompleteTrackers,
                impressionTrackers,
                clickThroughUrl,
                clickThroughTrackers);
        try {
            context.startActivity(intentVideoPlayerActivity);
        } catch (ActivityNotFoundException e) {
            Log.d("MoPub", "Activity MraidVideoPlayerActivity not found. Did you declare it in your AndroidManifest.xml?");
        }
    }

    static Intent createIntentVast(
            Context context,
            String videoUrl,
            ArrayList<String> videoStartTrackers,
            ArrayList<String> videoFirstQuartileTrackers,
            ArrayList<String> videoMidpointTrackers,
            ArrayList<String> videoThirdQuartileTrackers,
            ArrayList<String> videoCompleteTrackers,
            ArrayList<String> impressionTrackers,
            String clickThroughUrl,
            ArrayList<String> clickThroughTrackers) {
        Intent intentVideoPlayerActivity = new Intent(context, MraidVideoPlayerActivity.class);
        intentVideoPlayerActivity.setFlags(FLAG_ACTIVITY_NEW_TASK);
        intentVideoPlayerActivity.putExtra(VIDEO_CLASS_EXTRAS_KEY, "vast");
        intentVideoPlayerActivity.putExtra(VIDEO_URL, videoUrl);
        intentVideoPlayerActivity.putStringArrayListExtra(VastVideoView.VIDEO_START_TRACKERS, videoStartTrackers);
        intentVideoPlayerActivity.putStringArrayListExtra(VastVideoView.VIDEO_FIRST_QUARTER_TRACKERS, videoFirstQuartileTrackers);
        intentVideoPlayerActivity.putStringArrayListExtra(VastVideoView.VIDEO_MID_POINT_TRACKERS, videoMidpointTrackers);
        intentVideoPlayerActivity.putStringArrayListExtra(VastVideoView.VIDEO_THIRD_QUARTER_TRACKERS, videoThirdQuartileTrackers);
        intentVideoPlayerActivity.putStringArrayListExtra(VastVideoView.VIDEO_COMPLETE_TRACKERS, videoCompleteTrackers);
        intentVideoPlayerActivity.putStringArrayListExtra(VastVideoView.VIDEO_IMPRESSION_TRACKERS, impressionTrackers);
        intentVideoPlayerActivity.putExtra(VastVideoView.VIDEO_CLICK_THROUGH_URL, clickThroughUrl);
        intentVideoPlayerActivity.putStringArrayListExtra(VastVideoView.VIDEO_CLICK_THROUGH_TRACKERS, clickThroughTrackers);
        return intentVideoPlayerActivity;
    }

    @Override
    public View getAdView() {
        mVideoView = createVideoView();
        return mVideoView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideInterstitialCloseButton();
        mVideoView.start();

        broadcastVastInterstitialAction(ACTION_INTERSTITIAL_SHOW);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.onResume();
    }

    @Override
    protected void onPause() {
        mVideoView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        broadcastVastInterstitialAction(ACTION_INTERSTITIAL_DISMISS);
        super.onDestroy();
    }

    private BaseVideoView createVideoView() {
        String clazz = getIntent().getStringExtra(VIDEO_CLASS_EXTRAS_KEY);

        if ("vast".equals(clazz)) {
            return new VastVideoView(this, getIntent(), this);
        } else if ("mraid".equals(clazz)) {
            return new MraidVideoView(this, getIntent(), this);
        } else {
            broadcastInterstitialAction(ACTION_INTERSTITIAL_FAIL);
            finish();
            return new BaseVideoView(this) {};
        }
    }

    /*
     * Implementation of BaseVideoView.CloseButtonStatusListener
     */

    @Override
    public void showCloseButton() {
        showInterstitialCloseButton();
    }

    @Override
    public void videoError(boolean shouldFinish) {
        Log.d("MoPub", "Error: video can not be played.");
        showInterstitialCloseButton();
        broadcastInterstitialAction(ACTION_INTERSTITIAL_FAIL);
        if (shouldFinish) {
            finish();
        }
    }

    @Override
    public void videoCompleted(boolean shouldFinish) {
        showInterstitialCloseButton();
        if (shouldFinish) {
            finish();
        }
    }

    @Override
    public void videoClicked() {
        broadcastInterstitialAction(ACTION_INTERSTITIAL_CLICK);
    }

    /*
     * XXX Nathan: MraidVideoViews have already signalled that they have displayed/dismissed by this point.
     * VastVideoViews, however, do not have a "splash screen", so this is their only opportunity to
     * relay the shown/dismissed callback.
     */
    private void broadcastVastInterstitialAction(String action) {
        if (mVideoView instanceof VastVideoView) {
            broadcastInterstitialAction(action);
        }
    }
}
