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

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 03.01.13
 * Time: 13:45
 */
public class LeftImageEditText extends RoboEditText {

	public static final int ONE = 0;
	public static final int TOP = 1;
	public static final int MID = 2;
	public static final int BOT = 3;

	public static float BORDER_OFFSET = 2.0f;

	private Drawable icon;
	private int imageWidth;
	private boolean initialized;
	private ShapeDrawable backForImage;
	private int roundMode;
	private float density;
	private Paint linePaint;
	private int lineYStop;
	private int lineYStart;
	private int lineXStop;
	private int lineXStart;


	public LeftImageEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public LeftImageEditText(Context context) {
		super(context);
	}

	public LeftImageEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		density = context.getResources().getDisplayMetrics().density;

		TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.EnhancedField);
		roundMode = array.getInteger(R.styleable.EnhancedField_round_mode, ONE);
		// back for image
		int color;
		color = array.getInteger(R.styleable.EnhancedField_color, Color.WHITE);
		icon = array.getDrawable(R.styleable.EnhancedField_leftImage);
		// back for image

//		int color = Color.WHITE;
//
//		final int N = array.getIndexCount();
//		for (int i = 0; i < N; i++) {
//			int attr = array.getIndex(i);
//			switch (attr) {
//				case R.styleable.EnhancedField_round_mode:
//					roundMode = array.getInteger(i, ONE);
//					break;
//				case R.styleable.EnhancedField_color:
//					color = array.getInteger(i, Color.WHITE);
//					break;
//				case R.styleable.EnhancedField_leftImage:
//					icon = array.getDrawable(i);
//					break;
//			}
//		}

		imageWidth = icon.getIntrinsicWidth();
		int imageHeight = icon.getIntrinsicHeight();
		icon.setBounds(0, 0, imageWidth, imageHeight);

		float radius = context.getResources().getDimension(R.dimen.new_round_button_radius);
		float[] outerR;
		switch (roundMode) {
			case ONE:
				outerR = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};
				break;
			case TOP:
				outerR = new float[]{radius, radius, 0, 0, 0, 0, 0, 0};
				break;
			case MID:
				outerR = new float[]{0, 0, 0, 0, 0, 0, 0, 0};
				break;
			case BOT:
				outerR = new float[]{0, 0, 0, 0, 0, 0, radius, radius};
				break;
			default:
				outerR = new float[]{radius, radius, radius, radius, 0, 0, 0, 0};
				break;
		}
		backForImage = new ShapeDrawable(new RoundRectShape(outerR, null, null));
		backForImage.getPaint().setColor(color);

		linePaint = new Paint();
		linePaint.setColor(context.getResources().getColor(R.color.light_grey_border));
		linePaint.setStrokeWidth(1);
		linePaint.setStyle(Paint.Style.STROKE);
	}

	@Override
	protected void onDraw(Canvas canvas) { // TODO use Picture?
		if (!initialized) {
			initImage();
		}

		int height = getHeight();

		backForImage.draw(canvas);

		// place image
		canvas.save();
		float imgCenterX = (height - imageWidth)/2;
		float imgCenterY = (height - imageWidth)/2;
		canvas.translate(imgCenterX, imgCenterY);
		icon.draw(canvas);
		canvas.restore();

		if (roundMode != ONE) {
			canvas.drawLine(lineXStart, lineYStart, lineXStop, lineYStop, linePaint);
		}
		if (roundMode == MID) {
			canvas.drawLine(lineXStart, 0, lineXStop, 0, linePaint);
		}

		// place additional clickable element
		canvas.translate(getHeight() + BORDER_OFFSET, - BORDER_OFFSET);
		super.onDraw(canvas);
	}

	private void initImage() {
		int width = getWidth();
		int height = getHeight();

		lineXStart = (int) BORDER_OFFSET;
		lineXStop = (int) (width - BORDER_OFFSET);
		lineYStart = height - 1;
		lineYStop = height - 1;

		int x0 = (int)BORDER_OFFSET;
		int y0 = 0;
		int x1 = height;
		int y1 = 0;

		switch (roundMode) {
			case ONE:
				y0 = (int) BORDER_OFFSET;
				y1 = (int) (height - BORDER_OFFSET + 1);

				break;
			case TOP:
				y0 = (int)BORDER_OFFSET;
				y1 = (int) (height - BORDER_OFFSET + 1);

				break;
			case MID:
				y0 = (int)BORDER_OFFSET - 1;
				y1 = (int) (height - BORDER_OFFSET) +1;
				break;
			case BOT:
				lineYStart = 0;
				lineYStop = 0;

				y0 = (int)BORDER_OFFSET - 1;
				y1 =  (int) (height - BORDER_OFFSET);
				break;
		}
		backForImage.setBounds(x0, y0, x1, y1);

		initialized = true;
	}
}
