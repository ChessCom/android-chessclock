package com.chess.ui.fragments.live;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioGroup;
import com.chess.R;
import com.chess.ui.fragments.daily.DailyChatFragment;
import com.chess.ui.fragments.daily.GameDailyAnalysisFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.11.13
 * Time: 10:15
 */
public class GameLiveFragmentTablet extends GameLiveFragment implements RadioGroup.OnCheckedChangeListener {

	private RadioGroup topButtonsGroup;
	private int previousCheckedId;
	private View chatFragmentContainer;

	public GameLiveFragmentTablet() { }

	public static GameLiveFragmentTablet createInstance(long id) {
		GameLiveFragmentTablet fragment = new GameLiveFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putLong(GAME_ID, id);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void switch2Analysis() {
		showSubmitButtonsLay(false);

		getActivityFace().openFragment(GameDailyAnalysisFragment.createInstance(gameId, getUsername()));
	}

	@Override
	protected void widgetsInit(View view) {
		super.widgetsInit(view);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			topButtonsGroup = (RadioGroup) view.findViewById(R.id.topButtonsGroup);
			topButtonsGroup.setOnCheckedChangeListener(this);
			chatFragmentContainer = view.findViewById(R.id.chatFragmentContainer);
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
