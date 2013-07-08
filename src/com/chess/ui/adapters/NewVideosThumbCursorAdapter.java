package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 29.01.13
 * Time: 17:28
 */
public class NewVideosThumbCursorAdapter extends ItemsCursorAdapter {

	public static final String GREY_COLOR_DIVIDER = "##";
	public static final String DURATION_DIVIDER = "| ";

	private final ItemClickListenerFace clickFace;

	private CharacterStyle foregroundSpan;

	public NewVideosThumbCursorAdapter(ItemClickListenerFace clickFace, Cursor cursor) {
		super(clickFace.getMeContext(), cursor);

		int lightGrey = context.getResources().getColor(R.color.new_subtitle_light_grey);
		foregroundSpan = new ForegroundColorSpan(lightGrey);

		this.clickFace = clickFace;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_video_lib_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		holder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);
		holder.durationTxt = (TextView) view.findViewById(R.id.durationTxt);
		holder.watchedIconTxt = (TextView) view.findViewById(R.id.watchedIconTxt);

		holder.watchedIconTxt.setOnClickListener(clickFace);
		holder.titleTxt.setOnClickListener(clickFace);
		holder.authorTxt.setOnClickListener(clickFace);

		view.setTag(holder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		holder.titleTxt.setTag(R.id.list_item_id, cursor.getPosition());
		holder.authorTxt.setTag(R.id.list_item_id, cursor.getPosition());
		holder.watchedIconTxt.setTag(R.id.list_item_id, cursor.getPosition());

		String firstName = DBDataManager.getString(cursor, DBConstants.V_FIRST_NAME);
		CharSequence chessTitle = DBDataManager.getString(cursor, DBConstants.V_CHESS_TITLE);
		String lastName =  DBDataManager.getString(cursor, DBConstants.V_LAST_NAME);
		CharSequence authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER + StaticData.SYMBOL_SPACE
				+ firstName + StaticData.SYMBOL_SPACE + lastName;
		authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, foregroundSpan);
		holder.authorTxt.setText(authorStr);
		holder.durationTxt.setText(DURATION_DIVIDER +
				context.getString(R.string.min_arg, getString(cursor, DBConstants.V_MINUTES)));
		holder.titleTxt.setText(DBDataManager.getString(cursor, DBConstants.V_TITLE));
	}

	protected class ViewHolder {
		public TextView titleTxt;
		public TextView authorTxt;
		public TextView durationTxt;
		public TextView watchedIconTxt;
	}
}
