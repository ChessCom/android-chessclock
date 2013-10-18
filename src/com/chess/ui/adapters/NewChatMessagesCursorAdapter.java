package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.db.DbScheme;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.08.13
 * Time: 5:31
 */
public class NewChatMessagesCursorAdapter extends ItemsCursorAdapter {

	private final int imageSize;
	private final HashMap<String, SmartImageFetcher.Data> imageDataMap;

	public NewChatMessagesCursorAdapter(Context context, Cursor cursor, SmartImageFetcher imageFetcher) {
		super(context, cursor, imageFetcher);
		imageSize = (int) (resources.getDimension(R.dimen.daily_list_item_image_size) / resources.getDisplayMetrics().density);
		imageDataMap = new HashMap<String, SmartImageFetcher.Data>();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_chat_message_update_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.playerImg = (ProgressImageView) view.findViewById(R.id.playerImg);
		holder.messageTxt = (TextView) view.findViewById(R.id.messageTxt);

		view.setTag(holder);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		holder.messageTxt.setText(getString(cursor, DbScheme.V_MESSAGE));
		String avatarUrl = getString(cursor, DbScheme.V_USER_AVATAR);

		if (!imageDataMap.containsKey(avatarUrl)) {
			imageDataMap.put(avatarUrl, new SmartImageFetcher.Data(avatarUrl, imageSize));
		}

		imageFetcher.loadImage(imageDataMap.get(avatarUrl), holder.playerImg.getImageView());
	}

	protected class ViewHolder {
		public ProgressImageView playerImg;
		public TextView messageTxt;

	}
}
