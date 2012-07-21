package com.chess.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import com.chess.R;
import com.chess.model.PopupItem;
import com.chess.ui.interfaces.PopupDialogFace;

/**
 * @author Alexey Schekin (schekin@azoft.com)
 * @created 10.07.12
 * @modified 10.07.12
 */
public abstract class BasePopupDialogFragment extends DialogFragment implements View.OnClickListener  {

    protected static final String POPUP_ITEM = "popup item";

    protected PopupDialogFace listener;
    protected PopupItem popupItem;

    protected int buttonsNumber;
    protected boolean isShowed;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, 0);
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

    public void updatePopupItem(PopupItem popupItem) {
        this.popupItem = popupItem;
		if(!isVisible()){
			Bundle arguments = new Bundle();
			arguments.putSerializable(POPUP_ITEM, popupItem);
			setArguments(arguments);
		}
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(POPUP_ITEM, popupItem);
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

    @Override
    public void show(FragmentManager manager, String tag) {
        isShowed = true;
		FragmentTransaction ft = manager.beginTransaction();
		ft.add(this, tag);
		ft.commitAllowingStateLoss();
    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        isShowed = true;
        return super.show(transaction, tag);
    }

    @Override
    public void dismiss() {
        if(isShowed || isVisible())
            super.dismiss();
        isShowed = false;
    }
}
