package com.chess.ui.views.drawables;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import com.chess.R;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 26.05.13
 * Time: 7:53
 */
public class SquareShadowDrawable extends Drawable {

	private Paint[] shinePaints;
	private Path[] shinePaths;

	private boolean pathsInitiated;

	private float width;
	private float height;

	private int shadowColor;

	public SquareShadowDrawable(Context context) {
		init(context);
	}

	private void init(Context context) {
		Resources resources = context.getResources();
		shadowColor = resources.getColor(R.color.glassy_button);

		shinePaints = new Paint[4];
		for (int i = 0; i < shinePaints.length; i++) {
			shinePaints[i] = new Paint();
			shinePaints[i].setDither(true);
			shinePaints[i].setAntiAlias(true);
		}

		shinePaths = new Path[4];

		width = resources.getDisplayMetrics().widthPixels;
		height = resources.getDisplayMetrics().heightPixels;
	}

	private static final int LEFT = 0;
	private static final int TOP = 1;
	private static final int RIGHT = 2;
	private static final int BOTTOM = 3;

	private void createShinePath() {
		width = getBounds().right;
		height = getBounds().bottom;

		float[] borders = new float[4];

		borders[TOP] = height / 10;
		borders[LEFT] = width / 10;
		borders[RIGHT] = (width * 9) / 10;
		borders[BOTTOM] = (height * 9) / 10;

		for (int i = 0; i < shinePaints.length; i++) {
			shinePaths[i] = new Path();
			setCoordinates(shinePaths[i], 0, (int) width, 0, (int) height);

			switch (i) {
				case LEFT:
					shinePaints[i].setShader(new LinearGradient(0, 0, borders[i], 0, shadowColor, 0x00000000,
							Shader.TileMode.CLAMP));
					break;
				case TOP:
					shinePaints[i].setShader(new LinearGradient(0, 0, 0, borders[i], shadowColor, 0x00000000,
							Shader.TileMode.CLAMP));
					break;
				case RIGHT:
					shinePaints[i].setShader(new LinearGradient(width, 0, borders[i], 0, shadowColor, 0x00000000,
							Shader.TileMode.CLAMP));
					break;
				case BOTTOM:
					shinePaints[i].setShader(new LinearGradient(0, height, 0, borders[i], shadowColor, 0x00000000,
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