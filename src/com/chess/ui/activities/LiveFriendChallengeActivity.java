package com.chess.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.live.client.Challenge;
import com.chess.live.client.LiveChessClientFacade;
import com.chess.live.client.PieceColor;
import com.chess.live.util.GameTimeConfig;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.flurry.android.FlurryAgent;

public class LiveFriendChallengeActivity extends LiveBaseActivity {

	private static final String NO_ONLINE_FRIENDS_TAG = "no online friends";
	private static final String CHALLENGE_SENT_TAG = "challenge was sent";

	private Spinner friendsSpinner;
	private AutoCompleteTextView initialTime;
	private AutoCompleteTextView bonusTime;
	private CheckBox isRated;
	private RadioButton chess960;

	private InitialTimeTextWatcher initialTimeTextWatcher;
	private InitialTimeValidator initialTimeValidator;
	private BonusTimeTextWatcher bonusTimeTextWatcher;
	private BonusTimeValidator bonusTimeValidator;
	private static final int CHALLENGE_WAS_SENT = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
		setContentView(R.layout.live_challenge_friend);

		friendsSpinner = (Spinner) findViewById(R.id.friend);
		isRated = (CheckBox) findViewById(R.id.ratedGame);
		initialTime = (AutoCompleteTextView) findViewById(R.id.initialTime);
		bonusTime = (AutoCompleteTextView) findViewById(R.id.bonusTime);

		initialTime.setText(preferences.getString(AppConstants.CHALLENGE_INITIAL_TIME, "5"));
		initialTime.addTextChangedListener(initialTimeTextWatcher);
		initialTime.setValidator(initialTimeValidator);
		initialTime.setOnEditorActionListener(null);

		bonusTime.setText(preferences.getString(AppConstants.CHALLENGE_BONUS_TIME, "0"));
		bonusTime.addTextChangedListener(bonusTimeTextWatcher);
		bonusTime.setValidator(bonusTimeValidator);
		findViewById(R.id.createchallenge).setOnClickListener(this);
	}


	private void init() {
		initialTimeTextWatcher = new InitialTimeTextWatcher();
		initialTimeValidator = new InitialTimeValidator();
		bonusTimeTextWatcher = new BonusTimeTextWatcher();
		bonusTimeValidator = new BonusTimeValidator();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (getLccHolder().getUser() == null) {
			getLccHolder().logout();
			backToHomeActivity();
		}

		updateScreen();
	}

	private void updateScreen() {
		String[] friends = getLccHolder().getOnlineFriends();

		ArrayAdapter<String> friendsAdapter = new ChessSpinnerAdapter(this, friends);

		friendsSpinner.setAdapter(friendsAdapter);
		if (friendsSpinner.getSelectedItem().equals(StaticData.SYMBOL_EMPTY)) {
			popupItem.setTitle(R.string.sorry);
			popupItem.setMessage(R.string.nofriends_online);
			popupItem.setPositiveBtnId(R.string.invitetitle);

			popupDialogFragment.show(getSupportFragmentManager(), NO_ONLINE_FRIENDS_TAG);
		}
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		if (fragment.getTag().equals(NO_ONLINE_FRIENDS_TAG)) {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(RestHelper.BASE_URL)));
		}
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		super.onNegativeBtnClick(fragment);
		if (fragment.getTag().equals(NO_ONLINE_FRIENDS_TAG)) {
			onBackPressed();
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.createchallenge) {
			createChallenge();
		}
	}

	private void createChallenge() {
		if (friendsSpinner.getCount() == 0) {
			return;
		}
		if (initialTime.getText().toString().length() < 1 || bonusTime.getText().toString().length() < 1) {
			initialTime.setText("10");
			bonusTime.setText("0");
		}

		boolean rated = isRated.isChecked();

		int initialTimeInteger = Integer.parseInt(initialTime.getText().toString());
		int bonusTimeInteger = Integer.parseInt(bonusTime.getText().toString());

		GameTimeConfig gameTimeConfig = new GameTimeConfig(initialTimeInteger * 60 * 10, bonusTimeInteger * 10);

		Integer minRating = null;
		Integer maxRating = null;
		Integer minMembershipLevel = null;

		Challenge challenge = LiveChessClientFacade.createCustomSeekOrChallenge(
				getLccHolder().getUser(),
				friendsSpinner.getSelectedItem().toString().trim(),
				PieceColor.UNDEFINED, rated, gameTimeConfig,
				minMembershipLevel, minRating, maxRating);

        FlurryAgent.onEvent(FlurryData.CHALLENGE_CREATED);
		challengeTaskRunner.runSendChallengeTask(challenge);

        preferencesEditor.putString(AppConstants.CHALLENGE_INITIAL_TIME, initialTime.getText().toString().trim());
        preferencesEditor.putString(AppConstants.CHALLENGE_BONUS_TIME, bonusTime.getText().toString().trim());
        preferencesEditor.commit();

		popupItem.setTitle(R.string.congratulations);
		popupItem.setMessage(R.string.challengeSent);

		popupDialogFragment.setButtons(1);
		popupDialogFragment.show(getSupportFragmentManager(), CHALLENGE_SENT_TAG);
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
			final String textString = text.toString();
			final Integer bonusTime = Integer.parseInt(textString);
			return !textString.equals(StaticData.SYMBOL_EMPTY) && bonusTime >= 0 && bonusTime <= 60;
		}

		@Override
		public CharSequence fixText(CharSequence invalidText) {
			return preferences.getString(AppConstants.CHALLENGE_BONUS_TIME, "0");
		}
	}

}
