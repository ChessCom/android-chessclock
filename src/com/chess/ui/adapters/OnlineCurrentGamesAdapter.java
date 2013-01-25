package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.model.BaseGameItem;
import com.chess.model.GameListCurrentItem;

import java.util.List;

public class OnlineCurrentGamesAdapter extends ItemsAdapter<GameListCurrentItem> {


	protected static final String CHESS_960 = " (960)";
	protected static final String HOUR_SYMBOL = "h";

	public OnlineCurrentGamesAdapter(Context context, List<GameListCurrentItem> itemList) {
		super(context, itemList);
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.game_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.playerTxt = (TextView) view.findViewById(R.id.playerTxt);
		holder.gameInfoTxt = (TextView) view.findViewById(R.id.gameInfoTxt);

		view.setTag(holder);
		return view;
	}

	@Override
	protected void bindView(GameListCurrentItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		String gameType = StaticData.SYMBOL_EMPTY;
		if (item.getGameType() == BaseGameItem.CHESS_960) {
			gameType = CHESS_960;
		}

		String draw = StaticData.SYMBOL_EMPTY;
		if (item.isDrawOfferPending()) {
			draw = "\n" + context.getString(R.string.draw_offered);
		}

		holder.playerTxt.setText(item.getOpponentUsername() + gameType + draw);

		String infoText = StaticData.SYMBOL_EMPTY;
		if (item.isMyTurn()) {

			int amount = item.getTimeRemainingAmount();

			if (item.getTimeRemainingUnits().equals(HOUR_SYMBOL))
				infoText = amount + context.getString(R.string.hours);
			else
				infoText = amount + context.getString(R.string.days);
		}

		holder.gameInfoTxt.setText(infoText);
	}

	protected class ViewHolder {
		public TextView playerTxt;
		public TextView gameInfoTxt;
	}
}
