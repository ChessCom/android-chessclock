package com.chess;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RadioButton;

public class RoboRadioButton extends RadioButton {
	private String ttfName = "Regular";

	public RoboRadioButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        setupFont(attrs);
	}

	public RoboRadioButton(Context context) {
		super(context);
	}

	public RoboRadioButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupFont(attrs);
    }

    private void setupFont(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.RobotoTextView);
		try {
			if (array.getString(R.styleable.RobotoTextView_ttf) != null) {
				ttfName = array.getString(R.styleable.RobotoTextView_ttf);
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

	@Override
	public void setTypeface(Typeface tf) {
		super.setTypeface(tf);
	}

}
