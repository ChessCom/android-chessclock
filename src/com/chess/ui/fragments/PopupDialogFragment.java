package com.chess.ui.fragments;

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
    private Button middleBtn;
    private Button rightBtn;

	public static PopupDialogFragment newInstance(PopupItem popupItem, PopupDialogFace listener) {
        PopupDialogFragment frag = new PopupDialogFragment();
		Bundle arguments = new Bundle();
		arguments.putSerializable(POPUP_ITEM, popupItem);
		frag.setArguments(arguments);
		frag.listener = listener;
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.popup_default, container, false);

        messageTxt = (TextView)view.findViewById(R.id.popupMessage);
        titleTxt = (TextView)view.findViewById(R.id.popupTitle);

        leftBtn = (Button)view.findViewById(R.id.positiveBtn);
        middleBtn = (Button)view.findViewById(R.id.middleBtn);
        rightBtn = (Button)view.findViewById(R.id.negativeBtn);

        leftBtn.setOnClickListener(this);
        middleBtn.setOnClickListener(this);
        rightBtn.setOnClickListener(this);

        middleBtn.setVisibility(View.GONE);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        switch (buttonsNumber){
            case 1:
                rightBtn.setVisibility(View.GONE);
                break;
            case 3:
                middleBtn.setVisibility(View.VISIBLE);
                break;
        }
    }



	@Override
	public void onResume() {
		super.onResume();
		String message = popupItem.getMessage(getActivity());
		if(message.contains(StaticData.SYMBOL_TAG)){
			messageTxt.setText(Html.fromHtml(message));
		}else{
			messageTxt.setText(message);
		}
		messageTxt.setVisibility(View.VISIBLE);
		titleTxt.setText(popupItem.getTitle(getActivity()));

		leftBtn.setText(popupItem.getPositiveBtnId());
		if(buttonsNumber == 3)
			middleBtn.setText(popupItem.getNeutralBtnId());
		rightBtn.setText(popupItem.getNegativeBtnId());
	}

}
