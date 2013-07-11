package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.db.DBConstants;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 31.01.13
 * Time: 13:34
 */
public class NewVideosSectionedCursorAdapter extends NewSectionedCursorLimitedAdapter {

	public static final String GREY_COLOR_DIVIDER = "##";
	public static final String DURATION_DIVIDER = "| ";
	private final int watchedTextColor;
	private final int unWatchedTextColor;
	private final int watchedIconColor;
	private final int unWatchedIconColor;

	private CharacterStyle foregroundSpan;
	private SparseBooleanArray viewedMap;

	public NewVideosSectionedCursorAdapter(Context context, Cursor cursor, int itemsPerSectionCnt) {
		super(context, cursor, R.layout.new_arrow_section_header, DBConstants.V_CATEGORY, itemsPerSectionCnt);

		int lightGrey = resources.getColor(R.color.new_subtitle_light_grey);
		foregroundSpan = new ForegroundColorSpan(lightGrey);

		watchedTextColor = resources.getColor(R.color.new_light_grey_3);
		unWatchedTextColor = resources.getColor(R.color.new_text_blue);
		watchedIconColor = resources.getColor(R.color.new_light_grey_2);
		unWatchedIconColor = resources.getColor(R.color.orange_button);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_common_description_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		holder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);
		holder.durationTxt = (TextView) view.findViewById(R.id.durationTxt);
		holder.icon = (TextView) view.findViewById(R.id.watchedIconTxt);

		view.setTag(holder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		String firstName = getString(cursor, DBConstants.V_FIRST_NAME);
		CharSequence chessTitle = getString(cursor, DBConstants.V_CHESS_TITLE);
		String lastName =  getString(cursor, DBConstants.V_LAST_NAME);
		CharSequence authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER + StaticData.SYMBOL_SPACE
				+ firstName + StaticData.SYMBOL_SPACE + lastName;
		authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, foregroundSpan);
		holder.authorTxt.setText(authorStr);

		holder.durationTxt.setText(DURATION_DIVIDER +
				context.getString(R.string.min_arg, getString(cursor, DBConstants.V_MINUTES)));

		holder.titleTxt.setText(getString(cursor, DBConstants.V_TITLE));
		if (viewedMap.get(getInt(cursor, DBConstants.V_ID), false)) {
			holder.titleTxt.setTextColor(watchedTextColor);
			holder.icon.setTextColor(watchedIconColor);
			holder.icon.setText(R.string.ic_check);
		} else {
			holder.titleTxt.setTextColor(unWatchedTextColor);
			holder.icon.setTextColor(unWatchedIconColor);
			holder.icon.setText(R.string.ic_play);
		}

	}

	public void addViewedMap(SparseBooleanArray viewedMap) {
		this.viewedMap = viewedMap;
	}

	protected class ViewHolder {
		public TextView titleTxt;
		public TextView authorTxt;
		public TextView durationTxt;
		public TextView icon;
	}
}