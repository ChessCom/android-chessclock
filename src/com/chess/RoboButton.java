package com.chess;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;
import com.chess.ui.views.drawables.ButtonDrawable;
import com.chess.utilities.AppUtils;

import java.io.Serializable;

public class RoboButton extends Button implements Serializable {

	private static final long serialVersionUID = -7816685707888388856L;

	private String ttfName = "Bold";

	public RoboButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setupFont(attrs);
	}

	public RoboButton(Context context) {
		super(context);
	}

	public RoboButton(Context context, AttributeSet attrs) {
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
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), RoboTextView.MAIN_PATH + ttfName + ".ttf");
        setTypeface(font);

		if (attrs != null) {
			TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.RoboButton);
			if (array == null)
				return;
			try {
				if (!array.hasValue(R.styleable.RoboButton_btn_radius)) {
					return;
				}
			} finally {
				array.recycle();
			}
			ButtonDrawable background = new ButtonDrawable(getContext(), attrs);
			if (AppUtils.HONEYCOMB_PLUS_API) {
				setBackground(background);
			} else {
				setBackgroundDrawable(background);
			}
		}

    }

    public void setFont(String font) {
        ttfName = font;
        init(null);
    }
}
