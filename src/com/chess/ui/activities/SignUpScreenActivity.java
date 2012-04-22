package com.chess.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import com.chess.R;
import com.chess.backend.StatusHelper;
import com.chess.backend.Web;
import com.chess.backend.statics.FlurryData;
import com.chess.lcc.android.LccHolder;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.CoreActivityActionBar;
import com.chess.utilities.MyProgressDialog;
import com.facebook.android.Facebook;
import com.facebook.android.LoginButton;
import com.facebook.android.SessionEvents;
import com.facebook.android.SessionStore;
import com.flurry.android.FlurryAgent;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * SignUpScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:34
 */
public class SignUpScreenActivity extends CoreActivityActionBar implements View.OnClickListener, AdapterView.OnItemSelectedListener {


	private EditText userNameEdt;
	private EditText emailEdt;
	private EditText passwordEdt;
	private EditText regRetypeEdt;
	private Button regSubmit;
	private int CID = -1;
	private Context context;
	private static String[] COUNTRIES;
	private static String[] COUNTRIES_ID;
	private String[] tmp2;

    private Facebook facebook;
    private LoginButton facebookLoginButton;
    private static int SIGNIN_CALLBACK_CODE = 16;
    private static int SIGNIN_FACEBOOK_CALLBACK_CODE = 128;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register_screen);

		context = this;

		COUNTRIES = getResources().getStringArray(R.array.countries);
		COUNTRIES_ID = getResources().getStringArray(R.array.countries_id);
		
		userNameEdt = (EditText) findViewById(R.id.RegUsername);
		emailEdt = (EditText) findViewById(R.id.RegEmail);
		passwordEdt = (EditText) findViewById(R.id.RegPassword);
		regRetypeEdt = (EditText) findViewById(R.id.RegRetype);
		regSubmit = (Button) findViewById(R.id.RegSubmitBtn);

		userNameEdt.addTextChangedListener(new FieldChangeWatcher(userNameEdt));
		emailEdt.addTextChangedListener(new FieldChangeWatcher(emailEdt));
		passwordEdt.addTextChangedListener(new FieldChangeWatcher(passwordEdt));
		regRetypeEdt.addTextChangedListener(new FieldChangeWatcher(regRetypeEdt));
		

		getCountryCode();
		Spinner countrySpinner = (Spinner) findViewById(R.id.country);
		countrySpinner.setAdapter(new ChessSpinnerAdapter(this, tmp2));
		countrySpinner.setOnItemSelectedListener(this);
		
		regSubmit.setOnClickListener(this);

        facebookLoginButton = (LoginButton) findViewById(R.id.fb_connect);

        facebook = new Facebook(AppConstants.FACEBOOK_APP_ID);
        SessionStore.restore(facebook, this);
        SessionEvents.addAuthListener(new SampleAuthListener());
        SessionEvents.addLogoutListener(new SampleLogoutListener());
		
        facebookLoginButton.init(this, facebook);
	}

	private void getCountryCode(){
		String[] tmp = COUNTRIES.clone();
		java.util.Arrays.sort(tmp);
		int i = 0, k = 0;
		for (i = 0; i < tmp.length; i++) {
			if (tmp[i].equals(getString(R.string.united_states))) {
				k = i;
				break;
			}
		}
		tmp2 = new String[tmp.length];
		tmp2[0] = tmp[k];
		for (i = 0; i < tmp2.length; i++) {
			if (i < k) {
				tmp2[i + 1] = tmp[i];
			} else if (i > k) {
				tmp2[i] = tmp[i];
			}
		}
	}
	
	@Override
	public void update(int code) {
		if (code == 0) {
			String query = "http://www." + LccHolder.HOST + AppConstants.API_V2_LOGIN;
			try {
				if (appService != null) {
					appService.RunSingleTaskPost(1,
                            query,
                            progressDialog = new MyProgressDialog(
                                    ProgressDialog.show(context, null, getString(R.string.loading), true)),
                            AppConstants.USERNAME, userNameEdt.getText().toString(),
                            AppConstants.PASSWORD, passwordEdt.getText().toString()
                    );
				}
			} catch (Exception ignored) { // TODO handle correctly
			}
		} else if (code == 1) {
			FlurryAgent.onEvent("New Account Created", null);  // TODO
			String[] r = response.split(":");
			mainApp.getSharedDataEditor().putString(AppConstants.USERNAME, userNameEdt.getText().toString().toLowerCase());
			mainApp.getSharedDataEditor().putString(AppConstants.PASSWORD, passwordEdt.getText().toString());
			mainApp.getSharedDataEditor().putString(AppConstants.USER_PREMIUM_STATUS, r[0].split("[+]")[1]);
			mainApp.getSharedDataEditor().putString(AppConstants.API_VERSION, r[1]);
			try {
				mainApp.getSharedDataEditor().putString(AppConstants.USER_TOKEN, URLEncoder.encode(r[2], AppConstants.UTF_8));
			} catch (UnsupportedEncodingException ignored) {
			}
			mainApp.getSharedDataEditor().putString(AppConstants.USER_SESSION_ID, r[3]);
			mainApp.getSharedDataEditor().commit();

			startActivity(new Intent(context, HomeScreenActivity.class));
			finish();

			showToast(R.string.congratulations);
		}
        if (response.length() > 0) {
            final String[] responseArray = response.split(":");
            if (responseArray.length >= 4) {
                if (code == SIGNIN_CALLBACK_CODE) {
                    mainApp.getSharedDataEditor().putString(AppConstants.USERNAME, userNameEdt.getText().toString().trim().toLowerCase());
                    doUpdate(responseArray);
                } else if (code == SIGNIN_FACEBOOK_CALLBACK_CODE && responseArray.length >= 5) {
                    FlurryAgent.onEvent(FlurryData.FB_LOGIN, null);
                    mainApp.getSharedDataEditor().putString(AppConstants.USERNAME, responseArray[4].trim().toLowerCase());
                    doUpdate(responseArray);
                }
            }
        }

	}

	@Override
	protected void onResume() {
		super.onResume();
		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.RegSubmitBtn) {
			if (userNameEdt.getText().toString().length() < 3) {
				userNameEdt.setError(getString(R.string.too_short));
				userNameEdt.requestFocus();
				showToast(getString(R.string.wrongusername));
				return;
			}
			if (emailEdt.getText().toString().equals(AppConstants.SYMBOL_EMPTY)) {
				emailEdt.setError(getString(R.string.can_not_be_empty));
				emailEdt.requestFocus();
				showToast(getString(R.string.wrongemail));
				return;
			}
			if (passwordEdt.getText().toString().length() < 6) {
				passwordEdt.setError(getString(R.string.too_short));
				passwordEdt.requestFocus();
				showToast(getString(R.string.wrongpassword));
				return;
			}
			if (!passwordEdt.getText().toString().equals(regRetypeEdt.getText().toString())) {
				passwordEdt.setError(getString(R.string.pass_dont_match));
				passwordEdt.requestFocus();
				showToast(getString(R.string.wrongretype));
				return;
			}
			if (CID == -1) {
				showToast(getString(R.string.wrongcountry));
				return;
			}

			String query = AppConstants.SYMBOL_EMPTY;
			try {
				query = "http://www." + LccHolder.HOST
                        + "/api/register?username=" + URLEncoder.encode(userNameEdt.getText().toString(), AppConstants.UTF_8)
                        + "&password=" + URLEncoder.encode(passwordEdt.getText().toString(), AppConstants.UTF_8)
						+ "&email=" + URLEncoder.encode(emailEdt.getText().toString(), AppConstants.UTF_8)
						+ "&country_id=" + CID + "&app_type=android";
			} catch (Exception e) {   // TODO handle correctly
			}

			if (appService != null) {
				appService.RunSingleTask(0,
						query,
						progressDialog = new MyProgressDialog(ProgressDialog.show(context, null, getString(R.string.loading), true))
				);
			}
		}
	}

	private class FieldChangeWatcher implements TextWatcher {
		private EditText editText;

		public FieldChangeWatcher(EditText editText) {
			this.editText = editText;
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s.length() > 1) {
				editText.setError(null);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
		int i = 0;
		while (i < COUNTRIES.length) {   // TODO use predefined strings
			if (COUNTRIES[i].equals(tmp2[pos])) {
				break;
			}
			i++;
		}
		CID = Integer.parseInt(COUNTRIES_ID[i]);
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {

	}

    private void doUpdate(String[] response) {
        mainApp.getSharedDataEditor().putString(AppConstants.PASSWORD, passwordEdt.getText().toString().trim());
        mainApp.getSharedDataEditor().putString(AppConstants.USER_PREMIUM_STATUS, response[0].split("[+]")[1]);
        mainApp.getSharedDataEditor().putString(AppConstants.API_VERSION, response[1]);
        try {
            mainApp.getSharedDataEditor().putString(AppConstants.USER_TOKEN, URLEncoder.encode(response[2], AppConstants.UTF_8));
        } catch (UnsupportedEncodingException ignored) {
        }
        mainApp.getSharedDataEditor().putString(AppConstants.USER_SESSION_ID, response[3]);
        mainApp.getSharedDataEditor().commit();

        FlurryAgent.onEvent("Logged In");
        if (mainApp.getSharedData().getBoolean(mainApp.getUserName() + AppConstants.PREF_NOTIFICATION, true)) {
//			startService(new Intent(this, Notifications.class));
			StatusHelper.checkStatusUpdate(mainApp, context);
		}
        mainApp.guest = false;


		backToHomeActivity();
    }


    public class SampleAuthListener implements SessionEvents.AuthListener {
        @Override
        public void onAuthSucceed() {
            String query = "http://www." + LccHolder.HOST + "/api/v2/login?facebook_access_token="
                    + facebook.getAccessToken() + "&return=username";
            response = Web.Request(query, "GET", null, null);

            if (response.contains("Success+")) {
                update(SIGNIN_FACEBOOK_CALLBACK_CODE);
            } else if (response.contains("Error+Facebook user has no Chess.com account")) {
                showToast(R.string.no_chess_account_signup_please);
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www." + LccHolder.HOST
                        + "/register.html")));
            }
        }

        @Override
        public void onAuthFail(String error) {
            showToast(getString(R.string.login_failed) + AppConstants.SYMBOL_SPACE + error);
        }
    }

    public class SampleLogoutListener implements SessionEvents.LogoutListener {
        @Override
        public void onLogoutBegin() {
            showToast(R.string.loggin_out);
        }

        @Override
        public void onLogoutFinish() {
            showToast(R.string.you_logged_out);
        }
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		facebook.authorizeCallback(requestCode, resultCode, data);
	}

}