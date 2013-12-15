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
	private Drawable backDrawable;
	private final Rect rect;
	private final ColorDrawable colorDrawable;
	private final AppData appData;
	private final Drawable stauntonDrawable;

	public ActionBarBackgroundDrawable(Context context) {

		int topBarColor1 = context.getResources().getColor(R.color.action_bar_overlay);
		rect = new Rect();
		appData = new AppData(context);

		stauntonDrawable = context.getResources().getDrawable(R.drawable.img_staunton_top_bar);
		colorDrawable = new ColorDrawable(topBarColor1);

		updateDrawable();
	}

	@Override
	public void draw(Canvas canvas) {
		canvas.getClipBounds(rect);
		backDrawable.setBounds(rect);
		backDrawable.draw(canvas);
	}

	@Override
	public void setAlpha(int alpha) {
		backDrawable.setAlpha(alpha);
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		backDrawable.setColorFilter(cf);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.OPAQUE;
	}

	public void updateDrawable(){
		String themeName = appData.getThemeBackgroundName();
		// if we have staunton theme, then set custom image
		if (!TextUtils.isEmpty(themeName) && themeName.contains(STAUNTON)) {
			backDrawable = stauntonDrawable;
		} else {
			backDrawable = colorDrawable;
		}
	}
}
