package com.chess.ui.views;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.02.13
 * Time: 19:28
 */
public class ChartView extends View {

	private static final float GRAPH_HEIGHT = 200;

	private Paint mPaint;
	private Path mPath;
	private PathEffect[] mEffects;
	private int[] mColors;
	private float mPhase;
	private float[] data;

	public ChartView(Context context) {
		super(context);
		init(context);
	}

	public ChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		data = new float[40];
		for (int i1 = 0; i1 < data.length; i1++) {
			data[i1] = ((float) Math.random() * 35);

		}


		setFocusable(true);
		setFocusableInTouchMode(true);

		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//		mPaint.setStyle(Paint.Style.STROKE);
//		mPaint.setStrokeWidth(6);

		mPath = makeFollowPath(data);

		mEffects = new PathEffect[6];

		mColors = new int[]{Color.BLACK,
				0xFF88b2cc,
				Color.BLUE,
				Color.GREEN, Color.MAGENTA, Color.BLACK
		};
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.WHITE);

		RectF bounds = new RectF();
		mPath.computeBounds(bounds, false);

		canvas.translate(10 - bounds.left, 10 - bounds.top);

		makeEffects(mEffects, mPhase);
		mPhase += 1;
		invalidate();

//		for (int i = 0; i < mEffects.length; i++) {
//			mPaint.setPathEffect(mEffects[i]);
//			mPaint.setColor(mColors[i]);
//			canvas.drawPath(mPath, mPaint);
//			canvas.translate(0, 28);
//		}

		drawPaths(canvas);
	}

	private void drawPaths(Canvas canvas) {
//		mPaint.setPathEffect(mEffects[1]);
//		mPaint.setColor(mColors[1]);
		mPaint.setAntiAlias(true);
		mPaint.setShader(new LinearGradient(0, 0, 0, GRAPH_HEIGHT, 0xffe3f3fb, 0xffc8e7f7, Shader.TileMode.CLAMP));
		canvas.drawPath(mPath, mPaint);


		int strokeWidth = 3;     // TODO

		Paint strokePaint = new Paint();
		strokePaint.setAntiAlias(true);
		strokePaint.setStyle(Paint.Style.STROKE);
		strokePaint.setStrokeWidth(strokeWidth);
		strokePaint.setColor(0xFF88b2cc);

		canvas.drawPath(mPath, strokePaint);
		canvas.translate(0, 28);
	}

	private static void makeEffects(PathEffect[] e, float phase) {
		e[1] = new CornerPathEffect(2);

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_DPAD_CENTER:
				mPath = makeFollowPath(data);
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}



	private static Path makeFollowPath(float[] data) {
		Path path = new Path();
		path.moveTo(0, 0);
		for (int i = 1; i < data.length; i++) {
			path.lineTo(i * 10, data[i]);
		}
		path.lineTo(10 * (data.length - 1), GRAPH_HEIGHT);
		path.lineTo(0, GRAPH_HEIGHT);
		path.close();

		return path;
	}

//	private static Path makePathDash() {
//		Path p = new Path();
//		p.moveTo(4, 0);
//		p.lineTo(0, -4);
//		p.lineTo(8, -4);
//		p.lineTo(12, 0);
//		p.lineTo(8, 4);
//		p.lineTo(0, 4);
//		return p;
//	}
}
