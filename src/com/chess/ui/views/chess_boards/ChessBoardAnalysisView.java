package com.chess.ui.views.chess_boards;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.chess.statics.StaticData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.interfaces.boards.BoardViewAnalysisFace;
import com.chess.ui.interfaces.game_ui.GameAnalysisFace;
import com.chess.ui.views.game_controls.ControlsAnalysisView;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.02.13
 * Time: 18:18
 */
public class ChessBoardAnalysisView extends ChessBoardBaseView implements BoardViewAnalysisFace {

	private static final String DIVIDER_1 = "|";
	private static final String DIVIDER_2 = ":";

	private GameAnalysisFace gameAnalysisFace;


	public ChessBoardAnalysisView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public void setGameActivityFace(GameAnalysisFace gameActivityFace) {
		super.setGameFace(gameActivityFace);

		gameAnalysisFace = gameActivityFace;
	}

	public void setControlsView(ControlsAnalysisView controlsView) {
		super.setControlsView(controlsView);
		controlsView.setBoardViewFace(this);
	}

	@Override
	public void afterUserMove() {

		super.afterUserMove();

		getBoardFace().setMovesCount(getBoardFace().getPly());
		gameAnalysisFace.invalidateGameScreen();

		isGameOver();
	}


	@Override
	protected boolean isGameOver() {
		//saving game for comp game mode if human is playing
		if ((ChessBoard.isComputerVsHumanGameMode(getBoardFace()) || ChessBoard.isHumanVsHumanGameMode(getBoardFace()))
				&& !getBoardFace().isAnalysis()) {

			StringBuilder builder = new StringBuilder();
			builder.append(getBoardFace().getMode());

			builder.append(" [").append(getBoardFace().getMoveListSAN()).append("] "); // todo: remove debug info

			int i;
			for (i = 0; i < getBoardFace().getMovesCount(); i++) {
				Move move = getBoardFace().getHistDat()[i].move;
				builder.append(DIVIDER_1)
						.append(move.from).append(DIVIDER_2)
						.append(move.to).append(DIVIDER_2)
						.append(move.promote).append(DIVIDER_2)
						.append(move.bits);
			}

			appData.setSavedCompGame(builder.toString());
		}
		return super.isGameOver();
	}

//	@Override  // no need to support further as it was only on 2.1, and we don't support 2.1
//	public boolean onTrackballEvent(MotionEvent event) {
//		if (useTouchTimer) { // start count before next touch
//			handler.postDelayed(checkUserIsActive, StaticData.WAKE_SCREEN_TIMEOUT);
//			userActive = true;
//		}
//
//		float sens = 0.3f;
//		if (event.getAction() == MotionEvent.ACTION_MOVE) {
//			track = true;
//			if (event.getX() > sens)
//				trackX += squareSize;
//			else if (event.getX() < -sens)
//				trackX -= squareSize;
//			if (event.getY() > sens)
//				trackY += squareSize;
//			else if (event.getY() < -sens)
//				trackY -= squareSize;
//			if (trackX < 0)
//				trackX = 0;
//			if (trackY < 0)
//				trackY = 0;
//			if (trackX > 7 * squareSize)
//				trackX = 7 * squareSize;
//			if (trackY > 7 * squareSize)
//				trackY = 7 * squareSize;
//			invalidate();
//		} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
//			int col = (trackX - trackX % squareSize) / squareSize;
//			int row = (trackY - trackY % squareSize) / squareSize;
//
//			if (firstClick) {
//				from = ChessBoard.getPositionIndex(col, row, getBoardFace().isReside());
//				if (getBoardFace().getPiece(from) != 6 && getBoardFace().getSide() == getBoardFace().getColor(from)) {
//					pieceSelected = true;
//					firstClick = false;
//					invalidate();
//				}
//			} else {
//				to = ChessBoard.getPositionIndex(col, row, getBoardFace().isReside());
//				pieceSelected = false;
//				firstClick = true;
//				boolean found = false;
//
//				Move move = null;
//				List<Move> moves = getBoardFace().generateLegalMoves();
//				for (Move move1 : moves) {
//					move = move1;
//					if (move.from == from && move.to == to) {
//						found = true;
//						break;
//					}
//				}
//				if ((((to < 8) && (getBoardFace().getSide() == ChessBoard.WHITE_SIDE)) ||
//						((to > 55) && (getBoardFace().getSide() == ChessBoard.BLACK_SIDE))) &&
//						(getBoardFace().getPiece(from) == ChessBoard.PAWN) && found) {
//
//					gameAnalysisFace.showChoosePieceDialog(col, row);
//					return true;
//				}
//
//				boolean moveMade = false;
//				MoveAnimator moveAnimator = null;
//				if (found) {
//					moveAnimator = new MoveAnimator(move, true);
//					moveMade = getBoardFace().makeMove(move);
//				}
//				if (moveMade) {
//					moveAnimator.setForceCompEngine(true); // TODO @engine: probably postpone afterUserMove() only for vs comp mode
//					setMoveAnimator(moveAnimator);
//					//afterUserMove(); //
//				} else if (getBoardFace().getPiece(to) != ChessBoard.EMPTY
//						&& getBoardFace().getSide() == getBoardFace().getColor(to)) {
//					pieceSelected = true;
//					firstClick = false;
//					from = ChessBoard.getPositionIndex(col, row, getBoardFace().isReside());
//				}
//				invalidate();
//			}
//		}
//		return true;
//	}

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
	public void flipBoard() {
		getBoardFace().setReside(!getBoardFace().isReside());
		invalidateMe();
	}

	@Override
	public void vsComputer() {
		gameAnalysisFace.vsComputer();
	}

	@Override
	public void restart() {
		gameAnalysisFace.restart();
	}

	@Override
	public void openNotes() {
		gameAnalysisFace.openNotes();
	}

	@Override
	public void closeBoard() {
		gameAnalysisFace.closeBoard();
	}

	@Override
	public void showExplorer() {
		gameAnalysisFace.showExplorer();
	}

}

