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
import android.view.View;
import android.widget.ImageView;

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

	public static float BORDER_OFFSET;
	public static float LINE_WIDTH;

	private Drawable icon;
	private int imageWidth;
	private Paint borderPaint;
	private Path path;
	private boolean initialized;
	private ShapeDrawable backForImage;
	private int roundMode;
	private boolean overlapBack;
	private boolean showBorder;


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


		float density = context.getResources().getDisplayMetrics().density;

		// back for image
		TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LeftImageEditText);
		int color = Color.WHITE;

		final int N = a.getIndexCount();
		for (int i = 0; i < N; i++) {
			int attr = a.getIndex(i);
			switch (attr) {
				case R.styleable.LeftImageEditText_round_mode:
					roundMode = a.getInteger(i, ONE);
					break;
				case R.styleable.LeftImageEditText_color:
					color = a.getInteger(i, Color.WHITE);
					break;
				case R.styleable.LeftImageEditText_overlapBack:
					overlapBack = a.getBoolean(i, false);
					break;
				case R.styleable.LeftImageEditText_showBorder:
					showBorder = a.getBoolean(i, false);
					break;
				case R.styleable.LeftImageEditText_leftImage:
					icon = a.getDrawable(i);
					break;
			}
		}

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


		borderPaint = new Paint();
		borderPaint.setColor(Color.GREEN);
		borderPaint.setStrokeWidth(3);
		borderPaint.setStyle(Paint.Style.STROKE);

		path = new Path();

		float borderOffset = 5f;
		float lineWidth = 0.5f;
		if (density <= DisplayMetrics.DENSITY_LOW) {
			lineWidth = 0.5f;
			borderOffset = 5.5f;
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
		if (showBorder) { // place border
			path.moveTo(getHeight(), 0);
			path.lineTo(getHeight(), getHeight());
			canvas.drawPath(path, borderPaint);
		}

		if (overlapBack) {
			backForImage.draw(canvas);
		}

		// place image
		canvas.save();
		float imgCenterX = (height - imageWidth)/2;
		float imgCenterY = (height - imageWidth)/2;
		canvas.translate(imgCenterX, imgCenterY);
		icon.draw(canvas);
		canvas.restore();



		// place additional clickable element
		canvas.translate(getHeight() + BORDER_OFFSET, -BORDER_OFFSET);
		super.onDraw(canvas);
	}

	private void initImage(Canvas canvas) {
		backForImage.setBounds(0, 0 + 1, getHeight(), getHeight() - 2);

		initialized = true;
	}
}
