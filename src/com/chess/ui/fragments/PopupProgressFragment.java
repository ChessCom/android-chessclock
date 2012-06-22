package com.chess.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.model.PopupItem;


/**
 * @author alien_roger
 * @created at: 07.04.12 7:13
 */
public class PopupProgressFragment extends DialogFragment {

	private static final String POPUP_ITEM = "popup item";

	private PopupItem popupItem;
    private boolean cancelable;
	private TextView titleTxt;
	private TextView messageTxt;


	public static PopupProgressFragment newInstance(PopupItem popupItem) {
		PopupProgressFragment frag = new PopupProgressFragment();
		Bundle arguments = new Bundle();
		arguments.putSerializable(POPUP_ITEM, popupItem);
		frag.setArguments(arguments);
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

		titleTxt = (TextView) view.findViewById(R.id.popupTitle);
		messageTxt = (TextView) view.findViewById(R.id.popupMessage);
		return view;
	}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(!cancelable)
            getDialog().setCancelable(false);
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
		if(!message.equals(StaticData.SYMBOL_EMPTY)){
			messageTxt.setVisibility(View.VISIBLE);
			messageTxt.setText(message);

		}
		titleTxt.setText(popupItem.getTitle(getActivity()));
	}

	public void setNotCancelable(){
        cancelable = false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cancelable = true;
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(POPUP_ITEM, popupItem);
	}
}
