package com.chess.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.DataHolder;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.FlurryData;
import com.chess.backend.statics.StaticData;
import com.chess.live.client.Challenge;
import com.chess.live.client.LiveChessClientFacade;
import com.chess.live.client.PieceColor;
import com.chess.live.util.GameTimeConfig;
import com.chess.ui.adapters.ChessSpinnerAdapter;
import com.flurry.android.FlurryAgent;

public class LiveFriendChallengeActivity extends LiveBaseActivity implements View.OnTouchListener {

	private static final String NO_ONLINE_FRIENDS_TAG = "no online friends";
//	private static final String CHALLENGE_SENT_TAG = "challenge was sent";

	private Spinner friendsSpinner;
	private AutoCompleteTextView initialTimeEdt;
	private AutoCompleteTextView bonusTimeEdt;
	private CheckBox isRated;

	private InitialTimeTextWatcher initialTimeTextWatcher;
	private InitialTimeValidator initialTimeValidator;
	private BonusTimeTextWatcher bonusTimeTextWatcher;
	private BonusTimeValidator bonusTimeValidator;
    private TextView friendsTxt;
	private Spinner iPlayAsSpnr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.live_challenge_friend);

		init();
        widgetsInit();
	}

	private void init() {
		initialTimeTextWatcher = new InitialTimeTextWatcher();
		initialTimeValidator = new InitialTimeValidator();
		bonusTimeTextWatcher = new BonusTimeTextWatcher();
		bonusTimeValidator = new BonusTimeValidator();

		DataHolder.getInstance().setLiveChess(true);

		showActionSettings = true;
	}

    protected void widgetsInit() {
        friendsSpinner = (Spinner) findViewById(R.id.friendsSpinner);
        isRated = (CheckBox) findViewById(R.id.ratedGame);
        bonusTimeEdt = (AutoCompleteTextView) findViewById(R.id.bonusTime);

		initialTimeEdt = (AutoCompleteTextView) findViewById(R.id.initialTime);
		initialTimeEdt.setText(preferences.getString(AppConstants.CHALLENGE_INITIAL_TIME, "5"));
        initialTimeEdt.addTextChangedListener(initialTimeTextWatcher);
        initialTimeEdt.setValidator(initialTimeValidator);
        initialTimeEdt.setOnTouchListener(this);
		initialTimeEdt.setSelection(initialTimeEdt.getText().length());

        bonusTimeEdt.setText(preferences.getString(AppConstants.CHALLENGE_BONUS_TIME, "0"));
        bonusTimeEdt.addTextChangedListener(bonusTimeTextWatcher);
        bonusTimeEdt.setValidator(bonusTimeValidator);
		bonusTimeEdt.setSelection(bonusTimeEdt.getText().length());
		bonusTimeEdt.setOnTouchListener(this);

		iPlayAsSpnr = (Spinner) findViewById(R.id.iplayas);
		iPlayAsSpnr.setAdapter(new ChessSpinnerAdapter(this, getItemsFromEntries(R.array.playas)));

		findViewById(R.id.createchallenge).setOnClickListener(this);
        friendsTxt = (TextView) findViewById(R.id.friendsTxt);
    }

    @Override
	protected void onResume() {
		super.onResume();

		if (getLccHolder().getUser() == null) {    // added in case user returns here after process has been killed
			if (DataHolder.getInstance().isLiveChess()) {
				getLccHolder().logout();
			}
			backToHomeActivity();
		}

		updateScreen();
	}

	private void updateScreen() {
		String[] friends = getLccHolder().getOnlineFriends();
        int friendsCnt = friends.length;
		if (friendsCnt == 0 || friends[0].equals(StaticData.SYMBOL_EMPTY)) {
			friendsSpinner.setEnabled(false);

			popupItem.setPositiveBtnId(R.string.invite);
			showPopupDialog(R.string.sorry, R.string.nofriends_online, NO_ONLINE_FRIENDS_TAG);
		}else {
            if(friendsCnt > 1){
                friendsTxt.setText(getString(R.string.friends) + StaticData.SYMBOL_SPACE + StaticData.SYMBOL_LEFT_PAR
                        + friendsCnt + StaticData.SYMBOL_RIGHT_PAR);
            }else{
                friendsTxt.setText(R.string.friend);
            }
            
			friendsSpinner.setEnabled(true);
			friendsSpinner.setAdapter(new ChessSpinnerAdapter(this, getItemsFromArray(friends)));

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
		if(isFinishing())
			return;

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
		if (initialTimeEdt.getText().toString().length() < 1 || bonusTimeEdt.getText().toString().length() < 1) {
			initialTimeEdt.setText("10");
			bonusTimeEdt.setText("0");
		}

		boolean rated = isRated.isChecked();

		int initialTimeInteger = Integer.parseInt(initialTimeEdt.getText().toString());
		int bonusTimeInteger = Integer.parseInt(bonusTimeEdt.getText().toString());

		GameTimeConfig gameTimeConfig = new GameTimeConfig(initialTimeInteger * 60 * 10, bonusTimeInteger * 10);

		Integer minRating = null;
		Integer maxRating = null;
		Integer minMembershipLevel = null;
		PieceColor pieceColor;
		switch (iPlayAsSpnr.getSelectedItemPosition()){
			case 1: pieceColor = PieceColor.WHITE; break;
			case 2: pieceColor = PieceColor.BLACK; break;
			default: pieceColor = PieceColor.UNDEFINED; break;
		}

		Challenge challenge = LiveChessClientFacade.createCustomSeekOrChallenge(
				getLccHolder().getUser(),
				friendsSpinner.getSelectedItem().toString().trim(),
				pieceColor, rated, gameTimeConfig,
				minMembershipLevel, minRating, maxRating);

		if(!getLccHolder().isConnected() || getLccHolder().getClient() == null){ // TODO should leave that screen on connection lost or when LCC is become null
			getLccHolder().logout();
			backToHomeActivity();
			return;
		}

		FlurryAgent.logEvent(FlurryData.CHALLENGE_CREATED);
		challengeTaskRunner.runSendChallengeTask(challenge);

		preferencesEditor.putString(AppConstants.CHALLENGE_INITIAL_TIME, initialTimeEdt.getText().toString().trim());
		preferencesEditor.putString(AppConstants.CHALLENGE_BONUS_TIME, bonusTimeEdt.getText().toString().trim());
		preferencesEditor.commit();
	}

	@Override
	protected void challengeTaskUpdated(Challenge challenge){
		showSinglePopupDialog(R.string.congratulations, R.string.challengeSent);
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
			final Integer initialTime = Integer.valueOf(textString);
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
