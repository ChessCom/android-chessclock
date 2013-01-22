package com.chess.ui.views.drawables;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import com.chess.R;

public class BackgroundChessDrawable extends Drawable {

	private Paint gradientPaint;
	private Path gradientPath;

	private BitmapDrawable image;

	private boolean pathsInitiated;

	private int height;
	private int width;

	private Context context;
	private boolean configChanged;

	public BackgroundChessDrawable(Context context) {
		init(context);
		setChangingConfigurations(Configuration.ORIENTATION_LANDSCAPE | Configuration.ORIENTATION_PORTRAIT);
	}

	private void init(Context context) {
		this.context = context;
		gradientPaint = new Paint();
		gradientPaint.setDither(true);
		gradientPaint.setAntiAlias(true);

		width = context.getResources().getDisplayMetrics().widthPixels;
		height = context.getResources().getDisplayMetrics().heightPixels;

		image = (BitmapDrawable) context.getResources().getDrawable(R.drawable.chess_back);
		image.setBounds(0, 0, width, height);
		image.setDither(true);
		image.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
	}

	private void createGradientPath() {
		float border = -5;
		gradientPath = new Path();
		setCoordinates(gradientPath, 0, width, 0, height);
		int blackColor = 0xB4000000;
		gradientPaint.setShader(
				new LinearGradient(0, height, 0, border, blackColor, 0x00000000,
						Shader.TileMode.CLAMP));
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
			createGradientPath();
		}

		if(configChanged){
			width = context.getResources().getDisplayMetrics().widthPixels;
			height = context.getResources().getDisplayMetrics().heightPixels;
			setCoordinates(gradientPath, 0, width, 0, height);
			image.setBounds(0, 0, width, height);
			configChanged = false;
		}
		canvas.save();

		image.draw(canvas);

		canvas.restore();

		canvas.drawPath(gradientPath, gradientPaint);
		canvas.drawPath(gradientPath, gradientPaint);
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

	public void updateConfig() {
		configChanged = true;
		invalidateSelf();
	}
}
