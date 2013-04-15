package com.chess.ui.fragments.popup_fragments;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import com.chess.R;
import com.chess.model.PopupItem;
import com.chess.ui.interfaces.PopupDialogFace;


/**
 * @author alien_roger
 * @created at: 07.04.12 7:13
 */
public class PopupCustomViewFragment extends BasePopupDialogFragment {

	protected Button leftBtn;
	protected Button rightBtn;
	protected FrameLayout customView;

	public static PopupCustomViewFragment newInstance(PopupItem popupItem) {
		PopupCustomViewFragment frag = new PopupCustomViewFragment();
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
		setRetainInstance(false);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.popup_custom_dialog, container, false);

		customView = (FrameLayout) view.findViewById(R.id.customView);

		leftBtn = (Button) view.findViewById(R.id.positiveBtn);
		rightBtn = (Button) view.findViewById(R.id.negativeBtn);

		leftBtn.setOnClickListener(this);
		rightBtn.setOnClickListener(this);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		buttonsNumber = popupItem.getButtons();
		if (buttonsNumber == 0) {
			leftBtn.setVisibility(View.GONE);
			rightBtn.setVisibility(View.GONE);
		} else if (buttonsNumber == 1) {
			rightBtn.setVisibility(View.GONE);
		}

//		customView.removeAllViews(); // still haveIllegal exception  after echess game finished
		// we need remove parent from child's view  see note below. Should work
		removeParent();
		if (popupItem.getCustomView() == null) {
			getDialog().dismiss();
			return;
		}
		customView.addView(popupItem.getCustomView());

		leftBtn.setText(popupItem.getPositiveBtnId());
		rightBtn.setText(popupItem.getNegativeBtnId());
	}

	/*   // Note!
		if (child.getParent() != null) {
            throw new IllegalStateException("The specified child already has a parent. " +
                    "You must call removeView() on the child's parent first.");
        }
	 */

	@Override
	public void onPause() {
		super.onPause();
		removeParent();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		customView = null;
	}

	private void removeParent() {
		final View view = popupItem.getCustomView();
		if (view == null) {
			return;
		}

		ViewGroup childParent = (ViewGroup) view.getParent();
		if (childParent != null)
			childParent.removeAllViews();
		customView.removeAllViews();
		customView.removeView(view);
	}
}
