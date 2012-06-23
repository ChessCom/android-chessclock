package com.chess.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.interfaces.ChessUpdateListener2;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.ui.adapters.ChessSpinnerAdapter;

public class OnlineCreateChallengeActivity2 extends LiveBaseActivity2 implements OnClickListener {
	private Spinner iplayas;
	private Spinner daysPerMoveSpinner;
	private Spinner minrating;
	private Spinner maxrating;
	private CheckBox isRated;
	private RadioButton chess960;
	private CreateChallengeUpdateListener createChallengeUpdateListener;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.online_create_challenge);


		daysPerMoveSpinner = (Spinner) findViewById(R.id.dayspermove);
		daysPerMoveSpinner.setAdapter(new ChessSpinnerAdapter(this, R.array.dayspermove));

		chess960 = (RadioButton) findViewById(R.id.chess960);
		iplayas = (Spinner) findViewById(R.id.iplayas);
		iplayas.setAdapter(new ChessSpinnerAdapter(this, R.array.playas));

		minrating = (Spinner) findViewById(R.id.minRating);
		minrating.setAdapter(new ChessSpinnerAdapter(this, R.array.minRating));
		minrating.setSelection(preferences.getInt(AppConstants.CHALLENGE_MIN_RATING, 0));

		maxrating = (Spinner) findViewById(R.id.maxRating);
		maxrating.setAdapter(new ChessSpinnerAdapter(this, R.array.maxRating));
		maxrating.setSelection(preferences.getInt(AppConstants.CHALLENGE_MAX_RATING, 0));

		isRated = (CheckBox) findViewById(R.id.ratedGame);

		findViewById(R.id.createchallenge).setOnClickListener(this);

		createChallengeUpdateListener = new CreateChallengeUpdateListener();
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
		if (view.getId() == R.id.createchallenge) {
			createChallenge();
		}
	}

	private void createChallenge() {
		Integer minRating = minRatings[minrating.getSelectedItemPosition()];
		Integer maxRating = maxRatings[maxrating.getSelectedItemPosition()];


		int color = iplayas.getSelectedItemPosition();
		int days = daysArr[daysPerMoveSpinner.getSelectedItemPosition()];
		int gametype = chess960.isChecked()? 2 :0;
		int israted = isRated.isChecked() ? 1 :0;


//		String query = "http://www." + LccHolder.HOST + "/api/echess_new_game?id="
//				+ preferences.getString(AppConstants.USER_TOKEN, StaticData.SYMBOL_EMPTY) +
//				"&timepermove=" + days +
//				"&iplayas=" + color +
//				"&israted=" + israted +
//				"&game_type=" + gametype;


		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.ECHESS_NEW_GAME);
		loadItem.addRequestParams(RestHelper.P_TIMEPERMOVE, String.valueOf(days));
		loadItem.addRequestParams(RestHelper.P_IPLAYAS, String.valueOf(color));
		loadItem.addRequestParams(RestHelper.P_ISRATED, String.valueOf(israted));
		loadItem.addRequestParams(RestHelper.P_GAME_TYPE, String.valueOf(gametype));

		if (minRating != null)
			loadItem.addRequestParams(RestHelper.P_MINRATING, String.valueOf(minRating));

		if (maxRating != null)
			loadItem.addRequestParams(RestHelper.P_MAXRATING, String.valueOf(maxRating));

		new GetStringObjTask(createChallengeUpdateListener).executeTask(loadItem);
	}


	private class CreateChallengeUpdateListener extends ChessUpdateListener2 {
		public CreateChallengeUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			showSinglePopupDialog(R.string.congratulations, R.string.onlinegamecreated);
		}
	}
}
