package com.chess.ui.views.chess_boards;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.chess.statics.StaticData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.interfaces.boards.BoardViewDiagramFace;
import com.chess.ui.interfaces.game_ui.GameDiagramFace;
import com.chess.ui.views.game_controls.ControlsDiagramView;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.09.13
 * Time: 19:23
 */
public class ChessBoardDiagramView extends ChessBoardBaseView implements BoardViewDiagramFace {

	private static final String DIVIDER_1 = "|";
	private static final String DIVIDER_2 = ":";

	private GameDiagramFace gameDiagramFace;

	public ChessBoardDiagramView(Context context) {
		super(context);

	}
	public ChessBoardDiagramView(Context context, AttributeSet attrs) {
		super(context, attrs);

	}

	public void setGameActivityFace(GameDiagramFace gameActivityFace) {
		super.setGameFace(gameActivityFace);

		gameDiagramFace = gameActivityFace;
	}

	public void setControlsView(ControlsDiagramView controlsView) {
		super.setControlsView(controlsView);
		controlsView.setBoardViewFace(this);
	}

	@Override
	public void afterUserMove() {

		super.afterUserMove();

		getBoardFace().setMovesCount(getBoardFace().getPly());
		gameDiagramFace.invalidateGameScreen();

		gameDiagramFace.verifyMove();

//		isGameOver();
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
	public void onMoveBack() {
		if (noMovesToAnimate() && getBoardFace().getPly() > 0) {
			getBoardFace().setFinished(false);
			pieceSelected = false;
			setMoveAnimator(getBoardFace().getLastMove(), false);
			resetValidMoves();
			getBoardFace().takeBack();
			invalidate();
			gameDiagramFace.onMoveBack();
			gameDiagramFace.invalidateGameScreen();
		}
	}

	@Override
	public void onMoveForward() {
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
			gameDiagramFace.onMoveForward();
			gameDiagramFace.invalidateGameScreen();
		}
	}

	@Override
	public void onPlay() {
		gameDiagramFace.onPlay();
	}

	@Override
	public void onRewindBack() {
		gameDiagramFace.onRewindBack();
	}

	@Override
	public void onRewindForward() {
		gameDiagramFace.onRewindForward();
	}

	@Override
	public void showHint() {
		gameDiagramFace.showHint();
	}

	@Override
	public void showSolution() {
		gameDiagramFace.showAnswer();
	}

	@Override
	public void restart() {
		gameDiagramFace.restart();
	}

}


