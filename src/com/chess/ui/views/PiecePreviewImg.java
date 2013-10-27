package com.chess.ui.views;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.chess.utilities.FontsHelper;
import com.chess.R;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 28.07.13
 * Time: 21:29
 */
public class PiecePreviewImg extends ImageView {

	private static final int COLUMN_CNT = 1; // = total column count - 1;
	private static final int SQUARES_IN_ROW_CNT = 2;
	private float numYOffset = 10;
	private float text_y_offset = 3;

	private int viewWidth;
	private int viewHeight;
	private int square;
	private Rect rect;
	private int[] pieces = new int[]{0,1,0,1};
	private int[] colors = new int[]{0,0,1,1};
	protected String[] signs = {"a", "b"};
	protected String[] nums = {"1", "2"};

	protected Bitmap[][] piecesBitmaps;
	private float density;
	private boolean showCoordinates = true;
	private boolean isHighlightEnabled = true;
	private Paint madeMovePaint;
	private Paint coordinatesPaint;


	public PiecePreviewImg(Context context) {
		super(context);
		init();
	}

	public PiecePreviewImg(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		rect = new Rect();
		density = getResources().getDisplayMetrics().density;

		numYOffset *= density;
		text_y_offset *= density;

		madeMovePaint = new Paint();
		madeMovePaint.setStrokeWidth(3.0f);
		madeMovePaint.setStyle(Paint.Style.STROKE);
		madeMovePaint.setColor(Color.YELLOW);

		int coordinateFont = getResources().getInteger(R.integer.board_highlight_font);
		int coordinateColor = getResources().getColor(R.color.coordinate_color_dark);

		coordinatesPaint = new Paint();
		coordinatesPaint.setStyle(Paint.Style.FILL);
		coordinatesPaint.setColor(coordinateColor);
		coordinatesPaint.setTextSize(coordinateFont * density);
		coordinatesPaint.setTypeface(FontsHelper.getInstance().getTypeFace(getContext(), FontsHelper.BOLD_FONT));

//		piecesBitmaps = new Bitmap[2][2];
	}

	public void setPiecesBitmaps(Bitmap[][] bitmaps){
		piecesBitmaps = bitmaps;
	}

	@Override
	protected void onDraw(Canvas canvas) {
//		drawHighlights(canvas);
//		drawCoordinates(canvas);
		if (piecesBitmaps != null) {
			drawPieces(canvas);
		}
	}


	protected void drawPieces(Canvas canvas) {
		for (int i = 0; i < 4; i++) {

			int color = colors[i];
			int piece = pieces[i];
			int x = getColumn(i);
			int y = getRow(i);
			int inSet = (int) (1 * density);

			rect.set(x * square + inSet, y * square + inSet, x * square + square - inSet, y * square + square - inSet);
			canvas.drawBitmap(piecesBitmaps[color][piece], null, rect, null);
		}
	}

	protected void drawCoordinates(Canvas canvas) {
		if (showCoordinates) {
			float xInset = (square / 8) * 7;
			for (int i = 0; i < SQUARES_IN_ROW_CNT; i++) {
				canvas.drawText(nums[1 - i], 2, i * square + numYOffset, coordinatesPaint);
				canvas.drawText(signs[i], i * square + xInset , SQUARES_IN_ROW_CNT * square - text_y_offset, coordinatesPaint);
			}
		}
	}

	protected void drawHighlights(Canvas canvas) {
		if (isHighlightEnabled) { // draw moved piece highlight from -> to
			// from
			int x1 = 1;
			int y1 = 1;
			canvas.drawRect(x1 * square + 1, y1 * square + 1,
					x1 * square + square - 1, y1 * square + square - 1, madeMovePaint);
		}
	}

	/**
	 * Get horizontal coordinate on the board for the given index of column
	 */
	private int getColumn(int x) {
		return (x & COLUMN_CNT);
	}

	/**
	 * Get vertical coordinate on the board for the given index of row
	 */
	private int getRow(int y) {
		return (y / 2); // the same as /8
	}

	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		viewWidth = (xNew == 0 ? viewWidth : xNew);
		viewHeight = (yNew == 0 ? viewHeight : yNew);
		square = viewHeight / 2; // there are only 2 rows and columns
	}

}
