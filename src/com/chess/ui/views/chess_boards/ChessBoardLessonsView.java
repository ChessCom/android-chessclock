package com.chess.ui.views.chess_boards;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.chess.statics.StaticData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.interfaces.boards.BoardViewLessonsFace;
import com.chess.ui.interfaces.boards.LessonsBoardFace;
import com.chess.ui.interfaces.game_ui.GameLessonFace;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.07.13
 * Time: 12:49
 */
public class ChessBoardLessonsView extends ChessBoardBaseView implements BoardViewLessonsFace {

	private GameLessonFace gameLessonFace;

	public ChessBoardLessonsView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setGameUiFace(GameLessonFace gameLessonFace) {
		super.setGameFace(gameLessonFace);

		this.gameLessonFace = gameLessonFace;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.setDrawFilter(drawFilter);
		super.onDraw(canvas);
		drawBoard(canvas);

		if (gameLessonFace != null && getBoardFace() != null) {
			drawCoordinates(canvas);
			drawHighlights(canvas);

			drawPiecesAndAnimation(canvas);
			drawDragPosition(canvas);
		}
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
		if (!getBoardFace().isAnalysis()) {
			if (((LessonsBoardFace)getBoardFace()).isLatestMoveMadeUser()) {
				return true;
			}
		}

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
	protected void afterUserMove() {
		super.afterUserMove();

		getBoardFace().setMovesCount(getBoardFace().getPly());
		gameLessonFace.invalidateGameScreen();

		if (!getBoardFace().isAnalysis()) {
			gameLessonFace.verifyMove();
		}
	}

	@Override
	public void start() {
		gameLessonFace.startLesson();
	}

	@Override
	public void showHint() {
		gameLessonFace.showHint();
	}

	@Override
	public void restart() {
		gameLessonFace.restart();
	}

	@Override
	public void nextPosition() {
		gameLessonFace.nextPosition();
	}
}
