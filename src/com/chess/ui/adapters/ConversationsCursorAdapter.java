package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.image_load.AvatarView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.db.DbScheme;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.utilities.AppUtils;
import com.chess.utilities.FontsHelper;
import com.chess.widgets.RoboTextView;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 01.08.13
 * Time: 17:07
 */
public class ConversationsCursorAdapter extends ItemsCursorAdapter {

	public static final String ORIGINAL_MESSAGE_BY = "Original Message by";
	public static final String MESSAGE_SEPARATOR = "----------------------------------------------------------------------";

	private final int paddingTop;
	private final int paddingSide;
	private final HashMap<String, SmartImageFetcher.Data> imageDataMap;

	private int imageSize;

	public ConversationsCursorAdapter(Context context, Cursor cursor, SmartImageFetcher imageFetcher) {
		super(context, cursor, imageFetcher);
		float density = resources.getDisplayMetrics().density;
		imageSize = (int) (40 * density);
		paddingTop = resources.getDimensionPixelSize(R.dimen.smaller_scr_side_padding);
		paddingSide = resources.getDimensionPixelSize(R.dimen.default_scr_side_padding);

		imageDataMap = new HashMap<String, SmartImageFetcher.Data>();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.conversation_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.photoImg = (AvatarView) view.findViewById(R.id.photoImg);
		holder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);
		holder.lastMessageTxt = (RoboTextView) view.findViewById(R.id.lastMessageTxt);
		holder.lastMessageDateTxt = (TextView) view.findViewById(R.id.lastMessageDateTxt);

		view.setTag(holder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		boolean isOpponentUserOnline = getInt(cursor, DbScheme.V_OTHER_USER_IS_ONLINE) > 0;
		holder.photoImg.setOnline(isOpponentUserOnline);

		boolean haveNewMessages = getInt(cursor, DbScheme.V_NEW_MESSAGES_COUNT) > 0;
		if (haveNewMessages) {
			holder.lastMessageTxt.setFont(FontsHelper.BOLD_FONT);
			ButtonDrawableBuilder.setBackgroundToView(view, R.style.ListItem_Header_2_Light);

		} else {
			holder.lastMessageTxt.setFont(FontsHelper.DEFAULT_FONT);
			ButtonDrawableBuilder.setBackgroundToView(view, R.style.ListItem_Header_2);
		}
		view.setPadding(paddingSide, paddingTop, paddingSide, paddingTop);

		String otherUserAvatarUrl = getString(cursor, DbScheme.V_OTHER_USER_AVATAR_URL);
		if (!imageDataMap.containsKey(otherUserAvatarUrl)) {
			imageDataMap.put(otherUserAvatarUrl, new SmartImageFetcher.Data(otherUserAvatarUrl, imageSize));
		}

		imageFetcher.loadImage(imageDataMap.get(otherUserAvatarUrl), holder.photoImg.getImageView());

		holder.authorTxt.setText(getString(cursor, DbScheme.V_OTHER_USER_USERNAME));
		String message = getString(cursor, DbScheme.V_LAST_MESSAGE_CONTENT); // already saved in DB as plain text
		if (message.contains(ORIGINAL_MESSAGE_BY)) {
			int quoteStart = message.indexOf(MESSAGE_SEPARATOR);
			message = message.substring(0, quoteStart);
		}

		holder.lastMessageTxt.setText(message);
		long timeAgo = getLong(cursor, DbScheme.V_LAST_MESSAGE_CREATED_AT);
		String lastDate = AppUtils.getMomentsAgoFromSeconds(timeAgo, context);
		holder.lastMessageDateTxt.setText(lastDate);
	}

	private static class ViewHolder {
		private AvatarView photoImg;
		private TextView authorTxt;
		private RoboTextView lastMessageTxt;
		private TextView lastMessageDateTxt;
	}
}
