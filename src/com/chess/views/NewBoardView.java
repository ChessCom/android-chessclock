package com.chess.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import com.chess.R;
import com.chess.core.AppConstants;
import com.chess.core.CoreActivityActionBar;
import com.chess.core.IntentConstants;
import com.chess.core.MainApp;
import com.chess.core.interfaces.BoardFace;
import com.chess.engine.*;

import java.util.Iterator;
import java.util.TreeSet;

public class NewBoardView extends ImageView {

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
	private CoreActivityActionBar activity;
	private MainApp mainApp;

//	private Board2 newBoardView;
	private BoardFace boardFace;
	public boolean hint;
	public boolean firstclick = true;
	public boolean compmoving;
	public boolean sel;
	public boolean track;
	public boolean drag;
	public boolean finished ;
	public boolean stopThinking ;
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



	private GamePanelView gamePanelView;
	private PieceItem pieceItem;

	public NewBoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		activity = (CoreActivityActionBar) context;
		mainApp = activity.getMainApp();

		green = new Paint();
		white = new Paint();
		black = new Paint();
		red = new Paint();
		// captured piece Item
		pieceItem = new PieceItem();

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

		width = context.getResources().getDisplayMetrics().widthPixels;
		height = context.getResources().getDisplayMetrics().heightPixels;

		image = context.getResources().getDrawable(R.drawable.chess_back);
		int opacity = context.getResources().getInteger(R.integer.fade_opacity);
//		blackColor ^= (opacity * 0xFF / 100) << 32;
		image.setBounds(0, 0, (int) width, (int) height);

		image.setDither(true);

	}

	public void AfterMove() {
		boardFace.setMovesCount(boardFace.getHply());
		activity.update(0);	//movelist
		if (MainApp.isLiveOrEchessGameMode(boardFace) && !boardFace.isAnalysis()) {
			boolean ssb;
			if (mainApp.isLiveChess()) {
				ssb = mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_SHOW_SUBMIT_MOVE_LIVE, false);
			} else {
				ssb = mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_SHOW_SUBMIT_MOVE, true);
			}
			if (ssb) {
				activity.findViewById(R.id.moveButtons).setVisibility(View.VISIBLE);
				boardFace.setSubmit(true);
			} else {
				activity.update(1);
			}
		}
		if (!MainApp.isTacticsGameMode(boardFace) && isResult())
			return;
		switch (boardFace.getMode()) {
			case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_WHITE: {	//w - human; b - comp
				ComputerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
				break;
			}
			case AppConstants.GAME_MODE_COMPUTER_VS_HUMAN_BLACK: {	//w - comp; b - human
				ComputerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
				break;
			}
			case AppConstants.GAME_MODE_TACTICS: {
				if (!boardFace.isAnalysis())
					activity.update(4);
				break;
			}
			default:
				break;
		}
	}

	boolean isResult() {
		//saving game
		if (MainApp.isComputerVsHumanGameMode(boardFace) || MainApp.isHumanVsHumanGameMode(boardFace)) {
			String saving = "" + boardFace.getMode();

			int i;
			for (i = 0; i < boardFace.getMovesCount(); i++) {
				Move m = boardFace.getHistDat()[i].m;
				saving += "|" + m.from + ":" + m.to + ":" + m.promote + ":" + m.bits;
			}

			mainApp.getSharedDataEditor().putString(AppConstants.SAVED_COMPUTER_GAME, saving);
			mainApp.getSharedDataEditor().commit();
		}

		TreeSet<Move> validMoves = boardFace.gen();

		Iterator<Move> i = validMoves.iterator();
		boolean found = false;
		while (i.hasNext()) {
			if (boardFace.makeMove((Move) i.next(), false)) {
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
				activity.update(0);
				if (boardFace.getSide() == Board.LIGHT)
					message = "0 - 1 Black mates";
				else
					message = "1 - 0 White mates";
			} else
				message = "0 - 0 Stalemate";
		} else if (boardFace.reps() == 3 && !mainApp.isLiveChess())
			message = "1/2 - 1/2 Draw by repetition";
		/*else if (newBoardView.fifty >= 100)
					message = "1/2 - 1/2 Draw by fifty move rule";*/
		if (message != null) {
			finished = true;
			mainApp.ShowMessage(message);

			mainApp.sendBroadcast(
					new Intent(IntentConstants.ACTION_SHOW_GAME_END_POPUP).putExtra(AppConstants.MESSAGE, "GAME OVER: " + message)
							.putExtra(AppConstants.FINISHABLE, false));

			return true;
		}
		if (boardFace.inCheck(boardFace.getSide())) {
			boardFace.getHistDat()[boardFace.getHply() - 1].notation += "+";
			activity.update(0);
			mainApp.ShowMessage("Check!");
		}
		return false;
	}

	public void ComputerMove(final int time) {
		if (MainApp.isComputerVsComputerGameMode(boardFace) && stopThinking) {
			stopThinking = false;
			return;
		}
		compmoving = true;
		activity.update(2);
		new Thread(new Runnable() {
			@Override
			public void run() {
				p_tmp = boardFace.getPiece().clone();
				c_tmp = boardFace.getColor().clone();
				Search2 searcher = new Search2(boardFace);
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
					activity.update(0);	//movelist
					activity.update(3);
					invalidate();
					if (isResult())
						return;
					if (MainApp.isComputerVsComputerGameMode(boardFace)
							|| (hint && !MainApp.isHumanVsHumanGameMode(boardFace))) {
						if (hint)
							hint = false;
						ComputerMove(time);
					}
				}
			};
		}).start();
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
//			setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec),
//					MeasureSpec.getSize(widthMeasureSpec + widthMeasureSpec / 5));
			setMeasuredDimension(resolveSize((int)width,widthMeasureSpec),
					resolveSize((int)width,heightMeasureSpec));
		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//			setMeasuredDimension(MeasureSpec.getSize(heightMeasureSpec + heightMeasureSpec / 5),
