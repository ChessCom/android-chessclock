package com.chess.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import com.chess.R;
import com.chess.adapters.ChessSpinnerAdapter;
import com.chess.core.AppConstants;
import com.chess.core.CoreActivityActionBar;
import com.chess.lcc.android.LccHolder;
import com.chess.utilities.MyProgressDialog;
import com.chess.views.BackgroundChessDrawable;

public class OnlineCreateChallengeActivity extends CoreActivityActionBar implements OnClickListener {
	private Spinner iplayas;
	private Spinner dayspermove;
	private Spinner minrating;
	private Spinner maxrating;
	private CheckBox isRated;
	private RadioButton chess960;


	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();

		setContentView(R.layout.online_create_challenge);
		findViewById(R.id.mainView).setBackgroundDrawable(new BackgroundChessDrawable(this));

		dayspermove = (Spinner) findViewById(R.id.dayspermove);
		chess960 = (RadioButton) findViewById(R.id.chess960);
		iplayas = (Spinner) findViewById(R.id.iplayas);

		minrating = (Spinner) findViewById(R.id.minRating);
		minrating.setAdapter(new ChessSpinnerAdapter(this,R.array.onlineSpinner));
		minrating.setSelection(mainApp.getSharedData().getInt(AppConstants.CHALLENGE_MIN_RATING, 0));

		maxrating = (Spinner) findViewById(R.id.maxRating);
		maxrating.setAdapter(new ChessSpinnerAdapter(this,R.array.onlineSpinner));
		maxrating.setSelection(mainApp.getSharedData().getInt(AppConstants.CHALLENGE_MAX_RATING, 0));

		isRated = (CheckBox) findViewById(R.id.ratedGame);

		findViewById(R.id.createchallenge).setOnClickListener(this);
	}

	private void init(){

	}

	@Override
	public void Update(int code) {
		if (code == 0) {
			mainApp.ShowDialog(this, getString(R.string.congratulations), getString(R.string.onlinegamecreated));
		}
	}


	private Integer[] minRatings = new Integer[]{
			null,
			1000,
			1200,
			1400,
			1600,
			1800,
			2000
	};

	private Integer[] maxRatings = new Integer[]{
			null,
			1000,
			1200,
			1400,
			1600,
			1800,
			2000,
			2200,
			2400
	};

	private int[] daysArr = new int[]{
			1,
			2,
			3,
			5,
			7,
			14		
	};

	@Override
	public void onClick(View view) {
		if(view.getId() == R.id.createchallenge){

			Integer minRating = minRatings[minrating.getSelectedItemPosition()];
			Integer maxRating = maxRatings[maxrating.getSelectedItemPosition()];


			int color = iplayas.getSelectedItemPosition();
			int days = 1;
			days = daysArr[dayspermove.getSelectedItemPosition()];
			int israted = 0;
			int gametype = 0;

			if (isRated.isChecked())
				israted = 1;
			else
				israted = 0;
			if (chess960.isChecked())
				gametype = 2;

			String query = "http://www." + LccHolder.HOST + "/api/echess_new_game?id=" + mainApp.getSharedData().getString(AppConstants.USER_TOKEN, "") +
					"&timepermove=" + days +
					"&iplayas=" + color +
					"&israted=" + israted +
					"&game_type=" + gametype;
			if (minRating != null) query += "&minrating=" + minRating;
			if (maxRating != null) query += "&maxrating=" + maxRating;

			if (appService != null) {
				appService.RunSingleTask(0,
						query,
						progressDialog = new MyProgressDialog(ProgressDialog
								.show(OnlineCreateChallengeActivity.this, null, getString(R.string.creating), true))
				);
			}
		}

	}

}
