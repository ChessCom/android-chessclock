package com.chess.ui.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.chess.R;
import com.chess.lcc.android.LccHolder;
import com.chess.lcc.android.OuterChallengeListener;
import com.chess.live.client.Challenge;
import com.chess.live.util.GameTimeConfig;
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
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// set listener to lccHolder
		outerChallengeListener = new LiveOuterChallengeListener();
		lccHolder.setOuterChallengeListener(outerChallengeListener);
	}


	@Override
	public void onLeftBtnClick(PopupDialogFragment fragment) {  // TODO catch dialog
		if(fragment.getTag().equals(LOGOUT_TAG)){
			lccHolder.logout();
			backToHomeActivity();
		}else if(fragment.getTag().equals(CHALLENGE_TAG)){

			LccHolder.LOG.info("Accept challenge: " + currentChallenge);
			lccHolder.getAndroid().runAcceptChallengeTask(currentChallenge);
			update(2);
		}
		fragment.getDialog().dismiss();
	}

	@Override
	public void onRightBtnClick(PopupDialogFragment fragment) {
		if (fragment.getTag().equals(CHALLENGE_TAG)) {
			LccHolder.LOG.info("Decline challenge: " + currentChallenge);
			lccHolder.getAndroid().runRejectChallengeTask(currentChallenge);
			update(3);
		}
		fragment.getDialog().dismiss();
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
				popupItem.setTitle(R.string.confirm);
				popupItem.setMessage(R.string.signout_confirm);

				popupDialogFragment.updatePopupItem(popupItem);
				popupDialogFragment.show(getSupportFragmentManager(), LOGOUT_TAG);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class LiveOuterChallengeListener implements OuterChallengeListener {

		@Override
		public void showDialog(Challenge challenge) {

			currentChallenge = challenge;

			popupItem.setTitle(R.string.you_been_challenged);
			popupItem.setMessage(composeMessage(challenge));
			popupItem.setRightBtnId(R.string.decline);
			popupItem.setLeftBtnId(R.string.accept);

			popupDialogFragment.updatePopupItem(popupItem);
			popupDialogFragment.show(getSupportFragmentManager(), CHALLENGE_TAG);
		}

		private String composeMessage(Challenge challenge){
			String rated = challenge.isRated()? getString(R.string.rated): getString(R.string.unrated);
			GameTimeConfig config = challenge.getGameTimeConfig();
			String blitz = "";
			if(config.isBlitz()){
				blitz = "(Blitz)";
			}else if(config.isLightning()){
				blitz = "(Lightning)";
			}else if(config.isStandard()){
				blitz = "(Standard)";
			}

			String timeIncrement = "";
			
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
