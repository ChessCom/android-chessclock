package com.chess.ui.fragments.profiles;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.MenuItem;
import android.view.View;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.daily_games.DailySeekItem;
import com.chess.statics.Symbol;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.engine.configs.DailyGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.messages.NewMessageFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.09.13
 * Time: 12:53
 */
public abstract class ProfileBaseFragment extends CommonLogicFragment {

	protected String username;
	private static final String CREATE_CHALLENGE_TAG = "create challenge confirm popup";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			username = getArguments().getString(USERNAME);
		} else {
			username = savedInstanceState.getString(USERNAME);
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_message, true);
		getActivityFace().showActionMenu(R.id.menu_challenge, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(USERNAME, username);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_message:
				getActivityFace().openFragment(NewMessageFragment.createInstance(username));
				break;
			case R.id.menu_challenge:
				String title = getString(R.string.challenge) + Symbol.SPACE + username + Symbol.QUESTION;
				showPopupDialog(title, CREATE_CHALLENGE_TAG);
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPositiveBtnClick(DialogFragment fragment) {
		String tag = fragment.getTag();
		if (tag == null) {
			super.onPositiveBtnClick(fragment);
			return;
		}

		if (tag.equals(CREATE_CHALLENGE_TAG)) {
			createDailyChallenge(username);
		}
		super.onPositiveBtnClick(fragment);
	}

	private void createDailyChallenge(String opponentName) {
		// create challenge using formed configuration
		DailyGameConfig dailyGameConfig = new DailyGameConfig.Builder().build();
		dailyGameConfig.setOpponentName(opponentName);


		LoadItem loadItem = LoadHelper.postGameSeek(getUserToken(), dailyGameConfig);
		new RequestJsonTask<DailySeekItem>(new CreateChallengeUpdateListener()).executeTask(loadItem);
	}

	private class CreateChallengeUpdateListener extends ChessLoadUpdateListener<DailySeekItem> {

		public CreateChallengeUpdateListener() {
			super(DailySeekItem.class);
		}

		@Override
		public void updateData(DailySeekItem returnedObj) {
			showSinglePopupDialog(R.string.challenge_created, R.string.you_will_notified_when_game_starts);
		}
	}
}
