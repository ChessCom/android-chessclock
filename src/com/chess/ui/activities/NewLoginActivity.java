package com.chess.ui.activities;

import android.app.Activity;
import android.os.Bundle;
import com.chess.R;
import com.chess.ui.fragments.LoginFragment;
import com.chess.ui.fragments.RightMenuFragment;
import com.chess.ui.fragments.SampleListFragment;
import com.slidingmenu.lib.SlidingMenu;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 13:37
 */
public class NewLoginActivity extends LiveBaseActivity {

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setSlidingActionBarEnabled(true);

		setContentView(R.layout.new_main_active_frame);

		// set the Above View
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.content_frame, new LoginFragment())
				.commit();

		SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.LEFT_RIGHT);
		sm.setSecondaryMenu(R.layout.slide_menu_right_frame);
		getSupportFragmentManager()
				.beginTransaction()
				.replace(R.id.menu_frame_right, new RightMenuFragment())
				.commit();
		sm.setSecondaryShadowDrawable(R.drawable.defaultshadowright);
		sm.setShadowDrawable(R.drawable.defaultshadow);
	}
}