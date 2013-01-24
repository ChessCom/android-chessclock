package com.chess.ui.views.drawables;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import com.chess.R;
import com.chess.backend.statics.AppConstants;

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

	private final int[] whitePieceDrawableIds;
	private final int[] blackPieceDrawableIds;
	private final Drawable[] whitePieceDrawables;
	private final Drawable[] blackPieceDrawables;

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
	private Drawable backDrawable;
	private Context context;

	public CapturedPiecesDrawable(Context context) {
		this.context = context;
		float density = context.getResources().getDisplayMetrics().density;

		INNER_PIECE_OFFSET *= density;
		BETWEEN_PIECE_OFFSET *= density;
		BETWEEN_PAWN_PIECE_OFFSET *= density;

		whitePieceDrawableIds = new int[]{
				R.drawable.captured_wp,
				R.drawable.captured_wn,
				R.drawable.captured_wb,
				R.drawable.captured_wr,
				R.drawable.captured_wq
		};

		blackPieceDrawableIds = new int[]{   // TODO reuse to set other drawable sets
				R.drawable.captured_bp,
				R.drawable.captured_bn,
				R.drawable.captured_bb,
				R.drawable.captured_br,
				R.drawable.captured_bq
		};

		whitePieceDrawables = new Drawable[5];
		blackPieceDrawables = new Drawable[5];

		backDrawable = context.getResources().getDrawable(R.drawable.back_grey_emboss);


		for (int i = 0, whitePieceDrawablesLength = whitePieceDrawables.length; i < whitePieceDrawablesLength; i++) {
			whitePieceDrawables[i] = context.getResources().getDrawable(whitePieceDrawableIds[i]);
			whitePieceDrawables[i].setBounds(0, 0, whitePieceDrawables[i].getIntrinsicWidth(), whitePieceDrawables[i].getIntrinsicHeight());
		}

		pieceWidth = whitePieceDrawables[KNIGHT_ID].getIntrinsicWidth();
		INNER_PIECE_OFFSET = pieceWidth / 4;

		BETWEEN_PIECE_OFFSET = pieceWidth + INNER_PIECE_OFFSET;

		BETWEEN_PAWN_PIECE_OFFSET = BETWEEN_PIECE_OFFSET * 2;

		for (int i = 0, blackPieceDrawablesLength = blackPieceDrawables.length; i < blackPieceDrawablesLength; i++) {
			blackPieceDrawables[i] = context.getResources().getDrawable(blackPieceDrawableIds[i]);
			blackPieceDrawables[i].setBounds(0, 0, blackPieceDrawables[i].getIntrinsicWidth(), blackPieceDrawables[i].getIntrinsicHeight());
		}

		currentSideDrawables = blackPieceDrawables;

		capturedPawnCnt = 8;
		capturedKnightCnt = 2;
		capturedBishopCnt = 2;
		capturedRookCnt = 2;
		capturedQueenCnt = 1;
	}

	@Override
	public void draw(Canvas canvas) {
		int width = canvas.getWidth();
		int height = canvas.getHeight();


		if (side == AppConstants.WHITE_SIDE) {
			backDrawable = context.getResources().getDrawable(R.drawable.back_white_emboss);

			currentSideDrawables = blackPieceDrawables;
		} else {
			backDrawable = context.getResources().getDrawable(R.drawable.back_grey_emboss);
			currentSideDrawables = whitePieceDrawables;

		}

		backDrawable.setBounds(0, 0, width, height);
//		backDrawable.draw(canvas);

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


	public int getCapturedPawnCnt() {
		return capturedPawnCnt;
	}

	public void setCapturedPawnCnt(int capturedPawnCnt) {
		this.capturedPawnCnt = capturedPawnCnt;
	}

	public int getCapturedKnightCnt() {
		return capturedKnightCnt;
	}

	public void setCapturedKnightCnt(int capturedKnightCnt) {
		this.capturedKnightCnt = capturedKnightCnt;
	}

	public int getCapturedBishopCnt() {
		return capturedBishopCnt;
	}

	public void setCapturedBishopCnt(int capturedBishopCnt) {
		this.capturedBishopCnt = capturedBishopCnt;
	}

	public int getCapturedRookCnt() {
		return capturedRookCnt;
	}

	public void setCapturedRookCnt(int capturedRookCnt) {
		this.capturedRookCnt = capturedRookCnt;
	}

	public int getCapturedQueenCnt() {
		return capturedQueenCnt;
	}

	public void setCapturedQueenCnt(int capturedQueenCnt) {
		this.capturedQueenCnt = capturedQueenCnt;
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
