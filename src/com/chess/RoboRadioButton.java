package com.chess;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RadioButton;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;

public class RoboRadioButton extends RadioButton {
	private String ttfName = "Regular";

	public RoboRadioButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        setupFont(context, attrs);
	}

	public RoboRadioButton(Context context) {
		super(context);
	}

	public RoboRadioButton(Context context, AttributeSet attrs) {
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
		} finally {
			array.recycle();
		}

		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		if (!isInEditMode() && ttfName != null) {
			setTypeface(FontsHelper.getInstance().getTypeFace(context, ttfName));
		}
		ButtonDrawableBuilder.setBackgroundToView(this, attrs);
	}

	public void setFont(String font) {
		ttfName = font;
		init(getContext(), null);
	}

}
