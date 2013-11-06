package com.chess.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import com.chess.R;
import com.chess.ui.views.drawables.IconDrawable;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 03.01.13
 * Time: 13:45
 */
public class FacebookButton extends RoboButton {

	public static final float BORDER_OFFSET = 3.0f;
	public static final float LINE_WIDTH = 1.0f;

	private Drawable icon;
	private Paint borderPaint;
	private int[] borderColors;
	private int imageWidth;
	private float facebookTextShift;

	public FacebookButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public FacebookButton(Context context) {
		super(context);
		init(context);
	}

	public FacebookButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		Resources resources = context.getResources();

		icon = new IconDrawable(context, R.string.ic_facebook, R.color.white, R.dimen.social_icon_size);
		imageWidth = icon.getIntrinsicWidth();
		int imageHeight = icon.getIntrinsicHeight();
		icon.setBounds(0, 0, imageWidth, imageHeight);

		float density = resources.getDisplayMetrics().density;

		borderColors = new int[4];
		borderColors[0] = resources.getColor(R.color.f_emboss_top_2);
		borderColors[1] = resources.getColor(R.color.f_emboss_top_1);
		borderColors[2] = 0xFF284160;
		borderColors[3] = resources.getColor(R.color.f_emboss_bottom_2);

		borderPaint = new Paint();
		borderPaint.setStrokeWidth(1);
		borderPaint.setStyle(Paint.Style.STROKE);

		facebookTextShift = 20 * density;
	}

	@Override
	protected void onDraw(Canvas canvas) { // TODO use Picture?

		int height = getHeight();
		for (int i = 0, cnt = borderColors.length; i < cnt; i++) {
			float left = height + i * LINE_WIDTH - 1;
			float top = 0;
			float right = height + i * LINE_WIDTH - 1;
			float bottom = 0;
			switch (i){
				case 0:
					top = BORDER_OFFSET;
					bottom = height - BORDER_OFFSET - 1 ;
					break;
				case 1:
				case 3:
					top = BORDER_OFFSET;
					bottom = height- BORDER_OFFSET;
					break;
				case 2:
					top = BORDER_OFFSET - LINE_WIDTH;
					bottom = height - BORDER_OFFSET + LINE_WIDTH;
					break;
			}

			borderPaint.setColor(borderColors[i]);
			canvas.drawLine(left, top, right, bottom, borderPaint);
		}

		// place image
		canvas.save();
		float imgCenterX = BORDER_OFFSET/2 + (height - imageWidth)/2;
		float imgCenterY = (height - imageWidth)/2;
		canvas.translate(imgCenterX, imgCenterY);
		icon.draw(canvas);
		canvas.restore();

		// place additional clickable element
		canvas.translate(facebookTextShift, 0);  // TODO get child's text width and make shift according to it
		super.onDraw(canvas);
	}

}
