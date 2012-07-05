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


	protected boolean isGameOver() {
		// Check available moves
		TreeSet<Move> validMoves = boardFace.gen();

		Iterator<Move> i = validMoves.iterator();
		boolean found = false;
		while (i.hasNext()) {   // compute available moves
			if (boardFace.makeMove(i.next(), false)) {
				boardFace.takeBack();
				found = true;
				break;
			}
		}
		String message = null;
		if (!found) {
			if (boardFace.inCheck(boardFace.getSide())) {
				boardFace.getHistDat()[boardFace.getHply() - 1].notation += "#";
				gameActivityFace.invalidateGameScreen();

				if (boardFace.getSide() == ChessBoard.LIGHT)
					message = getResources().getString(R.string.black_wins);
				else
					message = getResources().getString(R.string.white_wins);
			} else
				message = getResources().getString(R.string.draw_by_stalemate);
		} else if (boardFace.reps() == 3 )
			message = getResources().getString(R.string.draw_by_3fold_repetition);

		if (message != null) {
			finished = true;

			gameActivityFace.onGameOver(message, false);

			return true;
		}

		if (boardFace.inCheck(boardFace.getSide())) {
			boardFace.getHistDat()[boardFace.getHply() - 1].notation += "+";
			gameActivityFace.invalidateGameScreen();

			gameActivityFace.onCheck();
		}
		return false;
	}



}
