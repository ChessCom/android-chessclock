package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.statics.Symbol;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.utilities.AppUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 29.01.13
 * Time: 17:28
 */
public class ArticlesCursorAdapter extends ItemsCursorAdapter {

	public static final String GREY_COLOR_DIVIDER = "##";
	public static final String NO_ITEM_IMAGE = "no_item_image";

	protected static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");
	protected final int completedTextColor;
	protected final int incompleteTextColor;
	protected final HashMap<String, SmartImageFetcher.Data> imageDataMap;
	protected int PHOTO_SIZE;
	protected CharacterStyle foregroundSpan;
	protected Date date;
	protected SparseBooleanArray viewedMap;

	public ArticlesCursorAdapter(Context context, Cursor cursor, SmartImageFetcher imageFetcher) {
		super(context, cursor, imageFetcher);

		int lightGrey = resources.getColor(R.color.new_subtitle_light_grey);
		foregroundSpan = new ForegroundColorSpan(lightGrey);

		completedTextColor = resources.getColor(R.color.new_light_grey_3);
		incompleteTextColor = resources.getColor(R.color.new_text_blue);

		date = new Date();

		PHOTO_SIZE = resources.getDimensionPixelSize(R.dimen.article_thumb_width);
		imageDataMap = new HashMap<String, SmartImageFetcher.Data>();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.article_thumb_list_item, parent, false);
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

		String firstName = DbDataManager.getString(cursor, DbScheme.V_FIRST_NAME);
		String chessTitle = DbDataManager.getString(cursor, DbScheme.V_CHESS_TITLE);
		String lastName = DbDataManager.getString(cursor, DbScheme.V_LAST_NAME);
		CharSequence authorStr;
		if (TextUtils.isEmpty(chessTitle)) {
			authorStr = firstName + Symbol.SPACE + lastName;
		} else {
			authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER
					+ Symbol.SPACE + firstName + Symbol.SPACE + lastName;
			authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, foregroundSpan);
		}
		holder.authorTxt.setText(authorStr);

		holder.titleTxt.setText(Html.fromHtml(DbDataManager.getString(cursor, DbScheme.V_TITLE)));
		date.setTime(DbDataManager.getLong(cursor, DbScheme.V_CREATE_DATE) * 1000L);
		holder.dateTxt.setText(Symbol.DIVIDER + dateFormatter.format(date));

		String articleImageUrl = DbDataManager.getString(cursor, DbScheme.V_PHOTO_URL);
		if (articleImageUrl.contains(NO_ITEM_IMAGE)) {
			articleImageUrl = DbDataManager.getString(cursor, DbScheme.V_USER_AVATAR);
		}
		if (!imageDataMap.containsKey(articleImageUrl)) {
			imageDataMap.put(articleImageUrl, new SmartImageFetcher.Data(articleImageUrl, PHOTO_SIZE));
		}

		imageFetcher.loadImage(imageDataMap.get(articleImageUrl), holder.thumbnailImg.getImageView());

		if (viewedMap.get(getInt(cursor, DbScheme.V_ID), false)) {
			holder.titleTxt.setTextColor(completedTextColor);
		} else {
			holder.titleTxt.setTextColor(incompleteTextColor);
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
		public TextView contentTxt;
	}
}
