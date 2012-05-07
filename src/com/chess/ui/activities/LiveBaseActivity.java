package com.chess.ui.activities;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.chess.R;
import com.chess.lcc.android.LccHolder;
import com.chess.lcc.android.OuterChallengeListener;
import com.chess.live.client.Challenge;
import com.chess.live.util.GameTimeConfig;
import com.chess.model.PopupItem;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.CoreActivityActionBar;
import com.chess.ui.fragments.PopupDialogFragment;

/**
 * LiveBaseActivity class
 *
 * @author alien_roger
 * @created at: 11.04.12 9:00
 */
public abstract class LiveBaseActivity extends CoreActivityActionBar{

	protected static final String CHALLENGE_TAG = "challenge_tag";
	protected static final String LOGOUT_TAG = "logout_tag";

	protected LiveOuterChallengeListener outerChallengeListener;
	protected Challenge currentChallenge;

	@Override
	protected void onResume() {
		super.onResume();
		outerChallengeListener = new LiveOuterChallengeListener();
		lccHolder.setOuterChallengeListener(outerChallengeListener);
		getActionBarHelper().hideMenuItemById(R.id.menu_singOut, lccHolder.isConnected());
	}

	@Override
	public void onLeftBtnClick(PopupDialogFragment fragment) {
		fragment.getDialog().dismiss();
		if (fragment.getTag().equals(LOGOUT_TAG)) {
			lccHolder.logout();
			backToHomeActivity();
		} else if(fragment.getTag().contains(CHALLENGE_TAG)) { // Challenge accepted!
			LccHolder.LOG.info("Accept challenge: " + currentChallenge);
			lccHolder.declineAllChallenges(currentChallenge);
			lccHolder.getAndroid().runAcceptChallengeTask(currentChallenge);
		}

	}

	@Override
	public void onRightBtnClick(PopupDialogFragment fragment) {
		if (fragment.getTag().equals(CHALLENGE_TAG)) {// Challenge declined!
			LccHolder.LOG.info("Decline challenge: " + currentChallenge);
			fragment.getDialog().dismiss();
			lccHolder.declineCurrentChallenge(currentChallenge);
			popupManager.remove(fragment);
		}else
            fragment.getDialog().dismiss();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.sign_out, menu);
		getActionBarHelper().hideMenuItemById(R.id.menu_singOut, lccHolder.isConnected(), menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_singOut:
				PopupItem popupItem = new PopupItem();
				popupItem.setTitle(R.string.confirm);
				popupItem.setMessage(R.string.signout_confirm);

				PopupDialogFragment popupDialogFragment = PopupDialogFragment.newInstance(popupItem, this);
				popupDialogFragment.show(getSupportFragmentManager(), LOGOUT_TAG);
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
			popupItem.setRightBtnId(R.string.decline);
			popupItem.setLeftBtnId(R.string.accept);

			PopupDialogFragment popupDialogFragment = PopupDialogFragment.newInstance(popupItem, LiveBaseActivity.this);
			popupDialogFragment.show(getSupportFragmentManager(), CHALLENGE_TAG);
		}

		@Override
		public void showDialog(Challenge challenge) {
			if(popupManager.size() > 0){
				return;
			}

			currentChallenge = challenge;
			popupItem.setTitle(R.string.you_been_challenged);
			popupItem.setMessage(composeMessage(challenge));
			popupItem.setRightBtnId(R.string.decline);
			popupItem.setLeftBtnId(R.string.accept);

			PopupDialogFragment popupDialogFragment = PopupDialogFragment.newInstance(popupItem, LiveBaseActivity.this);
			popupDialogFragment.updatePopupItem(popupItem);
			popupDialogFragment.show(getSupportFragmentManager(), CHALLENGE_TAG );

			popupManager.add(popupDialogFragment);
		}

		@Override
		public void hidePopups() {
			dismissAllPopups();
		}

		private String composeMessage(Challenge challenge){
			String rated = challenge.isRated()? getString(R.string.rated): getString(R.string.unrated);
			GameTimeConfig config = challenge.getGameTimeConfig();
			String blitz = AppConstants.SYMBOL_EMPTY;
			if(config.isBlitz()){
				blitz = getString(R.string.blitz_mod);
			}else if(config.isLightning()){
				blitz = getString(R.string.lightning_mod);
			}else if(config.isStandard()){
				blitz = getString(R.string.standard_mod);
			}

			String timeIncrement = AppConstants.SYMBOL_EMPTY;
			
			if(config.getTimeIncrement() > 0){
				timeIncrement = " | "+ String.valueOf(config.getTimeIncrement()/10);
			}
					
			String timeMode = config.getBaseTime()/10/60 + timeIncrement + AppConstants.SYMBOL_SPACE + blitz;
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
					.append(getString(R.string.opponent_)).append(AppConstants.SYMBOL_SPACE)
					.append(challenge.getFrom().getUsername()).append(AppConstants.SYMBOL_NEW_STR)
					.append(getString(R.string.time_)).append(AppConstants.SYMBOL_SPACE)
					.append(timeMode).append(AppConstants.SYMBOL_NEW_STR)
					.append(getString(R.string.you_play)).append(AppConstants.SYMBOL_SPACE)
					.append(playerColor).append(AppConstants.SYMBOL_NEW_STR)
					.append(rated)
					.toString();
		}
	}
}
