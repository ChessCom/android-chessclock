package com.chess.ui.views.drawables;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import com.chess.R;
import com.chess.utilities.AppUtils;

public class LogoBackgroundDrawable extends Drawable {

	public static final float TALL_KOEF = 1.7f;

	private Rect fullScreenRect;
	private GradientDrawable shadowDrawable;
	private Bitmap roundedBitmap;
	private int patternWidth;
	private BitmapDrawable imageBackDrawable;
	private Bitmap shadowOvalBitmap;
	private Rect ovalRect;
	private Paint fullPaint;
	private Rect squareRect;
	private float logoOffset;
	private int densityDpi;
	private RectF outerRect;
	private PorterDuffXfermode xRefMode1;
	private PorterDuffXfermode xRefMode2;
	private boolean configChanged;
	private Context context;
	private float screenProportion;
	private boolean portraitMode;
	public int IMAGE_HEIGHT = 500;
	public int SHADOW_HEIGHT = 600;
	private int oldHeight;
	private int oldWidth;

	public LogoBackgroundDrawable(Context context) {
		init(context);
		setChangingConfigurations(Configuration.ORIENTATION_LANDSCAPE | Configuration.ORIENTATION_PORTRAIT);
	}

	private void init(Context context) {
		this.context = context;
		int backgroundColor = context.getResources().getColor(R.color.new_main_back);

		densityDpi = context.getResources().getDisplayMetrics().densityDpi;
		fullScreenRect = new Rect();
		squareRect = new Rect();

		xRefMode1 = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
		xRefMode2 = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

		shadowDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
				new int[] { 0x00312e2a, 0x00312e2a, backgroundColor });
		shadowDrawable.setShape(GradientDrawable.OVAL);
		shadowDrawable.setGradientType(GradientDrawable.RADIAL_GRADIENT);

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

		paint.setXfermode(xRefMode2);
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

		outerRect = new RectF(0, 0, width, IMAGE_HEIGHT); // should always be proportional oval
		float outerRadius = 300;

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		// draw limiter on canvas
		canvas.drawRoundRect(outerRect, outerRadius, outerRadius, paint);

		paint.setXfermode(xRefMode1);
		// back pattern. Should be square proportions
		imageBackDrawable.setBounds(0, 0, width, height);

		// cross layers and save on canvas
		canvas.saveLayer(outerRect, paint, Canvas.ALL_SAVE_FLAG);
		imageBackDrawable.draw(canvas);
		canvas.restore();

