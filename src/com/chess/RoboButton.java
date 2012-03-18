package com.chess;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

public class RoboButton extends Button {
	private Context context;
	private String ttfName = "Regular";
//    private float density;

	public RoboButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setupFont(context, attrs);
	}

	public RoboButton(Context context) {
		super(context);
	}

	public RoboButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupFont(context, attrs);
	}

	private void setupFont(Context context, AttributeSet attrs) {
		this.context = context;


		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RobotoTextView);

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
		Typeface font = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-" + ttfName + ".ttf");
		setTypeface(font);
	}

	@Override
	public void setTypeface(Typeface tf) {
		super.setTypeface(tf);
	}

}
