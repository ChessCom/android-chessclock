package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.db.DbScheme;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.08.13
 * Time: 5:31
 */
public class NewChatMessagesCursorAdapter extends ItemsCursorAdapter {

	private final int imageSize;

	public NewChatMessagesCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		imageSize = (int) (resources.getDimension(R.dimen.list_item_image_size_big) / resources.getDisplayMetrics().density);
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
		imageLoader.download(avatarUrl, holder.playerImg, imageSize);
	}

	protected class ViewHolder {
		public ProgressImageView playerImg;
		public TextView messageTxt;

	}
}
