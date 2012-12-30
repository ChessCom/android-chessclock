package com.chess.ui.popup_fragments;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.model.PopupItem;
import com.chess.ui.interfaces.PopupDialogFace;


/**
 * PopupDialogFragment class
 *
 * @author alien_roger
 * @created at: 07.04.12 7:13
 */
public class PopupDialogFragment extends BasePopupDialogFragment {

	private TextView titleTxt;
	private TextView messageTxt;
	private Button leftBtn;
	private Button neutralBtn;
	private Button rightBtn;

	public static PopupDialogFragment newInstance(PopupItem popupItem) {
		PopupDialogFragment frag = new PopupDialogFragment();
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.popup_default, container, false);

		messageTxt = (TextView) view.findViewById(R.id.popupMessage);
		titleTxt = (TextView) view.findViewById(R.id.popupTitle);

		leftBtn = (Button) view.findViewById(R.id.positiveBtn);
		neutralBtn = (Button) view.findViewById(R.id.neutralBtn);
		rightBtn = (Button) view.findViewById(R.id.negativeBtn);

		leftBtn.setOnClickListener(this);
		neutralBtn.setOnClickListener(this);
		rightBtn.setOnClickListener(this);

		neutralBtn.setVisibility(View.GONE);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();

		String message = popupItem.getMessage(getActivity());
		if (message.contains(StaticData.SYMBOL_TAG)) {
			messageTxt.setText(Html.fromHtml(message));
		} else {
			messageTxt.setText(message);
		}
		messageTxt.setVisibility(View.VISIBLE);
		titleTxt.setText(popupItem.getTitle(getActivity()));

        buttonsNumber = popupItem.getButtons();
		switch (buttonsNumber) {
			case 1:
				rightBtn.setVisibility(View.GONE);
				break;
			case 3:
				neutralBtn.setVisibility(View.VISIBLE);
				neutralBtn.setText(popupItem.getNeutralBtnId());
				break;
		}
		leftBtn.setText(popupItem.getPositiveBtnId());
		rightBtn.setText(popupItem.getNegativeBtnId());

	}

}
