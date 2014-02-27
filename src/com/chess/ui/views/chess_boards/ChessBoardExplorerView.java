package com.chess.ui.views.chess_boards;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.chess.statics.StaticData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.interfaces.game_ui.GameFace;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.09.13
 * Time: 10:23
 */
public class ChessBoardExplorerView extends ChessBoardBaseView {

	private GameFace gameFace;

	public ChessBoardExplorerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setGameUiFace(GameFace gameFace) {
		super.setGameFace(gameFace);

		this.gameFace = gameFace;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (useTouchTimer) { // start count before next touch
			handler.postDelayed(checkUserIsActive, StaticData.WAKE_SCREEN_TIMEOUT);
			userActive = true;
		}

		if (squareSize == 0) {
			return super.onTouchEvent(event);
		}

		trackTouchEvent = false;

		return super.onTouchEvent(event);
	}

	@Override
	public void promote(int promote, int file, int rank) {
		boolean found = false;
		Move move = null;
		List<Move> moves = getBoardFace().generateLegalMoves();
		for (Move move1 : moves) {
			move = move1;
			if (move.from == fromSquare && move.to == toSquare && move.promote == promote) {
				found = true;
				break;
			}
		}

		boolean moveMade = false;
		MoveAnimator moveAnimator = null;
		if (found) {
			moveAnimator = new MoveAnimator(move, true);
			moveMade = getBoardFace().makeMove(move);
		}
		if (moveMade) {
			moveAnimator.setForceCompEngine(true); // TODO @engine: probably postpone afterUserMove() only for vs comp mode
			setMoveAnimator(moveAnimator);
			//afterUserMove(); //
		} else if (getBoardFace().getPiece(toSquare) != ChessBoard.EMPTY
				&& getBoardFace().getSide() == getBoardFace().getColor(toSquare)) {
			pieceSelected = true;
			firstClick = false;
			fromSquare = ChessBoard.getPositionIndex(file, rank, getBoardFace().isReside());
		}
		invalidateMe();
	}

	@Override
	protected void afterUserMove() {
		super.afterUserMove();

		getBoardFace().setMovesCount(getBoardFace().getPly());
		gameFace.invalidateGameScreen();
		gameFace.updateAfterMove();
	}

}