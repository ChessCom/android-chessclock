package com.chess.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.*;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.chess.R;
import com.chess.backend.entity.api.stats.GraphData;
import com.google.gson.Gson;

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

	private Paint mPaint;
	private Path mPath;
	private int widthPixels;
	private float yAspect;
	private int xPointRange;
	private int backColor;
	private Paint borderPaint;
	private List<long[]> dataArray;
	private int minY;
	private int maxY;
	private float density;
	private int graphTopColor;
	private int graphBottomColor;
	private Rect clipBounds;

	public ChartView(Context context) {
		super(context);
		init(context);
	}

	public ChartView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ChartView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private static final String graphString = "{\"min_y\":945,\"max_x\":1606,\"series\":[[1260259200000,1045],[1302246000000,1218],[1305270000000,1307],[1306220400000,1318],[1311318000000,1471],[1359619200000,1506],[1367478000000,1319],[1369983600000,1201],[1370674800000,1073]]}";
// 1260259200000,
// 1302246000000,
// 1305270000000,
// 1306220400000,
// 1311318000000,
// 1359619200000,
// 1367478000000,
// 1369983600000,
// 1370674800000,

	private void init(Context context) {
		Resources resources = context.getResources();
		density = resources.getDisplayMetrics().density;
		clipBounds = new Rect();

		Gson gson = new Gson();
		GraphData graphData = gson.fromJson(graphString, GraphData.class);

		dataArray = graphData.getSeries();

		widthPixels = resources.getDisplayMetrics().widthPixels;

		setFocusable(true);
		setFocusableInTouchMode(true);

		// get min and max of Y values
		minY = Integer.MAX_VALUE;
		maxY = Integer.MIN_VALUE;
		for (long[] longs : dataArray) {
			int yValue = (int) longs[VALUE];
			minY = Math.min(minY, yValue);
			maxY = Math.max(maxY, yValue);
		}
		Log.d("TEST", " _______________________ ");
		Log.d("TEST", " minY = " + minY + " maxY = " + maxY);

		xPointRange = widthPixels / dataArray.size();

		int borderColor = resources.getColor(R.color.graph_border);
		backColor = resources.getColor(R.color.graph_back);
		graphTopColor = resources.getColor(R.color.graph_gradient_top);
		graphBottomColor = resources.getColor(R.color.graph_gradient_bottom);

		borderPaint = new Paint();
		borderPaint.setColor(borderColor);
		borderPaint.setStyle(Paint.Style.STROKE);
		borderPaint.setStrokeWidth(1.5f * density);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.getClipBounds(clipBounds);
		int bottom = clipBounds.bottom;
		canvas.drawColor(backColor);

		canvas.save();
		drawPaths(canvas);
		canvas.restore();

		canvas.drawLine(0, bottom, widthPixels, bottom, borderPaint);
		canvas.drawLine(0, 0, widthPixels, 0, borderPaint);
	}

	private void drawPaths(Canvas canvas) {
		if (mPath == null) {
			int height = canvas.getClipBounds().bottom;
			minY -= 200;
			maxY += 400;

			int diff = maxY - minY;

			yAspect = (float) (diff / height);

			mPath = makeFollowPath(dataArray, height);

			mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  			mPaint.setAntiAlias(true);
			LinearGradient shader = new LinearGradient(0, 0, 0, height, graphTopColor, graphBottomColor, Shader.TileMode.CLAMP);
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


	private Path makeFollowPath(List<long[]> data, int height) {
		Path path = new Path();
		Log.d("TEST", " yAspect = " + yAspect);
		long startYValue = data.get(0)[VALUE] - minY;
		path.moveTo(-10, height - startYValue / yAspect);
		for (int i = 1; i < data.size(); i++) {
			long yValue = data.get(i)[VALUE] - minY;
			Log.d("TEST", "height = " + height + " graph x = " + i + " y = " + (height - yValue / yAspect));
			path.lineTo(i * xPointRange, height - yValue / yAspect);
		}
		long yValue = data.get(data.size() - 1)[VALUE] - minY;
		path.lineTo(widthPixels, height - yValue / yAspect);

		path.lineTo(widthPixels, height);
		path.lineTo(-10, height);
		path.close();

		return path;
	}


}
