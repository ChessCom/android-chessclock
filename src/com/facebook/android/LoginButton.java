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
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import com.chess.R;
import com.chess.core.MainApp;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.SessionEvents.AuthListener;
import com.facebook.android.SessionEvents.LogoutListener;

public class LoginButton extends /* Image */Button {

	private Facebook mFb;
	private Handler mHandler;
	private final SessionListener mSessionListener = new SessionListener();
	private String[] mPermissions;

	public LoginButton(Context context) {
		super(context);
	}

	public LoginButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public LoginButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void init(final Facebook fb, final String[] permissions) {
		mFb = fb;
		mPermissions = permissions;
		mHandler = new Handler();

//		setBackgroundColor(Color.TRANSPARENT);
//		setAdjustViewBounds(true);
//		setImageResource(fb.isSessionValid() ? R.drawable.logout_button
//				: R.drawable.login_button_selector);
		setBackgroundResource(fb.isSessionValid() ? R.drawable.button_f_logout_selector
				: R.drawable.button_f_login_selector);
		setText(getResources().getString(
				fb.isSessionValid() ? R.string.logout
						: R.string.connect_with_facebook));

		invalidate();
//		drawableStateChanged();

		SessionEvents.addAuthListener(mSessionListener);
		SessionEvents.addLogoutListener(mSessionListener);
		setOnClickListener(new ButtonOnClickListener());
	}

	private final class ButtonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View arg0) {
			if (mFb.isSessionValid()) {
				SessionEvents.onLogoutBegin();
				AsyncFacebookRunner asyncRunner = new AsyncFacebookRunner(mFb);
				asyncRunner.logout(getContext(), new LogoutRequestListener());
			} else {
				mFb.authorize(getContext(), MainApp.APP_ID, mPermissions,
						new LoginDialogListener());
			}
		}
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
		public void onComplete(String response) {
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
			setBackgroundResource(R.drawable.button_f_logout_selector);// ImageResource(R.drawable.button_f_logout_selector);
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
			setBackgroundResource(R.drawable.button_f_login_selector);
			setText(getResources().getString(R.string.connect_with_facebook));

//			invalidate();
//			setImageResource(R.drawable.login_button_selector);
		}
	}

}
