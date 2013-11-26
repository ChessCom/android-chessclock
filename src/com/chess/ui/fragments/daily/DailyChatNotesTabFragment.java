package com.chess.ui.fragments.daily;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.home.HomeFeedFragment;
import com.chess.ui.fragments.home.HomePlayFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.04.13
 * Time: 11:12
 */
public class DailyChatNotesTabFragment extends CommonLogicFragment implements RadioGroup.OnCheckedChangeListener {

	private RadioGroup tabRadioGroup;
	private int previousCheckedId;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_chat_notes_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		((TextView) view.findViewById(R.id.leftTabBtn)).setText(R.string.play);
		((TextView) view.findViewById(R.id.centerTabBtn)).setText(R.string.learn);
		((TextView) view.findViewById(R.id.rightTabBtn)).setText(R.string.feed);

		showActionBar(true);

		Fragment homeGamesFragment = new HomePlayFragment();
		changeInternalFragment(homeGamesFragment);

		tabRadioGroup = (RadioGroup) view.findViewById(R.id.tabRadioGroup);
		tabRadioGroup.setOnCheckedChangeListener(this);

		previousCheckedId = tabRadioGroup.getCheckedRadioButtonId();
	}

	private void changeInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.tab_content_frame, fragment).commit();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		updateTabs();
	}

	private void updateTabs() {
		int checkedButtonId = tabRadioGroup.getCheckedRadioButtonId();
		if (checkedButtonId != previousCheckedId) {
			previousCheckedId = checkedButtonId;
			switch (checkedButtonId) {
				case R.id.leftTabBtn: {
					Fragment fragment = findFragmentByTag(HomePlayFragment.class.getSimpleName());
					if (fragment == null) {
						fragment = new HomePlayFragment();
//						fragment = HomePlayFragment.createInstance(CENTER_MODE);
					}
					changeInternalFragment(fragment);
					break;
				}
				case R.id.rightTabBtn: {
					Fragment fragment = findFragmentByTag(HomeFeedFragment.class.getSimpleName());
					if (fragment == null) {
						fragment = new HomeFeedFragment();
					}
					changeInternalFragment(fragment);
					break;
				}
			}
		}
	}
}
