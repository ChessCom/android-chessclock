package com.chess.ui.fragments.settings;

import com.chess.ui.interfaces.FragmentParentFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.11.13
 * Time: 10:20
 */
public class SettingsThemeFragmentTablet extends SettingsThemeFragment {

	private FragmentParentFace parentFace;

	public SettingsThemeFragmentTablet() {}

	public static SettingsThemeFragmentTablet createInstance(FragmentParentFace parentFace) {
		SettingsThemeFragmentTablet fragment = new SettingsThemeFragmentTablet();
		fragment.parentFace = parentFace;
		return fragment;
	}

	@Override
	protected void openCustomizeFragment() {
		if (parentFace == null) {
			return;
		}
		parentFace.changeFragment(SettingsThemeCustomizeFragmentTablet.createInstance(parentFace, currentThemeItem));
	}
}
