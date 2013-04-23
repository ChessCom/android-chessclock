package com.chess.ui.fragments.sign_in;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.ui.fragments.CommonLogicFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 23.04.13
 * Time: 7:04
 */
public class WelcomeMainFragment extends CommonLogicFragment {

	private ViewPager viewPager;
	private static final int PAGE_CNT = 5;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_welcome_main_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		viewPager = (ViewPager) view.findViewById(R.id.mainViewPager);
		WelcomePagerAdapter mainPageAdapter = new WelcomePagerAdapter(getFragmentManager());
		viewPager.setAdapter(mainPageAdapter);
//		viewPager.setOnPageChangeListener(pageChangeListener);
	}

	private class WelcomePagerAdapter extends FragmentPagerAdapter {

		public WelcomePagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0: {
					Fragment fragment = getFragmentManager().findFragmentByTag(WelcomeOneFragment.class.getSimpleName());
					if (fragment == null) {
						fragment = new WelcomeOneFragment();
					}
					return fragment;
				}
				case 1: {
					Fragment fragment = getFragmentManager().findFragmentByTag(WelcomeTwoFragment.class.getSimpleName());
					if (fragment == null) {
						fragment = new WelcomeTwoFragment();
					}
					return fragment;
				}
				case 2: {
					Fragment fragment = getFragmentManager().findFragmentByTag(WelcomeThreeFragment.class.getSimpleName());
					if (fragment == null) {
						fragment = new WelcomeThreeFragment();
					}
					return fragment;
				}
				case 3: {
					Fragment fragment = getFragmentManager().findFragmentByTag(WelcomeFourFragment.class.getSimpleName());
					if (fragment == null) {
						fragment = new WelcomeFourFragment();
					}
					return fragment;
				}
				case 4: {
					Fragment fragment = getFragmentManager().findFragmentByTag(SignUpFragment.class.getSimpleName());
					if (fragment == null) {
						fragment = new SignUpFragment();
					}
					return fragment;
				}
			}
			return null;
		}

		@Override
		public int getCount() {
			return PAGE_CNT;
		}
	}
}
