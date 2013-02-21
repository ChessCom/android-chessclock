package com.chess.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.chess.backend.statics.StaticData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.interfaces.BoardViewTacticsFace;
import com.chess.ui.interfaces.GameTacticsActivityFace;

import java.util.Iterator;
import java.util.TreeSet;

public class ChessBoardTacticsView extends ChessBoardBaseView implements BoardViewTacticsFace {

	private GameTacticsActivityFace gameTacticsActivityFace;
	private ControlsTacticsView controlsTacticsView;


	public ChessBoardTacticsView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

	public void setGameActivityFace(GameTacticsActivityFace gameActivityFace) {
		super.setGameActivityFace(gameActivityFace);

		gameTacticsActivityFace = gameActivityFace;
	}

    public void afterMove() {
		getBoardFace().setMovesCount(getBoardFace().getHply());
		gameActivityFace.invalidateGameScreen();

		if (!getBoardFace().isAnalysis()) {
			gameTacticsActivityFace.verifyMove();
        }
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

//		drawCapturedPieces(); // TODO restore
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
                from = ChessBoard.getPositionIndex(col, row, getBoardFace().isReside());
                if (getBoardFace().getPieces()[from] != 6 && getBoardFace().getSide() == getBoardFace().getColor()[from]) {
                    pieceSelected = true;
                    firstclick = false;
                    invalidate();
                }
            } else {
                to = ChessBoard.getPositionIndex(col, row, getBoardFace().isReside());
                pieceSelected = false;
                firstclick = true;
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
                if ((((to < 8) && (getBoardFace().getSide() == ChessBoard.LIGHT)) ||
                        ((to > 55) && (getBoardFace().getSide() == ChessBoard.DARK))) &&
                        (getBoardFace().getPieces()[from] == ChessBoard.PAWN) && found) {

                    gameActivityFace.showChoosePieceDialog(col, row);
                    return true;
                }
                if (found && getBoardFace().makeMove(move)) {
                    invalidate();
                    afterMove();
                } else if (getBoardFace().getPieces()[to] != 6 && getBoardFace().getSide() == getBoardFace().getColor()[to]) {
                    pieceSelected = true;
                    firstclick = false;
                    from = ChessBoard.getPositionIndex(col, row, getBoardFace().isReside());
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
            return super.onTouchEvent(event);
        }

        track = false;
        if (!getBoardFace().isAnalysis()) {
//            if (finished ) // TODO probably never happens
            if (getBoardFace().isFinished()) // TODO probably never happens
                return true;

            if (getBoardFace().getHply() % 2 == 0) { // probably could be changed to isLatestMoveMadeUser()
                return true;
            }
        }

        return super.onTouchEvent(event);
    }

    private Runnable checkUserIsActive = new Runnable() {
        @Override
        public void run() {
            if (userActive) {
                userActive = false;
                handler.removeCallbacks(this);
                handler.postDelayed(this, StaticData.WAKE_SCREEN_TIMEOUT);
            } else
                gameActivityFace.turnScreenOff();

        }
    };

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
        if (found && getBoardFace().makeMove(move)) {
            invalidate();
            afterMove();
        } else if (getBoardFace().getPieces()[to] != 6 && getBoardFace().getSide() == getBoardFace().getColor()[to]) {
            pieceSelected = true;
            firstclick = false;
            from = ChessBoard.getPositionIndex(col, row, getBoardFace().isReside());
            invalidate();
        } else {
            invalidate();
        }
    }

    @Override
    public void showHint() {
		gameTacticsActivityFace.showHint();
    }


//	public void setFinished(boolean finished) {
//		this.finished = finished;
//	}

	@Override
	public void showStats() {
		gameTacticsActivityFace.showStats();
	}

	@Override
	public void showHelp() {
		gameTacticsActivityFace.showHelp();
	}

	@Override
	public void restart() {
		gameTacticsActivityFace.restart();
	}

	public void setControlsView(ControlsTacticsView controlsView) {
		super.setControlsView(controlsView);
		this.controlsTacticsView = controlsView;
	}
}
