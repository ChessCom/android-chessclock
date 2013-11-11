package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.statics.Symbol;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 10.11.13
 * Time: 17:54
 */
public class ArticlesCursorAdapterTablet extends ArticlesCursorAdapter {

	public ArticlesCursorAdapterTablet(Context context, Cursor cursor, SmartImageFetcher imageFetcher) {
		super(context, cursor, imageFetcher);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_article_thumb_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.thumbnailImg = (ProgressImageView) view.findViewById(R.id.thumbnailImg);
		holder.titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		holder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);
		holder.dateTxt = (TextView) view.findViewById(R.id.dateTxt);
		holder.contentTxt = (TextView) view.findViewById(R.id.contentTxt);

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

		String contentPreviewStr = getString(cursor, DbScheme.V_PREVIEW_BODY);
		holder.contentTxt.setText(Html.fromHtml(contentPreviewStr).toString().trim());

		holder.titleTxt.setText(Html.fromHtml(DbDataManager.getString(cursor, DbScheme.V_TITLE)));
		date.setTime(DbDataManager.getLong(cursor, DbScheme.V_CREATE_DATE) * 1000L);
		holder.dateTxt.setText(dateFormatter.format(date));

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
}
