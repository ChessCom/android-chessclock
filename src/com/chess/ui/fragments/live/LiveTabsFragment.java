package com.chess.ui.fragments.live;

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
import com.chess.ui.fragments.stats.StatsGameDetailsFragment;
import com.chess.ui.fragments.stats.StatsGameFragment;
import com.chess.ui.interfaces.FragmentTabsFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.09.13
 * Time: 6:25
 */
public class LiveTabsFragment extends CommonLogicFragment implements RadioGroup.OnCheckedChangeListener, FragmentTabsFace {

	private RadioGroup tabRadioGroup;
	private int previousCheckedId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_base_tabs_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.live);

		((TextView)view.findViewById(R.id.leftTabBtn)).setText(R.string.new_);
		((TextView)view.findViewById(R.id.centerTabBtn)).setText(R.string.archive);
		((TextView)view.findViewById(R.id.rightTabBtn)).setText(R.string.stats);

		showActionBar(true);

		changeInternalFragment(new LiveNewGameFragment());

		tabRadioGroup = (RadioGroup) view.findViewById(R.id.tabRadioGroup);
		tabRadioGroup.setOnCheckedChangeListener(this);
		tabRadioGroup.check(R.id.leftTabBtn);// TODO centerTabBtn

		previousCheckedId = tabRadioGroup.getCheckedRadioButtonId();
	}

	@Override
	public void onResume() {
		super.onResume();
		updateTabs();
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
				case R.id.leftTabBtn:
					changeInternalFragment(new LiveNewGameFragment());
					break;
				case R.id.centerTabBtn:
					changeInternalFragment(new LiveArchiveFragment());
					break;
				case R.id.rightTabBtn:
					changeInternalFragment(StatsGameDetailsFragment.createInstance(StatsGameFragment.LIVE_STANDARD, getUsername()));
					break;
			}
		}
	}

	private void changeInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.tab_content_frame, fragment, fragment.getClass().getSimpleName());
		transaction.addToBackStack(fragment.getClass().getSimpleName());
		transaction.commit();
	}

	@Override
	public void changeInternalFragment(int code) {
	}

	@Override
	public void onPageSelected(int page) {

	}
}
