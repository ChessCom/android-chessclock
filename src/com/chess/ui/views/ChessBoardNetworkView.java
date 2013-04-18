package com.chess.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.interfaces.GameActivityFace;

import java.util.Iterator;
import java.util.TreeSet;

public abstract class ChessBoardNetworkView extends ChessBoardBaseView {

	private String whiteUserName;
	private String blackUserName;

	public ChessBoardNetworkView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected abstract boolean need2ShowSubmitButtons();

	@Override
	public void setGameActivityFace(GameActivityFace gameActivityFace) {
		super.setGameActivityFace(gameActivityFace);

		updatePlayerNames(gameActivityFace.getWhitePlayerName(), gameActivityFace.getBlackPlayerName());
	}

	@Override
	public void afterMove() {
		boardFace.setMovesCount(boardFace.getHply());
		gameActivityFace.invalidateGameScreen();

		if (!boardFace.isAnalysis()) {
			if (need2ShowSubmitButtons()) {
				boardFace.setSubmit(true);
				gameActivityFace.showSubmitButtonsLay(true);
			} else {
				gameActivityFace.updateAfterMove();
			}
		}

		isGameOver();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.setDrawFilter(drawFilter);
		super.onDraw(canvas);

		drawBoard(canvas);

		drawPieces(canvas);

		drawCoordinates(canvas);

		drawHighlight(canvas);

		drawDragPosition(canvas);

		drawTrackballDrag(canvas);

		drawCapturedPieces();
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		if (useTouchTimer) { // start count before next touch
			handler.postDelayed(checkUserIsActive, StaticData.WAKE_SCREEN_TIMEOUT);
			userActive = true;
		}

		float sens = 0.3f;
		if (event.getAction() == MotionEvent.ACTION_MOVE) {
			track = true;
			if (event.getX() > sens)
				trackX += square;
			else if (event.getX() < -sens)
				trackX -= square;
			if (event.getY() > sens)
				trackY += square;
			else if (event.getY() < -sens)
				trackY -= square;
			if (trackX < 0)
				trackX = 0;
			if (trackY < 0)
				trackY = 0;
			if (trackX > 7 * square)
				trackX = 7 * square;
			if (trackY > 7 * square)
				trackY = 7 * square;
			invalidate();
		} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
			int col = (trackX - trackX % square) / square;
			int row = (trackY - trackY % square) / square;

			if (firstclick) {
				from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
				if (boardFace.getPieces()[from] != 6 && boardFace.getSide() == boardFace.getColor()[from]) {
					pieceSelected = true;
					firstclick = false;
					invalidate();
				}
			} else {
				to = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
				pieceSelected = false;
				firstclick = true;
				boolean found = false;

				TreeSet<Move> moves = boardFace.gen();
				Iterator<Move> moveIterator = moves.iterator();

				Move move = null;
				while (moveIterator.hasNext()) {
					move = moveIterator.next();
					if (move.from == from && move.to == to) {
						found = true;
						break;
					}
				}
				if ((((to < 8) && (boardFace.getSide() == ChessBoard.LIGHT)) ||
						((to > 55) && (boardFace.getSide() == ChessBoard.DARK))) &&
						(boardFace.getPieces()[from] == ChessBoard.PAWN) && found) {

					gameActivityFace.showChoosePieceDialog(col, row);
					return true;
				}
				if (found && boardFace.makeMove(move)) {
					invalidate();
					afterMove();
				} else if (boardFace.getPieces()[to] != 6 && boardFace.getSide() == boardFace.getColor()[to]) {
					pieceSelected = true;
					firstclick = false;
					from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
					invalidate();
				} else {
					invalidate();
				}
			}
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (useTouchTimer) { // start count before next touch
			handler.postDelayed(checkUserIsActive, StaticData.WAKE_SCREEN_TIMEOUT);
			userActive = true;
		}

		if (square == 0) {
			return processTouchEvent(event);
		}

        if (isLocked()) {
			return processTouchEvent(event);
		}

		track = false;
		if (!boardFace.isAnalysis()) {
			if (AppData.isFinishedEchessGameMode(boardFace) || finished || boardFace.isSubmit() ||
					(boardFace.getHply() < boardFace.getMovesCount())) {
				return true;
			}

			if(whiteUserName.equals(StaticData.SYMBOL_EMPTY) || blackUserName.equals(StaticData.SYMBOL_EMPTY))
				return true;

			if (whiteUserName.equals(userName)  && !boardFace.isWhiteToMove()) {
				return true;
			}
			if (blackUserName.equals(userName) && boardFace.isWhiteToMove()) {
				return true;
			}

		}

		return super.onTouchEvent(event);
	}

    @Override
	public void switchChat() {
		gameActivityFace.switch2Chat();
	}

	@Override
	public void showHint() {
	}

//	public void updateMoves(String newMove) {
//		int[] moveFT = MoveParser.parse(getBoardFace(), newMove);
//		if (moveFT.length == 4) {
//			Move move;
//			if (moveFT[3] == 2)
//				move = new Move(moveFT[0], moveFT[1], 0, 2);
//			else
//				move = new Move(moveFT[0], moveFT[1], moveFT[2], moveFT[3]);
//
//			getBoardFace().makeMove(move, false);
//		} else {
//			Move move = new Move(moveFT[0], moveFT[1], 0, 0);
//			getBoardFace().makeMove(move, false);
//		}
//	}

	public void updatePlayerNames(String whitePlayerName, String blackPlayerName) {
		if (!whitePlayerName.isEmpty() && !blackPlayerName.isEmpty()) {
			whiteUserName = whitePlayerName.substring(0, whitePlayerName.indexOf(StaticData.SYMBOL_LEFT_PAR)).trim().toLowerCase();
			blackUserName = blackPlayerName.substring(0, blackPlayerName.indexOf(StaticData.SYMBOL_LEFT_PAR)).trim().toLowerCase();
		}
	}
}
