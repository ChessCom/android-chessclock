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
import android.content.SharedPreferences;
import android.util.Log;
import com.mopub.mobileads.factories.HttpClientFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import static android.content.Context.MODE_PRIVATE;

public class MoPubConversionTracker {
    private static final String TRACK_HOST = "ads.mopub.com";
    private static final String TRACK_HANDLER = "/m/open";
    private static final String PREFERENCE_NAME = "mopubSettings";

    private Context mContext;
    private String mIsTrackedKey;
    private SharedPreferences mSharedPreferences;
    private String mPackageName;

    public void reportAppOpen(Context context) {
        if (context == null) {
            return;
        }

        mContext = context;
        mPackageName = mContext.getPackageName();
        mIsTrackedKey = mPackageName + " tracked";
        mSharedPreferences = mContext.getSharedPreferences(PREFERENCE_NAME, MODE_PRIVATE);

        if (!isAlreadyTracked()) {
            new Thread(new TrackOpen()).start();
        } else {
            Log.d("MoPub", "Conversion already tracked");
        }
    }

    private boolean isAlreadyTracked() {
        return mSharedPreferences.getBoolean(mIsTrackedKey, false);
    }

    private class ConversionUrlGenerator extends BaseUrlGenerator {
        @Override
        public String generateUrlString(String serverHostname) {
            initUrlString(serverHostname, TRACK_HANDLER);

            setApiVersion("6");
            setPackageId(mPackageName);
            setUdid(getUdidFromContext(mContext));
            setAppVersion(getAppVersionFromContext(mContext));
            return getFinalUrlString();
        }

        private void setPackageId(String packageName) {
            addParam("id", packageName);
        }
    }

    private class TrackOpen implements Runnable {
        public void run() {
            String url = new ConversionUrlGenerator().generateUrlString(TRACK_HOST);
            Log.d("MoPub", "Conversion track: " + url);

            DefaultHttpClient httpClient = HttpClientFactory.create();
            HttpResponse response;
            try {
                HttpGet httpget = new HttpGet(url);
                response = httpClient.execute(httpget);
            } catch (Exception e) {
                Log.d("MoPub", "Conversion track failed [" + e.getClass().getSimpleName() + "]: " + url);
                return;
            }

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                Log.d("MoPub", "Conversion track failed: Status code != 200.");
                return;
            }

            HttpEntity entity = response.getEntity();
            if (entity == null || entity.getContentLength() == 0) {
                Log.d("MoPub", "Conversion track failed: Response was empty.");
                return;
            }

            // If we made it here, the request has been tracked
            Log.d("MoPub", "Conversion track successful.");
            mSharedPreferences
                    .edit()
                    .putBoolean(mIsTrackedKey, true)
                    .commit();
        }
    }
}
