package com.chess.ui.views.drawables;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import com.chess.ui.engine.ChessBoard;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.01.13
 * Time: 22:02
 */
public class BoardAvatarDrawable extends Drawable {

	private float CORNER_RADIUS = 1.5f;
	private int BORDER_THICK = 1;
	private Bitmap roundedBitmap;
	private Drawable imageBackDrawable;
	private GradientDrawable solidBackDrawable;
	private final GradientDrawable solidBackWhiteDrawable;
	private final GradientDrawable solidBackBlackDrawable;
	private int side;


	public BoardAvatarDrawable(Context context, Bitmap sourcePhoto) {
		float density = context.getResources().getDisplayMetrics().density;
		CORNER_RADIUS *= density;
		BORDER_THICK *= density;

		imageBackDrawable = new BitmapDrawable(context.getResources(), sourcePhoto);

		solidBackWhiteDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
				new int[] {0xFFFFFFFF, 0xFFFFFFFF});
		solidBackBlackDrawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
				new int[] {0xFF181715, 0xFF181715});
	}

	@Override
	public void draw(Canvas canvas) {
		int width = getBounds().width();
		int height = getBounds().height();

		if (roundedBitmap == null) {
			roundedBitmap = createRoundedBitmap(width - BORDER_THICK * 2, height - BORDER_THICK * 2);
		}

		solidBackDrawable = side == ChessBoard.WHITE_SIDE? solidBackWhiteDrawable: solidBackBlackDrawable;

		solidBackDrawable.setBounds(0, 0, width, height);
		setCornerRadii(solidBackDrawable, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS, CORNER_RADIUS);
		solidBackDrawable.draw(canvas);

		canvas.save();
		canvas.translate(BORDER_THICK, BORDER_THICK);
		canvas.drawBitmap(roundedBitmap, 0, 0, null);
		canvas.restore();
	}

	static void setCornerRadii(GradientDrawable drawable, float r0, float r1, float r2, float r3) {
		drawable.setCornerRadii(new float[] { r0, r0, r1, r1,
				r2, r2, r3, r3 });
	}

	private Bitmap createRoundedBitmap(int width, int height) {
		// back pattern
		Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(output);

		RectF outerRect = new RectF(0, 0, width, height);
		float outerRadius = 0;

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		// draw limiter on canvas
		canvas.drawRoundRect(outerRect, outerRadius, outerRadius, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		imageBackDrawable.setBounds(0, 0, width, height);

		// cross layers and save on canvas
		canvas.saveLayer(outerRect, paint, Canvas.ALL_SAVE_FLAG);
		imageBackDrawable.draw(canvas);
		canvas.restore();

		return Bitmap.createBitmap(output);
	}

	public void setSide(int side) {
		this.side = side;
		invalidateSelf();
	}

	public void setBorderThick(int thick) {
		BORDER_THICK = thick;
	}

	@Override
	public void setAlpha(int alpha) {
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
	}

	@Override
	public int getOpacity() {
		return PixelFormat.OPAQUE;
	}
}
