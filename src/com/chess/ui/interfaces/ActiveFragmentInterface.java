package com.chess.ui.interfaces;


import com.chess.ui.activities.CoreActivityActionBar;
import com.chess.ui.fragments.BasePopupsFragment;

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
}
