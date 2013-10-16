package com.chess.ui.views.chess_boards;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import com.chess.R;
import com.chess.statics.AppData;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.boards.BoardViewFace;
import com.chess.ui.interfaces.game_ui.GameFace;
import com.chess.ui.views.NotationView;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.game_controls.ControlsBaseView;
import com.chess.utilities.FontsHelper;

import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ChessBoardBaseView class
 *
 * @author alien_roger
 * @created at: 25.04.12 10:54
 */
public abstract class ChessBoardBaseView extends ImageView implements BoardViewFace, NotationView.BoardForNotationFace {

	public static final int P_ALPHA_ID = 0;
	public static final int P_BOOK_ID = 1;
	public static final int P_CASES_ID = 2;
	public static final int P_CLASSIC_ID = 3;
	public static final int P_CLUB_ID = 4;
	public static final int P_CONDAL_ID = 5;
	public static final int P_MAYA_ID = 6;
	public static final int P_MODERN_ID = 7;
	public static final int P_VINTAGE_ID = 8;
	public static final int P_THE3D_ID = 9;

	public static final int PIECE_ANIM_SPEED = 150; // 250 looks too long. is not?
	public static final String VALID_MOVES = "valid_moves";

	int pieceXDelta, pieceYDelta; // top/left pixel draw position relative to square

	private static final int SQUARES_NUMBER = 8;
	public static final int EMPTY_ID = 6;
	protected float density;
	protected AppData appData;
	private int boardId;

	protected WeakHashMap<Integer, Bitmap> whitePiecesMap;
	protected WeakHashMap<Integer, Bitmap> blackPiecesMap;
	protected SharedPreferences preferences;

	protected boolean firstClick = true;
	protected boolean pieceSelected;
	protected boolean track;
	protected boolean drag;

	protected int W;
	protected int H;
	protected int side;
	protected int squareSize;
	protected int from = -1;
	protected int to = -1;
	private int previousFrom;

	protected int dragX = 0;
	protected int dragY = 0;
	protected int trackX = 0;
	protected int trackY = 0;

	private float numYOffset = 10;
	private float textYOffset = 3;

	protected Paint yellowPaint;
	protected Paint whitePaint;
	protected Paint coordinatesPaint;
	protected Paint madeMovePaint;
	protected Paint greenPaint;
	protected Paint possibleMovePaint;

	protected String[] signs = {"a", "b", "c", "d", "e", "f", "g", "h"};
	protected String[] nums = {"1", "2", "3", "4", "5", "6", "7", "8"};

	protected int viewWidth;
	protected int viewHeight;
	private int previousWidth;

	protected Rect rect;

	protected boolean isHighlightEnabled;
	protected boolean showLegalMoves;
	protected boolean showCoordinates;
	protected String username;
	protected boolean useTouchTimer;
	protected Handler handler;
	protected boolean userActive;
	protected Resources resources;
	private GameFace gameFace;
	protected boolean locked;
	protected PaintFlagsDrawFilter drawFilter;
	private NotationView notationsView;
	private ControlsBaseView controlsBaseView;
	private PanelInfoGameView topPanelView;
	private PanelInfoGameView bottomPanelView;
	private Paint boardBackPaint;
	private String[] originalNotations;

	// new engine
	private MoveAnimator moveAnimator;
	private MoveAnimator secondMoveAnimator;
	//	private HashMap<org.petero.droidfish.gamelogic.Move, PieceColor> moveHints =
//			new HashMap<org.petero.droidfish.gamelogic.Move, PieceColor>();
//	private Paint whiteMoveArrowPaint;
//	private Paint blackMoveArrowPaint;
	protected boolean navigating;
	private int draggingFrom = -1;
	private CopyOnWriteArrayList<Move> validMoves = new CopyOnWriteArrayList<Move>(); // lets try this type
	private BitmapFactory.Options bitmapOptions;
	private int pieceInset;
	private int customBoardId = NO_ID;
	private boolean using3dPieces;
	private Rect clipBoundsRect;
	private int _3dPiecesOffset;
	private int _3dPiecesOffsetDrag;

	public ChessBoardBaseView(Context context) {
		super(context);
		init(context);
	}

	public ChessBoardBaseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		resources = context.getResources();
		density = resources.getDisplayMetrics().density;

		drawFilter = new PaintFlagsDrawFilter(0, Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
		boardBackPaint = new Paint();

		appData = new AppData(context);
		boardId = appData.getChessBoardId();
		clipBoundsRect = new Rect();

		handler = new Handler();
		greenPaint = new Paint();
		yellowPaint = new Paint();
		whitePaint = new Paint();
		coordinatesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		madeMovePaint = new Paint();
		possibleMovePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		rect = new Rect();

		_3dPiecesOffset = (int) (14 * density);
		_3dPiecesOffsetDrag = (int) (28 * density);
		pieceInset = (int) (1 * density);

		int highlightColor = resources.getColor(R.color.highlight_color);

		yellowPaint.setStrokeWidth(resources.getDimension(R.dimen.highlight_stroke_width));
		yellowPaint.setStyle(Style.STROKE);
		yellowPaint.setColor(highlightColor);

		whitePaint.setStrokeWidth(1.5f * density);
		whitePaint.setStyle(Style.STROKE);
		whitePaint.setColor(Color.WHITE);

		int coordinateFont = resources.getInteger(R.integer.board_highlight_font);
		int coordinateColor = resources.getColor(R.color.coordinate_color);

		numYOffset = coordinateFont * density;
		textYOffset *= density;

		coordinatesPaint.setStyle(Style.FILL);
		coordinatesPaint.setColor(coordinateColor);
		coordinatesPaint.setShadowLayer(1.0f, 1.0f, 1.0f, 0x7FFFFFFF);
		coordinatesPaint.setTextSize(coordinateFont * density);
		coordinatesPaint.setTypeface(FontsHelper.getInstance().getTypeFace(getContext(), FontsHelper.BOLD_FONT));

		madeMovePaint.setStrokeWidth(resources.getDimension(R.dimen.highlight_stroke_width));
		madeMovePaint.setStyle(Style.STROKE);
		madeMovePaint.setColor(highlightColor);

		greenPaint.setStrokeWidth(1.5f * density);
		greenPaint.setStyle(Style.STROKE);
		greenPaint.setColor(Color.GREEN);

		int possibleMoveHighlightColor = resources.getColor(R.color.possible_move_highlight);
		possibleMovePaint.setStrokeWidth(4.0f);
		possibleMovePaint.setStyle(Style.FILL);
		possibleMovePaint.setColor(possibleMoveHighlightColor);

//		width = resources.getDisplayMetrics().widthPixels;
//		height = resources.getDisplayMetrics().heightPixels;

		handler.postDelayed(checkUserIsActive, StaticData.WAKE_SCREEN_TIMEOUT);
		userActive = false;

		preferences = appData.getPreferences();

//		whiteMoveArrowPaint = initMoveArrowPaint(Color.WHITE);
//		blackMoveArrowPaint = initMoveArrowPaint(Color.BLACK);

		pieceXDelta = -1;
		pieceYDelta = -1;
	}

