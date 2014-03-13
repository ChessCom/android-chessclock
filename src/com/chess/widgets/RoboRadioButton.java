package com.chess.widgets;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RadioButton;
import com.chess.R;
import com.chess.ui.views.drawables.smart_button.ButtonDrawable;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.utilities.AppUtils;
import com.chess.utilities.FontsHelper;

public class RoboRadioButton extends RadioButton {

	private String ttfName = FontsHelper.DEFAULT_FONT;

	// flag for ide integration
	private boolean isInEditMode = false;

	public RoboRadioButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		// setting ide mode flag
		isInEditMode = isInEditMode();

		setupFont(context, attrs);
	}

	public RoboRadioButton(Context context) {
		super(context);

		// setting ide mode flag
		isInEditMode = isInEditMode();
	}

	public RoboRadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);

		// setting ide mode flag
		isInEditMode = isInEditMode();

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

		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		if (!isInEditMode) {
			setTypeface(FontsHelper.getInstance().getTypeFace(context, ttfName));
		}
		ButtonDrawableBuilder.setBackgroundToView(this, attrs);
	}

	public void setFont(String font) {
		ttfName = font;
		init(getContext(), null);
	}

	public void setDrawableStyle(int styleId) {
		ButtonDrawable buttonDrawable = ButtonDrawableBuilder.createDrawable(getContext(), styleId);
		if (AppUtils.JELLYBEAN_PLUS_API) {
			setBackground(buttonDrawable);
		} else {
			setBackgroundDrawable(buttonDrawable);
		}
	}
}
