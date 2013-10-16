package com.chess.ui.views.chess_boards;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import com.chess.statics.StaticData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.interfaces.boards.BoardFace;
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

	private static final long HINT_REVERSE_DELAY = 1500;

	private static final String DIVIDER_1 = "|";
	private static final String DIVIDER_2 = ":";

	private GameAnalysisFace gameAnalysisActivityFace;


	public ChessBoardAnalysisView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public void setGameActivityFace(GameAnalysisFace gameActivityFace) {
		super.setGameFace(gameActivityFace);

		gameAnalysisActivityFace = gameActivityFace;
	}

	public void setControlsView(ControlsAnalysisView controlsView) {
		super.setControlsView(controlsView);
		controlsView.setBoardViewFace(this);
	}

	@Override
	protected void onBoardFaceSet(BoardFace boardFace) {
//		pieces_tmp = boardFace.getPieces().clone();
//		colors_tmp = boardFace.getColor().clone();
	}

	@Override
	public void afterUserMove() {

		super.afterUserMove();

		getBoardFace().setMovesCount(getBoardFace().getPly());
		gameAnalysisActivityFace.invalidateGameScreen();

		isGameOver();
	}


	@Override
	protected boolean isGameOver() {
		//saving game for comp game mode if human is playing
		if ((getAppData().isComputerVsHumanGameMode(getBoardFace()) || getAppData().isHumanVsHumanGameMode(getBoardFace()))
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
//					gameAnalysisActivityFace.showChoosePieceDialog(col, row);
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

		track = false;

		return super.onTouchEvent(event);
	}

	@Override
	public void promote(int promote, int file, int rank) {
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
		} else if (getBoardFace().getPiece(to) != ChessBoard.EMPTY
				&& getBoardFace().getSide() == getBoardFace().getColor(to)) {
			pieceSelected = true;
			firstClick = false;
			from = ChessBoard.getPositionIndex(file, rank, getBoardFace().isReside());
		}
		invalidate();
	}

	@Override
	public void flipBoard() {
		getBoardFace().setReside(!getBoardFace().isReside());
		invalidate();
		gameAnalysisActivityFace.invalidateGameScreen();
	}

	@Override
	public void switchAnalysis() {
		super.switchAnalysis();
//		controlsAnalysisView.en(ControlsCompView.B_HINT_ID, !getBoardFace().isAnalysis());
	}

	@Override
	public void restart() {
		gameAnalysisActivityFace.restart();
	}

	@Override
	public void moveBack() {

		if (noMovesToAnimate() && getBoardFace().getPly() > 0) {
			getBoardFace().setFinished(false);
			pieceSelected = false;
			setMoveAnimator(getBoardFace().getLastMove(), false);
			resetValidMoves();
			getBoardFace().takeBack();
			Log.d("TEST", "moveBack");
			Log.d("TEST", "invalidate");

			invalidate();
			gameAnalysisActivityFace.invalidateGameScreen();
		}
	}

	@Override
	public void moveForward() {

		if (noMovesToAnimate()) {
			pieceSelected = false;

			Move move = getBoardFace().getNextMove();
			if (move == null) {
				return;
			}
			setMoveAnimator(move, true);
			resetValidMoves();
			getBoardFace().takeNext();

			invalidate();
			gameAnalysisActivityFace.invalidateGameScreen();
		}
	}

	@Override
	public void closeBoard() {
		gameAnalysisActivityFace.closeBoard();
	}

	@Override
	public void showExplorer() {
		gameAnalysisActivityFace.showExplorer();
	}

}

