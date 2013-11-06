package com.chess.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.CheckedTextView;
import com.chess.R;
import com.chess.utilities.FontsHelper;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.07.13
 * Time: 16:55
 */
public class RoboCheckedTextView extends CheckedTextView {

	private String ttfName = FontsHelper.DEFAULT_FONT;

	public RoboCheckedTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setupFont(context, attrs);
	}

	public RoboCheckedTextView(Context context) {
		super(context);
	}

	public RoboCheckedTextView(Context context, AttributeSet attrs) {
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
			setTypeface(FontsHelper.getInstance().getTypeFace(context, ttfName));
		}
	}

	public void setFont(String font) {
		ttfName = font;
		init(getContext());
	}

}
