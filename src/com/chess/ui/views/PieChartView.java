package com.chess.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import com.chess.R;
import com.chess.backend.entity.api.stats.GamesInfoByResult;


/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.02.13
 * Time: 19:15
 */
public class PieChartView extends View {

	public static final String PERCENT_FORMAT = " %.0f%%";
	private int topOffset;
	private float lineLeftOffset;
	public int donutHalfSize;
	private int textWidth;

	public static final String MAIN_PATH = "fonts/custom-";

	private ShapeDrawable[] mDrawables;
	private GamesInfoByResult games;
	private float winsPercent;
	private float lostPercent;
	private float drawPercent;

	private String winsText;
	private String lossText;
	private String drawsText;

	private int textGreenColor = 0xFF57832f;
	private int textOrangeColor = 0xFFe48629;
	private int textGreyColor = 0xFF65605b;
	private int backgroundColor;
	private Paint textLegendPaint;
	private int donutSize;
	private int donutWidthSize;
	private int centerPointX;
	private Paint centerLinePaint;
	private String winTotal;
	private String lossTotal;
	private String drawnTotal;
	private Paint centerTextPaint;
	private int insideTopTextOffset;
	private int rightTextWidth;

	public PieChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		setFocusable(true);

		Resources resources = context.getResources();
		float density = resources.getDisplayMetrics().density;

		donutSize = resources.getDimensionPixelSize(R.dimen.pie_chart_donut_size);
		donutHalfSize = donutSize / 2;
		donutWidthSize = resources.getDimensionPixelSize(R.dimen.pie_chart_donut_width_size);
		insideTopTextOffset = (int) (8 * density);
		textWidth = (int) (20 * density);
		rightTextWidth =  resources.getDimensionPixelSize(R.dimen.pie_chart_right_text_width);
		topOffset = (int) (20 * density);
		lineLeftOffset = (int) (10 * density);

		int height = donutSize + topOffset * 2;

		setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));

		backgroundColor = resources.getColor(R.color.white);

		mDrawables = new ShapeDrawable[4];

		int totalDegree = 360;
		// set wins
		// calc percent
		int startDegree = -90;

		if (games != null) {
			winsPercent = ((float) games.getWins() / games.getTotal());
		}
		float winsDegree = totalDegree * winsPercent;
		{
			String result = resources.getString(R.string.won) + PERCENT_FORMAT;
			winsText = String.format(resources.getConfiguration().locale, result, winsPercent * 100);
		}
		if (games != null) {
			drawPercent = ((float) games.getDraws() / games.getTotal());
		}
		float drawDegree = totalDegree * drawPercent;
		float drawStartDegree = startDegree - winsDegree;
		{
			String result = resources.getString(R.string.draw) + PERCENT_FORMAT;
			drawsText = String.format(resources.getConfiguration().locale, result, drawPercent * 100);
		}
		if (games != null) {
			lostPercent = ((float) games.getLosses() / games.getTotal());
		}
		float lostDegree = totalDegree * lostPercent;
		float lossStartDegree = startDegree - winsDegree - drawDegree;
		{
			String result = resources.getString(R.string.lost) + PERCENT_FORMAT;
			lossText = String.format(resources.getConfiguration().locale, result, lostPercent * 100);
		}
		mDrawables[0] = new MyShapeDrawable(new ArcShape(startDegree, -winsDegree));
		mDrawables[1] = new MyShapeDrawable(new ArcShape(drawStartDegree, -drawDegree));
		mDrawables[2] = new MyShapeDrawable(new ArcShape(lossStartDegree, -lostDegree));
		mDrawables[3] = new ShapeDrawable(new OvalShape());

		mDrawables[3].getPaint().setColor(backgroundColor);

		int greenColor1 = resources.getColor(R.color.chart_green_1);
		int greenColor2 = resources.getColor(R.color.chart_green_2);
		mDrawables[0].getPaint().setShader(makeRadial(greenColor1, greenColor2));

		int greyColor1 = resources.getColor(R.color.chart_grey_1);
		int greyColor2 = resources.getColor(R.color.chart_grey_2);
		mDrawables[1].getPaint().setShader(makeRadial(greyColor1, greyColor2));

		int orangeColor1 = resources.getColor(R.color.chart_orange_1);
		int orangeColor2 = resources.getColor(R.color.chart_orange_2);
		mDrawables[2].getPaint().setShader(makeRadial(orangeColor1, orangeColor2));


		{
			MyShapeDrawable drawable = (MyShapeDrawable) mDrawables[0];
			drawable.getStrokePaint().setStrokeWidth(1);
		}

		{
			MyShapeDrawable drawable = (MyShapeDrawable) mDrawables[1];
			drawable.getStrokePaint().setStrokeWidth(1);
		}

		{
			MyShapeDrawable drawable = (MyShapeDrawable) mDrawables[2];
			drawable.getStrokePaint().setStrokeWidth(1);
		}

		Typeface typeface = Typeface.createFromAsset(getContext().getAssets(), MAIN_PATH + "Bold" + ".ttf");
		{// legend paint setup
			textLegendPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			textLegendPaint.setTypeface(typeface);
			textLegendPaint.setTextSize(13 * density);

			// center lines
			centerLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			centerLinePaint.setColor(0xFFdfdfdf);
			centerLinePaint.setStyle(Paint.Style.STROKE);
			centerLinePaint.setStrokeWidth(2);
		}

		if (games != null) {
			{// center textValues
				winTotal = games.getWins() + " W";
				lossTotal = games.getLosses() + " L";
				drawnTotal = games.getDraws() + " D";

				centerTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
				centerTextPaint.setTypeface(typeface);
				centerTextPaint.setTextSize(14 * density + 0.5f);
			}
		}
	}

	private Shader makeRadial(int color1, int color2) {
		return new RadialGradient(donutHalfSize, donutHalfSize, donutHalfSize, color1, color2, Shader.TileMode.CLAMP);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int width = canvas.getWidth();
		centerPointX = width / 2;

		canvas.drawColor(backgroundColor);

		drawDonut(canvas);

		// draw legend labels
		{// wins
			int winsX = centerPointX - donutHalfSize - textWidth;
			int winsY = topOffset;
			textLegendPaint.setColor(textGreenColor);
			canvas.drawText(winsText, winsX, winsY, textLegendPaint);
		}

		{// losses
			int lossX = centerPointX + donutHalfSize - rightTextWidth;
			int lossY = topOffset;
			textLegendPaint.setColor(textOrangeColor);
			canvas.drawText(lossText, lossX, lossY, textLegendPaint);
		}

		{// draws
			int drawsX = centerPointX + donutHalfSize - rightTextWidth;
			int drawsY = (int) (topOffset + donutSize + textLegendPaint.getTextSize());
			textLegendPaint.setColor(textGreyColor);
			canvas.drawText(drawsText, drawsX, drawsY, textLegendPaint);
		}
	}

	private void drawDonut(Canvas canvas) {
		int x = 0;
		int y = 0;

		canvas.save();
		canvas.translate(centerPointX - donutHalfSize, topOffset);
		// pie 1
		mDrawables[0].setBounds(x, y, x + donutSize, y + donutSize);
		mDrawables[0].draw(canvas);

		// pie 2
		mDrawables[1].setBounds(x, y, x + donutSize, y + donutSize);
		mDrawables[1].draw(canvas);

		// pie 3
		mDrawables[2].setBounds(x, y, x + donutSize, y + donutSize);
		mDrawables[2].draw(canvas);

		// white overlay
		canvas.save();
		int overlayCenter = donutWidthSize / 2;
		int xOffset = donutHalfSize - overlayCenter;
		canvas.translate(xOffset, xOffset);

		mDrawables[3].setBounds(x, y, x + donutWidthSize, y + donutWidthSize);
		mDrawables[3].draw(canvas);

		int widthBetweenLines = donutWidthSize / 3;

		// center lines
		int topLineY = overlayCenter - widthBetweenLines / 2;
		canvas.drawLine(lineLeftOffset, topLineY, donutWidthSize - lineLeftOffset, topLineY, centerLinePaint);

		int bottomLineY = overlayCenter + widthBetweenLines / 2;
		canvas.drawLine(lineLeftOffset, bottomLineY, donutWidthSize - lineLeftOffset, bottomLineY, centerLinePaint);


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
				float textStartPositionY = topLineY - insideTopTextOffset /*- centerTextPaint.getTextSize()*/;

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
				float textStartPositionY = bottomLineY + insideTopTextOffset + centerTextPaint.getTextSize();

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
