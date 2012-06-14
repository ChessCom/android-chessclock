/*
 * AdFetcher.java
 * 
 * Copyright (c) 2012, MoPub Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.Executor;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

/*
 * AdFetcher is a delegate of an AdView that handles loading ad data over a
 * network connection. The ad is fetched in a background thread by executing
 * AdFetchTask, which is an AsyncTask subclass. This class gracefully handles
 * the changes to AsyncTask in Android 4.0.1 (we continue to run parallel to
 * the app developer's background tasks). Further, AdFetcher keeps track of
 * the last completed task to prevent out-of-order execution.
 */
public class AdFetcher {
    private static final int HTTP_CLIENT_TIMEOUT_MILLISECONDS = 10000;
    // This is equivalent to Build.VERSION_CODES.ICE_CREAM_SANDWICH
    private static final int VERSION_CODE_ICE_CREAM_SANDWICH = 14;
    
    private AdView mAdView;
    private AdFetchTask mCurrentTask;
    private String mUserAgent;  
    private long mCurrentTaskId;
    private long mLastCompletedTaskId;
    
    public AdFetcher(AdView adview, String userAgent) {
        mAdView = adview;
        mUserAgent = userAgent;
        mCurrentTaskId = -1;
        mLastCompletedTaskId = -1;
    }
    
