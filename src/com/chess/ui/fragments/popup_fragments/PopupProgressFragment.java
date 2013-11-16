package com.chess.ui.fragments.popup_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.model.PopupItem;


/**
 * @author alien_roger
 * @created at: 07.04.12 7:13
 */
public class PopupProgressFragment extends BasePopupDialogFragment {

    private boolean cancelable = true;

	public static PopupProgressFragment createInstance(PopupItem popupItem) {
		PopupProgressFragment frag = new PopupProgressFragment();
		Bundle arguments = new Bundle();
		arguments.putParcelable(POPUP_ITEM, popupItem);
		frag.setArguments(arguments);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(STYLE_NORMAL,  R.style.NoTitle);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.popup_progress, container, false);
	}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(!cancelable) {
			setCancelable(false);
		}
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
