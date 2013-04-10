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
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import com.chess.R;
import com.chess.RoboTextView;
import com.chess.backend.statics.AppConstants;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.interfaces.BoardFace;
import com.chess.ui.interfaces.BoardViewFace;
import com.chess.ui.interfaces.GameActivityFace;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * ChessBoardBaseView class
 *
 * @author alien_roger
 * @created at: 25.04.12 10:54
 */
public abstract class ChessBoardBaseView extends ImageView implements BoardViewFace, View.OnClickListener {


	public static final int P_ALPHA_ID = 0;
	public static final int P_BOOK_ID = 1;
	public static final int P_CASES_ID = 2;
	public static final int P_CLASSIC_ID = 3;
	public static final int P_CLUB_ID = 4;
	public static final int P_CONDAL_ID = 5;
	public static final int P_MAYA_ID = 6;
	public static final int P_MODERN_ID = 7;
	public static final int P_VINTAGE_ID = 8;

	public static final int EMPTY_ID = 6;
	private static final int QVGA_WIDTH = 240;
	private int boardId;

	protected Bitmap[][] piecesBitmaps;
	protected SharedPreferences preferences;

	//	protected boolean finished;
	protected boolean firstclick = true;
	protected boolean pieceSelected;
	protected boolean track;
	protected boolean drag;
	protected int[] pieces_tmp;
	protected int[] colors_tmp;
	private int side;
	protected int square;
	protected int from = -1;
	protected int to = -1;
	protected int dragX = 0;
	protected int dragY = 0;
	protected int trackX = 0;
	protected int trackY = 0;

	private float NUM_Y_OFFSET = 15;
	private float TEXT_Y_OFFSET = 3;

	protected Paint whitePaint;
	protected Paint coordinatesPaint;
	protected Paint redPaint;
	protected Paint greenPaint;
	protected Paint greenHighlightPaint;

	protected String[] signs = {"a", "b", "c", "d", "e", "f", "g", "h"};
	protected String[] nums = {"1", "2", "3", "4", "5", "6", "7", "8"};

	protected int viewWidth;
	protected int viewHeight;
	private int previousWidth;

	protected float width;
	protected float height;
	protected Rect rect;

	//	protected ControlsBaseView controlsBaseView;
	protected boolean isHighlightEnabled;
	protected boolean showCoordinates;
	protected String userName;
	protected boolean useTouchTimer;
	protected Handler handler;
	protected boolean userActive;
	protected Resources resources;
	private GameActivityFace gameActivityFace;
	protected boolean locked;
	protected PaintFlagsDrawFilter drawFilter;
	private NotationView notationsView;
	private ControlsBaseView controlsBaseView;
	private PanelInfoGameView topPanelView;
	private PanelInfoGameView bottomPanelView;
	private Paint boardBackPaint;

	public ChessBoardBaseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		resources = context.getResources();
		float density = resources.getDisplayMetrics().density;

		NUM_Y_OFFSET *= density;
		TEXT_Y_OFFSET *= density;

		drawFilter = new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG);
		boardBackPaint = new Paint();

		boardId = AppData.getChessBoardId(getContext());
