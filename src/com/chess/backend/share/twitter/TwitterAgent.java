package com.chess.backend.share.twitter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.chess.R;
import com.chess.ui.activities.PrepareRequestTokenActivity;
import oauth.signpost.OAuth;

/**
 * @author Alexey Schekin (schekin@azoft.com)
 * @created 24.02.12
 * @modified 24.02.12
 */
public class TwitterAgent {

	private final SharedPreferences prefs;
	private final Handler mTwitterHandler = new Handler();
	private final Activity activity;

	private String tweet;

	public TwitterAgent(Activity activity) {
		this.activity = activity;
		this.prefs = PreferenceManager.getDefaultSharedPreferences(activity);
	}

	public void sendTweet(String msg) {
		tweet = msg;
		new AsyncAuthorize().executeTask();
	}

	private class AsyncAuthorize extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {
			return TwitterUtils.isAuthenticated(prefs);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			if (result) {
				new SendAsyncThread().executeTask();
			} else {
				Intent i = new Intent(activity.getApplicationContext(), PrepareRequestTokenActivity.class);
				i.putExtra(ShareConstants.TWEET_MSG, tweet);
				activity.startActivity(i);
			}
		}

		public AsyncTask<Void, Void, Boolean> executeTask(Void... input){
			if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB){
				executeOnExecutor(THREAD_POOL_EXECUTOR, input);
			}else
				execute(input);
			return this;
		}
	}

	private class SendAsyncThread extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				TwitterUtils.sendTweet(prefs, tweet);
				mTwitterHandler.post(mUpdateTwitterNotification);
			} catch (Exception ex) {
                mTwitterHandler.post(mUpdateFailedTwitterNotification);
				ex.printStackTrace();
			}
			return null;
		}

		public AsyncTask<Void, Void, Boolean> executeTask(Void... input){
			if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB){
				executeOnExecutor(THREAD_POOL_EXECUTOR, input);
			}else
				execute(input);
			return this;
		}

    }


	private final Runnable mUpdateTwitterNotification = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(activity, R.string.twitter_post_made , Toast.LENGTH_LONG).show();
		}
	};

	private final Runnable mUpdateFailedTwitterNotification = new Runnable() {
		@Override
		public void run() {
			Toast.makeText(activity, R.string.twitter_post_fail, Toast.LENGTH_LONG).show();
		}
	};

	public void clearTwitterCredentials() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
		final SharedPreferences.Editor edit = prefs.edit();
		edit.remove(OAuth.OAUTH_TOKEN);
		edit.remove(OAuth.OAUTH_TOKEN_SECRET);
		edit.commit();
	}
}