    public void fetchAdForUrl(String url) {
        mCurrentTaskId++;
        Log.i("MoPub", "Fetching ad for task #" + mCurrentTaskId);
        
        if (mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
        
        mCurrentTask = new AdFetchTask(this);
        
        if (Build.VERSION.SDK_INT >= VERSION_CODE_ICE_CREAM_SANDWICH) {
            Class<?> cls = AdFetchTask.class;
            Class<?>[] parameterTypes = {Executor.class, Object[].class};
            
            String[] parameters = {url};
            
            try {
                Method method = cls.getMethod("executeOnExecutor", parameterTypes);
                Field field = cls.getField("THREAD_POOL_EXECUTOR");
                method.invoke(mCurrentTask, field.get(cls), parameters);
            } catch (NoSuchMethodException exception) {
                Log.d("MoPub", "Error executing AdFetchTask on ICS+, method not found.");
            } catch (InvocationTargetException exception) {
                Log.d("MoPub", "Error executing AdFetchTask on ICS+, thrown by executeOnExecutor.");
            } catch (Exception exception) {
                Log.d("MoPub", "Error executing AdFetchTask on ICS+: " + exception.toString());
            }
        } else {
            mCurrentTask.execute(url);
        }
    }
    
    public void cancelFetch() {
        if (mCurrentTask != null) {
            Log.i("MoPub", "Canceling fetch ad for task #" + mCurrentTaskId);
            mCurrentTask.cancel(true);
        }
    }
    
    private void markTaskCompleted(long taskId) {
        if (taskId > mLastCompletedTaskId) {
            mLastCompletedTaskId = taskId;
        }
    }
    
    public void cleanup() {
        cancelFetch();
        
        mAdView = null;
        mUserAgent = "";
    }
    
    private static class AdFetchTask extends AsyncTask<String, Void, AdFetchResult> {
        private AdFetcher mAdFetcher;
        private AdView mAdView;
        private Exception mException;
        private HttpClient mHttpClient;
        private long mTaskId;
        private int mTimeoutMilliseconds = HTTP_CLIENT_TIMEOUT_MILLISECONDS;
        
        private AdFetchTask(AdFetcher adFetcher) {
            mAdFetcher = adFetcher;
            
            mAdView = mAdFetcher.mAdView;
            mHttpClient = getDefaultHttpClient();
            mTaskId = mAdFetcher.mCurrentTaskId;
        }

        @Override
        protected AdFetchResult doInBackground(String... urls) {
            AdFetchResult result = null;
            try {
                result = fetch(urls[0]);
            } catch (Exception exception) {
                mException = exception;
            }
            return result;
        }
        
        private AdFetchResult fetch(String url) throws Exception {
            HttpGet httpget = new HttpGet(url);
            httpget.addHeader("User-Agent", mAdFetcher.mUserAgent);
            
            // We check to see if this AsyncTask was cancelled, as per
            // http://developer.android.com/reference/android/os/AsyncTask.html
            if (isCancelled()) {
                return null;
            }

            if (mAdView == null || mAdView.isDestroyed()) {
                Log.d("MoPub", "Error loading ad: AdView has already been GCed or destroyed.");
                return null;
            }

            HttpResponse response = mHttpClient.execute(httpget);
            HttpEntity entity = response.getEntity();

            if (response == null || entity == null ||
                    response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                Log.d("MoPub", "MoPub server returned invalid response.");
                return null;
            }

            mAdView.configureAdViewUsingHeadersFromHttpResponse(response);

            // Ensure that the ad type header is valid and not "clear".
            Header atHeader = response.getFirstHeader("X-Adtype");
            if (atHeader == null || atHeader.getValue().equals("clear")) {
                Log.d("MoPub", "MoPub server returned no ad.");
                return null;
            }

            if (atHeader.getValue().equals("custom")) {
                // Handle custom native ad type.
                Log.i("MoPub", "Performing custom event.");
                Header cmHeader = response.getFirstHeader("X-Customselector");
                return new PerformCustomEventTaskResult(mAdView, cmHeader);
                
            } else if (atHeader.getValue().equals("mraid")) {
                // Handle mraid ad type.
                Log.i("MoPub", "Loading mraid ad");
                HashMap<String, String> paramsHash = new HashMap<String, String>();
                paramsHash.put("X-Adtype", atHeader.getValue());

                String data = httpEntityToString(entity);
                paramsHash.put("X-Nativeparams", data);
                return new LoadNativeAdTaskResult(mAdView, paramsHash);
                
            } else if (!atHeader.getValue().equals("html")) {
                // Handle native SDK ad type.
                Log.i("MoPub", "Loading native ad");

                HashMap<String, String> paramsHash = new HashMap<String, String>();
                paramsHash.put("X-Adtype", atHeader.getValue());

                Header npHeader = response.getFirstHeader("X-Nativeparams");
                paramsHash.put("X-Nativeparams", "{}");
                if (npHeader != null) {
                    paramsHash.put("X-Nativeparams", npHeader.getValue());
                }

                Header ftHeader = response.getFirstHeader("X-Fulladtype");
                if (ftHeader != null) {
                    paramsHash.put("X-Fulladtype", ftHeader.getValue());
                }

                return new LoadNativeAdTaskResult(mAdView, paramsHash);
            }

            // Handle HTML ad.
            String data = httpEntityToString(entity);
            return new LoadHtmlAdTaskResult(mAdView, data);
        }
        
        @Override
        protected void onPostExecute(AdFetchResult result) {
            if (!isMostCurrentTask()) {
                Log.d("MoPub", "Ad response is stale.");
                releaseResources();
                return;
            }
            
            // If cleanup() has already been called on the AdView, don't proceed.
            if (mAdView == null || mAdView.isDestroyed()) {
                if (result != null) {
                    result.cleanup();
                }
                mAdFetcher.markTaskCompleted(mTaskId);
                releaseResources();
                return;
            }
            
            if (result == null) {
                if (mException != null) {
                    Log.d("MoPub", "Exception caught while loading ad: " + mException);
                }
                mAdView.loadFailUrl();
            } else {
                result.execute();
                result.cleanup();
            }
            
            mAdFetcher.markTaskCompleted(mTaskId);
            releaseResources();
        }
        
        @Override
        protected void onCancelled() {
            if (!isMostCurrentTask()) {
                Log.d("MoPub", "Ad response is stale.");
                releaseResources();
                return;
            }
            
            Log.d("MoPub", "Ad loading was cancelled.");
            if (mException != null) {
                Log.d("MoPub", "Exception caught while loading ad: " + mException);
            }
            mAdFetcher.markTaskCompleted(mTaskId);
            releaseResources();
        }
        
        private String httpEntityToString(HttpEntity entity)
                throws IOException {
                
            InputStream inputStream = entity.getContent();
            int numberBytesRead = 0;
            StringBuffer out = new StringBuffer();
            byte[] bytes = new byte[4096];
            
            while (numberBytesRead != -1) {
                out.append(new String(bytes, 0, numberBytesRead));
                numberBytesRead = inputStream.read(bytes);
            }
            
            inputStream.close();
            
            return out.toString();
        }
        
        private void releaseResources() {
            mAdFetcher = null;
            
            if (mHttpClient != null) {
                ClientConnectionManager manager = mHttpClient.getConnectionManager();
                if (manager != null) {
                    manager.shutdown();
                }
                mHttpClient = null;
            }
            
            mException = null;
        }
        
        private DefaultHttpClient getDefaultHttpClient() {
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
        
        private boolean isMostCurrentTask() {
            return mTaskId >= mAdFetcher.mLastCompletedTaskId;
        }
    }

    private static abstract class AdFetchResult {
        WeakReference<AdView> mWeakAdView;
        
        public AdFetchResult(AdView adView) {
            mWeakAdView = new WeakReference<AdView>(adView);
        }
        
        abstract void execute();
        
        /* The AsyncTask thread pool often appears to keep references to these
         * objects, preventing GC. This method should be used to release
         * resources to mitigate the GC issue.
         */
        abstract void cleanup();
    }
    
    private static class PerformCustomEventTaskResult extends AdFetchResult {
        protected Header mHeader;
        
        public PerformCustomEventTaskResult(AdView adView, Header header) {
            super(adView);
            mHeader = header;
        }
        
        public void execute() {
            AdView adView = mWeakAdView.get();
            if (adView == null || adView.isDestroyed()) {
                return;
            }
            
            adView.setIsLoading(false);
            MoPubView mpv = adView.mMoPubView;
            
            if (mHeader == null) {
                Log.i("MoPub", "Couldn't call custom method because the server did not specify one.");
                mpv.adFailed();
                return;
            }
            
            String methodName = mHeader.getValue();
            Log.i("MoPub", "Trying to call method named " + methodName);
            
            Class<? extends Activity> c;
            Method method;
            Activity userActivity = mpv.getActivity();
            try {
                c = userActivity.getClass();
                method = c.getMethod(methodName, MoPubView.class);
                method.invoke(userActivity, mpv);
            } catch (NoSuchMethodException e) {
                Log.d("MoPub", "Couldn't perform custom method named " + methodName +
                        "(MoPubView view) because your activity class has no such method");
                return;
            } catch (Exception e) {
                Log.d("MoPub", "Couldn't perform custom method named " + methodName);
                return;
            }
        }
        
        public void cleanup() {
            mHeader = null;
        }
    }
    
    private static class LoadNativeAdTaskResult extends AdFetchResult {
        protected HashMap<String, String> mParamsHash;
        
        private LoadNativeAdTaskResult(AdView adView, HashMap<String, String> hash) {
            super(adView);
            mParamsHash = hash;
        }
        
        public void execute() {
            AdView adView = mWeakAdView.get();
            if (adView == null || adView.isDestroyed()) {
                return;
            }
            
            adView.setIsLoading(false);
            MoPubView mpv = adView.mMoPubView;
            mpv.loadNativeSDK(mParamsHash);
        }

        public void cleanup() {
            mParamsHash = null;
        }
    }
    
    private static class LoadHtmlAdTaskResult extends AdFetchResult {
        protected String mData;
        
        private LoadHtmlAdTaskResult(AdView adView, String data) {
            super(adView);
            mData = data;
        }
        
        public void execute() {
            AdView adView = mWeakAdView.get();
            if (adView == null || adView.isDestroyed()) {
                return;
            }
            
            if (mData == null) {
                return;
            }
            
            adView.setResponseString(mData);
            adView.loadDataWithBaseURL("http://" + MoPubView.HOST + "/", mData,
                    "text/html", "utf-8", null);
        }
        
        public void cleanup() {
            mData = null;
        }
    }
}