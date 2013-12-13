package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.widgets.RoboTextView;
import com.chess.backend.image_load.AvatarView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.db.DbScheme;
import com.chess.utilities.AppUtils;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 02.08.13
 * Time: 5:46
 */
public class MessagesCursorAdapter extends ItemsCursorAdapter {

	public static final String ORIGINAL_MESSAGE_BY = "Original Message by";
	public static final String MESSAGE_SEPARATOR = "----------------------------------------------------------------------";

	private final HashMap<String, SmartImageFetcher.Data> imageDataMap;
	private int imageSize;
	public MessagesCursorAdapter(Context context, Cursor cursor, SmartImageFetcher imageFetcher) {
		super(context, cursor, imageFetcher);
		imageSize = resources.getDimensionPixelSize(R.dimen.daily_list_item_image_size);
		imageDataMap = new HashMap<String, SmartImageFetcher.Data>();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_messages_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.photoImg = (AvatarView) view.findViewById(R.id.photoImg);
		holder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);
		holder.messageTxt = (RoboTextView) view.findViewById(R.id.messageTxt);
		holder.messageDateTxt = (TextView) view.findViewById(R.id.messageDateTxt);

		view.setTag(holder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		boolean isOpponentOnline = getInt(cursor, DbScheme.V_OTHER_USER_IS_ONLINE) > 0;
		holder.photoImg.setOnline(isOpponentOnline);

		String otherUserAvatarUrl = getString(cursor, DbScheme.V_OTHER_USER_AVATAR_URL);

		if (!imageDataMap.containsKey(otherUserAvatarUrl)) {
			imageDataMap.put(otherUserAvatarUrl, new SmartImageFetcher.Data(otherUserAvatarUrl, imageSize));
		}

		imageFetcher.loadImage(imageDataMap.get(otherUserAvatarUrl), holder.photoImg.getImageView());

		holder.authorTxt.setText(getString(cursor, DbScheme.V_OTHER_USER_USERNAME));
		String message = getString(cursor, DbScheme.V_LAST_MESSAGE_CONTENT);
		if (message.contains(ORIGINAL_MESSAGE_BY)) {
			int quoteStart = message.indexOf(MESSAGE_SEPARATOR);
			message = message.substring(0, quoteStart);
		}

		loadTextWithImage(holder.messageTxt, message, imageSize);

		long timeAgo = getLong(cursor, DbScheme.V_CREATE_DATE);
		String lastDate = AppUtils.getMomentsAgoFromSeconds(timeAgo, context);
		holder.messageDateTxt.setText(lastDate);
	}

	private static class ViewHolder {
		private AvatarView photoImg;
		private TextView authorTxt;
		private RoboTextView messageTxt;
		private TextView messageDateTxt;
	}
}
