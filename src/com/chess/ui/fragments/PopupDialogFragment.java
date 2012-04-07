package com.chess.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.chess.R;
import com.chess.model.PopupItem;
import com.chess.ui.interfaces.PopupDialogFace;

/**
 * PopupDialogFragment class
 *
 * @author alien_roger
 * @created at: 07.04.12 7:13
 */
public class PopupDialogFragment extends DialogFragment implements View.OnClickListener {
	private PopupDialogFace listener;
	private PopupItem popupItem;
	private TextView titleTxt;
	private TextView messageTxt;
	private Button leftBtn;
	private Button rightBtn;

	public static PopupDialogFragment newInstance(PopupItem popupItem, PopupDialogFace listener) {
		PopupDialogFragment frag = new PopupDialogFragment();
		frag.popupItem = popupItem;
		frag.listener = listener;
		return frag;
	}



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setStyle(STYLE_NO_TITLE, 0);
		setStyle(STYLE_NO_FRAME, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.default_popup, container, false);

		messageTxt = (TextView)v.findViewById(R.id.popupMessage);
		titleTxt = (TextView)v.findViewById(R.id.popupTitle);
		leftBtn = (Button)v.findViewById(R.id.okBtn);
		rightBtn = (Button)v.findViewById(R.id.cancelBtn);

		if(popupItem.getMessageId() == 0)
			messageTxt.setText(popupItem.getMessage());
		else
			messageTxt.setText(popupItem.getMessageId());

		if(popupItem.getMessageId() == 0)
			titleTxt.setText(popupItem.getTitle());
		else
			titleTxt.setText(popupItem.getTitleId());

		leftBtn.setText(popupItem.getLeftBtnId());
		rightBtn.setText(popupItem.getRightBtnId());

		leftBtn.setOnClickListener(this);
		rightBtn.setOnClickListener(this);
		return v;
	}

	@Override
	public void onClick(View view) {
		if(view.getId() == R.id.okBtn){
			listener.onLeftBtnClick(this);
		}else if(view.getId() == R.id.cancelBtn){
			listener.onRightBtnClick(this);
		}
	}

	public void updatePopupItem(PopupItem popupItem) {
		this.popupItem = popupItem;
	}
}
