package com.chess.ui.views;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.ui.activities.GameBaseActivity;
import com.chess.ui.activities.GameTacticsScreenActivity;
import com.chess.ui.core.AppConstants;
import com.chess.ui.core.IntentConstants;
import com.chess.ui.core.MainApp;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.engine.Search;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.interfaces.BoardViewFace;
import com.chess.ui.interfaces.GameActivityFace;

import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeSet;

public class ChessBoardView extends ImageView implements BoardViewFace {

	public int W;
	public int H;
	public int side;
	public int square;
	public int from = -1;
	public int to = -1;
	public int dragX = 0;
	public int dragY = 0;
	public int trackX = 0;
	public int trackY = 0;
	private GameActivityFace gameActivityFace;
	private MainApp mainApp;

	//	private ChessBoard boardView;
	private BoardFace boardFace;
	public boolean hint;
	public boolean firstclick = true;
	public boolean compmoving;
	public boolean sel;
	public boolean track;
	public boolean drag;
	public boolean finished;
	public boolean stopThinking;
	private int[] p_tmp;
	private int[] c_tmp;

	private Paint white;
	private Paint black;
	private Paint red;
	private Paint green;

	private String[] signs = {"a", "b", "c", "d", "e", "f", "g", "h"};
	private String[] nums = {"1", "2", "3", "4", "5", "6", "7", "8"};

	private int viewWidth = 0;
	private int viewHeight = 0;


	private Drawable image;


	private float width;
	private float height;
	private Rect rect;

	private GamePanelView gamePanelView;
	//	private PieceItem pieceItem;
	private Resources resources;
	private boolean isHighlightEnabled;
	private boolean showCoordinates;
	private int compStrength;
	private String userName;
    private boolean useTouchTimer;
    private Handler handler;
    private boolean userActive;


    public ChessBoardView(Context context, AttributeSet attrs) {
		super(context, attrs);

		resources = context.getResources();
        handler = new Handler();
		green = new Paint();
		white = new Paint();
		black = new Paint();
		red = new Paint();
		rect = new Rect();
		// captured piece Item
//		pieceItem = new PieceItem();

		white.setStrokeWidth(2.0f);
		white.setStyle(Style.STROKE);
		black.setStrokeWidth(1.0f);
		black.setStyle(Style.FILL);
		red.setStrokeWidth(2.0f);
		red.setStyle(Style.STROKE);
		green.setStrokeWidth(2.0f);
		green.setStyle(Style.STROKE);

		white.setColor(Color.WHITE);
		black.setColor(Color.BLACK);
		red.setColor(Color.RED);
		green.setColor(Color.GREEN);

		width = resources.getDisplayMetrics().widthPixels;
		height = resources.getDisplayMetrics().heightPixels;

		image = resources.getDrawable(R.drawable.chess_back);
		int opacity = resources.getInteger(R.integer.fade_opacity);
//		blackColor ^= (opacity * 0xFF / 100) << 32;
		image.setBounds(0, 0, (int) width, (int) height);
		image.setDither(true);

        Log.d("TEST", "ChessBoardView created, start active timer at " + Calendar.getInstance().getTime().toGMTString());
        handler.postDelayed(checkUserIsActive, StaticData.WAKE_SCREEN_TIMEOUT);
        userActive = false;
	}

	public void setGameActivityFace(GameActivityFace gameActivityFace) {
		this.gameActivityFace = gameActivityFace;
		mainApp = gameActivityFace.getMainApp();

		isHighlightEnabled = mainApp.getSharedData().getBoolean(mainApp.getUserName()
				+ AppConstants.PREF_BOARD_SQUARE_HIGHLIGHT, true);

		showCoordinates = mainApp.getSharedData().getBoolean(mainApp.getUserName()
				+ AppConstants.PREF_BOARD_COORDINATES, true);


		compStrength = mainApp.getSharedData().getInt(mainApp.getUserName()
				+ AppConstants.PREF_COMPUTER_STRENGTH, 0);

		userName = mainApp.getUserName();
	}

	private boolean need2ShowSubmitButtons(){
		String sharedKey;
		sharedKey = mainApp.isLiveChess() ? AppConstants.PREF_SHOW_SUBMIT_MOVE_LIVE : AppConstants.PREF_SHOW_SUBMIT_MOVE;
		return mainApp.getSharedData().getBoolean(mainApp.getUserName()
				+ sharedKey, !mainApp.isLiveChess());
	}

