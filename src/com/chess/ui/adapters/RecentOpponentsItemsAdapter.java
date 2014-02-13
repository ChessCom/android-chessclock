package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.image_load.AvatarView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.model.OpponentItem;

import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 17.01.14
 * Time: 16:42
 */
public class RecentOpponentsItemsAdapter extends ItemsAdapter<OpponentItem> {

	private final int imageSize;
	private final HashMap<String, SmartImageFetcher.Data> imageDataMap;

	public RecentOpponentsItemsAdapter(Context context, List<OpponentItem> itemList, SmartImageFetcher imageFetcher) {
		super(context, itemList, imageFetcher);

		imageSize = resources.getDimensionPixelSize(R.dimen.daily_list_item_image_size);

		imageDataMap = new HashMap<String, SmartImageFetcher.Data>();
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.recent_opponent_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.playerImg = (AvatarView) view.findViewById(R.id.playerImg);
		holder.playerTxt = (TextView) view.findViewById(R.id.playerNameTxt);

		view.setTag(holder);
		return view;
	}

	@Override
	protected void bindView(OpponentItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		// get player side, and choose opponent

		holder.playerTxt.setText(item.getName());

		String imageUrl = item.getAvatarUrl();
		if (!imageDataMap.containsKey(imageUrl)) {
			imageDataMap.put(imageUrl, new SmartImageFetcher.Data(item.getAvatarUrl(), imageSize));
		}

		imageFetcher.loadImage(imageDataMap.get(imageUrl), holder.playerImg.getImageView());
	}

	protected class ViewHolder {
		public AvatarView playerImg;
		public TextView playerTxt;
	}
}
