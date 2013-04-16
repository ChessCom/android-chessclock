package com.chess.ui.fragments.sign_in;

import android.os.Bundle;
import android.view.View;
import com.chess.R;
import com.chess.ui.fragments.CommonLogicFragment;


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

		if (JELLY_BEAN_PLUS_API) {
			view.findViewById(R.id.mainFrame).setBackground(getActivityFace().getLogoBackground());
		} else {
			view.findViewById(R.id.mainFrame).setBackgroundDrawable(getActivityFace().getLogoBackground());
		}
		getActivityFace().getLogoBackground().invalidateSelf();
	}
}
