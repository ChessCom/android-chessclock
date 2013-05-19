package com.chess;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.ToggleButton;

import java.io.Serializable;

public class RoboToggleButton extends ToggleButton implements Serializable {

	private static final long serialVersionUID = -7816685707888388856L;

	private String ttfName = "Bold";

	public RoboToggleButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setupFont(context, attrs);
	}

	public RoboToggleButton(Context context) {
		super(context);
	}

	public RoboToggleButton(Context context, AttributeSet attrs) {
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

		init(context);
	}

	private void init(Context context) {
		if (!isInEditMode()) {
			Typeface font = Typeface.createFromAsset(context.getAssets(), RoboTextView.MAIN_PATH + ttfName + ".ttf");
			setTypeface(font);
		}
	}

	public void setFont(String font) {
		ttfName = font;
		init(getContext());
	}
}
