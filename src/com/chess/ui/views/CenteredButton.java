package com.chess.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import com.chess.R;
import com.chess.RoboButton;

public class CenteredButton extends FrameLayout implements View.OnTouchListener {

	private static final int DEFAULT_WIDTH = 100;
	private static final int DEFAULT_HEIGHT = 100;
	private RoboButton button;
	private Drawable drawable;

	CharSequence buttonText;

	private int mMaxChildWidth = 0;
	private int mMaxChildHeight = 0;
	private float density;

	public CenteredButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initFromAttr(context, attrs);
	}

	public CenteredButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		initFromAttr(context, attrs);
	}

	public CenteredButton(Context context) {
		super(context);
	}

	private void initFromAttr(Context context, AttributeSet attrs) {
		// look up any layout-defined attributes
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CenteredButton);

		// Get the screen's density scale
		density = getResources().getDisplayMetrics().density;
		// Convert the dps to pixels, based on density scale

		final int N = a.getIndexCount();
		for (int i = 0; i < N; i++) {
			int attr = a.getIndex(i);
			switch (attr) {
				case R.styleable.CenteredButton_buttonDrawable: {
					drawable = a.getDrawable(i);
				}
				break;
				case R.styleable.CenteredButton_buttonText: {
					buttonText = a.getText(attr);
				}
				break;
			}
		}

		button = new RoboButton(getContext());
        button.setFont("Bold");
		LayoutParams buttonParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		button.setLayoutParams(buttonParams);
		button.setText(buttonText);
		button.setTextAppearance(getContext(), R.style.DashboardItemText);
		float shadowRadius = 1 * density + 0.5f;
		float shadowDx = 0 * density + 0.5f;
		float shadowDy = 0 * density + 0.5f;
		button.setShadowLayer(shadowRadius, shadowDx, shadowDy, Color.BLACK);
		button.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
		button.setBackgroundColor(Color.TRANSPARENT);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.gravity = Gravity.CENTER;

		addView(button, params);
		this.setTouchDelegate(button.getTouchDelegate());
		button.setClickable(true);
		button.setOnTouchListener(this);
		setClickable(true);
	}


	@Override
	public void setPressed(boolean pressed) {
		super.setPressed(pressed);
//		Log.d("TEST", "setPressed -> pressed = " + pressed);
		button.setPressed(pressed);
	}

	/**
	 * Implement this method to handle touch screen motion events.
	 *
	 * @param event The motion event.
	 * @return True if the event was handled, false otherwise.
	 */
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
//				Log.d("TEST", "onTouchEvent -> ACTION_DOWN");
				button.performClick();
				setPressed(true);
				button.refreshDrawableState();
				break;
			case MotionEvent.ACTION_UP:
//				Log.d("TEST", "onTouchEvent -> ACTION_UP");
				button.setPressed(false);
				button.refreshDrawableState();
				break;
			case MotionEvent.ACTION_MOVE:
//				Log.d("TEST", "onTouchEvent -> ACTION_MOVE");
				button.setPressed(true);
				button.refreshDrawableState();

				break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		switch (motionEvent.getAction()) {
			case MotionEvent.ACTION_DOWN:
				Log.d("TEST", "onTouch -> ACTION_DOWN");
				setPressed(true);
				refreshDrawableState();
				performClick();

				break;
			case MotionEvent.ACTION_UP:
				Log.d("TEST", "onTouch -> ACTION_UP");
				setPressed(false);
				refreshDrawableState();
				break;
		}
		return false;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (button == null)
			return;
		super.dispatchDraw(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		int height = measureParam(heightMeasureSpec, (int) (DEFAULT_HEIGHT * density));
		int width = measureParam(widthMeasureSpec, (int) (DEFAULT_WIDTH * density));

		int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec),
				MeasureSpec.AT_MOST);
		int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec),
				MeasureSpec.AT_MOST);

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() == GONE) {
				continue;
			}

			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

			mMaxChildWidth = Math.max(mMaxChildWidth, child.getMeasuredWidth());
			mMaxChildHeight = Math.max(mMaxChildHeight, child.getMeasuredHeight());
		}

		// Measure again for each child to be exactly the same size.

		childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxChildWidth, MeasureSpec.EXACTLY);
		childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxChildHeight, MeasureSpec.EXACTLY);

		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() == GONE) {
				continue;
			}

			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
		}

		setMeasuredDimension(width, height);
	}

	private int measureParam(int valueMeasureSpec, int value) {
		switch (MeasureSpec.getMode(valueMeasureSpec)) {
			case MeasureSpec.EXACTLY:
				return MeasureSpec.getSize(valueMeasureSpec);
			case MeasureSpec.AT_MOST:
				return Math.min(value, MeasureSpec.getSize(valueMeasureSpec));
			default:
			case MeasureSpec.UNSPECIFIED:
				return value;
		}
	}

}
