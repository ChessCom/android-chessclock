package com.chess.ui.fragments;

import android.app.Activity;
import com.chess.ui.activities.CoreActivityActionBar;
import com.chess.ui.interfaces.ActiveFragmentInterface;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 02.01.13
 * Time: 10:18
 */
public class CommonLogicFragment extends BasePopupsFragment {

	private ActiveFragmentInterface activityFace;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		activityFace = (ActiveFragmentInterface) activity;
	}

	protected CoreActivityActionBar getInstance() {
		return activityFace.getActionBarActivity();
	}

	protected ActiveFragmentInterface getActivityFace (){
		return activityFace;
	}

}
