package com.chess.utilities;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import com.chess.R;
import com.chess.views.BackgroundChessDrawable;

/**
 * CommonUtils class
 *
 * @author alien_roger
 * @created at: 01.02.12 7:50
 */
public class CommonUtils {

	private static final int MDPI_DENSITY = 1;

	public static void setBackground(View mainView, Context context) {
		mainView.setBackgroundDrawable(new BackgroundChessDrawable(context));

//		int padding = getResources().getDrawable(R.drawable.chess_cell).getIntrinsicWidth() / 2;
		int paddingTop = (int) context.getResources().getDimension(R.dimen.dashboard_padding_top);
		int paddingLeft = (int) context.getResources().getDimension(R.dimen.dashboard_padding_side);
		int paddingRight = (int) context.getResources().getDimension(R.dimen.dashboard_padding_side);
		int paddingBot = (int) context.getResources().getDimension(R.dimen.dashboard_padding_bot);
		mainView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBot);
	}

	public static boolean needFullScreen(Context context) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		return displayMetrics.density < MDPI_DENSITY || displayMetrics.densityDpi == DisplayMetrics.DENSITY_LOW;
	}
}
