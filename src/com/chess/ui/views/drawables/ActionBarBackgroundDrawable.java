package com.chess.ui.views.drawables;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import com.chess.R;
import com.chess.statics.AppData;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 09.01.13
 * Time: 6:55
 */
public class ActionBarBackgroundDrawable extends Drawable {

	public static final String STAUNTON = "Staunton";
	private final Drawable backDawable;
	private final Rect rect;

	public ActionBarBackgroundDrawable(Context context) {

		int topBarColor1 = context.getResources().getColor(R.color.action_bar_overlay);
		rect = new Rect();
		AppData appData = new AppData(context);

		// if we have staunton theme, then set custom image
		if (!TextUtils.isEmpty(appData.getThemeName()) && appData.getThemeName().contains(STAUNTON)) {
			backDawable = context.getResources().getDrawable(R.drawable.img_staunton_top_bar);
		} else {
			backDawable = new ColorDrawable(topBarColor1);
		}
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.getClipBounds(rect);
		backDawable.setBounds(rect);
		backDawable.draw(canvas);
	}

	@Override
	public void setAlpha(int alpha) {
		backDawable.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		backDawable.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.OPAQUE;
	}
}
