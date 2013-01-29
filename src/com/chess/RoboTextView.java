package com.chess;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import java.io.Serializable;

public class RoboTextView extends TextView implements Serializable {

	private static final long serialVersionUID = -2417945858405913303L;
	public static final String MAIN_PATH = "fonts/trebuc-";
	public static final String DEFAULT_FONT = "Regular";
	public static final String BOLD_FONT = "Bold";
	public static final String ROBOTO_BOLD_FONT = "RobotoBold";
	public static final String ROBOTO_REGULAR_FONT = "RobotoRegular";
	public static final String HELV_NEUE_FONT = "HelveticaNeue";

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
		try {
			if (array.getString(R.styleable.RobotoTextView_ttf) != null) {
				ttfName = array.getString(R.styleable.RobotoTextView_ttf);
				Log.d("TEST", "set font " + ttfName);
			}
		} finally {
			array.recycle();
		}

        init();
    }

    private void init() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), MAIN_PATH + ttfName + ".ttf");
        setTypeface(font);
    }

	public void setFont(String font) {
		ttfName = font;
		init();
	}

}
