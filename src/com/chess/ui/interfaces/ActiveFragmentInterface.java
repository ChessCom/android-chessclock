package com.chess.ui.interfaces;


import com.chess.backend.statics.AppData;
import com.chess.ui.activities.CoreActivityActionBar;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.fragments.CommonLogicFragment;
import com.slidingmenu.lib.SlidingMenu;

public interface ActiveFragmentInterface {

	void setCustomActionBarViewId(int viewId);

	void toggleLeftMenu();

	void toggleRightMenu();

	void openFragment(BasePopupsFragment fragment);

	void switchFragment(BasePopupsFragment fragment);

	void openFragment(BasePopupsFragment fragment, int code);

	void switchFragment(BasePopupsFragment fragment, int code);

    void showPreviousFragment();

    void setBadgeValueForId(int menuId, int value);

	CoreActivityActionBar getActionBarActivity();

	void changeRightFragment(CommonLogicFragment fragment);

	void changeLeftFragment(CommonLogicFragment fragment);

	void registerGcm();

	void unRegisterGcm();

	void setTouchModeToSlidingMenu(int touchMode);

	void addOnOpenMenuListener(SlidingMenu.OnOpenedListener listener);

	void removeOnOpenMenuListener(SlidingMenu.OnOpenedListener listener);

	void setFullScreen();

	void clearFragmentStack();

	void updateTitle(CharSequence title);

	void setTitlePadding(int padding);

	void showActionBar(boolean show);

	void setMainBackground(int drawableThemeId);

	void setMainBackground(String drawableThemeId);

	void showActionMenu(int menuId, boolean show);

	void updateActionBarIcons();

	AppData getMeAppData();

	String getMeUsername();

	String getMeUserToken();
}
