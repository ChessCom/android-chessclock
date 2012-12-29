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
import com.chess.utilities.AppUtils;

public class OnlineCurrentGamesCursorAdapter extends ItemsCursorAdapter {

	protected static final String CHESS_960 = " (960)";
	protected static final String HOUR_SYMBOL = "h";

	public OnlineCurrentGamesCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);// TODO change later with CursorLoader
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

		String draw = StaticData.SYMBOL_EMPTY;
		if (getInt(cursor, DBConstants.V_OPPONENT_OFFERED_DRAW) > 0) {
			draw = "\n" + context.getString(R.string.drawoffered);
		}

		holder.playerTxt.setText(getString(cursor, DBConstants.V_OPPONENT_NAME) + gameType + draw);

		String infoText = StaticData.SYMBOL_EMPTY;
		if (getInt(cursor, DBConstants.V_IS_MY_TURN) > 0) {

			long amount = getLong(cursor, DBConstants.V_TIME_REMAINING);
			infoText = AppUtils.getTimeLeftFromSeconds(amount, context);
		}

		holder.gameInfoTxt.setText(infoText);
	}

	protected class ViewHolder {
		public TextView playerTxt;
		public TextView gameInfoTxt;
	}
}
