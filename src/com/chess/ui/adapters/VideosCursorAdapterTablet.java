package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.statics.Symbol;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 10.11.13
 * Time: 12:00
 */
public class VideosCursorAdapterTablet extends VideosCursorAdapter {

	public VideosCursorAdapterTablet(ItemClickListenerFace clickFace, Cursor cursor) {
		super(clickFace, cursor);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.video_lib_thumb_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		holder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);
		holder.durationTxt = (TextView) view.findViewById(R.id.durationTxt);
		holder.completedIconTxt = (TextView) view.findViewById(R.id.completedIconTxt);

		holder.completedIconTxt.setOnClickListener(clickFace);
		view.setTag(holder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		holder.completedIconTxt.setTag(R.id.list_item_id, cursor.getPosition());

		String firstName = DbDataManager.getString(cursor, DbScheme.V_FIRST_NAME);
		CharSequence chessTitle = DbDataManager.getString(cursor, DbScheme.V_CHESS_TITLE);
		String lastName =  DbDataManager.getString(cursor, DbScheme.V_LAST_NAME);
		CharSequence authorStr;
		if (TextUtils.isEmpty(chessTitle)) {
			authorStr = firstName + Symbol.SPACE + lastName;
		} else {
			authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER
					+ Symbol.SPACE + firstName + Symbol.SPACE + lastName;
			authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, foregroundSpan);
		}
		holder.titleTxt.setText(Html.fromHtml(DbDataManager.getString(cursor, DbScheme.V_TITLE)));
		holder.authorTxt.setText(authorStr);
		String durationStr = context.getString(R.string.min_arg, getString(cursor, DbScheme.V_MINUTES));
		String viewsCntStr = SLASH_DIVIDER + context.getString(R.string.views_arg, getString(cursor, DbScheme.V_VIEW_COUNT));
		holder.durationTxt.setText(durationStr + viewsCntStr);

		if (viewedMap.get(getInt(cursor, DbScheme.V_ID), false)) {
			holder.titleTxt.setTextColor(completedTextColor);
			holder.completedIconTxt.setTextColor(completedIconColor);
			holder.completedIconTxt.setText(R.string.ic_check);
			holder.completedIconTxt.setPadding(0, 0, 0, 0);

		} else {
			holder.titleTxt.setTextColor(incompleteTextColor);
			holder.completedIconTxt.setTextColor(incompleteIconColor);
			holder.completedIconTxt.setText(R.string.ic_play);
			holder.completedIconTxt.setPadding((int) (4 * density), 0, 0, 0);
		}
	}
}
