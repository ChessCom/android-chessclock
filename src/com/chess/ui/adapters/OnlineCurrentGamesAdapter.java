package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import com.chess.R;
import com.chess.model.GameListItem;
import com.chess.ui.core.AppConstants;

import java.util.List;

public class OnlineCurrentGamesAdapter extends OnlineGamesAdapter {


	public OnlineCurrentGamesAdapter(Context context, List<GameListItem> itemList) {
		super(context, itemList);
	}

	@Override
	protected void bindView(GameListItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		String gameType = AppConstants.SYMBOL_EMPTY;
		if (item.values.get(GameListItem.GAME_TYPE) != null && item.values.get(GameListItem.GAME_TYPE).equals("2")) {
			gameType = " (960)";
		}

		String draw = AppConstants.SYMBOL_EMPTY;
		if (item.values.get(GameListItem.IS_DRAW_OFFER_PENDING).equals("p"))
			draw = "\n" + context.getString(R.string.drawoffered);

		holder.playerTxt.setText(item.values.get(GameListItem.OPPONENT_USERNAME) + gameType + draw);

		String infoText = AppConstants.SYMBOL_EMPTY;
		if (item.values.get(GameListItem.IS_MY_TURN).equals("1")) {

			String amount = item.values.get(GameListItem.TIME_REMAINING_AMOUNT);
			if (item.values.get(GameListItem.TIME_REMAINING_AMOUNT).substring(0, 1).equals("0"))
				amount = amount.substring(1);
			if (item.values.get(GameListItem.TIME_REMAINING_UNITS).equals("h"))
				infoText = amount + context.getString(R.string.hours);
			else
				infoText = amount + context.getString(R.string.days);
		}

		holder.gameInfoTxt.setText(infoText);
	}

}
