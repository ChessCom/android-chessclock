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

import android.os.AsyncTask;
import android.util.Log;
import com.mopub.mobileads.factories.HttpClientFactory;
import com.mopub.mobileads.util.HttpClients;
import com.mopub.mobileads.util.Streams;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.*;

public class VastVideoDownloadTask extends AsyncTask<String, Void, Boolean> {
    private static final String TEMP_FILE_PREFIX = "mopub-vast";
    private static final long MAX_TEMP_FILE_LENGTH = 25 * 1000 * 1000;
    private static final int HTTP_CLIENT_TIMEOUT = 10 * 1000;
    private final DefaultHttpClient mHttpClient;

    public interface OnDownloadCompleteListener {
        public void onDownloadSuccess();
        public void onDownloadFailed();
    }

    private final DiskLruCache mDiskLruCache;
    private final OnDownloadCompleteListener mOnDownloadCompleteListener;

    public VastVideoDownloadTask(OnDownloadCompleteListener listener, DiskLruCache diskLruCache) {
        mOnDownloadCompleteListener = listener;
        mDiskLruCache = diskLruCache;
        mHttpClient = HttpClientFactory.create(HTTP_CLIENT_TIMEOUT);
    }

    @Override
    protected Boolean doInBackground(String... params) {
        if (params == null || params[0] == null) {
            return false;
        }

        return downloadToCache(params[0]);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            if (mOnDownloadCompleteListener != null) {
                mOnDownloadCompleteListener.onDownloadSuccess();
            }
        } else {
            if (mOnDownloadCompleteListener != null) {
                mOnDownloadCompleteListener.onDownloadFailed();
            }
        }
    }

    Boolean downloadToCache(String videoUrl) {
        boolean savedSuccessfully = false;

        try {
            InputStream inputStream = connectToUrl(videoUrl);
            File tempFile = copyInputStreamToTempFile(inputStream);
            savedSuccessfully = copyTempFileIntoCache(videoUrl, tempFile);
            tempFile.delete();
        } catch (Exception e) {
            Log.d("MoPub", "Failed to download video.");
        } finally {
            HttpClients.safeShutdown(mHttpClient);
        }

        return savedSuccessfully;
    }

    InputStream connectToUrl(String videoUrl) throws IOException {
        if (videoUrl == null) {
            throw new IOException("Unable to connect to null url.");
        }

        HttpGet httpget = new HttpGet(videoUrl);
        HttpResponse response = mHttpClient.execute(httpget);

        if (response == null || response.getEntity() == null) {
            throw new IOException("Obtained null response from video url: " + videoUrl);
        }

        return response.getEntity().getContent();
    }

    File copyInputStreamToTempFile(InputStream inputStream) throws IOException {
        File tempFile = File.createTempFile(TEMP_FILE_PREFIX, null, mDiskLruCache.getCacheDirectory());
        OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFile));

        try {
            Streams.copyContent(inputStream, outputStream, MAX_TEMP_FILE_LENGTH);
        } catch (IOException exception) {
            tempFile.delete();
            throw exception;
        } finally {
            Streams.closeStream(inputStream);
            Streams.closeStream(outputStream);
        }

        return tempFile;
    }

    boolean copyTempFileIntoCache(String videoUrl, File tempFile) throws FileNotFoundException {
        InputStream temporaryVideoStream = new BufferedInputStream(new FileInputStream(tempFile));
        boolean savedSuccessfully = mDiskLruCache.putStream(videoUrl, temporaryVideoStream);
        Streams.closeStream(temporaryVideoStream);
        return savedSuccessfully;
    }
}
