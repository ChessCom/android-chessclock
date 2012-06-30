package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.model.GameListItem;

import java.util.List;

public class OnlineChallengesGamesAdapter extends OnlineGamesAdapter {

	public OnlineChallengesGamesAdapter(Context context, List<GameListItem> itemList) {
		super(context, itemList);
	}

	@Override
	protected void bindView(GameListItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		String time = item.values.get(GameListItem.DAYS_PER_MOVE) + context.getString(R.string.days);
		String gameType = StaticData.SYMBOL_EMPTY;

		if (item.values.get(GameListItem.GAME_TYPE) != null && item.values.get(GameListItem.GAME_TYPE).equals("2")) {
			gameType = " (960)";
		}

		String opponentRating = StaticData.SYMBOL_LEFT_PAR + item.values.get(GameListItem.OPPONENT_RATING) + StaticData.SYMBOL_RIGHT_PAR;
		String userName = item.values.get(GameListItem.OPPONENT_USERNAME);

		holder.playerTxt.setText(userName + StaticData.SYMBOL_SPACE + opponentRating);
		holder.gameInfoTxt.setText( gameType + StaticData.SYMBOL_SPACE + time);
	}
}
