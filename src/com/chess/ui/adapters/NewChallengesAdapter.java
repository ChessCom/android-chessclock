package com.chess.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.gcm.NewChallengeNotificationItem;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;

import java.util.HashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.08.13
 * Time: 9:57
 */
public class NewChallengesAdapter extends ItemsAdapter<NewChallengeNotificationItem> {

	private final int imageSize;
	private final HashMap<String, SmartImageFetcher.Data> imageDataMap;

	public NewChallengesAdapter(Context context, List<NewChallengeNotificationItem> itemList, SmartImageFetcher imageFetcher) {
		super(context, itemList, imageFetcher);
		imageSize = resources.getDimensionPixelSize(R.dimen.daily_list_item_image_size);

		imageDataMap = new HashMap<String, SmartImageFetcher.Data>();
	}

	@Override
	protected View createView(ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_chat_message_update_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.playerImg = (ProgressImageView) view.findViewById(R.id.playerImg);
		holder.messageTxt = (TextView) view.findViewById(R.id.messageTxt);

		view.setTag(holder);
		return view;
	}

	@Override
	protected void bindView(NewChallengeNotificationItem item, int pos, View convertView) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		holder.messageTxt.setText(item.getUsername());

		String imageUrl = item.getAvatar();
		if (!imageDataMap.containsKey(imageUrl)) {
			imageDataMap.put(imageUrl,  new SmartImageFetcher.Data(item.getAvatar(), imageSize));
		}

		imageFetcher.loadImage(imageUrl, holder.playerImg.getImageView());
	}

	protected class ViewHolder {
		public ProgressImageView playerImg;
		public TextView messageTxt;

	}
}
