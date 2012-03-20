package com.chess.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import com.chess.R;
import com.chess.lcc.android.LccHolder;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.CoreActivityActionBar;
import com.chess.ui.views.BackgroundChessDrawable;
import com.chess.utilities.MyProgressDialog;
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


	private EditText RegUsername;
	private EditText RegEmail;
	private EditText RegPassword;
	private EditText RegRetype;
	private Spinner countrySpinner;
	private Button RegSubmit;
	private int CID = -1;
	private Context context;
	private static String[] COUNTRIES;
	private static String[] COUNTRIES_ID;
	private String[] tmp2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		findViewById(R.id.mainView).setBackgroundDrawable(new BackgroundChessDrawable(this));
		context = this;

		COUNTRIES = getResources().getStringArray(R.array.countries);
		COUNTRIES_ID = getResources().getStringArray(R.array.countries_id);
		RegUsername = (EditText) findViewById(R.id.RegUsername);
		RegEmail = (EditText) findViewById(R.id.RegEmail);
		RegPassword = (EditText) findViewById(R.id.RegPassword);
		RegRetype = (EditText) findViewById(R.id.RegRetype);
		RegSubmit = (Button) findViewById(R.id.RegSubmitBtn);
		countrySpinner = (Spinner) findViewById(R.id.country);


		String[] tmp = COUNTRIES.clone();
		java.util.Arrays.sort(tmp);
		int i = 0, k = 0;
		for (i = 0; i < tmp.length; i++) {
			if (tmp[i].equals("United States")) {
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

//		ArrayAdapter<String> adapterF = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tmp2);
//		adapterF.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//		countrySpinner.setAdapter(adapterF);
		countrySpinner.setAdapter(new ChessSpinnerAdapter(this, tmp2));

		countrySpinner.setOnItemSelectedListener(this);
		RegSubmit.setOnClickListener(this);
	}

	@Override
	public void update(int code) {
		if (code == 0) {
			String query = "http://www." + LccHolder.HOST + "/api/v2/login";
			try {
				if (appService != null) {
					appService.RunSingleTaskPost(1,
							query,
							progressDialog = new MyProgressDialog(
									ProgressDialog.show(context, null, getString(R.string.loading), true)),
							AppConstants.USERNAME, /*URLEncoder.encode(*/RegUsername.getText().toString()/*, "UTF-8")*/,
							"password", /*URLEncoder.encode(*/RegPassword.getText().toString()/*, "UTF-8")*/
					);
				}
			} catch (Exception ignored) { // TODO handle correctly
			}
		} else if (code == 1) {
			FlurryAgent.onEvent("New Account Created", null);
			String[] r = response.split(":");
			mainApp.getSharedDataEditor().putString(AppConstants.USERNAME, RegUsername.getText().toString().toLowerCase());
			mainApp.getSharedDataEditor().putString(AppConstants.PASSWORD, RegPassword.getText().toString());
			mainApp.getSharedDataEditor().putString(AppConstants.USER_PREMIUM_STATUS, r[0].split("[+]")[1]);
			mainApp.getSharedDataEditor().putString(AppConstants.API_VERSION, r[1]);
			try {
				mainApp.getSharedDataEditor().putString(AppConstants.USER_TOKEN, URLEncoder.encode(r[2], "UTF-8"));
			} catch (UnsupportedEncodingException ignored) {
			}
			mainApp.getSharedDataEditor().putString(AppConstants.USER_SESSION_ID, r[3]);
			mainApp.getSharedDataEditor().commit();
			startActivity(new Intent(context, HomeScreenActivity.class));
			finish();
			mainApp.ShowMessage(getString(R.string.congratulations));
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.RegSubmitBtn) {
			if (RegUsername.getText().toString().length() < 3) {
				mainApp.ShowMessage(getString(R.string.wrongusername));
				return;
			}
			if (RegEmail.getText().toString().equals("")) {
				mainApp.ShowMessage(getString(R.string.wrongemail));
				return;
			}
			if (RegPassword.getText().toString().length() < 6) {
				mainApp.ShowMessage(getString(R.string.wrongpassword));
				return;
			}
			if (!RegPassword.getText().toString().equals(RegRetype.getText().toString())) {
				mainApp.ShowMessage(getString(R.string.wrongretype));
				return;
			}
			if (CID == -1) {
				mainApp.ShowMessage(getString(R.string.wrongcountry));
				return;
			}

			String query = "";
			try {
				query = "http://www." + LccHolder.HOST + "/api/register?username=" + URLEncoder.encode(RegUsername.getText().toString(), "UTF-8") + "&password=" + URLEncoder.encode(RegPassword.getText().toString(), "UTF-8")
						+ "&email=" + URLEncoder.encode(RegEmail.getText().toString(), "UTF-8")
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

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
		int i = 0;
		while (i < COUNTRIES.length) {
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
}