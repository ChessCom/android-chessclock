package com.chess.ui.fragments.settings;

import android.os.Bundle;
import android.view.View;
import com.chess.R;
import com.chess.backend.entity.api.themes.ThemeItem;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.interfaces.FragmentParentFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.11.13
 * Time: 10:54
 */
public class SettingsThemeCustomizeFragmentTablet extends SettingsThemeCustomizeFragment {

	private FragmentParentFace parentFace;

	public SettingsThemeCustomizeFragmentTablet() {
	}

	public static SettingsThemeCustomizeFragmentTablet createInstance(FragmentParentFace parentFace, ThemeItem.Data themeItem) {
		SettingsThemeCustomizeFragmentTablet fragment = new SettingsThemeCustomizeFragmentTablet();
		fragment.parentFace = parentFace;

		Bundle bundle = new Bundle();
		bundle.putParcelable(THEME_ITEM, themeItem);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	protected void openFragment(BasePopupsFragment fragment) {
		parentFace.changeFragment(fragment);
	}

	@Override
	public void onClick(View view) {

		int id = view.getId();
		if (id == R.id.piecesView) {
			openFragment(new SettingsThemePiecesFragment(parentFace));
		} else if (id == R.id.boardView) {
			openFragment(new SettingsThemeBoardsFragment(parentFace));
		} else {
			super.onClick(view);
		}
	}
}
