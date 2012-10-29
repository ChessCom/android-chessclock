package com.chess.ui.interfaces;

import android.support.v4.app.DialogFragment;

/**
 * PopupDialogFace class
 *
 * @author alien_roger
 * @created at: 07.04.12 7:36
 */
public interface PopupDialogFace {

	void onPositiveBtnClick(DialogFragment fragment);

    void onNeutralBtnCLick(DialogFragment fragment);

	void onNegativeBtnClick(DialogFragment fragment);
}
