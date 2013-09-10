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
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.statics.Symbol;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.utilities.AppUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 29.01.13
 * Time: 17:28
 */
public class ArticlesThumbCursorAdapter extends ItemsCursorAdapter {

	public static final String GREY_COLOR_DIVIDER = "##";
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");
	private final int watchedTextColor;
	private final int unWatchedTextColor;
	private int PHOTO_SIZE;
	private CharacterStyle foregroundSpan;
	private Date date;
	private SparseBooleanArray viewedMap;

	public ArticlesThumbCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);

		int lightGrey = context.getResources().getColor(R.color.new_subtitle_light_grey);
		foregroundSpan = new ForegroundColorSpan(lightGrey);

		watchedTextColor = resources.getColor(R.color.new_light_grey_3);
		unWatchedTextColor = resources.getColor(R.color.new_text_blue);

		date = new Date();

		PHOTO_SIZE = (int) context.getResources().getDimension(R.dimen.article_thumb_width);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_article_thumb_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.thumbnailImg = (ProgressImageView) view.findViewById(R.id.thumbnailImg);
		holder.titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		holder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);
		holder.dateTxt = (TextView) view.findViewById(R.id.dateTxt);

		view.setTag(holder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		String firstName = DbDataManager.getString(cursor, DbScheme.V_FIRST_NAME).equals("")? "TestFirstName" : DbDataManager.getString(cursor, DbScheme.V_FIRST_NAME);
		String chessTitle = DbDataManager.getString(cursor, DbScheme.V_CHESS_TITLE).equals("")? "TIM" : DbDataManager.getString(cursor, DbScheme.V_CHESS_TITLE);
		String lastName = DbDataManager.getString(cursor, DbScheme.V_LAST_NAME).equals("")? "TestLastName" : DbDataManager.getString(cursor, DbScheme.V_LAST_NAME);
		CharSequence authorStr;
		if (TextUtils.isEmpty(chessTitle)) {
			authorStr = firstName + Symbol.SPACE + lastName;
		} else {
			authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER
					+ Symbol.SPACE + firstName + Symbol.SPACE + lastName;
			authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, foregroundSpan);
		}
		holder.authorTxt.setText(authorStr);

		holder.titleTxt.setText(DbDataManager.getString(cursor, DbScheme.V_TITLE));
		date.setTime(DbDataManager.getLong(cursor, DbScheme.V_CREATE_DATE) * 1000L);
		holder.dateTxt.setText(dateFormatter.format(date));

		imageLoader.download(DbDataManager.getString(cursor, DbScheme.V_PHOTO_URL), holder.thumbnailImg, PHOTO_SIZE );

		if (viewedMap.get(getInt(cursor, DbScheme.V_ID), false)) {
			holder.titleTxt.setTextColor(watchedTextColor);
		} else {
			holder.titleTxt.setTextColor(unWatchedTextColor);
		}
	}

	public void addViewedMap(SparseBooleanArray viewedArticlesMap) {
		this.viewedMap = viewedArticlesMap;
	}

	protected class ViewHolder {
		public ProgressImageView thumbnailImg;
		public TextView titleTxt;
		public TextView authorTxt;
		public TextView dateTxt;
	}
}
