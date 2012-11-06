package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.model.BaseGameItem;
import com.chess.model.GameListChallengeItem;

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

		String time = item.getDaysPerMove() + context.getString(R.string.days);
		String gameType = StaticData.SYMBOL_EMPTY;

		if (item.getGameType() == BaseGameItem.CHESS_960) {
			gameType = " (960)";
		}

		String opponentRating = StaticData.SYMBOL_LEFT_PAR + item.getOpponentRating() + StaticData.SYMBOL_RIGHT_PAR;
		String userName = item.getOpponentUsername();

		holder.playerTxt.setText(userName + StaticData.SYMBOL_SPACE + opponentRating);
		holder.gameInfoTxt.setText( gameType + StaticData.SYMBOL_SPACE + time);
	}

	protected class ViewHolder {
		public TextView playerTxt;
		public TextView gameInfoTxt;
	}

}
