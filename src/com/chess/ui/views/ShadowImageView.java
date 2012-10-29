package com.chess.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.widget.ImageView;

public class ShadowImageView extends ImageView {

	private Rect mRect;
	private Paint mPaint;
	private Paint mShadow;

	public ShadowImageView(Context context) {
		super(context);
		mRect = new Rect();
		mPaint = new Paint();

		mPaint.setAntiAlias(true);
		mPaint.setShadowLayer(2f, 1f, 1f, Color.BLACK);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Rect r = mRect;
		Paint paint = mPaint;

		// radius=10, y-offset=2, color=black 
		mShadow.setShadowLayer(10.0f, 0.0f, 2.0f, 0xFF000000);
		// in onDraw(Canvas) 
//		canvas.drawBitmap(bitmap, 0.0f, 0.0f, mShadow);		

		canvas.drawRect(r, paint);
		super.onDraw(canvas);
	}

	@Override
	protected void onMeasure(int w, int h) {
		super.onMeasure(w, h);
		int mH, mW;
		mW = getSuggestedMinimumWidth() < getMeasuredWidth() ? getMeasuredWidth()
				: getSuggestedMinimumWidth();
		mH = getSuggestedMinimumHeight() < getMeasuredHeight() ? getMeasuredHeight()
				: getSuggestedMinimumHeight();
		setMeasuredDimension(mW + 5, mH + 5);
	}
}