//		loadBoard(AppData.getChessBoardId(getContext()));
		loadPieces(AppData.getPiecesId(getContext()));

		handler = new Handler();
		greenPaint = new Paint();
		whitePaint = new Paint();
		coordinatesPaint = new Paint();
		redPaint = new Paint();
		greenHighlightPaint = new Paint();
		rect = new Rect();

		whitePaint.setStrokeWidth(2.0f);
		whitePaint.setStyle(Style.STROKE);
		whitePaint.setColor(Color.WHITE);

		Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), RoboTextView.MAIN_PATH + "Regular.ttf");
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

		greenHighlightPaint.setStrokeWidth(4.0f);
		greenHighlightPaint.setStyle(Style.FILL_AND_STROKE);
		greenHighlightPaint.setColor(0x5600FF00);

		width = resources.getDisplayMetrics().widthPixels;
		height = resources.getDisplayMetrics().heightPixels;

		handler.postDelayed(checkUserIsActive, StaticData.WAKE_SCREEN_TIMEOUT);
		userActive = false;

		preferences = AppData.getPreferences(getContext());

	}

	public void setGameActivityFace(GameActivityFace gameActivityFace) {
		this.gameActivityFace = gameActivityFace;
		onBoardFaceSet(gameActivityFace.getBoardFace());

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
	}


	protected BoardFace getBoardFace() {
		return gameActivityFace.getBoardFace();
	}

	protected void onBoardFaceSet(BoardFace boardFace) {
	}

	private void drawCapturedPieces() {
		int[] whiteAlivePiecesCount = new int[EMPTY_ID];
		int[] blackAlivePiecesCount = new int[EMPTY_ID];

		for (int i = 0; i < 64; i++) {
			int pieceId = getBoardFace().getPiece(i);
			if (pieceId == EMPTY_ID) {
				continue;
			}

			if (getBoardFace().getColor()[i] == ChessBoard.WHITE_SIDE) {
				whiteAlivePiecesCount[pieceId]++;
			} else {
				blackAlivePiecesCount[pieceId]++;
			}
		}

		if (topPanelView.getSide() == AppConstants.WHITE_SIDE) { // if opponent is playing white
			topPanelView.updateCapturedPieces(blackAlivePiecesCount);
			bottomPanelView.updateCapturedPieces(whiteAlivePiecesCount);
		} else { // if user is playing black
			topPanelView.updateCapturedPieces(whiteAlivePiecesCount);
			bottomPanelView.updateCapturedPieces(blackAlivePiecesCount);
		}
	}

	public void setTopPanelView(PanelInfoGameView topPanelView) {
		this.topPanelView = topPanelView;
	}

	public void setBottomPanelView(PanelInfoGameView bottomPanelView) {
		this.bottomPanelView = bottomPanelView;
	}

	public void setControlsView(ControlsBaseView controlsBaseView) {
		this.controlsBaseView = controlsBaseView;
	}

	public void setNotationsView(NotationView notationsView) {
		this.notationsView = notationsView;
	}

	@Override
	public void showOptions() {
		gameActivityFace.showOptions();
	}

	@Override
	public void moveBack() {
//		finished = false;
		getBoardFace().setFinished(false);
		pieceSelected = false;
		getBoardFace().takeBack();
		invalidate();
		gameActivityFace.invalidateGameScreen();
		if (notationsView != null) {
			notationsView.show(true);
		}
	}

	@Override
	public void switchAnalysis() {
		boolean isAnalysis = getBoardFace().toggleAnalysis();

		gameActivityFace.switch2Analysis(isAnalysis);
	}

//	public void enableAnalysis() {  // TODO recheck logic
//		gameActivityFace.switch2Analysis(true);
//	}

	@Override
	public void moveForward() {
		pieceSelected = false;
		getBoardFace().takeNext();
		invalidate();
		gameActivityFace.invalidateGameScreen();
	}

