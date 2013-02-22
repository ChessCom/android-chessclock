package com.chess.ui.views.drawables;

import android.graphics.*;
import android.graphics.drawable.Drawable;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.02.13
 * Time: 17:32
 */
public class AnalysisBackDrawable extends Drawable {

	private Paint paint;

	public AnalysisBackDrawable() {
		// make a checkedBoard pattern
		int color1 = 0xFF3f3c38;
		int color2 = 0xFF312e2b;
		Bitmap bm = Bitmap.createBitmap(new int[] {
				color1, color2,
				color2, color1 }, 2, 2, Bitmap.Config.ARGB_8888);

		BitmapShader shader = new BitmapShader(bm, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		paint = new Paint();
		paint.setShader(shader);
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
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
