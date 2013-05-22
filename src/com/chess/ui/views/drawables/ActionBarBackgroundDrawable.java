package com.chess.ui.views.drawables;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import com.chess.R;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 09.01.13
 * Time: 6:55
 */
public class ActionBarBackgroundDrawable extends Drawable {

	private final Drawable mDrawable;
	private final Rect mRect;

	public ActionBarBackgroundDrawable(Context context) {

		int topBarColor1 = context.getResources().getColor(R.color.action_bar_overlay);
		mRect = new Rect();

		mDrawable = new ColorDrawable(topBarColor1);
	}

	@Override
	public void draw(Canvas canvas) {
		mRect.set(0, 0, canvas.getWidth(), canvas.getHeight());
		mDrawable.setBounds(mRect);
		mDrawable.draw(canvas);
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
