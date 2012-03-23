package com.chess.ui.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.Spinner;
import com.chess.R;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.CoreActivityActionBar;
import com.chess.ui.views.BackgroundChessDrawable;
import com.flurry.android.FlurryAgent;

/**
 * ComputerScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:21
 */
public class ComputerScreenActivity extends CoreActivityActionBar implements View.OnClickListener {

	private Spinner strength;
	private LogoutTask logoutTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.computer_screen);
		findViewById(R.id.mainView).setBackgroundDrawable(new BackgroundChessDrawable(this));

		logoutTask = new LogoutTask();

		strength = (Spinner) findViewById(R.id.PrefStrength);
		strength.setAdapter(new ChessSpinnerAdapter(this, R.array.strength));
		strength.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
				try {
					if (mainApp.getSharedDataEditor() != null && mainApp.getSharedData() != null && pos >= 0) {
						mainApp.getSharedDataEditor().putInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, pos);
						mainApp.getSharedDataEditor().commit();
					}
				} catch (Exception ignored) {
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> a) {
			}
		});

		findViewById(R.id.start).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) { // make code more clear
		if (view.getId() == R.id.load) {
			FlurryAgent.onEvent("New Game VS Computer", null);

			startActivity(new Intent(coreContext, GameCompScreenActivity.class)
					.putExtra(AppConstants.GAME_MODE,
							Integer.parseInt(mainApp.getSharedData()
									.getString(AppConstants.SAVED_COMPUTER_GAME, "").substring(0, 1))));
		} else if (view.getId() == R.id.start) {
			RadioButton whiteHuman, blackHuman;
			whiteHuman = (RadioButton) findViewById(R.id.wHuman);
			blackHuman = (RadioButton) findViewById(R.id.bHuman);

			// TODO replace with bit shifts
			// TODO init mode on changes
			int mode = AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE;
			if (!whiteHuman.isChecked() && blackHuman.isChecked())
				mode = AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK;
			else if (whiteHuman.isChecked() && blackHuman.isChecked())
				mode = AppConstants.GAME_MODE_HUMAN_VS_HUMAN;
			else if (!whiteHuman.isChecked() && !blackHuman.isChecked())
				mode = AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER;

			mainApp.getSharedDataEditor().putString(AppConstants.SAVED_COMPUTER_GAME, "");
			mainApp.getSharedDataEditor().commit();

			FlurryAgent.onEvent("New Game VS Computer", null);
//			startActivity(new Intent(this, Game.class).putExtra(AppConstants.GAME_MODE, mode));
			startActivity(new Intent(this, GameCompScreenActivity.class).putExtra(AppConstants.GAME_MODE, mode));
		}
	}

	private class LogoutTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			mainApp.getLccHolder().logout();
			return null;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (strength != null && mainApp != null && mainApp.getSharedData() != null) {
			strength.post(new Runnable() {
				@Override
				public void run() {
					strength.setSelection(mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, 0));
				}
			});

			if (!mainApp.getSharedData().getString(AppConstants.SAVED_COMPUTER_GAME, "").equals("")) {
				findViewById(R.id.load).setVisibility(View.VISIBLE);
				findViewById(R.id.load).setOnClickListener(this);
			} else {
				findViewById(R.id.load).setVisibility(View.GONE);
			}
		}
	}


//	@Override
//	public void LoadPrev(int code) {
//		//finish();
//		mainApp.getTabHost().setCurrentTab(0);
//	}

	@Override
	public void update(int code) {
	}
}