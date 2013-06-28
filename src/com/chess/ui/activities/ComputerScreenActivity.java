package com.chess.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RadioButton;
import android.widget.Spinner;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.chess.ui.engine.ChessBoardComp;
import com.flurry.android.FlurryAgent;

/**
 * ComputerScreenActivity class
 *
 * @author alien_roger
 * @created at: 08.02.12 7:21
 */
public class ComputerScreenActivity extends LiveBaseActivity implements AdapterView.OnItemSelectedListener {

	private Spinner strength;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.computer_screen);

		findViewById(R.id.load).setOnClickListener(this);

		strength = (Spinner) findViewById(R.id.strengthSpinner);
		strength.setAdapter(new ChessSpinnerAdapter(this, getItemsFromEntries(R.array.strength)));
		strength.setOnItemSelectedListener(this);

		findViewById(R.id.start).setOnClickListener(this);

		//new StartEngineTask(getApplicationContext(), new InitComputerEngineUpdateListener()).executeTask();
	}

	@Override
	protected void onResume() {
		super.onResume();
		strength.setSelection(AppData.getCompStrength(this));

		if (AppData.haveSavedCompGame(this)) {
			findViewById(R.id.load).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.load).setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View view) { // make code more clear
		if (view.getId() == R.id.load) {
			FlurryAgent.logEvent(FlurryData.NEW_GAME_VS_COMPUTER, null);
			Intent intent = new Intent(this, GameCompScreenActivity.class);
			int compGameId = Integer.parseInt(AppData.getCompSavedGame(this).substring(0, 1));
			intent.putExtra(AppConstants.GAME_MODE, compGameId);
			startActivity(intent);
		} else if (view.getId() == R.id.start) {
			RadioButton whiteHuman, blackHuman;
			whiteHuman = (RadioButton) findViewById(R.id.wHuman);
			blackHuman = (RadioButton) findViewById(R.id.bHuman);

			int mode = AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE;
			if (!whiteHuman.isChecked() && blackHuman.isChecked())
				mode = AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK;
			else if (whiteHuman.isChecked() && blackHuman.isChecked())
				mode = AppConstants.GAME_MODE_HUMAN_VS_HUMAN;
			else if (!whiteHuman.isChecked() && !blackHuman.isChecked())
				mode = AppConstants.GAME_MODE_COMPUTER_VS_COMPUTER;

			ChessBoardComp.resetInstance();
			preferencesEditor.putString(AppData.getUserName(this) + AppConstants.SAVED_COMPUTER_GAME, StaticData.SYMBOL_EMPTY);
			preferencesEditor.commit();

			FlurryAgent.logEvent(FlurryData.NEW_GAME_VS_COMPUTER, null);
			Intent intent = new Intent(this, GameCompScreenActivity.class);
			intent.putExtra(AppConstants.GAME_MODE, mode);
			startActivity(intent);
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
		preferencesEditor.putInt(AppData.getUserName(getContext()) + AppConstants.PREF_COMPUTER_STRENGTH, pos);
		preferencesEditor.commit();
	}

	@Override
	public void onNothingSelected(AdapterView<?> adapterView) {
	}
}