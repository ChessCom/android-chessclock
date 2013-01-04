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
import android.widget.Button;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 03.01.13
 * Time: 13:45
 */
public class FacebookButton extends RoboButton {

	public static final int ONE = 0;
	public static final int TOP = 1;
	public static final int MID = 2;
	public static final int BOT = 3;
	public static float BORDER_OFFSET;
	public static float LINE_WIDTH;

	private Drawable icon;
	private Paint borderPaint;
	private boolean initialized;
	private int[] borderColors;
	private float density;

	public FacebookButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public FacebookButton(Context context) {
		super(context);
	}

	public FacebookButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		icon = context.getResources().getDrawable(R.drawable.facebook_icon);
		icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());

		density = context.getResources().getDisplayMetrics().density;


		borderColors = new int[4];
		borderColors[0] = context.getResources().getColor(R.color.any_button_stroke);
		borderColors[1] = context.getResources().getColor(R.color.f_emboss_top_left);
		borderColors[2] = 0xFF284160;
		borderColors[3] = 0xFF354c78;

		borderPaint = new Paint();
		borderPaint.setStrokeWidth(1);
		borderPaint.setStyle(Paint.Style.STROKE);

		BORDER_OFFSET = 5 * density;
		LINE_WIDTH = 0.5f * density;
	}

	@Override
	protected void onDraw(Canvas canvas) { // TODO use Picture?
		if (!initialized) {
			initImage(canvas);
		}

		for (int i = 0, cnt = borderColors.length; i < cnt; i++) {
			float left = getHeight() + i * LINE_WIDTH;
			float top = 0;
			float right = getHeight() + i * LINE_WIDTH;
			float bottom = 0;
			switch (i){
				case 0:
					top = BORDER_OFFSET;
					bottom = getHeight() - BORDER_OFFSET + LINE_WIDTH;
					break;
				case 1:
				case 3:
					top = BORDER_OFFSET - LINE_WIDTH;
					bottom = getHeight() - BORDER_OFFSET;
					break;
				case 2:
					top = BORDER_OFFSET - LINE_WIDTH * 2;
					bottom = getHeight() - BORDER_OFFSET - LINE_WIDTH;
					break;
			}

			borderPaint.setColor(borderColors[i]);
			canvas.drawLine(left, top, right, bottom, borderPaint);
		}

		// place image
		canvas.save();
		float imgCenterX = 0;
		float imgCenterY = BORDER_OFFSET;
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