	/**
	 * Set gameFace to boardview. It will automatically call getBoardFace() which will init it.
	 */
	public void setGameFace(GameFace gameFace) {
		this.gameFace = gameFace;
		onBoardFaceSet(gameFace.getBoardFace());

		username = getAppData().getUsername();

		isHighlightEnabled = getAppData().isHighlightLastMove();
		showLegalMoves = getAppData().isShowLegalMoves();

		showCoordinates = getAppData().isShowCoordinates();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			setMeasuredDimension(resolveSize(parentWidth, widthMeasureSpec),
					resolveSize(parentWidth, heightMeasureSpec));
		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			setMeasuredDimension(resolveSize(parentHeight, widthMeasureSpec),
					resolveSize(parentHeight, heightMeasureSpec));
		}

		pieceXDelta = -1;
		pieceYDelta = -1;
	}

	protected BoardFace getBoardFace() {
		return gameFace.getBoardFace();
	}

	protected void onBoardFaceSet(BoardFace boardFace) {
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.setDrawFilter(drawFilter);
		super.onDraw(canvas);

		drawBoard(canvas);

		if (gameFace != null && getBoardFace() != null) {
			drawCoordinates(canvas);
			drawHighlights(canvas);

			if (using3dPieces) {
				getDrawingRect(clipBoundsRect);
				int saveCount = canvas.save(Canvas.CLIP_SAVE_FLAG);
				clipBoundsRect.set(clipBoundsRect.left, clipBoundsRect.top - (int) (28 * density), clipBoundsRect.right, clipBoundsRect.bottom);
				canvas.clipRect(clipBoundsRect, Region.Op.REPLACE);

				drawPiecesAndAnimation(canvas);
				drawDragPosition(canvas);

				canvas.restoreToCount(saveCount);

				gameFace.updateParentView();
			} else {
				drawPiecesAndAnimation(canvas);
				drawDragPosition(canvas);
			}
		}
	}

	private void drawCapturedPieces() {
		int[] whiteAlivePiecesCount = new int[EMPTY_ID];
		int[] blackAlivePiecesCount = new int[EMPTY_ID];

		BoardFace boardFace = getBoardFace();
		for (int pos = 0; pos < ChessBoard.SQUARES_CNT; pos++) {
			int pieceId = boardFace.getPiece(pos);
			if (pieceId == EMPTY_ID) {
				continue;
			}

			if (boardFace.getColor(pos) == ChessBoard.WHITE_SIDE) {
				whiteAlivePiecesCount[pieceId]++;
			} else {
				blackAlivePiecesCount[pieceId]++;
			}
		}

		if (topPanelView != null) {// for diagram
			if (isUserWhite()) {
				topPanelView.updateCapturedPieces(blackAlivePiecesCount);
				bottomPanelView.updateCapturedPieces(whiteAlivePiecesCount);
			} else {
				topPanelView.updateCapturedPieces(whiteAlivePiecesCount);
				bottomPanelView.updateCapturedPieces(blackAlivePiecesCount);
			}
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
		gameFace.showOptions();
	}

	private boolean isUserWhite() {
		return gameFace.isUserColorWhite() == null ? true : gameFace.isUserColorWhite();
	}

	private boolean isUserColor(int color) {
		if (appData.isHumanVsHumanGameMode(getBoardFace())) {
			return getBoardFace().isWhiteToMove() ? color == ChessBoard.WHITE_SIDE : color == ChessBoard.BLACK_SIDE;
		} else if (isUserWhite()) {
			return color == ChessBoard.WHITE_SIDE;
		} else {
			return color == ChessBoard.BLACK_SIDE;
		}
	}

	@Override
	public void moveBack() {
		BoardFace boardFace = getBoardFace();
		if (noMovesToAnimate() && boardFace.getPly() > 0) {

			boardFace.setFinished(false);
			pieceSelected = false;
			setMoveAnimator(boardFace.getLastMove(), false);
			resetValidMoves();
			boardFace.takeBack();

			invalidate();
			// update full screen view to update 3d pieces cut
			requestLayout();
//			getParent().requestLayout();
			gameFace.invalidateGameScreen();

			if (notationsView != null) { // we might don't have notations  so probably should be moved to fragment level
				notationsView.moveBack(boardFace.getPly());
			}
		}
	}

	@Override
	public void switchAnalysis() {
		gameFace.switch2Analysis();
	}

	@Override
	public void moveForward() {

		if (noMovesToAnimate()) {
			pieceSelected = false;
			BoardFace boardFace = getBoardFace();
			Move move = boardFace.getNextMove();
			if (move == null) {
				return;
			}
			setMoveAnimator(move, true);
			resetValidMoves();
			boardFace.takeNext();

			invalidate();
			gameFace.invalidateGameScreen();

			if (notationsView != null) {
				notationsView.moveForward(boardFace.getPly());
			}
		}
	}

	@Override
	public void newGame() {
		gameFace.newGame();
	}

	public void updateNotations(String[] notations) {
		if (originalNotations == null || originalNotations.length < notations.length) { // TODO check
			originalNotations = notations;
		}
		if (notationsView != null) {
			notationsView.updateNotations(notations, this, getBoardFace().getPly());
		}

//		checkControlsButtons();
		drawCapturedPieces();
		invalidate();
		requestFocus();
	}

//	private void checkControlsButtons() {
//		BoardFace boardFace = getBoardFace();
//		if (boardFace.getPly() < boardFace.getMovesCount()) {
//			controlsBaseView.enableForwardBtn(true);
//		} else {
//			controlsBaseView.enableForwardBtn(false);
//		}
//
//		if (boardFace.getPly() < 1) {
//			controlsBaseView.enableBackBtn(false);
//		} else {
//			controlsBaseView.enableBackBtn(true);
//		}
//	}

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
				if (gameFace != null) {
					gameFace.turnScreenOff();
				}
			}
		}
	};

	public void releaseRunnable() {
		handler.removeCallbacks(checkUserIsActive);
	}

	protected void drawBoard(Canvas canvas) {
		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), boardBackPaint);
	}

	protected void drawPiecesAndAnimation(Canvas canvas) {
		boolean animationActive;

		// draw just piece without animation
		if (moveAnimator == null && secondMoveAnimator == null) {
			drawPieces(canvas, false, null);
		}

		// draw animations // TODO rework with Animator facility
		if (moveAnimator != null) {
			animationActive = moveAnimator.updateState();
			drawPieces(canvas, animationActive, moveAnimator);
			if (animationActive) {
				moveAnimator.draw(canvas);
			} else {
				if (moveAnimator.isForceCompEngine()) {
					afterUserMove();
				}
				moveAnimator = null;

				if (secondMoveAnimator != null) {
					BoardFace boardFace = getBoardFace();
					if (secondMoveAnimator.isForward()) {
						resetValidMoves();
						boardFace.takeNext();
					} else {
						resetValidMoves();
						boardFace.takeBack();
					}
				} else {
					navigating = false;
				}
			}
		}

		if (moveAnimator == null && secondMoveAnimator != null) {
			animationActive = secondMoveAnimator.updateState();
			drawPieces(canvas, animationActive, secondMoveAnimator);
			if (animationActive) {
				secondMoveAnimator.draw(canvas);
			} else {
				secondMoveAnimator = null;
				navigating = false;
			}
		}
	}

	protected void drawPieces(Canvas canvas, boolean animationActive, MoveAnimator moveAnimator) {
		BoardFace boardFace = getBoardFace();

		for (int pos = 0; pos < ChessBoard.SQUARES_CNT; pos++) {
			// do not draw if in drag or animated
			if ((drag && pos == from) || (animationActive && moveAnimator.isSquareHidden(pos))) {
				continue;
			}

			int color = boardFace.getColor(pos);
			int piece = boardFace.getPiece(pos);
			int x = ChessBoard.getColumn(pos, boardFace.isReside());
			int y = ChessBoard.getRow(pos, boardFace.isReside());
			// TODO rework logic to store changed pieces and redraw only them
			if (color != ChessBoard.EMPTY && piece != ChessBoard.EMPTY) {    // here is the simple replace/redraw of piece // draw it bit inside of square
				// calculate piece size bounds
				int left = x * squareSize + pieceInset;
				int right = x * squareSize + squareSize - pieceInset;
				int bottom = y * squareSize + squareSize - pieceInset;
				if (using3dPieces) {
					int top = y * squareSize + pieceInset - _3dPiecesOffset;
					rect.set(left, top, right, bottom);
				} else {
					int top = y * squareSize + pieceInset;
					rect.set(left, top, right, bottom);
				}
				Bitmap pieceBitmap = getPieceBitmap(color, piece);

				if (pieceBitmap == null || pieceBitmap.isRecycled()) { // we closed the view, no need to show animation. // TODO find better way of using bitmaps
					return;
				}
				canvas.drawBitmap(pieceBitmap, null, rect, null);
			}
		}

		// draw piece that will be captured
		if (animationActive && moveAnimator.getCapturedPieceBitmap() != null) {
			int capturedPiecePosition = moveAnimator.getCapturedPiecePosition();
			int x = ChessBoard.getColumn(capturedPiecePosition, boardFace.isReside());
			int y = ChessBoard.getRow(capturedPiecePosition, boardFace.isReside());

			int left = x * squareSize;
			int right = x * squareSize + squareSize;
			int bottom = y * squareSize + squareSize;
			if (using3dPieces) {
				int top = y * squareSize - _3dPiecesOffset;
				rect.set(left, top, right, bottom);
			} else {
				int top = y * squareSize;
				rect.set(left, top, right, bottom);
			}
			canvas.drawBitmap(moveAnimator.getCapturedPieceBitmap(), null, rect, null);
		}
	}

	protected void drawCoordinates(Canvas canvas) {
		if (showCoordinates) {
			BoardFace boardFace = getBoardFace();
			int xOffset = (squareSize / 8) * 7;
			float yPosition = SQUARES_NUMBER * squareSize - textYOffset;
			for (int i = 0; i < SQUARES_NUMBER; i++) {
				if (boardFace.isReside()) {
					// draw ranks coordinates (1, 2, 3, 4, 5, 6, 7, 8)
					canvas.drawText(nums[i], 2, i * squareSize + numYOffset, coordinatesPaint);
					// draw file coordinates (a, b, c, d, e, f, g, h)
					canvas.drawText(signs[7 - i], i * squareSize + xOffset, yPosition, coordinatesPaint);
				} else {
					// draw ranks coordinates (1, 2, 3, 4, 5, 6, 7, 8)
					canvas.drawText(nums[7 - i], 2, i * squareSize + numYOffset, coordinatesPaint);
					// draw file coordinates (a, b, c, d, e, f, g, h)
					canvas.drawText(signs[i], i * squareSize + xOffset, yPosition, coordinatesPaint);
				}
			}
		}
	}

	protected void drawHighlights(Canvas canvas) {
		int offset = (int) (1 * density);

		BoardFace boardFace = getBoardFace();
		if (pieceSelected) { // draw rectangle around the start move piece position
			int x = ChessBoard.getColumn(from, boardFace.isReside());
			int y = ChessBoard.getRow(from, boardFace.isReside());
			canvas.drawRect(x * squareSize + offset, y * squareSize + offset,
					x * squareSize + squareSize - offset, y * squareSize + squareSize - offset, yellowPaint);
		} else if (isHighlightEnabled && boardFace.getPly() > 0) { // draw moved piece highlight from -> to
			// from
			Move move = boardFace.getHistDat()[boardFace.getPly() - 1].move;
			int x1 = ChessBoard.getColumn(move.from, boardFace.isReside());
			int y1 = ChessBoard.getRow(move.from, boardFace.isReside());
			canvas.drawRect(x1 * squareSize + offset, y1 * squareSize + offset,
					x1 * squareSize + squareSize - offset, y1 * squareSize + squareSize - offset, madeMovePaint);
			// to
			int x2 = ChessBoard.getColumn(move.to, boardFace.isReside());
			int y2 = ChessBoard.getRow(move.to, boardFace.isReside());
			canvas.drawRect(x2 * squareSize + offset, y2 * squareSize + offset,
					x2 * squareSize + squareSize - offset, y2 * squareSize + squareSize - offset, madeMovePaint);
		}

		// highlight with semi-transparent dots all possible moves for selected piece
		if (pieceSelected && showLegalMoves) {

			boolean isWhiteToMove = boardFace.isWhiteToMove();
			boolean isUserWhite = isUserWhite();
			boolean isUsersTurn = ((isUserWhite && isWhiteToMove) || (!isUserWhite && !isWhiteToMove))
					|| appData.isHumanVsHumanGameMode(getBoardFace());

			Log.d(VALID_MOVES, "draw validMoves.isEmpty() " + validMoves.isEmpty());
			Log.d(VALID_MOVES, "draw validMoves.size() " + validMoves.size());
			Log.d(VALID_MOVES, "draw isUsersTurn " + isUsersTurn);
			Log.d(VALID_MOVES, "draw isNewMove() " + isNewMove());
			if (isNewMove()) {
				if (isUserColor(boardFace.getColor(from)) && isUsersTurn) {
					validMoves = boardFace.generateValidMoves(!isUsersTurn);
				} else if (boardFace.isAnalysis()) {
					validMoves = boardFace.generateValidMoves(false);
				}
				previousFrom = from;
			}

			for (Move move : validMoves) {
				if (move.from == from || move.from == draggingFrom) {
					int x = ChessBoard.getColumn(move.to, boardFace.isReside());
					int y = ChessBoard.getRow(move.to, boardFace.isReside()) + 1;
					canvas.drawCircle(x * squareSize + squareSize / 2, y * squareSize - squareSize / 2, squareSize / 5, possibleMovePaint);
				}
			}
		}
	}

	protected void drawDragPosition(Canvas canvas) {
		if (drag) {
			int color = getBoardFace().getColor(draggingFrom);
			int piece = getBoardFace().getPiece(draggingFrom);

			int halfSquare = squareSize / 2;
			int x = dragX - halfSquare;
			int y = dragY - halfSquare;
			int file = (dragX - dragX % squareSize) / squareSize;
			int rank = ((dragY + squareSize) - (dragY + squareSize) % squareSize) / squareSize;
			if (color != ChessBoard.EMPTY && piece != ChessBoard.EMPTY) {
				// set piece bounds
				int pieceLeft = x - halfSquare;
				int pieceRight = x + squareSize + halfSquare;
				int pieceBottom = y + squareSize + halfSquare;
				if (using3dPieces) {
					int pieceTop = y - halfSquare - _3dPiecesOffsetDrag;
					rect.set(pieceLeft, pieceTop, pieceRight, pieceBottom);
				} else {
					int pieceTop = y - halfSquare;
					rect.set(pieceLeft, pieceTop, pieceRight, pieceBottom);
				}

				// draw yellow rect above the square
				int rectLeft = file * squareSize - halfSquare;
				int rectTop = rank * squareSize - halfSquare;
				int rectRight = file * squareSize + squareSize + halfSquare;
				int rectBottom = rank * squareSize + squareSize + halfSquare;
				canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, whitePaint);
				// draw piece
				Bitmap pieceBitmap = getPieceBitmap(color, piece);

				canvas.drawBitmap(pieceBitmap, null, rect, null);
			}
		}
	}

	protected boolean processTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	protected boolean isLocked() {
		return locked || gameFace == null || !gameFace.currentGameExist(); // fix NPE when user clicks on empty board, probably should be refactored
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
		if (squareSize == 0) {
			return false;
		}
		int file = (int) (event.getX() - event.getX() % squareSize) / squareSize;
		int rank = (int) (event.getY() - event.getY() % squareSize) / squareSize;
		if (file > 7 || file < 0 || rank > 7 || rank < 0) {
			invalidate();
			return false;
		}
		BoardFace boardFace = getBoardFace();
		if (firstClick) {
			from = ChessBoard.getPositionIndex(file, rank, boardFace.isReside());
			//draggingFrom = from;
			if (isUserColor(boardFace.getColor(from))
					|| (boardFace.isAnalysis() /*&& boardFace.getPiece(to) != ChessBoard.EMPTY*/)) {
				pieceSelected = true;
				firstClick = false;
				invalidate();
			}
		} else {
			int fromSquare = ChessBoard.getPositionIndex(file, rank, boardFace.isReside());
			// don't touch not our piece or don't make empty square as fromSquare
			if (isUserColor(boardFace.getColor(fromSquare))
					|| (boardFace.isAnalysis() && boardFace.getPiece(fromSquare) != ChessBoard.EMPTY)) {
				from = fromSquare;
				pieceSelected = true;
				firstClick = false;
				invalidate();
			}
		}
		return true;
	}

	protected boolean onActionMove(MotionEvent event) {
		dragX = (int) event.getX();
		dragY = (int) event.getY() - squareSize;

		int file = (dragX - dragX % squareSize) / squareSize;
		int rank = (dragY - dragY % squareSize) / squareSize;

		if (file > 7 || file < 0 || rank > 7 || rank < 0) {
			invalidate();
			return false;
		}

		BoardFace boardFace = getBoardFace();
		if (!drag && !pieceSelected) {
			from = ChessBoard.getPositionIndex(file, rank, boardFace.isReside());
		}

		if (!firstClick) {
			draggingFrom = from;
			// do not drag captured piece // ??
			drag = isUserColor(boardFace.getColor(draggingFrom)) || (boardFace.isAnalysis());
			to = ChessBoard.getPositionIndex(file, rank, boardFace.isReside());
			invalidate();
		}
		return true;
	}

	protected boolean onActionUp(MotionEvent event) {

		int file = (int) (event.getX() - event.getX() % squareSize) / squareSize;
		int rank = (int) (event.getY() - event.getY() % squareSize) / squareSize;

		boolean showAnimation = !drag;

		drag = false;
		draggingFrom = -1;
		// if outside of the boardBitmap - return
		if (file > 7 || file < 0 || rank > 7 || rank < 0) { // if touched out of board
			invalidate();
			return false;
		}

		BoardFace boardFace = getBoardFace();
		if (firstClick) {
			from = ChessBoard.getPositionIndex(file, rank, boardFace.isReside());
			if (isUserColor(boardFace.getColor(from))
					|| (boardFace.isAnalysis() && boardFace.getPiece(to) != ChessBoard.EMPTY)) {
				pieceSelected = true;
				firstClick = false;
				invalidate();
			}
		} else {
			to = ChessBoard.getPositionIndex(file, rank, boardFace.isReside());
			pieceSelected = false;
			firstClick = true;

			boolean found = false;
			Move move = null;
			List<Move> moves = boardFace.generateLegalMoves();
			for (Move move1 : moves) { // search for move that was made
				move = move1;
				if (move.from == from && move.to == to) {
					found = true;
					break;
				}
			}

			// if promote - show popup and return
			if (found && boardFace.isPromote(from, to)) {
				gameFace.showChoosePieceDialog(file, rank);
				return true;
			}

			boolean moveMade = false;
			MoveAnimator moveAnimator = null;
			if (found) {
				if (showAnimation) {
					moveAnimator = new MoveAnimator(move, true);
				}
				moveMade = boardFace.makeMove(move);
			}

			if (moveMade) {
				if (showAnimation) {
					moveAnimator.setForceCompEngine(true); // TODO @engine: probably postpone afterUserMove() only for vs comp mode
					setMoveAnimator(moveAnimator);
				} else {
					afterUserMove();
				}
			} else if (boardFace.getPiece(to) != ChessBoard.EMPTY
					&& (isUserColor(boardFace.getColor(to)) || boardFace.isAnalysis())) {
				pieceSelected = true;
				firstClick = false;
				from = ChessBoard.getPositionIndex(file, rank, boardFace.isReside());
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
		squareSize = viewHeight / 8;

		loadBoard();
		loadPieces(appData.getPiecesId());
	}

	public void promote(int promote, int file, int rank) {
		boolean found = false;

		BoardFace boardFace = getBoardFace();
		Move move = null;
		List<Move> moves = boardFace.generateLegalMoves();
		for (Move move1 : moves) {
			move = move1;
			if (move.from == from && move.to == to && move.promote == promote) {
				found = true;
				break;
			}
		}

		boolean moveMade = false;
		MoveAnimator moveAnimator = null;
		if (found) {
			moveAnimator = new MoveAnimator(move, true);
			moveMade = boardFace.makeMove(move);
		}
		if (moveMade) {
			moveAnimator.setForceCompEngine(true); // TODO @engine: probably postpone afterUserMove() only for vs comp mode
			setMoveAnimator(moveAnimator);
			//afterUserMove(); //
		} else if (boardFace.getPiece(to) != ChessBoard.EMPTY
				&& boardFace.getSide() == boardFace.getColor(to)) {
			pieceSelected = true;
			firstClick = false;
			from = ChessBoard.getPositionIndex(file, rank, boardFace.isReside());
		}
		invalidate();
	}

	/**
	 * Check made move for mate state
	 * also update captured pieces drawable
	 *
	 * @return true if game is over
	 */
	protected boolean isGameOver() {
		drawCapturedPieces();

		BoardFace boardFace = getBoardFace();
		int side = boardFace.getSide();
		if (!boardFace.isAnalysis()) {
			int messageId = 0;

			if (!boardFace.isPossibleToMakeMoves()) {
				if (boardFace.isPerformCheck(side)) {
					messageId = side == ChessBoard.WHITE_SIDE ? R.string.black_wins : R.string.white_wins;
				} else {
					messageId = R.string.draw_by_stalemate;
				}
			} else if (boardFace.getRepetitions() == 3) {
				messageId = R.string.draw_by_3fold_repetition;
			}

			if (messageId != 0) {
				boardFace.setFinished(true);
				gameFace.onGameOver(getResources().getString(messageId), false);
				return true;
			}
		} else if (boardFace.isPerformCheck(side)) {
			if (!boardFace.isPossibleToMakeMoves()) {
				boardFace.getHistDat()[boardFace.getPly() - 1].notation += "#";
				gameFace.invalidateGameScreen();
				boardFace.setFinished(true);
				return true;
			} else {
				boardFace.getHistDat()[boardFace.getPly() - 1].notation += "+";
				gameFace.invalidateGameScreen();
				gameFace.onCheck();
			}
		}

		return false;
	}

	protected void afterUserMove() {
		Log.d(VALID_MOVES, "afterUserMove generate");
		if (showLegalMoves && isNewMove()) {
			validMoves = getBoardFace().generateValidMoves(true);
			previousFrom = from;
		}
	}

	/**
	 * Used to detect if we need to generate new valid moves or not
	 */
	private boolean isNewMove() {
		return from != previousFrom;
	}

	@Override
	public void updateParent() {
		gameFace.updateParentView();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == NotationView.NOTATION_ID) {// scroll to the specified position
			Integer pos = (Integer) v.getTag(R.id.list_item_id);

			resetValidMoves();
			BoardFace boardFace = getBoardFace();
			int totalHply = boardFace.getPly() - 1;
			if (totalHply < pos) {
				for (int i = totalHply; i < pos; i++) {
					boardFace.takeNext();
				}
			} else {
				for (int i = totalHply; i > pos; i--) {
					boardFace.takeBack();
				}
			}

			gameFace.onNotationClicked(pos);

			// TODO @comp: check, show animation for notation scroll
//			checkControlsButtons();
			invalidate();
		}
	}

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

			Bitmap boardBitmap;
			BitmapShader shader;

			if (customBoardId != NO_ID) {
				shader = setBoardFromResource(customBoardId);
			} else if (!TextUtils.isEmpty(appData.getThemeBoardPath())) {
				boardBitmap = BitmapFactory.decodeFile(appData.getThemeBoardPath());
				if (boardBitmap == null) {
					getAppData().setThemeBoardPath(Symbol.EMPTY); // clear theme
					boardBackPaint.setShader(setBoardFromResource(boardsDrawables[boardId]));
					return;
				}
				boardBitmap = Bitmap.createScaledBitmap(boardBitmap, viewWidth, viewWidth, true);

				shader = new BitmapShader(boardBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
			} else {
				shader = setBoardFromResource(boardsDrawables[boardId]);
			}

			boardBackPaint.setShader(shader);
		}
	}

	private BitmapShader setBoardFromResource(int resourceId) {
		Bitmap boardBitmap;
		BitmapShader shader;
		BitmapDrawable drawable = (BitmapDrawable) resources.getDrawable(resourceId);
		boardBitmap = drawable.getBitmap();

		int bitmapSize = viewHeight / 4;
		boardBitmap = Bitmap.createScaledBitmap(boardBitmap, bitmapSize, bitmapSize, true);
		shader = new BitmapShader(boardBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		return shader;
	}

	public void setCustomBoard(int resourceId) {
		customBoardId = resourceId;
	}

	private void setPieceBitmapFromArray(int[] drawableArray) {
		/* Note on android.developers.com
		In the past, a popular memory cache implementation was a SoftReference or WeakReference bitmap cache,
		however this is not recommended. Starting from Android 2.3 (API Level 9) the garbage collector is more
		aggressive with collecting soft/weak references which makes them fairly ineffective. In addition, prior
		to Android 3.0 (API Level 11), the backing data of a bitmap was stored in native memory which is not
		released in a predictable manner, potentially causing an application to briefly exceed its memory limits
		and crash.

		 */

		if (whitePiecesMap == null) {
			whitePiecesMap = new WeakHashMap<Integer, Bitmap>();
		}

		if (blackPiecesMap == null) {
			blackPiecesMap = new WeakHashMap<Integer, Bitmap>(); // TODO probably we don't need weak or soft referemces
		}

		bitmapOptions = new BitmapFactory.Options();
		// get bitmapOptions size. It's always the same for same sized drawables
		int drawableId = drawableArray[0];
		bitmapOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(getResources(), drawableId, bitmapOptions);

		for (int j = 0; j < 6; j++) {
			// create white piece
			drawableId = drawableArray[j];
			Bitmap pieceBitmap = createBitmapForPiece(drawableId);
			whitePiecesMap.put(j, pieceBitmap);

			// create black piece
			drawableId = drawableArray[6 + j];
			pieceBitmap = createBitmapForPiece(drawableId);
			blackPiecesMap.put(j, pieceBitmap);
		}
	}

	/**
	 * Create bitmap to be re-used, based on the size of one of the bitmaps
	 * pass bitmapOptions to get info
	 */
	private Bitmap createBitmapForPiece(int drawableId) {
		Bitmap pieceBitmap;

		// Calculate inSampleSize
		int pieceSize = squareSize - pieceInset;
		bitmapOptions.inSampleSize = calculateInSampleSize(bitmapOptions, pieceSize, pieceSize);

		// Decode bitmap with inSampleSize set
		bitmapOptions.inJustDecodeBounds = false;
		pieceBitmap = BitmapFactory.decodeResource(getResources(), drawableId, bitmapOptions);

		return pieceBitmap;
	}

	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and width
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// Choose the smallest ratio as inSampleSize value, this will guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	protected AppData getAppData() {
		return appData;
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
			case P_THE3D_ID:
				using3dPieces = true;
				setPieceBitmapFromArray(the3dPiecesDrawableIds);
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

	private int[] the3dPiecesDrawableIds = new int[]{
			R.drawable.the3d_wp,
			R.drawable.the3d_wn,
			R.drawable.the3d_wb,
			R.drawable.the3d_wr,
			R.drawable.the3d_wq,
			R.drawable.the3d_wk,
			R.drawable.the3d_bp,
			R.drawable.the3d_bn,
			R.drawable.the3d_bb,
			R.drawable.the3d_br,
			R.drawable.the3d_bq,
			R.drawable.the3d_bk,
	};

	public void updateBoardAndPiecesImgs() {
		boardId = getAppData().getChessBoardId();
		loadPieces(getAppData().getPiecesId());

		invalidate();
	}

//	private Paint initMoveArrowPaint(int arrowColor) {
//		Paint paint = new Paint();
//		paint.setStyle(Style.FILL);
//		paint.setAntiAlias(true);
//		paint.setColor(arrowColor);
//		paint.setAlpha(192);
//		return paint;
//	}

//	public final void setMoveHints(HashMap<org.petero.droidfish.gamelogic.Move, PieceColor> moveHints) {
//		/*boolean equal;
//		if ((this.moveHints == null) || (moveHints == null)) {
//			equal = this.moveHints == moveHints;
//		} else {
//			equal = this.moveHints.equals(moveHints);
//		}
//		if (!equal) {
//			this.moveHints = moveHints;
//			invalidate();
//		}*/
//	}

//	public final void drawMoveHints(Canvas canvas) {
//
//		if ((moveHints == null || moveHints.isEmpty()))
//			return;
//		float h = (float) (square / 2.0);
//		float d = (float) (square / 8.0);
//		double v = 35 * Math.PI / 180;
//		double cosv = Math.cos(v);
//		double sinv = Math.sin(v);
//		double tanv = Math.tan(v);
//
//		for (org.petero.droidfish.gamelogic.Move move : moveHints.keySet()) {
//			if ((move == null) || (move.from == move.to))
//				continue;
//			float x0 = getXCoordinate(Position.getX(move.from)) + h;
//			float y0 = getYCoordinateForArrow(Position.getY(move.from)) + h;
//			float x1 = getXCoordinate(Position.getX(move.to)) + h;
//			float y1 = getYCoordinateForArrow(Position.getY(move.to)) + h;
//
//			float x2 = (float) (Math.hypot(x1 - x0, y1 - y0) + d);
//			float y2 = 0;
//			float x3 = (float) (x2 - h * cosv);
//			float y3 = (float) (y2 - h * sinv);
//			float x4 = (float) (x3 - d * sinv);
//			float y4 = (float) (y3 + d * cosv);
//			float x5 = (float) (x4 + (-d / 2 - y4) / tanv);
//			float y5 = (float) (-d / 2);
//			float x6 = 0;
//			float y6 = y5 / 2;
//			Path path = new Path();
//			path.moveTo(x2, y2);
//			path.lineTo(x3, y3);
////          path.lineTo(x4, y4);
//			path.lineTo(x5, y5);
//			path.lineTo(x6, y6);
//			path.lineTo(x6, -y6);
//			path.lineTo(x5, -y5);
////          path.lineTo(x4, -y4);
//			path.lineTo(x3, -y3);
//			path.close();
//			Matrix mtx = new Matrix();
//			mtx.postRotate((float) (Math.atan2(y1 - y0, x1 - x0) * 180 / Math.PI));
//			mtx.postTranslate(x0, y0);
//			path.transform(mtx);
//
//			Paint p = moveHints.get(move) == PieceColor.WHITE ? whiteMoveArrowPaint : blackMoveArrowPaint;
//
//			canvas.drawPath(path, p);
//		}
//	}

	private int getXCoordinate(int x) {
		return squareSize * (getBoardFace().isReside() ? 7 - x : x);
	}

	private int getYCoordinate(int y) {
		return squareSize * (getBoardFace().isReside() ? 7 - y : y);
	}

	public void releaseBitmaps() {
		if (whitePiecesMap != null) {
			for (Bitmap bitmap : whitePiecesMap.values()) {
				bitmap.recycle();
			}
		}

		if (blackPiecesMap != null) {
			for (Bitmap bitmap : blackPiecesMap.values()) {
				bitmap.recycle();
			}
		}

		whitePiecesMap = null;
		blackPiecesMap = null;
	}

// todo: should be only one getYCoordinate method after refactoring
//	private int getYCoordinateForArrow(int y) {
//		return square * (getBoardFace().isReside() ? y : 7 - y);
//	}

// TODO: refactor!

	protected class MoveAnimator {
		long startTime;
		long stopTime;
		long now;
		int from1, to1, hide1 = -1;
		int from2, to2, hide2 = -1; // rename
		private Bitmap pieceBitmap;
		private Bitmap rookCastlingBitmap;
		private Bitmap capturedPieceBitmap;
		private int capturedPiecePosition;
		private boolean firstRun = true;
		private final Move move;
		private final boolean forward;
		private long animationTime;
		private boolean forceCompEngine;

		public MoveAnimator(Move move, boolean forward) {
			this.move = move;
			this.forward = forward;

			BoardFace boardFace = getBoardFace();
			int moveFromPosition = forward ? move.from : move.to;
			int fromColor = boardFace.getColor(moveFromPosition);
			int fromPiece = boardFace.getPiece(moveFromPosition);

			if (fromPiece == ChessBoard.EMPTY) {
//				throw new IllegalArgumentException("fromPiece can't be EMPTY square here, check Move generation object. move is " + move);
				fromPiece = ChessBoard.PAWN; // TODO fix real problem
			}
			if (fromColor == ChessBoard.EMPTY) {
				fromColor = 0;
			}

			pieceBitmap = getPieceBitmap(fromColor, fromPiece);

			// todo: check game load

			int moveToPosition = forward ? move.to : move.from;
			if (boardFace.getPiece(moveToPosition) != ChessBoard.EMPTY) {
				int capturedColor = boardFace.getColor(moveToPosition);
				int capturedPiece = boardFace.getPiece(moveToPosition);

				capturedPieceBitmap = getPieceBitmap(capturedColor, capturedPiece);
				capturedPiecePosition = moveToPosition;
				hide2 = moveToPosition;
			}

			// todo: refactor!
			if (forward) {
				from1 = move.from;
				to1 = move.to;
				hide1 = move.to;
				if (move.isCastling()) {
					if (move.to == move.from + 2) { // O-O
						from2 = move.to + 1;
						to2 = move.to - 1;
					} else if (move.to == move.from - 2) { // O-O-O
						from2 = move.to - 2;
						to2 = move.to + 1;
					}
					hide2 = to2;
					rookCastlingBitmap = getPieceBitmap(fromColor, ChessBoard.ROOK);
				}
			} else {
				from1 = move.to;
				to1 = move.from;
				hide1 = to1;

				if (move.isCastling()) {
					if (move.to == move.from + 2) { // O-O
						from2 = move.to - 1;
						to2 = move.to + 1;
					} else if (move.to == move.from - 2) { // O-O-O
						from2 = move.to + 1;
						to2 = move.to - 2;
					}
					hide2 = to2;
					rookCastlingBitmap = getPieceBitmap(fromColor, ChessBoard.ROOK);
				}
			}
		}


		public boolean isForward() {
			return forward;
		}

		public Bitmap getCapturedPieceBitmap() {
			return capturedPieceBitmap;
		}

		public int getCapturedPiecePosition() {
			return capturedPiecePosition;
		}

		public boolean updateState() {
			now = System.currentTimeMillis();
			return isAnimationActive();
		}

		private boolean isAnimationActive() {

			if (firstRun) {
				initTimer();
				firstRun = false;
			}

			if (/*(startTime < 0) ||*/ (now >= stopTime))
				return false;
			return true;
		}

		public long getAnimationTime() {
			return animationTime;
		}

		public boolean isSquareHidden(int square) {
			/*if (!isAnimationActive())
				return false;*/
			return square == hide1 || square == hide2;
		}

		private void initTimer() {
			int dx = ChessBoard.getFile(move.to) - ChessBoard.getFile(move.from);
			int dy = ChessBoard.getRank(move.to) - ChessBoard.getRank(move.from);
			double dist = Math.sqrt(dx * dx + dy * dy);
			double t = Math.sqrt(dist) * PIECE_ANIM_SPEED;
			animationTime = (int) Math.round(t);

			startTime = System.currentTimeMillis();
			stopTime = startTime + animationTime;
		}

		public final void draw(Canvas canvas) {

			if (!isAnimationActive()) {
				return;
			}

			double animationTimeFactor = (now - startTime) / (double) (stopTime - startTime);
			drawAnimPiece(canvas, pieceBitmap, from1, to1, animationTimeFactor);
			drawAnimPiece(canvas, rookCastlingBitmap, from2, to2, animationTimeFactor);
			/*long now2 = System.currentTimeMillis();
			long delay = 20 - (now2 - now);
			if (delay < 1) {
				delay = 1;
			}*/
			/*handler.postDelayed(new Runnable() {
				@Override
				public void run() {*/
			invalidate();
				/*}
			}, delay);*/
		}

		private void drawAnimPiece(Canvas canvas, Bitmap pieceBitmap, int from, int to, double animationTimeFactor) {
			if (pieceBitmap == null) {
				return;
			}

			final int xCrd1 = getXCoordinate(ChessBoard.getFile(from));
			final int yCrd1 = getYCoordinate(ChessBoard.getRank(from));
			final int xCrd2 = getXCoordinate(ChessBoard.getFile(to));
			final int yCrd2 = getYCoordinate(ChessBoard.getRank(to));
			final int xCrd = xCrd1 + (int) Math.round((xCrd2 - xCrd1) * animationTimeFactor);
			final int yCrd = yCrd1 + (int) Math.round((yCrd2 - yCrd1) * animationTimeFactor);

			if (using3dPieces) {
				rect.set(xCrd, yCrd - _3dPiecesOffset, xCrd + squareSize, yCrd + squareSize);
			} else {
				rect.set(xCrd, yCrd, xCrd + squareSize, yCrd + squareSize);
			}
			canvas.drawBitmap(pieceBitmap, null, rect, null);
		}

		public boolean isForceCompEngine() {
			return forceCompEngine;
		}

		public void setForceCompEngine(boolean forceCompEngine) {
			this.forceCompEngine = forceCompEngine;
		}

	}

	private Bitmap getPieceBitmap(int fromColor, int fromPiece) {
		Bitmap pieceBitmap;
		if (fromColor == ChessBoard.WHITE_SIDE) {
			pieceBitmap = whitePiecesMap.get(fromPiece);
		} else {
			pieceBitmap = blackPiecesMap.get(fromPiece);
		}
		return pieceBitmap;
	}

	protected boolean noMovesToAnimate() {
		return moveAnimator == null && secondMoveAnimator == null;
	}

	public void setMoveAnimator(Move move, boolean forward) {
		this.moveAnimator = new MoveAnimator(move, forward); // TODO avoid create instances all the time. Try to reuse object
	}

	public void setMoveAnimator(MoveAnimator moveAnimator) {
		this.moveAnimator = moveAnimator;
	}

	public void setSecondMoveAnimator(MoveAnimator moveAnimator) {
		this.secondMoveAnimator = moveAnimator;
	}

	public void resetValidMoves() {
		// todo: possible refactoring - get rid of resetValidMoves,
		// but Compare board Changing when drawing move coordinate highlights instead,
		// for example by moves list and currentMoveNumber
		Log.d(VALID_MOVES, "validlog 2 clear");
		validMoves.clear();
	}
}
