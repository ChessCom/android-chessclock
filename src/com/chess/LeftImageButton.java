package com.chess;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.Button;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 03.01.13
 * Time: 13:45
 */
public class LeftImageButton extends Button {

	public static float BORDER_OFFSET;
	public static float LINE_WIDTH;

	private Drawable icon;
	private Paint borderPaint;
	private boolean initialized;
	private int[] borderColors;
	private int imageWidth;

	public LeftImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public LeftImageButton(Context context) {
		super(context);
	}

	public LeftImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		float density = context.getResources().getDisplayMetrics().density;
		int densityDpi = context.getResources().getDisplayMetrics().densityDpi;

		// back for image
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LeftImageEditText);

		final int N = a.getIndexCount();
		for (int i = 0; i < N; i++) {
			int attr = a.getIndex(i);
			switch (attr) {
				case R.styleable.LeftImageEditText_leftImage:
					icon = a.getDrawable(i);
					break;
			}
		}

		imageWidth = icon.getIntrinsicWidth();
		int imageHeight = icon.getIntrinsicHeight();
		icon.setBounds(0, 0, imageWidth, imageHeight);

		borderColors = new int[2];
		borderColors[0] = 0xFF423f3a; // TODO place in colors
		borderColors[1] = context.getResources().getColor(R.color.any_button_stroke);

		borderPaint = new Paint();
		borderPaint.setStrokeWidth(1);
		borderPaint.setStyle(Paint.Style.STROKE);

		float borderOffset = 1.5f;
		float lineWidth = 0.5f;
		if (densityDpi <= DisplayMetrics.DENSITY_LOW) {
			lineWidth = 0.5f;
			borderOffset = 0.5f;
		}
		BORDER_OFFSET = borderOffset * density;
		LINE_WIDTH = lineWidth * density;
	}

	@Override
	protected void onDraw(Canvas canvas) { // TODO use Picture?
		if (!initialized) {
			initImage(canvas);
		}

		int height = getHeight();
		for (int i = 0, cnt = borderColors.length; i < cnt; i++) {
			float left = height + i * LINE_WIDTH;
			float top = 0;
			float right = height + i * LINE_WIDTH;
			float bottom = 0;
			switch (i){
				case 0:
					top = BORDER_OFFSET - LINE_WIDTH * 2;
					bottom = height - BORDER_OFFSET + LINE_WIDTH;
					break;
				case 1:
					top = BORDER_OFFSET - LINE_WIDTH;
					bottom = height - BORDER_OFFSET;
					break;
			}

			borderPaint.setColor(borderColors[i]);
			canvas.drawLine(left, top, right, bottom, borderPaint);
		}

		// place image
		canvas.save();
		float imgCenterX = BORDER_OFFSET/2 + (height - imageWidth)/2;
		float imgCenterY = (height - imageWidth)/2;
		canvas.translate(imgCenterX, imgCenterY);
		icon.draw(canvas);
		canvas.restore();

		// place additional clickable element
		canvas.translate(BORDER_OFFSET *4, 0);
		super.onDraw(canvas);
	}

	private void initImage(Canvas canvas) {

		initialized = true;

	}
}
