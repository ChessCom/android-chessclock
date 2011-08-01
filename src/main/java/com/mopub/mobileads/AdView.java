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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.mopub.mobileads.MoPubView.LocationAwareness;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AdView extends WebView {
    public static final String AD_ORIENTATION_PORTRAIT_ONLY = "p";
    public static final String AD_ORIENTATION_LANDSCAPE_ONLY = "l";
    public static final String AD_ORIENTATION_BOTH = "b";
    public static final String DEVICE_ORIENTATION_PORTRAIT = "p";
    public static final String DEVICE_ORIENTATION_LANDSCAPE = "l";
    public static final String DEVICE_ORIENTATION_SQUARE = "s";
    public static final String DEVICE_ORIENTATION_UNKNOWN = "u";
    public static final String EXTRA_AD_CLICK_DATA = "com.mopub.intent.extra.AD_CLICK_DATA";
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
    private long mRefreshTimeMilliseconds = 0;
    private int mTimeoutMilliseconds = -1;
    private int mWidth;
    private int mHeight;
    private String mAdOrientation;

    private MoPubView mMoPubView;
    private HttpResponse mResponse;
    private String mResponseString;
    private String mUserAgent;

    public AdView(Context context, MoPubView view) {
        super(context);
        
        mMoPubView = view;
        mAutorefreshEnabled = true;

        // Disable scrolling and zoom
        setHorizontalScrollBarEnabled(false);
        setHorizontalScrollbarOverlay(false);
        setVerticalScrollBarEnabled(false);
        setVerticalScrollbarOverlay(false);
        getSettings().setSupportZoom(false);

        // Store user agent string at beginning to prevent NPE during background thread operations
        mUserAgent = getSettings().getUserAgentString();
        
        getSettings().setJavaScriptEnabled(true);
        getSettings().setPluginsEnabled(true);

        setBackgroundColor(Color.TRANSPARENT);
        setWebViewClient(new AdWebViewClient());
    }
    
    private class AdWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            AdView adView = (AdView) view;
           
            // Handle the special mopub:// scheme calls.
            if (url.startsWith("mopub://")) {
                Uri uri = Uri.parse(url);
                String host = uri.getHost();
                
                if (host.equals("finishLoad")) adView.pageFinished();
                else if (host.equals("close")) adView.pageClosed();
                else if (host.equals("failLoad")) adView.loadFailUrl();
                else if (host.equals("custom")) adView.handleCustomIntentFromUri(uri);
                return true;
            }
            // Handle other phone intents.
            else if (url.startsWith("tel:") || url.startsWith("voicemail:") ||
                    url.startsWith("sms:") || url.startsWith("mailto:") ||
                    url.startsWith("geo:") || url.startsWith("google.streetview:")) { 
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)); 
                try {
                    getContext().startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Log.w("MoPub", "Could not handle intent with URI: " + url +
                        ". Is this intent unsupported on your phone?");
                }
                return true;
            }

            String clickthroughUrl = adView.getClickthroughUrl();
            if (clickthroughUrl != null) url = clickthroughUrl + "&r=" + Uri.encode(url);
            Log.d("MoPub", "Ad clicked. Click URL: " + url);
            mMoPubView.adClicked();

            showBrowserAfterFollowingRedirectsForUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            // If the URL being loaded shares the redirectUrl prefix, open it in the browser.
            String redirectUrl = ((AdView)view).getRedirectUrl();
            if (redirectUrl != null && url.startsWith(redirectUrl)) {
                view.stopLoading();
                showBrowserAfterFollowingRedirectsForUrl(url);
            }
        }
    }
    
    private void pageFinished() {
        Log.i("MoPub", "Ad successfully loaded.");
        mIsLoading = false;
        scheduleRefreshTimerIfEnabled();
        mMoPubView.removeAllViews();
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER);
        mMoPubView.addView(this, layoutParams);

        mMoPubView.adLoaded();
    }

    private void pageFailed() {
        Log.i("MoPub", "Ad failed to load.");
        mIsLoading = false;
        scheduleRefreshTimerIfEnabled();
        mMoPubView.adFailed();
    }

    private void pageClosed() {
        mMoPubView.adClosed();
    }
    
    private void handleCustomIntentFromUri(Uri uri) {
        mMoPubView.adClicked();
        String action = uri.getQueryParameter("fnc");
        String adData = uri.getQueryParameter("data");
        Intent customIntent = new Intent(action);
        if (adData != null) customIntent.putExtra(EXTRA_AD_CLICK_DATA, adData);
        try {
            getContext().startActivity(customIntent);
        } catch (ActivityNotFoundException e) {
            Log.w("MoPub", "Could not handle custom intent: " + action +
                    ". Is your intent spelled correctly?");
        }
    }
    
    private void showBrowserAfterFollowingRedirectsForUrl(String url) {
        new LoadClickedUrlTask().execute(url);
    }
    
    private class LoadClickedUrlTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            String startingUrl = urls[0];
            URL url = null;
            try {
                url = new URL(startingUrl);
            } catch (MalformedURLException e) {
                // If starting URL is a market URL, just return it.
                return (startingUrl.startsWith("market://")) ? startingUrl : "";
            }
            
            // Find the target URL, manually following redirects if necessary. We can't use 
            // HttpClient for this since the target may not be a supported URL (e.g. a market URL).
            int statusCode = -1;
            HttpURLConnection connection = null;
            String nextLocation = url.toString();
            
            // Keep track of where we've been to detect redirect cycles.
            Set<String> redirectLocations = new HashSet<String>();
            redirectLocations.add(nextLocation);
            
            try {
                do {
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestProperty("User-Agent", mUserAgent);
                    connection.setInstanceFollowRedirects(false);
                    
                    statusCode = connection.getResponseCode();
                    if (statusCode == HttpStatus.SC_OK) {
                        // Successfully reached the end of redirects: nextLocation is our target.
                        connection.disconnect();
                        break;
                    } else {
                        // Depending on statusCode, we'll either continue to redirect, or error
                        // out (in which case nextLocation will probably be null).
                        nextLocation = connection.getHeaderField("location");
                        connection.disconnect();
                        
                        // Check for redirect cycle.
                        if (!redirectLocations.add(nextLocation)) {
                            Log.d("MoPub", "Click redirect cycle detected -- will show blank.");
                            return "";
                        }
                            
                        url = new URL(nextLocation);
                    }
                }
                while (isStatusCodeForRedirection(statusCode));
            } catch (IOException e) {
                // Might result from nextLocation being a market URL. If so, return that URL.
                if (nextLocation != null) {
                    return (nextLocation.startsWith("market://")) ? nextLocation : "";
                } else return "";
            } finally {
                if (connection != null) connection.disconnect();
            }
            
            return nextLocation;
        }
        
        private boolean isStatusCodeForRedirection(int statusCode) {
            return (statusCode == HttpStatus.SC_MOVED_TEMPORARILY ||
                    statusCode == HttpStatus.SC_MOVED_PERMANENTLY ||
                    statusCode == HttpStatus.SC_TEMPORARY_REDIRECT ||
                    statusCode == HttpStatus.SC_SEE_OTHER);
        }

        @Override
        protected void onPostExecute(String uri) {
            if (uri == null || uri.equals("")) uri = "about:blank";
            Log.d("MoPub", "Final URI to show in browser: " + uri);
            Intent actionIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            try {
                getContext().startActivity(actionIntent);
            } catch (ActivityNotFoundException e) {
                String action = actionIntent.getAction();
                if (action.startsWith("market://")) {
                    Log.w("MoPub", "Could not handle market action: " + action
                            + ". Perhaps you're running in the emulator, which does not have "
                            + "the Android Market?");
                } else {
                    Log.w("MoPub", "Could not handle intent action: " + action);
                }
                
                getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("about:blank")));
            }
        }
    }

    public void loadAd() {
        if (mAdUnitId == null) {
            throw new RuntimeException("AdUnitId is null for this view. " + 
                    "You may have forgotten to call setAdUnitId().");
        }

        if (mLocation == null) mLocation = getLastKnownLocation();

        String adUrl = generateAdUrl();
        mMoPubView.adWillLoad(adUrl);
        loadUrl(adUrl);
    }
    
    /*
     * Returns the last known location of the device using its GPS and network location providers.
     * May be null if: 
     * - Location permissions are not requested in the Android manifest file
     * - The location providers don't exist
     * - Location awareness is disabled in the parent MoPubView
     */
    private Location getLastKnownLocation() {
        LocationAwareness locationAwareness = mMoPubView.getLocationAwareness();
        int locationPrecision = mMoPubView.getLocationPrecision();
        Location result = null;
        
        if (locationAwareness == LocationAwareness.LOCATION_AWARENESS_DISABLED) {
            return null;
        }
        
        LocationManager lm 
                = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        Location gpsLocation = null;
        try {
            gpsLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        } catch (SecurityException e) {
            Log.d("MoPub", "Failed to retrieve location: access appears to be disabled.");
        } catch (IllegalArgumentException e) {
            Log.d("MoPub", "Failed to retrieve location: device has no GPS provider.");
        }
        
        Location networkLocation = null;
        try {
            networkLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (SecurityException e) {
            Log.d("MoPub", "Failed to retrieve location: access appears to be disabled.");
        } catch (IllegalArgumentException e) {
            Log.d("MoPub", "Failed to retrieve location: device has no network provider.");
        }
        
        if (gpsLocation == null && networkLocation == null) {
            return null;
        }
        else if (gpsLocation != null && networkLocation != null) {
            if (gpsLocation.getTime() > networkLocation.getTime()) result = gpsLocation;
            else result = networkLocation;
        }
        else if (gpsLocation != null) result = gpsLocation;
        else result = networkLocation;
        
        // Truncate latitude/longitude to the number of digits specified by locationPrecision.
        if (locationAwareness == LocationAwareness.LOCATION_AWARENESS_TRUNCATED) {
            double lat = result.getLatitude();
            double truncatedLat = BigDecimal.valueOf(lat)
                .setScale(locationPrecision, BigDecimal.ROUND_HALF_DOWN)
                .doubleValue();
            result.setLatitude(truncatedLat);
            
            double lon = result.getLongitude();
            double truncatedLon = BigDecimal.valueOf(lon)
                .setScale(locationPrecision, BigDecimal.ROUND_HALF_DOWN)
                .doubleValue();
            result.setLongitude(truncatedLon);
        }
        
        return result;
    }
    
    private String generateAdUrl() {
        StringBuilder sz = new StringBuilder("http://" + MoPubView.HOST + MoPubView.AD_HANDLER);
        sz.append("?v=4&id=" + mAdUnitId);
        
        String udid = Secure.getString(getContext().getContentResolver(), Secure.ANDROID_ID);
        String udidDigest = (udid == null) ? "" : sha1(udid);
        sz.append("&udid=sha:" + udidDigest);

        if (mKeywords != null) {
            sz.append("&q=" + Uri.encode(mKeywords));
        }
        
        if (mLocation != null) {
            sz.append("&ll=" + mLocation.getLatitude() + "," + mLocation.getLongitude());
        }
        
        sz.append("&z=" + getTimeZoneOffsetString());
        
        int orientation = getResources().getConfiguration().orientation;
        String orString = DEVICE_ORIENTATION_UNKNOWN;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            orString = DEVICE_ORIENTATION_PORTRAIT;
        } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            orString = DEVICE_ORIENTATION_LANDSCAPE;
        } else if (orientation == Configuration.ORIENTATION_SQUARE) {
            orString = DEVICE_ORIENTATION_SQUARE;
        }
        sz.append("&o=" + orString);
        
        DisplayMetrics metrics = new DisplayMetrics();
        Activity activity = (Activity) getContext();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        sz.append("&sc_a=" + metrics.density);
      
        return sz.toString();
    }
    
    private String sha1(String s) {
        try { 
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
            
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            return "";
        }
    }
    
    private String getTimeZoneOffsetString() {
        SimpleDateFormat format = new SimpleDateFormat("Z");
        format.setTimeZone(TimeZone.getDefault());
        return format.format(new Date());
    }
    
    /*
     * Overrides the WebView's loadUrl() in order to expose HTTP response headers.
     */
    @Override
    public void loadUrl(String url) {
        if (url.startsWith("javascript:")) {
            super.loadUrl(url);
            return;
        }

        if (mIsLoading) {
            Log.i("MoPub", "Already loading an ad for " + mAdUnitId + ", wait to finish.");
            return;
        }
        
        mUrl = url;
        mIsLoading = true;
        new LoadUrlTask().execute(mUrl);
    }
    
    /*
     * Background operation that loads a URL.
     */
    private class LoadUrlTask extends AsyncTask<String, Void, LoadUrlTaskResult> {
        private Exception error;
        
        protected LoadUrlTaskResult doInBackground(String... urls) {
            LoadUrlTaskResult result = null;
            try {
                result = loadAdFromNetwork(urls[0]);
            } catch(Exception e) {
                this.error = e;
            }
            return result;
        }
        protected void onPostExecute(LoadUrlTaskResult result) {
            if (error != null || result == null) {
                pageFailed();
            } else if (result != null) {
                result.execute();
            }
        }
    }
    
    private abstract interface LoadUrlTaskResult {
        abstract void execute();
    }
    
    private class PerformCustomEventTaskResult implements LoadUrlTaskResult {
        protected Header mHeader;
        
        public PerformCustomEventTaskResult(Header header) {
            mHeader = header;
        }
        
        public void execute() {
            performCustomEventFromHeader(mHeader);
        }
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
    
    private class LoadNativeAdTaskResult implements LoadUrlTaskResult {
        protected HashMap<String, String> mParamsHash;
        
        public LoadNativeAdTaskResult(HashMap<String, String> hash) {
            mParamsHash = hash;
        }
        
        public void execute() {
            mIsLoading = false;
            mMoPubView.loadNativeSDK(mParamsHash);
        }
    }
    
    private class LoadHtmlAdTaskResult implements LoadUrlTaskResult {
        protected String mData;
        
        public LoadHtmlAdTaskResult(String data) {
            mData = data;
        }
        
        public void execute() {
            mResponseString = mData;
            loadDataWithBaseURL("http://"+MoPubView.HOST+"/", 
                mData, "text/html", "utf-8", null);
        }
    }

    private LoadUrlTaskResult loadAdFromNetwork(String url) throws Exception {
        HttpGet httpget = new HttpGet(url);
        httpget.addHeader("User-Agent", mUserAgent);
        
        HttpClient httpclient = getAdViewHttpClient();
        mResponse = httpclient.execute(httpget);
        HttpEntity entity = mResponse.getEntity();
        
        // Anything but a 200 OK is an invalid response.
        if (mResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK || 
        		entity == null || entity.getContentLength() == 0) {
        	throw new Exception("MoPub server returned invalid response.");
        }
        
        // Ensure that the ad type header is valid and not "clear".
        Header atHeader = mResponse.getFirstHeader("X-Adtype");
        if (atHeader == null || atHeader.getValue().equals("clear")) {
            throw new Exception("MoPub server returned no ad.");
        }
        
        configureAdViewUsingHeadersFromHttpResponse(mResponse);
        
        // Handle custom event ad type.
        if (atHeader.getValue().equals("custom")) {
            Log.i("MoPub", "Performing custom event.");
            Header cmHeader = mResponse.getFirstHeader("X-Customselector");
            mIsLoading = false;
            return new PerformCustomEventTaskResult(cmHeader);
        }
        // Handle native SDK ad type.
        else if (!atHeader.getValue().equals("html")) {
            Log.i("MoPub", "Loading native ad");
            Header npHeader = mResponse.getFirstHeader("X-Nativeparams");
            if (npHeader != null) {
                mIsLoading = false;
                HashMap<String, String> paramsHash = new HashMap<String, String>();
                paramsHash.put("X-Adtype", atHeader.getValue());
                paramsHash.put("X-Nativeparams", npHeader.getValue());
                Header ftHeader = mResponse.getFirstHeader("X-Fulladtype");
                if (ftHeader != null) paramsHash.put("X-Fulladtype", ftHeader.getValue());
                return new LoadNativeAdTaskResult(paramsHash);
            }
            else throw new Exception("Could not load native ad; MoPub provided no parameters.");
        }
        
        // Handle HTML ad.
        InputStream is = entity.getContent();
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = is.read(b)) != -1;) {
            out.append(new String(b, 0, n));
        }
        return new LoadHtmlAdTaskResult(out.toString());
    }
    
    private DefaultHttpClient getAdViewHttpClient() {
        HttpParams httpParameters = new BasicHttpParams();

        if (mTimeoutMilliseconds > 0) {
            // Set timeouts to wait for connection establishment / receiving data.
            HttpConnectionParams.setConnectionTimeout(httpParameters, mTimeoutMilliseconds);
            HttpConnectionParams.setSoTimeout(httpParameters, mTimeoutMilliseconds);
        }

        // Set the buffer size to avoid OutOfMemoryError exceptions on certain HTC devices.
        // http://stackoverflow.com/questions/5358014/android-httpclient-oom-on-4g-lte-htc-thunderbolt
        HttpConnectionParams.setSocketBufferSize(httpParameters, 8192);

        return new DefaultHttpClient(httpParameters);
    }
    
    private void configureAdViewUsingHeadersFromHttpResponse(HttpResponse response) {
        // Print the ad network type to the console.
        Header ntHeader = response.getFirstHeader("X-Networktype");
        if (ntHeader != null) Log.i("MoPub", "Fetching ad network type: " + ntHeader.getValue());

        // Set the redirect URL prefix: navigating to any matching URLs will send us to the browser.
        Header rdHeader = response.getFirstHeader("X-Launchpage");
        if (rdHeader != null) mRedirectUrl = rdHeader.getValue();
        else mRedirectUrl = null;

        // Set the URL that is prepended to links for click-tracking purposes.
        Header ctHeader = response.getFirstHeader("X-Clickthrough");
        if (ctHeader != null) mClickthroughUrl = ctHeader.getValue();
        else mClickthroughUrl = null;

        // Set the fall-back URL to be used if the current request fails.
        Header flHeader = response.getFirstHeader("X-Failurl");
        if (flHeader != null) mFailUrl = flHeader.getValue();
        else mFailUrl = null;
        
        // Set the URL to be used for impression tracking.
        Header imHeader = response.getFirstHeader("X-Imptracker");
        if (imHeader != null) mImpressionUrl = imHeader.getValue();
        else mImpressionUrl = null;
        
        // Set the webview's scrollability.
        Header scHeader = response.getFirstHeader("X-Scrollable");
        boolean enabled = false;
        if (scHeader != null) enabled = scHeader.getValue().equals("1");
        setWebViewScrollingEnabled(enabled);

        // Set the width and height.
        Header wHeader = response.getFirstHeader("X-Width");
        Header hHeader = response.getFirstHeader("X-Height");
        if (wHeader != null && hHeader != null) {
            mWidth = Integer.parseInt(wHeader.getValue().trim());
            mHeight = Integer.parseInt(hHeader.getValue().trim());
        }
        else {
            mWidth = 0;
            mHeight = 0;
        }

        // Set the auto-refresh time. A timer will be scheduled upon ad success or failure.
        Header rtHeader = response.getFirstHeader("X-Refreshtime");
        if (rtHeader != null) {
            mRefreshTimeMilliseconds = Long.valueOf(rtHeader.getValue()) * 1000;
            if (mRefreshTimeMilliseconds < MINIMUM_REFRESH_TIME_MILLISECONDS) {
                mRefreshTimeMilliseconds = MINIMUM_REFRESH_TIME_MILLISECONDS;
            }
        }
        else mRefreshTimeMilliseconds = 0;
        
        // Set the allowed orientations for this ad.
        Header orHeader = response.getFirstHeader("X-Orientation");
        mAdOrientation = (orHeader != null) ? orHeader.getValue() : null;
    }
    
    private void setWebViewScrollingEnabled(boolean enabled) {
        if (enabled) {
            setOnTouchListener(null);
        } else {
            setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    return (event.getAction() == MotionEvent.ACTION_MOVE);
                }
            });
        }
    }
    
    /*
     * Stops refreshing ads.
     */
    protected void cleanup() {
        setAutorefreshEnabled(false);
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
        else pageFailed();
    }
    
    protected void loadResponseString(String responseString) {
        loadDataWithBaseURL("http://"+MoPubView.HOST+"/",
                responseString,"text/html","utf-8", null);
    }

    protected void trackImpression() {
        if (mImpressionUrl == null) return;
        
        new Thread(new Runnable() {
            public void run () {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(mImpressionUrl);
                httpget.addHeader("User-Agent", mUserAgent);
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
        if (mClickthroughUrl == null) return;

        new Thread(new Runnable() {
            public void run () {
                DefaultHttpClient httpclient = new DefaultHttpClient();
                HttpGet httpget = new HttpGet(mClickthroughUrl);
                httpget.addHeader("User-Agent", mUserAgent);
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

    private Handler mRefreshHandler = new Handler();
    private Runnable mRefreshRunnable = new Runnable() {
        public void run() {
            loadAd();
        }
    };

    protected void scheduleRefreshTimerIfEnabled() {
        cancelRefreshTimer();
        if (!mAutorefreshEnabled || mRefreshTimeMilliseconds <= 0) return;
        mRefreshHandler.postDelayed(mRefreshRunnable, mRefreshTimeMilliseconds);
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
        mTimeoutMilliseconds = milliseconds;
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
        else scheduleRefreshTimerIfEnabled();
    }
    
    public boolean getAutorefreshEnabled() {
        return mAutorefreshEnabled;
    }
}
