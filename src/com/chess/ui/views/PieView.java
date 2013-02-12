package com.chess.ui.views;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.view.View;
import com.chess.R;
import com.chess.backend.entity.new_api.stats.GamesInfoByResult;


/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.02.13
 * Time: 19:15
 */
public class PieView extends View {


	private static final int TOP_OFFSET = 30;
	private static final float LINE_LEFT_OFFSET = 10;
	public static int DONUT_HALF_SIZE;
	private int TEXT_WIDTH;

	public static final String MAIN_PATH = "fonts/trebuc-";

	private ShapeDrawable[] mDrawables;
	private GamesInfoByResult games;
	private float winsPercent;
	private float lossPercent;
	private float drawPercent;

	private String winsText;
	private String lossText;
	private String drawsText;

	private int textGreenColor = 0xFF57832f;
	private int textOrangeColor = 0xFFe48629;
	private int textGreyColor = 0xFF65605b;
	private int backgroundColor;
	private Paint textLegendPaint;
	private static int DONUT_SIZE;
	private int DONUT_OVERLAY_SIZE;
	private int centerPointX;
	private Paint centerLinePaint;
	private String winTotal;
	private String lossTotal;
	private String drawnTotal;
	private Paint centerTextPaint;
	private int INSIDE_TOP_TEXT_OFFSET;

	public PieView(Context context) {
		super(context);
		init(context);
	}

	PieView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	PieView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public PieView(Context context, GamesInfoByResult games) {
		super(context);
		this.games = games;
		init(context);
	}

