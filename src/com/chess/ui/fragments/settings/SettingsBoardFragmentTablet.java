package com.chess.ui.fragments.settings;

import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.interfaces.FragmentParentFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.11.13
 * Time: 10:46
 */
public class SettingsBoardFragmentTablet extends SettingsBoardFragment {

	private FragmentParentFace parentFace;

	public SettingsBoardFragmentTablet() {}

	public SettingsBoardFragmentTablet(FragmentParentFace parentFace) {
		this.parentFace = parentFace;
	}

	@Override
	protected void openFragment(BasePopupsFragment fragment) {
		parentFace.changeFragment(fragment);
	}
}

