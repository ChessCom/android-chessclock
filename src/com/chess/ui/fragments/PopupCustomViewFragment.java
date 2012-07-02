package com.chess.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
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
public class PopupCustomViewFragment extends DialogFragment implements View.OnClickListener {

	private static final String POPUP_ITEM = "popup item";
	private PopupDialogFace listener;
	private PopupItem popupItem;
	private Button leftBtn;
	private Button rightBtn;
    private int buttonsNumber;

    public static PopupCustomViewFragment newInstance(PopupItem popupItem, PopupDialogFace listener) {
		PopupCustomViewFragment frag = new PopupCustomViewFragment();
		Bundle arguments = new Bundle();
		arguments.putSerializable(POPUP_ITEM, popupItem);
		frag.setArguments(arguments);
		frag.listener = listener;
		return frag;
	}



	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_TITLE, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.popup_custom_dialog, container, false);

		((FrameLayout)v.findViewById(R.id.customView)).addView(popupItem.getCustomView());

		leftBtn = (Button)v.findViewById(R.id.positiveBtn);
		rightBtn = (Button)v.findViewById(R.id.negativeBtn);

		leftBtn.setOnClickListener(this);
		rightBtn.setOnClickListener(this);
		return v;
	}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(buttonsNumber == 0){
			leftBtn.setVisibility(View.GONE);
			rightBtn.setVisibility(View.GONE);
		}else if(buttonsNumber == 1){
            rightBtn.setVisibility(View.GONE);
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
		leftBtn.setText(popupItem.getPositiveBtnId());
		rightBtn.setText(popupItem.getNegativeBtnId());
	}

	@Override
	public void onClick(View view) {
		if(view.getId() == R.id.positiveBtn){
			listener.onPositiveBtnClick(this);
		}else if(view.getId() == R.id.negativeBtn){
			listener.onNegativeBtnClick(this);
		}
	}

    public void setButtons(int buttonsNumber){
        this.buttonsNumber = buttonsNumber;
    }
}
