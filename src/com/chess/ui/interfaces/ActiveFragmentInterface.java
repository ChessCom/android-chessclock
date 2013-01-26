package com.chess.ui.interfaces;


import android.graphics.drawable.Drawable;
import com.chess.lcc.android.LccHolder;
import com.chess.ui.activities.CoreActivityActionBar;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.fragments.CommonLogicFragment;
import com.slidingmenu.lib.SlidingMenu;

public interface ActiveFragmentInterface {

    boolean isMenuActive();

	void toggleMenu(int code);

	void closeMenu(int code);

	void openFragment(BasePopupsFragment fragment);

	void switchFragment(BasePopupsFragment fragment);

	void openFragment(BasePopupsFragment fragment, int code);

	void switchFragment(BasePopupsFragment fragment, int code);

    void showPreviousFragment();

    void updateCurrentActiveFragment();

    void setBadgeValueForId(int menuId, int value);

	CoreActivityActionBar getActionBarActivity();

	Drawable getLogoBackground();

	void changeRightFragment(CommonLogicFragment fragment);

	void changeLeftFragment(CommonLogicFragment fragment);

	void registerGcm();

	void setTouchModeToSlidingMenu(int touchMode);

	void addOnOpenMenuListener(SlidingMenu.OnOpenedListener listener);

	void setFullScreen();

	LccHolder getMeLccHolder();
}