//	public void setFinished(boolean finished) {
//		this.finished = finished;
//	}
//
//	public boolean isFinished() {
//		return finished;
//	}

	@Override
	public void newGame() {
		gameActivityFace.newGame();
	}

	//	public void updateNotations(CharSequence move) {
	public void updateNotations(String[] notations) {
		if (notationsView != null) {
			notationsView.updateNotations(notations, this, getBoardFace().getHply());
		}

//		controlsBaseView.updateNotations(notations);
//		invalidate();
//		requestFocus();
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
			} else {
				gameActivityFace.turnScreenOff();
			}
		}
	};

	protected void drawBoard(Canvas canvas) {
		if (viewHeight < viewWidth && viewWidth != QVGA_WIDTH) {
			square = viewHeight / 8;
		} else {
			square = viewWidth / 8;
		}

		side = square * 2;

		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), boardBackPaint);
	}

	protected void drawPieces(Canvas canvas) {
		for (int i = 0; i < 64; i++) {
			if (drag && i == from) {
				continue;
			}
			int color = getBoardFace().getColor()[i];
			int piece = getBoardFace().getPieces()[i];
			int x = ChessBoard.getColumn(i, getBoardFace().isReside());
			int y = ChessBoard.getRow(i, getBoardFace().isReside());

			// TODO rework logic to store changed pieces and redraw only them
			if (color != ChessBoard.EMPTY && piece != ChessBoard.EMPTY) {    // here is the simple replace/redraw of piece
				rect.set(x * square, y * square, x * square + square, y * square + square);
				canvas.drawBitmap(piecesBitmaps[color][piece], null, rect, null);
			}
		}
	}

	protected void drawCoordinates(Canvas canvas) {
		if (showCoordinates) {
			for (int i = 0; i < 8; i++) {
				if (getBoardFace().isReside()) {
					canvas.drawText(nums[i], 2, i * square + NUM_Y_OFFSET, coordinatesPaint);
					canvas.drawText(signs[7 - i], i * square + TEXT_Y_OFFSET, 8 * square - TEXT_Y_OFFSET, coordinatesPaint);
				} else {
					canvas.drawText(nums[7 - i], 2, i * square + NUM_Y_OFFSET, coordinatesPaint);
					canvas.drawText(signs[i], i * square + TEXT_Y_OFFSET, 8 * square - TEXT_Y_OFFSET, coordinatesPaint);
				}
			}
		}
	}

	protected void drawHighlight(Canvas canvas) {
		if (isHighlightEnabled && getBoardFace().getHply() > 0) { // draw moved piece highlight from -> to
			Move move = getBoardFace().getHistDat()[getBoardFace().getHply() - 1].move;
			int x1 = ChessBoard.getColumn(move.from, getBoardFace().isReside());
			int y1 = ChessBoard.getRow(move.from, getBoardFace().isReside());
			canvas.drawRect(x1 * square, y1 * square, x1 * square + square, y1 * square + square, redPaint);
			int x2 = ChessBoard.getColumn(move.to, getBoardFace().isReside());
			int y2 = ChessBoard.getRow(move.to, getBoardFace().isReside());
			canvas.drawRect(x2 * square, y2 * square, x2 * square + square, y2 * square + square, redPaint);
		}

		if (pieceSelected) { // draw rectangle around the start move piece position
			int x = ChessBoard.getColumn(from, getBoardFace().isReside());
			int y = ChessBoard.getRow(from, getBoardFace().isReside());
			canvas.drawRect(x * square, y * square, x * square + square, y * square + square, whitePaint);
		}

		if (pieceSelected) { // draw all possible moved coordinates  // TODO improve logic
			TreeSet<Move> moves = getBoardFace().gen();

			for (Move move : moves) {
				if (move.from == from && getBoardFace().makeMove(move, false)) {
					getBoardFace().takeBack();
					int x = ChessBoard.getColumn(move.to, getBoardFace().isReside());
					int y = ChessBoard.getRow(move.to, getBoardFace().isReside());
					canvas.drawRect(x * square, y * square, x * square + square, y * square + square, greenHighlightPaint);
				}
			}
		}
	}

	protected void drawDragPosition(Canvas canvas) {
		if (drag) {
			int color = getBoardFace().getColor()[from];
			int piece = getBoardFace().getPieces()[from];
			int halfSquare = square / 2;
			int x = dragX - halfSquare;
			int y = dragY - halfSquare;
			int col = (dragX - dragX % square) / square;
			int row = ((dragY + square) - (dragY + square) % square) / square;
			if (color != ChessBoard.EMPTY && piece != ChessBoard.EMPTY) {
				rect.set(x - halfSquare, y - halfSquare, x + square + halfSquare, y + square + halfSquare);
				canvas.drawBitmap(piecesBitmaps[color][piece], null, rect, null);
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
		int col = (int) (event.getX() - event.getX() % square) / square;
		int row = (int) (event.getY() - event.getY() % square) / square;
		if (col > 7 || col < 0 || row > 7 || row < 0) {
			invalidate();
			return false;
		}
		if (firstclick) {
			from = ChessBoard.getPositionIndex(col, row, getBoardFace().isReside());
			if (getBoardFace().getPieces()[from] != ChessBoard.EMPTY
					&& getBoardFace().getSide() == getBoardFace().getColor()[from]) {
				pieceSelected = true;
				firstclick = false;
				invalidate();
			}
		} else {
			int fromPosIndex = ChessBoard.getPositionIndex(col, row, getBoardFace().isReside());
			if (getBoardFace().getPieces()[fromPosIndex] != ChessBoard.EMPTY
					&& getBoardFace().getSide() == getBoardFace().getColor()[fromPosIndex]) {
				from = fromPosIndex;
				pieceSelected = true;
				firstclick = false;
				invalidate();
			}
		}
		return true;
	}

	protected boolean onActionMove(MotionEvent event) {
		dragX = (int) event.getX();
		dragY = (int) event.getY() - square;
		int col = (dragX - dragX % square) / square;
		int row = (dragY - dragY % square) / square;
		if (col > 7 || col < 0 || row > 7 || row < 0) {
			invalidate();
			return false;
		}
		if (!drag && !pieceSelected)
			from = ChessBoard.getPositionIndex(col, row, getBoardFace().isReside());
		if (!firstclick && getBoardFace().getSide() == getBoardFace().getColor()[from]) {
			drag = true;
			to = ChessBoard.getPositionIndex(col, row, getBoardFace().isReside());
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
			from = ChessBoard.getPositionIndex(col, row, getBoardFace().isReside());
			if (getBoardFace().getPieces()[from] != ChessBoard.EMPTY
					&& getBoardFace().getSide() == getBoardFace().getColor()[from]) {
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
				move = moveIterator.next();     // search for move that was made
				if (move.from == from && move.to == to) {
					found = true;
					break;
				}
			}

			// if promote
			if ((((to < 8) && (getBoardFace().getSide() == ChessBoard.WHITE_SIDE)) ||
					((to > 55) && (getBoardFace().getSide() == ChessBoard.BLACK_SIDE))) &&
					(getBoardFace().getPieces()[from] == ChessBoard.PAWN) && found) {

				gameActivityFace.showChoosePieceDialog(col, row);
				return true;
			}

			if (found && getBoardFace().makeMove(move)) { // if move is valid
				afterMove();
			} else if (getBoardFace().getPieces()[to] != ChessBoard.EMPTY
					&& getBoardFace().getSide() == getBoardFace().getColor()[to]) {
				pieceSelected = true;
				firstclick = false;
				from = ChessBoard.getPositionIndex(col, row, getBoardFace().isReside());
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
		loadBoard();
	}


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
		} else if (getBoardFace().getPieces()[to] != ChessBoard.EMPTY
				&& getBoardFace().getSide() == getBoardFace().getColor()[to]) {
			pieceSelected = true;
			firstclick = false;
			from = ChessBoard.getPositionIndex(col, row, getBoardFace().isReside());
			invalidate();
		} else {
			invalidate();
		}
	}

	/**
	 * Check made move for mate state
	 * also update captured pieces drawable
	 *
	 * @return
	 */
	protected boolean isGameOver() {
		drawCapturedPieces();

		String message = null;
		if (!getBoardFace().isAnalysis()) {
			if (!getBoardFace().isPossibleToMakeMoves()) {
				if (getBoardFace().inCheck(getBoardFace().getSide())) {
					if (getBoardFace().getSide() == ChessBoard.WHITE_SIDE)
						message = getResources().getString(R.string.black_wins);
					else
						message = getResources().getString(R.string.white_wins);
				} else
					message = getResources().getString(R.string.draw_by_stalemate);
			} else if (getBoardFace().reps() == 3)
				message = getResources().getString(R.string.draw_by_3fold_repetition);
		} else if (getBoardFace().inCheck(getBoardFace().getSide())) {

			if (!getBoardFace().isPossibleToMakeMoves()) {
				getBoardFace().getHistDat()[getBoardFace().getHply() - 1].notation += "#";
				gameActivityFace.invalidateGameScreen();
				getBoardFace().setFinished(true);
//				finished = true;
				return true;
			} else {
				getBoardFace().getHistDat()[getBoardFace().getHply() - 1].notation += "+";
				gameActivityFace.invalidateGameScreen();
				gameActivityFace.onCheck();
			}
		}

		if (message != null) {
//			finished = true; // will be set from activity face
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
		controlsBaseView.lock(lock);
		setEnabled(!lock);
	}

	protected void loadBoard() {
		if (previousWidth != viewWidth) { // update only if size has changed
			previousWidth = viewWidth;

			BitmapDrawable drawable = (BitmapDrawable) resources.getDrawable(boardsDrawables[boardId]);
			Bitmap boardBitmap = drawable.getBitmap();

			int bitmapSize;
			if (viewHeight < viewWidth && viewWidth != QVGA_WIDTH) { // if landscape mode
				bitmapSize = viewHeight / 4;
			} else {
				bitmapSize = viewWidth / 4;
			}
			boardBitmap = Bitmap.createScaledBitmap(boardBitmap, bitmapSize, bitmapSize, true);

			BitmapShader shader = new BitmapShader(boardBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

			boardBackPaint.setShader(shader);
		}
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
		boardId = AppData.getChessBoardId(getContext());
//		loadBoard(AppData.getChessBoardId(getContext()));
		loadPieces(AppData.getPiecesId(getContext()));

		invalidate();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == NotationView.NOTATION_ID) {// scroll to the specified position
			Integer pos = (Integer) v.getTag(R.id.list_item_id);

			int totalHply = getBoardFace().getHply() - 1;
			if (totalHply < pos) {
				for (int i = totalHply; i < pos; i++) {
					getBoardFace().takeNext();
				}
			} else {
				for (int i = totalHply; i > pos; i--) {
					getBoardFace().takeBack();
				}
			}
			checkControlsButtons();
			invalidate();
		}
	}

	private void checkControlsButtons() {
		BoardFace boardFace = getBoardFace();
		if (boardFace.getHply() < boardFace.getMovesCount()) {
			controlsBaseView.enableForwardBtn(true);
		} else {
			controlsBaseView.enableForwardBtn(false);
		}

		if (boardFace.getHply() <= 1) {
			controlsBaseView.enableBackBtn(false);
		} else {
			controlsBaseView.enableBackBtn(true);
		}
	}
}
