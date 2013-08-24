package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.image_load.AvatarView;
import com.chess.db.DbScheme;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 23.08.13
 * Time: 18:23
 */
public class CommentsCursorAdapter extends ItemsCursorAdapter {

	private final int imgSize;
	private final SparseArray<String> countryMap;

	public CommentsCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		imgSize = (int) (40 * resources.getDisplayMetrics().density);

		String[] countryNames = resources.getStringArray(R.array.new_countries);
		int[] countryCodes = resources.getIntArray(R.array.new_country_ids);
		countryMap = new SparseArray<String>();
		for (int i = 0; i < countryNames.length; i++) {
			countryMap.put(countryCodes[i], countryNames[i]);
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_comment_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.photoImg = (AvatarView) view.findViewById(R.id.photoImg);
		holder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);
		holder.countryImg = (ImageView) view.findViewById(R.id.countryImg);
		holder.premiumImg = (ImageView) view.findViewById(R.id.premiumImg);
		holder.dateTxt = (TextView) view.findViewById(R.id.dateTxt);
		holder.bodyTxt = (TextView) view.findViewById(R.id.bodyTxt);

		view.setTag(holder);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		holder.photoImg.setOnline(false);

		String userAvatarUrl = getString(cursor, DbScheme.V_USER_AVATAR);
		imageLoader.download(userAvatarUrl, holder.photoImg, imgSize);

		holder.authorTxt.setText(getString(cursor, DbScheme.V_USERNAME));
//		// set premium icon
//		int status = getInt(cursor, DbScheme.V_PREMIUM_STATUS);
//		holder.premiumImg.setImageResource(AppUtils.getPremiumIcon(status));

		// set country flag
		Drawable drawable = AppUtils.getCountryFlagScaled(context, countryMap.get(getInt(cursor, DbScheme.V_COUNTRY_ID)));
		holder.countryImg.setImageDrawable(drawable);

		// Last time ago
		long timeAgo = getLong(cursor, DbScheme.V_CREATE_DATE);
		String lastDate = AppUtils.getMomentsAgoFromSeconds(timeAgo, context);
		holder.dateTxt.setText(lastDate);

		holder.bodyTxt.setText(Html.fromHtml(getString(cursor, DbScheme.V_BODY)));
	}

	private static class ViewHolder {
		AvatarView photoImg;
		TextView authorTxt;
		ImageView countryImg;
		ImageView premiumImg;
		TextView dateTxt;
		TextView bodyTxt;
	}

}
