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
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.HashMap;

public class AdView extends WebView {
    
    public static final String AD_ORIENTATION_PORTRAIT_ONLY      = "p";
    public static final String AD_ORIENTATION_LANDSCAPE_ONLY     = "l";
    public static final String AD_ORIENTATION_BOTH               = "b";
    public static final long MINIMUM_REFRESH_TIME_MILLISECONDS = 10000;
    
    private String mAdUnitId;
    private String mKeywords;
    private String mUrl;
    private String mClickthroughUrl;
    private String mRedirectUrl;
    private String mFailUrl;
    private String mImpressionUrl;
    private Location mLocation;
    private boolean mIsLoading;
    private boolean mAutorefreshEnabled;
    private long mRefreshTime = 0;
    private int mTimeout = -1; // HTTP connection timeout in msec
    private int mWidth;
    private int mHeight;
    private String mAdOrientation;

    private MoPubView mMoPubView;
    private HttpResponse mResponse;
    private String mResponseString;

    public AdView(Context context, MoPubView view) {
        super(context);

        mMoPubView = view;
        mAutorefreshEnabled = true;

        setVerticalScrollBarEnabled(false);
        setHorizontalScrollBarEnabled(false);
        
        getSettings().setJavaScriptEnabled(true);
        getSettings().setPluginsEnabled(true);

        // Prevent user from scrolling the web view since it always adds a margin
        setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return(event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });

        // Set background transparent since some ads don't fill the full width
        setBackgroundColor(0);

