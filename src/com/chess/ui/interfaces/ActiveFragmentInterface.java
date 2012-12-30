package com.chess.ui.interfaces;


import com.chess.ui.fragments.BaseFragment;

public interface ActiveFragmentInterface {

    boolean isLeftMenuActive();

	void toggleMenu();

	void closeMenu();

	void openFragment(BaseFragment fragment);

    void showPreviousFragment();

    void updateCurrentActiveFragment();

}
