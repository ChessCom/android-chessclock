package com.chess;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import com.chess.ui.views.drawables.smart_button.ButtonDrawable;
import com.chess.ui.views.drawables.smart_button.RectButtonDrawable;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 20.05.13
 * Time: 23:18
 */
public class RelLayout extends RelativeLayout {

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

		if (attrs != null) {
			TypedArray array =context.obtainStyledAttributes(attrs, R.styleable.RoboButton);
			if (array == null) {
				return;
			}
			boolean isRect = false;
			try {
				if (!array.hasValue(R.styleable.RoboButton_btn_is_solid)) {
					return;
				}
				isRect = array.getBoolean(R.styleable.RoboButton_btn_is_rect, false);
			} finally {
				array.recycle();
			}

			if (isRect) {
				RectButtonDrawable background = new RectButtonDrawable(getContext(), attrs);
				if (AppUtils.HONEYCOMB_PLUS_API) {
					setBackground(background);
				} else {
					setBackgroundDrawable(background);
				}
			} else {
				ButtonDrawable background = new ButtonDrawable(getContext(), attrs);
				if (AppUtils.HONEYCOMB_PLUS_API) {
					setBackground(background);
				} else {
					setBackgroundDrawable(background);
				}
			}
		}
	}
}
