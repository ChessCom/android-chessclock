package com.chess.ui.views.chess_boards;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.StaticData;
import com.chess.model.ComputeMoveItem;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardComp;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.stockfish.CompEngineHelper;
import com.chess.ui.engine.stockfish.PostMoveToCompTask;
import com.chess.ui.interfaces.boards.BoardViewCompFace;
import com.chess.ui.interfaces.game_ui.GameCompFace;
import com.chess.ui.views.game_controls.ControlsCompView;

import java.util.Iterator;
import java.util.TreeSet;

public class ChessBoardCompView extends ChessBoardBaseView implements BoardViewCompFace {

	public static final long HINT_REVERSE_DELAY = 1500;

	private static final String DIVIDER_1 = "|";
	private static final String DIVIDER_2 = ":";

	private PostMoveToCompTask computeMoveTask;

	private GameCompFace gameCompActivityFace;
	private ControlsCompView controlsCompView;


	public ChessBoardCompView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setGameActivityFace(GameCompFace gameActivityFace) {
		super.setGameFace(gameActivityFace);
        gameCompActivityFace = gameActivityFace;
    }

	public void setControlsView(ControlsCompView controlsView) {
		super.setControlsView(controlsView);
		controlsCompView = controlsView;
		controlsCompView.setBoardViewFace(this);
	}

	private ChessBoardComp getBoardComp(){
		return ChessBoardComp.getInstance(gameCompActivityFace);
	}

	@Override
	public void afterUserMove() {
		super.afterUserMove();

        getBoardFace().setMovesCount(getBoardFace().getHply());
		gameCompActivityFace.invalidateGameScreen();

		Log.d(CompEngineHelper.TAG, "DEBUGBOARD isGameOver() " + isGameOver());

        if (isGameOver())
            return;

        //if (!getBoardFace().isAnalysis() && !getAppData().isHumanVsHumanGameMode(getBoardFace())) {
		postMoveToEngine(getBoardFace().getLastMove());
		//}
    }

    @Override
	public boolean isGameOver() {
        //saving game for comp game mode if human is playing
        if ((getAppData().isComputerVsHumanGameMode(getBoardFace()) || getAppData().isHumanVsHumanGameMode(getBoardFace()))
                && !getBoardFace().isAnalysis()) {

			StringBuilder builder = new StringBuilder();
			builder.append(getBoardFace().getMode());

			builder.append(" [").append(getBoardFace().getMoveListSAN().toString().replaceAll("\n", " ")).append("] "); // todo: remove debug info

            int i;
            for (i = 0; i < getBoardFace().getMovesCount(); i++) {
                Move move = getBoardFace().getHistDat()[i].move;
				builder.append(DIVIDER_1)
						.append(move.from).append(DIVIDER_2)
						.append(move.to).append(DIVIDER_2)
						.append(move.promote).append(DIVIDER_2)
						.append(move.bits);
            }

			Log.d(CompEngineHelper.TAG, "STORE game " + builder.toString());

			SharedPreferences.Editor editor = preferences.edit();
			editor.putString(getAppData().getUsername() + AppConstants.SAVED_COMPUTER_GAME, builder.toString());
			editor.commit();
        }
		return super.isGameOver();
    }

	public void postMoveToEngine(Move lastMove) {

		Log.d(CompEngineHelper.TAG, "DEBUGBOARD try to postMoveToEngine " + lastMove);

		if (isHint())
			return;

		if (!getAppData().isHumanVsHumanGameMode(getBoardFace()) && !getBoardFace().isAnalysis()) {
			setComputerMoving(true);
			gameCompActivityFace.onCompMove();
		}

		ComputeMoveItem computeMoveItem = new ComputeMoveItem();
		computeMoveItem.setBoardFace(getBoardFace());
		computeMoveItem.setMove(lastMove.toString());

		Log.d(CompEngineHelper.TAG, "make move lastMove " + lastMove);

		computeMoveTask = new PostMoveToCompTask(computeMoveItem, CompEngineHelper.getInstance(), gameCompActivityFace);
		computeMoveTask.execute();
	}

	public void computerMove(final int time) {
		if (isHint())
			return;

		setComputerMoving(true);

	}

