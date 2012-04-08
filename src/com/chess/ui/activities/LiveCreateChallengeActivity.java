package com.chess.ui.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.Spinner;
import com.chess.R;
import com.chess.lcc.android.LccHolder;
import com.chess.live.client.Challenge;
import com.chess.live.client.LiveChessClientFacade;
import com.chess.live.client.PieceColor;
import com.chess.live.util.GameTimeConfig;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.CoreActivityActionBar;
import com.chess.ui.fragments.PopupDialogFragment;
import com.flurry.android.FlurryAgent;

public class LiveCreateChallengeActivity extends CoreActivityActionBar implements OnClickListener {
	private Spinner minrating;
	private Spinner maxrating;
	private CheckBox isRated;
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

		setContentView(R.layout.live_create_challenge);
		findViewById(R.id.mainView).setBackgroundDrawable(backgroundChessDrawable);

		initialTime = (AutoCompleteTextView) findViewById(R.id.initialTime);
		bonusTime = (AutoCompleteTextView) findViewById(R.id.bonusTime);

		initialTime.setText(mainApp.getSharedData().getString(AppConstants.CHALLENGE_INITIAL_TIME, "5"));
		initialTime.addTextChangedListener(initialTimeTextWatcher);
		initialTime.setValidator(initialTimeValidator);
		initialTime.setOnEditorActionListener(null);

		bonusTime.setText(mainApp.getSharedData().getString(AppConstants.CHALLENGE_BONUS_TIME, "0"));
		bonusTime.addTextChangedListener(bonusTimeTextWatcher);
		bonusTime.setValidator(bonusTimeValidator);

		minrating = (Spinner) findViewById(R.id.minRating);
		minrating.setAdapter(new ChessSpinnerAdapter(this, R.array.minRating));
		minrating.setSelection(mainApp.getSharedData().getInt(AppConstants.CHALLENGE_MIN_RATING, 0));

		maxrating = (Spinner) findViewById(R.id.maxRating);
		maxrating.setAdapter(new ChessSpinnerAdapter(this, R.array.maxRating));
		maxrating.setSelection(mainApp.getSharedData().getInt(AppConstants.CHALLENGE_MAX_RATING, 0));

		isRated = (CheckBox) findViewById(R.id.ratedGame);

		findViewById(R.id.createchallenge).setOnClickListener(this);
	}

	private void init() {
		initialTimeTextWatcher = new InitialTimeTextWatcher();
		initialTimeValidator = new InitialTimeValidator();
		bonusTimeTextWatcher = new BonusTimeTextWatcher();
		bonusTimeValidator = new BonusTimeValidator();
	}

	@Override
	public void update(int code) {
		if (code == 0) {
			mainApp.getSharedDataEditor().putString(AppConstants.CHALLENGE_INITIAL_TIME, initialTime.getText().toString().trim());
			mainApp.getSharedDataEditor().putString(AppConstants.CHALLENGE_BONUS_TIME, bonusTime.getText().toString().trim());
			mainApp.getSharedDataEditor().putInt(AppConstants.CHALLENGE_MIN_RATING, minrating.getSelectedItemPosition());
			mainApp.getSharedDataEditor().putInt(AppConstants.CHALLENGE_MAX_RATING, maxrating.getSelectedItemPosition());
			mainApp.getSharedDataEditor().commit();
			//mainApp.showDialog(this, getString(R.string.congratulations), getString(R.string.challengeSent));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mainApp.isLiveChess() && lccHolder.getUser() == null) {
			lccHolder.logout();
			backToHomeActivity();
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

	@Override
	public void onLeftBtnClick(PopupDialogFragment fragment) {
		lccHolder.logout();
		backToHomeActivity();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.sign_out, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_singOut:
				popupItem.setTitleId(R.string.confirm);
				popupItem.setMessageId(R.string.signout_confirm);

				popupDialogFragment.updatePopupItem(popupItem);
				popupDialogFragment.show(getSupportFragmentManager(), "dialog");
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.createchallenge) {

			Integer minRating = minRatings[minrating.getSelectedItemPosition()];
			Integer maxRating = maxRatings[maxrating.getSelectedItemPosition()];

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
			Boolean rated = isRated.isChecked();
			Integer initialTimeInteger = new Integer(initialTime.getText().toString());
			Integer bonusTimeInteger = new Integer(bonusTime.getText().toString());
			GameTimeConfig gameTimeConfig = new GameTimeConfig(initialTimeInteger * 60 * 10, bonusTimeInteger * 10);
			String to = null;
			Challenge challenge = LiveChessClientFacade.createCustomSeekOrChallenge(
					lccHolder.getUser(), to, PieceColor.UNDEFINED, rated, gameTimeConfig, minRating, maxRating);
			if (appService != null) {
				FlurryAgent.onEvent("Challenge Created", null);
				lccHolder.getAndroid().runSendChallengeTask(
						//progressDialog = MyProgressDialog.show(FriendChallenge.this, null, getString(R.string.creating), true),
						null,
						challenge
				);
				update(GameBaseActivity.CALLBACK_REPAINT_UI);
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
			final Integer initialTime = Integer.parseInt(textString);
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
			final String textString = text.toString().trim();
			final Integer bonusTime = Integer.parseInt(textString);
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
