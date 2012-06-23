package com.chess.ui.views;

import android.content.Context;
import android.util.AttributeSet;
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
//                gameActivityFace.update(GameBaseActivity.CALLBACK_REPAINT_UI);
				gameActivityFace.invalidateGameScreen();

				if (boardFace.getSide() == ChessBoard.LIGHT)
					message = "0 - 1 Black mates";
				else
					message = "1 - 0 White mates";
			} else
				message = "0 - 0 Stalemate";
		} else if (boardFace.reps() == 3 )
			message = "1/2 - 1/2 Draw by repetition";

		if (message != null) {
			finished = true;

//			gameActivityFace.pushToast(message);

//			Intent intent = new Intent(IntentConstants.ACTION_SHOW_GAME_END_POPUP);
//			intent.putExtra(AppConstants.MESSAGE, "GAME OVER: " + message);
//			intent.putExtra(AppConstants.FINISHABLE, false);
//			mainApp.sendBroadcast(intent);
			gameActivityFace.onGameOver(message, false);

			return true;
		}

		if (boardFace.inCheck(boardFace.getSide())) {
			boardFace.getHistDat()[boardFace.getHply() - 1].notation += "+";
//            gameActivityFace.update(GameBaseActivity.CALLBACK_REPAINT_UI);
			gameActivityFace.invalidateGameScreen();

			gameActivityFace.onCheck();
		}
		return false;
	}



}
