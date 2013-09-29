package com.chess.ui.views.chess_boards;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.chess.statics.StaticData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardTactics;
import com.chess.ui.engine.Move;
import com.chess.ui.interfaces.boards.BoardViewTacticsFace;
import com.chess.ui.interfaces.game_ui.GameTacticsFace;

import java.util.List;

public class ChessBoardTacticsView extends ChessBoardBaseView implements BoardViewTacticsFace {

	private GameTacticsFace gameTacticsFace;


	public ChessBoardTacticsView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

	public void setGameFace(GameTacticsFace gameActivityFace) {
		super.setGameFace(gameActivityFace);

		gameTacticsFace = gameActivityFace;
	}

    @Override
	public void afterUserMove() {
		super.afterUserMove();

		getBoardFace().setMovesCount(getBoardFace().getPly());
		gameTacticsFace.invalidateGameScreen();

		if (!getBoardFace().isAnalysis()) {
			gameTacticsFace.verifyMove();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(drawFilter);
        super.onDraw(canvas);
		drawBoard(canvas);

		if (gameTacticsFace != null && getBoardFace() != null) {
			drawCoordinates(canvas);
			drawHighlights(canvas);
			drawTrackballDrag(canvas);

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

		if (isLocked()) {
			return processTouchEvent(event);
		}

        track = false;
        if (!getBoardFace().isAnalysis()) {
            if (getBoardFace().isFinished()) // TODO probably never happens
                return true;

            if (((ChessBoardTactics)getBoardFace()).isLatestMoveMadeUser()) {
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
    public void showHint() {
		gameTacticsFace.showHint();
    }

	@Override
	public void flipBoard() {
		getBoardFace().setReside(!getBoardFace().isReside());
		invalidate();
	}

	@Override
	public void restart() {
		gameTacticsFace.restart();
	}

	@Override
	public void showExplorer() {
		gameTacticsFace.showExplorer();
	}

}
