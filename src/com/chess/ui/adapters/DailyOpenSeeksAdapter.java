package com.chess.ui.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.daily_games.DailyChallengeItem;
import com.chess.backend.image_load.AvatarView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.statics.Symbol;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.FontsHelper;
import com.chess.widgets.RoboTextView;

import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 09.01.14
 * Time: 22:08
 */
public class DailyOpenSeeksAdapter extends ItemsAdapter<DailyChallengeItem.Data> {

	private final HashMap<String, SmartImageFetcher.Data> imageDataMap;
	private final int imageSize;
	private final ItemClickListenerFace clickListenerFace;

	public DailyOpenSeeksAdapter(ItemClickListenerFace clickListenerFace, List<DailyChallengeItem.Data> itemList, SmartImageFetcher imageFetcher) {
		super(clickListenerFace.getMeContext(), itemList, imageFetcher);
		this.clickListenerFace = clickListenerFace;
		imageSize = resources.getDimensionPixelSize(R.dimen.daily_list_item_image_size);
		imageDataMap = new HashMap<String, SmartImageFetcher.Data>();
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.daily_finished_games_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.playerImg = (AvatarView) view.findViewById(R.id.playerImg);
		holder.playerTxt = (TextView) view.findViewById(R.id.playerNameTxt);
		holder.premiumImg = (ImageView) view.findViewById(R.id.premiumImg);
		holder.ratingTxt = (TextView) view.findViewById(R.id.ratingTxt);
		holder.gameResultTxt = (RoboTextView) view.findViewById(R.id.gameResultTxt);
		holder.gameTypeTxt = (TextView) view.findViewById(R.id.gameTypeTxt);

		holder.premiumImg.setVisibility(View.GONE);

		holder.gameResultTxt.setText(R.string.ic_check);
		holder.gameResultTxt.setFont(FontsHelper.ICON_FONT);
		holder.gameResultTxt.setTextSize(resources.getDimensionPixelSize(R.dimen.glyph_icon_big) / density);
		holder.gameResultTxt.setTextColor(resources.getColor(R.color.new_light_grey_2));
		holder.gameResultTxt.setOnClickListener(clickListenerFace);

		view.setTag(holder);
		return view;
	}

	@Override
	protected void bindView(DailyChallengeItem.Data item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		holder.gameResultTxt.setTag(itemListId, pos);

		// get player side, and choose opponent
		String avatarUrl = item.getOpponentAvatar();
		String opponentName = item.getOpponentUsername();
		int opponentRating = item.getOpponentRating();

		holder.playerTxt.setText(opponentName);
		holder.ratingTxt.setText(Symbol.wrapInPars(opponentRating));

		if (!imageDataMap.containsKey(avatarUrl)) {
			imageDataMap.put(avatarUrl, new SmartImageFetcher.Data(avatarUrl, imageSize));
		}

		ImageView imageView = holder.playerImg.getImageView();
		imageFetcher.loadImage(imageDataMap.get(avatarUrl), imageView);

		holder.playerImg.setOnline(item.isOpponentOnline());

		if (item.getGameTypeId() == RestHelper.V_GAME_CHESS) {
			holder.gameTypeTxt.setText(R.string.ic_daily_game);
		} else {
			holder.gameTypeTxt.setText(R.string.ic_daily960_game);
		}
	}

	protected class ViewHolder {
		public AvatarView playerImg;
		public TextView playerTxt;
		public ImageView premiumImg;
		public TextView ratingTxt;
		public RoboTextView gameResultTxt;
		public TextView gameTypeTxt;
	}

}
