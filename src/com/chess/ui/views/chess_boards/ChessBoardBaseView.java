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
import com.chess.R;
import com.chess.statics.AppData;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.engine.ChessBoard;
import com.chess.ui.engine.Move;
import com.chess.ui.interfaces.boards.BoardFace;
import com.chess.ui.interfaces.boards.BoardViewFace;
import com.chess.ui.interfaces.game_ui.GameFace;
import com.chess.ui.views.NotationsView;
import com.chess.ui.views.PanelInfoGameView;
import com.chess.ui.views.game_controls.ControlsBaseView;
import com.chess.utilities.AppUtils;
import com.chess.utilities.FontsHelper;

import java.io.File;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ChessBoardBaseView class
 *
 * @author alien_roger
 * @created at: 25.04.12 10:54
 */
public abstract class ChessBoardBaseView extends View implements BoardViewFace, NotationsView.BoardForNotationFace {

	public static final int PIECE_ANIM_SPEED = 100; // 100ms
	public static final int PIECE_ANIM_FAST_SPEED = 5; // should be very fast
	public static final String VALID_MOVES = "valid_moves";
	private static final long FAST_MOVE_TASK_DELAY = 5;

	int pieceXDelta, pieceYDelta; // top/left pixel draw position relative to square

	private static final int SQUARES_IN_LINE = 8;
	public static final int EMPTY_ID = 6;
	protected float density;
	protected AppData appData;

	protected WeakHashMap<Integer, Bitmap> whitePiecesMap;
	protected WeakHashMap<Integer, Bitmap> blackPiecesMap;
	protected SharedPreferences preferences;

	protected boolean firstClick = true;
	protected boolean pieceSelected;
	protected boolean track;
	private boolean dragging;

	protected int W;
	protected int H;
	protected int side;
	protected float squareSize;
	protected int from;
	protected int to;
	private int previousFrom;

	protected float dragX = 0;
	protected float dragY = 0;
//	protected int trackX = 0;
//	protected int trackY = 0;

	private float numYOffset = 10;
	private float textYOffset = 3;

	protected Paint selectedPiecePaint;
	protected Paint moveToPaint;
	protected Paint coordinatesPaint;
	protected Paint madeMovePaint;
	//	protected Paint greenPaint;
	protected Paint possibleMovePaint;

	protected String[] letters = {"a", "b", "c", "d", "e", "f", "g", "h"};
	protected String[] numbers = {"1", "2", "3", "4", "5", "6", "7", "8"};

	protected float viewWidth;
	protected float viewHeight;
	private float previousWidth;

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
	private NotationFace notationsFace;
	private ControlsBaseView controlsBaseView;
	private PanelInfoGameView topPanelView;
	private PanelInfoGameView bottomPanelView;
	private Paint boardBackPaint;

	// new engine
	private MoveAnimator moveAnimator;
	private MoveAnimator secondMoveAnimator;
	//	private HashMap<org.petero.droidfish.gamelogic.Move, PieceColor> moveHints =
//			new HashMap<org.petero.droidfish.gamelogic.Move, PieceColor>();
//	private Paint whiteMoveArrowPaint;
//	private Paint blackMoveArrowPaint;
	//protected boolean navigating;
	private int draggingFrom = -1;
	private CopyOnWriteArrayList<Move> validMoves = new CopyOnWriteArrayList<Move>(); // lets try this type
	private BitmapFactory.Options bitmapOptions;
	private int pieceInset;
	private int customBoardId = NO_ID;
	private boolean use3dPieces;
	private Rect clipBoundsRect;
	private int _3dPiecesOffset;
	private int _3dPiecesOffsetDrag;
	private int coordinateColorLight;
	private int coordinateColorDark;
	private int customHighlight;
	private int[] customCoordinatesColors;
	private String customPiecesName;
	private boolean fastMode;
	private int _3dPieceMaxHeight;
	private Paint piecesPaint;
	private int _3dPiecesOffsetSmall;
	private boolean isChessKid = true;
	private boolean isTablet;
	private Bitmap boardBitmap;
	private int originalParentWidth;
	private int originalParentHeight;
	private boolean borderSubtracted;

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
		clipBoundsRect = new Rect();
		isTablet = AppUtils.isTablet(context);

		handler = new Handler();
		rect = new Rect();

		_3dPiecesOffset = resources.getDimensionPixelSize(R.dimen._3dPiece_offset_for_board);
		_3dPiecesOffsetSmall = resources.getDimensionPixelSize(R.dimen._3dPiece_offset_for_board_small);
		_3dPiecesOffsetDrag = resources.getDimensionPixelSize(R.dimen._3dPiece_offset_for_board_drag);
		pieceInset = (int) (1 * density);

		int highlightColor = appData.getThemeBoardHighlight();
		if (highlightColor == AppData.UNDEFINED) {
			highlightColor = resources.getColor(R.color.highlight_color);
		}

		piecesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		selectedPiecePaint = new Paint();
		selectedPiecePaint.setStyle(Style.FILL);
		selectedPiecePaint.setColor(highlightColor);

		int moveToIndicatorColor = resources.getColor(R.color.move_to_indicator_color);
		moveToPaint = new Paint();
		moveToPaint.setStrokeWidth(1.5f * density);
		moveToPaint.setStyle(Style.STROKE);
		moveToPaint.setColor(moveToIndicatorColor);

		int coordinateFont = resources.getInteger(R.integer.board_highlight_font);
		coordinateColorLight = appData.getThemeBoardCoordinateLight();
		if (coordinateColorLight == AppData.UNDEFINED) {
			coordinateColorLight = resources.getColor(R.color.coordinate_color_light);
		}
		coordinateColorDark = appData.getThemeBoardCoordinateDark();
		if (coordinateColorDark == AppData.UNDEFINED) {
			coordinateColorDark = resources.getColor(R.color.coordinate_color_dark);
		}

