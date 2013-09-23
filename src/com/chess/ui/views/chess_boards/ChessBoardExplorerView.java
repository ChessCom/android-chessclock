package com.chess.ui.views.chess_boards;

import android.content.Context;
import android.graphics.Canvas;
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
	protected void onDraw(Canvas canvas) {
		canvas.setDrawFilter(drawFilter);
		super.onDraw(canvas);
		drawBoard(canvas);

		if (gameFace != null && getBoardFace() != null) {
			drawCoordinates(canvas);
			drawHighlights(canvas);
			drawTrackballDrag(canvas);

			drawPiecesAndAnimation(canvas);
			drawDragPosition(canvas);
		}
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

			if (firstClick) {
				from = ChessBoard.getPositionIndex(col, row, getBoardFace().isReside());
				if (getBoardFace().getPieces()[from] != 6 && getBoardFace().getSide() == getBoardFace().getColor()[from]) {
					pieceSelected = true;
					firstClick = false;
					invalidate();
				}
			} else {
				to = ChessBoard.getPositionIndex(col, row, getBoardFace().isReside());
				pieceSelected = false;
				firstClick = true;
				boolean found = false;

				Move move = null;
				List<Move> moves = getBoardFace().generateLegalMoves();
				for (Move move1 : moves) {
					move = move1;
					if (move.from == from && move.to == to) {
						found = true;
						break;
					}
				}
				if ((((to < 8) && (getBoardFace().getSide() == ChessBoard.WHITE_SIDE)) ||
						((to > 55) && (getBoardFace().getSide() == ChessBoard.BLACK_SIDE))) &&
						(getBoardFace().getPieces()[from] == ChessBoard.PAWN) && found) {

					gameFace.showChoosePieceDialog(col, row);
					return true;
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
				} else if (getBoardFace().getPieces()[to] != ChessBoard.EMPTY
						&& getBoardFace().getSide() == getBoardFace().getColor()[to]) {
					pieceSelected = true;
					firstClick = false;
					from = ChessBoard.getPositionIndex(col, row, getBoardFace().isReside());
				}
				invalidate();
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
			return super.onTouchEvent(event);
		}

		track = false;
		if (!getBoardFace().isAnalysis()) {
//			if (((LessonsBoardFace)getBoardFace()).isLatestMoveMadeUser()) {
//				return true;
//			}
		}

		return super.onTouchEvent(event);
	}

	@Override
	public void promote(int promote, int col, int row) {
		boolean found = false;
		Move move = null;
		List<Move> moves = getBoardFace().generateLegalMoves();
		for (Move move1 : moves) {
			move = move1;
			if (move.from == from && move.to == to && move.promote == promote) {
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
		} else if (getBoardFace().getPieces()[to] != ChessBoard.EMPTY
				&& getBoardFace().getSide() == getBoardFace().getColor()[to]) {
			pieceSelected = true;
			firstClick = false;
			from = ChessBoard.getPositionIndex(col, row, getBoardFace().isReside());
		}
		invalidate();
	}

	@Override
	protected void afterUserMove() {
		super.afterUserMove();

		getBoardFace().setMovesCount(getBoardFace().getPly());
		gameFace.invalidateGameScreen();
		gameFace.updateAfterMove();
	}

}