	public void makeHint() {
		controlsCompView.enableHintButton(false);

		setHint(true);
		setComputerMoving(true);
//		gameCompActivityFace.onCompMove();

		Log.d(CompEngineHelper.TAG, "ask for move hint");

		gameCompActivityFace.onCompMove();
		CompEngineHelper.getInstance().makeHint();
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

		drawHighlights(canvas);
		drawTrackballDrag(canvas);

		drawPiecesAndAnimation(canvas);
		drawDragPosition(canvas);

		drawMoveHints(canvas); // todo @compengine: move to base class for all game modes
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

					gameCompActivityFace.showChoosePieceDialog(col, row);
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
            return super.processTouchEvent(event);
        }

        track = false;
        if (!getBoardFace().isAnalysis()) {
            if (/*isComputerMoving() ||*/ getBoardFace().isFinished())
                return true;

			/*if ((getAppData().isComputerVsHumanWhiteGameMode(getBoardFace()) && !getBoardFace().isWhiteToMove())
					|| (getAppData().isComputerVsHumanBlackGameMode(getBoardFace()) && getBoardFace().isWhiteToMove())) {
				return true;
			}*/
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
        if (!isComputerMoving()) {// shouldn't be able to flip while comp moving
            getBoardFace().setReside(!getBoardFace().isReside());
            if (getAppData().isComputerVsHumanGameMode(getBoardFace())) {
				int engineMode;
				//if (getAppData().isComputerVsHumanWhiteGameMode(getBoardFace())) {
				if (getBoardFace().isWhiteToMove()) { // supports Flip when user navigated moves Back
                    getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK);
                } else /*if (getAppData().isComputerVsHumanBlackGameMode(getBoardFace()))*/ {
                    getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE);

                }
				getBoardFace().setMovesCount(getBoardFace().getHply()); // supports Flip when user navigated moves Back
				setComputerMoving(true);
				gameCompActivityFace.onCompMove();
				engineMode = CompEngineHelper.mapGameMode(getBoardFace().getMode());
				CompEngineHelper.getInstance().updateEngineGameMode(engineMode);
				CompEngineHelper.getInstance().setGameMode(engineMode);
                //postMoveToEngine(getBoardFace().getLastMove(), false, compStrength);
            }
            invalidate();
        }

		gameCompActivityFace.toggleSides();
		gameCompActivityFace.invalidateGameScreen();
	}

//	@Override
//	public void enableAnalysis() {   // TODO recheck logic
//		gameCompActivityFace.switch2Analysis(true);  // will open new screen
//	}

	@Override
    public void moveBack() {
		boolean blackCompFirstMove =
				getAppData().isComputerVsHumanBlackGameMode(getBoardFace()) && getBoardFace().getHply() == 1;

        if (!isComputerMoving() && noMovesToAnimate() && !navigating && getBoardFace().getHply() > 0 && !blackCompFirstMove) {

			navigating = true;
			CompEngineHelper.getInstance().moveBack();

			getBoardFace().setFinished(false);
            pieceSelected = false;

			setMoveAnimator(getBoardFace().getLastMove(), false);
			resetValidMoves();
			getBoardFace().takeBack();

			Move move = getBoardFace().getLastMove();
			if (move != null && getAppData().isComputerVsHumanGameMode(getBoardFace()) && !getBoardFace().isAnalysis()) {
				setSecondMoveAnimator(new MoveAnimator(move, false));
			}
            invalidate();
			gameCompActivityFace.invalidateGameScreen();
        }
    }

    @Override
    public void moveForward() {
        if (!isComputerMoving() && noMovesToAnimate() && !navigating) {

			pieceSelected = false;

			Move move = getBoardFace().getNextMove();
			if (move == null) {
				return;
			}
			navigating = true;
			CompEngineHelper.getInstance().moveForward();
			setMoveAnimator(move, true);
			resetValidMoves();
			getBoardFace().takeNext();

			if (getAppData().isComputerVsHumanGameMode(getBoardFace()) && !getBoardFace().isAnalysis()) {
				move = getBoardFace().getNextMove();
				if (move != null) {
					setSecondMoveAnimator(new MoveAnimator(move, true));
				}
			}
            invalidate();
			gameCompActivityFace.invalidateGameScreen();
        }
    }

    @Override
    public void showHint() {
        if (!isComputerMoving() && noMovesToAnimate() && !navigating && !isHint()) {
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
