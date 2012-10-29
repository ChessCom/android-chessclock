package com.chess.ui.views;

import android.content.Context;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class BackgroundChessView extends FrameLayout {

	private Paint paint;
	private Paint[] shinePaints;
	private Path[] shinePaths;
	private Rect rect;
	private float density;

	private final int DEFAULT_WIDTH = 50;
	private final int DEFAULT_HEIGHT = 50;

	private int cellSize = 50;
	private int cellColor1 = 0xFF494846;
	private int cellColor2 = 0xFF51504d;
	private boolean pathsInitiated;

	public BackgroundChessView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public BackgroundChessView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public BackgroundChessView(Context context) {
		super(context);
		init();
	}

	private void init() {
		density = getContext().getResources().getDisplayMetrics().density;
		cellSize *= (int) density;
		paint = new Paint();
		shinePaints = new Paint[4];
		for (int i = 0; i < shinePaints.length; i++) {
			shinePaints[i] = new Paint();
			shinePaints[i].setDither(true);
			shinePaints[i].setAntiAlias(true);
		}

		shinePaths = new Path[4];
		rect = new Rect(0, 0, 5, 5);

		paint.setStrokeWidth(1.0f);
		paint.setColor(Color.BLUE);
		paint.setStyle(Style.FILL);
		paint.setDither(true);
		paint.setAntiAlias(true);
	}

	public void setCellColor1(int color) {
		cellColor1 = color;
	}

	public void setCellColor2(int color) {
		cellColor2 = color;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (!pathsInitiated) {
			createShinePath();
		}
		canvas.save();

		int width = getWidth();
		int height = getHeight();
		int j = 0;
		while (j < height) {
			int i = 0;
			while (i < width) {
				int x1 = cellSize * i;
				int x2 = cellSize * (i + 1);
				int y1 = cellSize * j;
				int y2 = cellSize * (j + 1);

				if (j % 2 == 0) {
					paint.setColor((i % 2 == 0) ? cellColor1 : cellColor2);
				} else {
					paint.setColor((i % 2 == 0) ? cellColor2 : cellColor1);
				}
				rect.set(x1, y1, x2, y2);
				canvas.drawRect(rect, paint);
				i++;
			}
			j++;
		}
		canvas.restore();

		for (int i = 0; i < shinePaints.length; i++) {
			canvas.drawPath(shinePaths[i], shinePaints[i]);
		}
	}

	private void createShinePath() {
		int width = getWidth();
		int height = getHeight();
		for (int i = 0; i < shinePaints.length; i++) {
			shinePaths[i] = new Path();
			setCoordinates(shinePaths[i], 0, width, 0, height);
			switch (i) {
				case 0:
					shinePaints[i].setShader(new LinearGradient(0, 0, 0, height / 4, 0xFF000000, 0x0000000,
							Shader.TileMode.CLAMP));
					break;
				case 1:
					shinePaints[i].setShader(new LinearGradient(0, 0, width / 4, 0, 0xFF000000, 0x0000000,
							Shader.TileMode.CLAMP));
					break;
				case 2:
					shinePaints[i].setShader(new LinearGradient(width, 0, width * 3 / 4, 0, 0xFF000000, 0x0000000,
							Shader.TileMode.CLAMP));
					break;
				case 3:
					shinePaints[i].setShader(new LinearGradient(0, height, 0, height * 3 / 4, 0xFF000000, 0x0000000,
							Shader.TileMode.CLAMP));
					break;
				default:
					break;
			}
		}
		pathsInitiated = true;
	}

	private void setCoordinates(Path path, int x0, int x1, int y0, int y1) {
		path.moveTo(x0, y0);
		path.lineTo(x0, y1);
		path.lineTo(x1, y1);
		path.lineTo(x1, y0);
		path.close();
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
