package com.chess.ui.views.drawables;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import com.chess.R;
import com.chess.ui.engine.ChessBoard;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 23.01.13
 * Time: 21:31
 */
public class CapturedPiecesDrawable extends Drawable {

	private final static int PAWN_ID = 0;
	private final static int KNIGHT_ID = 1;
	private final static int BISHOP_ID = 2;
	private final static int ROOK_ID = 3;
	private final static int QUEEN_ID = 4;

	private final Drawable[] whitePieceDrawables;
	private final Drawable[] blackPieceDrawables;
	private int totalPiecesCountArray[] = new int[]{8, 2, 2, 2, 1, 1};

	private int INNER_PIECE_OFFSET = 4;
	private int BETWEEN_PIECE_OFFSET = 20;
	private int BETWEEN_PAWN_PIECE_OFFSET = 37;

	private int capturedPawnCnt;
	private int capturedKnightCnt;
	private int capturedBishopCnt;
	private int capturedRookCnt;
	private int capturedQueenCnt;

	private int side;

	private Drawable[] currentSideDrawables;
	private final int pieceWidth;

	public CapturedPiecesDrawable(Context context) {
		boolean smallScreen = AppUtils.noNeedTitleBar(context);

		float density = context.getResources().getDisplayMetrics().density;

		INNER_PIECE_OFFSET *= density;
		BETWEEN_PIECE_OFFSET *= density;
		BETWEEN_PAWN_PIECE_OFFSET *= density;

		// TODO get selected pieces set and create scaled versions of each piece

		int[] whitePieceDrawableIds = new int[]{
				R.drawable.captured_wp,
				R.drawable.captured_wn,
				R.drawable.captured_wb,
				R.drawable.captured_wr,
				R.drawable.captured_wq
		};

		int[] blackPieceDrawableIds = new int[]{   // TODO reuse to set other drawable sets
				R.drawable.captured_bp,
				R.drawable.captured_bn,
				R.drawable.captured_bb,
				R.drawable.captured_br,
				R.drawable.captured_bq
		};

		whitePieceDrawables = new Drawable[5];
		blackPieceDrawables = new Drawable[5];

		for (int i = 0, whitePieceDrawablesLength = whitePieceDrawables.length; i < whitePieceDrawablesLength; i++) {
			whitePieceDrawables[i] = context.getResources().getDrawable(whitePieceDrawableIds[i]);
			whitePieceDrawables[i].setBounds(0, 0, whitePieceDrawables[i].getIntrinsicWidth(), whitePieceDrawables[i].getIntrinsicHeight());
		}

		pieceWidth = whitePieceDrawables[KNIGHT_ID].getIntrinsicWidth();
		if (smallScreen) {
			INNER_PIECE_OFFSET = pieceWidth / 6;

			BETWEEN_PIECE_OFFSET = pieceWidth + INNER_PIECE_OFFSET;

			BETWEEN_PAWN_PIECE_OFFSET = (int) (BETWEEN_PIECE_OFFSET * 1.5f);
		} else {
			INNER_PIECE_OFFSET = pieceWidth / 4;

			BETWEEN_PIECE_OFFSET = pieceWidth + INNER_PIECE_OFFSET;

			BETWEEN_PAWN_PIECE_OFFSET = BETWEEN_PIECE_OFFSET * 2;
		}

		for (int i = 0, blackPieceDrawablesLength = blackPieceDrawables.length; i < blackPieceDrawablesLength; i++) {
			blackPieceDrawables[i] = context.getResources().getDrawable(blackPieceDrawableIds[i]);
			blackPieceDrawables[i].setBounds(0, 0, blackPieceDrawables[i].getIntrinsicWidth(), blackPieceDrawables[i].getIntrinsicHeight());
		}

		currentSideDrawables = blackPieceDrawables;

//		capturedPawnCnt = 8;
//		capturedKnightCnt = 2;
//		capturedBishopCnt = 2;
//		capturedRookCnt = 2;
//		capturedQueenCnt = 1;
	}

