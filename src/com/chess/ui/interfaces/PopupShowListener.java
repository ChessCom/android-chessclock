package com.chess.ui.interfaces;

/**
 * Created by vm on 04.02.14.
 */

public interface PopupShowListener {

	void showPopupDialog(int titleId, String tag);

	void safeShowSinglePopupDialog(int titleId, String message);

	void showSinglePopupDialog(int titleId, int messageId);

	void setPositiveBtnId(int leftBtnId);
}
