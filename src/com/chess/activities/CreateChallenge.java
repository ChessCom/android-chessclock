package com.chess.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.Spinner;
import com.chess.R;
import com.chess.adapters.ChessSpinnerAdapter;
import com.chess.core.AppConstants;
import com.chess.core.CoreActivity;
import com.chess.core.Tabs;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Challenge;
import com.chess.live.client.LiveChessClientFacade;
import com.chess.live.client.PieceColor;
import com.chess.live.util.GameTimeConfig;
import com.chess.utilities.MyProgressDialog;
import com.chess.views.BackgroundChessDrawable;
import com.flurry.android.FlurryAgent;

public class CreateChallenge extends CoreActivity implements OnClickListener {
	private Spinner iplayas, dayspermove, minrating, maxrating;
	private CheckBox isRated;
	private RadioButton chess960;
	private AutoCompleteTextView initialTime;
	private AutoCompleteTextView bonusTime;
	private InitialTimeTextWatcher initialTimeTextWatcher;
	private InitialTimeValidator initialTimeValidator;
	private BonusTimeTextWatcher bonusTimeTextWatcher;
	private BonusTimeValidator bonusTimeValidator;

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
		
		if (mainApp.isLiveChess()) {
			setContentView(R.layout.live_create_challenge);
			initialTime = (AutoCompleteTextView) findViewById(R.id.initialTime);
			bonusTime = (AutoCompleteTextView) findViewById(R.id.bonusTime);

			initialTime.setText(mainApp.getSharedData().getString(AppConstants.CHALLENGE_INITIAL_TIME, "5"));
			initialTime.addTextChangedListener(initialTimeTextWatcher);
			initialTime.setValidator(initialTimeValidator);
			initialTime.setOnEditorActionListener(null);
			
			bonusTime.setText(mainApp.getSharedData().getString(AppConstants.CHALLENGE_BONUS_TIME, "0"));
			bonusTime.addTextChangedListener(bonusTimeTextWatcher );
			bonusTime.setValidator(bonusTimeValidator);

		} else {
			setContentView(R.layout.createopenchallenge);
			dayspermove = (Spinner) findViewById(R.id.dayspermove);
			chess960 = (RadioButton) findViewById(R.id.chess960);
			iplayas = (Spinner) findViewById(R.id.iplayas);
		}
		findViewById(R.id.mainView).setBackgroundDrawable(new BackgroundChessDrawable(this));

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
		initialTimeTextWatcher = new InitialTimeTextWatcher();
		initialTimeValidator = new InitialTimeValidator();
		bonusTimeTextWatcher = new BonusTimeTextWatcher();
		bonusTimeValidator = new BonusTimeValidator();
	}

	@Override
	public void LoadNext(int code) {
	}

	@Override
	public void LoadPrev(int code) {
		finish();
	}

	@Override
	public void Update(int code) {
		if (code == 0) {
			if (mainApp.isLiveChess()) {
				mainApp.getSharedDataEditor().putString(AppConstants.CHALLENGE_INITIAL_TIME, initialTime.getText().toString().trim());
				mainApp.getSharedDataEditor().putString(AppConstants.CHALLENGE_BONUS_TIME, bonusTime.getText().toString().trim());
				mainApp.getSharedDataEditor().putInt(AppConstants.CHALLENGE_MIN_RATING, minrating.getSelectedItemPosition());
				mainApp.getSharedDataEditor().putInt(AppConstants.CHALLENGE_MAX_RATING, maxrating.getSelectedItemPosition());
				mainApp.getSharedDataEditor().commit();
				//mainApp.ShowDialog(this, getString(R.string.congratulations), getString(R.string.challengeSent));
			} else {
				mainApp.ShowDialog(this, getString(R.string.congratulations), getString(R.string.onlinegamecreated));
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mainApp.isLiveChess() && lccHolder.getUser() == null) {
			lccHolder.logout();
			startActivity(new Intent(this, Tabs.class));
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

			if (mainApp.isLiveChess()) {
				if (initialTime.getText().toString().length() < 1 || bonusTime.getText().toString().length() < 1) {
					initialTime.setText("10");
					bonusTime.setText("0");
				}
				if (lccHolder.getOwnSeeksCount() >= LccHolder.OWN_SEEKS_LIMIT) {
					return;
				}
				/*PieceColor color;
											  switch(iplayas.getSelectedItemPosition())
											  {
												case 0:
												  color = PieceColor.UNDEFINED;
												  break;
												case 1:
												  color = PieceColor.WHITE;
												  break;
												case 2:
												  color = PieceColor.BLACK;
												  break;
												default:
												  color = PieceColor.UNDEFINED;
												  break;
											  }*/
				final Boolean rated = isRated.isChecked();
				final Integer initialTimeInteger = new Integer(initialTime.getText().toString());
				final Integer bonusTimeInteger = new Integer(bonusTime.getText().toString());
				final GameTimeConfig gameTimeConfig = new GameTimeConfig(initialTimeInteger * 60 * 10, bonusTimeInteger * 10);
				final String to = null;
				final Challenge challenge = LiveChessClientFacade.createCustomSeekOrChallenge(
						lccHolder.getUser(), to, PieceColor.UNDEFINED, rated, gameTimeConfig, minRating, maxRating);
				if (appService != null) {
					FlurryAgent.onEvent("Challenge Created", null);
					lccHolder.getAndroid().runSendChallengeTask(
							//progressDialog = MyProgressDialog.show(FriendChallenge.this, null, getString(R.string.creating), true),
							null,
							challenge
					);
					Update(0);
				}
			} else {
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
									.show(CreateChallenge.this, null, getString(R.string.creating), true))
					);
				}
			}
		}

	}


	private class InitialTimeTextWatcher implements TextWatcher {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			initialTime.performValidation();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			initialTime.performValidation();
		}
	}

	private class InitialTimeValidator implements AutoCompleteTextView.Validator {
		@Override
		public boolean isValid(CharSequence text) {
			final String textString = text.toString().trim();
			final Integer initialTime = new Integer(textString);
			return !textString.equals("") && initialTime >= 1 && initialTime <= 120;
		}

		@Override
		public CharSequence fixText(CharSequence invalidText) {
			return mainApp.getSharedData().getString(AppConstants.CHALLENGE_INITIAL_TIME, "5");
		}
	}

	private class BonusTimeTextWatcher implements TextWatcher {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			bonusTime.performValidation();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			bonusTime.performValidation();
		}
	}

	private class BonusTimeValidator implements AutoCompleteTextView.Validator {
		@Override
		public boolean isValid(CharSequence text) {
			final String textString = new String(text.toString().toString());
			final Integer bonusTime = new Integer(textString);
			if (!textString.equals("") && bonusTime >= 0 && bonusTime <= 60) {
				return true;
			} else {
				return false;
			}
		}

		@Override
		public CharSequence fixText(CharSequence invalidText) {
			return mainApp.getSharedData().getString(AppConstants.CHALLENGE_BONUS_TIME, "0");
		}
	}

}
