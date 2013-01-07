package com.chess.ui.views;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import com.chess.R;

public class NewBackgroundChessDrawable extends Drawable {

	private Rect fullScreenRect;
	private GradientDrawable mDrawable;
	private Bitmap framedPhoto;
	private int patternWidth;
	private BitmapDrawable imageDrawable;
	private Bitmap shadowOvalBitmap;
	private Rect ovalRect;
	private Paint fullPaint;
	private Rect squareRect;
	private float logoOffset;

	public NewBackgroundChessDrawable(Context context) {
		init(context);
		setChangingConfigurations(Configuration.ORIENTATION_LANDSCAPE | Configuration.ORIENTATION_PORTRAIT);
	}

	private void init(Context context) {
		int backgroundColor = context.getResources().getColor(R.color.new_main_back);
		logoOffset = context.getResources().getDimension(R.dimen.new_signin_main_margin_top) - 60;

		fullScreenRect = new Rect(0, 0, 330, 330);
		squareRect = new Rect(0, 0, 330, 330);

		mDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
				new int[] { 0x00312e2a, 0x00312e2a, backgroundColor });
		mDrawable.setShape(GradientDrawable.OVAL);
		mDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);
		mDrawable.setGradientRadius(140);

		imageDrawable = ((BitmapDrawable)context.getResources().getDrawable(R.drawable.img_new_logo_back_pattern));
		patternWidth = imageDrawable.getIntrinsicWidth();
		imageDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);

		fullPaint = new Paint();
		fullPaint.setColor(backgroundColor);
		fullPaint.setStyle(Paint.Style.FILL);
	}

	private Bitmap createShadow(int width, int height) {
		// back pattern
		Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(output);

		RectF outerRect = new RectF(0, 0, width, 300);
		float outerRadiusX = 300;
		float outerRadiusY = 300;

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		// draw limiter on canvas
		canvas.drawRoundRect(outerRect, outerRadiusX, outerRadiusY, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		mDrawable.setBounds(0, 0, width, height);

		// cross layers and save on canvas
		canvas.saveLayer(outerRect, paint, Canvas.ALL_SAVE_FLAG);
		mDrawable.draw(canvas);
		canvas.restore();

		return Bitmap.createBitmap(output);
	}

	private void createFramePhoto(int width, int height) {
		// back pattern
		imageDrawable.setBounds(0, 0, width, height);

		Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(output);

		RectF outerRect = new RectF(0, 0, width, 300);
		float outerRadiusX = 300;
		float outerRadiusY = 300;

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		// draw limiter on canvas
		canvas.drawRoundRect(outerRect, outerRadiusX, outerRadiusY, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		imageDrawable.setBounds(0, 0, width, height);

		// cross layers and save on canvas
		canvas.saveLayer(outerRect, paint, Canvas.ALL_SAVE_FLAG);
		imageDrawable.draw(canvas);
		canvas.restore();

		framedPhoto = Bitmap.createBitmap(output);
	}

	@Override
	public void draw(Canvas canvas) {
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		if (framedPhoto == null) {
			int backWidth = patternWidth * 4;
			createFramePhoto(backWidth, backWidth);

			// enlarge a bit a region for shadowed gradient
			squareRect.set(0, 0, width, width);
			mDrawable.setBounds(squareRect);

			ovalRect = new Rect(0, 0, width + 40, 300 + 60);

			shadowOvalBitmap = createShadow(300, 300);
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
		canvas.translate(-20, logoOffset - 5);
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
