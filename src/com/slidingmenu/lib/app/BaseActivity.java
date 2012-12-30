package com.slidingmenu.lib.app;

import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import com.chess.R;
import com.chess.ui.fragments.SampleListFragment;
import com.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends SlidingFragmentActivity {

	private int mTitleRes;
	protected ListFragment mFrag;

	public BaseActivity(int titleRes) {
		mTitleRes = titleRes;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setTitle(mTitleRes);

		// set the Behind View
		setBehindContentView(R.layout.slide_menu_left_frame);
		FragmentTransaction ft = this.getSupportFragmentManager().beginTransaction();
		mFrag = new SampleListFragment();
		ft.replace(R.id.menu_frame_left, mFrag);
		ft.commit();

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
//		sm.setShadowDrawable(R.drawable.defaultshadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);

//		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			toggle();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.sign_out, menu);
		return true;
	}

	public class BasePagerAdapter extends FragmentPagerAdapter {
		private List<Fragment> mFragments = new ArrayList<Fragment>();
		private ViewPager mPager;

		public BasePagerAdapter(FragmentManager fm, ViewPager vp) {
			super(fm);
			mPager = vp;
			mPager.setAdapter(this);
			for (int i = 0; i < 3; i++) {
				addTab(new SampleListFragment());
			}
		}

		public void addTab(Fragment frag) {
			mFragments.add(frag);
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}
	}

}
