package com.chess.ui.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.PostMoveToCompTask;
import com.chess.model.ComputeMoveItem;
import com.chess.ui.engine.*;
import com.chess.ui.interfaces.GameCompActivityFace;

import java.util.Iterator;
import java.util.TreeSet;

public class ChessBoardCompView extends ChessBoardBaseView {

	public static final long HINT_REVERSE_DELAY = 1500;

	private static final String DIVIDER_1 = "|";
	private static final String DIVIDER_2 = ":";
//	private boolean hint; // TODO make independent from board
//    private boolean computerMoving; // TODO make independent from board
	private PostMoveToCompTask computeMoveTask;

	private GameCompActivityFace gameCompActivityFace;

	public ChessBoardCompView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setGameActivityFace(GameCompActivityFace gameActivityFace) {
		super.setGameActivityFace(gameActivityFace);

        gameCompActivityFace = gameActivityFace;
    }

	private ChessBoardComp getBoardComp(){
		return ChessBoardComp.getInstance(gameActivityFace);
	}

	@Override
	public void afterMove() {
        boardFace.setMovesCount(boardFace.getHply());
		gameActivityFace.invalidateGameScreen();

        if (isGameOver())
            return;

        //if (!boardFace.isAnalysis()/* && !AppData.isHumanVsHumanGameMode(boardFace)*/) {
			postMoveToEngine(boardFace.getLastMove());
        //}
    }

    @Override
	public boolean isGameOver() {
        //saving game for comp game mode if human is playing
        if ((AppData.isComputerVsHumanGameMode(boardFace) || AppData.isHumanVsHumanGameMode(boardFace))
                && !boardFace.isAnalysis()) {

			//getBoardFace().setFen(AppData.getCompEngineHelper().getFen()); // move to another place?

			StringBuilder builder = new StringBuilder();
			builder.append(boardFace.getMode());

			builder.append(" [" + boardFace.getMoveListSAN().toString().replaceAll("\n", " ") + "] "); // todo: remove debug info

			/*builder.append(DIVIDER_1)
					.append("FEN").append(DIVIDER_2) // ?
					.append(AppData.getCompEngineHelper().getFen());*/

            int i;
            for (i = 0; i < boardFace.getMovesCount(); i++) {
                Move move = boardFace.getHistDat()[i].move;
				builder.append(DIVIDER_1)
						.append(move.from).append(DIVIDER_2)
						.append(move.to).append(DIVIDER_2)
						.append(move.promote).append(DIVIDER_2)
						.append(move.bits);
            }

			Log.d(CompEngineHelper.TAG, "STORE game " + builder.toString());

			SharedPreferences.Editor editor = preferences.edit();
			editor.putString(AppData.getUserName(getContext()) + AppConstants.SAVED_COMPUTER_GAME, builder.toString());
			editor.commit();
        }
		return super.isGameOver();
    }

	public void postMoveToEngine(Move lastMove) {
		if (isHint())
			return;

		if (!AppData.isHumanVsHumanGameMode(boardFace) && !getBoardFace().isAnalysis()) {
			setComputerMoving(true);
			gameCompActivityFace.onCompMove();
		}

		ComputeMoveItem computeMoveItem = new ComputeMoveItem();
		computeMoveItem.setBoardFace(getBoardFace());
		computeMoveItem.setMove(lastMove.toString());

		Log.d(CompEngineHelper.TAG, "make move lastMove " + lastMove);

		computeMoveTask = new PostMoveToCompTask(computeMoveItem, AppData.getCompEngineHelper(), gameCompActivityFace);
		computeMoveTask.execute();
	}

	public void makeHint() {

		gamePanelView.toggleControlButton(GamePanelView.B_HINT_ID, true);

		setHint(true);
		setComputerMoving(true);
//		gameCompActivityFace.onCompMove();

		Log.d(CompEngineHelper.TAG, "ask for move hint");

		gameCompActivityFace.onCompMove();
		AppData.getCompEngineHelper().makeHint();
	}

