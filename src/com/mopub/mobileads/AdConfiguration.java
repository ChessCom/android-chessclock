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
import android.os.Build;
import android.provider.Settings;
import android.webkit.WebView;
import com.mopub.mobileads.util.DateAndTime;
import com.mopub.mobileads.util.VersionCode;
import org.apache.http.HttpResponse;

import java.io.*;
import java.util.*;

import static com.mopub.mobileads.AdFetcher.AD_CONFIGURATION_KEY;
import static com.mopub.mobileads.util.HttpResponses.extractHeader;
import static com.mopub.mobileads.util.HttpResponses.extractIntHeader;
import static com.mopub.mobileads.util.HttpResponses.extractIntegerHeader;
import static com.mopub.mobileads.util.ResponseHeader.AD_TIMEOUT;
import static com.mopub.mobileads.util.ResponseHeader.AD_TYPE;
import static com.mopub.mobileads.util.ResponseHeader.CLICKTHROUGH_URL;
import static com.mopub.mobileads.util.ResponseHeader.DSP_CREATIVE_ID;
import static com.mopub.mobileads.util.ResponseHeader.FAIL_URL;
import static com.mopub.mobileads.util.ResponseHeader.HEIGHT;
import static com.mopub.mobileads.util.ResponseHeader.IMPRESSION_URL;
import static com.mopub.mobileads.util.ResponseHeader.NETWORK_TYPE;
import static com.mopub.mobileads.util.ResponseHeader.REDIRECT_URL;
import static com.mopub.mobileads.util.ResponseHeader.REFRESH_TIME;
import static com.mopub.mobileads.util.ResponseHeader.WIDTH;

public class AdConfiguration implements Serializable {
    private static final int MINIMUM_REFRESH_TIME_MILLISECONDS = 10000;
    private static final int DEFAULT_REFRESH_TIME_MILLISECONDS = 60000;
    private static final String mPlatform = "Android";
    private final String mSdkVersion;

    private final String mHashedUdid;
    private final String mUserAgent;
    private final String mDeviceLocale;
    private final String mDeviceModel;
    private final int mPlatformVersion;

    private String mResponseString;
    private String mAdUnitId;

    private String mAdType;
    private String mNetworkType;
    private String mRedirectUrl;
    private String mClickthroughUrl;
    private String mFailUrl;
    private String mImpressionUrl;
    private long mTimeStamp;
    private int mWidth;
    private int mHeight;
    private Integer mAdTimeoutDelay;
    private int mRefreshTimeMilliseconds;
    private String mDspCreativeId;

    static AdConfiguration extractFromMap(Map<String,Object> map) {
        if (map == null) {
            return null;
        }

        Object adConfiguration = map.get(AD_CONFIGURATION_KEY);

        if (adConfiguration instanceof AdConfiguration) {
            return (AdConfiguration) adConfiguration;
        }

        return null;
    }

    AdConfiguration(final Context context) {
        setDefaults();

        if (context != null) {
            String udid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            mHashedUdid = Utils.sha1((udid != null) ? udid : "");

            mUserAgent = new WebView(context).getSettings().getUserAgentString();
            mDeviceLocale = context.getResources().getConfiguration().locale.toString();
        } else {
            mHashedUdid = null;
            mUserAgent = null;
            mDeviceLocale = null;
        }

        mDeviceModel = Build.MANUFACTURER + " " + Build.MODEL;
        mPlatformVersion = VersionCode.currentApiLevel().getApiLevel();
        mSdkVersion = MoPub.SDK_VERSION;
    }

    void cleanup() {
        setDefaults();
    }

