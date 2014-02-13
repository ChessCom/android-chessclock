package com.chess.ui.views.drawables;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import com.chess.R;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 04.02.13
 * Time: 19:43
 */
public class PieChartDrawable extends Drawable {

	private final GradientDrawable gradientDrawable;

	public PieChartDrawable(Context context) {


		gradientDrawable = (GradientDrawable) context.getResources().getDrawable(R.drawable.pie_drawable);
//		gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
//				new int[] { 0xFFFF0000, 0xFFFFFFFF });
		gradientDrawable.setBounds(0, 0, 100, 100);
//		gradientDrawable.setShape(GradientDrawable.RING);
//		gradientDrawable.setGradientRadius(50);
//
//		gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
//		gradientDrawable.setColors(new int[]{0xFFFFFFFF, 0xFFFF0000});


	}

	@Override
	public void draw(Canvas canvas) {
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		// draw gradient circle
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.GREEN);

		canvas.translate(width / 2, height / 2);
//		canvas.drawCircle(width/2, height/2, width/2, paint);
		gradientDrawable.draw(canvas);
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