		numYOffset = coordinateFont * density;
		textYOffset *= density;

		coordinatesPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		coordinatesPaint.setStyle(Style.FILL);
		coordinatesPaint.setTextSize(coordinateFont * density);
		coordinatesPaint.setTypeface(FontsHelper.getInstance().getTypeFace(getContext(), FontsHelper.BOLD_FONT));

		madeMovePaint = new Paint();
		madeMovePaint.setStyle(Style.FILL);
		madeMovePaint.setColor(highlightColor);

//		greenPaint = new Paint();
//		greenPaint.setStrokeWidth(1.5f * density);
//		greenPaint.setStyle(Style.STROKE);
//		greenPaint.setColor(Color.GREEN);

		int possibleMoveHighlightColor = resources.getColor(R.color.possible_move_highlight);

		possibleMovePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		possibleMovePaint.setStyle(Style.FILL);
		possibleMovePaint.setColor(possibleMoveHighlightColor);

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
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
//		logTest("onMeasure parentWidth = " + parentWidth + " parentHeight = " + parentHeight);

		int width = 0;
		int height = 0;
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			width = resolveSize(parentWidth, widthMeasureSpec);
			height = resolveSize(parentWidth, heightMeasureSpec);
		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			width = resolveSize(parentHeight, widthMeasureSpec);
			height = resolveSize(parentHeight, heightMeasureSpec);
		}

		setMeasuredDimension(width, height);
		pieceXDelta = -1;
		pieceYDelta = -1;
	}

	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		viewWidth = (xNew == 0 ? viewWidth : xNew);
		viewHeight = (yNew == 0 ? viewHeight : yNew);
		squareSize = viewWidth / 8f;
