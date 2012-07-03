package com.chess.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
public class PopupDialogFragment extends DialogFragment implements View.OnClickListener {

	private static final String POPUP_ITEM = "popup item";

    private PopupDialogFace listener;
    private PopupItem popupItem;
    private TextView titleTxt;
    private TextView messageTxt;
    private Button leftBtn;
    private Button middleBtn;
    private Button rightBtn;
    private int buttonsNumber;

    public static PopupDialogFragment newInstance(PopupItem popupItem, PopupDialogFace listener) {
        PopupDialogFragment frag = new PopupDialogFragment();
		Bundle arguments = new Bundle();
		arguments.putSerializable(POPUP_ITEM, popupItem);
		frag.setArguments(arguments);
		frag.listener = listener;
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, 0);
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
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if(getArguments() != null){
			popupItem = (PopupItem) getArguments().getSerializable(POPUP_ITEM);
		}else{
			popupItem = (PopupItem) savedInstanceState.getSerializable(POPUP_ITEM);
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
		titleTxt.setText(popupItem.getTitle(getActivity()));

		leftBtn.setText(popupItem.getPositiveBtnId());
		middleBtn.setText(popupItem.getNegativeBtnId());
		rightBtn.setText(popupItem.getNegativeBtnId());
	}

	@Override
    public void onClick(View view) {
        if(listener == null){  // TODO handle NPE
            dismiss();
            return;
        }

        if(view.getId() == R.id.positiveBtn){
            listener.onPositiveBtnClick(this);
        }else if(view.getId() == R.id.negativeBtn){
            listener.onNegativeBtnClick(this);
        }
    }

    public void updatePopupItem(PopupItem popupItem) {
        this.popupItem = popupItem;
		Bundle arguments = new Bundle();
		arguments.putSerializable(POPUP_ITEM, popupItem);
		setArguments(arguments);
    }

    public void setButtons(int buttonsNumber){
        this.buttonsNumber = buttonsNumber;
    }
}
