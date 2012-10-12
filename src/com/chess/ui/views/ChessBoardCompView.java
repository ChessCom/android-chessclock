package com.chess.ui.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import com.chess.R;
import com.chess.backend.interfaces.AbstractUpdateListener;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.ComputeMoveTask;
import com.chess.model.ComputeMoveItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardComp;
import com.chess.ui.engine.Move;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.interfaces.GameCompActivityFace;

import java.util.Iterator;
import java.util.TreeSet;

public class ChessBoardCompView extends ChessBoardBaseView {

	private static final long HINT_REVERSE_DELAY = 1500;

	private static final String DIVIDER_1 = "|";
	private static final String DIVIDER_2 = ":";
//	private boolean hint; // TODO make independent from board
//    private boolean computerMoving; // TODO make independent from board
	private ChessBoardComp chessBoardComp;
	private int compStrength;
	private int[] compStrengthArray;
	private ComputeMoveTask computeMoveTask;

	private GameCompActivityFace gameCompActivityFace;
	private HintMoveUpdateListener hintMoveUpdateListener;
	private ComputeMoveUpdateListener computeMoveUpdateListener;


	public ChessBoardCompView(Context context, AttributeSet attrs) {
        super(context, attrs);

		compStrengthArray = resources.getIntArray(R.array.comp_strength);

		hintMoveUpdateListener = new HintMoveUpdateListener();
		computeMoveUpdateListener = new ComputeMoveUpdateListener();
    }

    public void setGameActivityFace(GameCompActivityFace gameActivityFace) {
		super.setGameActivityFace(gameActivityFace);

        gameCompActivityFace = gameActivityFace;

		compStrength = compStrengthArray[AppData.getCompStrength(getContext())];
    }

	@Override
	protected void onBoardFaceSet(BoardFace boardFace) {
		pieces_tmp = boardFace.getPieces().clone();
		colors_tmp = boardFace.getColor().clone();
	}

	private ChessBoardComp getBoardComp(){
		return ChessBoardComp.getInstance(gameActivityFace);
	}

	public void afterMove() {
        boardFace.setMovesCount(boardFace.getHply());
		gameActivityFace.invalidateGameScreen();

        if (isGameOver())
            return;

        if (!boardFace.isAnalysis() && !AppData.isHumanVsHumanGameMode(boardFace)) {
			computerMove(compStrength);
        }
    }


    protected boolean isGameOver() {
        //saving game for comp game mode if human is playing
        if ((AppData.isComputerVsHumanGameMode(boardFace) || AppData.isHumanVsHumanGameMode(boardFace))
                && !boardFace.isAnalysis()) {

			StringBuilder builder = new StringBuilder();
			builder.append(boardFace.getMode());
            int i;
            for (i = 0; i < boardFace.getMovesCount(); i++) {
                Move move = boardFace.getHistDat()[i].move;
				builder.append(DIVIDER_1)
						.append(move.from).append(DIVIDER_2)
						.append(move.to).append(DIVIDER_2)
						.append(move.promote).append(DIVIDER_2)
						.append(move.bits);
            }

			SharedPreferences.Editor editor = preferences.edit();
			Log.d("TEST", "saving game = " + builder.toString());
			editor.putString(AppData.getUserName(getContext()) + AppConstants.SAVED_COMPUTER_GAME, builder.toString());
			editor.commit();
        }
		return super.isGameOver();
    }

	public void computerMove(final int time) {
		if (isHint())
			return;

		setComputerMoving(true);
		gameCompActivityFace.onCompMove();
		ComputeMoveItem computeMoveItem = new ComputeMoveItem();
		computeMoveItem.setBoardFace(getBoardFace());
		computeMoveItem.setColors_tmp(colors_tmp);
		computeMoveItem.setPieces_tmp(pieces_tmp);
		computeMoveItem.setMoveTime(time);

		pieces_tmp = getBoardFace().getPieces().clone();
		colors_tmp = getBoardFace().getColor().clone();

		computeMoveTask = new ComputeMoveTask(computeMoveItem, computeMoveUpdateListener);
		computeMoveTask.executeTask();
	}

	public void makeHint(final int time) {


		gamePanelView.toggleControlButton(GamePanelView.B_HINT_ID, true);

		setHint(true);
		setComputerMoving(true);
//		gameCompActivityFace.onCompMove();
		ComputeMoveItem computeMoveItem = new ComputeMoveItem();
		computeMoveItem.setBoardFace(getBoardFace());
		computeMoveItem.setColors_tmp(colors_tmp);
		computeMoveItem.setPieces_tmp(pieces_tmp);
		computeMoveItem.setMoveTime(time);

		pieces_tmp = getBoardFace().getPieces().clone();
		colors_tmp = getBoardFace().getColor().clone();

		computeMoveTask = new ComputeMoveTask(computeMoveItem, hintMoveUpdateListener);
		computeMoveTask.executeTask();
	}

	public void stopComputerMove() {
		if (computeMoveTask != null) {
			setComputerMoving(false);
			computeMoveTask.cancel(true);
		}
	}

	private class ComputeMoveUpdateListener extends AbstractUpdateListener<ComputeMoveItem> {
		public ComputeMoveUpdateListener() {
			super(getContext());
		}

