package com.chess.activities.tabs;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioButton;
import android.widget.Spinner;
import com.chess.R;
import com.chess.activities.Game;
import com.chess.core.AppConstants;
import com.chess.core.CoreActivity;
import com.flurry.android.FlurryAgent;

public class Computer extends CoreActivity implements OnClickListener {

	private Spinner strength;
	private LogoutTask logoutTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.computer);
		
		logoutTask = new LogoutTask();

		strength = (Spinner) findViewById(R.id.PrefStrength);
		strength.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> a, View v, int pos, long id) {
				try {
					if (mainApp.getSharedDataEditor() != null && mainApp.getSharedData() != null && pos >= 0) {
						mainApp.getSharedDataEditor().putInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, pos);
						mainApp.getSharedDataEditor().commit();
					}
				} catch (Exception e) {
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> a) {
			}
		});

		findViewById(R.id.start).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				LoadNext(0);
			}
		});
	}

	@Override
	public void onClick(View view) { // make code more clear
		if(view.getId() == R.id.load){
			FlurryAgent.onEvent("New Game VS Computer", null);
			startActivity(new Intent(Computer.this, Game.class).putExtra(AppConstants.GAME_MODE, Integer.parseInt(mainApp.getSharedData().getString(AppConstants.SAVED_COMPUTER_GAME, "").substring(0, 1))));
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
		if (mainApp.isLiveChess()) {
			mainApp.setLiveChess(false);
			logoutTask.execute();  // do not create new instance every time
		}
		super.onResume();
		
		if (strength != null && mainApp != null && mainApp.getSharedData() != null) {
			strength.post(new Runnable() {
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

	@Override
	public void LoadNext(int code) {
		RadioButton wh, bh;
		wh = (RadioButton) findViewById(R.id.wHuman);
		bh = (RadioButton) findViewById(R.id.bHuman);

		int mode = 0;
		if (!wh.isChecked() && bh.isChecked())
			mode = 1;
		else if (wh.isChecked() && bh.isChecked())
			mode = 2;
		else if (!wh.isChecked() && !bh.isChecked())
			mode = 3;

		mainApp.getSharedDataEditor().putString(AppConstants.SAVED_COMPUTER_GAME, "");
		mainApp.getSharedDataEditor().commit();

		FlurryAgent.onEvent("New Game VS Computer", null);
		startActivity(new Intent(this, Game.class).putExtra(AppConstants.GAME_MODE, mode));
	}

	@Override
	public void LoadPrev(int code) {
		//finish();
		mainApp.getTabHost().setCurrentTab(0);
	}

	@Override
	public void Update(int code) {
	}
}
