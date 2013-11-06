package com.chess.ui.views;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import com.chess.R;
import com.chess.widgets.RoboButton;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;

public class CenteredButton extends FrameLayout implements View.OnTouchListener {

	private static final int DEFAULT_WIDTH = 100;
	private static final int DEFAULT_HEIGHT = 100;
	private RoboButton button;
	private Drawable drawable;

	CharSequence buttonText;

	private int mMaxChildWidth = 0;
	private int mMaxChildHeight = 0;
	private float density;
	private ObjectAnimator flipFirstHalf;

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

		initFlipAnimation();
	}


	@Override
	public void setPressed(boolean pressed) {
		button.setPressed(pressed);
		button.refreshDrawableState();
		super.setPressed(pressed);
		refreshDrawableState();
	}

	/**
	 * Implement this method to handle touch screen motion events.
	 *
	 * @param event The motion event.
	 * @return True if the event was handled, false otherwise.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				setPressed(true);

				flipIt();
				break;
			case MotionEvent.ACTION_UP:
				setPressed(false);

				break;
			case MotionEvent.ACTION_MOVE:
				setPressed(true);

				break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		switch (motionEvent.getAction()) {
			case MotionEvent.ACTION_DOWN:
				setPressed(true);

				flipIt();

				break;
			case MotionEvent.ACTION_UP:
				setPressed(false);
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

	private Interpolator accelerator = new AccelerateInterpolator();
	private Interpolator decelerator = new DecelerateInterpolator();
	private static final int DURATION = 100;

	private void initFlipAnimation() {
		final View animationView = this;

		flipFirstHalf = ObjectAnimator.ofFloat(animationView,"rotationY", 0f, 90f);
		flipFirstHalf.setDuration(DURATION);
		flipFirstHalf.setInterpolator(accelerator);

		final ObjectAnimator flipSecondHalf = ObjectAnimator.ofFloat(animationView,"rotationY", -90f, 0f);
		flipSecondHalf.setDuration(DURATION);
		flipSecondHalf.setInterpolator(decelerator);

		flipFirstHalf.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator anim) {
				flipSecondHalf.start();
			}
		});

		flipSecondHalf.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator anim) {
				performClick();
			}
		});
	}


	private void flipIt() {
		flipFirstHalf.start();
	}


}
