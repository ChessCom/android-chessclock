package com.chess.ui.views.chess_boards;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import com.chess.model.ComputeMoveItem;
import com.chess.statics.AppConstants;
import com.chess.statics.StaticData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.ChessBoardComp;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.stockfish.CompEngineHelper;
import com.chess.ui.engine.stockfish.PostMoveToCompTask;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.boards.BoardViewCompFace;
import com.chess.ui.interfaces.game_ui.GameCompFace;
import com.chess.ui.views.game_controls.ControlsCompView;

import java.util.List;

public class ChessBoardCompView extends ChessBoardBaseView implements BoardViewCompFace {

	public static final long HINT_REVERSE_DELAY = 500;

	private static final String DIVIDER_1 = "|";
	private static final String DIVIDER_2 = ":";

	private PostMoveToCompTask computeMoveTask;

	private GameCompFace gameCompFace;
	private ControlsCompView controlsCompView;


	public ChessBoardCompView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setGameUiFace(GameCompFace gameActivityFace) {
		super.setGameFace(gameActivityFace);
		gameCompFace = gameActivityFace;
	}

	public void setControlsView(ControlsCompView controlsView) {
		super.setControlsView(controlsView);
		controlsCompView = controlsView;
		controlsCompView.setBoardViewFace(this);
	}

	private ChessBoardComp getBoardComp() {
		return (ChessBoardComp) getBoardFace();
	}

	@Override
	public void afterUserMove() {
		super.afterUserMove();

		getBoardFace().setMovesCount(getBoardFace().getPly());
		gameCompFace.invalidateGameScreen();
//		gameCompFace.updateAfterMove(); // do nothing

		boolean gameOver = isGameOver(); // do not show popup twice
		Log.d(CompEngineHelper.TAG, "DEBUGBOARD isGameOver() " + gameOver);

		if (gameOver) {
			return;
		}

		postMoveToEngine(getBoardFace().getLastMove()); // TODO on slow device we can touch piece while comp is loading, prevent in future
	}

	@Override
	public boolean isGameOver() {
		if (!isHint()) {
			saveCompGame();
		}
		return super.isGameOver();
	}

	private void saveCompGame() {
		//saving game for comp game mode if human is playing
		if ((ChessBoard.isComputerVsHumanGameMode(getBoardFace()) || ChessBoard.isHumanVsHumanGameMode(getBoardFace()))
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

			Log.d(CompEngineHelper.TAG, "STORE game " + builder.toString());

			appData.setSavedCompGame(builder.toString());

			// todo: check analysis
			CompEngineHelper.getInstance().storeEngineState();
		}
	}

	public void postMoveToEngine(Move lastMove) {

		Log.d(CompEngineHelper.TAG, "DEBUGBOARD try to postMoveToEngine " + lastMove);

		if (isHint())
			return;

		if (!ChessBoard.isHumanVsHumanGameMode(getBoardFace()) && !getBoardFace().isAnalysis()) {
			setComputerMoving(true);
			gameCompFace.onCompMove();
		}

		ComputeMoveItem computeMoveItem = new ComputeMoveItem();
		computeMoveItem.setBoardFace(getBoardFace());
		computeMoveItem.setMove(lastMove.toString());

		Log.d(CompEngineHelper.TAG, "make move lastMove " + lastMove);

		computeMoveTask = new PostMoveToCompTask(computeMoveItem, CompEngineHelper.getInstance(), gameCompFace);
		computeMoveTask.execute();
	}

	public void computerMove() {
		if (isHint()) {
			return;
		}

		setComputerMoving(true);

	}

	private void makeHint() {
		controlsCompView.enableHintButton(false);

		setHint(true);
		setComputerMoving(true);

//		Log.d(CompEngineHelper.TAG, "ask for move hint");

		gameCompFace.onCompMove();
		CompEngineHelper.getInstance().makeHint();
	}

