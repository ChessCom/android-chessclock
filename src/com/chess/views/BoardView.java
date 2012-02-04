package com.chess.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import com.chess.R;
import com.chess.core.AppConstants;
import com.chess.core.CoreActivity;
import com.chess.core.IntentConstants;
import com.chess.core.MainApp;
import com.chess.engine.Board;
import com.chess.engine.Move;
import com.chess.engine.Search;

import java.util.Iterator;
import java.util.TreeSet;

public class BoardView extends ImageView {

	public int W, H,
			side, square,
			from = -1, to = -1,
			dragX = 0, dragY = 0,
			trackX = 0, trackY = 0;
	private CoreActivity activity;
	private MainApp mainApp;

	private Board board;
	public boolean hint = false,
			firstclick = true,
			compmoving = false,
			sel = false, track = false,
			drag = false,
			finished = false;
	public boolean stopThinking = false;
	private int[] p_tmp, c_tmp;

	private Paint white, black, red, green;

	private String[] signs = {"a", "b", "c", "d", "e", "f", "g", "h"};
	private String[] nums = {"1", "2", "3", "4", "5", "6", "7", "8"};

	private int viewWidth = 0;
	private int viewHeight = 0;

	public BoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		activity = (CoreActivity) context;
		mainApp = activity.getMainApp();

