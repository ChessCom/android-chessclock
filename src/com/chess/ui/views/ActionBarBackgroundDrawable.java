package com.chess.ui.views;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.Shape;
import com.chess.R;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 09.01.13
 * Time: 6:55
 */
public class ActionBarBackgroundDrawable extends Drawable {

	private Context context;
	private final GradientDrawable mDrawable;
	private final Paint mPaint;
	private final Rect mRect;
	private final BitmapDrawable imageOverDrawable;
	private final int topBarCornerRadius;

	public ActionBarBackgroundDrawable(Context context) {
		this.context = context;

		float density = context.getResources().getDisplayMetrics().density;
		int topBarColor1 = context.getResources().getColor(R.color.top_bar_1);
		int topBarColor2 = context.getResources().getColor(R.color.top_bar_2);
		int topBarBorderColor = context.getResources().getColor(R.color.top_bar_border);
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mRect = new Rect();

		mDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[] { topBarColor1, topBarColor2 });
		mDrawable.setShape(GradientDrawable.RECTANGLE);
		mDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
		mDrawable.setStroke((int) density, topBarBorderColor);

		imageOverDrawable = (BitmapDrawable) context.getResources().getDrawable(R.drawable.img_top_bar_texture_overlay);
		imageOverDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

		topBarCornerRadius = (int) context.getResources().getDimension(R.dimen.new_round_button_radius);
	}

	@Override
	public void draw(Canvas canvas) {
		mRect.set(0, 0, canvas.getWidth(), canvas.getHeight());
		mDrawable.setBounds(mRect);
		setCornerRadii(mDrawable, topBarCornerRadius, topBarCornerRadius, 0, 0);
		mDrawable.draw(canvas);

		imageOverDrawable.setBounds(mRect);
		imageOverDrawable.draw(canvas);
	}

	static void setCornerRadii(GradientDrawable drawable, float r0, float r1, float r2, float r3) {
		drawable.setCornerRadii(new float[] { r0, r0, r1, r1,
											  r2, r2, r3, r3 });
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
