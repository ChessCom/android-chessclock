package com.chess;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
public class LeftRightImageEditText extends RoboEditText {

	public static final int ONE = 0;
	public static final int TOP = 1;
	public static final int MID = 2;
	public static final int BOT = 3;

	public static float BORDER_OFFSET;
	public static float LINE_WIDTH;

	private Drawable rightIcon;
	private int rightImageWidth;
	private int rightImageHeight;
//	private int origianlHeight;

	private Drawable icon;
	private int imageWidth;
	private Paint borderPaint;
	private boolean initialized;
	private ShapeDrawable backForImage;
	private int roundMode;
	private float density;
	private Paint linePaint;
	private int lineYStop;
	private int lineYStart;
	private int lineXStop;
	private int lineXStart;
	private float backWidth;
	private int rightImageOffset;
	private int currentHeight;
	private boolean enlargeHeight;


	public LeftRightImageEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	public LeftRightImageEditText(Context context) {
		super(context);
	}

	public LeftRightImageEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.EnhancedField);
		rightIcon = array.getDrawable(R.styleable.EnhancedField_rightImage);

		rightImageWidth = rightIcon.getIntrinsicWidth();
		rightImageHeight = rightIcon.getIntrinsicHeight();
		rightIcon.setBounds(0, 0, rightImageWidth, rightImageHeight);

		density = context.getResources().getDisplayMetrics().density;
		int densityDpi = context.getResources().getDisplayMetrics().densityDpi;

		// back for image
		int color = Color.WHITE;

		final int N = array.getIndexCount();
		for (int i = 0; i < N; i++) {
			int attr = array.getIndex(i);
			switch (attr) {
				case R.styleable.EnhancedField_round_mode:
					roundMode = array.getInteger(i, ONE);
					break;
				case R.styleable.EnhancedField_color:
					color = array.getInteger(i, Color.WHITE);
					break;
				case R.styleable.EnhancedField_leftImage:
					icon = array.getDrawable(i);
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

		linePaint = new Paint();
		linePaint.setColor(context.getResources().getColor(R.color.light_grey_border));
		linePaint.setStrokeWidth(1);
		linePaint.setStyle(Paint.Style.STROKE);

		float borderOffset = 1.0f;
		float lineWidth = 0.5f;
		if (densityDpi <= DisplayMetrics.DENSITY_LOW) {
			lineWidth = 0.5f;
			borderOffset = 0.5f;
		}
		BORDER_OFFSET = borderOffset * density;
		LINE_WIDTH = lineWidth * density;

		backWidth = context.getResources().getDimension(R.dimen.new_edit_field_height) + BORDER_OFFSET;

		if (rightImageHeight > backWidth) {
			enlargeHeight = true;
		}

		rightImageOffset = (int) (26 * density);
	}

	@Override
	protected void onDraw(Canvas canvas) { // TODO use Picture?
		if (!initialized) {
			initImage(canvas);
		}
		int height = getHeight();
		int width = getWidth();

		backForImage.draw(canvas);

		// place image
		canvas.save();
		float imgCenterX = (backWidth - imageWidth)/2;
		float imgCenterY = (height - imageWidth)/2;
		canvas.translate(imgCenterX, imgCenterY);
		icon.draw(canvas);
		canvas.restore();

		// place second image
		canvas.save();
		imgCenterX = width - rightImageOffset/2 - rightImageWidth;
		imgCenterY = (height - rightImageWidth)/2;
		canvas.translate(imgCenterX, imgCenterY);
		rightIcon.draw(canvas);
		canvas.restore();

		if (roundMode != ONE) {
			canvas.drawLine(lineXStart, lineYStart, lineXStop, lineYStop, linePaint);
		}
		if (roundMode == MID) {
			canvas.drawLine(lineXStart, 0, lineXStop, 0, linePaint);
		}

		// place additional clickable element
		if (enlargeHeight) {
			canvas.translate(backWidth + BORDER_OFFSET, - rightImageHeight/2);
		} else {
			canvas.translate(backWidth + BORDER_OFFSET, 0);
		}
		super.onDraw(canvas);
	}


	private void initImage(Canvas canvas) {
		int width = getWidth();
		int height = getHeight();

		lineXStart = (int) BORDER_OFFSET;
		lineXStop = (int) (width - BORDER_OFFSET);
		lineYStart = height - 1;
		lineYStop = height - 1;

		switch (roundMode) {
			case ONE:
				backForImage.setBounds((int)BORDER_OFFSET , (int)BORDER_OFFSET, (int) backWidth, (int) (height - BORDER_OFFSET + 1));
				break;
			case TOP:
				backForImage.setBounds((int)BORDER_OFFSET, (int)BORDER_OFFSET, (int) backWidth, (int) (height - BORDER_OFFSET + 1));
				break;
			case MID:
				backForImage.setBounds((int)BORDER_OFFSET, (int)BORDER_OFFSET - 1, (int) backWidth, (int) (height - BORDER_OFFSET) +1);
				break;
			case BOT:
				lineYStart = 0;
				lineYStop = 0;

				backForImage.setBounds((int) BORDER_OFFSET , (int)BORDER_OFFSET - 1, (int) backWidth, (int) (height - BORDER_OFFSET));
				break;
			default:
				backForImage.setBounds((int)BORDER_OFFSET, (int)BORDER_OFFSET, (int) backWidth, (int) (height - BORDER_OFFSET + 1));
				break;
		}

		initialized = true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (enlargeHeight) {
			int parentWidth = MeasureSpec.getSize(widthMeasureSpec);

			currentHeight = rightImageHeight + rightImageOffset;
			setMeasuredDimension(parentWidth, currentHeight);
		}else {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}
}