	public void afterMove() {	// TODO handle here analysis moves in comp game
		boardFace.setMovesCount(boardFace.getHply());
		gameActivityFace.update(GameBaseActivity.CALLBACK_REPAINT_UI);	//movelist

		if (MainApp.isLiveOrEchessGameMode(boardFace) && !boardFace.isAnalysis()) {
			if (need2ShowSubmitButtons()) {
				gameActivityFace.showSubmitButtonsLay(true);
				boardFace.setSubmit(true);
			} else {
				gameActivityFace.update(GameBaseActivity.CALLBACK_SEND_MOVE);
			}
		}

		if (!MainApp.isTacticsGameMode(boardFace) && isGameOver())
			return;

		if (!boardFace.isAnalysis()) {
			switch (boardFace.getMode()) {
				case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE: {	//w - human; b - comp
					computerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getUserName() + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
					break;
				}
				case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK: {	//w - comp; b - human
					computerMove(mainApp.strength[compStrength]);
					break;
				}
				case AppConstants.GAME_MODE_TACTICS: {
					gameActivityFace.update(GameTacticsScreenActivity.CALLBACK_CHECK_TACTICS_MOVE);
					break;
				}
				default:
					break;
			}
		}
	}


	boolean isGameOver() {
		//saving game for comp game mode if human is playing
		if ((MainApp.isComputerVsHumanGameMode(boardFace) || MainApp.isHumanVsHumanGameMode(boardFace))
				&& !boardFace.isAnalysis()) {

			String saving = "" + boardFace.getMode();

			int i;
			for (i = 0; i < boardFace.getMovesCount(); i++) {
				Move m = boardFace.getHistDat()[i].m;
				saving += "|" + m.from + ":" + m.to + ":" + m.promote + ":" + m.bits;
			}

			mainApp.getSharedDataEditor().putString(AppConstants.SAVED_COMPUTER_GAME, saving);
			mainApp.getSharedDataEditor().commit();
		}

		// Check available moves
		TreeSet<Move> validMoves = boardFace.gen();

		Iterator<Move> i = validMoves.iterator();
		boolean found = false;
		while (i.hasNext()) {   // compute available moves
			if (boardFace.makeMove(i.next(), false)) {
				boardFace.takeBack();
				found = true;
				break;
			}
		}
		String message = null;
		/*if(	p[0][0]==0 && p[0][1]==0 && p[0][2]==0 && p[0][3]==0 && p[0][4]==0 &&
					p[1][0]==0 && p[1][1]==0 && p[1][2]==0 && p[1][3]==0 && p[1][4]==0){
					message = "0 - 0 Stalemate";
				}*/
		if (!found) {
			if (boardFace.inCheck(boardFace.getSide())) {
				boardFace.getHistDat()[boardFace.getHply() - 1].notation += "#";
				gameActivityFace.update(GameBaseActivity.CALLBACK_REPAINT_UI);

				if (boardFace.getSide() == ChessBoard.LIGHT)
					message = "0 - 1 Black mates";
				else
					message = "1 - 0 White mates";
			} else
				message = "0 - 0 Stalemate";
		} else if (boardFace.reps() == 3 && !mainApp.isLiveChess())
			message = "1/2 - 1/2 Draw by repetition";
		/*else if (fifty >= 100)
					message = "1/2 - 1/2 Draw by fifty move rule";*/
		if (message != null) {
			finished = true;
			mainApp.showToast(message);
			Intent intent = new Intent(IntentConstants.ACTION_SHOW_GAME_END_POPUP);
			intent.putExtra(AppConstants.MESSAGE, "GAME OVER: " + message);
			intent.putExtra(AppConstants.FINISHABLE, false);
			mainApp.sendBroadcast(intent);

			return true;
		}

		if (boardFace.inCheck(boardFace.getSide())) {
			boardFace.getHistDat()[boardFace.getHply() - 1].notation += "+";
			gameActivityFace.update(GameBaseActivity.CALLBACK_REPAINT_UI);

			mainApp.showToast(getContext().getResources().getString(R.string.check));
		}
		return false;
	}

