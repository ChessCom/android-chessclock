package com.chess.ui.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import com.chess.R;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.live.client.PieceColor; // or create similar enum
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.interfaces.BoardViewFace;
import com.chess.ui.interfaces.GameActivityFace;
import org.petero.droidfish.gamelogic.Position;

import java.util.*;

/**
 * ChessBoardBaseView class
 *
 * @author alien_roger
 * @created at: 25.04.12 10:54
 */
public abstract class ChessBoardBaseView extends ImageView implements BoardViewFace {

	public static final int P_ALPHA_ID = 0;
	public static final int P_BOOK_ID = 1;
	public static final int P_CASES_ID = 2;
	public static final int P_CLASSIC_ID = 3;
	public static final int P_CLUB_ID = 4;
	public static final int P_CONDAL_ID = 5;
	public static final int P_MAYA_ID = 6;
	public static final int P_MODERN_ID = 7;
	public static final int P_VINTAGE_ID = 8;

	int pieceXDelta, pieceYDelta; // top/left pixel draw position relative to square

	protected Bitmap[][] piecesBitmaps;
	protected Bitmap boardBitmap;
	protected SharedPreferences preferences;

	protected boolean finished;
	protected boolean firstclick = true;
	protected boolean pieceSelected;
	protected boolean track;
	protected boolean drag;

	protected int W;
	protected int H;
	protected int side;
	protected int square;
	protected int from = -1;
	protected int to = -1;
	protected int dragX = 0;
	protected int dragY = 0;
	protected int trackX = 0;
	protected int trackY = 0;

	protected Paint whitePaint;
	protected Paint coordinatesPaint;
	protected Paint redPaint;
	protected Paint greenPaint;

	protected String[] signs = {"a", "b", "c", "d", "e", "f", "g", "h"};
	protected String[] nums = {"1", "2", "3", "4", "5", "6", "7", "8"};

	protected int viewWidth = 0;
	protected int viewHeight = 0;

	protected float width;
	protected float height;
	protected Rect rect;

	protected GamePanelView gamePanelView;
	protected boolean isHighlightEnabled;
	protected boolean showCoordinates;
	protected String userName;
	protected boolean useTouchTimer;
	protected Handler handler;
	protected boolean userActive;
	protected Resources resources;
	protected GameActivityFace gameActivityFace;
	protected BoardFace boardFace;
	protected boolean locked;
	protected PaintFlagsDrawFilter drawFilter;
	private float density;

	private HashMap<org.petero.droidfish.gamelogic.Move, PieceColor> moveHints =
			new HashMap<org.petero.droidfish.gamelogic.Move, PieceColor>();
	private Paint whiteMoveArrowPaint;
	private Paint blackMoveArrowPaint;
	protected LinkedList<MoveAnimator> movesToAnimate = new LinkedList<MoveAnimator>();

	public ChessBoardBaseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		resources = context.getResources();
		density = resources.getDisplayMetrics().density;