        // set web view client
        setWebViewClient(new AdWebViewClient());
    }

    // Have to override loadUrl() in order to get the headers, which
    // MoPub uses to pass control information to the client.  Unfortunately
    // Android WebView doesn't let us get to the headers...
    @Override
    public void loadUrl(String url) {
        if (url.startsWith("javascript:")) {
            super.loadUrl(url);
            return;
        }

        // If this ad view is already loading a request, don't proceed; instead, wait
        // for the previous load to finish.
        if (mIsLoading) {
            Log.i("MoPub", "Already loading an ad for "+mAdUnitId+", wait to finish.");
            return;
        }
        mUrl = url;
        mIsLoading = true;
        new LoadUrlTask().execute(mUrl);
    }

    private class LoadUrlTask extends AsyncTask<String, Void, HttpResponse> {
        protected HttpResponse doInBackground(String... urls) {
            return loadAdFromNetwork(urls[0]);
        }
        protected void onPostExecute(HttpResponse response) {
            handleAdFromNetwork(response);
        }
    }

    private HttpResponse loadAdFromNetwork(String url) {
        HttpParams httpParameters = new BasicHttpParams();

        if (mTimeout > 0) {
            // Set the timeout in milliseconds until a connection is established.
            int timeoutConnection = mTimeout;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            // Set the default socket timeout (SO_TIMEOUT)
            // in milliseconds which is the timeout for waiting for data.
            int timeoutSocket = mTimeout;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        }

        // Set the buffer size to avoid OutOfMemoryError exceptions on certain HTC devices.
        // http://stackoverflow.com/questions/5358014/android-httpclient-oom-on-4g-lte-htc-thunderbolt
        HttpConnectionParams.setSocketBufferSize(httpParameters, 8192);

        HttpGet httpget = new HttpGet(url);
        httpget.addHeader("User-Agent", getSettings().getUserAgentString());
        DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);
        try {
            return httpclient.execute(httpget);
        } catch (ClientProtocolException e) {
            pageFailed();
            return null;
        } catch (IOException e) {
            pageFailed();
            return null;
        }
    }

    private void handleAdFromNetwork(HttpResponse response) {
        mResponse = response;
        if (mResponse == null || mResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            pageFailed();
            return;
        }

        HttpEntity entity = mResponse.getEntity();
        if (entity == null || entity.getContentLength() == 0) {
            pageFailed();
            return;
        }

        // Get the various header messages
        // If there is no ad, don't bother loading the data
        Header atHeader = mResponse.getFirstHeader("X-Adtype");
        if (atHeader == null || atHeader.getValue().equals("clear")) {
            pageFailed();
            return;
        }


        // If we made it this far, an ad has been loaded
        
        // Get the network header message
        Header netHeader = mResponse.getFirstHeader("X-Networktype");
        if (netHeader != null && !netHeader.getValue().equals("")){
        	Log.i("MoPub","Fetching Ad Network type: "+netHeader.getValue());
        }

        // Redirect if we get an X-Launchpage header so that AdMob clicks work
        Header rdHeader = mResponse.getFirstHeader("X-Launchpage");
        if (rdHeader != null) {
            mRedirectUrl = rdHeader.getValue();
        }
        else {
            mRedirectUrl = null;
        }

        Header ctHeader = mResponse.getFirstHeader("X-Clickthrough");
        if (ctHeader != null) {
            mClickthroughUrl = ctHeader.getValue();
        }
        else {
            mClickthroughUrl = null;
        }

        Header flHeader = mResponse.getFirstHeader("X-Failurl");
        if (flHeader != null) {
            mFailUrl = flHeader.getValue();
        }
        else {
            mFailUrl = null;
        }
        
        Header imHeader = mResponse.getFirstHeader("X-Imptracker");
        if (imHeader != null) {
            mImpressionUrl = imHeader.getValue();
        }
        else {
            mImpressionUrl = null;
        }

        Header wHeader = mResponse.getFirstHeader("X-Width");
        Header hHeader = mResponse.getFirstHeader("X-Height");
        if (wHeader != null && hHeader != null) {
            mWidth = Integer.parseInt(wHeader.getValue().trim());
            mHeight = Integer.parseInt(hHeader.getValue().trim());
        }
        else {
            mWidth = 0;
            mHeight = 0;
        }

        // Get the period for the autorefresh timer, which will be scheduled either when the ad
        // appears, or if it fails to load.  Passed down as seconds, but stored as milliseconds.
        Header rtHeader = mResponse.getFirstHeader("X-Refreshtime");
        if (rtHeader != null) {
            mRefreshTime = Long.valueOf(rtHeader.getValue()) * 1000;
            if (mRefreshTime < MINIMUM_REFRESH_TIME_MILLISECONDS) {
                mRefreshTime = MINIMUM_REFRESH_TIME_MILLISECONDS;
            }
        }
        else {
            mRefreshTime = 0;
        }
        
        Header orHeader = mResponse.getFirstHeader("X-Orientation");
        mAdOrientation = (orHeader != null) ? orHeader.getValue() : null;
        
        if (atHeader.getValue().equals("custom")) {
            Log.i("MoPub", "Performing custom event");
            Header cmHeader = mResponse.getFirstHeader("X-Customselector");
            mIsLoading = false;
            performCustomEventFromHeader(cmHeader);
            return;
        }
        // Handle requests for native SDK ads
        else if (!atHeader.getValue().equals("html")) {
            Log.i("MoPub","Loading native ad");
            Header npHeader = mResponse.getFirstHeader("X-Nativeparams");
            if (npHeader != null) {
                mIsLoading = false;
                
                HashMap<String, String> paramsHash = new HashMap<String, String>();
                paramsHash.put("X-Adtype", atHeader.getValue());
                paramsHash.put("X-Nativeparams", npHeader.getValue());
                Header ftHeader = mResponse.getFirstHeader("X-Fulladtype");
                if (ftHeader != null) paramsHash.put("X-Fulladtype", ftHeader.getValue());
                
                mMoPubView.loadNativeSDK(paramsHash);
                return;
            }
            else {
                pageFailed();
                return;
            }
        }

        mResponseString = null;
        StringBuilder sb = new StringBuilder();
        InputStream is;
        try {
            is = entity.getContent();
        } catch (IllegalStateException e1) {
            pageFailed();
            return;
        } catch (IOException e1) {
            pageFailed();
            return;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            pageFailed();
            return;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // Ignore since at this point we have the data we need
            }
        }
        mResponseString = sb.toString();
        loadDataWithBaseURL("http://"+MoPubView.HOST+"/",
                mResponseString,"text/html","utf-8", null);
    }
    
    private void performCustomEventFromHeader(Header methodHeader) {
        if (methodHeader == null) {
            Log.i("MoPub", "Couldn't call custom method because the server did not specify one.");
            mMoPubView.adFailed();
            return;
        }
        
        String methodName = methodHeader.getValue();
        Log.i("MoPub", "Trying to call method named " + methodName);
        
        Class<? extends Activity> c;
        Method method;
        Activity userActivity = mMoPubView.getActivity();
        try {
            c = userActivity.getClass();
            method = c.getMethod(methodName, MoPubView.class);
            method.invoke(userActivity, mMoPubView);
        } catch (NoSuchMethodException e) {
            Log.d("MoPub", "Couldn't perform custom method named " + methodName +
                    "(MoPubView view) because your activity class has no such method");
            return;
        } catch (Exception e) {
            Log.d("MoPub", "Couldn't perform custom method named " + methodName);
            return;
        }
    }

    private String generateAdUrl() {
        StringBuilder sz = new StringBuilder("http://"+MoPubView.HOST+MoPubView.AD_HANDLER);
        sz.append("?v=3&id=" + mAdUnitId);
        sz.append("&udid="+Secure.getString(getContext().getContentResolver(), Secure.ANDROID_ID));

        if (mKeywords != null) {
            sz.append("&q=" + Uri.encode(mKeywords));
        }
        if (mLocation != null) {
            sz.append("&ll=" + mLocation.getLatitude() + "," + mLocation.getLongitude());
        }
        return sz.toString();
    }

    public void loadAd() {
        if (mAdUnitId == null) {
            throw new RuntimeException("AdUnitId isn't set in com.mopub.mobileads.AdView");
        }

        // Get the last location if one hasn't been provided through setLocation()
        // This leaves mLocation = null if no providers are available
        if (mLocation == null) {
            LocationManager lm 
                    = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            try {
                mLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } catch (SecurityException e) {
                // Ignore since access to location may be disabled
            }
            Location loc_network = null;
            try {
                loc_network= lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } catch (SecurityException e) {
                // Ignore since access to location may be disabled
            }

            if (mLocation == null) {
                mLocation = loc_network;
            }
            else if (loc_network != null && loc_network.getTime() > mLocation.getTime()) {
                mLocation = loc_network;
            }
        }

        String adUrl = generateAdUrl();
        Log.d("MoPub", "ad url: "+adUrl);
        mMoPubView.adWillLoad(adUrl);
        loadUrl(adUrl);
    }
    
    protected void loadResponseString(String responseString) {
        loadDataWithBaseURL("http://"+MoPubView.HOST+"/",
                responseString,"text/html","utf-8", null);
    }

    @Override
    public void reload() {
        Log.d("MoPub", "Reload ad: "+mUrl);
        loadUrl(mUrl);
    }

    public void loadFailUrl() {
        mIsLoading = false;
        if (mFailUrl != null) {
            Log.d("MoPub", "Loading failover url: "+mFailUrl);
            loadUrl(mFailUrl);
        }
        else {
            pageFailed();
        }
    }

    protected void trackImpression() {
        if (mImpressionUrl == null)
            return;
        
        new Thread(new Runnable() {
            public void run () {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(mImpressionUrl);
                httpget.addHeader("User-Agent", getSettings().getUserAgentString());
                try {
                    httpclient.execute(httpget);
                } catch (ClientProtocolException e) {
                    Log.i("MoPub", "Impression tracking failed: "+mImpressionUrl);
                } catch (IOException e) {
                    Log.i("MoPub", "Impression tracking failed: "+mImpressionUrl);
                }
            }
        }).start();
    }
    
    protected void registerClick() {
        if (mClickthroughUrl == null)
            return;

        new Thread(new Runnable() {
            public void run () {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(mClickthroughUrl);
                httpget.addHeader("User-Agent", getSettings().getUserAgentString());
                try {
                    httpclient.execute(httpget);
                } catch (ClientProtocolException e) {
                    Log.i("MoPub", "Click tracking failed: "+mClickthroughUrl);
                } catch (IOException e) {
                    Log.i("MoPub", "Click tracking failed: "+mClickthroughUrl);
                }
            }
        }).start();
    }

    private void pageFinished() {
        Log.i("MoPub","pageFinished");
        mIsLoading = false;
        if (mAutorefreshEnabled) scheduleRefreshTimer();
        mMoPubView.removeAllViews();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        mMoPubView.addView(this, layoutParams);

        mMoPubView.adLoaded();
    }

    private void pageFailed() {
        Log.i("MoPub", "pageFailed");
        mIsLoading = false;
        if (mAutorefreshEnabled) scheduleRefreshTimer();
        mMoPubView.adFailed();
    }

    private void pageClosed() {
        mMoPubView.adClosed();
    }

    private Handler mRefreshHandler = new Handler();
    private Runnable mRefreshRunnable = new Runnable() {
        public void run() {
            loadAd();
        }
    };

    protected void scheduleRefreshTimer() {
        // Cancel any previously scheduled refreshes.
        cancelRefreshTimer();
        if (mRefreshTime <= 0) {
            return;
        }

        mRefreshHandler.postDelayed(mRefreshRunnable, mRefreshTime);
    }

    protected void cancelRefreshTimer() {
        mRefreshHandler.removeCallbacks(mRefreshRunnable);
    }

    // Getters and Setters

    public String getKeywords() {
        return mKeywords;
    }

    public void setKeywords(String keywords) {
        mKeywords = keywords;
    }

    public Location getLocation() {
        return mLocation;
    }

    public void setLocation(Location location) {
        mLocation = location;
    }

    public String getAdUnitId() {
        return mAdUnitId;
    }

    public void setAdUnitId(String adUnitId) {
        mAdUnitId = adUnitId;
    }

    public void setTimeout(int milliseconds) {
        mTimeout = milliseconds;
    }

    public int getAdWidth() {
        return mWidth;
    }

    public int getAdHeight() {
        return mHeight;
    }
    
    public String getAdOrientation() {
        return mAdOrientation;
    }

    public String getClickthroughUrl() {
        return mClickthroughUrl;
    }

    public String getRedirectUrl() {
        return mRedirectUrl;
    }

    public HttpResponse getResponse() {
        return mResponse;
    }

    public String getResponseString() {
        return mResponseString;
    }
    
    public void setAutorefreshEnabled(boolean enabled) {
        mAutorefreshEnabled = enabled;
        if (!mAutorefreshEnabled) cancelRefreshTimer();
    }
    
    public boolean getAutorefreshEnabled() {
        return mAutorefreshEnabled;
    }

    private class AdWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("MoPub", "url: "+url);

            // Check if this is a local call
            if (url.startsWith("mopub://")) {
                if (url.equals("mopub://finishLoad")) {
                    ((AdView)view).pageFinished();
                }
                else if (url.equals("mopub://close")) {
                    ((AdView)view).pageClosed();
                }
                else if (url.equals("mopub://failLoad")) {
                    ((AdView)view).loadFailUrl();
                }
                return true;
            }

            String uri = url;

            String clickthroughUrl = ((AdView)view).getClickthroughUrl();
            if (clickthroughUrl != null) {
                uri = clickthroughUrl + "&r=" + Uri.encode(url);
            }

            Log.d("MoPub", "click url: "+uri);
            mMoPubView.adClicked();

            // and fire off a system wide intent
            view.getContext().startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            String redirectUrl = ((AdView)view).getRedirectUrl();
            if (redirectUrl != null && url.startsWith(redirectUrl)) {
                view.stopLoading();
                view.getContext().startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url)));
            }
        }
    }
}
