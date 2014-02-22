package com.chess.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.chess.R;
import com.chess.ui.views.drawables.smart_button.ButtonDrawable;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 20.05.13
 * Time: 23:18
 */
public class RelLayout extends RelativeLayout {
	private Drawable mForegroundSelector;

	public RelLayout(Context context) {
		super(context);
	}

	public RelLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public RelLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		ButtonDrawableBuilder.setBackgroundToView(this, attrs);

		setForeground(context.getResources().getDrawable(R.drawable.dark_list_item_selector));
	}

	public void setDrawableStyle(int styleId) {
		ButtonDrawable buttonDrawable = ButtonDrawableBuilder.createDrawable(getContext(), styleId);
		if (AppUtils.JELLYBEAN_PLUS_API) {
			setBackground(buttonDrawable);
		} else {
			setBackgroundDrawable(buttonDrawable);
		}
	}

	@Override
	protected void drawableStateChanged() {
		super.drawableStateChanged();

		if (mForegroundSelector != null && mForegroundSelector.isStateful()) {
			mForegroundSelector.setState(getDrawableState());
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		if (mForegroundSelector != null) {
			mForegroundSelector.setBounds(0, 0, w, h);
		}
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		if (mForegroundSelector != null) {
			mForegroundSelector.draw(canvas);
		}
	}

	@Override
	protected boolean verifyDrawable(Drawable who) {
		return super.verifyDrawable(who) || (who == mForegroundSelector);
	}

	@TargetApi(11)
	@Override
	public void jumpDrawablesToCurrentState() {
		super.jumpDrawablesToCurrentState();
		if (mForegroundSelector != null) {
			mForegroundSelector.jumpToCurrentState();
		}
	}

	public void setForeground(Drawable drawable) {
		if (mForegroundSelector != drawable) {
			if (mForegroundSelector != null) {
				mForegroundSelector.setCallback(null);
				unscheduleDrawable(mForegroundSelector);
			}

			mForegroundSelector = drawable;

			if (drawable != null) {
				setWillNotDraw(false);
				drawable.setCallback(this);
				if (drawable.isStateful()) {
					drawable.setState(getDrawableState());
				}
			} else {
				setWillNotDraw(true);
			}
			requestLayout();
			invalidate();
		}
	}
}
