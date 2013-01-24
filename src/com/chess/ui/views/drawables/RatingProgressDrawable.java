package com.chess.ui.views.drawables;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.widget.SeekBar;
import com.chess.R;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.01.13
 * Time: 19:52
 */
public class RatingProgressDrawable extends Drawable {

	private final BitmapDrawable imageBackDrawable;

	private float CORNER_RADIUS = 10;
	private int BORDER_STROKE_THICK = 1;
	private float BORDER_THICK = 2.5f;
	private int RIGHT_OFFSET = 10;
	private int CENTER_OFFSET = 4;
	private final SeekBar seekBar;

	private Rect patternImageRect;
	private Bitmap roundedBitmap;
	private final PorterDuffXfermode xRefMode2;

	private final GradientDrawable gradientDrawable;
	private final GradientDrawable gradientStrokeDrawable;
	private int ratingBarHeight = 20;


	public RatingProgressDrawable(Context context, SeekBar seekBar) {
		this.seekBar = seekBar;

		float density = context.getResources().getDisplayMetrics().density;

		BORDER_THICK *= density;
		RIGHT_OFFSET  *= density;
		CORNER_RADIUS *= density;
		CENTER_OFFSET *= density;

		imageBackDrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_progress_bitmap_back);
		imageBackDrawable.setAntiAlias(true);
		imageBackDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

		patternImageRect = new Rect();
		xRefMode2 = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

		gradientStrokeDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
				new int[] {0xFF2d2a27, 0xFF35322f});

		gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
				new int[] {0xFF292623, 0xFF393532});

		ratingBarHeight *= context.getResources().getDisplayMetrics().density;
	}

	@Override
	public void draw(Canvas canvas) {
		int width = seekBar.getWidth();
		int height = seekBar.getHeight()/2;


		gradientStrokeDrawable.setBounds(0, 0, width - RIGHT_OFFSET, (int) (height + BORDER_THICK * 2 + BORDER_STROKE_THICK * 2));
		gradientDrawable.setBounds(BORDER_STROKE_THICK, 0, width - RIGHT_OFFSET - BORDER_STROKE_THICK * 2, (int) (height + BORDER_THICK * 2));


		// draw shape above background
		if (roundedBitmap == null) {
			roundedBitmap = createRoundedBitmap(width, height);

			patternImageRect.set(0, 0, (int) (width - RIGHT_OFFSET - BORDER_THICK * 2 - BORDER_STROKE_THICK * 2), height);
		}

		// draw small stroke around gradient back
		canvas.save();
		if (AppUtils.HONEYCOMB_PLUS_API) {
			canvas.translate(0, canvas.getHeight() / 4 - BORDER_THICK - BORDER_STROKE_THICK);

		} else {
			canvas.translate(0, height / 3 - BORDER_THICK - BORDER_STROKE_THICK + CENTER_OFFSET);

		}
		setCornerRadii(gradientStrokeDrawable, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS);
		gradientStrokeDrawable.draw(canvas);
		canvas.restore();

		// gradient rounded background
		canvas.save();
		if (AppUtils.HONEYCOMB_PLUS_API) {
			canvas.translate(BORDER_STROKE_THICK, canvas.getHeight() / 4 - BORDER_THICK );

		} else {
			canvas.translate(BORDER_STROKE_THICK, height / 3  - BORDER_THICK + CENTER_OFFSET);

		}
		setCornerRadii(gradientDrawable, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS);
		gradientDrawable.draw(canvas);
		canvas.restore();

		// patterned rounded background
		canvas.save();
		if (AppUtils.HONEYCOMB_PLUS_API) {
			canvas.translate(BORDER_THICK + BORDER_STROKE_THICK, canvas.getHeight() / 4);
		} else {
			canvas.translate(BORDER_THICK + BORDER_STROKE_THICK, height / 3 + CENTER_OFFSET );
		}
		canvas.drawBitmap(roundedBitmap, null, patternImageRect, null);
		canvas.restore();
	}

	static void setCornerRadii(GradientDrawable drawable, float r0, float r1, float r2, float r3) {
		drawable.setCornerRadii(new float[] { r0, r0, r1, r1,
				r2, r2, r3, r3 });
	}

	private Bitmap createRoundedBitmap(int width, int height) {
		// back pattern
		Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(output);

		RectF outerRect = new RectF(0, 0, width, height);
		float outerRadiusX = CORNER_RADIUS;

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		// draw limiter on canvas
		canvas.drawRoundRect(outerRect, outerRadiusX, outerRadiusX, paint);

		paint.setXfermode(xRefMode2);
		imageBackDrawable.setBounds(0, 0, width, height);

		// cross layers and save on canvas
		canvas.saveLayer(outerRect, paint, Canvas.ALL_SAVE_FLAG);
		imageBackDrawable.draw(canvas);
		canvas.restore();

		return Bitmap.createBitmap(output);
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
