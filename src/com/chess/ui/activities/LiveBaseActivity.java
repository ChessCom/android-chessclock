package com.chess.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.MenuItem;
import com.chess.R;
import com.chess.backend.entity.DataHolder;
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
import com.chess.utilities.AppUtils;

/**
 * LiveBaseActivity class
 *
 * @author alien_roger
 * @created at: 11.04.12 9:00
 */
public abstract class LiveBaseActivity extends CoreActivityActionBar {

	private static final String TAG = "LiveBaseActivity";

	protected static final String CHALLENGE_TAG = "challenge_tag";
	protected static final String LOGOUT_TAG = "logout_tag";


	protected LiveOuterChallengeListener outerChallengeListener;
	protected Challenge currentChallenge;
	protected LccChallengeTaskRunner challengeTaskRunner;
	protected ChallengeTaskListener challengeTaskListener;
	protected GameTaskListener gameTaskListener;
	protected LccGameTaskRunner gameTaskRunner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		challengeTaskListener = new ChallengeTaskListener();
		gameTaskListener = new GameTaskListener();

		gameTaskRunner = new LccGameTaskRunner(gameTaskListener);
		challengeTaskRunner = new LccChallengeTaskRunner(challengeTaskListener);
		outerChallengeListener = new LiveOuterChallengeListener();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if(DataHolder.getInstance().isLiveChess() && !AppUtils.isNetworkAvailable(this)){ // check only if live
			popupItem.setPositiveBtnId(R.string.wireless_settings);
			showPopupDialog(R.string.warning, R.string.no_network, NETWORK_CHECK_TAG);
		}else{
			dismissFragmentDialog();
			LccHolder.getInstance(this).checkAndConnect();
		}

		LccHolder.getInstance(getContext()).setOuterChallengeListener(outerChallengeListener);

		getActionBarHelper().showMenuItemById(R.id.menu_search, showSearch);
		getActionBarHelper().showMenuItemById(R.id.menu_singOut, LccHolder.getInstance(this).isConnected());
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		super.onPositiveBtnClick(fragment);
		if (fragment.getTag().equals(LOGOUT_TAG)) {
			getLccHolder().logout();
			backToHomeActivity();
		} else if (fragment.getTag().contains(CHALLENGE_TAG)) { // Challenge accepted!
			Log.i(TAG, "Accept challenge: " + currentChallenge);
			challengeTaskRunner.declineAllChallenges(currentChallenge, getLccHolder().getChallenges());
			challengeTaskRunner.runAcceptChallengeTask(currentChallenge);
			popupManager.remove(fragment);
		} else if(fragment.getTag().equals(NETWORK_CHECK_TAG)){
			startActivityForResult(new Intent(Settings.ACTION_WIRELESS_SETTINGS), NETWORK_REQUEST);
		}
	}

	@Override
	public void onNegativeBtnClick(DialogFragment fragment) {
		super.onNegativeBtnClick(fragment);
		if (fragment.getTag().equals(CHALLENGE_TAG)) {// Challenge declined!
			Log.i(TAG, "Decline challenge: " + currentChallenge);
			challengeTaskRunner.declineCurrentChallenge(currentChallenge, getLccHolder().getChallenges());
			popupManager.remove(fragment);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK && requestCode == NETWORK_REQUEST){
			LccHolder.getInstance(this).checkAndConnect();
		}
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

			popupItem.setPositiveBtnId(R.string.accept);
			popupItem.setNegativeBtnId(R.string.decline);
			showPopupDialog(R.string.you_been_challenged, composeMessage(challenge), CHALLENGE_TAG);
		}

		@Override
		public void showDialog(Challenge challenge) {
			if (popupManager.size() > 0) {
				return;
			}

			currentChallenge = challenge;

			PopupItem popupItem = new PopupItem();
			popupItem.setTitle(R.string.you_been_challenged);
			popupItem.setMessage(composeMessage(challenge));
			popupItem.setNegativeBtnId(R.string.decline);
			popupItem.setPositiveBtnId(R.string.accept);

			PopupDialogFragment popupDialogFragment = PopupDialogFragment.newInstance(popupItem);
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
				blitz = getString(R.string.blitz_game);
			} else if (config.isLightning()) {
				blitz = getString(R.string.lightning_game);
			} else if (config.isStandard()) {
				blitz = getString(R.string.standard_game);
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
					.append(challenge.getFrom().getUsername())
					.append(getString(R.string.time_)).append(StaticData.SYMBOL_SPACE)
					.append(timeMode)
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
