package com.chess.ui.interfaces;


import com.chess.ui.fragments.BaseFragment;

public interface ActiveFragmentInterface {

    boolean isMenuActive(int code);

	void toggleMenu(int code);

	void closeMenu(int code);

	void openFragment(BaseFragment fragment);

	void switchFragment(BaseFragment fragment);

	void openFragment(BaseFragment fragment, int code);

	void switchFragment(BaseFragment fragment, int code);

    void showPreviousFragment();

    void updateCurrentActiveFragment();

}
