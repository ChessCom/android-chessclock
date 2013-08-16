package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.RoboTextView;
import com.chess.backend.image_load.AvatarView;
import com.chess.db.DbScheme;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 02.08.13
 * Time: 5:46
 */
public class MessagesCursorAdapter extends ItemsCursorAdapter {

	private int imgSize;
	public MessagesCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		imgSize = (int) (40 * resources.getDisplayMetrics().density);
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
		imageLoader.download(otherUserAvatarUrl, holder.photoImg, imgSize);

		holder.authorTxt.setText(getString(cursor, DbScheme.V_OTHER_USER_USERNAME));
		Spanned message = Html.fromHtml(getString(cursor, DbScheme.V_LAST_MESSAGE_CONTENT));
		holder.messageTxt.setText(message);
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
