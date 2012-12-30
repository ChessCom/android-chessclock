package com.chess.ui.fragments;

import android.app.Activity;
import android.support.v4.app.Fragment;
import com.chess.ui.interfaces.ActiveFragmentInterface;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 15:22
 */
public class BaseFragment extends Fragment {

	private ActiveFragmentInterface activityFace;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		activityFace = (ActiveFragmentInterface) activity;
	}

	protected ActiveFragmentInterface getActivityFace() {
		return activityFace;
	}

}
