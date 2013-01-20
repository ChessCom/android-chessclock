package com.chess.ui.views;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.widget.SeekBar;
import com.chess.R;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.01.13
 * Time: 19:52
 */
public class RatingProgressDrawable extends Drawable {

	private final BitmapDrawable imageBackDrawable;

	private static final float CORNER_RADIUS = 21;
	private static final int BORDER_STROKE_THICK = 1;
	private static final int BORDER_THICK = 5;

	private Rect patternImageRect;
	private Bitmap roundedBitmap;
	private final PorterDuffXfermode xRefMode2;
	private SeekBar ratingBar;
	private final GradientDrawable gradientDrawable;
	private final GradientDrawable gradientStrokeDrawable;


	public RatingProgressDrawable(Context context, SeekBar ratingBar) {
		this.ratingBar = ratingBar;
		imageBackDrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_progress_bitmap_back);
		imageBackDrawable.setAntiAlias(true);
		imageBackDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

		patternImageRect = new Rect();
		xRefMode2 = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

		gradientStrokeDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
				new int[] {0xFF2d2a27, 0xFF35322f});

		gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
				new int[] {0xFF292623, 0xFF393532});


	}

	@Override
	public void draw(Canvas canvas) {
		int width = canvas.getWidth();
		int height = ratingBar.getMinimumHeight();


		gradientStrokeDrawable.setBounds(0, 0, width - 20, height + BORDER_THICK * 2 + BORDER_STROKE_THICK * 2);
		gradientDrawable.setBounds(BORDER_STROKE_THICK, 0, width - 20 - BORDER_STROKE_THICK * 2,
				height + BORDER_THICK * 2);


		// draw shape above background
		if (roundedBitmap == null) {
			roundedBitmap = createRoundedBitmap(width, height);

			patternImageRect.set(0, 0, width - 20 - BORDER_THICK * 2 - BORDER_STROKE_THICK * 2, height);
		}

		// draw small stroke around gradient back
		canvas.save();
		canvas.translate(0, canvas.getHeight() / 4 - BORDER_THICK - BORDER_STROKE_THICK);
		setCornerRadii(gradientStrokeDrawable, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS);
		gradientStrokeDrawable.draw(canvas);
		canvas.restore();

		// gradient rounded background
		canvas.save();
		canvas.translate(BORDER_STROKE_THICK, canvas.getHeight() / 4 - BORDER_THICK );
		setCornerRadii(gradientDrawable, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS);
		gradientDrawable.draw(canvas);
		canvas.restore();

		// patterned rounded background
		canvas.save();
		canvas.translate(BORDER_THICK + BORDER_STROKE_THICK, canvas.getHeight() / 4);
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
		return PixelFormat.TRANSLUCENT;
	}
}
