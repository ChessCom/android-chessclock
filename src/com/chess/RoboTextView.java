package com.chess;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import java.io.Serializable;

public class RoboTextView extends TextView implements Serializable {

	private static final long serialVersionUID = -2417945858405913303L;
	public static final String MAIN_PATH = "fonts/trebuc-";
	public static final String DEFAULT_FONT = "Regular";

	private String ttfName = DEFAULT_FONT;

	public RoboTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        setupFont(attrs);
	}

	public RoboTextView(Context context) {
		super(context);
	}

	public RoboTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
        setupFont(attrs);
    }

    private void setupFont(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.RobotoTextView);
		if (array.getString(R.styleable.RobotoTextView_ttf) != null) {
			ttfName = array.getString(R.styleable.RobotoTextView_ttf);
		}

        init();
    }

    private void init() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), MAIN_PATH + ttfName + ".ttf");
        setTypeface(font);
    }

	@Override
	public void setTypeface(Typeface tf) {
		super.setTypeface(tf);
	}


}
