package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 29.01.13
 * Time: 17:28
 */
public class VideosCursorAdapter extends ItemsCursorAdapter {

	public static final String GREY_COLOR_DIVIDER = "##";

	private CharacterStyle foregroundSpan;

	public VideosCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);

		int lightGrey = context.getResources().getColor(R.color.new_subtitle_light_grey);
		foregroundSpan = new ForegroundColorSpan(lightGrey);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_common_description_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		holder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);

		view.setTag(holder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		String firstName = DBDataManager.getString(cursor, DBConstants.V_FIRST_NAME);
		CharSequence chessTitle = DBDataManager.getString(cursor, DBConstants.V_CHESS_TITLE);
		String lastName =  DBDataManager.getString(cursor, DBConstants.V_LAST_NAME);
		CharSequence authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER + StaticData.SYMBOL_SPACE
				+ firstName + StaticData.SYMBOL_SPACE + lastName;
		authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, foregroundSpan);
		holder.authorTxt.setText(authorStr);

		holder.titleTxt.setText(DBDataManager.getString(cursor, DBConstants.V_NAME));


	}

	protected class ViewHolder {
		public TextView titleTxt;
		public TextView authorTxt;
	}
}
