package com.chess.ui.views;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import com.chess.R;

public class LogoBackgroundDrawable extends Drawable {

	public static final int IMAGE_HEIGHT = 500;
	public static final int SHADOW_HEIGHT = 600;

	private Rect fullScreenRect;
	private GradientDrawable shadowDrawable;
	private Bitmap framedPhoto;
	private int patternWidth;
	private BitmapDrawable imageBackDrawable;
	private Bitmap shadowOvalBitmap;
	private Rect ovalRect;
	private Paint fullPaint;
	private Rect squareRect;
	private float logoOffset;

	public LogoBackgroundDrawable(Context context) {
		init(context);
		setChangingConfigurations(Configuration.ORIENTATION_LANDSCAPE | Configuration.ORIENTATION_PORTRAIT);
	}

	private void init(Context context) {
		int backgroundColor = context.getResources().getColor(R.color.new_main_back);
		logoOffset = context.getResources().getDimension(R.dimen.new_signin_main_margin_top) - 120;

		fullScreenRect = new Rect();
		squareRect = new Rect();

		shadowDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
				new int[] { 0x00312e2a, 0x00312e2a, backgroundColor });
		shadowDrawable.setShape(GradientDrawable.OVAL);
		shadowDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
		shadowDrawable.setGradientRadius(SHADOW_HEIGHT / 2);

		imageBackDrawable = ((BitmapDrawable)context.getResources().getDrawable(R.drawable.img_new_logo_back_pattern));
		patternWidth = imageBackDrawable.getIntrinsicWidth();
		imageBackDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

		fullPaint = new Paint();
		fullPaint.setColor(backgroundColor);
		fullPaint.setStyle(Paint.Style.FILL);
	}

	private Bitmap createShadow(int width, int height) {
		// back pattern
		Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(output);

		RectF outerRect = new RectF(0, 0, width, height);
		float outerRadius = 300;

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		// draw limiter on canvas
		canvas.drawRoundRect(outerRect, outerRadius, outerRadius, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		shadowDrawable.setBounds(0, 0, width, height);

		// cross layers and save on canvas
		canvas.saveLayer(outerRect, paint, Canvas.ALL_SAVE_FLAG);
		shadowDrawable.draw(canvas);
		canvas.restore();

		return Bitmap.createBitmap(output);
	}

	private void createRoundedBackground(int width, int height) {

		Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(output);

		RectF outerRect = new RectF(0, 0, width, IMAGE_HEIGHT);
		float outerRadius = 300;

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		// draw limiter on canvas
		canvas.drawRoundRect(outerRect, outerRadius, outerRadius, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		// back pattern
		imageBackDrawable.setBounds(0, 0, width, height);

		// cross layers and save on canvas
		canvas.saveLayer(outerRect, paint, Canvas.ALL_SAVE_FLAG);
		imageBackDrawable.draw(canvas);
		canvas.restore();

		framedPhoto = Bitmap.createBitmap(output);
	}

	@Override
	public void draw(Canvas canvas) {
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		if (framedPhoto == null) {
			int backWidth = patternWidth * 4;
			createRoundedBackground(backWidth, backWidth);

			// enlarge a bit a region for shadowed gradient
			squareRect.set(0, 0, width, width);
			shadowDrawable.setBounds(squareRect);

			ovalRect = new Rect(0, 0, width + 40, IMAGE_HEIGHT + 80);

			shadowOvalBitmap = createShadow(SHADOW_HEIGHT, SHADOW_HEIGHT);
		}

		// background
		fullScreenRect.set(0, 0, width, height);
		canvas.drawRect(fullScreenRect, fullPaint);

		canvas.save();
		// move down to place under Chess.com logo
		canvas.translate(0, logoOffset);
		canvas.drawBitmap(framedPhoto, null, squareRect, null);
		canvas.restore();

		canvas.save();
		canvas.translate(-20, logoOffset - 10);
		canvas.drawBitmap(shadowOvalBitmap, null, ovalRect, fullPaint);
		canvas.restore();
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
