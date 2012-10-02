package com.chess.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import com.chess.backend.statics.StaticData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.interfaces.GameTacticsActivityFace;
import com.chess.ui.interfaces.TacticBoardFace;

import java.util.Iterator;
import java.util.TreeSet;

public class ChessBoardTacticsView extends ChessBoardBaseView {

	private GameTacticsActivityFace gameTacticsActivityFace;
	protected TacticBoardFace boardFace;


	public ChessBoardTacticsView(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

	public void setGameActivityFace(GameTacticsActivityFace gameActivityFace) {
		super.setGameActivityFace(gameActivityFace);

		gameTacticsActivityFace = gameActivityFace;
	}

	protected void onBoardFaceSet(BoardFace boardFace) {
		this.boardFace = (TacticBoardFace) boardFace;
	}

    public void afterMove() {
        boardFace.setMovesCount(boardFace.getHply());
		gameActivityFace.invalidateGameScreen();

		if (!boardFace.isAnalysis()) {
			gameTacticsActivityFace.checkMove();
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
            return super.onTouchEvent(event);
        }

        track = false;
        if (!boardFace.isAnalysis()) {
            if (finished ) // TODO probably never happens
                return true;

            if (boardFace.getHply() % 2 == 0) {
                return true;
            }
        }

        int col, row;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                col = (int) (event.getX() - event.getX() % square) / square;
                row = (int) (event.getY() - event.getY() % square) / square;
                if (col > 7 || col < 0 || row > 7 || row < 0) {
                    invalidate();
                    return false;
                }
                if (firstclick) {
                    from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
                    if (boardFace.getPieces()[from] != 6 && boardFace.getSide() == boardFace.getColor()[from]) {
                        pieceSelected = true;
                        firstclick = false;
                        invalidate();
                    }
                } else {
                    int fromPosIndex = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
                    if (boardFace.getPieces()[fromPosIndex] != 6 && boardFace.getSide() == boardFace.getColor()[fromPosIndex]) {
                        from = fromPosIndex;
                        pieceSelected = true;
                        firstclick = false;
                        invalidate();
                    }
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                dragX = (int) event.getX();
                dragY = (int) event.getY() - square;
                col = (dragX - dragX % square) / square;
                row = (dragY - dragY % square) / square;
                if (col > 7 || col < 0 || row > 7 || row < 0) {
                    invalidate();
                    return false;
                }
                if (!drag && !pieceSelected)
                    from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
                if (!firstclick && boardFace.getSide() == boardFace.getColor()[from]) {
                    drag = true;
                    to = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
                    invalidate();
                }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                col = (int) (event.getX() - event.getX() % square) / square;
                row = (int) (event.getY() - event.getY() % square) / square;

                drag = false;
                // if outside of the boardBitmap - return
                if (col > 7 || col < 0 || row > 7 || row < 0) { // if touched out of board
                    invalidate();
                    return false;
                }
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
                        move = moveIterator.next();     // search for move that was made
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

                    if (found && move != null && boardFace.makeMove(move)) {
                        afterMove();
                    } else if (boardFace.getPieces()[to] != 6 && boardFace.getSide() == boardFace.getColor()[to]) {
                        pieceSelected = true;
                        firstclick = false;
                        from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
                    }
                    invalidate();
                }
                return true;
            }
            default:
                break;
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

	@Override
	public TacticBoardFace getBoardFace() {
		return boardFace;
	}

    public void promote(int promote, int col, int row) {
        boolean found = false;
        TreeSet<Move> moves = boardFace.gen();
        Iterator<Move> iterator = moves.iterator();

        Move move = null;
        while (iterator.hasNext()) {
            move = iterator.next();
            if (move.from == from && move.to == to && move.promote == promote) {
                found = true;
                break;
            }
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



    @Override
    public void switchChat() {
    }


    @Override
    public void showHint() { // no hints for tactics

    }


	public void setFinished(boolean finished) {
		this.finished = finished;
	}
}
