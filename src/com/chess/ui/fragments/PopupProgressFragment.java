package com.chess.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.model.PopupItem;


/**
 * @author alien_roger
 * @created at: 07.04.12 7:13
 */
public class PopupProgressFragment extends DialogFragment {

	private PopupItem popupItem;
    private boolean cancelable;


    public static PopupProgressFragment newInstance(PopupItem popupItem) {
		PopupProgressFragment frag = new PopupProgressFragment();
		frag.popupItem = popupItem;
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NO_TITLE, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.popup_progress, container, false);

		TextView titleTxt = (TextView) view.findViewById(R.id.popupTitle);
		TextView messageTxt = (TextView) view.findViewById(R.id.popupMessage);

		messageTxt.setText(popupItem.getMessage(getActivity()));
		titleTxt.setText(popupItem.getTitle(getActivity()));

		return view;
	}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(!cancelable)
            getDialog().setCancelable(false);
    }

    public void setNotCancelable(){
        cancelable = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelable = true;
    }
}
