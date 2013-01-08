package com.chess.ui.fragments;

import android.os.Bundle;
import android.view.View;
import com.chess.R;
import com.chess.ui.views.LogoBackgroundDrawable;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.01.13
 * Time: 9:16
 */
public class ProfileSetupsFragment extends CommonLogicFragment {

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		showActionBar(false);

		if (HONEYCOMB_PLUS_API) {
			view.findViewById(R.id.mainFrame).setBackground(new LogoBackgroundDrawable(getActivity()));
		} else {
			view.findViewById(R.id.mainFrame).setBackgroundDrawable(new LogoBackgroundDrawable(getActivity()));
		}
	}
}