		drawFilter = new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG);

		loadBoard(AppData.getChessBoardId(getContext()));
		loadPieces(AppData.getPiecesId(getContext()));

		handler = new Handler();
		greenPaint = new Paint();
		whitePaint = new Paint();
		coordinatesPaint = new Paint();
		redPaint = new Paint();
		rect = new Rect();

		whitePaint.setStrokeWidth(2.0f);
		whitePaint.setStyle(Style.STROKE);
		whitePaint.setColor(Color.WHITE);

		Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/Roboto-Regular.ttf");
		int coordinateFont = getResources().getInteger(R.integer.board_highlight_font);

		coordinatesPaint.setStrokeWidth(1.0f);
		coordinatesPaint.setStyle(Style.FILL);
		coordinatesPaint.setColor(Color.WHITE);
		coordinatesPaint.setShadowLayer(1.5f, 1.0f, 1.5f, 0xFF000000);
		coordinatesPaint.setTextSize(coordinateFont * density);
		coordinatesPaint.setTypeface(typeface);

		redPaint.setStrokeWidth(2.0f);
		redPaint.setStyle(Style.STROKE);
		redPaint.setColor(Color.RED);

		greenPaint.setStrokeWidth(2.0f);
		greenPaint.setStyle(Style.STROKE);
		greenPaint.setColor(Color.GREEN);


		width = resources.getDisplayMetrics().widthPixels;
		height = resources.getDisplayMetrics().heightPixels;

		handler.postDelayed(checkUserIsActive, StaticData.WAKE_SCREEN_TIMEOUT);
		userActive = false;

		preferences = AppData.getPreferences(getContext());

		whiteMoveArrowPaint = initMoveArrowPaint(Color.WHITE);
		blackMoveArrowPaint = initMoveArrowPaint(Color.BLACK);

		pieceXDelta = -1;
		pieceYDelta = -1;
	}

	public void setGameActivityFace(GameActivityFace gameActivityFace) {
		this.gameActivityFace = gameActivityFace;
		userName = AppData.getUserName(getContext());

		isHighlightEnabled = AppData.isHighlightEnabled(getContext());

		showCoordinates = AppData.showCoordinates(getContext());
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

		pieceXDelta = -1;
		pieceYDelta = -1;
	}

	public BoardFace getBoardFace() {
		return boardFace;
	}

	public void setBoardFace(BoardFace boardFace) {
		this.boardFace = boardFace;
		onBoardFaceSet(boardFace);
	}

	protected void onBoardFaceSet(BoardFace boardFace) {
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
		getBoardFace().setReside(!getBoardFace().isReside());

		invalidate();
		gameActivityFace.invalidateGameScreen();
	}

	@Override
	public void moveBack() {
		finished = false;
		pieceSelected = false;
		getBoardFace().takeBack();
		invalidate();
		gameActivityFace.invalidateGameScreen();
	}

	@Override
	public void switchAnalysis() {

		boolean isAnalysis = getBoardFace().toggleAnalysis();

		Log.d("", "Analysis setpos switchAnalysis  " + boardFace.isAnalysis());

		gamePanelView.toggleControlButton(GamePanelView.B_ANALYSIS_ID, isAnalysis);
		gameActivityFace.switch2Analysis(isAnalysis);
	}

	public void enableAnalysis() {
		gamePanelView.toggleControlButton(GamePanelView.B_ANALYSIS_ID, true);
		gamePanelView.enableAnalysisMode(true);
		gameActivityFace.switch2Analysis(true);
	}

	public void disableAnalysis() { // probably could be refactored to enableAnalysis(boolean enable)
		gamePanelView.toggleControlButton(GamePanelView.B_ANALYSIS_ID, false);
		gamePanelView.enableAnalysisMode(false);
		gameActivityFace.switch2Analysis(false);
	}

