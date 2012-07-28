package com.chess.ui.activities;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import com.chess.R;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Challenge;
import com.chess.live.client.LiveChessClientFacade;
import com.chess.live.client.PieceColor;
import com.chess.live.util.GameTimeConfig;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.flurry.android.FlurryAgent;

public class LiveCreateChallengeActivity extends LiveBaseActivity implements View.OnTouchListener {
	private Spinner minRatingSpnr;
	private Spinner maxRatingSpnr;
	private CheckBox isRated;
	private AutoCompleteTextView initialTimeEdt;
	private AutoCompleteTextView bonusTimeEdt;
	private InitialTimeTextWatcher initialTimeTextWatcher;
	private InitialTimeValidator initialTimeValidator;
	private BonusTimeTextWatcher bonusTimeTextWatcher;
	private BonusTimeValidator bonusTimeValidator;
	private Button createChallengeBtn;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.live_create_challenge);

		init();

		initialTimeEdt = (AutoCompleteTextView) findViewById(R.id.initialTime);
		bonusTimeEdt = (AutoCompleteTextView) findViewById(R.id.bonusTime);

		initialTimeEdt.setText(preferences.getString(AppConstants.CHALLENGE_INITIAL_TIME, "5"));
		initialTimeEdt.addTextChangedListener(initialTimeTextWatcher);
		initialTimeEdt.setValidator(initialTimeValidator);
		initialTimeEdt.setOnTouchListener(this);
		initialTimeEdt.setSelection(initialTimeEdt.getText().length());

		bonusTimeEdt.setText(preferences.getString(AppConstants.CHALLENGE_BONUS_TIME, "0"));
		bonusTimeEdt.addTextChangedListener(bonusTimeTextWatcher);
		bonusTimeEdt.setValidator(bonusTimeValidator);
		bonusTimeEdt.setSelection(bonusTimeEdt.getText().length());

		minRatingSpnr = (Spinner) findViewById(R.id.minRating);
		minRatingSpnr.setAdapter(new ChessSpinnerAdapter(this, R.array.minRating));
		minRatingSpnr.setSelection(preferences.getInt(AppConstants.CHALLENGE_MIN_RATING, 0));

		maxRatingSpnr = (Spinner) findViewById(R.id.maxRating);
		maxRatingSpnr.setAdapter(new ChessSpinnerAdapter(this, R.array.maxRating));
		maxRatingSpnr.setSelection(preferences.getInt(AppConstants.CHALLENGE_MAX_RATING, 0));

		isRated = (CheckBox) findViewById(R.id.ratedGame);

		createChallengeBtn = (Button) findViewById(R.id.createchallenge);
		createChallengeBtn.setOnClickListener(this);
	}

	private void init() {
		initialTimeTextWatcher = new InitialTimeTextWatcher();
		initialTimeValidator = new InitialTimeValidator();
		bonusTimeTextWatcher = new BonusTimeTextWatcher();
		bonusTimeValidator = new BonusTimeValidator();

		DataHolder.getInstance().setLiveChess(true);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.createchallenge) {
			int minPos = minRatingSpnr.getSelectedItemPosition();
			int maxPos = maxRatingSpnr.getSelectedItemPosition();

			Integer minRating = minPos == 0? null : Integer.parseInt((String) maxRatingSpnr.getAdapter().getItem(minPos));
			Integer maxRating = maxPos == 0? null : Integer.parseInt((String) maxRatingSpnr.getAdapter().getItem(maxPos));
			Integer minMembershipLevel = 0;

			if (initialTimeEdt.getText().toString().length() < 1 || bonusTimeEdt.getText().toString().length() < 1) {
				initialTimeEdt.setText("10");
				bonusTimeEdt.setText("0");
			}
			if (getLccHolder().getOwnSeeksCount() >= LccHolder.OWN_SEEKS_LIMIT) {
				return;
			}

			Boolean rated = isRated.isChecked();
			Integer initialTimeInteger = new Integer(initialTimeEdt.getText().toString());
			Integer bonusTimeInteger = new Integer(bonusTimeEdt.getText().toString());
			GameTimeConfig gameTimeConfig = new GameTimeConfig(initialTimeInteger * 60 * 10, bonusTimeInteger * 10);
			String to = null;
			Challenge challenge = LiveChessClientFacade.createCustomSeekOrChallenge(
					getLccHolder().getUser(), to, PieceColor.UNDEFINED, rated, gameTimeConfig,
					minMembershipLevel, minRating, maxRating);

			FlurryAgent.onEvent(FlurryData.CHALLENGE_CREATED, null);

			challengeTaskRunner.runSendChallengeTask(challenge);

			preferencesEditor.putString(AppConstants.CHALLENGE_INITIAL_TIME, initialTimeEdt.getText().toString().trim());
			preferencesEditor.putString(AppConstants.CHALLENGE_BONUS_TIME, bonusTimeEdt.getText().toString().trim());
			preferencesEditor.putInt(AppConstants.CHALLENGE_MIN_RATING, minRatingSpnr.getSelectedItemPosition());
			preferencesEditor.putInt(AppConstants.CHALLENGE_MAX_RATING, maxRatingSpnr.getSelectedItemPosition());
			preferencesEditor.commit();

			createChallengeBtn.setEnabled(false);
			new Handler().postDelayed(new Runnable() {
				public void run() {
					createChallengeBtn.setEnabled(true);
				}
			}, 2000);
		}
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (view.getId() == R.id.initialTime){
			initialTimeEdt.setSelection(initialTimeEdt.getText().length());
		} else if (view.getId() == R.id.bonusTime) {
			bonusTimeEdt.setSelection(bonusTimeEdt.getText().length());
		}
		return false;
	}

	private class InitialTimeTextWatcher implements TextWatcher {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			initialTimeEdt.performValidation();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			initialTimeEdt.performValidation();
		}
	}

	private class InitialTimeValidator implements AutoCompleteTextView.Validator {
		@Override
		public boolean isValid(CharSequence text) {
			final String textString = text.toString().trim();
			final Integer initialTime = Integer.parseInt(textString);
			return !textString.equals(StaticData.SYMBOL_EMPTY) && initialTime >= 1 && initialTime <= 120;
		}

		@Override
		public CharSequence fixText(CharSequence invalidText) {
			return preferences.getString(AppConstants.CHALLENGE_INITIAL_TIME, "5");
		}
	}

	private class BonusTimeTextWatcher implements TextWatcher {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			bonusTimeEdt.performValidation();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			bonusTimeEdt.performValidation();
		}
	}

	private class BonusTimeValidator implements AutoCompleteTextView.Validator {
		@Override
		public boolean isValid(CharSequence text) {
			final String textString = text.toString().trim();
			final Integer bonusTime = Integer.parseInt(textString);
            return !textString.equals(StaticData.SYMBOL_EMPTY) && bonusTime >= 0 && bonusTime <= 60;
		}

		@Override
		public CharSequence fixText(CharSequence invalidText) {
			return preferences.getString(AppConstants.CHALLENGE_BONUS_TIME, "0");
		}
	}

}
