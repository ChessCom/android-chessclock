package com.chess;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import java.io.Serializable;

public class RoboEditText extends EditText implements Serializable {

	private static final long serialVersionUID = 8485060880629295457L;

	private String ttfName = "Regular";

	public RoboEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        setupFont(attrs);
	}

	public RoboEditText(Context context) {
		super(context);
	}

	public RoboEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
        setupFont(attrs);
    }

    private void setupFont(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.RobotoTextView);
		if (array.getString(R.styleable.RobotoTextView_ttf) != null) {
			ttfName = array.getString(R.styleable.RobotoTextView_ttf);
		}

//        final int N = array.getIndexCount();
//        for (int i = 0; i < N; i++) {
//            int attr = array.getIndex(i);
//            switch (attr) {
//                case R.styleable.RobotoTextView_ttf: {
//                    ttfName = array.getString(i);
//                }
//                break;
//            }
//        }
        init();
    }

    private void init() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), RoboTextView.MAIN_FONT + ttfName + ".ttf");
        setTypeface(font);
    }
}