	private void init(Context context) {
		setFocusable(true);

		float density = context.getResources().getDisplayMetrics().density;

		DONUT_SIZE = (int) (174 * density);
		DONUT_HALF_SIZE = DONUT_SIZE / 2;
		DONUT_OVERLAY_SIZE = (int) (112 * density);
		INSIDE_TOP_TEXT_OFFSET = (int) (8 * density);
		TEXT_WIDTH = (int) (20 * density);

		backgroundColor = context.getResources().getColor(R.color.white);

		mDrawables = new ShapeDrawable[4];

		int totalDegree = 360;
		// set wins
		// calc percent
		int startDegree = -90;

		if (games != null) {
			winsPercent = ((float) games.getWins() / games.getTotal());
		}
		float winsDegree = totalDegree * winsPercent;
		winsText = getResources().getString(R.string.pie_chart_win_legend, winsPercent * 100);

		if (games != null) {
			drawPercent = ((float) games.getDraws() / games.getTotal());
		}
		float drawDegree = totalDegree * drawPercent;
		float drawStartDegree = startDegree - winsDegree;
		drawsText = getResources().getString(R.string.pie_chart_drawn_legend, drawPercent * 100);

		if (games != null) {
			lossPercent = ((float) games.getLosses() / games.getTotal());
		}
		float lossDegree = totalDegree * lossPercent;
		float lossStartDegree = startDegree - winsDegree - drawDegree;
		lossText = getResources().getString(R.string.pie_chart_loss_legend, lossPercent * 100);

		mDrawables[0] = new MyShapeDrawable(new ArcShape(startDegree, -winsDegree));
		mDrawables[1] = new MyShapeDrawable(new ArcShape(drawStartDegree, -drawDegree));
		mDrawables[2] = new MyShapeDrawable(new ArcShape(lossStartDegree, -lossDegree));
		mDrawables[3] = new ShapeDrawable(new OvalShape());

		mDrawables[3].getPaint().setColor(backgroundColor);

		int greenColor1 = context.getResources().getColor(R.color.chart_green_1);
		int greenColor2 = context.getResources().getColor(R.color.chart_green_2);
		mDrawables[0].getPaint().setShader(makeRadial(greenColor1, greenColor2));

		int greyColor1 = context.getResources().getColor(R.color.chart_grey_1);
		int greyColor2 = context.getResources().getColor(R.color.chart_grey_2);
		mDrawables[1].getPaint().setShader(makeRadial(greyColor1, greyColor2));

		int orangeColor1 = context.getResources().getColor(R.color.chart_orange_1);
		int orangeColor2 = context.getResources().getColor(R.color.chart_orange_2);
		mDrawables[2].getPaint().setShader(makeRadial(orangeColor1, orangeColor2));


		{
			MyShapeDrawable msd = (MyShapeDrawable) mDrawables[0];
			msd.getStrokePaint().setStrokeWidth(1);
		}

		{
			MyShapeDrawable msd = (MyShapeDrawable) mDrawables[1];
			msd.getStrokePaint().setStrokeWidth(1);
		}

		{
			MyShapeDrawable msd = (MyShapeDrawable) mDrawables[2];
			msd.getStrokePaint().setStrokeWidth(1);
		}

		Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), MAIN_PATH + "Bold" + ".ttf");
		{// legend paint setup
			textLegendPaint = new Paint();
			textLegendPaint.setTypeface(typeface);
			textLegendPaint.setTextSize(13 * density);

			// center lines
			centerLinePaint = new Paint();
			centerLinePaint.setColor(0xFFdfdfdf);
			centerLinePaint.setStyle(Paint.Style.STROKE);
			centerLinePaint.setStrokeWidth(2);
		}

		if (games != null) {
			{// center textValues
				winTotal = games.getWins() + " W";
				lossTotal = games.getLosses() + " L";
				drawnTotal = games.getDraws() + " D";

				centerTextPaint = new Paint();
				centerTextPaint.setTypeface(typeface);
				centerTextPaint.setTextSize(14 * density + 0.5f);
			}
		}
	}

	private static Shader makeRadial(int color1, int color2) {
		return new RadialGradient(DONUT_HALF_SIZE, DONUT_HALF_SIZE, DONUT_HALF_SIZE, color1, color2, Shader.TileMode.CLAMP);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int width = canvas.getWidth();
		centerPointX = width / 2;

		canvas.drawColor(backgroundColor);

		drawDonut(canvas);

		// draw legend labels
		{// wins
			int winsX = centerPointX - DONUT_HALF_SIZE - TEXT_WIDTH;
			int winsY = TOP_OFFSET;
			textLegendPaint.setColor(textGreenColor);
			canvas.drawText(winsText, winsX, winsY, textLegendPaint);
		}

		{// losses
			int lossX = centerPointX + DONUT_HALF_SIZE - TEXT_WIDTH;
			int lossY = TOP_OFFSET;
			textLegendPaint.setColor(textOrangeColor);
			canvas.drawText(lossText, lossX, lossY, textLegendPaint);
		}

		{// draws
			int drawsX = centerPointX + DONUT_HALF_SIZE - TEXT_WIDTH;
			int drawsY = (int) (TOP_OFFSET + DONUT_SIZE + textLegendPaint.getTextSize());
			textLegendPaint.setColor(textGreyColor);
			canvas.drawText(drawsText, drawsX, drawsY, textLegendPaint);
		}
	}

	private void drawDonut(Canvas canvas) {
		int x = 0;
		int y = 0;

		canvas.save();
		canvas.translate(centerPointX - DONUT_HALF_SIZE, TOP_OFFSET);
		// pie 1
		mDrawables[0].setBounds(x, y, x + DONUT_SIZE, y + DONUT_SIZE);
		mDrawables[0].draw(canvas);

		// pie 2
		mDrawables[1].setBounds(x, y, x + DONUT_SIZE, y + DONUT_SIZE);
		mDrawables[1].draw(canvas);

		// pie 3
		mDrawables[2].setBounds(x, y, x + DONUT_SIZE, y + DONUT_SIZE);
		mDrawables[2].draw(canvas);

		// white overlay
		canvas.save();
		int overlayCenter = DONUT_OVERLAY_SIZE / 2;
		int xOffset = DONUT_HALF_SIZE - overlayCenter;
		canvas.translate(xOffset, xOffset);

		mDrawables[3].setBounds(x, y, x + DONUT_OVERLAY_SIZE, y + DONUT_OVERLAY_SIZE);
		mDrawables[3].draw(canvas);

		int widthBetweenLines = DONUT_OVERLAY_SIZE / 3;

		// center lines
		int topLineY = overlayCenter - widthBetweenLines / 2;
		canvas.drawLine(LINE_LEFT_OFFSET, topLineY, DONUT_OVERLAY_SIZE - LINE_LEFT_OFFSET, topLineY, centerLinePaint);

		int bottomLineY = overlayCenter + widthBetweenLines / 2;
		canvas.drawLine(LINE_LEFT_OFFSET, bottomLineY, DONUT_OVERLAY_SIZE - LINE_LEFT_OFFSET, bottomLineY, centerLinePaint);


		if (games != null) {
			// draw values inside
			{// wins total
				float[] textWidths = new float[winTotal.length()];
				centerTextPaint.getTextWidths(winTotal, textWidths);
				float labelLength = 0;
				for (float textWidth : textWidths) {
					labelLength += textWidth;
				}

				float textStartPositionX = overlayCenter - labelLength / 2;
				float textStartPositionY = topLineY - INSIDE_TOP_TEXT_OFFSET /*- centerTextPaint.getTextSize()*/;

				centerTextPaint.setColor(textGreenColor);
				canvas.drawText(winTotal, textStartPositionX, textStartPositionY, centerTextPaint);
			}

			{// losses total
				float[] textWidths = new float[lossTotal.length()];
				centerTextPaint.getTextWidths(lossTotal, textWidths);
				float labelLength = 0;
				for (float textWidth : textWidths) {
					labelLength += textWidth;
				}

				float textStartPositionX = overlayCenter - labelLength / 2;
				float textStartPositionY = overlayCenter + centerTextPaint.getTextSize() / 2;

				centerTextPaint.setColor(textOrangeColor);
				canvas.drawText(lossTotal, textStartPositionX, textStartPositionY, centerTextPaint);
			}

			{// draws total
				float[] textWidths = new float[drawnTotal.length()];
				centerTextPaint.getTextWidths(drawnTotal, textWidths);
				float labelLength = 0;
				for (float textWidth : textWidths) {
					labelLength += textWidth;
				}

				float textStartPositionX = overlayCenter - labelLength / 2;
				float textStartPositionY = bottomLineY + INSIDE_TOP_TEXT_OFFSET + centerTextPaint.getTextSize();

				centerTextPaint.setColor(textGreyColor);
				canvas.drawText(drawnTotal, textStartPositionX, textStartPositionY, centerTextPaint);
			}
		}

		canvas.restore();
		canvas.restore();
	}

	public void setGames(GamesInfoByResult games) {
		this.games = games;
		init(getContext());
		invalidate();
	}


	private static class MyShapeDrawable extends ShapeDrawable {
		private Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		public MyShapeDrawable(Shape s) {
			super(s);
			mStrokePaint.setStyle(Paint.Style.STROKE);
			mStrokePaint.setColor(0x26FFFFFF);  // TODO set properly
		}

		public Paint getStrokePaint() {
			return mStrokePaint;
		}

		@Override
		protected void onDraw(Shape s, Canvas c, Paint p) {
			s.draw(c, p);
			s.draw(c, mStrokePaint);
		}
	}
}
