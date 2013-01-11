package com.slidingmenu.lib.app;

import android.os.Bundle;
import android.support.v4.app.*;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import com.chess.R;
import com.chess.ui.fragments.SampleListFragment;
import com.slidingmenu.lib.SlidingMenu;

import java.util.ArrayList;
import java.util.List;

public class BaseActivity extends SlidingFragmentActivity {

	private int mTitleRes;
	protected Fragment leftMenuFragment;

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
		leftMenuFragment = new SampleListFragment();
		ft.replace(R.id.menu_frame_left, leftMenuFragment);
		ft.commit();

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
//		sm.setShadowDrawable(R.drawable.defaultshadow);
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {  // TODO check
		return true;
	}

}
