package com.chess.ui.fragments.daily;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioGroup;
import com.chess.R;
import com.chess.db.DbDataManager;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 05.11.13
 * Time: 18:42
 */
public class GameDailyFragmentTablet extends GameDailyFragment implements RadioGroup.OnCheckedChangeListener {


	private RadioGroup topButtonsGroup;
	private int previousCheckedId;
	private View chatFragmentContainer;
	private View chatBtn;

	public GameDailyFragmentTablet() {
	}

	public static GameDailyFragmentTablet createInstance(long gameId, String username) {
		GameDailyFragmentTablet fragment = new GameDailyFragmentTablet();
		Bundle arguments = new Bundle();
		arguments.putLong(GAME_ID, gameId);
		arguments.putString(USERNAME, username);
		fragment.setArguments(arguments);

		return fragment;
	}

	public static GameDailyFragmentTablet createInstance(long gameId, boolean forceUpdate) {
		GameDailyFragmentTablet fragment = new GameDailyFragmentTablet();
		Bundle arguments = new Bundle();
		arguments.putLong(GAME_ID, gameId);
		arguments.putBoolean(FORCE_UPDATE, forceUpdate);
		fragment.setArguments(arguments);

		return fragment;
	}

	public static GameDailyFragmentTablet createInstance(long gameId) {
		GameDailyFragmentTablet fragment = new GameDailyFragmentTablet();
		Bundle arguments = new Bundle();
		arguments.putLong(GAME_ID, gameId);
		fragment.setArguments(arguments);

		return fragment;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!username.equals(getUsername())) {
			chatBtn.setVisibility(View.GONE);
		}
	}

	@Override
	public void switch2Analysis() {
		showSubmitButtonsLay(false);

		getActivityFace().openFragment(GameDailyAnalysisFragment.createInstance(gameId, username, false));
	}

	@Override
	public void switch2Chat() {
		if (currentGame == null) {
			return;
		}

		// update game state in DB
		currentGame.setHasNewMessage(false);
		DbDataManager.saveDailyGame(getContentResolver(), currentGame, username);

		currentGame.setHasNewMessage(false);
		getControlsView().haveNewMessage(false);

		if (inLandscape()) {
			topButtonsGroup.check(R.id.chatBtn);
		} else {
			getActivityFace().openFragment(DailyChatFragment.createInstance(gameId, labelsConfig.topPlayerAvatar));
		}

	}

	@Override
	protected void widgetsInit(View view) {
		super.widgetsInit(view);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			topButtonsGroup = (RadioGroup) view.findViewById(R.id.topButtonsGroup);
			topButtonsGroup.setOnCheckedChangeListener(this);
			chatFragmentContainer = view.findViewById(R.id.chatFragmentContainer);
			chatBtn = view.findViewById(R.id.chatBtn);
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		updateRightView();
	}

	private void updateRightView() {
		int checkedButtonId = topButtonsGroup.getCheckedRadioButtonId();
		if (checkedButtonId != previousCheckedId) {
			previousCheckedId = checkedButtonId;
			switch (checkedButtonId) {
				case R.id.notationsBtn:
					((View) getNotationsFace()).setVisibility(View.VISIBLE);
					// hide chat
					chatFragmentContainer.setVisibility(View.GONE);
					break;
				case R.id.chatBtn:
					((View) getNotationsFace()).setVisibility(View.INVISIBLE);
					chatFragmentContainer.setVisibility(View.VISIBLE);

					String fragmentTag = DailyChatFragment.class.getSimpleName();

					Fragment fragmentByTag = getChildFragmentManager().findFragmentByTag(fragmentTag);
					if (fragmentByTag == null) {
						fragmentByTag = DailyChatFragment.createInstance(gameId, labelsConfig.topPlayerAvatar);

						FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
						transaction.replace(R.id.chatFragmentContainer, fragmentByTag, fragmentTag);
						transaction.commit();
					}

					break;
			}
		}
	}
}
