package com.chess.views;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

public class CenteredButton extends FrameLayout {

	static final String TAG = "CenteredButton";
	private final int DEFAULT_WIDTH = 100;
	private final int DEFAULT_HEIGHT = 100;
	private Button button;
	private Drawable drawable;

	int mRadius;
	int mAnrType;
	CharSequence mLegend;

	static final int ANR_NONE = 0;
	static final int ANR_SHADOW = 1;
	static final int ANR_DROP = 2;

	void sleepSixSeconds() {
		// hang forever; good for producing ANRs
		long start = SystemClock.uptimeMillis();
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		} while (SystemClock.uptimeMillis() < start + 6000);
	}

	// Shadow builder that can ANR if desired
	class ANRShadowBuilder extends DragShadowBuilder {
		boolean mDoAnr;

		public ANRShadowBuilder(View view, boolean doAnr) {
			super(view);
			mDoAnr = doAnr;
		}

		@Override
		public void onDrawShadow(Canvas canvas) {
			if (mDoAnr) {
				sleepSixSeconds();
			}
			super.onDrawShadow(canvas);
		}
	}

	public CenteredButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
//		attrs.
		init(context, attrs);
	}

	public CenteredButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public CenteredButton(Context context) {
		super(context);
//		init(context);
	}

	private void init(Context context, AttributeSet attrs) {
		// look up any layout-defined attributes
//		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CenteredButton);

//		final int N = a.getIndexCount();
//		for (int i = 0; i < N; i++) {
//			int attr = a.getIndex(i);
//			switch (attr) {
//			case R.styleable.CenteredButton_buttonDrawable: {
//				drawable = a.getDrawable(i);
////					mRadius = a.getDimensionPixelSize(attr, 0);
//			}
//				break;
//
//			case R.styleable.CenteredButton_buttonText: {
//				mLegend = a.getText(attr);
//			}
//				break;
//
//			}
//		}

		Log.i(TAG, "DraggableDot @ " + this + " : radius=" + mRadius + " legend='" + mLegend + "' anr=" + mAnrType);

		setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				ClipData data = ClipData.newPlainText("dot", "Dot : " + v.toString());
				v.startDrag(data, new ANRShadowBuilder(v, mAnrType == ANR_SHADOW), v, 0);
				return true;
			}
		});

		button = new Button(getContext());
		button.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
		button.setText(mLegend);
		button.setTextColor(Color.RED);
		int left = 0;
		int right = 50;
		int top = 0;
		int bottom = 50;
		Rect bounds = new Rect(left, top, right, bottom);
		drawable.setBounds(bounds);
		button.setCompoundDrawables(null, drawable, null, null);
		button.setBackgroundColor(Color.BLACK);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		addView(button, params);
//		setBackgroundColor(Color.WHITE);
	}

	public void setButton(Button button) {
		this.button = button;
		button.setTouchDelegate(this.getTouchDelegate());
//		button.get
//		findViewById(R.id.meter_Btn).setTouchDelegate(findViewById(R.id.linearLayout2).getTouchDelegate());
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (button == null)
			return;

		button.draw(canvas);
		super.dispatchDraw(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int height = measureParam(heightMeasureSpec, DEFAULT_HEIGHT);
		int width = measureParam(widthMeasureSpec, DEFAULT_WIDTH);
		setMeasuredDimension(width, height);
	}

	private int measureParam(int valueMeasureSpec, int value) {
		switch (View.MeasureSpec.getMode(valueMeasureSpec)) {
		case MeasureSpec.AT_MOST:
			return MeasureSpec.getSize(valueMeasureSpec);
		case MeasureSpec.EXACTLY:
			return Math.min(value, MeasureSpec.getSize(valueMeasureSpec));
		default:
		case MeasureSpec.UNSPECIFIED:
			return value;
		}
	}
}
