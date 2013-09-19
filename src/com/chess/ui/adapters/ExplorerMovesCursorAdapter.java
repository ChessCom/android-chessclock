package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.statics.Symbol;
import com.chess.db.DbScheme;

import java.text.NumberFormat;

public class ExplorerMovesCursorAdapter extends ItemsCursorAdapter {

	private final LinearLayout.LayoutParams whiteParams;
	private final LinearLayout.LayoutParams drawsParams;
	private final LinearLayout.LayoutParams blackParams;
	private final int regularPadding;
	private final int topFirstPadding;
	private final int sidePadding;

	public ExplorerMovesCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);

		whiteParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
		drawsParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
		blackParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
		sidePadding = resources.getDimensionPixelSize(R.dimen.default_scr_side_padding);
		topFirstPadding = (int) (12 * resources.getDisplayMetrics().density);
		regularPadding = (int) (6 * resources.getDisplayMetrics().density);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_explorer_moves_list_item, parent, false);

		ViewHolder holder = new ViewHolder();
		holder.moveTxt = (TextView) view.findViewById(R.id.moveTxt);
		holder.numGamesTxt = (TextView) view.findViewById(R.id.numGamesTxt);
		holder.whiteWinsPercentTxt = (TextView) view.findViewById(R.id.whiteWinsPercentTxt);
		holder.drawsPercentTxt = (TextView) view.findViewById(R.id.drawsPercentTxt);
		holder.blackWinsPercentTxt = (TextView) view.findViewById(R.id.blackWinsPercentTxt);

		view.setTag(holder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		if (cursor.getPosition() == 0) {
			view.setPadding(sidePadding, topFirstPadding, sidePadding, regularPadding);
		} else {
			view.setPadding(sidePadding, regularPadding, sidePadding, regularPadding);
		}

		String moveStr = getString(cursor, DbScheme.V_MOVE);
		long numberOfGames = getLong(cursor, DbScheme.V_NUM_GAMES);
		String numGamesStr = NumberFormat.getInstance().format(numberOfGames);
		holder.moveTxt.setText(moveStr);
		holder.numGamesTxt.setText(numGamesStr);

		int whiteWonPercent = getInt(cursor, DbScheme.V_WHITE_WON_PERCENT);
		int drawPercent = getInt(cursor, DbScheme.V_DRAW_PERCENT);
		int blackWonPercent = getInt(cursor, DbScheme.V_BLACK_WON_PERCENT);

		if (whiteWonPercent == 0) {
			holder.whiteWinsPercentTxt.setVisibility(View.GONE);
		} else {
			holder.whiteWinsPercentTxt.setVisibility(View.VISIBLE);
		}

		if (drawPercent == 0) {
			holder.drawsPercentTxt.setVisibility(View.GONE);
		} else {
			holder.drawsPercentTxt.setVisibility(View.VISIBLE);
		}

		if (blackWonPercent == 0) {
			holder.blackWinsPercentTxt.setVisibility(View.GONE);
		} else {
			holder.blackWinsPercentTxt.setVisibility(View.VISIBLE);
		}

		// Changing weights for views
		whiteParams.weight = whiteWonPercent;
		drawsParams.weight = drawPercent;
		blackParams.weight = blackWonPercent;

		holder.whiteWinsPercentTxt.setLayoutParams(whiteParams);
		holder.drawsPercentTxt.setLayoutParams(drawsParams);
		holder.blackWinsPercentTxt.setLayoutParams(blackParams);

		String whiteWinsStr = String.valueOf(whiteWonPercent) + Symbol.PERCENT;
		String drawsStr = String.valueOf(drawPercent) + Symbol.PERCENT;
		String blackWinsStr = String.valueOf(blackWonPercent) + Symbol.PERCENT;

		holder.whiteWinsPercentTxt.setText(whiteWinsStr);
		holder.drawsPercentTxt.setText(drawsStr);
		holder.blackWinsPercentTxt.setText(blackWinsStr);
	}

	private class ViewHolder {
		public TextView moveTxt;
		public TextView numGamesTxt;
		public TextView whiteWinsPercentTxt;
		public TextView drawsPercentTxt;
		public TextView blackWinsPercentTxt;
	}
}