		@Override
		public void updateData(ComputeMoveItem returnedObj) {
			super.updateData(returnedObj);
			setComputerMoving(false);
			pieces_tmp = returnedObj.getPieces_tmp();
			colors_tmp = returnedObj.getColors_tmp();

			boardFace.setMovesCount(boardFace.getHply());

			gameActivityFace.invalidateGameScreen();
			gameCompActivityFace.onPlayerMove();
			invalidate();

			if (isGameOver())
				return;

			if (AppData.isComputerVsComputerGameMode(boardFace)
					|| (isHint() && !AppData.isHumanVsHumanGameMode(boardFace))) {
				computerMove(returnedObj.getMoveTime());
			}
		}
	}

	private class HintMoveUpdateListener extends AbstractUpdateListener<ComputeMoveItem> {
		public HintMoveUpdateListener() {
			super(getContext());
		}

		@Override
		public void updateData(ComputeMoveItem returnedObj) {
			super.updateData(returnedObj);
			setComputerMoving(false);
			pieces_tmp = returnedObj.getPieces_tmp();
			colors_tmp = returnedObj.getColors_tmp();

//			boardFace.setMovesCount(boardFace.getHply());

//			gameActivityFace.invalidateGameScreen();
			invalidate();

			if (AppData.isComputerVsComputerGameMode(boardFace) || (!AppData.isHumanVsHumanGameMode(boardFace))) {

//				computerMove(returnedObj.getMoveTime());

				handler.postDelayed(reverseHintTask, HINT_REVERSE_DELAY);
			}
		}
	}

	private Runnable reverseHintTask = new Runnable() {
		@Override
		public void run() {
			getBoardFace().takeBack();
			invalidate();

			setHint(false);
			gamePanelView.toggleControlButton(GamePanelView.B_HINT_ID, false);
		}
	};

	@Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(drawFilter);
        super.onDraw(canvas);
		drawBoard(canvas);

        if (!isComputerMoving()) {
			drawPieces(canvas);
        } else {
            for (int i = 0; i < 64; i++) {
                if (drag && i == from)
                    continue;

                int color = colors_tmp[i];
                int piece = pieces_tmp[i];
                int x = ChessBoard.getColumn(i, boardFace.isReside());
                int y = ChessBoard.getRow(i, boardFace.isReside());
                if (color != 6 && piece != 6) {     // here is the simple replace/redraw of piece
                    rect.set(x * square, y * square, x * square + square, y * square + square);
                    canvas.drawBitmap(piecesBitmaps[color][piece], null, rect, null);
                }
            }
        }

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
            if (isComputerMoving() || finished)
                return true;

			if ((AppData.isComputerVsHumanWhiteGameMode(boardFace) && !boardFace.isWhiteToMove())
					|| (AppData.isComputerVsHumanBlackGameMode(boardFace) && boardFace.isWhiteToMove())) {
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
                dragY =  (int) event.getY() - square;
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

                    if ((((to < 8) && (boardFace.getSide() == ChessBoard.LIGHT)) || // if in promotion position
							((to > 55) && (boardFace.getSide() == ChessBoard.DARK))) &&
                            (boardFace.getPieces()[from] == ChessBoard.PAWN) && found) {

                        gameActivityFace.showChoosePieceDialog(col, row);
                        return true;
                    }
					// && move != null -> always true because found flag preceding that state
                    if (found /*&& move != null*/ && boardFace.makeMove(move)) {
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
    public void flipBoard() {
        if (!isComputerMoving()) {
            getBoardFace().setReside(!getBoardFace().isReside());
            if (AppData.isComputerVsHumanGameMode(getBoardFace())) {
                if (AppData.isComputerVsHumanWhiteGameMode(getBoardFace())) {
                    getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK);

                } else if (AppData.isComputerVsHumanBlackGameMode(getBoardFace())) {
                    getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE);

                }
                computerMove(compStrength);
            }
            invalidate();
			gameActivityFace.invalidateGameScreen();
        }
    }

    @Override
    public void switchChat() {
    }

	@Override
	public void switchAnalysis() {
		super.switchAnalysis();
		gamePanelView.enableGameButton(GamePanelView.B_HINT_ID, !boardFace.isAnalysis());
	}

	@Override
    public void moveBack() {
        if (!isComputerMoving()) {
            finished = false;
            pieceSelected = false;
            getBoardFace().takeBack();
            invalidate();
			gameActivityFace.invalidateGameScreen();
        }
    }

    @Override
    public void moveForward() {
        if (!isComputerMoving()) {
            pieceSelected = false;
            getBoardFace().takeNext();
            invalidate();
			gameActivityFace.invalidateGameScreen();
        }
    }

    @Override
    public void showHint() {
        if (!isComputerMoving()) {
			makeHint(compStrength);
        }
    }

	private boolean isHint() {
		return getBoardComp().isHint();
	}

	private void setHint(boolean hint) {
		getBoardComp().setHint(hint);
	}

	private void setComputerMoving(boolean computerMoving) {
		getBoardComp().setComputerMoving(computerMoving);
	}

	public boolean isComputerMoving() {
		return getBoardComp().isComputerMoving();
	}

}
