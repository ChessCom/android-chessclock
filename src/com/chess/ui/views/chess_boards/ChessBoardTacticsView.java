package com.chess.ui.views.chess_boards;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.chess.backend.statics.StaticData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardTactics;
import com.chess.ui.engine.Move;
import com.chess.ui.interfaces.boards.BoardViewTacticsFace;
import com.chess.ui.interfaces.game_ui.GameTacticsFace;

import java.util.Iterator;
import java.util.TreeSet;

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
	public void afterMove() {
		getBoardFace().setMovesCount(getBoardFace().getHply());
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
			drawDragPosition(canvas);
			drawTrackballDrag(canvas);

			drawPiecesAndAnimation(canvas);
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

                TreeSet<Move> moves = getBoardFace().gen();
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

                    gameTacticsFace.showChoosePieceDialog(col, row);
                    return true;
                }

				boolean moveMade = false;
				MoveAnimator moveAnimator = null;
				if (found) {
					moveAnimator = new MoveAnimator(move, true);
					moveMade = getBoardFace().makeMove(move);
				}
				if (moveMade) {
					moveAnimator.setForceCompEngine(true); // TODO @engine: probably postpone afterMove() only for vs comp mode
					setMoveAnimator(moveAnimator);
					//afterMove(); //
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
            if (getBoardFace().isFinished()) // TODO probably never happens
                return true;

            if (((ChessBoardTactics)getBoardFace()).isLatestMoveMadeUser()) {
                return true;
            }
        }

        return super.onTouchEvent(event);
    }

    @Override
	public void promote(int promote, int col, int row) {
        boolean found = false;
        TreeSet<Move> moves = getBoardFace().gen();
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
			moveAnimator.setForceCompEngine(true); // TODO @engine: probably postpone afterMove() only for vs comp mode
			setMoveAnimator(moveAnimator);
			//afterMove(); //
		} else if (getBoardFace().getPieces()[to] != ChessBoard.EMPTY
				&& getBoardFace().getSide() == getBoardFace().getColor()[to]) {
			pieceSelected = true;
			firstClick = false;
			from = ChessBoard.getPositionIndex(col, row, getBoardFace().isReside());
		}
		invalidate();
    }

    @Override
    public void showHint() {
		gameTacticsFace.showAnswer();
    }

	@Override
	public void flipBoard() {
	}

//	public void enableAnalysis() {  // TODO recheck logic
//		gameFace.switch2Analysis(true);
//	}

	@Override
	public void showStats() {
		gameTacticsFace.showStats();
	}

	@Override
	public void showHelp() {
		gameTacticsFace.showHelp();
	}

	@Override
	public void restart() {
		gameTacticsFace.restart();
	}

}
