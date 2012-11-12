package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.db.DBConstants;
import com.chess.model.BaseGameItem;

public class OnlineFinishedGamesCursorAdapter extends ItemsCursorAdapter {

	protected static final String CHESS_960 = " (960)";


	public OnlineFinishedGamesCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.game_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.playerTxt = (TextView) view.findViewById(R.id.playerTxt);
		holder.gameInfoTxt = (TextView) view.findViewById(R.id.gameInfoTxt);

		view.setTag(holder);
		return view;
	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		String gameType = StaticData.SYMBOL_EMPTY;

		if (getInt(cursor, DBConstants.V_GAME_TYPE) == BaseGameItem.CHESS_960) {
			gameType = CHESS_960;
		}

		String result = context.getString(R.string.lost);
		if (getInt(cursor, DBConstants.V_GAME_RESULT) == BaseGameItem.GAME_WON) {
			result = context.getString(R.string.won);
		} else if (getInt(cursor, DBConstants.V_GAME_RESULT) == BaseGameItem.GAME_DRAW) {
			result = context.getString(R.string.draw);
		}

		holder.playerTxt.setText(getString(cursor, DBConstants.V_OPPONENT_NAME) + gameType);
		holder.gameInfoTxt.setText(result);
	}

	protected class ViewHolder {
		public TextView playerTxt;
		public TextView gameInfoTxt;
	}
}
