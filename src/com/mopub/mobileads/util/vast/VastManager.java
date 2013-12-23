package com.mopub.mobileads.util.vast;

import android.os.AsyncTask;
import com.mopub.mobileads.factories.HttpClientFactory;
import com.mopub.mobileads.util.HttpClients;
import com.mopub.mobileads.util.Strings;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class VastManager {
    static final int MAX_TIMES_TO_FOLLOW_VAST_REDIRECT = 20; // more than reasonable number of nested VAST urls to follow
    static final int VAST_REDIRECT_TIMEOUT_MILLISECONDS = 30 * 1000; // 30 seconds

    private List<String> mImpressionTrackers;
    private List<String> mVideoStartTrackers;
    private List<String> mVideoFirstQuartileTrackers;
    private List<String> mVideoMidpointTrackers;
    private List<String> mVideoThirdQuartileTrackers;
    private List<String> mVideoCompleteTrackers;
    private String mClickThroughUrl;
    private List<String> mClickTrackers;
    private String mMediaFileUrl;

    private int mTimesFollowedVastRedirect;
    private HttpClient mHttpClient;
    private VastManagerListener mListener;

    private ProcessVastBackgroundTask mVastBackgroundTask;

    public VastManager() {
        mImpressionTrackers = new ArrayList<String>();
        mVideoStartTrackers = new ArrayList<String>();
        mVideoFirstQuartileTrackers = new ArrayList<String>();
        mVideoMidpointTrackers = new ArrayList<String>();
        mVideoThirdQuartileTrackers = new ArrayList<String>();
        mVideoCompleteTrackers = new ArrayList<String>();
        mClickTrackers = new ArrayList<String>();

        mHttpClient = HttpClientFactory.create(VAST_REDIRECT_TIMEOUT_MILLISECONDS);
    }

    public void processVast(String vastXml, VastManagerListener listener) {
        if (mVastBackgroundTask == null) {
            mListener = listener;
            mVastBackgroundTask = new ProcessVastBackgroundTask();
            mVastBackgroundTask.execute(vastXml);
        }
    }

    public List<String> getImpressionTrackers() {
        return mImpressionTrackers;
    }

    public List<String> getVideoStartTrackers() {
        return mVideoStartTrackers;
    }

    public List<String> getVideoFirstQuartileTrackers() {
        return mVideoFirstQuartileTrackers;
    }

    public List<String> getVideoMidpointTrackers() {
        return mVideoMidpointTrackers;
    }

    public List<String> getVideoThirdQuartileTrackers() {
        return mVideoThirdQuartileTrackers;
    }

    public List<String> getVideoCompleteTrackers() {
        return mVideoCompleteTrackers;
    }

    public String getClickThroughUrl() {
        return mClickThroughUrl;
    }

    public List<String> getClickTrackers() {
        return mClickTrackers;
    }

    public String getMediaFileUrl() {
        return mMediaFileUrl;
    }

    public void cancel() {
        if (mVastBackgroundTask != null) {
            mVastBackgroundTask.cancel(true);
        }
    }

    private void vastProcessComplete(boolean canceled) {
        HttpClients.safeShutdown(mHttpClient);

        mTimesFollowedVastRedirect = 0;
        mVastBackgroundTask = null;

        if (!canceled) {
            mListener.onComplete(this);
        }
    }

    private void loadVastDataFromXml(VastXmlManager xmlManager) {
        mImpressionTrackers.addAll(xmlManager.getImpressionTrackers());
        mVideoStartTrackers.addAll(xmlManager.getVideoStartTrackers());
        mVideoFirstQuartileTrackers.addAll(xmlManager.getVideoFirstQuartileTrackers());
        mVideoMidpointTrackers.addAll(xmlManager.getVideoMidpointTrackers());
        mVideoThirdQuartileTrackers.addAll(xmlManager.getVideoThirdQuartileTrackers());
        mVideoCompleteTrackers.addAll(xmlManager.getVideoCompleteTrackers());
        mClickTrackers.addAll(xmlManager.getClickTrackers());

        if (mClickThroughUrl == null) {
            mClickThroughUrl = xmlManager.getClickThroughUrl();
        }

        if (mMediaFileUrl == null) {
            mMediaFileUrl = xmlManager.getMediaFileUrl();
        }
    }

    private String processVastFollowingRedirect(String vastXml) throws IOException, SAXException, ParserConfigurationException {
        VastXmlManager xmlManager = new VastXmlManager();
        xmlManager.parseVastXml(vastXml);

        // add relevant vast data from this document
        loadVastDataFromXml(xmlManager);

        String redirectUrl = xmlManager.getVastAdTagURI();
        if (redirectUrl != null && mTimesFollowedVastRedirect < MAX_TIMES_TO_FOLLOW_VAST_REDIRECT) {
            mTimesFollowedVastRedirect++;

            HttpGet httpget = new HttpGet(redirectUrl);
            HttpResponse response = mHttpClient.execute(httpget);
            HttpEntity entity = response.getEntity();
            return (entity != null) ? Strings.fromStream(entity.getContent()) : null;
        }

        return null;
    }

    public interface VastManagerListener {
        public void onComplete(VastManager vastManager);
    }

    private class ProcessVastBackgroundTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {
                if (strings != null && strings.length > 0) {
                    String vastXml = strings[0];

                    while (vastXml != null && vastXml.length() > 0 && !isCancelled()) {
                        vastXml = processVastFollowingRedirect(vastXml);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            vastProcessComplete(false);
        }

        @Override
        protected void onCancelled() {
            vastProcessComplete(true);
        }
    }

    @Deprecated // for testing
    void setTimesFollowedVastRedirect(int timesFollowedVastRedirect) {
        mTimesFollowedVastRedirect = timesFollowedVastRedirect;
    }
}
