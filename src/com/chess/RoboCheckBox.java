package com.chess;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.CheckBox;

public class RoboCheckBox extends CheckBox {
	private String ttfName = "Regular";

	public RoboCheckBox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        setupFont(attrs);
	}

	public RoboCheckBox(Context context) {
		super(context);
	}

	public RoboCheckBox(Context context, AttributeSet attrs) {
		super(context, attrs);
        setupFont(attrs);
	}

    private void setupFont(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.RoboTextView);
		try {
			if (array.hasValue(R.styleable.RoboTextView_ttf)) {
				ttfName = array.getString(R.styleable.RoboTextView_ttf);
			}
		} finally {
			array.recycle();
		}

        init();
    }

	private void init() {
		Typeface font = Typeface.createFromAsset(getContext().getAssets(), RoboTextView.MAIN_PATH + ttfName + ".ttf");
		setTypeface(font);
	}

}