	public void stopComputerMove() {
		if (computeMoveTask != null) {
			setComputerMoving(false);
			computeMoveTask.cancel(true);
		}
	}

	@Override
    protected void onDraw(Canvas canvas) {
        canvas.setDrawFilter(drawFilter);
        super.onDraw(canvas);
		drawBoard(canvas);

        //if (!isComputerMoving()) {

			// todo @compengine: move to base class for all game modes

			MoveAnimator moveAnimator = null;
			boolean animationActive = false;
			if (movesToAnimate.size() > 0) {
				moveAnimator = movesToAnimate.getFirst();
				/*Log.d("testtest", "moveAnimator " + moveAnimator);
				Log.d("testtest", "movesToAnimate.size() " + movesToAnimate.size());*/

				animationActive = moveAnimator.updateState();
				if (animationActive) {
					moveAnimator.draw(canvas);
				} else {
					movesToAnimate.remove(moveAnimator);
					invalidate(); // ?
				}
				//Log.d("testtest", "animationActive " + animationActive);
			}

			drawPieces(canvas, animationActive, moveAnimator);

			drawHighlight(canvas);
			drawDragPosition(canvas);
			drawTrackballDrag(canvas);
        /*} else {
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
        }*/

		drawMoveHints(canvas); // todo @compengine: move to base class for all game modes

		drawCoordinates(canvas);
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
            return super.processTouchEvent(event);
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

        return super.onTouchEvent(event);
    }

    @Override
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
				int engineMode;
                if (AppData.isComputerVsHumanWhiteGameMode(getBoardFace())) {
					getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK);

                } else if (AppData.isComputerVsHumanBlackGameMode(getBoardFace())) {
                    getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE);

                }
				setComputerMoving(true);
				gameCompActivityFace.onCompMove();
				engineMode = CompEngineHelper.mapGameMode(getBoardFace().getMode());
				AppData.getCompEngineHelper().setGameMode(engineMode);
                //postMoveToEngine(getBoardFace().getLastMove(), false, compStrength);
            }
            invalidate();
			//gameActivityFace.invalidateGameScreen();
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
	public void enableAnalysis() {
		gamePanelView.toggleControlButton(GamePanelView.B_ANALYSIS_ID, true);
		gamePanelView.enableAnalysisMode(true);
		gameActivityFace.switch2Analysis(true);
		gamePanelView.enableGameButton(GamePanelView.B_HINT_ID, false);
	}

	@Override
    public void moveBack() {
        if (!isComputerMoving() && !(AppData.isComputerVsHumanBlackGameMode(boardFace) && getBoardFace().getHply() == 1)) {

			AppData.getCompEngineHelper().moveBack();

			finished = false;
			pieceSelected = false;

			Move move = getBoardFace().takeBack();
			addMoveAnimator(move, false);

			if (AppData.isComputerVsHumanGameMode(boardFace) && !boardFace.isAnalysis()) {
				move = getBoardFace().takeBack(); // todo: create method for ply
				addMoveAnimator(move, false);
			}
			invalidate();
			gameActivityFace.invalidateGameScreen();
        }
    }

    @Override
    public void moveForward() {
        if (!isComputerMoving()) {

			AppData.getCompEngineHelper().moveForward();

			pieceSelected = false;
			getBoardFace().takeNext();

			if (AppData.isComputerVsHumanGameMode(boardFace) && !boardFace.isAnalysis()) {
				getBoardFace().takeNext(); // todo: create method for ply
			}
            invalidate();
			gameActivityFace.invalidateGameScreen();
		}
    }

    @Override
    public void showHint() {
        if (!isComputerMoving() && !isHint()) {
			makeHint();
        }
    }

	public boolean isHint() {
		return getBoardComp().isHint();
	}

	public void setHint(boolean hint) {
		getBoardComp().setHint(hint);
	}

	public void setComputerMoving(boolean computerMoving) {
		getBoardComp().setComputerMoving(computerMoving);
	}

	public boolean isComputerMoving() {
		return getBoardComp().isComputerMoving();
	}
}
