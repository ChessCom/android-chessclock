package com.chess.backend.share.facebook;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.facebook.android.*;
import com.facebook.android.Facebook.DialogListener;

import java.io.IOException;

public class FacebookConnector {

	private static final String FACEBOOK_PERMISSION = "publish_stream";

	private Facebook facebook = null;
	private Context context;
	private String[] permissions;
	private Handler mHandler;
	private FragmentActivity activity;

    public FacebookConnector(FragmentActivity activity) {
		facebook = new Facebook(AppConstants.FACEBOOK_APP_ID);

		context = activity;
		SessionStore.restore(facebook, context);
        SessionListener mSessionListener = new SessionListener();
        SessionEvents.addAuthListener(mSessionListener);
        SessionEvents.addLogoutListener(mSessionListener);

		permissions = new String[] {FACEBOOK_PERMISSION};
		mHandler = new Handler();
		this.activity=activity;
	}

	void login() {
        if (!facebook.isSessionValid()) {
            facebook.authorize(activity, permissions,new LoginDialogListener());
        }
    }

	public void logout() {
        SessionEvents.onLogoutBegin();
        AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(this.facebook);
        asyncRunner.logout(this.context, new LogoutRequestListener());
	}

	public void postMessageOnWall(Bundle params) {
		if (facebook.isSessionValid()) {
		    try {
				String response = facebook.request("me/feed", params, "POST");
				System.out.println(response);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			facebook.dialog(context, "feed", params, new UpdateStatusListener());
//			login();
		}
	}

    public void updateStatus(Bundle params) {
        facebook.dialog(context, "feed", params, new UpdateStatusListener());
    }

    private final class LoginDialogListener implements DialogListener {
        @Override
		public void onComplete(Bundle values) {
            SessionEvents.onLoginSuccess();
        }

        @Override
		public void onFacebookError(FacebookError error) {
            SessionEvents.onLoginError(error.getMessage());
        }

        @Override
		public void onError(DialogError error) {
            SessionEvents.onLoginError(error.getMessage());
        }

        @Override
		public void onCancel() {
            SessionEvents.onLoginError("Action Canceled");
        }
    }

    public class LogoutRequestListener extends com.chess.backend.share.facebook.BaseRequestListener {
        @Override
		public void onComplete(String response, final Object state) {
            // callback should be run in the original thread,
            // not the background thread
            mHandler.post(new Runnable() {
                @Override
				public void run() {
                    SessionEvents.onLogoutFinish();
                }
            });
        }
    }

    /*
     * callback for the feed dialog which updates the profile status
     */
    public class UpdateStatusListener extends com.chess.backend.share.facebook.BaseDialogListener {
        @Override
        public void onComplete(Bundle values) {
            final String postId = values.getString("post_id");
            if (postId != null) {
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.facebook_post_made), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context.getApplicationContext(), context.getString(R.string.facebook_post_fail), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onFacebookError(FacebookError error) {
            Toast.makeText(context.getApplicationContext(), "Facebook Error: " + error.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel() {
            Toast toast = Toast.makeText(context.getApplicationContext(), "Update status cancelled",
                    Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private class SessionListener implements com.facebook.android.SessionEvents.AuthListener, com.facebook.android.SessionEvents.LogoutListener {

        @Override
		public void onAuthSucceed() {
            SessionStore.save(facebook, context);
        }

        @Override
		public void onAuthFail(String error) {
        }

        @Override
		public void onLogoutBegin() {
        }

        @Override
		public void onLogoutFinish() {
            SessionStore.clear(context);
        }
    }

	public Facebook getFacebook() {
		return this.facebook;
	}
}
