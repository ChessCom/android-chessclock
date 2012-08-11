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
import com.chess.backend.interfaces.ChessUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.tasks.GetStringObjTask;
import com.chess.ui.adapters.ChessSpinnerAdapter;

public class OnlineOpenChallengeActivity extends LiveBaseActivity implements OnClickListener {

	private static final String ERROR_TAG = "send request failed popup";

	private Spinner iplayasSpnr;
	private Spinner daysPerMoveSpnr;
	private Spinner minRatingSpnr;
	private Spinner maxRatingSpnr;
	private CheckBox isRatedChkBx;
	private RadioButton chess960;
	private CreateChallengeUpdateListener createChallengeUpdateListener;
	private int[] daysArr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.online_open_challenge);

		daysArr = getResources().getIntArray(R.array.daysArr);
		daysPerMoveSpnr = (Spinner) findViewById(R.id.dayspermove);
		daysPerMoveSpnr.setAdapter(new ChessSpinnerAdapter(this, R.array.dayspermove));

		chess960 = (RadioButton) findViewById(R.id.chess960);
		iplayasSpnr = (Spinner) findViewById(R.id.iplayas);
		iplayasSpnr.setAdapter(new ChessSpinnerAdapter(this, R.array.playas));

		minRatingSpnr = (Spinner) findViewById(R.id.minRating);
		minRatingSpnr.setAdapter(new ChessSpinnerAdapter(this, R.array.minRating));
		minRatingSpnr.setSelection(preferences.getInt(AppConstants.CHALLENGE_MIN_RATING, 0));

		maxRatingSpnr = (Spinner) findViewById(R.id.maxRating);
		maxRatingSpnr.setAdapter(new ChessSpinnerAdapter(this, R.array.maxRating));
		maxRatingSpnr.setSelection(preferences.getInt(AppConstants.CHALLENGE_MAX_RATING, 0));

		isRatedChkBx = (CheckBox) findViewById(R.id.ratedGame);

		findViewById(R.id.createchallenge).setOnClickListener(this);

		createChallengeUpdateListener = new CreateChallengeUpdateListener();

		showActionSettings = true;
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.createchallenge) {
			createChallenge();
		}
	}

	private void createChallenge() {
		int minPos = minRatingSpnr.getSelectedItemPosition();
		int maxPos = maxRatingSpnr.getSelectedItemPosition();

		Integer minRating = minPos == 0 ? null : Integer.parseInt((String) maxRatingSpnr.getAdapter().getItem(minPos));
		Integer maxRating = maxPos == 0 ? null : Integer.parseInt((String) maxRatingSpnr.getAdapter().getItem(maxPos));


		int color = iplayasSpnr.getSelectedItemPosition();
		int days = daysArr[daysPerMoveSpnr.getSelectedItemPosition()];
		int gameType = chess960.isChecked() ? 2 : 0;
		int isRated = this.isRatedChkBx.isChecked() ? 1 : 0;

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.ECHESS_NEW_GAME);
		loadItem.addRequestParams(RestHelper.P_TIMEPERMOVE, String.valueOf(days));
		loadItem.addRequestParams(RestHelper.P_IPLAYAS, String.valueOf(color));
		loadItem.addRequestParams(RestHelper.P_ISRATED, String.valueOf(isRated));
		loadItem.addRequestParams(RestHelper.P_GAME_TYPE, String.valueOf(gameType));

		if (minRating != null)
			loadItem.addRequestParams(RestHelper.P_MINRATING, String.valueOf(minRating));

		if (maxRating != null)
			loadItem.addRequestParams(RestHelper.P_MAXRATING, String.valueOf(maxRating));

		new GetStringObjTask(createChallengeUpdateListener).executeTask(loadItem);
	}

	private class CreateChallengeUpdateListener extends ChessUpdateListener {
		public CreateChallengeUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(String returnedObj) {
			if (returnedObj.contains(RestHelper.R_SUCCESS_)) {
				showSinglePopupDialog(R.string.congratulations, R.string.onlinegamecreated);
			} else if (returnedObj.contains(RestHelper.R_ERROR)) {
				showPopupDialog(getString(R.string.error), returnedObj.substring(RestHelper.R_ERROR.length()),
						ERROR_TAG);
			}
		}
	}

}
