package com.chess.ui.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.new_api.DailyChallengeItem;
import com.chess.backend.statics.StaticData;
import com.chess.model.BaseGameItem;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.List;

public class DailyChallengesGamesAdapter extends ItemsAdapter<DailyChallengeItem.Data> {

	private final ItemClickListenerFace clickListenerFace;

	public DailyChallengesGamesAdapter(ItemClickListenerFace clickListenerFace, List<DailyChallengeItem.Data> itemList) {
		super(clickListenerFace.getMeContext(), itemList);
		this.clickListenerFace = clickListenerFace;
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_daily_challenge_game_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.playerTxt = (TextView) view.findViewById(R.id.playerNameTxt);
		holder.gameTimeTxt = (TextView) view.findViewById(R.id.gameTimeTxt);
		holder.ratedInfoTxt = (TextView) view.findViewById(R.id.ratedInfoTxt);
		holder.acceptBtn = (ImageView) view.findViewById(R.id.acceptBtn);
		holder.cancelBtn = (ImageView) view.findViewById(R.id.cancelBtn);

		holder.acceptBtn.setOnClickListener(clickListenerFace);
		holder.cancelBtn.setOnClickListener(clickListenerFace);

		view.setTag(holder);
		return view;
	}


	@Override
	protected void bindView(DailyChallengeItem.Data item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		holder.cancelBtn.setTag(itemListId, pos);
		holder.acceptBtn.setTag(itemListId, pos);

		String time = item.getDaysPerMove() + context.getString(R.string.days);
		String gameType = StaticData.SYMBOL_EMPTY;

		if (item.getGameType() == BaseGameItem.CHESS_960) {
			gameType = " (960)";
		}

		String opponentRating = StaticData.SYMBOL_LEFT_PAR + item.getOpponentRating() + StaticData.SYMBOL_RIGHT_PAR;
		String userName = item.getOpponentUsername();

		holder.playerTxt.setText(userName + StaticData.SYMBOL_SPACE + opponentRating);
		holder.gameTimeTxt.setText(gameType + StaticData.SYMBOL_SPACE + time);
		if (!item.isRated()) {
			holder.ratedInfoTxt.setText(context.getString(R.string.unrated));
		}
	}

	protected class ViewHolder {
		public TextView playerTxt;
		public TextView gameTimeTxt;
		public TextView ratedInfoTxt;
		public ImageView cancelBtn;
		public ImageView acceptBtn;
	}

}
