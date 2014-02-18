package com.chess.ui.fragments.live;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RadioGroup;
import com.chess.R;

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

	public GameLiveFragmentTablet() {
	}

	public static GameLiveFragmentTablet createInstance(long id) {
		GameLiveFragmentTablet fragment = new GameLiveFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putLong(GAME_ID, id);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void switch2Chat() {
		if (inLandscape()) {
			topButtonsGroup.check(R.id.chatBtn);
		} else {
			super.switch2Chat();
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

					String fragmentTag = LiveChatFragment.class.getSimpleName();

					Fragment fragmentByTag = getChildFragmentManager().findFragmentByTag(fragmentTag);
					if (fragmentByTag == null) {
						fragmentByTag = new LiveChatFragment();

						FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
						transaction.replace(R.id.chatFragmentContainer, fragmentByTag, fragmentTag);
						transaction.commit();
					} else {
						((LiveChatFragment)fragmentByTag).updateData();
					}

					break;
			}
		}
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
}
