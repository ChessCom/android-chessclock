package com.chess.ui.fragments.sign_in;

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

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.05.13
 * Time: 17:01
 */
public class WelcomeTabsFragment extends CommonLogicFragment implements RadioGroup.OnCheckedChangeListener {

	private static final int NON_INIT = -1;

	private RadioGroup tabRadioGroup;
	private int previousCheckedId = NON_INIT;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_welcome_tabs_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		TextView leftTabBtn = (TextView) view.findViewById(R.id.leftTabBtn);
		leftTabBtn.setText(R.string.log_in);
//		((TextView) view.findViewById(R.id.centerTabBtn)).setText(R);
		((TextView) view.findViewById(R.id.rightTabBtn)).setText(R.string.sign_up);


		tabRadioGroup = (RadioGroup) view.findViewById(R.id.tabRadioGroup);
		tabRadioGroup.setOnCheckedChangeListener(this);

		showActionBar(false);
	}

	@Override
	public void onStart() {
		super.onStart();

		if (previousCheckedId == NON_INIT) {
			tabRadioGroup.check(R.id.centerTabBtn);
		}

		updateTabs();
	}

	private void updateTabs() {
		int checkedButtonId = tabRadioGroup.getCheckedRadioButtonId();
		if (checkedButtonId != previousCheckedId) {
			previousCheckedId = checkedButtonId;
			switch (checkedButtonId) {
				case R.id.leftTabBtn: {
					Fragment fragment = findFragmentByTag(SignInFragment.class.getSimpleName());
					if (fragment == null) {
						fragment = new SignInFragment();
					}

					changeInternalFragment(fragment);
					break;
				}
				case R.id.centerTabBtn: {
					Fragment fragment = findFragmentByTag(WelcomeGameCompFragment.class.getSimpleName());
					if (fragment == null) {
						fragment = new WelcomeGameCompFragment();
					}
					changeInternalFragment(fragment);
					break;
				}
				case R.id.rightTabBtn: {
					Fragment fragment = findFragmentByTag(SignUpFragment.class.getSimpleName());
					if (fragment == null) {
						fragment = new SignUpFragment();
					}
					changeInternalFragment(fragment);
					break;
				}
			}
		}
	}

	private void changeInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.tab_content_frame, fragment).commit();
	}


	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		updateTabs();
	}
}
