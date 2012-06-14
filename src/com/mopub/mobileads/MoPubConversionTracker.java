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

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings.Secure;
import android.util.Log;

import com.mopub.mobileads.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

public class MoPubConversionTracker {
    private Context mContext;
    private String mPackageName;

    static private String TRACK_HOST = "ads.mopub.com";
    static private String TRACK_HANDLER = "/m/open";

    public void reportAppOpen(Context context) {
        if (context == null) {
            return;
        }
        mContext = context;
        mPackageName = mContext.getPackageName();

        SharedPreferences settings = mContext.getSharedPreferences("mopubSettings", 0);
        if (settings.getBoolean(mPackageName+" tracked", false) == false) {
            new Thread(mTrackOpen).start();
        } else {
            Log.d("MoPub", "Conversion already tracked");
        }
    }

    Runnable mTrackOpen = new Runnable() {
        public void run() {
            StringBuilder sz = new StringBuilder("http://"+TRACK_HOST+TRACK_HANDLER);
            sz.append("?v=6&id=" + mPackageName);
            
            String udid = Secure.getString(mContext.getContentResolver(), Secure.ANDROID_ID);
            String udidDigest = (udid == null) ? "" : Utils.sha1(udid);
            sz.append("&udid=sha:" + udidDigest);
            String url = sz.toString();
            Log.d("MoPub", "Conversion track: " + url);

            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            try {
                HttpGet httpget = new HttpGet(url);
                response = httpclient.execute(httpget);
            } catch (IllegalArgumentException e) {
                Log.d("MoPub", "Conversion track failed (IllegalArgumentException): "+url);
                return;
            } catch (ClientProtocolException e) {
                // Just fail silently. We'll try the next time the app opens
                Log.d("MoPub", "Conversion track failed: ClientProtocolException (no signal?)");
                return;
            } catch (IOException e) {
                // Just fail silently. We'll try the next time the app opens
                Log.d("MoPub", "Conversion track failed: IOException (no signal?)");
                return;
            }

            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                Log.d("MoPub", "Conversion track failed: Status code != 200");
                return;
            }

            HttpEntity entity = response.getEntity();
            if (entity == null || entity.getContentLength() == 0) {
                Log.d("MoPub", "Conversion track failed: Response was empty");
                return;
            }

            // If we made it here, the request has been tracked
            Log.d("MoPub", "Conversion track successful");
            SharedPreferences.Editor editor
            = mContext.getSharedPreferences("mopubSettings", 0).edit();
            editor.putBoolean(mPackageName+" tracked", true).commit();
        }
    };
}