	@Override
	public void draw(Canvas canvas) {
		int width = getBounds().width();
		int height = getBounds().height();

		if (side == ChessBoard.BLACK_SIDE) {
			currentSideDrawables = blackPieceDrawables;
		} else {
			currentSideDrawables = whitePieceDrawables;
		}

		// translate to vertical center
		canvas.translate(0, height / 2 - pieceWidth / 2);

		{// draw pawns
			for (int i = 0; i < capturedPawnCnt; i++) {
				canvas.save();
				canvas.translate(INNER_PIECE_OFFSET * i, 0);
				currentSideDrawables[PAWN_ID].draw(canvas);
				canvas.restore();
			}
		}

		{// draw knights
			canvas.translate(BETWEEN_PAWN_PIECE_OFFSET, 0);

			for (int i = 0; i < capturedKnightCnt; i++) {
				canvas.save();
				canvas.translate(INNER_PIECE_OFFSET * i, 0);
				currentSideDrawables[KNIGHT_ID].draw(canvas);
				canvas.restore();
			}
		}

		{// draw bishop
			canvas.translate(BETWEEN_PIECE_OFFSET, 0);

			for (int i = 0; i < capturedBishopCnt; i++) {
				canvas.save();
				canvas.translate(INNER_PIECE_OFFSET * i, 0);
				currentSideDrawables[BISHOP_ID].draw(canvas);
				canvas.restore();
			}
		}

		{// draw rook
			canvas.translate(BETWEEN_PIECE_OFFSET, 0);

			for (int i = 0; i < capturedRookCnt; i++) {
				canvas.save();
				canvas.translate(INNER_PIECE_OFFSET * i, 0);
				currentSideDrawables[ROOK_ID].draw(canvas);
				canvas.restore();
			}
		}

		{// draw queen
			canvas.translate(BETWEEN_PIECE_OFFSET, 0);

			for (int i = 0; i < capturedQueenCnt; i++) {
				canvas.save();
				canvas.translate(INNER_PIECE_OFFSET * i, 0);
				currentSideDrawables[QUEEN_ID].draw(canvas);
				canvas.restore();
			}
		}
	}

	public int getSide() {
		return side;
	}

	public void setSide(int side) {
		this.side = side;
		invalidateSelf();
	}

	public void updateCapturedPieces(int[] alivePiecesCountArray) {
		boolean update = false;
		for (int pieceId = 0; pieceId < alivePiecesCountArray.length; pieceId++) {
			int aliveCnt = alivePiecesCountArray[pieceId];
			int totalPieceCnt = totalPiecesCountArray[pieceId];
			int capturedPieceCnt = totalPieceCnt - aliveCnt;
			switch (pieceId) {
				case PAWN_ID:
					if (capturedPawnCnt != capturedPieceCnt) {
						capturedPawnCnt = capturedPieceCnt;
						update = true;
					}
					break;
				case KNIGHT_ID:
					if (capturedKnightCnt != capturedPieceCnt) {
						capturedKnightCnt = capturedPieceCnt;
						update = true;
					}
					break;
				case BISHOP_ID:
					if (capturedBishopCnt != capturedPieceCnt) {
						capturedBishopCnt = capturedPieceCnt;
						update = true;
					}
					break;
				case ROOK_ID:
					if (capturedRookCnt != capturedPieceCnt) {
						capturedRookCnt = capturedPieceCnt;
						update = true;
					}
					break;
				case QUEEN_ID:
					if (capturedQueenCnt != capturedPieceCnt) {
						capturedQueenCnt = capturedPieceCnt;
						update = true;
					}
					break;
			}
		}
		if (update) {
			invalidateSelf();
		}
	}

	public void dropPieces() {
		capturedPawnCnt = 0;
		capturedKnightCnt = 0;
		capturedBishopCnt = 0;
		capturedRookCnt = 0;
		capturedQueenCnt = 0;

		invalidateSelf();
	}

	@Override
	public void setAlpha(int alpha) {
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
	}

	@Override
	public int getOpacity() {
		return PixelFormat.OPAQUE;
	}


}
