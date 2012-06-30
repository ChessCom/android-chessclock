package com.chess.ui.activities;

import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.MenuItem;
import com.chess.R;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.StaticData;
import com.chess.lcc.android.LccChallengeTaskRunner;
import com.chess.lcc.android.LccGameTaskRunner;
import com.chess.lcc.android.LccHolder;
import com.chess.lcc.android.OuterChallengeListener;
import com.chess.live.client.Challenge;
import com.chess.live.client.Game;
import com.chess.live.util.GameTimeConfig;
import com.chess.model.PopupItem;
import com.chess.ui.fragments.PopupDialogFragment;

/**
 * LiveBaseActivity class
 *
 * @author alien_roger
 * @created at: 11.04.12 9:00
 */
public abstract class LiveBaseActivity extends CoreActivityActionBar {

	protected static final String CHALLENGE_TAG = "challenge_tag";
	protected static final String LOGOUT_TAG = "logout_tag";
	private static final String TAG = "LiveBaseActivity";

	protected LiveOuterChallengeListener outerChallengeListener;
	protected Challenge currentChallenge;
	protected LccChallengeTaskRunner challengeTaskRunner;
	protected ChallengeTaskListener challengeTaskListener;
	protected GameTaskListener gameTaskListener;
	protected LccGameTaskRunner gameTaskRunner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Build.VERSION.SDK_INT >= StaticData.SDK_ICE_CREAM_SANDWICH && getActionBar() != null) {
			getActionBar().setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
					| ActionBar.DISPLAY_USE_LOGO
					| ActionBar.DISPLAY_SHOW_HOME
					| ActionBar.DISPLAY_SHOW_TITLE);
		}
		challengeTaskListener = new ChallengeTaskListener();
		gameTaskListener = new GameTaskListener();

		gameTaskRunner = new LccGameTaskRunner(gameTaskListener);
		challengeTaskRunner = new LccChallengeTaskRunner(challengeTaskListener);
		outerChallengeListener = new LiveOuterChallengeListener();

	}

	@Override
	protected void onResume() {
		super.onResume();
		LccHolder.getInstance(getContext()).setOuterChallengeListener(outerChallengeListener);

		getActionBarHelper().showMenuItemById(R.id.menu_singOut, LccHolder.getInstance(this).isConnected());
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		fragment.getDialog().dismiss();
		if (fragment.getTag().equals(LOGOUT_TAG)) {
			getLccHolder().logout();
			backToHomeActivity();
		} else if (fragment.getTag().contains(CHALLENGE_TAG)) { // Challenge accepted!
			Log.i(TAG, "Accept challenge: " + currentChallenge);
			challengeTaskRunner.declineAllChallenges(currentChallenge, getLccHolder().getChallenges());
			challengeTaskRunner.runAcceptChallengeTask(currentChallenge);
		}
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		if (fragment.getTag().equals(CHALLENGE_TAG)) {// Challenge declined!
			Log.i(TAG, "Decline challenge: " + currentChallenge);
			fragment.getDialog().dismiss();
			challengeTaskRunner.declineCurrentChallenge(currentChallenge, getLccHolder().getChallenges());
			popupManager.remove(fragment);
		} else
			fragment.getDialog().dismiss();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_singOut:
				showPopupDialog(R.string.confirm, R.string.signout_confirm, LOGOUT_TAG);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class LiveOuterChallengeListener implements OuterChallengeListener {
		@Override
		public void showDelayedDialog(Challenge challenge) {
			currentChallenge = challenge;
			PopupItem popupItem = new PopupItem();
			popupItem.setTitle(R.string.you_been_challenged);
			popupItem.setMessage(composeMessage(challenge));
			popupItem.setNegativeBtnId(R.string.decline);
			popupItem.setPositiveBtnId(R.string.accept);

			PopupDialogFragment popupDialogFragment = PopupDialogFragment.newInstance(popupItem, LiveBaseActivity.this);
			popupDialogFragment.show(getSupportFragmentManager(), CHALLENGE_TAG);
		}

		@Override
		public void showDialog(Challenge challenge) {
			if (popupManager.size() > 0) {
				return;
			}

			currentChallenge = challenge;
			popupItem.setTitle(R.string.you_been_challenged);
			popupItem.setMessage(composeMessage(challenge));
			popupItem.setNegativeBtnId(R.string.decline);
			popupItem.setPositiveBtnId(R.string.accept);

			PopupDialogFragment popupDialogFragment = PopupDialogFragment.newInstance(popupItem, LiveBaseActivity.this);
			popupDialogFragment.updatePopupItem(popupItem);
			popupDialogFragment.show(getSupportFragmentManager(), CHALLENGE_TAG);

			popupManager.add(popupDialogFragment);
		}

		@Override
		public void hidePopups() {
			dismissAllPopups();
		}

		private String composeMessage(Challenge challenge) {
			String rated = challenge.isRated() ? getString(R.string.rated) : getString(R.string.unrated);
			GameTimeConfig config = challenge.getGameTimeConfig();
			String blitz = StaticData.SYMBOL_EMPTY;
			if (config.isBlitz()) {
				blitz = getString(R.string.blitz_mod);
			} else if (config.isLightning()) {
				blitz = getString(R.string.lightning_mod);
			} else if (config.isStandard()) {
				blitz = getString(R.string.standard_mod);
			}

			String timeIncrement = StaticData.SYMBOL_EMPTY;

			if (config.getTimeIncrement() > 0) {
				timeIncrement = " | " + String.valueOf(config.getTimeIncrement() / 10);
			}

			String timeMode = config.getBaseTime() / 10 / 60 + timeIncrement + StaticData.SYMBOL_SPACE + blitz;
			String playerColor;

			switch (challenge.getColor()) {
				case UNDEFINED:
					playerColor = getString(R.string.random);
					break;
				case WHITE:
					playerColor = getString(R.string.black);
					break;
				case BLACK:
					playerColor = getString(R.string.white);
					break;
				default:
					playerColor = getString(R.string.random);
					break;
			}

			return new StringBuilder()
					.append(getString(R.string.opponent_)).append(StaticData.SYMBOL_SPACE)
					.append(challenge.getFrom().getUsername()).append(StaticData.SYMBOL_NEW_STR)
					.append(getString(R.string.time_)).append(StaticData.SYMBOL_SPACE)
					.append(timeMode).append(StaticData.SYMBOL_NEW_STR)
					.append(getString(R.string.you_play)).append(StaticData.SYMBOL_SPACE)
					.append(playerColor).append(StaticData.SYMBOL_NEW_STR)
					.append(rated)
					.toString();
		}
	}

	private class ChallengeTaskListener extends ActionBarUpdateListener<Challenge> {
		public ChallengeTaskListener() {
			super(getInstance());
		}
	}

	private class GameTaskListener extends ActionBarUpdateListener<Game> {
		public GameTaskListener() {
			super(getInstance());
		}
	}

}
