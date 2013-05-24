package com.slidingmenu.lib.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import com.chess.R;
import com.slidingmenu.lib.SlidingMenu;

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

		// customize the SlidingMenu
		SlidingMenu sm = getSlidingMenu();
		sm.setShadowWidthRes(R.dimen.shadow_width);
		sm.setShadowDrawable(R.drawable.defaultshadow);
		sm.setBehindLeftOffsetRes(R.dimen.slidingmenu_offset_left);
		sm.setBehindRightOffsetRes(R.dimen.slidingmenu_offset_right);
		sm.setFadeDegree(0.35f);
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {  // TODO check
		return true;
	}

}