    void addHttpResponse(final HttpResponse httpResponse) {
        mAdType = extractHeader(httpResponse, AD_TYPE);

        // Set the network type of the ad.
        mNetworkType = extractHeader(httpResponse, NETWORK_TYPE);

        // Set the redirect URL prefix: navigating to any matching URLs will send us to the browser.
        mRedirectUrl = extractHeader(httpResponse, REDIRECT_URL);

        // Set the URL that is prepended to links for click-tracking purposes.
        mClickthroughUrl = extractHeader(httpResponse, CLICKTHROUGH_URL);

        // Set the fall-back URL to be used if the current request fails.
        mFailUrl = extractHeader(httpResponse, FAIL_URL);

        // Set the URL to be used for impression tracking.
        mImpressionUrl = extractHeader(httpResponse, IMPRESSION_URL);

        // Set the timestamp used for Ad Alert Reporting.
        mTimeStamp = DateAndTime.now().getTime();

        // Set the width and height.
        mWidth = extractIntHeader(httpResponse, WIDTH, 0);
        mHeight = extractIntHeader(httpResponse, HEIGHT, 0);

        // Set the allowable amount of time an ad has before it automatically fails.
        mAdTimeoutDelay = extractIntegerHeader(httpResponse, AD_TIMEOUT);

        // Set the auto-refresh time. A timer will be scheduled upon ad success or failure.
        if (!httpResponse.containsHeader(REFRESH_TIME.getKey())) {
            mRefreshTimeMilliseconds = 0;
        } else {
            mRefreshTimeMilliseconds = extractIntHeader(httpResponse, REFRESH_TIME, 0) * 1000;
            mRefreshTimeMilliseconds = Math.max(
                    mRefreshTimeMilliseconds,
                    MINIMUM_REFRESH_TIME_MILLISECONDS);
        }

        // Set the unique identifier for the creative that was returned.
        mDspCreativeId = extractHeader(httpResponse, DSP_CREATIVE_ID);
    }

    /*
     * MoPubView
     */

    String getAdUnitId() {
        return mAdUnitId;
    }

    void setAdUnitId(String adUnitId) {
        mAdUnitId = adUnitId;
    }

    String getResponseString() {
        return mResponseString;
    }

    void setResponseString(String responseString) {
        mResponseString = responseString;
    }

    /*
     * HttpResponse
     */

    String getAdType() {
        return mAdType;
    }

    String getNetworkType() {
        return mNetworkType;
    }

    String getRedirectUrl() {
        return mRedirectUrl;
    }

    String getClickthroughUrl() {
        return mClickthroughUrl;
    }

    @Deprecated
    void setClickthroughUrl(String clickthroughUrl) {
        mClickthroughUrl = clickthroughUrl;
    }

    String getFailUrl() {
        return mFailUrl;
    }

    void setFailUrl(String failUrl) {
        mFailUrl = failUrl;
    }

    String getImpressionUrl() {
        return mImpressionUrl;
    }

    long getTimeStamp() {
        return mTimeStamp;
    }

    int getWidth() {
        return mWidth;
    }

    int getHeight() {
        return mHeight;
    }

    Integer getAdTimeoutDelay() {
        return mAdTimeoutDelay;
    }

    int getRefreshTimeMilliseconds() {
        return mRefreshTimeMilliseconds;
    }

    @Deprecated
    void setRefreshTimeMilliseconds(int refreshTimeMilliseconds) {
        mRefreshTimeMilliseconds = refreshTimeMilliseconds;
    }

    String getDspCreativeId() {
        return mDspCreativeId;
    }

    /*
     * Context
     */

    String getHashedUdid() {
        return mHashedUdid;
    }

    String getUserAgent() {
        return mUserAgent;
    }

    String getDeviceLocale() {
        return mDeviceLocale;
    }

    String getDeviceModel() {
        return mDeviceModel;
    }

    int getPlatformVersion() {
        return mPlatformVersion;
    }

    String getPlatform() {
        return mPlatform;
    }

    /*
     * Misc.
     */

    String getSdkVersion() {
        return mSdkVersion;
    }

    private void setDefaults() {
        mAdUnitId = null;
        mResponseString = null;
        mAdType = null;
        mNetworkType = null;
        mRedirectUrl = null;
        mClickthroughUrl = null;
        mImpressionUrl = null;
        mTimeStamp = DateAndTime.now().getTime();
        mWidth = 0;
        mHeight = 0;
        mAdTimeoutDelay = null;
        mRefreshTimeMilliseconds = DEFAULT_REFRESH_TIME_MILLISECONDS;
        mFailUrl = null;
        mDspCreativeId = null;
    }
}
