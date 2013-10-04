package com.chess.ui.fragments.popup_fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.10.13
 * Time: 12:48
 */
public abstract class SimplePopupDialogFragment extends DialogFragment {

	protected boolean isShowed;
	protected boolean isPaused;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_TITLE, 0);
	}

	@Override
	public void onResume() {
		super.onResume();
		isPaused = false;
	}

	@Override
	public void onPause() {
		super.onPause();
		isPaused = true;
	}

	@Override
	public void show(FragmentManager manager, String tag) {
		isShowed = true;
		FragmentTransaction ft = manager.beginTransaction();
		ft.add(this, tag);
		try {
			ft.commitAllowingStateLoss();
		} catch (IllegalStateException ex) {
			Log.e("FragmentShow", "Fragment was showed when activity is dead " + ex.toString());
		}
	}

	@Override
	public int show(FragmentTransaction transaction, String tag) {
		isShowed = true;
		return super.show(transaction, tag);
	}

	@Override
	public void dismiss() {
		try {

			if (getDialog() != null) {
				getDialog().dismiss();
			}
			if (isShowed || isVisible()) {
				if (getDialog() != null) {
					getDialog().dismiss();
				}
				super.dismiss();
			}
		} catch (IllegalStateException ex) {
			if (ex.getMessage() != null && ex.getMessage().equals("Can not perform this action after onSaveInstanceState")) {
				Log.e("SimplePopupFragment", "caught - " + ex.getMessage());
			}
		}
		isShowed = false;
	}

	@Override
	public void onDestroyView() {
		if (getDialog() != null && getRetainInstance())
			getDialog().setOnDismissListener(null);
		super.onDestroyView();
	}
}
