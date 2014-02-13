package com.chess.widgets;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;
import com.chess.R;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.utilities.FontsHelper;

import java.io.Serializable;

public class RoboTextView extends TextView implements Serializable {

	private String ttfName = FontsHelper.DEFAULT_FONT;

	public RoboTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setupFont(context, attrs);
	}

	public RoboTextView(Context context) {
		super(context);
	}

	public RoboTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupFont(context, attrs);
	}

	private void setupFont(Context context, AttributeSet attrs) {
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RoboTextView);
		if (array == null) {
			return;
		}
		try {
			if (array.hasValue(R.styleable.RoboTextView_ttf)) {
				ttfName = array.getString(R.styleable.RoboTextView_ttf);
			}
			if (array.hasValue(R.styleable.RoboTextView_themeColor)) {
				boolean useThemeColor = array.getBoolean(R.styleable.RoboTextView_themeColor, false);
				if (useThemeColor) {
					setTextColor(FontsHelper.getInstance().getThemeColorStateList(context, false));
				}
			}
		} finally {
			array.recycle();
		}

		ButtonDrawableBuilder.setBackgroundToView(this, attrs);
		init(context, ttfName);
	}

	private void init(Context context, String ttfName) {
		setTypeface(FontsHelper.getInstance().getTypeFace(context, ttfName));
	}

	public void setFont(String font) {
		init(getContext(), font);
	}

}
