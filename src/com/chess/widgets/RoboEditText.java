package com.chess.widgets;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.EditText;
import com.chess.R;
import com.chess.utilities.FontsHelper;

import java.io.Serializable;

public class RoboEditText extends EditText implements Serializable {

	private static final long serialVersionUID = 8485060880629295457L;

	private String ttfName = "Regular";

	public RoboEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
        setupFont(context, attrs);
	}

	public RoboEditText(Context context) {
		super(context);
	}

	public RoboEditText(Context context, AttributeSet attrs) {
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
		if (!isInEditMode() && ttfName != null) {
			setTypeface(FontsHelper.getInstance().getTypeFace(context, ttfName));
		}
	}

	public void setFont(String font) {
		ttfName = font;
		init(getContext());
	}
}
