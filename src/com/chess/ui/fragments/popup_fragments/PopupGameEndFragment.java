package com.chess.ui.fragments.popup_fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.model.PopupItem;
import com.chess.ui.interfaces.PopupDialogFace;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.09.13
 * Time: 11:28
 */
public class PopupGameEndFragment extends BasePopupDialogFragment {
	private int size;

//	protected FrameLayout customView;

	public static PopupGameEndFragment createInstance(PopupItem popupItem) {
		PopupGameEndFragment frag = new PopupGameEndFragment();
		Bundle arguments = new Bundle();
		arguments.putParcelable(POPUP_ITEM, popupItem);
		frag.setArguments(arguments);
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		listener = (PopupDialogFace) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_TITLE, 0);

		size = getResources().getDisplayMetrics().widthPixels;
//		density = getResources().getDisplayMetrics().density;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return popupItem.getCustomView();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(size, size);
		view.setLayoutParams(params);
	}

	@Override
	public void onResume() {
		super.onResume();

		getDialog().setCanceledOnTouchOutside(true); // always cancel on touchOutside


		// we need remove parent from child's view  see note below. Should work
//		removeParent();
//		if (popupItem.getCustomView() == null) {
//			getDialog().dismiss();
//			return;
//		}
//		customView.addView(popupItem.getCustomView());
	}

	@Override
	public void onPause() {
		super.onPause();
//		removeParent();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
//		customView = null;
	}

//	private void removeParent(){
//		final View view = popupItem.getCustomView();
//		if (view == null) {
//			return;
//		}
//
//		ViewGroup childParent = (ViewGroup) view.getParent();
//		if(childParent != null) {
//			childParent.removeAllViews();
//		}
//		customView.removeAllViews();
//		customView.removeView(view);
//	}
}