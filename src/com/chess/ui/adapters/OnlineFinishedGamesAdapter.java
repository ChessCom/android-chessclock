package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.model.GameListItem;

import java.util.List;

public class OnlineFinishedGamesAdapter extends OnlineGamesAdapter {


	public OnlineFinishedGamesAdapter(Context context, List<GameListItem> itemList) {
		super(context, itemList);
	}

	@Override
	protected void bindView(GameListItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		String gameType = StaticData.SYMBOL_EMPTY;

		if (item.values.get(GameListItem.GAME_TYPE) != null && item.values.get(GameListItem.GAME_TYPE).equals("2")) {
			gameType = " (960)";
		}

		String result = context.getString(R.string.lost);
		if (item.values.get(GameListItem.GAME_RESULT).equals("1")) {
			result = context.getString(R.string.won);
		} else if (item.values.get(GameListItem.GAME_RESULT).equals("2")) {
			result = context.getString(R.string.draw);
		}

		holder.playerTxt.setText(item.values.get(GameListItem.OPPONENT_USERNAME) + gameType);
		holder.gameInfoTxt.setText(result);
	}
}
