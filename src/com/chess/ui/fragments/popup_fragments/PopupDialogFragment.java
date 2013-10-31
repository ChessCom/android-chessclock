package com.chess.ui.fragments.popup_fragments;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.utilities.FontsHelper;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.RoboTextView;
import com.chess.statics.Symbol;
import com.chess.model.PopupItem;
import com.chess.ui.interfaces.PopupDialogFace;

/**
 * PopupDialogFragment class
 *
 * @author alien_roger
 * @created at: 07.04.12 7:13
 */
public class PopupDialogFragment extends BasePopupDialogFragment {

	private RoboTextView titleTxt;
	private RoboTextView messageTxt;
	private RoboButton positiveBtn;
	private RoboButton neutralBtn;
	private RoboButton negativeBtn;
	private boolean cancelableOnTouch;

	public static PopupDialogFragment createInstance(PopupItem popupItem, PopupDialogFace popupListener) {
		PopupDialogFragment frag = new PopupDialogFragment();
		frag.listener = popupListener;
		Bundle arguments = new Bundle();
		arguments.putParcelable(POPUP_ITEM, popupItem);
		frag.setArguments(arguments);
		return frag;
	}

	public static PopupDialogFragment createInstance(PopupItem popupItem) {
		PopupDialogFragment frag = new PopupDialogFragment();
		Bundle arguments = new Bundle();
		arguments.putParcelable(POPUP_ITEM, popupItem);
		frag.setArguments(arguments);
		return frag;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (listener == null) {
			listener = (PopupDialogFace) activity;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.popup_default, container, false);

		messageTxt = (RoboTextView) view.findViewById(R.id.popupMessage);
		titleTxt = (RoboTextView) view.findViewById(R.id.popupTitle);

		positiveBtn = (RoboButton) view.findViewById(R.id.positiveBtn);
		neutralBtn = (RoboButton) view.findViewById(R.id.neutralBtn);
		negativeBtn = (RoboButton) view.findViewById(R.id.negativeBtn);

		positiveBtn.setOnClickListener(this);
		neutralBtn.setOnClickListener(this);
		negativeBtn.setOnClickListener(this);

		neutralBtn.setVisibility(View.GONE);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (cancelableOnTouch) {
			getDialog().setCanceledOnTouchOutside(true);
		}

		String message = popupItem.getMessage(getActivity());
		if (!TextUtils.isEmpty(message)) {
			if (message.contains(Symbol.TAG)) {
				messageTxt.setText(Html.fromHtml(message));
			} else {
				messageTxt.setText(message);
			}
			messageTxt.setVisibility(View.VISIBLE);
		}
		titleTxt.setText(popupItem.getTitle(getActivity()));

		buttonsNumber = popupItem.getButtons();
		switch (buttonsNumber) {
			case 1:
				negativeBtn.setVisibility(View.GONE);
				break;
			case 3:
				if (popupItem.getNeutralBtnId() != R.string.ic_play) {
					neutralBtn.setFont(FontsHelper.BOLD_FONT);
					neutralBtn.setTextSize(getResources().getDimensionPixelSize(R.dimen.default_button_text_size) / density);
				}
				neutralBtn.setVisibility(View.VISIBLE);
				neutralBtn.setText(popupItem.getNeutralBtnId());
				break;
		}

		switch (popupItem.getButtonToShow()) {
			case PopupItem.NEGATIVE:
				negativeBtn.setVisibility(View.VISIBLE);
				neutralBtn.setVisibility(View.GONE);
				positiveBtn.setVisibility(View.GONE);
				break;
			case PopupItem.NEUTRAL:
				negativeBtn.setVisibility(View.GONE);
				neutralBtn.setVisibility(View.VISIBLE);
				positiveBtn.setVisibility(View.GONE);
				break;
			case PopupItem.POSITIVE:
				negativeBtn.setVisibility(View.GONE);
				neutralBtn.setVisibility(View.GONE);
				positiveBtn.setVisibility(View.VISIBLE);
				break;
			case PopupItem.NEGATIVE_GREEN:
				negativeBtn.setDrawableStyle(R.style.Rect_Bottom_Middle_Green);
				negativeBtn.setVisibility(View.VISIBLE);
				neutralBtn.setVisibility(View.GONE);
				positiveBtn.setVisibility(View.GONE);
				break;
		}

		if (popupItem.getPositiveBtnId() != R.string.ic_check) {
			positiveBtn.setFont(FontsHelper.BOLD_FONT);
			positiveBtn.setTextSize(getResources().getDimensionPixelSize(R.dimen.default_button_text_size) / density);
		}

		if (popupItem.getNegativeBtnId() != R.string.ic_close) {
			negativeBtn.setFont(FontsHelper.BOLD_FONT);
			negativeBtn.setTextSize(getResources().getDimensionPixelSize(R.dimen.default_button_text_size) / density);
		}

		positiveBtn.setText(popupItem.getPositiveBtnId());
		negativeBtn.setText(popupItem.getNegativeBtnId());
	}

	public void setCancelableOnTouch(boolean cancelableOnTouch) {
		this.cancelableOnTouch = cancelableOnTouch;
	}
}
