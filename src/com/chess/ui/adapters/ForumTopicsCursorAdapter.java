package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.db.DbScheme;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.07.13
 * Time: 8:26
 */
public class ForumTopicsCursorAdapter extends ItemsCursorAdapter {

	public static final String DIVIDER = " | ";

	public ForumTopicsCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_forum_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.newPostImg = (ImageView) view.findViewById(R.id.newPostImg);
		holder.titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		holder.lastCommentAgoTxt = (TextView) view.findViewById(R.id.lastCommentAgoTxt);
		holder.postsCountTxt = (TextView) view.findViewById(R.id.postsCountTxt);

		view.setTag(holder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		long timestamp = getLong(cursor, DbScheme.V_LAST_POST_DATE);
		String lastCommentAgoStr = AppUtils.getMomentsAgoFromSeconds(timestamp, context);
		holder.lastCommentAgoTxt.setText(lastCommentAgoStr + DIVIDER);
		holder.titleTxt.setText(Html.fromHtml(getString(cursor, DbScheme.V_TITLE)));

		int postCount = getInt(cursor, DbScheme.V_POST_COUNT);
		holder.postsCountTxt.setText(context.getString(R.string.posts_arg, postCount));

//		if (haveNewPosts()) {
//			holder.newPostImg.setImageResource(R.drawable.ic_new_post_t);
//		} else {
//			holder.newPostImg.setImageResource(R.drawable.ic_new_post_f);
//		}
	}

	private boolean haveNewPosts() {
		return true;
	}

	protected class ViewHolder {
		public ImageView newPostImg;
		public TextView titleTxt;
		public TextView lastCommentAgoTxt;
		public TextView postsCountTxt;
	}
}