		green = new Paint();
		white = new Paint();
		black = new Paint();
		red = new Paint();

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

	}

	public void AfterMove() {
		board.movesCount = board.hply;
		activity.Update(0);	//movelist
		if (board.mode == 4 && !board.analysis) {
			boolean ssb;
			if (mainApp.isLiveChess()) {
				ssb = mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_SHOW_SUBMIT_MOVE_LIVE, false);
			} else {
				ssb = mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_SHOW_SUBMIT_MOVE, true);
			}
			if (ssb) {
				activity.findViewById(R.id.moveButtons).setVisibility(View.VISIBLE);
				board.submit = true;
			} else {
				activity.Update(1);
			}
		}
		if (board.mode < 6 && isResult())
			return;
		switch (board.mode) {
			case 0: {	//w - human; b - comp
				ComputerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
				break;
			}
			case 1: {	//w - comp; b - human
				ComputerMove(mainApp.strength[mainApp.getSharedData().getInt(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_COMPUTER_STRENGTH, 0)]);
				break;
			}
			case 6: {
				if (!board.analysis)
					activity.Update(4);
				break;
			}
			default:
				break;
		}
	}

	boolean isResult() {
		//saving game
		if (board.mode < 3) {
			String saving = "" + board.mode;

			int i;
			for (i = 0; i < board.movesCount; i++) {
				Move m = board.histDat[i].m;
				saving += "|" + m.from + ":" + m.to + ":" + m.promote + ":" + m.bits;
			}

			mainApp.getSharedDataEditor().putString(AppConstants.SAVED_COMPUTER_GAME, saving);
			mainApp.getSharedDataEditor().commit();
		}

		TreeSet<Move> validMoves = board.gen();

		Iterator<Move> i = validMoves.iterator();
		boolean found = false;
		while (i.hasNext()) {
			if (board.makeMove((Move) i.next(), false)) {
				board.takeBack();
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
			if (board.inCheck(board.side)) {
				board.histDat[board.hply - 1].notation += "#";
				activity.Update(0);
				if (board.side == Board.LIGHT)
					message = "0 - 1 Black mates";
				else
					message = "1 - 0 White mates";
			} else
				message = "0 - 0 Stalemate";
		} else if (board.reps() == 3 && !mainApp.isLiveChess())
			message = "1/2 - 1/2 Draw by repetition";
		/*else if (board.fifty >= 100)
					message = "1/2 - 1/2 Draw by fifty move rule";*/
		if (message != null) {
			finished = true;
			mainApp.ShowMessage(message);

			mainApp.sendBroadcast(
					new Intent(IntentConstants.ACTION_SHOW_GAME_END_POPUP).putExtra(AppConstants.MESSAGE, "GAME OVER: " + message)
							.putExtra(AppConstants.FINISHABLE, false));

			return true;
		}
		if (board.inCheck(board.side)) {
			board.histDat[board.hply - 1].notation += "+";
			activity.Update(0);
			mainApp.ShowMessage("Check!");
		}
		return false;
	}

	public void ComputerMove(final int time) {
		if (board.mode == 3 && stopThinking) {
			stopThinking = false;
			return;
		}
		compmoving = true;
		activity.Update(2);
		new Thread(new Runnable() {
			@Override
			public void run() {
				p_tmp = board.piece.clone();
				c_tmp = board.color.clone();
				Search searcher = new Search(board);
				searcher.think(0, time, 32);
				Move best = searcher.getBest();
				board.makeMove(best);
				compmoving = false;
				board.movesCount = board.hply;
				update.sendEmptyMessage(0);
			}

			private Handler update = new Handler() {
				@Override
				public void dispatchMessage(Message msg) {
					super.dispatchMessage(msg);
					activity.Update(0);	//movelist
					activity.Update(3);
					invalidate();
					if (isResult())
						return;
					if (board.mode == 3 || (hint && board.mode != 2)) {
						if (hint) hint = false;
						ComputerMove(time);
					}
				}
			};
		}).start();
	}

	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		/*if (mainApp.isLiveChess())
			{*/
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			this.setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(widthMeasureSpec + widthMeasureSpec / 5));
		} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
			this.setMeasuredDimension(MeasureSpec.getSize(heightMeasureSpec + heightMeasureSpec / 5), MeasureSpec.getSize(heightMeasureSpec));
		}
		//}
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
					Log.d("BoardView", "mainApp " + mainApp);
					Log.d("BoardView", "mainApp.board " + mainApp.getBoardBitmap());
					return;
				}
			}
		}

		if (!compmoving) {
			for (i = 0; i < 64; i++) {
				if (drag && i == from) continue;
				int c = board.color[i];
				int p = board.piece[i];
				int x = Board.COL(i, board.reside);
				int y = Board.ROW(i, board.reside);
				if (c != 6 && p != 6) {
					canvas.drawBitmap(mainApp.getPiecesBitmap()[c][p], null, new Rect(x * square, y * square, x * square + square, y * square + square), null);
				}
			}
		} else {
			for (i = 0; i < 64; i++) {
				if (drag && i == from) continue;
				int c = c_tmp[i];
				int p = p_tmp[i];
				int x = Board.COL(i, board.reside);
				int y = Board.ROW(i, board.reside);
				if (c != 6 && p != 6) {
					canvas.drawBitmap(mainApp.getPiecesBitmap()[c][p], null, new Rect(x * square, y * square, x * square + square, y * square + square), null);
				}
			}
		}

		if (mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_BOARD_COORDINATES, true)) {
			for (i = 0; i < 8; i++) {
				if (board.reside) {
					canvas.drawText(nums[i], 2, i * square + 12, black);
					canvas.drawText(signs[7 - i], i * square + 2, 8 * square - 2, black);
				} else {
					canvas.drawText(nums[7 - i], 2, i * square + 12, black);
					canvas.drawText(signs[i], i * square + 2, 8 * square - 2, black);
				}
			}
		}

		if (mainApp.getSharedData().getBoolean(mainApp.getSharedData().getString(AppConstants.USERNAME, "") + AppConstants.PREF_BOARD_SQUARE_HIGHLIGHT, true) && board.hply > 0 && !compmoving) {
			Move m = board.histDat[board.hply - 1].m;
			int x1 = Board.COL(m.from, board.reside);
			int y1 = Board.ROW(m.from, board.reside);
			canvas.drawRect(x1 * square, y1 * square, x1 * square + square, y1 * square + square, red);
			int x2 = Board.COL(m.to, board.reside);
			int y2 = Board.ROW(m.to, board.reside);
			canvas.drawRect(x2 * square, y2 * square, x2 * square + square, y2 * square + square, red);
		}

		if (sel) {
			int x = Board.COL(from, board.reside);
			int y = Board.ROW(from, board.reside);
			canvas.drawRect(x * square, y * square, x * square + square, y * square + square, white);
		}
		if (drag) {
			int c = board.color[from];
			int p = board.piece[from];
			int x = dragX - square / 2;
			int y = dragY - square / 2;
			int col = (int) (dragX - dragX % square) / square;
			int row = (int) ((dragY + square) - (dragY + square) % square) / square;
			if (c != 6 && p != 6) {
				canvas.drawBitmap(mainApp.getPiecesBitmap()[c][p], null, new Rect(x - square / 2, y - square / 2, x + square + square / 2, y + square + square / 2), null);
				canvas.drawRect(col * square - square / 2, row * square - square / 2, col * square + square + square / 2, row * square + square + square / 2, white);
			}
		}
		if (track) {
			int x = (int) (trackX - trackX % square) / square;
			int y = (int) (trackY - trackY % square) / square;
			canvas.drawRect(x * square, y * square, x * square + square, y * square + square, green);
		}

		//captured piecec
		if (!compmoving && board.mode != 6)
			if (W < H) {
				int h = H - W - 16;
				int side = W / 15;
				if (side > h / 2)
					side = h / 2;
				if (side < 10)
					return;

				//int offset = 1;
				int offset = side / 5;

				int w_pawns = 8, w_knights = 2, w_bishops = 2, w_rooks = 2, w_queen = 1;
				int b_pawns = 8, b_knights = 2, b_bishops = 2, b_rooks = 2, b_queen = 1;
				for (i = 0; i < 64; i++) {
					int piece = board.piece[i];
					if (board.color[i] == Board.LIGHT) {
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
/*			/*for(i=0;i<w_pawns;i++)
				canvas.drawBitmap(mainApp.capturedWP, null, new Rect(i*side-offset, W, i*side+side+offset, W+side+2*offset), null);
			for(i=0;i<w_knights;i++)
				canvas.drawBitmap(mainApp.capturedWN, null, new Rect((i)*side+8*side-offset, W, (i)*side+side+8*side+offset, W+side+2*offset), null);
			for(i=0;i<w_bishops;i++)
				canvas.drawBitmap(mainApp.capturedWB, null, new Rect((i)*side+10*side-offset, W, (i)*side+side+10*side+offset, W+side+2*offset), null);
			for(i=0;i<w_rooks;i++)
				canvas.drawBitmap(mainApp.capturedWR, null, new Rect((i)*side+12*side-offset, W, (i)*side+side+12*side+offset, W+side+2*offset), null);
			if(w_queen == 1)
				canvas.drawBitmap(mainApp.capturedWQ, null, new Rect(14*side-offset, W-offset, 15*side+offset, W+side+2*offset), null);
			//black
			for(i=0;i<b_pawns;i++)
				canvas.drawBitmap(mainApp.capturedBP, null, new Rect(i*side-offset, W+side, i*side+side+offset, W+2*side+2*offset), null);
			for(i=0;i<b_knights;i++)
				canvas.drawBitmap(mainApp.capturedBN, null, new Rect((i)*side+8*side-offset, W+side, (i)*side+side+8*side+offset, W+2*side+2*offset), null);
			for(i=0;i<b_bishops;i++)
				canvas.drawBitmap(mainApp.capturedBB, null, new Rect((i)*side+10*side-offset, W+side, (i)*side+side+10*side+offset, W+2*side+2*offset), null);
			for(i=0;i<b_rooks;i++)
				canvas.drawBitmap(mainApp.capturedBR, null, new Rect((i)*side+12*side-offset, W+side, (i)*side+side+12*side+offset, W+2*side+2*offset), null);
			if(b_queen == 1)
				canvas.drawBitmap(mainApp.capturedBQ, null, new Rect(14*side-offset, W+side, 15*side+offset, W+2*side+2*offset), null);*/
				for (i = 0; i < w_pawns; i++)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[0][0], null, new Rect(i * side - offset, W, i * side + side + offset, W + side + 2 * offset), null);
				for (i = 0; i < w_knights; i++)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[0][1], null, new Rect((i) * side + 8 * side - offset, W, (i) * side + side + 8 * side + offset, W + side + 2 * offset), null);
				for (i = 0; i < w_bishops; i++)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[0][2], null, new Rect((i) * side + 10 * side - offset, W, (i) * side + side + 10 * side + offset, W + side + 2 * offset), null);
				for (i = 0; i < w_rooks; i++)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[0][3], null, new Rect((i) * side + 12 * side - offset, W, (i) * side + side + 12 * side + offset, W + side + 2 * offset), null);
				if (w_queen == 1)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[0][4], null, new Rect(14 * side - offset, W - offset, 15 * side + offset, W + side + 2 * offset), null);
				//black
				for (i = 0; i < b_pawns; i++)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[1][0], null, new Rect(i * side - offset, W + side, i * side + side + offset, W + 2 * side + 2 * offset), null);
				for (i = 0; i < b_knights; i++)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[1][1], null, new Rect((i) * side + 8 * side - offset, W + side, (i) * side + side + 8 * side + offset, W + 2 * side + 2 * offset), null);
				for (i = 0; i < b_bishops; i++)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[1][2], null, new Rect((i) * side + 10 * side - offset, W + side, (i) * side + side + 10 * side + offset, W + 2 * side + 2 * offset), null);
				for (i = 0; i < b_rooks; i++)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[1][3], null, new Rect((i) * side + 12 * side - offset, W + side, (i) * side + side + 12 * side + offset, W + 2 * side + 2 * offset), null);
				if (b_queen == 1)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[1][4], null, new Rect(14 * side - offset, W + side, 15 * side + offset, W + 2 * side + 2 * offset), null);

			} else {
				int h = W - H;
				int side = H / 15;
				if (side > h / 2)
					side = h / 2;
				if (side < 10)
					return;
				//int offset = 1;
				int offset = side / 8;

				int w_pawns = 8, w_knights = 2, w_bishops = 2, w_rooks = 2, w_queen = 1;
				int b_pawns = 8, b_knights = 2, b_bishops = 2, b_rooks = 2, b_queen = 1;
				for (i = 0; i < 64; i++) {
					int piece = board.piece[i];
					if (board.color[i] == Board.LIGHT) {
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
				/*for(i=0;i<w_pawns;i++)
								canvas.drawBitmap(mainApp.capturedWP, null, new Rect(H, i*side-offset, H+side+2*offset, i*side+side+offset), null);
							for(i=0;i<w_knights;i++)
								canvas.drawBitmap(mainApp.capturedWN, null, new Rect(H, (i)*side+8*side-offset, H+side+2*offset, (i)*side+side+8*side+offset), null);
							for(i=0;i<w_bishops;i++)
								canvas.drawBitmap(mainApp.capturedWB, null, new Rect(H, (i)*side+10*side-offset, H+side+2*offset, (i)*side+side+10*side+offset), null);
							for(i=0;i<w_rooks;i++)
								canvas.drawBitmap(mainApp.capturedWR, null, new Rect(H, (i)*side+12*side-offset, H+side+2*offset, (i)*side+side+12*side+offset), null);
							if(w_queen == 1)
								canvas.drawBitmap(mainApp.capturedWQ, null, new Rect(H-offset, 14*side-offset, H+side+2*offset, 15*side+offset), null);
							//black
							for(i=0;i<b_pawns;i++)
								canvas.drawBitmap(mainApp.capturedBP, null, new Rect(H+side, i*side-offset, H+2*side+2*offset, i*side+side+offset), null);
							for(i=0;i<b_knights;i++)
								canvas.drawBitmap(mainApp.capturedBN, null, new Rect(H+side, (i)*side+8*side-offset, H+2*side+2*offset, (i)*side+side+8*side+offset), null);
							for(i=0;i<b_bishops;i++)
								canvas.drawBitmap(mainApp.capturedBB, null, new Rect(H+side, (i)*side+10*side-offset, H+2*side+2*offset, (i)*side+side+10*side+offset), null);
							for(i=0;i<b_rooks;i++)
								canvas.drawBitmap(mainApp.capturedBR, null, new Rect(H+side, (i)*side+12*side-offset, H+2*side+2*offset, (i)*side+side+12*side+offset), null);
							if(b_queen == 1)
								canvas.drawBitmap(mainApp.capturedBQ, null, new Rect(H+side, 14*side-offset, H+2*side+2*offset, 15*side+offset), null);*/
				for (i = 0; i < w_pawns; i++)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[0][0], null, new Rect(H, i * side - offset, H + side + 2 * offset, i * side + side + offset), null);
				for (i = 0; i < w_knights; i++)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[0][1], null, new Rect(H, (i) * side + 8 * side - offset, H + side + 2 * offset, (i) * side + side + 8 * side + offset), null);
				for (i = 0; i < w_bishops; i++)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[0][2], null, new Rect(H, (i) * side + 10 * side - offset, H + side + 2 * offset, (i) * side + side + 10 * side + offset), null);
				for (i = 0; i < w_rooks; i++)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[0][3], null, new Rect(H, (i) * side + 12 * side - offset, H + side + 2 * offset, (i) * side + side + 12 * side + offset), null);
				if (w_queen == 1)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[0][4], null, new Rect(H - offset, 14 * side - offset, H + side + 2 * offset, 15 * side + offset), null);
				//black
				for (i = 0; i < b_pawns; i++)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[1][0], null, new Rect(H + side, i * side - offset, H + 2 * side + 2 * offset, i * side + side + offset), null);
				for (i = 0; i < b_knights; i++)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[1][1], null, new Rect(H + side, (i) * side + 8 * side - offset, H + 2 * side + 2 * offset, (i) * side + side + 8 * side + offset), null);
				for (i = 0; i < b_bishops; i++)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[1][2], null, new Rect(H + side, (i) * side + 10 * side - offset, H + 2 * side + 2 * offset, (i) * side + side + 10 * side + offset), null);
				for (i = 0; i < b_rooks; i++)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[1][3], null, new Rect(H + side, (i) * side + 12 * side - offset, H + 2 * side + 2 * offset, (i) * side + side + 12 * side + offset), null);
				if (b_queen == 1)
					canvas.drawBitmap(mainApp.getPiecesBitmap()[1][4], null, new Rect(H + side, 14 * side - offset, H + 2 * side + 2 * offset, 15 * side + offset), null);
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
				from = Board.POS(col, row, board.reside);
				if (board.piece[from] != 6 && board.side == board.color[from]) {
					sel = true;
					firstclick = false;
					invalidate();
				}
			} else {
				to = Board.POS(col, row, board.reside);
				sel = false;
				firstclick = true;
				boolean found = false;
				TreeSet<Move> moves = board.gen();
				Iterator<Move> i = moves.iterator();

				Move m = null;
				while (i.hasNext()) {
					m = i.next();
					if (m.from == from && m.to == to) {
						found = true;
						break;
					}
				}
				if ((((to < 8) && (board.side == Board.LIGHT)) ||
						((to > 55) && (board.side == Board.DARK))) &&
						(board.piece[from] == Board.PAWN) && found) {
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
				if (found && m != null && board.makeMove(m)) {
					invalidate();
					AfterMove();
				} else if (board.piece[to] != 6 && board.side == board.color[to]) {
					sel = true;
					firstclick = false;
					from = Board.POS(col, row, board.reside);
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
		if (!board.analysis) {
			if (compmoving || board.mode == 5 || finished || board.submit ||
					(board.mode == 4 && board.hply < board.movesCount))
				return true;
			if (board.mode == 4 && mainApp.getCurrentGame() != null) {
				if (mainApp.getCurrentGame().values.get(AppConstants.WHITE_USERNAME).toLowerCase().equals(mainApp.getSharedData().getString(AppConstants.USERNAME, "")) && board.movesCount % 2 != 0)
					return true;
				if (mainApp.getCurrentGame().values.get(AppConstants.BLACK_USERNAME).toLowerCase().equals(mainApp.getSharedData().getString(AppConstants.USERNAME, "")) && board.movesCount % 2 == 0)
					return true;
			}
			if ((board.mode == 0 && board.hply % 2 != 0) || (board.mode == 1 && board.hply % 2 == 0)) {
				return true;
			}
			if (board.mode == 6 && board.hply % 2 == 0) {
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
					return true;
				}
				if (firstclick) {
					from = Board.POS(col, row, board.reside);
					if (board.piece[from] != 6 && board.side == board.color[from]) {
						sel = true;
						firstclick = false;
						invalidate();
					}
				} else {
					int f = Board.POS(col, row, board.reside);
					if (board.piece[f] != 6 && board.side == board.color[f]) {
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
					return true;
				}
				if (!drag && !sel)
					from = Board.POS(col, row, board.reside);
				if (!firstclick && board.side == board.color[from]) {
					drag = true;
					to = Board.POS(col, row, board.reside);
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
					return true;
				}
				if (firstclick) {
					from = Board.POS(col, row, board.reside);
					if (board.piece[from] != 6 && board.side == board.color[from]) {
						sel = true;
						firstclick = false;
						invalidate();
					}
				} else {
					to = Board.POS(col, row, board.reside);
					sel = false;
					firstclick = true;
					boolean found = false;
					TreeSet<Move> moves = board.gen();
					Iterator<Move> i = moves.iterator();

					Move m = null;
					while (i.hasNext()) {
						m = i.next();
						if (m.from == from && m.to == to) {
							found = true;
							break;
						}
					}
					if ((((to < 8) && (board.side == Board.LIGHT)) ||
							((to > 55) && (board.side == Board.DARK))) &&
							(board.piece[from] == Board.PAWN) && found) {
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
					if (found && m != null && board.makeMove(m)) {
						invalidate();
						AfterMove();
					} else if (board.piece[to] != 6 && board.side == board.color[to]) {
						sel = true;
						firstclick = false;
						from = Board.POS(col, row, board.reside);
						invalidate();
					} else {
						invalidate();
					}
				}
				return true;
			}
			default:
				break;
		}

		return super.onTouchEvent(event);
	}

	private void promote(int promote, int col, int row) {
		boolean found = false;
		TreeSet<Move> moves = board.gen();
		Iterator<Move> i = moves.iterator();

		Move m = null;
		while (i.hasNext()) {
			m = i.next();
			if (m.from == from && m.to == to && m.promote == promote) {
				found = true;
				break;
			}
		}
		if (found && m != null && board.makeMove(m)) {
			invalidate();
			AfterMove();
		} else if (board.piece[to] != 6 && board.side == board.color[to]) {
			sel = true;
			firstclick = false;
			from = Board.POS(col, row, board.reside);
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

	public Board getBoard() {
		return board;
	}

	public void setBoard(Board board) {
		this.board = board;
	}
}
