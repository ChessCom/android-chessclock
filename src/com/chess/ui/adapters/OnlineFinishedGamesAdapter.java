package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.model.GameListFinishedItem;

import java.util.List;

public class OnlineFinishedGamesAdapter extends ItemsAdapter<GameListFinishedItem> {


	public OnlineFinishedGamesAdapter(Context context, List<GameListFinishedItem> itemList) {
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
	protected void bindView(GameListFinishedItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		String gameType = StaticData.SYMBOL_EMPTY;

		if (item.getGameType() != null && item.getGameType().equals("2")) {
			gameType = " (960)";
		}

		String result = context.getString(R.string.lost);
		if (item.getGameResult().equals("1")) {
			result = context.getString(R.string.won);
		} else if (item.getGameResult().equals("2")) {
			result = context.getString(R.string.draw);
		}

		holder.playerTxt.setText(item.getOpponentUsername() + gameType);
		holder.gameInfoTxt.setText(result);
	}

	protected class ViewHolder {
		public TextView playerTxt;
		public TextView gameInfoTxt;
	}
}
