package com.chess.ui.views.drawables;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import com.chess.R;
import com.chess.utilities.FontsHelper;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 25.05.13
 * Time: 13:06
 */
public class IconDrawable extends Drawable {

	private Paint iconPaint;
	private String iconStr;
	private float iconHalfWidth;
	private int iconSize;

	/**
	 * Draw image from glyphIcon font resource
	 *
	 * @param context for resources
	 * @param iconStr to be used as an image, must be get from glyph_resources
	 * @param colorId color to be paint
	 * @param sizeId  font size
	 */
	public IconDrawable(Context context, String iconStr, int colorId, int sizeId) {
		this.iconStr = iconStr;
		init(context, colorId, sizeId);
	}

	/**
	 * Draw image from glyphIcon font resource
	 *
	 * @param context for resources
	 * @param iconId  to be used as an image, must be get from glyph_resources
	 * @param colorId color to be paint
	 * @param sizeId  font size
	 */
	public IconDrawable(Context context, int iconId, int colorId, int sizeId) {
		iconStr = context.getString(iconId);
		init(context, colorId, sizeId);
	}

	/**
	 * Draw image from glyphIcon font resource with default parameters
	 *
	 * @param context for resources
	 * @param iconId  to be used as an image, must be get from glyph_resources
	 */
	public IconDrawable(Context context, int iconId) {
		iconStr = context.getString(iconId);
		init(context, R.color.semitransparent_white_75, R.dimen.edit_field_icon_size);
	}

	/**
	 * Draw image from glyphIcon font resource with default parameters
	 *
	 * @param context for resources
	 * @param iconId  to be used as an image, must be get from glyph_resources
	 */
	public IconDrawable(Context context, int iconId, ColorStateList colorStateList, int sizeId) {
		iconStr = context.getString(iconId);
		Resources resources = context.getResources();
		int size = resources.getDimensionPixelSize(sizeId);

		iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		iconPaint.setStyle(Paint.Style.FILL);
		iconPaint.setColor(colorStateList.getDefaultColor());
		iconPaint.setTextSize(size);
		iconPaint.setTypeface(FontsHelper.getInstance().getTypeFace(context, FontsHelper.ICON_FONT));

		iconSize = (int) iconPaint.measureText(iconStr);
		iconHalfWidth = iconPaint.measureText(iconStr) / 2;
	}

	private void init(Context context, int colorId, int sizeId) {
		Resources resources = context.getResources();
		int color = resources.getColor(colorId);
		int size = resources.getDimensionPixelSize(sizeId);

		iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		iconPaint.setStyle(Paint.Style.FILL);
		iconPaint.setColor(color);
		iconPaint.setTextSize(size);
		iconPaint.setTypeface(FontsHelper.getInstance().getTypeFace(context, FontsHelper.ICON_FONT));

		iconSize = (int) iconPaint.measureText(iconStr);
		iconHalfWidth = iconPaint.measureText(iconStr) / 2;
	}

	@Override
	public void draw(Canvas canvas) {
		Rect clipBounds = getBounds();
		int width = clipBounds.right;
		int height = clipBounds.bottom;

		float iconY0 = height / 2 + iconHalfWidth;
		float iconX0 = width / 2 - iconHalfWidth;

		canvas.drawText(iconStr, iconX0, iconY0, iconPaint);
	}

	@Override
	public void setAlpha(int alpha) {
		int oldAlpha = iconPaint.getAlpha();
		if (alpha != oldAlpha) {
			iconPaint.setAlpha(alpha);
			invalidateSelf();
		}
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
		iconPaint.setColorFilter(cf);
		invalidateSelf();
	}

	@Override
	public int getOpacity() {
		return 0;
	}

	@Override
	public int getIntrinsicWidth() {
		return iconSize;
	}

	@Override
	public int getIntrinsicHeight() {
		return iconSize;
	}

	public void setShadowParams(float radius, int dx, int dy, int color) {
		iconPaint.setShadowLayer(radius, dx, dy, color);
	}

}
