package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.db.DBConstants;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.07.13
 * Time: 18:54
 */
public class NewForumsSectionedCursorAdapter extends NewSectionedCursorLimitedAdapter {

	public NewForumsSectionedCursorAdapter(Context context, Cursor cursor, int itemsPerSectionCnt) {
		super(context, cursor, R.layout.new_arrow_section_header, DBConstants.V_CATEGORY, itemsPerSectionCnt);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_forum_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		holder.lastCommentAgoTxt = (TextView) view.findViewById(R.id.lastCommentAgoTxt);
		holder.postsCountTxt = (TextView) view.findViewById(R.id.postsCountTxt);

		view.setTag(holder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		long timestamp = getLong(cursor, DBConstants.V_LAST_POST_DATE);
		String lastCommentAgoStr = AppUtils.getMomentsAgoFromSeconds(timestamp, context);
		holder.lastCommentAgoTxt.setText(lastCommentAgoStr);
		holder.titleTxt.setText(getString(cursor, DBConstants.V_TITLE));

		int postCount = getInt(cursor, DBConstants.V_POST_COUNT);
		holder.postsCountTxt.setText(context.getString(R.string.posts_arg, postCount));
	}

	protected class ViewHolder {
		public TextView titleTxt;
		public TextView lastCommentAgoTxt;
		public TextView postsCountTxt;
	}
}

