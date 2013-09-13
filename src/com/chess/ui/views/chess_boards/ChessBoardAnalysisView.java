package com.chess.ui.views.chess_boards;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.StaticData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.boards.BoardViewAnalysisFace;
import com.chess.ui.interfaces.game_ui.GameAnalysisFace;
import com.chess.ui.views.game_controls.ControlsAnalysisView;

import java.util.Iterator;
import java.util.TreeSet;

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
	private ControlsAnalysisView controlsAnalysisView;


	public ChessBoardAnalysisView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public void setGameActivityFace(GameAnalysisFace gameActivityFace) {
		super.setGameFace(gameActivityFace);

		gameAnalysisActivityFace = gameActivityFace;
	}

	public void setControlsView(ControlsAnalysisView controlsView) {
		super.setControlsView(controlsView);
		controlsAnalysisView = controlsView;
		controlsAnalysisView.setBoardViewFace(this);
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

			builder.append(" [" + getBoardFace().getMoveListSAN().toString().replaceAll("\n", " ") + "] "); // todo: remove debug info

			int i;
			for (i = 0; i < getBoardFace().getMovesCount(); i++) {
				Move move = getBoardFace().getHistDat()[i].move;
				builder.append(DIVIDER_1)
						.append(move.from).append(DIVIDER_2)
						.append(move.to).append(DIVIDER_2)
						.append(move.promote).append(DIVIDER_2)
						.append(move.bits);
			}

			SharedPreferences.Editor editor = preferences.edit();
			editor.putString(getAppData().getUsername() + AppConstants.SAVED_COMPUTER_GAME, builder.toString());
			editor.commit();
		}
		return super.isGameOver();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.setDrawFilter(drawFilter);
		super.onDraw(canvas);
		drawBoard(canvas);

		drawHighlights(canvas);
		drawTrackballDrag(canvas);

		drawPiecesAndAnimation(canvas);
		drawDragPosition(canvas);

		drawCoordinates(canvas);
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

				TreeSet<Move> moves = getBoardFace().generateLegalMoves();
				Iterator<Move> moveIterator = moves.iterator();

				Move move = null;
				while (moveIterator.hasNext()) {
					move = moveIterator.next();
					if (move.from == from && move.to == to) {
						found = true;
						break;
					}
				}
				if ((((to < 8) && (getBoardFace().getSide() == ChessBoard.WHITE_SIDE)) ||
						((to > 55) && (getBoardFace().getSide() == ChessBoard.BLACK_SIDE))) &&
						(getBoardFace().getPieces()[from] == ChessBoard.PAWN) && found) {

					gameAnalysisActivityFace.showChoosePieceDialog(col, row);
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

		return super.onTouchEvent(event);
	}

	@Override
	public void promote(int promote, int col, int row) {
		boolean found = false;
		TreeSet<Move> moves = getBoardFace().generateLegalMoves();
		Iterator<Move> iterator = moves.iterator();

		Move move = null;
		while (iterator.hasNext()) {
			move = iterator.next();
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

}

