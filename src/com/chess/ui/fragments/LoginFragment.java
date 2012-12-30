package com.chess.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 15:21
 */
public class LoginFragment extends BaseFragment {

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);    //To change body of overridden methods use File | Settings | File Templates.
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_login_screen, container, false);
	}
}
