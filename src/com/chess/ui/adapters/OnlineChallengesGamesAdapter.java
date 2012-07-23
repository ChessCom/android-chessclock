package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.model.GameListChallengeItem;
import com.chess.model.GameListItem;

import java.util.List;

public class OnlineChallengesGamesAdapter extends ItemsAdapter<GameListChallengeItem> {

	public OnlineChallengesGamesAdapter(Context context, List<GameListChallengeItem> itemList) {
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
	protected void bindView(GameListChallengeItem item, int pos, View convertView) {
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

	protected class ViewHolder {
		public TextView playerTxt;
		public TextView gameInfoTxt;
	}

}
