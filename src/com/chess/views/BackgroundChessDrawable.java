package com.chess.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;

public class BackgroundChessDrawable extends Drawable {

	private Paint paint;
	private Paint[] shinePaints;
	private Path[] shinePaths;
	private Rect rect;
	private float density = 1;

	private final int DEFAULT_WIDTH = 50;
	private final int DEFAULT_HEIGHT = 50;

	private int cellSize = 50;
	private int cellColor1 = 0xFF494846;
	private int cellColor2 = 0xFF51504d;
	private GradientDrawable gradienView;
	private boolean pathsInitiated;

	private final Context context;
	private float width;
	private float height;

	public BackgroundChessDrawable(Context context) {
		this.context = context;
		init();
	}

	private void init() {
		density = context.getResources().getDisplayMetrics().density;
		cellSize *= (int) density;
		paint = new Paint(/* Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG */);
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

		width = context.getResources().getDisplayMetrics().widthPixels;
		height = context.getResources().getDisplayMetrics().heightPixels;
	}

	public void setCellColor1(int color) {
		cellColor1 = color;
	}

	public void setCellColor2(int color) {
		cellColor2 = color;
	}

	private void createShinePath() {
//		int width = getIntrinsicWidth();
//		int height = getIntrinsicHeight();
		for (int i = 0; i < shinePaints.length; i++) {
			shinePaths[i] = new Path();
			setCoordinates(shinePaths[i], 0, (int) width, 0, (int) height);
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
	public void draw(Canvas canvas) {
		if (!pathsInitiated) {
			createShinePath();
		}
		canvas.save();

//		int width = getIntrinsicWidth();
//		int height = getIntrinsicHeight();
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

	@Override
	public int getOpacity() {
		return PixelFormat.OPAQUE;
	}

	@Override
	public void setAlpha(int alpha) {
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
	}
}
