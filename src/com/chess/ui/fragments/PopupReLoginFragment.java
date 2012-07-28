package com.chess.ui.fragments;

import android.os.Bundle;
import com.chess.model.PopupItem;


/**
 * @author alien_roger
 * @created at: 07.04.12 7:13
 */
public class PopupReLoginFragment extends PopupCustomViewFragment {

	public static PopupReLoginFragment newInstance(PopupItem popupItem) {
		PopupReLoginFragment frag = new PopupReLoginFragment();
		Bundle arguments = new Bundle();
		arguments.putSerializable(POPUP_ITEM, popupItem);
		frag.setArguments(arguments);
		return frag;
	}

}