//					MeasureSpec.getSize(heightMeasureSpec));
			setMeasuredDimension(resolveSize((int)height,widthMeasureSpec),
					resolveSize((int)height,heightMeasureSpec));
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

		side = W / 4;
		if (H < W) side = H / 4;
		square = side / 2;

		int i, j;
		for (i = 0; i < 4; i++) {
			for (j = 0; j < 4; j++) {
				try {
					if (mainApp == null || mainApp.getBoardBitmap() == null) {
						throw new Exception();
					}
					canvas.drawBitmap(mainApp.getBoardBitmap(), null, new Rect(i * side, j * side, i * side + side, j * side + side), null);
				} catch (Exception e) {
					e.printStackTrace();
//					Log.d("BoardView", "mainApp " + mainApp);
//					Log.d("BoardView", "mainApp.newBoardView " + mainApp.getBoardBitmap());
					return;
				}
			}
		}

//		image.draw(canvas);

		if (!compmoving) {
			for (i = 0; i < 64; i++) {
				if (drag && i == from) continue;
				int c = boardFace.getColor()[i];
				int p = boardFace.getPiece()[i];
				int x = Board.COL(i, boardFace.isReside());
				int y = Board.ROW(i, boardFace.isReside());
				if (c != 6 && p != 6) {
					canvas.drawBitmap(mainApp.getPiecesBitmap()[c][p], null, new Rect(x * square, y * square, x * square + square, y * square + square), null);
				}
			}
		} else {
			for (i = 0; i < 64; i++) {
				if (drag && i == from) continue;
				int c = c_tmp[i];
				int p = p_tmp[i];
				int x = Board.COL(i, boardFace.isReside());
				int y = Board.ROW(i, boardFace.isReside());
				if (c != 6 && p != 6) {
					canvas.drawBitmap(mainApp.getPiecesBitmap()[c][p], null, new Rect(x * square, y * square, x * square + square, y * square + square), null);
				}
			}
		}

		if (mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_BOARD_COORDINATES, true)) {
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

		if (mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_BOARD_SQUARE_HIGHLIGHT, true) && boardFace.getHply() > 0 && !compmoving) {
			Move m = boardFace.getHistDat()[boardFace.getHply() - 1].m;
			int x1 = Board.COL(m.from, boardFace.isReside());
			int y1 = Board.ROW(m.from, boardFace.isReside());
			canvas.drawRect(x1 * square, y1 * square, x1 * square + square, y1 * square + square, red);
			int x2 = Board.COL(m.to, boardFace.isReside());
			int y2 = Board.ROW(m.to, boardFace.isReside());
			canvas.drawRect(x2 * square, y2 * square, x2 * square + square, y2 * square + square, red);
		}

		if (sel) {
			int x = Board.COL(from, boardFace.isReside());
			int y = Board.ROW(from, boardFace.isReside());
			canvas.drawRect(x * square, y * square, x * square + square, y * square + square, white);
		}
		if (drag) {
			int c = boardFace.getColor()[from];
			int p = boardFace.getPiece()[from];
			int x = dragX - square / 2;
			int y = dragY - square / 2;
			int col = (int) (dragX - dragX % square) / square;
			int row = (int) ((dragY + square) - (dragY + square) % square) / square;
			if (c != 6 && p != 6) {
				canvas.drawBitmap(mainApp.getPiecesBitmap()[c][p], null,
						new Rect(x - square / 2, y - square / 2, x + square + square / 2, y + square + square / 2), null);
				canvas.drawRect(col * square - square / 2, row * square - square / 2,
						col * square + square + square / 2, row * square + square + square / 2, white);
			}
		}
		if (track) {
			int x = (int) (trackX - trackX % square) / square;
			int y = (int) (trackY - trackY % square) / square;
			canvas.drawRect(x * square, y * square, x * square + square, y * square + square, green);
		}

		//captured piecec
		if (!compmoving && !MainApp.isTacticsGameMode(boardFace))
			if (W < H) {
//				int h = H - W - 16;
//				int side = W / 15;
//				if (side > h / 2)
//					side = h / 2;
//				if (side < 10)
//					return;
//
//				//int offset = 1;
//				int offset = side / 5;

				int w_pawns = 8, w_knights = 2, w_bishops = 2, w_rooks = 2, w_queen = 1;
				int b_pawns = 8, b_knights = 2, b_bishops = 2, b_rooks = 2, b_queen = 1;
				for (i = 0; i < 64; i++) {
					int piece = boardFace.getPiece()[i];
					if (boardFace.getColor()[i] == Board.LIGHT) {
						if (piece == 0)
							w_pawns--;
						if (piece == 1)
							w_knights--;
						if (piece == 2)
							w_bishops--;
						if (piece == 3)
							w_rooks--;
						if (piece == 4)
							w_queen = 0;
					} else {
						if (piece == 0)
							b_pawns--;
						if (piece == 1)
							b_knights--;
						if (piece == 2)
							b_bishops--;
						if (piece == 3)
							b_rooks--;
						if (piece == 4)
							b_queen = 0;
					}
				}
				//white

				for (i = 0; i < w_pawns; i++){
					pieceItem.setCode(PieceItem.P);
					pieceItem.setWhite(true);
				}
				for (i = 0; i < w_knights; i++){
					pieceItem.setCode(PieceItem.N);
					pieceItem.setWhite(true);
				}
				for (i = 0; i < w_bishops; i++){
					pieceItem.setCode(PieceItem.B);
					pieceItem.setWhite(true);
				}
				for (i = 0; i < w_rooks; i++){
					pieceItem.setCode(PieceItem.R);
					pieceItem.setWhite(true);
				}
				if (w_queen == 1){
					pieceItem.setCode(PieceItem.Q);
					pieceItem.setWhite(true);
				}

				// back
				for (i = 0; i < b_pawns; i++){
					pieceItem.setCode(PieceItem.P);
					pieceItem.setWhite(false);
				}
				for (i = 0; i < b_knights; i++){
					pieceItem.setCode(PieceItem.N);
					pieceItem.setWhite(false);
				}
				for (i = 0; i < b_bishops; i++){
					pieceItem.setCode(PieceItem.B);
					pieceItem.setWhite(false);
				}
				for (i = 0; i < b_rooks; i++){
					pieceItem.setCode(PieceItem.R);
					pieceItem.setWhite(false);
				}
				if (w_queen == 1){
					pieceItem.setCode(PieceItem.Q);
					pieceItem.setWhite(false);
				}
			} else {

				int w_pawns = 8, w_knights = 2, w_bishops = 2, w_rooks = 2, w_queen = 1;
				int b_pawns = 8, b_knights = 2, b_bishops = 2, b_rooks = 2, b_queen = 1;
				for (i = 0; i < 64; i++) {
					int piece = boardFace.getPiece()[i];
					if (boardFace.getColor()[i] == Board.LIGHT) {
						if (piece == 0)
							w_pawns--;
						if (piece == 1)
							w_knights--;
						if (piece == 2)
							w_bishops--;
						if (piece == 3)
							w_rooks--;
						if (piece == 4)
							w_queen = 0;
					} else {
						if (piece == 0)
							b_pawns--;
						if (piece == 1)
							b_knights--;
						if (piece == 2)
							b_bishops--;
						if (piece == 3)
							b_rooks--;
						if (piece == 4)
							b_queen = 0;
					}
				}

				// white
				for (i = 0; i < w_pawns; i++){
					pieceItem.setCode(PieceItem.P);
					pieceItem.setWhite(true);
				}
				for (i = 0; i < w_knights; i++){
					pieceItem.setCode(PieceItem.N);
					pieceItem.setWhite(true);
				}
				for (i = 0; i < w_bishops; i++){
					pieceItem.setCode(PieceItem.B);
					pieceItem.setWhite(true);
				}
				for (i = 0; i < w_rooks; i++){
					pieceItem.setCode(PieceItem.R);
					pieceItem.setWhite(true);
				}
				if (w_queen == 1){
					pieceItem.setCode(PieceItem.Q);
					pieceItem.setWhite(true);
				}

				// back
				for (i = 0; i < b_pawns; i++){
					pieceItem.setCode(PieceItem.P);
					pieceItem.setWhite(false);
				}
				for (i = 0; i < b_knights; i++){
					pieceItem.setCode(PieceItem.N);
					pieceItem.setWhite(false);
				}
				for (i = 0; i < b_bishops; i++){
					pieceItem.setCode(PieceItem.B);
					pieceItem.setWhite(false);
				}
				for (i = 0; i < b_rooks; i++){
					pieceItem.setCode(PieceItem.R);
					pieceItem.setWhite(false);
				}
				if (w_queen == 1){
					pieceItem.setCode(PieceItem.Q);
					pieceItem.setWhite(false);
				}
			}
	}

	@Override
	public boolean onTrackballEvent(MotionEvent event) {
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
			if (trackX < 0) trackX = 0;
			if (trackY < 0) trackY = 0;
			if (trackX > 7 * square) trackX = 7 * square;
			if (trackY > 7 * square) trackY = 7 * square;
			invalidate();
		} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
			int col = (int) (trackX - trackX % square) / square;
			int row = (int) (trackY - trackY % square) / square;
			if (firstclick) {
				from = Board.POS(col, row, boardFace.isReside());
				if (boardFace.getPiece()[from] != 6 && boardFace.getSide() == boardFace.getColor()[from]) {
					sel = true;
					firstclick = false;
					invalidate();
				}
			} else {
				to = Board.POS(col, row, boardFace.isReside());
				sel = false;
				firstclick = true;
				boolean found = false;
				TreeSet<Move> moves = boardFace.gen();
				Iterator<Move> i = moves.iterator();

				Move m = null;
				while (i.hasNext()) {
					m = i.next();
					if (m.from == from && m.to == to) {
						found = true;
						break;
					}
				}
				if ((((to < 8) && (boardFace.getSide() == Board.LIGHT)) ||
						((to > 55) && (boardFace.getSide() == Board.DARK))) &&
						(boardFace.getPiece()[from] == Board.PAWN) && found) {
					final int c = col, r = row;
					new AlertDialog.Builder(activity)
							.setTitle("Choose a piece ")
							.setItems(new String[]{"Queen", "Rook", "Bishop", "Knight", "Cancel"}, new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									if (which == 4) {
										invalidate();
										return;
									}
									promote(4 - which, c, r);
								}
							}).setCancelable(false)
							.create().show();
					return true;
				}
				if (found && m != null && boardFace.makeMove(m)) {
					invalidate();
					AfterMove();
				} else if (boardFace.getPiece()[to] != 6 && boardFace.getSide() == boardFace.getColor()[to]) {
					sel = true;
					firstclick = false;
					from = Board.POS(col, row, boardFace.isReside());
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

		if (square == 0) {
			return super.onTouchEvent(event);
		}

		track = false;
		if (!boardFace.isAnalysis()) {
			if (compmoving || MainApp.isFinishedEchessGameMode(boardFace) || finished || boardFace.isSubmit() ||
					(MainApp.isLiveOrEchessGameMode(boardFace) && boardFace.getHply() < boardFace.getMovesCount()))
				return true;
			if (MainApp.isLiveOrEchessGameMode(boardFace) && mainApp.getCurrentGame() != null) {
				if (mainApp.getCurrentGame().values.get(AppConstants.WHITE_USERNAME).toLowerCase().equals(mainApp.getSharedData().getString(AppConstants.USERNAME, "")) && boardFace.getMovesCount() % 2 != 0)
					return true;
				if (mainApp.getCurrentGame().values.get(AppConstants.BLACK_USERNAME).toLowerCase().equals(mainApp.getSharedData().getString(AppConstants.USERNAME, "")) && boardFace.getMovesCount() % 2 == 0)
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

		int col = 0, row = 0;
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				col = (int) (event.getX() - event.getX() % square) / square;
				row = (int) (event.getY() - event.getY() % square) / square;
				if (col > 7 || col < 0 || row > 7 || row < 0) {
					invalidate();
					return false;
				}
				if (firstclick) {
					from = Board.POS(col, row, boardFace.isReside());
					if (boardFace.getPiece()[from] != 6 && boardFace.getSide() == boardFace.getColor()[from]) {
						sel = true;
						firstclick = false;
						invalidate();
					}
				} else {
					int f = Board.POS(col, row, boardFace.isReside());
					if (boardFace.getPiece()[f] != 6 && boardFace.getSide() == boardFace.getColor()[f]) {
						from = f;
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
				col = (int) (dragX - dragX % square) / square;
				row = (int) (dragY - dragY % square) / square;
				if (col > 7 || col < 0 || row > 7 || row < 0) {
					invalidate();
					return false;
				}
				if (!drag && !sel)
					from = Board.POS(col, row, boardFace.isReside());
				if (!firstclick && boardFace.getSide() == boardFace.getColor()[from]) {
					drag = true;
					to = Board.POS(col, row, boardFace.isReside());
					invalidate();
				}
				return true;
			}
			case MotionEvent.ACTION_UP: {
				col = (int) (event.getX() - event.getX() % square) / square;
				row = (int) (event.getY() - event.getY() % square) / square;
				drag = false;
				if (col > 7 || col < 0 || row > 7 || row < 0) {
					invalidate();
					return false;
				}
				if (firstclick) {
					from = Board.POS(col, row, boardFace.isReside());
					if (boardFace.getPiece()[from] != 6 && boardFace.getSide() == boardFace.getColor()[from]) {
						sel = true;
						firstclick = false;
						invalidate();
					}
				} else {
					to = Board.POS(col, row, boardFace.isReside());
					sel = false;
					firstclick = true;
					boolean found = false;
					TreeSet<Move> moves = boardFace.gen();
					Iterator<Move> i = moves.iterator();

					Move m = null;
					while (i.hasNext()) {
						m = i.next();
						if (m.from == from && m.to == to) {
							found = true;
							break;
						}
					}
					if ((((to < 8) && (boardFace.getSide() == Board.LIGHT)) ||
							((to > 55) && (boardFace.getSide() == Board.DARK))) &&
							(boardFace.getPiece()[from] == Board.PAWN) && found) {
						final int c = col, r = row;
						new AlertDialog.Builder(activity)
								.setTitle("Choose a piece ")
								.setItems(new String[]{"Queen", "Rook", "Bishop", "Knight", "Cancel"},
										new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog, int which) {
										if (which == 4) {
											invalidate();
											return;
										}
										promote(4 - which, c, r);
									}
								}).setCancelable(false)
								.create().show();
						return true;
					}
					if (found && m != null && boardFace.makeMove(m)) {
						invalidate();
						AfterMove();
					} else if (boardFace.getPiece()[to] != 6 && boardFace.getSide() == boardFace.getColor()[to]) {
						sel = true;
						firstclick = false;
						from = Board.POS(col, row, boardFace.isReside());
						invalidate();
					} else {
						invalidate();
					}

					finishMove();
				}
				return true;
			}
			default:
				break;
		}

		return super.onTouchEvent(event);
	}

	private void finishMove(){
		gamePanelView.capturePiece(pieceItem);
		pieceItem.setCaptured(false);
	}

	private void promote(int promote, int col, int row) {
		boolean found = false;
		TreeSet<Move> moves = boardFace.gen();
		Iterator<Move> i = moves.iterator();

		Move m = null;
		while (i.hasNext()) {
			m = i.next();
			if (m.from == from && m.to == to && m.promote == promote) {
				found = true;
				break;
			}
		}
		if (found && m != null && boardFace.makeMove(m)) {
			invalidate();
			AfterMove();
		} else if (boardFace.getPiece()[to] != 6 && boardFace.getSide() == boardFace.getColor()[to]) {
			sel = true;
			firstclick = false;
			from = Board.POS(col, row, boardFace.isReside());
			invalidate();
		} else {
			invalidate();
		}
	}

	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		viewWidth = (xNew == 0 ? viewWidth : xNew);
		viewHeight = (yNew == 0 ? viewHeight : yNew);
	}

	public BoardFace getBoardFace() {
		return boardFace;
	}

	public void setBoardFace(Board2 boardFace) {
		this.boardFace = boardFace;
	}


	public GamePanelView getGamePanelView() {
		return gamePanelView;
	}

	public void setGamePanelView(GamePanelView gamePanelView) {
		this.gamePanelView = gamePanelView;
	}
}
