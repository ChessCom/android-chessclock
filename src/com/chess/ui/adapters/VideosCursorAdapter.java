package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 29.01.13
 * Time: 17:28
 */
public class VideosCursorAdapter extends ItemsCursorAdapter {

	public static final String GREY_COLOR_DIVIDER = "##";
	public static final String DURATION_DIVIDER = "| ";

	private final ItemClickListenerFace clickFace;
	private final int watchedTextColor;
	private final int unWatchedTextColor;
	private final int watchedIconColor;
	private final int unWatchedIconColor;

	private CharacterStyle foregroundSpan;
	private SparseBooleanArray viewedMap;

	public VideosCursorAdapter(ItemClickListenerFace clickFace, Cursor cursor) {
		super(clickFace.getMeContext(), cursor);

		int lightGrey = context.getResources().getColor(R.color.new_subtitle_light_grey);
		foregroundSpan = new ForegroundColorSpan(lightGrey);

		watchedTextColor = resources.getColor(R.color.new_light_grey_3);
		unWatchedTextColor = resources.getColor(R.color.new_text_blue);
		watchedIconColor = resources.getColor(R.color.new_light_grey_2);
		unWatchedIconColor = resources.getColor(R.color.orange_button);
		this.clickFace = clickFace;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_video_lib_list_item, parent, false);
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
			authorStr = firstName + StaticData.SYMBOL_SPACE + lastName;
		} else {
			authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER
					+ StaticData.SYMBOL_SPACE + firstName + StaticData.SYMBOL_SPACE + lastName;
			authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, foregroundSpan);
		}
		holder.authorTxt.setText(authorStr);
		holder.durationTxt.setText(DURATION_DIVIDER +
				context.getString(R.string.min_arg, getString(cursor, DbScheme.V_MINUTES)));
		holder.titleTxt.setText(DbDataManager.getString(cursor, DbScheme.V_TITLE));

		if (viewedMap.get(getInt(cursor, DbScheme.V_ID), false)) {
			holder.titleTxt.setTextColor(watchedTextColor);
			holder.completedIconTxt.setTextColor(watchedIconColor);
			holder.completedIconTxt.setText(R.string.ic_check);
		} else {
			holder.titleTxt.setTextColor(unWatchedTextColor);
			holder.completedIconTxt.setTextColor(unWatchedIconColor);
			holder.completedIconTxt.setText(R.string.ic_play);
		}
	}

	public void addViewedMap(SparseBooleanArray viewedMap) {
		this.viewedMap = viewedMap;
	}

	protected class ViewHolder {
		public TextView titleTxt;
		public TextView authorTxt;
		public TextView durationTxt;
		public TextView completedIconTxt;
	}
}
