package com.chess.ui.views.drawables.smart_button;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import com.chess.ui.views.drawables.ChatBadgeDrawable;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 29.09.13
 * Time: 20:56
 */
public class RectButtonBadgeDrawable extends RectButtonDrawable implements BadgeButtonFace {

	private ChatBadgeDrawable chatBadgeDrawable;
	private Resources resources;
	private boolean initialized;
	private int sideOffset;
	private int badgeOffset;
	private String badgeValue;

	public RectButtonBadgeDrawable() { }

	@Override
	void init(Resources resources) {
		super.init(resources);
		this.resources = resources;

		float density = resources.getDisplayMetrics().density;
		sideOffset = (int) (3.5f * density);
		badgeOffset = (int) (ChatBadgeDrawable.BADGE_SIZE * density) / 2;
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);

		Rect bounds = getBounds();
		if (!initialized) {
			chatBadgeDrawable = new ChatBadgeDrawable(resources, bounds);
			chatBadgeDrawable.setValue(badgeValue);
			initialized = true;
		}

		canvas.save();
		int xOffset = bounds.right - badgeOffset - sideOffset * 4;
		int yOffset = badgeOffset - sideOffset;
		canvas.translate(xOffset, yOffset);

		chatBadgeDrawable.draw(canvas);
		canvas.restore();
	}

	@Override
	protected void onBoundsChange(Rect bounds) {
		super.onBoundsChange(bounds);

		initialized = false;
	}

	@Override
	public void setBadgeValue(String badgeValue) {
		this.badgeValue = badgeValue;
		invalidateSelf();
	}
}
