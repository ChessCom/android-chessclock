package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.db.DBConstants;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 13.07.13
 * Time: 20:21
 */
public class ForumPostsCursorAdapter extends ItemsCursorAdapter {

	public ForumPostsCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_forum_post_list_item, parent, false);
		ViewHolder holder = new ViewHolder();

		holder.thumbnailAuthorImg = (ImageView) view.findViewById(R.id.thumbnailAuthorImg);
		holder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);
		holder.countryImg = (ImageView) view.findViewById(R.id.countryImg);
		holder.dateTxt = (TextView) view.findViewById(R.id.dateTxt);
		holder.quoteTxt = (TextView) view.findViewById(R.id.quoteTxt);
		holder.bodyTxt = (TextView) view.findViewById(R.id.bodyTxt);

		view.setTag(holder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		holder.authorTxt.setText(getString(cursor, DBConstants.V_USERNAME));
		holder.bodyTxt.setText(Html.fromHtml(getString(cursor, DBConstants.V_DESCRIPTION)));

		long timestamp = getLong(cursor, DBConstants.V_CREATE_DATE);
		String lastCommentAgoStr = AppUtils.getMomentsAgoFromSeconds(timestamp, context);
		holder.dateTxt.setText(lastCommentAgoStr);
	}

	protected class ViewHolder {
		public ImageView thumbnailAuthorImg;
		public TextView authorTxt;
		public ImageView countryImg;
		public TextView dateTxt;
		public TextView quoteTxt;
		public TextView bodyTxt;
	}
}