//	@Override
//	public boolean isInAnalysis() {
//		return getBoardFace().isAnalysis();
//	}

	@Override
	public void moveForward() {
		pieceSelected = false;
		getBoardFace().takeNext();
		invalidate();
		gameActivityFace.invalidateGameScreen();
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public boolean isFinished() {
		return finished;
	}

	@Override
	public void newGame() {
		gameActivityFace.newGame();
	}

	public void setMovesLog(CharSequence move) {
		gamePanelView.setMovesLog(move);
		invalidate();
		requestFocus();
	}

	public void enableTouchTimer() {
		useTouchTimer = true;
	}

	protected Runnable checkUserIsActive = new Runnable() {
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

	protected void drawBoard(Canvas canvas) {
		W = viewWidth;
		H = viewHeight;

		if (H < W) {
			square = viewHeight / 8;
			H -= viewHeight % 8;
		} else {
			square = viewWidth / 8;
			W -= viewWidth % 8;
		}

		side = square * 2;

		int i, j;
		for (i = 0; i < 4; i++) {
			for (j = 0; j < 4; j++) {
				rect.set(i * side, j * side, i * side + side, j * side + side);
				canvas.drawBitmap(boardBitmap, null, rect, null);
			}
		}
	}

	protected void drawPieces(Canvas canvas, boolean animationActive, MoveAnimator moveAnimator) {
		int i;
		for (i = 0; i < 64; i++) {
			if (drag && i == from) {
				continue;
			}

			if (animationActive && i == moveAnimator.hide1) {
				if (moveAnimator.getCapturedPieceBitmap() != null) {
					// todo: refactor
					int x = ChessBoard.getColumn(i, boardFace.isReside());
					int y = ChessBoard.getRow(i, boardFace.isReside());
					rect.set(x * square, y * square, x * square + square, y * square + square);
					canvas.drawBitmap(moveAnimator.getCapturedPieceBitmap(), null, rect, null);
				}
				continue;
			}

			int color = boardFace.getColor()[i];
			int piece = boardFace.getPieces()[i];
			int x = ChessBoard.getColumn(i, boardFace.isReside());
			int y = ChessBoard.getRow(i, boardFace.isReside());

//			Log.d("TEST", "piece redraw color" + color);
			// TODO rework logic to store changed pieces and redraw only them
			if (color != ChessBoard.EMPTY && piece != ChessBoard.EMPTY) {    // here is the simple replace/redraw of piece
				rect.set(x * square, y * square, x * square + square, y * square + square);
				canvas.drawBitmap(piecesBitmaps[color][piece], null, rect, null);
			}
		}
	}

	protected void drawCoordinates(Canvas canvas) {
		int i;
		if (showCoordinates) {
			float numYShift = 15 * density;
			float textYShift = 3 * density;
			for (i = 0; i < 8; i++) {
				if (boardFace.isReside()) {
					canvas.drawText(nums[i], 2, i * square + numYShift, coordinatesPaint);
					canvas.drawText(signs[7 - i], i * square + textYShift, 8 * square - textYShift, coordinatesPaint);
				} else {
					canvas.drawText(nums[7 - i], 2, i * square + numYShift, coordinatesPaint);
					canvas.drawText(signs[i], i * square + textYShift, 8 * square - textYShift, coordinatesPaint);
				}
			}
		}
	}

	protected void drawHighlight(Canvas canvas) {
		if (isHighlightEnabled && boardFace.getHply() > 0) { // draw moved piece highlight from -> to
			Move move = boardFace.getHistDat()[boardFace.getHply() - 1].move;
			int x1 = ChessBoard.getColumn(move.from, boardFace.isReside());
			int y1 = ChessBoard.getRow(move.from, boardFace.isReside());
			canvas.drawRect(x1 * square, y1 * square, x1 * square + square, y1 * square + square, redPaint);
			int x2 = ChessBoard.getColumn(move.to, boardFace.isReside());
			int y2 = ChessBoard.getRow(move.to, boardFace.isReside());
			canvas.drawRect(x2 * square, y2 * square, x2 * square + square, y2 * square + square, redPaint);
		}

		if (pieceSelected) { // draw rectangle around the start move piece position
			int x = ChessBoard.getColumn(from, boardFace.isReside());
			int y = ChessBoard.getRow(from, boardFace.isReside());
			canvas.drawRect(x * square, y * square, x * square + square, y * square + square, whitePaint);
		}
	}

	protected void drawDragPosition(Canvas canvas) {
		if (drag) {
			int c = boardFace.getColor()[from];
			int p = boardFace.getPieces()[from];
			int halfSquare = square / 2;
			int x = dragX - halfSquare;
			int y = dragY - halfSquare;
			int col = (dragX - dragX % square) / square;
			int row = ((dragY + square) - (dragY + square) % square) / square;
			if (c != ChessBoard.EMPTY && p != ChessBoard.EMPTY) {
				rect.set(x - halfSquare, y - halfSquare, x + square + halfSquare, y + square + halfSquare);
				canvas.drawBitmap(piecesBitmaps[c][p], null, rect, null);
				canvas.drawRect(col * square - halfSquare, row * square - halfSquare,
						col * square + square + halfSquare, row * square + square + halfSquare, whitePaint);
			}
		}
	}

	protected void drawTrackballDrag(Canvas canvas) {
		if (track) {
			int x = (trackX - trackX % square) / square;
			int y = (trackY - trackY % square) / square;
			canvas.drawRect(x * square, y * square, x * square + square, y * square + square, greenPaint);
		}
	}

	protected void drawCapturedPieces() {
		// Count captured piecesBitmap
		gamePanelView.dropAlivePieces();

		for (int i = 0; i < 64; i++) {
			int pieceId = boardFace.getPiece(i);
			if (boardFace.getColor()[i] == ChessBoard.LIGHT) {
				gamePanelView.addAlivePiece(true, pieceId);
			} else {
				gamePanelView.addAlivePiece(false, pieceId);
			}
		}
		gamePanelView.updateCapturedPieces();
	}

//	protected void loadIdsFromResources(){  // TODO reuse later
//		TypedArray ar = getResources().obtainTypedArray(R.array.comp_strength);
//
//		int len = ar.length();
//		int[] resIds = new int[len];
//		for (int i = 0; i < len; i++)
//			resIds[i] = ar.getResourceId(i, 0);
//
//		ar.recycle();
//
//		// Do stuff with resolved reference array, resIds[]...
//		for (int i = 0; i < len; i++)
//			Log.v("TEST", "Res Id " + i + " is " + Integer.toHexString(resIds[i]));
//	}

    protected boolean processTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    protected boolean isLocked() {
        return locked || !gameActivityFace.currentGameExist();
    }

    @Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				return onActionDown(event);
			}
			case MotionEvent.ACTION_MOVE: {
				return onActionMove(event);
			}
			case MotionEvent.ACTION_UP: {
				return onActionUp(event);
			}
		}
		return super.onTouchEvent(event);
	}

	protected boolean onActionDown(MotionEvent event) {
		if (square == 0) {
			return false;
		}
		int col = (int) (event.getX() - event.getX() % square) / square;
		int row = (int) (event.getY() - event.getY() % square) / square;
		if (col > 7 || col < 0 || row > 7 || row < 0) {
			invalidate();
			return false;
		}
		if (firstclick) {
			from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
			if (boardFace.getPieces()[from] != ChessBoard.EMPTY && boardFace.getSide() == boardFace.getColor()[from]) {
				pieceSelected = true;
				firstclick = false;
				invalidate();
			}
		} else {
			int fromPosIndex = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
			if (boardFace.getPieces()[fromPosIndex] != ChessBoard.EMPTY && boardFace.getSide() == boardFace.getColor()[fromPosIndex]) {
				from = fromPosIndex;
				pieceSelected = true;
				firstclick = false;
				invalidate();
			}
		}
		return true;
	}

	protected boolean onActionMove(MotionEvent event){
		dragX = (int) event.getX();
		dragY = (int) event.getY() - square;
		int col = (dragX - dragX % square) / square;
		int row = (dragY - dragY % square) / square;
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

	protected boolean onActionUp(MotionEvent event) {
		int col = (int) (event.getX() - event.getX() % square) / square;
		int row = (int) (event.getY() - event.getY() % square) / square;

		drag = false;
		// if outside of the boardBitmap - return
		if (col > 7 || col < 0 || row > 7 || row < 0) { // if touched out of board
			invalidate();
			return false;
		}
		if (firstclick) {
			from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
			if (boardFace.getPieces()[from] != ChessBoard.EMPTY && boardFace.getSide() == boardFace.getColor()[from]) {
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

			// todo: show move animation when player makes move by click, and do not show for drag
			if (found && boardFace.makeMove(move)) {
				afterMove();
			} else if (boardFace.getPieces()[to] != ChessBoard.EMPTY && boardFace.getSide() == boardFace.getColor()[to]) {
				pieceSelected = true;
				firstclick = false;
				from = ChessBoard.getPositionIndex(col, row, boardFace.isReside());
			}
			invalidate();
		}
		return true;
	}


	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		viewWidth = (xNew == 0 ? viewWidth : xNew);
		viewHeight = (yNew == 0 ? viewHeight : yNew);
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

	protected boolean isGameOver() {

		String message = null;
		if (!boardFace.isAnalysis()) {
			if (!boardFace.isPossibleToMakeMoves()) {
				if (boardFace.inCheck(boardFace.getSide())) {
					if (boardFace.getSide() == ChessBoard.LIGHT)
						message = getResources().getString(R.string.black_wins);
					else
						message = getResources().getString(R.string.white_wins);
				} else
					message = getResources().getString(R.string.draw_by_stalemate);
			} else if (boardFace.reps() == 3)
				message = getResources().getString(R.string.draw_by_3fold_repetition);
		} else if (boardFace.inCheck(boardFace.getSide())) {

			if (!boardFace.isPossibleToMakeMoves()) {
				boardFace.getHistDat()[boardFace.getHply() - 1].notation += "#";
				gameActivityFace.invalidateGameScreen();
				finished = true;
				return true;
			} else {
				boardFace.getHistDat()[boardFace.getHply() - 1].notation += "+";
				gameActivityFace.invalidateGameScreen();
				gameActivityFace.onCheck();
			}
		}

		if (message != null) {
			finished = true;
			gameActivityFace.onGameOver(message, false);
			return true;
		}

		return false;
	}

	protected abstract void afterMove();

	private int[] boardsDrawables = {
			R.drawable.wood_dark,
			R.drawable.wood_light,
			R.drawable.blue,
			R.drawable.brown,
			R.drawable.green,
			R.drawable.grey,
			R.drawable.marble,
			R.drawable.red,
			R.drawable.tan
	};

	public void lockBoard(boolean lock) {
		locked = lock;
        gamePanelView.lock(lock);
		setEnabled(!lock);
	}

	protected void loadBoard(int boardId) {
		boardBitmap = ((BitmapDrawable) resources.getDrawable(boardsDrawables[boardId])).getBitmap();
	}

	private void setPieceBitmapFromArray(int[] drawableArray) {
		piecesBitmaps = new Bitmap[2][6];
		Resources resources = getResources();
		for (int j = 0; j < 6; j++) {
			piecesBitmaps[0][j] = ((BitmapDrawable) resources.getDrawable(drawableArray[j])).getBitmap();
		}
		for (int j = 0; j < 6; j++) {
			piecesBitmaps[1][j] = ((BitmapDrawable) resources.getDrawable(drawableArray[6 + j])).getBitmap();
		}
	}

	protected void loadPieces(int piecesSetId) {
		switch (piecesSetId) {
			case P_ALPHA_ID:
				setPieceBitmapFromArray(alphaPiecesDrawableIds);
				break;
			case P_BOOK_ID:
				setPieceBitmapFromArray(bookPiecesDrawableIds);
				break;
			case P_CASES_ID:
				setPieceBitmapFromArray(casesPiecesDrawableIds);
				break;
			case P_CLASSIC_ID:
				setPieceBitmapFromArray(classicPiecesDrawableIds);
				break;
			case P_CLUB_ID:
				setPieceBitmapFromArray(clubPiecesDrawableIds);
				break;
			case P_CONDAL_ID:
				setPieceBitmapFromArray(condalPiecesDrawableIds);
				break;
			case P_MAYA_ID:
				setPieceBitmapFromArray(mayaPiecesDrawableIds);
				break;
			case P_MODERN_ID:
				setPieceBitmapFromArray(modernPiecesDrawableIds);
				break;
			case P_VINTAGE_ID:
				setPieceBitmapFromArray(vintagePiecesDrawableIds);
				break;
		}
	}

	private int[] alphaPiecesDrawableIds = new int[]{
			R.drawable.alpha_wp,
			R.drawable.alpha_wn,
			R.drawable.alpha_wb,
			R.drawable.alpha_wr,
			R.drawable.alpha_wq,
			R.drawable.alpha_wk,
			R.drawable.alpha_bp,
			R.drawable.alpha_bn,
			R.drawable.alpha_bb,
			R.drawable.alpha_br,
			R.drawable.alpha_bq,
			R.drawable.alpha_bk,
	};

	private int[] bookPiecesDrawableIds = new int[]{
			R.drawable.book_wp,
			R.drawable.book_wn,
			R.drawable.book_wb,
			R.drawable.book_wr,
			R.drawable.book_wq,
			R.drawable.book_wk,
			R.drawable.book_bp,
			R.drawable.book_bn,
			R.drawable.book_bb,
			R.drawable.book_br,
			R.drawable.book_bq,
			R.drawable.book_bk,
	};

	private int[] casesPiecesDrawableIds = new int[]{
			R.drawable.cases_wp,
			R.drawable.cases_wn,
			R.drawable.cases_wb,
			R.drawable.cases_wr,
			R.drawable.cases_wq,
			R.drawable.cases_wk,
			R.drawable.cases_bp,
			R.drawable.cases_bn,
			R.drawable.cases_bb,
			R.drawable.cases_br,
			R.drawable.cases_bq,
			R.drawable.cases_bk,
	};

	private int[] classicPiecesDrawableIds = new int[]{
			R.drawable.classic_wp,
			R.drawable.classic_wn,
			R.drawable.classic_wb,
			R.drawable.classic_wr,
			R.drawable.classic_wq,
			R.drawable.classic_wk,
			R.drawable.classic_bp,
			R.drawable.classic_bn,
			R.drawable.classic_bb,
			R.drawable.classic_br,
			R.drawable.classic_bq,
			R.drawable.classic_bk,
	};

	private int[] clubPiecesDrawableIds = new int[]{
			R.drawable.club_wp,
			R.drawable.club_wn,
			R.drawable.club_wb,
			R.drawable.club_wr,
			R.drawable.club_wq,
			R.drawable.club_wk,
			R.drawable.club_bp,
			R.drawable.club_bn,
			R.drawable.club_bb,
			R.drawable.club_br,
			R.drawable.club_bq,
			R.drawable.club_bk,
	};


	private int[] condalPiecesDrawableIds = new int[]{
			R.drawable.condal_wp,
			R.drawable.condal_wn,
			R.drawable.condal_wb,
			R.drawable.condal_wr,
			R.drawable.condal_wq,
			R.drawable.condal_wk,
			R.drawable.condal_bp,
			R.drawable.condal_bn,
			R.drawable.condal_bb,
			R.drawable.condal_br,
			R.drawable.condal_bq,
			R.drawable.condal_bk,
	};

	private int[] mayaPiecesDrawableIds = new int[]{
			R.drawable.maya_wp,
			R.drawable.maya_wn,
			R.drawable.maya_wb,
			R.drawable.maya_wr,
			R.drawable.maya_wq,
			R.drawable.maya_wk,
			R.drawable.maya_bp,
			R.drawable.maya_bn,
			R.drawable.maya_bb,
			R.drawable.maya_br,
			R.drawable.maya_bq,
			R.drawable.maya_bk,
	};

	private int[] modernPiecesDrawableIds = new int[]{
			R.drawable.modern_wp,
			R.drawable.modern_wn,
			R.drawable.modern_wb,
			R.drawable.modern_wr,
			R.drawable.modern_wq,
			R.drawable.modern_wk,
			R.drawable.modern_bp,
			R.drawable.modern_bn,
			R.drawable.modern_bb,
			R.drawable.modern_br,
			R.drawable.modern_bq,
			R.drawable.modern_bk,
	};

	private int[] vintagePiecesDrawableIds = new int[]{
			R.drawable.vintage_wp,
			R.drawable.vintage_wn,
			R.drawable.vintage_wb,
			R.drawable.vintage_wr,
			R.drawable.vintage_wq,
			R.drawable.vintage_wk,
			R.drawable.vintage_bp,
			R.drawable.vintage_bn,
			R.drawable.vintage_bb,
			R.drawable.vintage_br,
			R.drawable.vintage_bq,
			R.drawable.vintage_bk,
	};

	public void updateBoardAndPiecesImgs() {
		loadBoard(AppData.getChessBoardId(getContext()));
		loadPieces(AppData.getPiecesId(getContext()));

		invalidate();
	}

	private Paint initMoveArrowPaint(int arrowColor) {
		Paint paint = new Paint();
		paint.setStyle(Style.FILL);
		paint.setAntiAlias(true);
		paint.setColor(arrowColor);
		paint.setAlpha(192);
		return paint;
	}

	public final void setMoveHints(HashMap<org.petero.droidfish.gamelogic.Move, PieceColor> moveHints) {
		boolean equal;
		if ((this.moveHints == null) || (moveHints == null)) {
			equal = this.moveHints == moveHints;
		} else {
			equal = this.moveHints.equals(moveHints);
		}
		if (!equal) {
			this.moveHints = moveHints;
			invalidate();
		}
	}

	public final void drawMoveHints(Canvas canvas) {

		if ((moveHints == null || moveHints.isEmpty()))
			return;
		float h = (float)(square / 2.0);
		float d = (float)(square / 8.0);
		double v = 35 * Math.PI / 180;
		double cosv = Math.cos(v);
		double sinv = Math.sin(v);
		double tanv = Math.tan(v);

		for (org.petero.droidfish.gamelogic.Move move : moveHints.keySet()) {
			if ((move == null) || (move.from == move.to))
				continue;
			float x0 = getXCoordinate(Position.getX(move.from)) + h;
			float y0 = getYCoordinateForArrow(Position.getY(move.from)) + h;
			float x1 = getXCoordinate(Position.getX(move.to)) + h;
			float y1 = getYCoordinateForArrow(Position.getY(move.to)) + h;

			float x2 = (float)(Math.hypot(x1 - x0, y1 - y0) + d);
			float y2 = 0;
			float x3 = (float)(x2 - h * cosv);
			float y3 = (float)(y2 - h * sinv);
			float x4 = (float)(x3 - d * sinv);
			float y4 = (float)(y3 + d * cosv);
			float x5 = (float)(x4 + (-d/2 - y4) / tanv);
			float y5 = (float)(-d / 2);
			float x6 = 0;
			float y6 = y5 / 2;
			Path path = new Path();
			path.moveTo(x2, y2);
			path.lineTo(x3, y3);
//          path.lineTo(x4, y4);
			path.lineTo(x5, y5);
			path.lineTo(x6, y6);
			path.lineTo(x6, -y6);
			path.lineTo(x5, -y5);
//          path.lineTo(x4, -y4);
			path.lineTo(x3, -y3);
			path.close();
			Matrix mtx = new Matrix();
			mtx.postRotate((float)(Math.atan2(y1 - y0, x1 - x0) * 180 / Math.PI));
			mtx.postTranslate(x0, y0);
			path.transform(mtx);

			Paint p = moveHints.get(move) == PieceColor.WHITE ? whiteMoveArrowPaint : blackMoveArrowPaint;

			canvas.drawPath(path, p);
		}
	}

	private int getXCoordinate(int x) {
		return square * (boardFace.isReside() ? 7 - x : x);
	}

	private int getYCoordinate(int y) {
		return square * (boardFace.isReside() ? 7 - y : y);
	}

	// todo: should be only one getYCoordinate method after refactoring
	private int getYCoordinateForArrow(int y) {
		return square * (boardFace.isReside() ? y : 7 - y);
	}

	// TODO: refactor!

	private Handler handlerTimer = new Handler();

	protected class MoveAnimator {
		//boolean paused;
		long startTime = -1;
		long stopTime;
		long now;
		int piece1, from1, to1, hide1 = -1;
		int piece2, from2, to2, hide2;
		private Bitmap pieceBitmap;
		private Bitmap capturedPieceBitmap;
		private boolean firstRun = true;
		private Move move;

		MoveAnimator(Move move) {
			this.move = move;
		}

		public void setPieceBitmap(Bitmap pieceBitmap) {
			this.pieceBitmap = pieceBitmap;
		}

		public void setCapturedPieceBitmap(Bitmap capturedPieceBitmap) {
			this.capturedPieceBitmap = capturedPieceBitmap;
		}

		public Bitmap getCapturedPieceBitmap() {
			return capturedPieceBitmap;
		}

		public final boolean updateState() {
			now = System.currentTimeMillis();
			return isAnimtionActive();
		}

		private final boolean isAnimtionActive() {

			if (firstRun) {
				initTimer();
				firstRun = false;
			}

			if ((startTime < 0) || (now >= stopTime))
				return false;
			return true;
		}

		/*public final boolean squareHidden(int sq) {
			if (!isAnimtionActive())
				return false;
			return (sq == hide1) || (sq == hide2);
		}*/

		private void initTimer() {
			int dx = ChessBoard.getColumn(move.to) - ChessBoard.getColumn(move.from);
			int dy = ChessBoard.getRow(move.to) - ChessBoard.getRow(move.from);
			double dist = Math.sqrt(dx * dx + dy * dy);
			double t = Math.sqrt(dist) * 1000; // extract speed
			int animTime = (int)Math.round(t);

			startTime = System.currentTimeMillis();
			stopTime = startTime + animTime;
		}

		public final void draw(Canvas canvas) {

			if (!isAnimtionActive()) {
				return;
			}

			double animState = (now - startTime) / (double)(stopTime - startTime);
			//drawAnimPiece(canvas, piece2, from2, to2, animState); // castling
			drawAnimPiece(canvas, piece1, from1, to1, animState);
			long now2 = System.currentTimeMillis();
			long delay = 20 - (now2 - now);
			if (delay < 1) {
				delay = 1;
			}
			handlerTimer.postDelayed(new Runnable() {
				@Override
				public void run() {
					invalidate();
				}
			}, delay);
		}

		private void drawAnimPiece(Canvas canvas, int piece, int from, int to, double animState) {
			if (piece == ChessBoard.EMPTY)
				return;
			final int xCrd1 = getXCoordinate(ChessBoard.getColumn(from));
			final int yCrd1 = getYCoordinate(ChessBoard.getRow(from));
			final int xCrd2 = getXCoordinate(ChessBoard.getColumn(to));
			final int yCrd2 = getYCoordinate(ChessBoard.getRow(to));
			final int xCrd = xCrd1 + (int)Math.round((xCrd2 - xCrd1) * animState);
			final int yCrd = yCrd1 + (int)Math.round((yCrd2 - yCrd1) * animState);

			//Log.d("testtest", xCrd + " " + yCrd);

			rect.set(xCrd, yCrd, xCrd + square, yCrd + square);

			canvas.drawBitmap(pieceBitmap, null, rect, null);
		}
	}

	public void addMoveAnimator(Move move, boolean forward) {

		//Log.d("testtest", "move " + move);

		MoveAnimator moveAnimator = new MoveAnimator(move);

		// todo: move init to MoveAnimator constructor

		//moveAnimator.startTime = -1;

		int fromColor = boardFace.getColor()[move.from];
		int fromPiece = boardFace.getPieces()[move.from];
		moveAnimator.setPieceBitmap(piecesBitmaps[fromColor][fromPiece]);

		Bitmap capturedPieceBitmap = null;
		if (boardFace.getPiece(move.to) != ChessBoard.EMPTY) {
			int capturedColor = boardFace.getColor()[move.to];
			int capturedPiece = boardFace.getPieces()[move.to];
			capturedPieceBitmap = piecesBitmaps[capturedColor][capturedPiece];
		}
		moveAnimator.setCapturedPieceBitmap(capturedPieceBitmap);

		moveAnimator.hide1 = -1;

		//if (animTime > 0) {
			moveAnimator.piece2 = ChessBoard.EMPTY;
			moveAnimator.from2 = -1;
			moveAnimator.to2 = -1;
			moveAnimator.hide1 = -1;
			moveAnimator.hide2 = -1;
			if (forward) {
				int pieceFrom = getBoardFace().getPiece(move.from);
				moveAnimator.piece1 = pieceFrom;
				moveAnimator.from1 = move.from;
				moveAnimator.to1 = move.to;
				moveAnimator.hide1 = move.to;
				int pieceTo = getBoardFace().getPiece(move.to);
				if (pieceTo == ChessBoard.EMPTY) { // capture
					moveAnimator.piece2 = pieceTo;
					moveAnimator.from2 = move.to;
					moveAnimator.to2 = move.to;
				} /*else if ((pieceFrom == Piece.WKING) || (pieceFrom == Piece.BKING)) {
					boolean wtm = Piece.isWhite(pieceFrom);
					// TODO @compengine: add castling positions
					if (move.to == move.from + 2) { // O-O
						moveAnimator.piece2 = wtm ? Piece.WROOK : ChessBoard.Piece.BROOK;
						moveAnimator.from2 = move.to + 1;
						moveAnimator.to2 = move.to - 1;
						moveAnimator.hide2 = moveAnimator.to2;
					} else if (move.to == move.from - 2) { // O-O-O
						moveAnimator.piece2 = wtm ? Piece.WROOK : Piece.BROOK;
						moveAnimator.from2 = move.to - 2;
						moveAnimator.to2 = move.to + 1;
						moveAnimator.hide2 = moveAnimator.to2;
					}
				}*/
			} else {
				int pieceFrom = getBoardFace().getPiece(move.from);
				moveAnimator.piece1 = pieceFrom;
				// todo: check promotions
				/*if (move.promote > 0)
					moveAnimator.piece1 = getBoardFace().isWhite(move.from) ? Piece.WPAWN : Piece.BPAWN;*/
				moveAnimator.from1 = move.to;
				moveAnimator.to1 = move.from;
				moveAnimator.hide1 = moveAnimator.to1;
				/*if ((p == Piece.WKING) || (p == Piece.BKING)) {
					boolean wtm = Piece.isWhite(p);
					if (move.to == move.from + 2) { // O-O
						moveAnimator.piece2 = wtm ? Piece.WROOK : Piece.BROOK;
						moveAnimator.from2 = move.to - 1;
						moveAnimator.to2 = move.to + 1;
						moveAnimator.hide2 = moveAnimator.to2;
					} else if (move.to == move.from - 2) { // O-O-O
						moveAnimator.piece2 = wtm ? Piece.WROOK : Piece.BROOK;
						moveAnimator.from2 = move.to + 1;
						moveAnimator.to2 = move.to - 2;
						moveAnimator.hide2 = moveAnimator.to2;
					}
				}*/
			}

			movesToAnimate.add(moveAnimator);
		//}
	}
}
