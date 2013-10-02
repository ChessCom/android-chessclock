package com.chess.ui.views.drawables;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import com.chess.FontsHelper;
import com.chess.R;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 31.12.12
 * Time: 6:22
 */
public class BadgeDrawable extends Drawable {

	private static final int ICON_SIZE = 48;
	private final Drawable icon;
	private final boolean badDevice;
	private int value;
	private Paint rectangleMainPaint;
	private Paint rectangleBorderPaint;
	private Paint textPaint;
	private final int bottom;
	private final int right;
	private final float density;
	private final RectF badgeRect;
	private final float cornerRadius;
	private final Paint rectangleBorderBottomPaint;
	private final RectF badgeBorderRect;
	private final int rectangleSize;
	private final float textSize;
	private final float textWidth;
	private final float textHeight;
	private final float viewHeight;
	private final float viewWidth;

	public BadgeDrawable(Context context, Drawable icon, int value) {
		this.icon = icon;
		this.value = value;
		badDevice = isBadDevice(context);

		if (badDevice) {
			this.icon.setBounds(0, 0, icon.getIntrinsicWidth() + 5, icon.getIntrinsicHeight() + 5);
		} else {
			this.icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
		}
		rectangleSize = 17;

		density = context.getResources().getDisplayMetrics().density;

		int border1Color = context.getResources().getColor(R.color.action_badge_border_1);
		int borderMainColor1 = context.getResources().getColor(R.color.action_badge_main_1);
		int borderMainColor2 = context.getResources().getColor(R.color.action_badge_main_2);
		int borderBorderTopColor = context.getResources().getColor(R.color.action_badge_border_top);
		int borderBorderBottomColor = context.getResources().getColor(R.color.action_badge_border_bottom);
		int main1 = context.getResources().getColor(R.color.action_badge_main_1);
		Shader mainShader = new LinearGradient(0, 0, rectangleSize, rectangleSize, borderMainColor1, borderMainColor2, Shader.TileMode.CLAMP);
		Shader borderShader = new LinearGradient(0, 0, rectangleSize, rectangleSize, borderBorderTopColor, borderBorderBottomColor, Shader.TileMode.CLAMP);

		rectangleMainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		rectangleMainPaint.setDither(true);
		rectangleMainPaint.setColor(main1);
		rectangleMainPaint.setStrokeWidth(0.0f);
		rectangleMainPaint.setStyle(Paint.Style.FILL);
//		Shader mainShader = new LinearGradient(0, 0, rectangleSize, rectangleSize, Color.WHITE, Color.RED, Shader.TileMode.CLAMP);
		rectangleMainPaint.setShader(mainShader);

		rectangleBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		rectangleBorderPaint.setDither(true);
		rectangleBorderPaint.setColor(border1Color);
		rectangleBorderPaint.setStrokeWidth(1.0f);
		rectangleBorderPaint.setStyle(Paint.Style.STROKE);

		rectangleBorderBottomPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		rectangleBorderBottomPaint.setDither(true);
		rectangleBorderBottomPaint.setColor(border1Color);
		rectangleBorderBottomPaint.setStrokeWidth(1.0f);
		rectangleBorderBottomPaint.setStyle(Paint.Style.STROKE);
		rectangleBorderBottomPaint.setShader(borderShader);

		textSize = 11 * density;
		textWidth = 3 * density;
		textHeight = 5 * density;

		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(textSize);
		textPaint.setTypeface(FontsHelper.getInstance().getTypeFace(context, FontsHelper.BOLD_FONT));

		Rect bounds = icon.getBounds();
		bottom = (int) (bounds.bottom * density);
		right = (int) (bounds.right * density);

		badgeRect = new RectF();
		badgeBorderRect = new RectF();

		cornerRadius = 4 * density;

		// Used only for PRE-ICE
		viewHeight = context.getResources().getDimension(R.dimen.actionbar_compat_height);
		viewWidth = context.getResources().getDimension(R.dimen.actionbar_compat_button_width);
	}

	private boolean isBadDevice(Context context) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		return (displayMetrics.density == 1.0f || displayMetrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM)
				/*&& (displayMetrics.heightPixels <= 480) */&& AppUtils.HONEYCOMB_PLUS_API;
	}

	@Override
	public void draw(Canvas canvas) {

		float x0;
		float y0;
		float x1;
		float y1;

		if (AppUtils.HONEYCOMB_PLUS_API) { // TODO set initialized flag
			x0 = 0;
			y0 = 0;
			x1 = rectangleSize * density;
			y1 = rectangleSize * density;

			if (badDevice) {
				Rect bounds = getBounds();
				x0 = bounds.centerX();
				y0 = bounds.centerY();
				x1 = x0 + rectangleSize * density;
				y1 = y0 + rectangleSize * density;

				badgeRect.set(x0, y0, x1, y1);
				badgeBorderRect.set(x0 + 1, y0 + 1, (rectangleSize - 0.5f) * density, (rectangleSize - 0.5f) * density);
			} else {
				badgeRect.set(x0, y0, x1, y1);
				badgeBorderRect.set(x0 + 1, y0 + 1, (rectangleSize - 0.5f) * density, (rectangleSize - 0.5f) * density);
			}

			float iconX0 = -icon.getIntrinsicWidth() / 2;
			float iconY0 = -icon.getIntrinsicHeight() / 2;
			canvas.save();
			if (badDevice) {
				float xx = icon.getIntrinsicWidth();
				canvas.translate((ICON_SIZE - xx) / 2 - 4, (ICON_SIZE - xx) / 2 - 4); // hate this stupid hardcode :(
			} else {
				canvas.translate(iconX0, iconY0);
			}

			icon.draw(canvas);
			canvas.restore();

		} else {
			float xShift = viewWidth / 2;
			float yShift = viewHeight / 2;

			x0 = 0 + xShift;
			y0 = 0 + xShift;
			x1 = rectangleSize * density + yShift;
			y1 = rectangleSize * density + yShift;


			badgeRect.set(x0, y0, x1, y1);
			badgeBorderRect.set(x0, y0, x1, y1);

			float iconX0 = (viewWidth - icon.getIntrinsicWidth()) / 2;
			float iconY0 = (viewHeight - icon.getIntrinsicHeight()) / 2;
			canvas.save();
			canvas.translate(iconX0, iconY0);
			icon.draw(canvas);
			canvas.restore();
		}

		if (value != 0) {
			canvas.drawRoundRect(badgeRect, cornerRadius, cornerRadius, rectangleMainPaint);
			canvas.drawRoundRect(badgeBorderRect, cornerRadius, cornerRadius, rectangleBorderBottomPaint);
			canvas.drawRoundRect(badgeRect, cornerRadius, cornerRadius, rectangleBorderPaint);

			// Draw text
			canvas.drawText(String.valueOf(value), badgeRect.centerX() - textWidth - density,
					badgeRect.centerY() + textHeight - density, textPaint);
		}
	}

	@Override
	public void setAlpha(int alpha) {
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
	}

	@Override
	public int getIntrinsicWidth() {
		if (badDevice) {
			return ICON_SIZE;
		} else {
			return super.getIntrinsicWidth();
		}
	}

	@Override
	public int getIntrinsicHeight() {
		if (badDevice) {
			return ICON_SIZE;
		} else {
			return super.getIntrinsicWidth();
		}
	}

	@Override
	public int getOpacity() {
		return PixelFormat.OPAQUE;
	}

	public void setValue(int value) {
		this.value = value;
		invalidateSelf();
	}
}
