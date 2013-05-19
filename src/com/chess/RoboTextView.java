package com.chess;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import java.io.Serializable;

public class RoboTextView extends TextView implements Serializable {

	private static final long serialVersionUID = -2417945858405913303L;
	public static final String MAIN_PATH = "fonts/custom-"; // Default font is Trebuchet MS
	public static final String DEFAULT_FONT = "Regular";
	public static final String BOLD_FONT = "Bold";
	public static final String ICON_FONT = "Icon"; // Chess.com Glyph
	public static final String ITALIC_FONT = "Italic";
	public static final String HELV_NEUE_FONT = "Neue"; // HelveticaNeue
	public static final String HELV_NEUE_BOLD_FONT = "NeueBold"; // HelveticaNeue

	private String ttfName = DEFAULT_FONT;

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
			Typeface font = Typeface.createFromAsset(context.getAssets(), MAIN_PATH + ttfName + ".ttf");
			setTypeface(font);
		}
    }

	public void setFont(String font) {
		ttfName = font;
		init(getContext());
	}

}