		roundedBitmap = Bitmap.createBitmap(output);
	}

	@Override
	public void draw(Canvas canvas) {
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		if (roundedBitmap == null || configChanged(canvas)) {
			setupDrawables(canvas);
			configChanged = false;
		}

		// background
		fullScreenRect.set(0, 0, width, height);
		canvas.drawRect(fullScreenRect, fullPaint);

		canvas.save();
		// move down to place under Chess.com logo
//		Paint green = new Paint();
//		green.setColor(Color.GREEN);
//		Paint blue = new Paint();
//		blue.setColor(Color.BLUE);
//		canvas.drawLine(0, logoOffset, width, logoOffset, green);
		canvas.translate(0, logoOffset);
//		canvas.drawLine(0, outerRect.top, width, outerRect.top, green);
//		canvas.drawLine(0, outerRect.bottom, width , outerRect.bottom, green );
//		canvas.drawLine(0, squareRect.bottom, width , squareRect.bottom, blue );

		canvas.drawBitmap(roundedBitmap, null, squareRect, null);
		canvas.restore();

//		Paint red = new Paint();
//		red.setColor(Color.RED);
//		canvas.drawLine(- width * 0.025f, logoOffset + (SHADOW_HEIGHT - IMAGE_HEIGHT), width , logoOffset + (SHADOW_HEIGHT - IMAGE_HEIGHT),red );
		canvas.save();
		if (AppUtils.HONEYCOMB_PLUS_API){
			if (portraitMode){
				if (screenProportion > TALL_KOEF) {
					canvas.translate(- width * 0.025f, logoOffset - (SHADOW_HEIGHT - IMAGE_HEIGHT));
				} else {
					canvas.translate(- width * 0.025f, logoOffset - (SHADOW_HEIGHT - IMAGE_HEIGHT));
//					canvas.translate(- width * 0.025f, logoOffset);
				}
			} else {
				canvas.translate(- width * 0.025f, logoOffset - (SHADOW_HEIGHT - IMAGE_HEIGHT));
			}
		} else {
			if (portraitMode){
				if (screenProportion > TALL_KOEF) {
					canvas.translate(- width * 0.025f, logoOffset - height * 0.01f);
				} else {
					canvas.translate(- width * 0.025f, logoOffset - height * 0.03f);
				}
			} else {
				canvas.translate(- width * 0.025f, logoOffset - height * 0.02f);
			}

		}

//		canvas.translate(- width * 0.025f, logoOffset);
		canvas.drawBitmap(shadowOvalBitmap, null, ovalRect, fullPaint);
//		canvas.drawLine(- width * 0.025f, ovalRect.top, width , ovalRect.top, red );
//		canvas.drawLine(- width * 0.025f, ovalRect.bottom, width , ovalRect.bottom, red );


//		canvas.drawLine(- width * 0.025f, logoOffset + (SHADOW_HEIGHT - IMAGE_HEIGHT) + ovalRect.height(), width ,
//				logoOffset + (SHADOW_HEIGHT - IMAGE_HEIGHT) + ovalRect.height(),red );
		canvas.restore();
	}

	private boolean configChanged(Canvas canvas) {
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		if (oldWidth != width || oldHeight != height) {
			oldWidth = width;
			oldHeight = height;
			configChanged = true;
		}

		return configChanged;
	}

	private void setupDrawables(Canvas canvas){
		int width = canvas.getWidth();
		int height = canvas.getHeight();

		// get screen proportion
		screenProportion = (float)height/width;
		Log.d("TEST", "proportion = " + screenProportion);

		if (AppUtils.HONEYCOMB_PLUS_API) {
			if (screenProportion > TALL_KOEF){  // TODO use resource flags
				IMAGE_HEIGHT = (int) (height * 0.39f);//500;
//				Log.d("TEST", "IMAGE_HEIGHT = " +IMAGE_HEIGHT  + " SCR HEIGHT = " + height + " SCR WIDTH = " + width);

				SHADOW_HEIGHT = (int) (IMAGE_HEIGHT + IMAGE_HEIGHT * 0.1f);//600;
//				Log.d("TEST", "SHADOW_HEIGHT = " + SHADOW_HEIGHT );
			} else {
				IMAGE_HEIGHT = (int) (height * 0.39f);//500;
//				Log.d("TEST", "IMAGE_HEIGHT = " +IMAGE_HEIGHT  + " SCR HEIGHT = " + height + " SCR WIDTH = " + width);

				SHADOW_HEIGHT = (int) (IMAGE_HEIGHT + IMAGE_HEIGHT * 0.3f);//600;
//				Log.d("TEST", "SHADOW_HEIGHT = " + SHADOW_HEIGHT );
			}
		} else {
//			if ()  // change
			IMAGE_HEIGHT = (int) (height * 0.39f);//500;
//			Log.d("TEST", "IMAGE_HEIGHT = " +IMAGE_HEIGHT  + " SCR HEIGHT = " + height + " SCR WIDTH = " + width);

			SHADOW_HEIGHT = (int) (IMAGE_HEIGHT + IMAGE_HEIGHT * 0.01f);//600;
//			Log.d("TEST", "SHADOW_HEIGHT = " + SHADOW_HEIGHT );
		}


		shadowDrawable.setGradientRadius(SHADOW_HEIGHT / 2.3f);

		int backWidth;
		if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
			portraitMode = true;
			backWidth = patternWidth * 4;
		} else {
			backWidth = patternWidth * 8;
		}
		createRoundedBackground(backWidth, backWidth);

		// enlarge a bit a region for shadowed gradient
		squareRect.set(0, 0, width, width);
		shadowDrawable.setBounds(squareRect);

		ovalRect = new Rect(0, 0, (int) (width + width * 0.05f), (int) (SHADOW_HEIGHT + SHADOW_HEIGHT * 0.16f));

		shadowOvalBitmap = createShadow(SHADOW_HEIGHT, SHADOW_HEIGHT);

		if (densityDpi > DisplayMetrics.DENSITY_HIGH) {
			logoOffset = height * 0.08f;
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

	public void updateConfig() {
		configChanged = true;
		invalidateSelf();
	}

}
