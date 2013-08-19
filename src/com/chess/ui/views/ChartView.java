package com.chess.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.View;
import com.chess.R;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.02.13
 * Time: 19:28
 */
public class ChartView extends View {

	private static final int TIME = 0;
	private static final int VALUE = 1;
	private static final long MILLISECONDS_PER_DAY = 86400 * 1000;

	private Paint mPaint;
	private Path mPath;
	private int widthPixels;
	private float yAspect;
	private int backColor;
	private Paint borderPaint;
	private int minY;
	private int maxY;
	private float density;
	private int graphTopColor;
	private int graphBottomColor;
	private Rect clipBounds;
	private SparseArray<Long> pointsArray;
	private SparseBooleanArray pointsExistArray;
	private boolean initialized;

	public ChartView(Context context, List<long[]> dataArray) {
		super(context);
		init(context, dataArray);
	}

	private void init(Context context, List<long[]> dataArray) {
		Resources resources = context.getResources();
		density = resources.getDisplayMetrics().density;
		widthPixels = resources.getDisplayMetrics().widthPixels;

		clipBounds = new Rect();

		// set colors
		int borderColor = resources.getColor(R.color.graph_border);
		backColor = resources.getColor(R.color.graph_back);
		graphTopColor = resources.getColor(R.color.graph_gradient_top);
		graphBottomColor = resources.getColor(R.color.graph_gradient_bottom);

		borderPaint = new Paint();
		borderPaint.setColor(borderColor);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setStrokeWidth(1.5f * density);

		setFocusable(true);
		setFocusableInTouchMode(true);

		if (dataArray == null) {
			initialized = false;
			return;
		}

		setPoints(dataArray);
	}

	private void setPoints(List<long[]> dataArray){
		{ // get min and max of X values
			// remove
			long firstPoint = dataArray.get(0)[TIME] - dataArray.get(0)[TIME] % MILLISECONDS_PER_DAY;

			long lastPoint = System.currentTimeMillis();
			lastPoint -= lastPoint % MILLISECONDS_PER_DAY;
			// distribute timestamps at whole width
			long xDiff = lastPoint - firstPoint;
			long xPointRange = xDiff / widthPixels;

			// convert xPointRange to optimal day{time} difference
			pointsArray = new SparseArray<Long>();
			pointsExistArray = new SparseBooleanArray();

			logTest(" xPointRange = " + (xPointRange - xPointRange % xPointRange));
			for (int i = 0; i < widthPixels; i++) {
				long timestampValue = firstPoint + i * xPointRange;
				timestampValue -= timestampValue % MILLISECONDS_PER_DAY;

				boolean found = false;
				long graphTimestamp;
				for (long[] aDataArray : dataArray) {
					graphTimestamp = aDataArray[TIME] - aDataArray[TIME] % MILLISECONDS_PER_DAY;

					long rating = aDataArray[VALUE];
					if ((timestampValue - graphTimestamp) >= 0 && (timestampValue - graphTimestamp) < (xPointRange * 2)) {
						logTest(" timestampValue = " + timestampValue + " graphTimestamp = " + graphTimestamp);
						pointsArray.put(i, rating);
						found = true;
						break;
					}
				}

				logTest("timestampValue = " + timestampValue);
				pointsExistArray.put(i, found);
			}
		}

		{// get min and max of Y values
			minY = Integer.MAX_VALUE;
			maxY = Integer.MIN_VALUE;
			for (long[] longs : dataArray) {
				int yValue = (int) longs[VALUE];
				minY = Math.min(minY, yValue);
				maxY = Math.max(maxY, yValue);
			}
			logTest(" _______________________ ");
			logTest(" minY = " + minY + " maxY = " + maxY);
		}

		initialized = true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.getClipBounds(clipBounds);
		int bottom = clipBounds.bottom;
		canvas.drawColor(backColor);

		if (initialized) {
			canvas.save();
			drawPaths(canvas);
			canvas.restore();
		} else {
			canvas.drawText("No Data :(", 0 ,0, borderPaint);
		}

		canvas.drawLine(0, bottom, widthPixels, bottom, borderPaint);
		canvas.drawLine(0, 0, widthPixels, 0, borderPaint);
	}

	private void drawPaths(Canvas canvas) {
		if (mPath == null) {
			int height = canvas.getClipBounds().bottom;
			int originalMaxY = maxY;
			minY -= 200;
			maxY += 400;

			int diff = maxY - minY;

			yAspect = (float) (diff / height);

			mPath = makeFollowPath(/*dataArray,*/ height);

			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mPaint.setAntiAlias(true);
			int gradientYStart = (int) (height - (originalMaxY - minY) / yAspect);
			LinearGradient shader = new LinearGradient(0, gradientYStart, 0, height, graphTopColor, graphBottomColor, Shader.TileMode.CLAMP);
			mPaint.setShader(shader);
		}

		canvas.drawPath(mPath, mPaint);

		int strokeWidth = (int) (1.5f * density);

		Paint strokePaint = new Paint();
		strokePaint.setAntiAlias(true);
		strokePaint.setStyle(Paint.Style.STROKE);
		strokePaint.setStrokeWidth(strokeWidth);
		strokePaint.setColor(0xFF88b2cc);

		canvas.drawPath(mPath, strokePaint);
	}

	private Path makeFollowPath(int height) {
		Path path = new Path();
		logTest(" yAspect = " + yAspect);
//		long startYValue = data.get(0)[VALUE] - minY;
		long startYValue = 0;
		path.moveTo(-10, height - startYValue / yAspect);
		long yValue = 0;
		for (int i = 0; i < widthPixels; i++) {
			if (pointsExistArray.get(i)) {
				yValue = pointsArray.get(i) - minY;
			}
			path.lineTo(i, height - yValue / yAspect);
		}

		path.lineTo(widthPixels, height);
		path.lineTo(-10, height);
		path.close();

		return path;
	}

	private void logTest(String string) {
		Log.d("TEST", string);
	}

	public void setGraphData(List<long[]> series) {
		setPoints(series);
		invalidate();
	}
}
