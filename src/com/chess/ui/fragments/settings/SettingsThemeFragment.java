package com.chess.ui.fragments.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.ui.fragments.CommonLogicFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 20.05.13
 * Time: 11:23
 */
public class SettingsThemeFragment extends CommonLogicFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_settings_themes_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.greenBtn).setOnClickListener(this);
		view.findViewById(R.id.tigersBtn).setOnClickListener(this);
		view.findViewById(R.id.aguaBtn).setOnClickListener(this);
		view.findViewById(R.id.charcoalBtn).setOnClickListener(this);
		view.findViewById(R.id.greyBtn).setOnClickListener(this);
		view.findViewById(R.id.blackwoodBtn).setOnClickListener(this);

	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.greenBtn) {
			getActivityFace().setMainBackground(R.drawable.img_theme_green_felt);
		} else if (id == R.id.tigersBtn) {
			getActivityFace().setMainBackground(R.drawable.img_theme_fighting_tigers);
		} else if (id == R.id.aguaBtn) {
			getActivityFace().setMainBackground(R.drawable.img_theme_agua);
		} else if (id == R.id.charcoalBtn) {
			getActivityFace().setMainBackground(R.drawable.img_theme_charcoal);
		} else if (id == R.id.greyBtn) {
			getActivityFace().setMainBackground(R.drawable.img_theme_grey_felt);
		} else if (id == R.id.blackwoodBtn) {
			getActivityFace().setMainBackground(R.drawable.img_theme_blackwood);

		}
	}
}
