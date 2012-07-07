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

	private InitialTimeTextWatcher initialTimeTextWatcher;
	private InitialTimeValidator initialTimeValidator;
	private BonusTimeTextWatcher bonusTimeTextWatcher;
	private BonusTimeValidator bonusTimeValidator;
    private TextView friendsTxt;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.live_challenge_friend);

		init();
        widgetsInit();

        // TODO add settings to action bar menu
	}

	private void init() {
		initialTimeTextWatcher = new InitialTimeTextWatcher();
		initialTimeValidator = new InitialTimeValidator();
		bonusTimeTextWatcher = new BonusTimeTextWatcher();
		bonusTimeValidator = new BonusTimeValidator();
	}

    protected void widgetsInit() {
        friendsSpinner = (Spinner) findViewById(R.id.friendsSpinner);
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
        friendsTxt = (TextView) findViewById(R.id.friendsTxt);
    }

    @Override
	protected void onResume() {
		super.onResume();

		updateScreen();
	}

	private void updateScreen() {
		String[] friends = getLccHolder().getOnlineFriends();
        int friendsCnt = friends.length;
		if (friendsCnt == 0) {
			friendsSpinner.setEnabled(false);

			popupItem.setPositiveBtnId(R.string.invitetitle);
			showPopupDialog(R.string.sorry, R.string.nofriends_online, NO_ONLINE_FRIENDS_TAG);
		}else {
            if(friendsCnt > 1){
                friendsTxt.setText(getString(R.string.friends) + StaticData.SYMBOL_SPACE + StaticData.SYMBOL_LEFT_PAR
                        + friendsCnt + StaticData.SYMBOL_RIGHT_PAR);
            }else{
                friendsTxt.setText(R.string.friend);
            }
            
			friendsSpinner.setEnabled(true);
			friendsSpinner.setAdapter(new ChessSpinnerAdapter(this, friends));

			dismissFragmentDialog();
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

	@Override
	public void onFriendsStatusChanged() {
		super.onFriendsStatusChanged();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				updateScreen();
			}
		});
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

		showPopupDialog(R.string.congratulations, R.string.challengeSent, CHALLENGE_SENT_TAG);
		popupDialogFragment.setButtons(1);
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