	public void stopComputerMove() {
		if (computeMoveTask != null) {
			setComputerMoving(false);
			computeMoveTask.cancel(true);
		}
	}

//	@Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//
//		// TODO restore when needed
////		drawMoveHints(canvas); // todo @compengine: move to base class for all game modes
//    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (useTouchTimer) { // start count before next touch
			handler.postDelayed(checkUserIsActive, StaticData.WAKE_SCREEN_TIMEOUT);
			userActive = true;
		}

		if (squareSize == 0) {
			return super.processTouchEvent(event);
		}

		trackTouchEvent = false;
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
	public void promote(int promote, int file, int rank) {
		boolean found = false;
		Move move = null;
		List<Move> moves = getBoardFace().generateLegalMoves();
		for (Move move1 : moves) {
			move = move1;
			if (move.from == fromSquare && move.to == toSquare && move.promote == promote) {
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
		} else if (getBoardFace().getPiece(toSquare) != ChessBoard.EMPTY
				&& getBoardFace().getSide() == getBoardFace().getColor(toSquare)) {
			pieceSelected = true;
			firstClick = false;
			fromSquare = ChessBoard.getPositionIndex(file, rank, getBoardFace().isReside());
		}
		invalidateMe();
	}

	@Override
	public void flipBoard() {
		if (!isComputerMoving()) {
			getBoardFace().setReside(!getBoardFace().isReside());
			if (ChessBoard.isComputerVsHumanGameMode(getBoardFace())) {
				int engineMode;
				//if (getAppData().isComputerVsHumanWhiteGameMode(getBoardFace())) {
				if (getBoardFace().isWhiteToMove()) { // supports Flip when user navigated moves Back
					getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_BLACK);
				} else /*if (getAppData().isComputerVsHumanBlackGameMode(getBoardFace()))*/ {
					getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_PLAYER_WHITE);
				}
				getBoardFace().setMovesCount(getBoardFace().getPly()); // supports Flip when user navigated moves Back
				setComputerMoving(true);
				gameCompFace.onCompMove();
				engineMode = CompEngineHelper.mapGameMode(getBoardFace().getMode());
				CompEngineHelper.getInstance().updateEngineGameMode(engineMode);
				CompEngineHelper.getInstance().setGameMode(engineMode);
				//postMoveToEngine(getBoardFace().getLastMove(), false, compStrength);
			}
		}

		gameCompFace.toggleSides();
		gameCompFace.invalidateGameScreen();
	}

	@Override
	public boolean moveBack() {
		boolean blackCompFirstMove =
				ChessBoard.isComputerVsHumanBlackGameMode(getBoardFace()) && getBoardFace().getPly() == 1;

		if (!isComputerMoving() && noMovesToAnimate() /*&& !navigating*/ && getBoardFace().getPly() > 0 && !blackCompFirstMove) {

			//navigating = true;
			CompEngineHelper.getInstance().moveBack();

			getBoardFace().setFinished(false);
			pieceSelected = false;

			setMoveAnimator(getBoardFace().getLastMove(), false);
			resetValidMoves();
			getBoardFace().takeBack();

			Move move = getBoardFace().getLastMove();
			if (move != null && ChessBoard.isComputerVsHumanGameMode(getBoardFace()) && !getBoardFace().isAnalysis()) {
				setSecondMoveAnimator(new MoveAnimator(move, false));
			}
			gameCompFace.invalidateGameScreen();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean moveForward() {
		if (!isComputerMoving() && noMovesToAnimate() /*&& !navigating*/) {

			pieceSelected = false;

			Move move = getBoardFace().getNextMove();
			if (move == null) {
				return false;
			}
			//navigating = true;
			CompEngineHelper.getInstance().moveForward();
			setMoveAnimator(move, true);
			resetValidMoves();
			getBoardFace().takeNext();

			if (ChessBoard.isComputerVsHumanGameMode(getBoardFace()) && !getBoardFace().isAnalysis()) {
				move = getBoardFace().getNextMove();
				if (move != null) {
					setSecondMoveAnimator(new MoveAnimator(move, true));
				}
			}
			gameCompFace.invalidateGameScreen();

			return true;
		} else {
			return false;
		}
	}

	@Override
	public void goToMove(Integer pos) {
		resetValidMoves();
		BoardFace boardFace = getBoardFace();
		int totalHply = boardFace.getPly() - 1;

		CompEngineHelper.getInstance().stopCompMoving();

		if (totalHply < pos) {
			for (int i = totalHply; i < pos; i++) {
				boardFace.takeNext(false);
				CompEngineHelper.getInstance().moveForwardHalf();
			}
		} else {
			for (int i = totalHply; i > pos; i--) {
				boardFace.takeBack();
				CompEngineHelper.getInstance().moveBackHalf();
			}
		}
		CompEngineHelper.getInstance().updateEngineAfterHalfMove();
	}

	public void updateBoardPosition(Integer pos) {
		super.goToMove(pos);
	}

	@Override
	public void computer() {
		gameCompFace.computer();
	}

	@Override
	public void showHint() {
		if (getBoardFace().isFinished()) { // don't show hints for finished games
			return;
		}

		if (!isComputerMoving() && !isComputerToMove() && noMovesToAnimate()) {
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

	@Override
	protected void onSecondMoveAnimated() {
		gameCompFace.invalidateGameScreen();
	}

	public boolean isComputerToMove() {
		return (ChessBoard.isComputerVsComputerGameMode(getBoardFace()))
				|| (ChessBoard.isComputerVsHumanWhiteGameMode(getBoardFace()) && !getBoardFace().isWhiteToMove())
				|| (ChessBoard.isComputerVsHumanBlackGameMode(getBoardFace()) && getBoardFace().isWhiteToMove());
	}
}