	public void computerMove(final int time) {
		if(boardFace.isAnalysis())
			return;

		if (MainApp.isComputerVsComputerGameMode(boardFace) && stopThinking) {
			stopThinking = false;
			return;
		}

		compmoving = true;
		gameActivityFace.update(GameBaseActivity.CALLBACK_COMP_MOVE);
		new Thread(new Runnable() {
			@Override
			public void run() {
				p_tmp = boardFace.getPieces().clone();
				c_tmp = boardFace.getColor().clone();
				Search searcher = new Search(boardFace);
				searcher.think(0, time, 32);
				Move best = searcher.getBest();
				boardFace.makeMove(best);
				compmoving = false;
				boardFace.setMovesCount(boardFace.getHply());
				update.sendEmptyMessage(0);
			}

			private Handler update = new Handler() {
				@Override
				public void dispatchMessage(Message msg) {
					super.dispatchMessage(msg);
					gameActivityFace.update(GameBaseActivity.CALLBACK_REPAINT_UI);	//movelist
					gameActivityFace.update(GameBaseActivity.CALLBACK_PLAYER_MOVE);
					invalidate();
					if (isGameOver())
						return;
					if (MainApp.isComputerVsComputerGameMode(boardFace)
							|| (hint && !MainApp.isHumanVsHumanGameMode(boardFace))) {
						if (hint)
							hint = false;
						computerMove(time);
					}
				}
			};
		}).start();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			setMeasuredDimension(resolveSize((int) width, widthMeasureSpec),
					resolveSize((int) width, heightMeasureSpec));
		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setMeasuredDimension(resolveSize((int) height, widthMeasureSpec),
					resolveSize((int) height, heightMeasureSpec));
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG));
		super.onDraw(canvas);
		/*W = getWidth();
		H = getHeight();*/
		W = viewWidth;
		H = viewHeight;

		if (H < W) {
			square = viewHeight / 8;
			H -= viewHeight % 8;
			//offsetY = (viewHeight % 8) / 2;
		} else {
			square = viewWidth / 8;
			W -= viewWidth % 8;
			//offsetX = (viewWidth % 8) / 2;
		}
		side = square * 2;

		int i, j;
		for (i = 0; i < 4; i++) {
			for (j = 0; j < 4; j++) {
				try {
					if (mainApp == null || mainApp.getBoardBitmap() == null) {
						throw new Exception();
					}
					rect.set(i * side, j * side, i * side + side, j * side + side);
					canvas.drawBitmap(mainApp.getBoardBitmap(), null,rect
							/*new Rect(i * side, j * side, i * side + side, j * side + side)*/, null);
				} catch (Exception e) {
					e.printStackTrace();
//					Log.d("BoardView", "mainApp " + mainApp);
//					Log.d("BoardView", "mainApp.boardView " + mainApp.getBoardBitmap());
					return;
				}
			}
		}

