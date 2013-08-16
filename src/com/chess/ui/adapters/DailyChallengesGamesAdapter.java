package com.chess.ui.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.api.DailyChallengeItem;
import com.chess.backend.image_load.AvatarView;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.List;

public class DailyChallengesGamesAdapter extends ItemsAdapter<DailyChallengeItem.Data> {

	private final ItemClickListenerFace clickListenerFace;
	private final int imageSize;

	public DailyChallengesGamesAdapter(ItemClickListenerFace clickListenerFace, List<DailyChallengeItem.Data> itemList) {
		super(clickListenerFace.getMeContext(), itemList);
		imageSize = (int) (resources.getDimension(R.dimen.list_item_image_size_big) / resources.getDisplayMetrics().density);
		this.clickListenerFace = clickListenerFace;
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_daily_challenge_game_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.playerImg = (AvatarView) view.findViewById(R.id.playerImg);
		holder.playerTxt = (TextView) view.findViewById(R.id.playerNameTxt);
		holder.acceptBtn = (TextView) view.findViewById(R.id.acceptBtn);
		holder.cancelBtn = (TextView) view.findViewById(R.id.cancelBtn);

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

		holder.playerTxt.setText(item.getOpponentUsername());

		imageLoader.download(item.getOpponentAvatar(), holder.playerImg, imageSize);
	}

	protected class ViewHolder {
		public AvatarView playerImg;
		public TextView playerTxt;
		public TextView cancelBtn;
		public TextView acceptBtn;
	}

}
