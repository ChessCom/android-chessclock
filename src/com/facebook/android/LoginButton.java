/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.android;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import com.chess.R;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.SessionEvents.AuthListener;
import com.facebook.android.SessionEvents.LogoutListener;

import java.io.Serializable;

public class LoginButton extends Button implements Serializable{

	private static final long serialVersionUID = 2254525526368290163L;

	private Facebook mFb;
    private Handler mHandler;
    private SessionListener mSessionListener = new SessionListener();
    private String[] mPermissions;
    private FragmentActivity mActivity;
    
    public LoginButton(Context context) {
        super(context);
    }
    
    public LoginButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public LoginButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void init(final FragmentActivity activity, final Facebook fb) {
    	init(activity, fb, new String[] {});
    }
    
    public void init(final FragmentActivity activity, final Facebook fb,
                     final String[] permissions) {
        mActivity = activity;
        mFb = fb;
        mPermissions = permissions;
        mHandler = new Handler();

		setBackgroundResource(fb.isSessionValid() ? R.drawable.button_f_selector
			: R.drawable.button_f_selector);
		setText(getResources().getString(fb.isSessionValid() ? R.string.logout
				: R.string.connect_with_facebook));

		invalidate();

        SessionEvents.addAuthListener(mSessionListener);
        SessionEvents.addLogoutListener(mSessionListener);
        setOnClickListener(new ButtonOnClickListener());
    }
    
    private final class ButtonOnClickListener implements OnClickListener {
        
        @Override
		public void onClick(View arg0) {
            if (mFb.isSessionValid()) {
				logout();
            } else {
                mFb.authorize(mActivity, mPermissions, new LoginDialogListener());
            }
        }
    }

	public void logout(){
		SessionEvents.onLogoutBegin();
		AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(mFb);
		asyncRunner.logout(getContext(), new LogoutRequestListener());
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
    
    private class LogoutRequestListener extends BaseRequestListener {
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
    
    private class SessionListener implements AuthListener, LogoutListener {
        
        @Override
		public void onAuthSucceed() {
			setBackgroundResource(R.drawable.button_f_selector);
			setText(getResources().getString(R.string.logout));
            SessionStore.save(mFb, getContext());
        }

        @Override
		public void onAuthFail(String error) {
        }
        
        @Override
		public void onLogoutBegin() {
        }
        
        @Override
		public void onLogoutFinish() {
            SessionStore.clear(getContext());
			setBackgroundResource(R.drawable.button_f_selector);
			setText(getResources().getString(R.string.connect_with_facebook));
        }
    }
    
}
