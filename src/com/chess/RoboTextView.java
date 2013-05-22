package com.chess;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TextView;

import java.io.Serializable;

public class RoboTextView extends TextView implements Serializable {

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
		String ttfName = null;
		try {
			if (array.hasValue(R.styleable.RoboTextView_ttf)) {
				ttfName = array.getString(R.styleable.RoboTextView_ttf);
			}
		} finally {
			array.recycle();
		}

        init(context, ttfName);
    }

    private void init(Context context, String ttfName) {
		if (!isInEditMode() && ttfName != null) {
			setTypeface(FontsHelper.getInstance().getTypeFace(context, ttfName));
		}
    }

	public void setFont(String font) {
		init(getContext(), font);
	}

}
