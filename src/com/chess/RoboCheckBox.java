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
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.RobotoTextView);

        final int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.RobotoTextView_ttf: {
                    ttfName = a.getString(i);
                }
                break;
            }
        }
        init();
    }

	private void init() {
		Typeface font = Typeface.createFromAsset(getContext().getAssets(), RoboTextView.MAIN_FONT + ttfName + ".ttf");
		setTypeface(font);
	}

}
