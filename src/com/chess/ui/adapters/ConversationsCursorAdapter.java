package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.FontsHelper;
import com.chess.R;
import com.chess.RoboTextView;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.db.DbScheme;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 01.08.13
 * Time: 17:07
 */
public class ConversationsCursorAdapter extends ItemsCursorAdapter {

	private final int paddingTop;
	private final int paddingSide;

	private int imgSize;
	public ConversationsCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		float density = resources.getDisplayMetrics().density;
		imgSize = (int) (40 * density);
		paddingTop = (int) (12 * density);
		paddingSide = (int) (12 * density);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_conversation_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.photoImg = (ProgressImageView) view.findViewById(R.id.photoImg);
		holder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);
		holder.lastMessageTxt = (RoboTextView) view.findViewById(R.id.lastMessageTxt);
		holder.lastMessageDateTxt = (TextView) view.findViewById(R.id.lastMessageDateTxt);

		view.setTag(holder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		boolean isOtherUserOnline = getInt(cursor, DbScheme.V_OTHER_USER_IS_ONLINE) > 0;

		boolean haveNewMessages = getInt(cursor, DbScheme.V_NEW_MESSAGES_COUNT) > 0;
		if (haveNewMessages) {
			holder.lastMessageTxt.setFont(FontsHelper.BOLD_FONT);
			view.setBackgroundResource(R.drawable.white_list_item_selector);

		} else {
			holder.lastMessageTxt.setFont(FontsHelper.DEFAULT_FONT);
			ButtonDrawableBuilder.setBackgroundToView(view, R.style.ListItem_Header_2_Light);
		}
		view.setPadding(paddingSide, paddingTop, paddingSide, paddingTop);

		String otherUserAvatarUrl = getString(cursor, DbScheme.V_OTHER_USER_AVATAR_URL);
		imageLoader.download(otherUserAvatarUrl, holder.photoImg, imgSize);

		holder.authorTxt.setText(getString(cursor, DbScheme.V_OTHER_USER_USERNAME));
		Spanned message = Html.fromHtml(getString(cursor, DbScheme.V_LAST_MESSAGE_CONTENT));
		holder.lastMessageTxt.setText(message);
		long timeAgo = getLong(cursor, DbScheme.V_LAST_MESSAGE_CREATED_AT);
		String lastDate = AppUtils.getMomentsAgoFromSeconds(timeAgo, context);
		holder.lastMessageDateTxt.setText(lastDate);
	}

	private static class ViewHolder {
		private ProgressImageView photoImg;
		private TextView authorTxt;
		private RoboTextView lastMessageTxt;
		private TextView lastMessageDateTxt;

	}
}
