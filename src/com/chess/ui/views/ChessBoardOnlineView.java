package com.chess.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;

public class ChessBoardOnlineView extends ChessBoardNetworkView {

	public ChessBoardOnlineView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected boolean need2ShowSubmitButtons() {
		String sharedKey = AppConstants.PREF_SHOW_SUBMIT_MOVE;
		return preferences.getBoolean(AppData.getUserName(getContext()) + sharedKey, true);
	}
}