//		logTest("onSizeChanged width = " + viewWidth + " height = " + viewHeight + ", squareSize = " + squareSize);

		loadBoard();
		loadPieces();
		updateCustomColors();
	}

	private void updateCustomColors() {
		if (customHighlight != 0) {
			madeMovePaint.setColor(customHighlight);
		}

		if (customCoordinatesColors != null) {
			coordinateColorLight = customCoordinatesColors[0];
			coordinateColorDark = customCoordinatesColors[1];
		}
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

			if (use3dPieces) {
				getDrawingRect(clipBoundsRect);
				int saveCount = canvas.save(Canvas.CLIP_SAVE_FLAG);
				clipBoundsRect.set(clipBoundsRect.left, clipBoundsRect.top - _3dPiecesOffsetDrag, clipBoundsRect.right, clipBoundsRect.bottom);
				canvas.clipRect(clipBoundsRect, Region.Op.REPLACE);

				drawMoveToIndicator(canvas);
				drawPiecesAndAnimation(canvas);
				drawPieceInDragMotion(canvas);

				canvas.restoreToCount(saveCount);
			} else {
				drawMoveToIndicator(canvas);
				drawPiecesAndAnimation(canvas);
				drawPieceInDragMotion(canvas);
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

	public void setNotationsFace(NotationFace notationsFace) {
		this.notationsFace = notationsFace;
	}

	@Override
	public void showOptions() {
		gameFace.showOptions();
	}

	protected boolean isUserWhite() {
		return gameFace.isUserColorWhite();
	}

	@Override
	public boolean moveBack() {
		resetMoving();

		BoardFace boardFace = getBoardFace();
		if (noMovesToAnimate() && boardFace.getPly() > 0) {
			gameFace.updateParentView();

			boardFace.setFinished(false);
			pieceSelected = false;
			setMoveAnimator(boardFace.getLastMove(), false);
			resetValidMoves();
			boardFace.takeBack();

			invalidate();
			gameFace.invalidateGameScreen();

			if (notationsFace != null) { // we might don't have notations  so probably should be moved to fragment level
				notationsFace.moveBack(boardFace.getPly());
			}

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean moveForward() {
		resetMoving();

		if (noMovesToAnimate()) {
			gameFace.updateParentView();

			pieceSelected = false;

			Move move = getBoardFace().getNextMove();
			if (move == null) {
				return false;
			}
			setMoveAnimator(move, true);
			resetValidMoves();
			getBoardFace().takeNext();

			invalidate();
			gameFace.invalidateGameScreen();

			if (notationsFace != null) {
				notationsFace.moveForward(getBoardFace().getPly());
			}

			return true;
		} else {
			return false;
		}
	}

	/**
	 * We made move, but bcz it's animated we can't make next move, so postpone next move animation
	 */
	@Override
	public void moveBackFast() {
		if (fastMode) {
			moveBack();
			handler.postDelayed(moveBackRunnable, FAST_MOVE_TASK_DELAY);
		} else {
			handler.removeCallbacks(moveBackRunnable);
		}
	}

	/**
	 * We made move, but bcz it's animated we can't make next move, so postpone next move animation
	 */
	@Override
	public void moveForwardFast() {
		if (fastMode) {
			moveForward();
			handler.postDelayed(moveForwardRunnable, FAST_MOVE_TASK_DELAY);
		} else {
			handler.removeCallbacks(moveForwardRunnable);
		}
	}

	private Runnable moveBackRunnable = new Runnable() {
		@Override
		public void run() {
			if (gameFace.isAlive()) {
				moveBackFast();
			}
		}
	};

	private Runnable moveForwardRunnable = new Runnable() {
		@Override
		public void run() {
			if (gameFace.isAlive()) {
				moveForwardFast();
			}
		}
	};

	@Override
	public void switchAnalysis() {
		gameFace.switch2Analysis();
	}

	@Override
	public void newGame() {
		gameFace.newGame();
	}

	public void updateNotations(String[] notations) {
		if (notationsFace != null) {
			notationsFace.updateNotations(notations, this, getBoardFace().getPly());
		}

		drawCapturedPieces();
		invalidate();
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
				if (gameFace != null) {
					gameFace.releaseScreenLockFlag();
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
						boardFace.takeNext();
					} else {
						boardFace.takeBack();
					}
					resetValidMoves();
					onSecondMoveAnimated();
				} else {
					gameFace.updateParentView();
					//navigating = false;
				}
			}
		}

		// if first part of animation(castling) finished, start second
		if (moveAnimator == null && secondMoveAnimator != null) {
			animationActive = secondMoveAnimator.updateState();
			drawPieces(canvas, animationActive, secondMoveAnimator);
			if (animationActive) {
				secondMoveAnimator.draw(canvas);
			} else {
				secondMoveAnimator = null;
				gameFace.updateParentView();
				//navigating = false;
			}
		}
	}

	protected void drawPieces(Canvas canvas, boolean animationActive, MoveAnimator moveAnimator) {
		BoardFace boardFace = getBoardFace();

		if (boardFace.isReside()) {
			for (int pos = ChessBoard.SQUARES_CNT - 1; pos >= 0; pos--) {
				// do not draw if in drag or animated
				if ((dragging && pos == from) || (animationActive && moveAnimator.isSquareHidden(pos))) {
					continue;
				}

				if (drawPieceOnCanvas(canvas, boardFace, pos)) {
					return;
				}
			}
		} else {
			for (int pos = 0; pos < ChessBoard.SQUARES_CNT; pos++) {
				// do not draw if in drag or animated
				if ((dragging && pos == from) || (animationActive && moveAnimator.isSquareHidden(pos))) {
					continue;
				}

				if (drawPieceOnCanvas(canvas, boardFace, pos)) {
					return;
				}
			}
		}

		// draw piece that will be captured
		if (animationActive && moveAnimator.getCapturedPieceBitmap() != null) {
			Bitmap capturedPieceBitmap = moveAnimator.getCapturedPieceBitmap();

			if (capturedPieceBitmap.isRecycled()) {
				return;
			}

			int piecePosition = moveAnimator.getCapturedPiecePosition();
			int piece = boardFace.getPiece(piecePosition);
			int column = ChessBoard.getColumn(piecePosition, boardFace.isReside());
			int row = ChessBoard.getRow(piecePosition, boardFace.isReside());

			int left = (int) (column * squareSize + pieceInset);
			int right = (int) (column * squareSize + squareSize - pieceInset);
			int bottom = (int) (row * squareSize + squareSize - pieceInset);
			int top = (int) (row * squareSize + pieceInset);
			if (use3dPieces) {
				top = (int) (row * squareSize + pieceInset - _3dPiecesOffset);
				top = adjustHeightFor3dPiece(capturedPieceBitmap, bottom, top, piece);
			}
			rect.set(left, top, right, bottom);
			canvas.drawBitmap(capturedPieceBitmap, null, rect, piecesPaint);
		}
	}

	private boolean drawPieceOnCanvas(Canvas canvas, BoardFace boardFace, int pos) {
		int color = boardFace.getColor(pos);
		int piece = boardFace.getPiece(pos);
		int file = ChessBoard.getColumn(pos, boardFace.isReside());
		int rank = ChessBoard.getRow(pos, boardFace.isReside());
		// TODO rework logic to store changed pieces and redraw only them
		if (color != ChessBoard.EMPTY && piece != ChessBoard.EMPTY) {    // here is the simple replace/redraw of piece // draw it bit inside of square
			Bitmap pieceBitmap = getPieceBitmap(color, piece);

			if (pieceBitmap == null || pieceBitmap.isRecycled()) { // we closed the view, no need to show animation. // TODO find better way of using bitmaps
				return true;
			}

			// calculate piece size bounds
			int left = (int) (file * squareSize + pieceInset);
			int right = (int) (file * squareSize + squareSize - pieceInset);
			int bottom = (int) (rank * squareSize + squareSize - pieceInset);
			int top = (int) (rank * squareSize + pieceInset);
			if (use3dPieces) {
				top = (int) (rank * squareSize + pieceInset - _3dPiecesOffset);
				top = adjustHeightFor3dPiece(pieceBitmap, bottom, top, piece);
			}

			rect.set(left, top, right, bottom);
			canvas.drawBitmap(pieceBitmap, null, rect, piecesPaint);
		}
		return false;
	}

	private int adjustHeightFor3dPiece(Bitmap pieceBitmap, int bottom, int top, int piece) {
		// chesskid pieces are different
		// pawn 117 / 177
		// knight 151 / 177
		// bishop 148 / 177
		// rook 136 / 177
		// queen 172 / 177
		// king 177 / 177

		float scale = 1;
		if (isChessKid) {
			switch (piece) {
				case ChessBoard.PAWN:
					if (isTablet) {
						scale = 0.861f;
					} else {
						scale = 0.661f;
					}
					break;
				case ChessBoard.KNIGHT:
					scale = 0.95f;
					break;
				case ChessBoard.BISHOP:
					if (isTablet) {
						scale = 0.93f;
					} else {
						scale = 0.77f;
					}
					break;
				case ChessBoard.ROOK:
					if (isTablet) {
						scale = 0.9683f;
					} else {
						scale = 0.7683f;
					}
					break;
				case ChessBoard.QUEEN:
					scale = 0.971f;
					break;
				case ChessBoard.KING:
					scale = 1;
					break;
			}
		} else {
			// pawn 112 / 125
			// knight 114 / 125
			// bishop 118 / 125
			// rook 120 / 125
			// queen 122 / 125
			// king 125 / 125
			switch (piece) {
				case ChessBoard.PAWN:
					if (isTablet) {
						scale = 0.86f;
					} else {
						scale = 0.76f;
					}
					break;
				case ChessBoard.KNIGHT:
					scale = 0.912f;
					break;
				case ChessBoard.BISHOP:
					scale = 0.944f;
					break;
				case ChessBoard.ROOK:
					scale = 0.96f;
					break;
				case ChessBoard.QUEEN:
					scale = 0.976f;
					break;
				case ChessBoard.KING:
					scale = 1;
					break;
			}
		}

		float offset = pieceBitmap.getHeight() - pieceBitmap.getHeight() * scale;
		top += offset;

		return top;
	}

	protected void drawPieceInDragMotion(Canvas canvas) {
		if (dragging) {
			int color = getBoardFace().getColor(draggingFrom);
			int piece = getBoardFace().getPiece(draggingFrom);

			float halfSquare = squareSize / 2;
			int x = (int) (dragX - halfSquare);
			int y = (int) (dragY - halfSquare);
			if (color != ChessBoard.EMPTY && piece != ChessBoard.EMPTY) {
				Bitmap pieceBitmap = getPieceBitmap(color, piece);
				if (pieceBitmap == null || pieceBitmap.isRecycled()) { // we closed the view, no need to show animation. // TODO find better way of using bitmaps
					return;
				}

				// set piece bounds
				int left = (int) (x - halfSquare);
				int right = (int) (x + squareSize + halfSquare);
				int bottom = (int) (y + squareSize + halfSquare);
				int top = (int) (y - halfSquare);
				if (use3dPieces) {
					top = (int) (y - halfSquare - _3dPiecesOffsetDrag);
					top = adjustHeightFor3dPiece(pieceBitmap, bottom, top, piece);
				}

				rect.set(left, top, right, bottom);
				// draw piece
				canvas.drawBitmap(pieceBitmap, null, rect, piecesPaint);
			}
		}
	}

	private void drawMoveToIndicator(Canvas canvas) {
		if (dragging) {
			int color = getBoardFace().getColor(draggingFrom);
			int piece = getBoardFace().getPiece(draggingFrom);

			float halfSquare = squareSize / 2;
			int file = (int) ((dragX - dragX % squareSize) / squareSize);
			int rank = (int) (((dragY + squareSize) - (dragY + squareSize) % squareSize) / squareSize);
			if (color != ChessBoard.EMPTY && piece != ChessBoard.EMPTY) {
				Bitmap pieceBitmap = getPieceBitmap(color, piece);
				if (pieceBitmap == null || pieceBitmap.isRecycled()) { // we closed the view, no need to show animation. // TODO find better way of using bitmaps
					return;
				}

				// draw highlight rect around the square
				int rectLeft = (int) (file * squareSize - halfSquare);
				int rectTop = (int) (rank * squareSize - halfSquare);
				int rectRight = (int) (file * squareSize + squareSize + halfSquare);
				int rectBottom = (int) (rank * squareSize + squareSize + halfSquare);
				canvas.drawRect(rectLeft, rectTop, rectRight, rectBottom, moveToPaint);
			}
		}
	}

	protected void drawCoordinates(Canvas canvas) {
		if (showCoordinates) {
			BoardFace boardFace = getBoardFace();
			// original square is 53 px, right offset is 2px
			float xRightOffset = squareSize * 2 / 53;
			float xNumberPosition = 2 * density;
			float yPosition = SQUARES_IN_LINE * squareSize - textYOffset;
			for (int i = 0; i < SQUARES_IN_LINE; i++) {

				if (i % 2 == 0) {
					coordinatesPaint.setColor(coordinateColorDark);
				} else {
					coordinatesPaint.setColor(coordinateColorLight);
				}
				if (boardFace.isReside()) {
					String letter = letters[i];
					float letterWidth = coordinatesPaint.measureText(letter);
					float textXCrd = (8 - i) * squareSize - xRightOffset - letterWidth;
					// draw ranks coordinates (1, 2, 3, 4, 5, 6, 7, 8)
					canvas.drawText(numbers[i], xNumberPosition, i * squareSize + numYOffset, coordinatesPaint);
					// draw file coordinates (a, b, c, d, e, f, g, h)

					canvas.drawText(letter, textXCrd, yPosition, coordinatesPaint);
				} else {
					String letter = letters[7 - i];
					float letterWidth = coordinatesPaint.measureText(letter);
					float textXCrd = (8 - i) * squareSize - xRightOffset - letterWidth;

					// draw ranks coordinates (8, 7, 6, 5, 4, 3, 2, 1)
					canvas.drawText(numbers[7 - i], xNumberPosition, i * squareSize + numYOffset, coordinatesPaint);
					// draw file coordinates (h, g, f, e, d, c, b,a,)
					canvas.drawText(letter, textXCrd, yPosition, coordinatesPaint);
				}
			}
		}
	}

	protected void drawHighlights(Canvas canvas) {

		BoardFace boardFace = getBoardFace();
		if (pieceSelected) { // fill square for the start move piece position
			float x = ChessBoard.getColumn(from, boardFace.isReside());
			float y = ChessBoard.getRow(from, boardFace.isReside());

			float left = x * squareSize;
			float top = y * squareSize;
			float right = x * squareSize + squareSize;
			float bottom = y * squareSize + squareSize;

			canvas.drawRect(left, top, right, bottom, selectedPiecePaint);
		} else if (isHighlightEnabled && boardFace.getPly() > 0) { // draw moved piece highlight from -> to
			Move move = boardFace.getHistDat()[boardFace.getPly() - 1].move;
			{// from
				int x1 = ChessBoard.getColumn(move.from, boardFace.isReside());
				int y1 = ChessBoard.getRow(move.from, boardFace.isReside());
				float left = x1 * squareSize;
				float top = y1 * squareSize;
				float right = x1 * squareSize + squareSize;
				float bottom = y1 * squareSize + squareSize;

				canvas.drawRect(left, top, right, bottom, madeMovePaint);
			}
			{// to
				int x2 = ChessBoard.getColumn(move.to, boardFace.isReside());
				int y2 = ChessBoard.getRow(move.to, boardFace.isReside());
				float left = x2 * squareSize;
				float top = y2 * squareSize;
				float right = x2 * squareSize + squareSize;
				float bottom = y2 * squareSize + squareSize;

				canvas.drawRect(left, top, right, bottom, madeMovePaint);
			}
		}

		// draw semi-transparent dots all possible moves for selected piece
		if (pieceSelected && showLegalMoves) {

			boolean isWhiteToMove = boardFace.isWhiteToMove();
			boolean isUserWhite = isUserWhite();
			boolean isUsersTurn = ((isUserWhite && isWhiteToMove) || (!isUserWhite && !isWhiteToMove))
					|| ChessBoard.isHumanVsHumanGameMode(getBoardFace());

//			Log.d(VALID_MOVES, "draw validMoves.isEmpty() " + validMoves.isEmpty());
//			Log.d(VALID_MOVES, "draw validMoves.size() " + validMoves.size());
//			Log.d(VALID_MOVES, "draw isUsersTurn " + isUsersTurn);
//			Log.d(VALID_MOVES, "draw isNewMove() " + isNewMove());
			if (isNewMove()) {
				if (isUserAbleToMove(boardFace.getColor(from)) && isUsersTurn) {
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

	protected boolean processTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	protected boolean isLocked() {
		return locked || gameFace == null || !gameFace.currentGameExist(); // fix NPE when user clicks on empty board, probably should be refactored
	}

	protected boolean isUserAbleToMove(int color) {
		return gameFace.isUserAbleToMove(color);
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
		int file = (int) ((event.getX() - event.getX() % squareSize) / squareSize);
		int rank = (int) ((event.getY() - event.getY() % squareSize) / squareSize);
		if (file > 7 || file < 0 || rank > 7 || rank < 0) {
			invalidate();
			return false;
		}
		BoardFace boardFace = getBoardFace();
		int fromSquare = ChessBoard.getPositionIndex(file, rank, boardFace.isReside());
		boolean userAbleToMove = isUserAbleToMove(boardFace.getColor(fromSquare));
		if (firstClick) {
			from = fromSquare;

			if (userAbleToMove) {
				pieceSelected = true;
				firstClick = false;
				invalidate();
			}
		} else {
			// don't touch not our piece or don't make empty square as fromSquare
			if (!boardFace.isAnalysis() && userAbleToMove && !pieceSelected) {
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

		int file = (int) ((dragX - dragX % squareSize) / squareSize);
		int rank = (int) ((dragY - dragY % squareSize) / squareSize);

		if (file > 7 || file < 0 || rank > 7 || rank < 0) {
			invalidate();
			return false;
		}

		BoardFace boardFace = getBoardFace();
		if (!dragging && !pieceSelected) {
			from = ChessBoard.getPositionIndex(file, rank, boardFace.isReside());
		}

		if (!firstClick) {
			draggingFrom = from;
			// do not drag captured piece // ??
			dragging = isUserAbleToMove(boardFace.getColor(draggingFrom)); // we do check in GameBaseFragment if boardFace is in Analysis mode
			to = ChessBoard.getPositionIndex(file, rank, boardFace.isReside());
			invalidate();
		}
		return true;
	}

	protected boolean onActionUp(MotionEvent event) {
		gameFace.updateParentView();

		int file = (int) ((event.getX() - event.getX() % squareSize) / squareSize);
		int rank = (int) ((event.getY() - event.getY() % squareSize) / squareSize);

		boolean showAnimation = !dragging;

		dragging = false;
		draggingFrom = -1;
		// if outside of the boardBitmap - return
		if (file > 7 || file < 0 || rank > 7 || rank < 0) { // if touched out of board
			invalidate();
			return false;
		}

		BoardFace boardFace = getBoardFace();

		int selectedSquare = ChessBoard.getPositionIndex(file, rank, boardFace.isReside());
		boolean isUserAbleToMove = isUserAbleToMove(boardFace.getColor(selectedSquare));
		if (firstClick) {
			from = selectedSquare;
			if (isUserAbleToMove || (boardFace.isAnalysis() && boardFace.getPiece(to) != ChessBoard.EMPTY)) {
				pieceSelected = true;
				firstClick = false;
				invalidate();
			}
		} else {
			to = selectedSquare;
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
			} else if (boardFace.getPiece(to) != ChessBoard.EMPTY && isUserAbleToMove) { // capture
				pieceSelected = true;
				firstClick = false;
				from = selectedSquare;
			}
			invalidate();
		}
		return true;
	}

	private void logTest(String message) {
		Log.d("TEST", message);
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

		if (getResources() == null) {
			Log.e("TEST", " resources are null"); // shouldn't be anyway
		}

		BoardFace boardFace = getBoardFace();
		int side = boardFace.getSide();
		if (!boardFace.isAnalysis()) {
			int titleId = 0;
			String reasonStr = Symbol.EMPTY;

			if (!boardFace.isPossibleToMakeMoves()) {
				if (boardFace.isPerformCheck(side)) {
					String whitePlayerName = gameFace.getWhitePlayerName();
					String blackPlayerName = gameFace.getBlackPlayerName();
					if (side == ChessBoard.WHITE_SIDE) {
						titleId = R.string.black_wins;
						reasonStr = getResources().getString(R.string.won_by_checkmate, blackPlayerName);
					} else {
						titleId = R.string.white_wins;
						reasonStr = getResources().getString(R.string.won_by_checkmate, whitePlayerName);
					}
				} else {
					titleId = R.string.draw_by_stalemate;
					reasonStr = getResources().getString(R.string.stalemate);
				}
			} else if (boardFace.getRepetitions() == 3) {
				titleId = R.string.draw_by_3fold_repetition;
				reasonStr = getResources().getString(R.string.draw_rep);

			}

			if (titleId != 0) {
				boardFace.setFinished(true);

				String title = getResources().getString(titleId);
				gameFace.onGameOver(title, reasonStr);
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
		if (v.getId() == NotationsView.NOTATION_ID) {// scroll to the specified position
			resetMoving();

			Integer pos = (Integer) v.getTag(R.id.list_item_id);

			goToMove(pos);
			drawCapturedPieces();

			gameFace.onNotationClicked(pos);

			// TODO @comp: check, show animation for notation scroll
//			checkControlsButtons();
			invalidate();
		}
	}

	public void goToMove(Integer pos) {
		resetValidMoves();
		BoardFace boardFace = getBoardFace();
		int totalHply = boardFace.getPly() - 1;
		if (totalHply < pos) {
			for (int i = totalHply; i < pos; i++) {
				boardFace.takeNext(false);
			}
		} else {
			for (int i = totalHply; i > pos; i--) {
				boardFace.takeBack();
			}
		}
	}

	public void goToLatestMove() {
		goToMove(getBoardFace().getMovesCount() - 1);
	}

	public void lockBoard(boolean lock) {
		locked = lock;
		lockBoardControls(lock);
		setEnabled(!lock);
	}

	public void lockBoardControls(boolean lock) {
		// todo: probably also lock Notation navigator
		controlsBaseView.lock(lock);
	}

	protected void loadBoard() {
		if (viewWidth > 0 ) {
			previousWidth = viewWidth;

			BitmapShader shader;

			if (customBoardId != NO_ID) {
				shader = setBoardFromResource();
			} else if (appData.isUseThemeBoard()) {
				// perform recycle
				recycleBoardBitmap();

				try {
					boardBitmap = BitmapFactory.decodeFile(appData.getThemeBoardPath());
				} catch (OutOfMemoryError ignore) {

				}
				if (boardBitmap == null) {
					getAppData().setThemeBoardPath(Symbol.EMPTY); // clear theme
					boardBackPaint.setShader(setBoardFromResource());
					return;
				}

				boardBitmap = Bitmap.createScaledBitmap(boardBitmap, (int) viewWidth, (int) viewWidth, true);

				shader = new BitmapShader(boardBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
			} else {
				shader = setBoardFromResource();
			}

			boardBackPaint.setShader(shader);
		}
	}

	private BitmapShader setBoardFromResource() {

		Context context = getContext();
		int resourceId;
		if (customBoardId != NO_ID) {
			resourceId = customBoardId;
		} else {
			String themeBoardName = appData.getThemeBoardName();
			if (themeBoardName.equals(context.getString(R.string.board_wood_dark))) {
				resourceId = R.drawable.board_wood_dark;
			} else if (themeBoardName.equals(context.getString(R.string.board_wood_light))) {
				resourceId = R.drawable.board_wood_light;
			} else if (themeBoardName.equals(context.getString(R.string.board_blue))) {
				resourceId = R.drawable.board_blue;
			} else if (themeBoardName.equals(context.getString(R.string.board_brown))) {
				resourceId = R.drawable.board_brown;
			} else if (themeBoardName.equals(context.getString(R.string.board_green))) {
				resourceId = R.drawable.board_green;
			} else if (themeBoardName.equals(context.getString(R.string.board_grey))) {
				resourceId = R.drawable.board_grey;
			} else if (themeBoardName.equals(context.getString(R.string.board_marble))) {
				resourceId = R.drawable.board_marble;
			} else if (themeBoardName.equals(context.getString(R.string.board_red))) {
				resourceId = R.drawable.board_red;
			} else if (themeBoardName.equals(context.getString(R.string.board_tan))) {
				resourceId = R.drawable.board_tan;
			} else { // if board wasn't selected yet, use default
				resourceId = R.drawable.board_wood_dark;
			}
		}

		// perform recycle
		recycleBoardBitmap();

		BitmapShader shader;
		BitmapDrawable drawable = (BitmapDrawable) resources.getDrawable(resourceId);
		boardBitmap = drawable.getBitmap();


		int bitmapSize = (int) Math.ceil(viewWidth / 4);
//		Log.d("TEST", " squareSize = " + squareSize);
//		Log.d("TEST", " boardBitmap size = " + bitmapSize);
		boardBitmap = Bitmap.createScaledBitmap(boardBitmap, bitmapSize, bitmapSize, true);
		shader = new BitmapShader(boardBitmap, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

		// update squareSize to match board properties and draw highlights correctly
//		if (!AppUtils.is10InchTablet(context)) {
		squareSize = bitmapSize / 2f;
//		}

//		Log.d("TEST", " squareSize = " + squareSize);

		return shader;
	}

	private void recycleBoardBitmap() {
		if (boardBitmap != null) {
			boardBitmap.recycle();
			boardBitmap = null;
		}
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
		// Decode bitmap with inSampleSize set
		bitmapOptions.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(getResources(), drawableId, bitmapOptions);
	}

	private Bitmap createBitmapForPiece(String filePath) {
		bitmapOptions.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(filePath, bitmapOptions);
	}

	protected AppData getAppData() {
		return appData;
	}

	protected void loadPieces() {
		Context context = getContext();
		if (context == null) {
			return;
		}

		if (!TextUtils.isEmpty(customPiecesName)) {
			setDefaultPiecesByName(context, customPiecesName);
			return;
		}

		if (getAppData().isUseThemePieces()) {
			String piecesThemePath = appData.getThemePiecesPath();

			use3dPieces = appData.isThemePieces3d();

			if (use3dPieces && !appData.getThemePiecesName().toLowerCase().contains("chesskid")) { // little hardcode here, until i'll re-write the logic
				_3dPiecesOffset = _3dPiecesOffsetSmall;
				isChessKid = false;
			}

			if (whitePiecesMap == null) {
				whitePiecesMap = new WeakHashMap<Integer, Bitmap>();
			} else {
				for (Bitmap bitmap : whitePiecesMap.values()) {
					bitmap.recycle();
				}
			}

			if (blackPiecesMap == null) {
				blackPiecesMap = new WeakHashMap<Integer, Bitmap>(); // TODO probably we don't need weak or soft referemces
			} else {
				for (Bitmap bitmap : blackPiecesMap.values()) {
					bitmap.recycle();
				}
			}

			File dirForPieces = AppUtils.getLocalDirForPieces(context, piecesThemePath);

			bitmapOptions = new BitmapFactory.Options();
			// get bitmapOptions size. It's always the same for same sized drawables
			String testPath = dirForPieces.getAbsolutePath() + "/wq.png";
			bitmapOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(testPath, bitmapOptions);

			_3dPieceMaxHeight = 0;
			// create white pieces
			String[] whitePieceImageCodes = ChessBoard.whitePieceImageCodes;
			for (int i = 0; i < whitePieceImageCodes.length; i++) {
				String pieceImageCode = whitePieceImageCodes[i];
				String filePath = dirForPieces.getAbsolutePath() + "/" + pieceImageCode + ".png";
				Bitmap pieceBitmap = createBitmapForPiece(filePath);

				if (pieceBitmap == null) {
					appData.setUseThemePieces(false);
					appData.setThemePiecesPath(Symbol.EMPTY);
					appData.setThemePiecesId(AppData.UNDEFINED);
					appData.setThemePieces3d(false);

					loadPieces();
					return;
				}

				whitePiecesMap.put(i, pieceBitmap);

				// get max height for 3d pieces
				if (use3dPieces) {
					_3dPieceMaxHeight = Math.max(pieceBitmap.getHeight(), _3dPieceMaxHeight);
				}
			}

			// create black pieces
			String[] blackPieceImageCodes = ChessBoard.blackPieceImageCodes;
			for (int i = 0; i < blackPieceImageCodes.length; i++) {
				String pieceImageCode = blackPieceImageCodes[i];
				String filePath = dirForPieces.getAbsolutePath() + "/" + pieceImageCode + ".png";
				Bitmap pieceBitmap = createBitmapForPiece(filePath);

				blackPiecesMap.put(i, pieceBitmap);
			}
		} else {
			String themePiecesName = appData.getThemePiecesName();
			setDefaultPiecesByName(context, themePiecesName);
		}
	}

	private void setDefaultPiecesByName(Context context, String themePiecesName) {
		if (themePiecesName.equals(context.getString(R.string.pieces_game))) {
			setPieceBitmapFromArray(gamePiecesDrawableIds);
		} else if (themePiecesName.equals(context.getString(R.string.pieces_alpha))) {
			setPieceBitmapFromArray(alphaPiecesDrawableIds);
		} else if (themePiecesName.equals(context.getString(R.string.pieces_book))) {
			setPieceBitmapFromArray(bookPiecesDrawableIds);
		} else if (themePiecesName.equals(context.getString(R.string.pieces_cases))) {
			setPieceBitmapFromArray(casesPiecesDrawableIds);
		} else if (themePiecesName.equals(context.getString(R.string.pieces_classic))) {
			setPieceBitmapFromArray(classicPiecesDrawableIds);
		} else if (themePiecesName.equals(context.getString(R.string.pieces_club))) {
			setPieceBitmapFromArray(clubPiecesDrawableIds);
		} else if (themePiecesName.equals(context.getString(R.string.pieces_condal))) {
			setPieceBitmapFromArray(condalPiecesDrawableIds);
		} else if (themePiecesName.equals(context.getString(R.string.pieces_maya))) {
			setPieceBitmapFromArray(mayaPiecesDrawableIds);
		} else if (themePiecesName.equals(context.getString(R.string.pieces_modern))) {
			setPieceBitmapFromArray(modernPiecesDrawableIds);
		} else if (themePiecesName.equals(context.getString(R.string.pieces_vintage))) {
			setPieceBitmapFromArray(vintagePiecesDrawableIds);
		} else { // if pieces wasn't selected yet, use default
			setPieceBitmapFromArray(gamePiecesDrawableIds);
		}
	}

	@Override
	public void setFastMovesMode(boolean fastMode) {
		this.fastMode = fastMode;

		if (!fastMode) { // stop any repeats
			removeCallbacks(moveBackRunnable);
			removeCallbacks(moveForwardRunnable);
		}
	}

	public static final int[] gamePiecesDrawableIds = new int[]{
			R.drawable.game_room_wp,
			R.drawable.game_room_wn,
			R.drawable.game_room_wb,
			R.drawable.game_room_wr,
			R.drawable.game_room_wq,
			R.drawable.game_room_wk,
			R.drawable.game_room_bp,
			R.drawable.game_room_bn,
			R.drawable.game_room_bb,
			R.drawable.game_room_br,
			R.drawable.game_room_bq,
			R.drawable.game_room_bk,
	};

	public static final int[] alphaPiecesDrawableIds = new int[]{
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

	public static final int[] bookPiecesDrawableIds = new int[]{
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

	public static final int[] casesPiecesDrawableIds = new int[]{
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

	public static final int[] classicPiecesDrawableIds = new int[]{
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

	public static final int[] clubPiecesDrawableIds = new int[]{
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


	public static final int[] condalPiecesDrawableIds = new int[]{
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

	public static final int[] mayaPiecesDrawableIds = new int[]{
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

	public static final int[] modernPiecesDrawableIds = new int[]{
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

	public static final int[] vintagePiecesDrawableIds = new int[]{
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
		loadPieces();
		loadBoard();
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
		return (int) (squareSize * (getBoardFace().isReside() ? 7 - x : x));
	}

	private int getYCoordinate(int y) {
		return (int) (squareSize * (getBoardFace().isReside() ? 7 - y : y));
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

		if (boardBitmap != null) {
			boardBitmap.recycle();
			boardBitmap = null;
		}

		Runtime.getRuntime().gc(); // TODO remove that when find solution
	}

	public void setCustomHighlight(int customHighlight) {
		this.customHighlight = customHighlight;
	}

	public void setCustomCoordinatesColors(int[] customCoordinatesColors) {
		this.customCoordinatesColors = customCoordinatesColors;
	}

	public void setCustomPiecesName(String customPiecesName) {
		this.customPiecesName = customPiecesName;
	}

// TODO: refactor! Use ObjectAnimator for better drawing and performance

	protected class MoveAnimator {
		long startTime;
		long stopTime;
		long now;
		int from1, to1, hide1 = -1;
		int from2, to2, hide2 = -1; // rename
		private int fromPiece;
		private Bitmap pieceBitmap;
		private Bitmap rookCastlingBitmap;
		private Bitmap capturedPieceBitmap;
		private int capturedPiecePosition;
		private boolean firstRun = true;
		private Move move;
		private boolean forward;
		private long animationTime;
		private boolean forceCompEngine;

		public MoveAnimator(Move move, boolean forward) {
			init(move, forward);
		}

		public void update(Move move, boolean forward) {
			init(move, forward);
		}

		private void init(Move move, boolean forward) {
			this.move = move;
			this.forward = forward;

			BoardFace boardFace = getBoardFace();
			int moveFromPosition = forward ? move.from : move.to;
			int fromColor = boardFace.getColor(moveFromPosition);
			fromPiece = boardFace.getPiece(moveFromPosition);

			if (fromPiece == ChessBoard.EMPTY) {
//				throw new IllegalArgumentException("fromPiece can't be EMPTY square here, check Move generation object." +
//						" move is " + move + " board is " + boardFace);
				fromPiece = ChessBoard.PAWN; // TODO fix real problem
			}
			if (fromColor == ChessBoard.EMPTY) {
//				throw new IllegalArgumentException("fromColor can't be EMPTY square here, check Move generation object. " +
//						"move is " + move + " board is " + boardFace);
				fromColor = 0; // TODO fix real problem
			}

			if (pieceBitmap != null) {
				pieceBitmap.recycle();
				pieceBitmap = null;
			}

			pieceBitmap = getPieceBitmap(fromColor, fromPiece);

			// todo: check game load

			int moveToPosition = forward ? move.to : move.from;
			if (boardFace.getPiece(moveToPosition) != ChessBoard.EMPTY) {
				int capturedColor = boardFace.getColor(moveToPosition);
				int capturedPiece = boardFace.getPiece(moveToPosition);

				if (capturedPieceBitmap != null) {
					capturedPieceBitmap.recycle();
					capturedPieceBitmap = null;
				}

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

					if (rookCastlingBitmap != null) {
						rookCastlingBitmap.recycle();
						rookCastlingBitmap = null;
					}

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

					if (rookCastlingBitmap != null) {
						rookCastlingBitmap.recycle();
						rookCastlingBitmap = null;
					}
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

			return (now < stopTime);
		}

		public long getAnimationTime() {
			return animationTime;
		}

		public boolean isSquareHidden(int square) {
			return square == hide1 || square == hide2;
		}

		private void initTimer() {
			int dx = ChessBoard.getFile(move.to) - ChessBoard.getFile(move.from);
			int dy = ChessBoard.getRank(move.to) - ChessBoard.getRank(move.from);
			double dist = Math.sqrt(dx * dx + dy * dy);
			double time;
			if (fastMode) {
				time = Math.sqrt(dist) * PIECE_ANIM_FAST_SPEED;
			} else {
				time = Math.sqrt(dist) * PIECE_ANIM_SPEED;
			}
			animationTime = (int) Math.round(time);

			startTime = System.currentTimeMillis();
			stopTime = startTime + animationTime;
		}

		public final void draw(Canvas canvas) {

			if (!isAnimationActive()) {
				return;
			}

			double animationTimeFactor = (now - startTime) / (double) (stopTime - startTime);
			drawAnimatedPiece(canvas, pieceBitmap, from1, to1, animationTimeFactor);
			drawAnimatedPiece(canvas, rookCastlingBitmap, from2, to2, animationTimeFactor);
			invalidate();
		}

		private void drawAnimatedPiece(Canvas canvas, Bitmap pieceBitmap, int from, int to, double animationTimeFactor) {
			if (pieceBitmap == null) {
				return;
			}

			final int xCrd1 = getXCoordinate(ChessBoard.getFile(from));
			final int yCrd1 = getYCoordinate(ChessBoard.getRank(from));
			final int xCrd2 = getXCoordinate(ChessBoard.getFile(to));
			final int yCrd2 = getYCoordinate(ChessBoard.getRank(to));
			final int xCrd = xCrd1 + (int) Math.round((xCrd2 - xCrd1) * animationTimeFactor);
			int yCrd = yCrd1 + (int) Math.round((yCrd2 - yCrd1) * animationTimeFactor);
			int top = 0;
			if (use3dPieces) {
				int bottom = (int) (yCrd + squareSize);
				top = yCrd - _3dPiecesOffset;

				top = adjustHeightFor3dPiece(pieceBitmap, bottom, top, fromPiece);

				rect.set(xCrd, top, (int) (xCrd + squareSize), bottom);
			} else {
				rect.set(xCrd, yCrd, (int) (xCrd + squareSize), (int) (yCrd + squareSize));
			}

			if (!pieceBitmap.isRecycled()) {
				canvas.drawBitmap(pieceBitmap, null, rect, piecesPaint);
			}
		}

		public boolean isForceCompEngine() {
			return forceCompEngine;
		}

		public void setForceCompEngine(boolean forceCompEngine) {
			this.forceCompEngine = forceCompEngine;
		}
	}

	private Bitmap getPieceBitmap(int fromColor, int fromPiece) {
		if (whitePiecesMap == null || blackPiecesMap == null) {
			return null;
		}

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

	protected void onSecondMoveAnimated() {
	}

	protected void resetMoving() {
		if (dragging) {
			dragging = false;
			pieceSelected = false;
			invalidate();
		}
	}
}
