package com.chess.ui.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.api.daily_games.DailyChallengeItem;
import com.chess.backend.image_load.AvatarView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.widgets.ProfileImageView;

import java.util.HashMap;
import java.util.List;

public class DailyChallengesGamesAdapter extends ItemsAdapter<DailyChallengeItem.Data> {

	private final ItemClickListenerFace clickListenerFace;
	private final int imageSize;
	private final HashMap<String, SmartImageFetcher.Data> imageDataMap;
	private ProfileImageView.ProfileOpenFace profileOpenFace;

	public DailyChallengesGamesAdapter(ItemClickListenerFace clickListenerFace, List<DailyChallengeItem.Data> itemList,
									   SmartImageFetcher imageFetcher, ProfileImageView.ProfileOpenFace profileOpenFace) {
		super(clickListenerFace.getMeContext(), itemList, imageFetcher);
		this.profileOpenFace = profileOpenFace;
		imageSize = resources.getDimensionPixelSize(R.dimen.daily_list_item_image_size);
		this.clickListenerFace = clickListenerFace;
		imageDataMap = new HashMap<String, SmartImageFetcher.Data>();

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
		holder.playerImg.getImageView().setUsername(item.getOpponentUsername(), profileOpenFace);

		String imageUrl = item.getOpponentAvatar();
		if (!imageDataMap.containsKey(imageUrl)) {
			imageDataMap.put(imageUrl, new SmartImageFetcher.Data(imageUrl, imageSize));
		}

		imageFetcher.loadImage(imageDataMap.get(imageUrl), holder.playerImg.getImageView());
	}

	protected class ViewHolder {
		public AvatarView playerImg;
		public TextView playerTxt;
		public TextView cancelBtn;
		public TextView acceptBtn;
	}

}
