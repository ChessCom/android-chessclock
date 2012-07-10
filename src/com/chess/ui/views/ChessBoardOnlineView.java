package com.chess.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import com.chess.R;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;

import java.util.Iterator;
import java.util.TreeSet;

public class ChessBoardOnlineView extends ChessBoardNetworkView {

	public ChessBoardOnlineView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected boolean need2ShowSubmitButtons() {
		String sharedKey;
		sharedKey = AppConstants.PREF_SHOW_SUBMIT_MOVE;
		return preferences.getBoolean(AppData.getUserName(getContext()) + sharedKey, false);
	}
}
