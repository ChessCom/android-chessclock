package com.chess;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public class RoboAutoCompleteTextView extends AutoCompleteTextView {
	private String ttfName = "Regular";

	public RoboAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        setupFont(attrs);
	}

	public RoboAutoCompleteTextView(Context context) {
		super(context);
	}

	public RoboAutoCompleteTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
        setupFont(attrs);
    }

    private void setupFont(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.RoboTextView);
		try {
			if (array.getString(R.styleable.RoboTextView_ttf) != null) {
				ttfName = array.getString(R.styleable.RoboTextView_ttf);
			}
		} finally {
			array.recycle();
		}



//        final int N = array.getIndexCount();
//        for (int i = 0; i < N; i++) {
//            int attr = array.getIndex(i);
//            switch (attr) {
//                case R.styleable.RoboTextView_ttf: {
//                    ttfName = array.getString(i);
//                }
//                break;
//            }
//        }
        init();
    }

    private void init() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), RoboTextView.MAIN_PATH + ttfName + ".ttf");
        setTypeface(font);
    }

	public void setFont(String font) {
		ttfName = font;
		init();
	}
}
