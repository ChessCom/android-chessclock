package com.chess.ui.views.drawables;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import com.chess.R;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 20.07.13
 * Time: 12:25
 */
public class YourMoveDrawable extends Drawable {


	private final IconDrawable helpIcon;
	private final IconDrawable oneIcon;
	private final IconDrawable twoIcon;
	private final IconDrawable noneIcon;
	private final int iconWidth1;
	private final int iconWidth2;
	private final int iconWidth3;
	private final int iconWidth4;
	private final int imageZoneWidth;
	private final int space1;
	private final int space2;
	private final int offset;

	public YourMoveDrawable(Context context) {
		float density = context.getResources().getDisplayMetrics().density;

//		int iconId, int colorId, int sizeId
		helpIcon = new IconDrawable(context, R.string.ic_help, R.color.controls_icon, R.dimen.game_controls_lesson_help_icon_size_big);
		oneIcon = new IconDrawable(context, R.string.ic_number_one, R.color.controls_icon, R.dimen.game_controls_lesson_help_icon_size);
		twoIcon = new IconDrawable(context, R.string.ic_number_two, R.color.controls_icon, R.dimen.game_controls_lesson_help_icon_size);
		noneIcon = new IconDrawable(context, R.string.ic_number_none, R.color.controls_icon, R.dimen.game_controls_lesson_help_icon_size);

		float shadowRadius = 2 * density;
		int shadowDx = 0;
		int shadowDy = 0;
		int shadowColor = context.getResources().getColor(R.color.back_shadows_chess);
		helpIcon.setShadowParams(shadowRadius, shadowDx, shadowDy, shadowColor);
		oneIcon.setShadowParams(shadowRadius, shadowDx, shadowDy, shadowColor);
		twoIcon.setShadowParams(shadowRadius, shadowDx, shadowDy, shadowColor);
		noneIcon.setShadowParams(shadowRadius, shadowDx, shadowDy, shadowColor);

		iconWidth1 = helpIcon.getIntrinsicWidth();
		iconWidth2 = oneIcon.getIntrinsicWidth();
		iconWidth3 = twoIcon.getIntrinsicWidth();
		iconWidth4 = noneIcon.getIntrinsicWidth();
		space1 = (int) (4 * density);
		space2 = (int) (2 * density);
		imageZoneWidth = iconWidth1 + iconWidth2 + iconWidth3 + iconWidth4 + space1 + space2 * 2;

		offset = (int) (10 * density);
	}

	@Override
	public void draw(Canvas canvas) {
		Rect clipBounds = canvas.getClipBounds();
		int right = clipBounds.right;
		int bottom = clipBounds.bottom;
		int centerX = right / 2 + offset;
		canvas.save();
		canvas.translate(centerX - imageZoneWidth / 2, bottom / 2);
		helpIcon.draw(canvas);
		canvas.restore();

		canvas.save();
		canvas.translate(centerX - imageZoneWidth / 2 + iconWidth1 + space1, bottom / 2);
		oneIcon.draw(canvas);
		canvas.restore();

		canvas.save();
		canvas.translate(centerX - imageZoneWidth / 2 + iconWidth1 + space1 + iconWidth2 + space2, bottom / 2);
		twoIcon.draw(canvas);
		canvas.restore();

		canvas.save();
		canvas.translate(centerX - imageZoneWidth / 2 + iconWidth1 + space1 + iconWidth2
				+ space2 + iconWidth3 + space2, bottom / 2);
		noneIcon.draw(canvas);
		canvas.restore();


	}

	@Override
	public void setAlpha(int alpha) {
	}

	@Override
	public void setColorFilter(ColorFilter cf) {

	}

	@Override
	public int getOpacity() {
		return 0;
	}
}