//		image.draw(canvas);

		if (!compmoving) {
			for (i = 0; i < 64; i++) {
				if (drag && i == from)
					continue;
				int c = boardFace.getColor()[i];
				int p = boardFace.getPieces()[i];
				int x = ChessBoard.COL(i, boardFace.isReside());
				int y = ChessBoard.ROW(i, boardFace.isReside());
				if (c != 6 && p != 6) {	// here is the simple replace/redraw of piece
					rect.set(x * square, y * square, x * square + square, y * square + square);
					canvas.drawBitmap(mainApp.getPiecesBitmaps()[c][p], null,rect, null);
				}
			}
		} else {
			for (i = 0; i < 64; i++) {
				if (drag && i == from)
					continue;
				int c = c_tmp[i];
				int p = p_tmp[i];
				int x = ChessBoard.COL(i, boardFace.isReside());
				int y = ChessBoard.ROW(i, boardFace.isReside());
				if (c != 6 && p != 6) {	 // here is the simple replace/redraw of piece
					rect.set(x * square, y * square, x * square + square, y * square + square);
					canvas.drawBitmap(mainApp.getPiecesBitmaps()[c][p], null,rect, null);
				}
			}
		}

		if (showCoordinates) {
			for (i = 0; i < 8; i++) {
				if (boardFace.isReside()) {
					canvas.drawText(nums[i], 2, i * square + 12, black);
					canvas.drawText(signs[7 - i], i * square + 2, 8 * square - 2, black);
				} else {
					canvas.drawText(nums[7 - i], 2, i * square + 12, black);
					canvas.drawText(signs[i], i * square + 2, 8 * square - 2, black);
				}
			}
		}

		if (isHighlightEnabled && boardFace.getHply() > 0 && !compmoving) {
			Move m = boardFace.getHistDat()[boardFace.getHply() - 1].m;
			int x1 = ChessBoard.COL(m.from, boardFace.isReside());
			int y1 = ChessBoard.ROW(m.from, boardFace.isReside());
			canvas.drawRect(x1 * square, y1 * square, x1 * square + square, y1 * square + square, red);
			int x2 = ChessBoard.COL(m.to, boardFace.isReside());
			int y2 = ChessBoard.ROW(m.to, boardFace.isReside());
			canvas.drawRect(x2 * square, y2 * square, x2 * square + square, y2 * square + square, red);
		}

		if (sel) {
			int x = ChessBoard.COL(from, boardFace.isReside());
			int y = ChessBoard.ROW(from, boardFace.isReside());
			canvas.drawRect(x * square, y * square, x * square + square, y * square + square, white);
		}
		if (drag) {
			int c = boardFace.getColor()[from];
			int p = boardFace.getPieces()[from];
			int x = dragX - square / 2;
			int y = dragY - square / 2;
			int col = (dragX - dragX % square) / square;
			int row = ((dragY + square) - (dragY + square) % square) / square;
			if (c != 6 && p != 6) {
				rect.set(x - square / 2, y - square / 2, x + square + square / 2, y + square + square / 2);
				canvas.drawBitmap(mainApp.getPiecesBitmaps()[c][p], null,rect, null);
				canvas.drawRect(col * square - square / 2, row * square - square / 2,
						col * square + square + square / 2, row * square + square + square / 2, white);
			}
		}
		if (track) {
			int x = (trackX - trackX % square) / square;
			int y = (trackY - trackY % square) / square;
			canvas.drawRect(x * square, y * square, x * square + square, y * square + square, green);
		}

		// Count captured piecesBitmap
		if (!compmoving /*&& !MainApp.isTacticsGameMode(boardFace)*/) {
			gamePanelView.dropAlivePieces();
			for (i = 0; i < 64; i++) {
				int pieceId = boardFace.getPiece(i);
				if (boardFace.getColor()[i] == ChessBoard.LIGHT) {
					gamePanelView.addAlivePiece(true, pieceId);
				} else {
					gamePanelView.addAlivePiece(false, pieceId);
				}
			}
		}
		finishMove();
	}

	private void finishMove() {
		// check all changed piecesBitmap
		gamePanelView.updateCapturedPieces();
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
        if(useTouchTimer){ // start count before next touch
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
					sel = true;
					firstclick = false;
					invalidate();
				}
			} else {
				to = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
				sel = false;
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
				if (found && move != null && boardFace.makeMove(move)) {
					invalidate();
					afterMove();
				} else if (boardFace.getPieces()[to] != 6 && boardFace.getSide() == boardFace.getColor()[to]) {
					sel = true;
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
        if(useTouchTimer){ // start count before next touch
            Log.d("TEST", "onTouchEvent() called in chessboard at " + Calendar.getInstance().getTime().toGMTString());
            handler.postDelayed(checkUserIsActive, StaticData.WAKE_SCREEN_TIMEOUT);
            userActive = true;
        }

		if (square == 0) {
			return super.onTouchEvent(event);
		}

		track = false;
		if (!boardFace.isAnalysis()) {
			if (compmoving || MainApp.isFinishedEchessGameMode(boardFace) || finished || boardFace.isSubmit() ||
					(MainApp.isLiveOrEchessGameMode(boardFace) && boardFace.getHply() < boardFace.getMovesCount()))
				return true;

			if (MainApp.isLiveOrEchessGameMode(boardFace) && mainApp.getCurrentGame() != null) {
				// TODO simplify
				if (mainApp.getCurrentGame().values.get(AppConstants.WHITE_USERNAME).toLowerCase()
						.equals(userName)
						&& boardFace.getMovesCount() % 2 != 0)
					return true;
				if (mainApp.getCurrentGame().values.get(AppConstants.BLACK_USERNAME).toLowerCase()
						.equals(userName)
						&& boardFace.getMovesCount() % 2 == 0)
					return true;
			}

			if ((MainApp.isComputerVsHumanWhiteGameMode(boardFace) && boardFace.getHply() % 2 != 0)
					|| (MainApp.isComputerVsHumanBlackGameMode(boardFace) && boardFace.getHply() % 2 == 0)) {
				return true;
			}
			if (MainApp.isTacticsGameMode(boardFace) && boardFace.getHply() % 2 == 0) {
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
						sel = true;
						firstclick = false;
						invalidate();
					}
				} else {
					int fromPosIndex = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
					if (boardFace.getPieces()[fromPosIndex] != 6 && boardFace.getSide() == boardFace.getColor()[fromPosIndex]) {
						from = fromPosIndex;
						sel = true;
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
				if (!drag && !sel)
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
						sel = true;
						firstclick = false;
						invalidate();
					}
				} else {
					to = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
					sel = false;
					firstclick = true;
					boolean found = false;
					TreeSet<Move> moves = boardFace.gen();
					Iterator<Move> moveIterator = moves.iterator();

					Move move = null;
					while (moveIterator.hasNext()) {
						move = moveIterator.next();	 // search for move that was made
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
						sel = true;
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
            if(userActive){
                userActive = false;
                Log.d("TEST", "checkUserIsActive user is active, postpone at " + Calendar.getInstance().getTime().toGMTString());
                handler.removeCallbacks(this);
                handler.postDelayed(this, StaticData.WAKE_SCREEN_TIMEOUT);
            }else
                gameActivityFace.turnScreenOff();

        }
    };
    

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
		if (found && move != null && boardFace.makeMove(move)) {
			invalidate();
			afterMove();
		} else if (boardFace.getPieces()[to] != 6 && boardFace.getSide() == boardFace.getColor()[to]) {
			sel = true;
			firstclick = false;
			from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
			invalidate();
		} else {
			invalidate();
		}
	}

	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		viewWidth = (xNew == 0 ? viewWidth : xNew);
		viewHeight = (yNew == 0 ? viewHeight : yNew);
	}

	public BoardFace getBoardFace() {
		return boardFace;
	}

	public void setBoardFace(ChessBoard boardFace) {
		this.boardFace = boardFace;
	}


	public GamePanelView getGamePanelView() {
		return gamePanelView;
	}

	public void setGamePanelView(GamePanelView gamePanelView) {
		this.gamePanelView = gamePanelView;
		this.gamePanelView.setBoardViewFace(this);
	}

	@Override
	public void showOptions() {
		gameActivityFace.showOptions();
	}

	@Override
	public void flipBoard() {
		stopThinking = true;
		if (!compmoving) {
			getBoardFace().setReside(!getBoardFace().isReside());
			if (MainApp.isComputerVsHumanGameMode(getBoardFace())) {
				if (MainApp.isComputerVsHumanWhiteGameMode(getBoardFace())) {
					getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK);
				} else if (MainApp.isComputerVsHumanBlackGameMode(getBoardFace())) {
					getBoardFace().setMode(AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE);
				}
				//getBoardFaceFace().mode ^= 1;
				computerMove(mainApp.strength[mainApp.getSharedData()
						.getInt(mainApp.getUserName()
								+ AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
			}
			invalidate();
			gameActivityFace.update(GameBaseActivity.CALLBACK_REPAINT_UI);
		}
	}

	@Override
	public void switchAnalysis() {
		boolean isAnalysis = getBoardFace().toggleAnalysis();

		gamePanelView.toggleControlButton(GamePanelView.B_ANALYSIS_ID, isAnalysis);
		gameActivityFace.switch2Analysis(isAnalysis); //  update(GameBaseActivity.CALLBACK_REPAINT_UI);
	}

	@Override
	public void switchChat() {
		gameActivityFace.switch2Chat();
	}

	@Override
	public void moveBack() {
		stopThinking = true;
		if (!compmoving) {
			finished = false;
			sel = false;
			getBoardFace().takeBack();
			invalidate();
			gameActivityFace.update(GameBaseActivity.CALLBACK_REPAINT_UI);
		}
	}

	@Override
	public void moveForward() {
		stopThinking = true;
		if (!compmoving) {
			sel = false;
			getBoardFace().takeNext();
			invalidate();
			gameActivityFace.update(GameBaseActivity.CALLBACK_REPAINT_UI);
		}
	}

	@Override
	public void showHint() {
		stopThinking = true;
		if (!compmoving) {
			hint = true;
			computerMove(mainApp.strength[mainApp.getSharedData()
					.getInt(mainApp.getUserName()
							+ AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
		}
	}

	@Override
	public void newGame() {
		gameActivityFace.newGame();
	}

	public void addMove2Log(CharSequence move) {
		gamePanelView.addMoveLog(move);
	}

    public void enableTouchTimer() {
        useTouchTimer = true;
    }
}
