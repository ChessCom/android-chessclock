package com.chess.ui.views.drawables;

import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import com.chess.R;
import com.chess.utilities.AppUtils;
import com.chess.utilities.FontsHelper;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 29.09.13
 * Time: 20:43
 */
public class ChatBadgeDrawable extends Drawable {

	public static final int BADGE_SIZE = 17;

	private static final int ICON_SIZE = 48;
	private final boolean badDevice;
	private Paint rectangleMainPaint;
	private Paint rectangleBorderPaint;
	private Paint textPaint;

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
	private Rect parentBounds;
	private String value;

	public ChatBadgeDrawable(Resources resources, Rect parentBounds) {
		this.parentBounds = parentBounds;


		badDevice = isBadDevice(resources);

//		if (badDevice) {
//			this.icon.setBounds(0, 0, icon.getIntrinsicWidth() + 5, icon.getIntrinsicHeight() + 5);
//		} else {
//			this.icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
//		}
		rectangleSize = 17;

		density = resources.getDisplayMetrics().density;

		int border1Color = resources.getColor(R.color.action_badge_border_1);
		int borderMainColor1 = resources.getColor(R.color.action_badge_main_1);
		int borderMainColor2 = resources.getColor(R.color.action_badge_main_2);
		int borderBorderTopColor = resources.getColor(R.color.action_badge_border_top);
		int borderBorderBottomColor = resources.getColor(R.color.action_badge_border_bottom);
		int main1 = resources.getColor(R.color.action_badge_main_1);
		Shader mainShader = new LinearGradient(0, 0, rectangleSize, rectangleSize, borderMainColor1, borderMainColor2, Shader.TileMode.CLAMP);
		Shader borderShader = new LinearGradient(0, 0, rectangleSize, rectangleSize, borderBorderTopColor, borderBorderBottomColor, Shader.TileMode.CLAMP);

		rectangleMainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		rectangleMainPaint.setDither(true);
		rectangleMainPaint.setColor(main1);
		rectangleMainPaint.setStrokeWidth(0.0f);
		rectangleMainPaint.setStyle(Paint.Style.FILL);
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

		textSize = 13 * density;
		textWidth = 3 * density;
		textHeight = 5 * density;

		textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setColor(Color.WHITE);
		textPaint.setTextSize(textSize);
		textPaint.setTypeface(FontsHelper.getInstance().getTypeFace(resources, FontsHelper.BOLD_FONT));

		badgeRect = new RectF();
		badgeBorderRect = new RectF();

		cornerRadius = 4 * density;

		// Used only for PRE-ICE
		viewHeight = resources.getDimension(R.dimen.actionbar_compat_height);
		viewWidth = resources.getDimension(R.dimen.actionbar_compat_button_width);
	}

	private boolean isBadDevice(Resources resources) {
		DisplayMetrics displayMetrics = resources.getDisplayMetrics();
		return (displayMetrics.density == 1.0f || displayMetrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM)
				&& (displayMetrics.heightPixels <= 480) && AppUtils.HONEYCOMB_PLUS_API;
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

			float iconX0 = parentBounds.right / 2;
			float iconY0 = parentBounds.bottom / 2;

			canvas.save();
			if (badDevice) {
				float xx = parentBounds.right;
				canvas.translate((ICON_SIZE - xx) / 2 - 4, (ICON_SIZE - xx) / 2 - 4); // hate this stupid hardcode :(
			} else {
				canvas.translate(iconX0, iconY0);
			}

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

			float iconX0 = (viewWidth - parentBounds.right) / 2;
			float iconY0 = (viewHeight - parentBounds.bottom) / 2;
			canvas.save();
			canvas.translate(iconX0, iconY0);
			canvas.restore();
		}

		canvas.drawRoundRect(badgeRect, cornerRadius, cornerRadius, rectangleMainPaint);
		canvas.drawRoundRect(badgeBorderRect, cornerRadius, cornerRadius, rectangleBorderBottomPaint);
		canvas.drawRoundRect(badgeRect, cornerRadius, cornerRadius, rectangleBorderPaint);

		// Draw text
		canvas.drawText(value, badgeRect.centerX() - textWidth,
				badgeRect.centerY() + textHeight - density, textPaint);
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

	public void setValue(String value) {
		this.value = value;
		invalidateSelf();
	}
}