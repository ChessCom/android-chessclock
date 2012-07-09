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
public class PopupCustomViewFragment extends PopupDialogFragment implements View.OnClickListener {

	private FrameLayout customView;

	public static PopupCustomViewFragment newInstance(PopupItem popupItem, PopupDialogFace listener) {
		PopupCustomViewFragment frag = new PopupCustomViewFragment();
		Bundle arguments = new Bundle();
		arguments.putSerializable(POPUP_ITEM, popupItem);
		frag.setArguments(arguments);
		frag.listener = listener;
		return frag;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.popup_custom_dialog, container, false);

		customView = (FrameLayout) view.findViewById(R.id.customView);

		leftBtn = (Button)view.findViewById(R.id.positiveBtn);
		rightBtn = (Button)view.findViewById(R.id.negativeBtn);

		leftBtn.setOnClickListener(this);
		rightBtn.setOnClickListener(this);
		return view;
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
	public void onResume() {
		super.onResume();
		customView.addView(popupItem.getCustomView());

		leftBtn.setText(popupItem.getPositiveBtnId());
		rightBtn.setText(popupItem.getNegativeBtnId());
	}

}
