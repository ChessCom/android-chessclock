package com.chess.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.chess.R;
import com.chess.backend.share.twitter.ShareConstants;
import com.chess.backend.share.twitter.TwitterUtils;
import com.chess.backend.share.OAuthRequestTokenTask;
import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

/**
 * Prepares a OAuthConsumer and OAuthProvider
 * <p/>
 * OAuthConsumer is configured with the consumer key & consumer secret.
 * OAuthProvider is configured with the 3 OAuth endpoints.
 * <p/>
 * Execute the OAuthRequestTokenTask to retrieve the request, and authorize the request.
 * <p/>
 * After the request is authorized, a callback is made here.
 */
public class PrepareRequestTokenActivity extends Activity {

    private final String TAG = getClass().getName();

    private OAuthConsumer consumer;
    private OAuthProvider provider;
    private final Handler mTwitterHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            this.consumer = new CommonsHttpOAuthConsumer(ShareConstants.CONSUMER_KEY, ShareConstants.CONSUMER_SECRET);
            this.provider = new CommonsHttpOAuthProvider(ShareConstants.REQUEST_URL, ShareConstants.ACCESS_URL, ShareConstants.AUTHORIZE_URL);
        } catch (Exception e) {
            Log.e(TAG, "Error creating consumer / provider", e);
        }

        Log.i(TAG, "Starting task to retrieve request token.");
        new OAuthRequestTokenTask(this, consumer, provider).executeTask();
    }

    /**
     * Called when the OAuthRequestTokenTask finishes (user has authorized the request token).
     * The callback URL will be intercepted here.
     */
    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final Uri uri = intent.getData();
        if (uri != null && uri.getScheme().equals(ShareConstants.OAUTH_CALLBACK_SCHEME)) {
            Log.i(TAG, "Callback received : " + uri);
            Log.i(TAG, "Retrieving Access Token");
            new RetrieveAccessTokenTask(consumer, provider, prefs).executeTask(uri);
            finish();
        }
    }

    public class RetrieveAccessTokenTask extends AsyncTask<Uri, Void, Void> {

        private OAuthProvider provider;
        private OAuthConsumer consumer;
        private SharedPreferences prefs;

        public RetrieveAccessTokenTask(OAuthConsumer consumer, OAuthProvider provider, SharedPreferences prefs) {
            this.consumer = consumer;
            this.provider = provider;
            this.prefs = prefs;
        }


        /**
         * Retrieve the oauth_verifier, and store the oauth and oauth_token_secret
         * for future API calls.
         */
        @Override
        protected Void doInBackground(Uri... params) {
            final Uri uri = params[0];
            final String oauth_verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);

            try {
                provider.retrieveAccessToken(consumer, oauth_verifier);

                final Editor edit = prefs.edit();
                edit.putString(OAuth.OAUTH_TOKEN, consumer.getToken());
                edit.putString(OAuth.OAUTH_TOKEN_SECRET, consumer.getTokenSecret());
                edit.commit();

                String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
                String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");

                consumer.setTokenWithSecret(token, secret);
                finish();
                executeAfterAccessTokenRetrieval();

                Log.i(TAG, "OAuth - Access Token Retrieved");

            } catch (Exception e) {
                Log.e(TAG, "OAuth - Access Token Retrieval Error", e);
            }

            return null;
        }


        private void executeAfterAccessTokenRetrieval() {
            String msg = getIntent().getExtras().getString(ShareConstants.TWEET_MSG);
            try {
                TwitterUtils.sendTweet(prefs, msg);
                mTwitterHandler.post(mUpdateTwitterNotification);
            } catch (Exception e) {
                mTwitterHandler.post(mUpdateFailedTwitterNotification);
                Log.e(TAG, "OAuth - Error sending to Twitter", e);
            }
        }

        private final Runnable mUpdateTwitterNotification = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PrepareRequestTokenActivity.this, R.string.twitter_post_made, Toast.LENGTH_LONG).show();
            }
        };

        private final Runnable mUpdateFailedTwitterNotification = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(PrepareRequestTokenActivity.this, R.string.twitter_post_fail, Toast.LENGTH_LONG).show();
            }
        };

		public AsyncTask<Uri, Void, Void> executeTask(Uri... input){
			if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB){
				executeOnExecutor(THREAD_POOL_EXECUTOR, input);
			}else
				execute(input);
			return this;
		}
    }

}
