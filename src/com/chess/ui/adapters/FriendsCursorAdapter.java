package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.image_load.AvatarView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.db.DbScheme;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

import java.util.Calendar;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.01.13
 * Time: 17:28
 */
public class FriendsCursorAdapter extends ItemsCursorAdapter {

	private final int imageSize;
	private final SparseArray<String> countryMap;
	private final Calendar today;
	private final Calendar lastLoginDate;
	private final ItemClickListenerFace clickListenerFace;
	private final HashMap<String, SmartImageFetcher.Data> imageDataMap;

	public FriendsCursorAdapter(ItemClickListenerFace clickListenerFace, Cursor cursor, SmartImageFetcher imageFetcher) {
		super(clickListenerFace.getMeContext(), cursor, imageFetcher);
		this.clickListenerFace = clickListenerFace;
		imageSize = resources.getDimensionPixelSize(R.dimen.friend_list_photo_size);

		String[] countryNames = resources.getStringArray(R.array.new_countries);
		int[] countryCodes = resources.getIntArray(R.array.new_country_ids);
		countryMap = new SparseArray<String>();
		for (int i = 0; i < countryNames.length; i++) {
			countryMap.put(countryCodes[i], countryNames[i]);
		}

		today = Calendar.getInstance();
		lastLoginDate = Calendar.getInstance();

		imageDataMap = new HashMap<String, SmartImageFetcher.Data>();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.friends_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.photoImg = (AvatarView) view.findViewById(R.id.photoImg);
		holder.usernameTxt = (TextView) view.findViewById(R.id.usernameTxt);
		holder.countryImg = (ImageView) view.findViewById(R.id.countryImg);
		holder.premiumImg = (ImageView) view.findViewById(R.id.premiumImg);
		holder.locationTxt = (TextView) view.findViewById(R.id.locationTxt);
		holder.onlineTxt = (TextView) view.findViewById(R.id.onlineTxt);

		View friendListItemView = view.findViewById(R.id.friendListItemView);
		friendListItemView.setOnClickListener(clickListenerFace);

		holder.challengeImgBtn = (Button) view.findViewById(R.id.challengeImgBtn);
		holder.challengeImgBtn.setOnClickListener(clickListenerFace);

		holder.messageImgBtn = (Button) view.findViewById(R.id.messageImgBtn);
		holder.messageImgBtn.setOnClickListener(clickListenerFace);

		view.setTag(holder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		holder.challengeImgBtn.setTag(R.id.list_item_id, cursor.getPosition());
		holder.messageImgBtn.setTag(R.id.list_item_id, cursor.getPosition());
		view.setTag(R.id.list_item_id, cursor.getPosition());

		// set premium icon
		int status = getInt(cursor, DbScheme.V_PREMIUM_STATUS);
		holder.premiumImg.setImageResource(AppUtils.getPremiumIcon(status));

		boolean isOnline = getInt(cursor, DbScheme.V_IS_OPPONENT_ONLINE) > 0;
		holder.photoImg.setOnline(isOnline);

		String locationStr = getString(cursor, DbScheme.V_LOCATION);
		if (!TextUtils.isEmpty(locationStr)) {
			holder.locationTxt.setText(locationStr);
			holder.locationTxt.setVisibility(View.VISIBLE);
		} else {
			holder.locationTxt.setVisibility(View.GONE);
		}

		if (isOnline) {
			holder.onlineTxt.setText(R.string.online_today);
		} else {
			holder.onlineTxt.setText(getLastLoginLabel(cursor));
		}
		holder.usernameTxt.setText(getString(cursor, DbScheme.V_USERNAME));

		// set country flag
		Drawable drawable = AppUtils.getCountryFlagScaled(context, countryMap.get(getInt(cursor, DbScheme.V_COUNTRY_ID)));
		holder.countryImg.setImageDrawable(drawable);

		// load avatar
		String avatarUrl = getString(cursor, DbScheme.V_PHOTO_URL);

		if (!imageDataMap.containsKey(avatarUrl)) {
			imageDataMap.put(avatarUrl, new SmartImageFetcher.Data(avatarUrl, imageSize));
		}

		imageFetcher.loadImage(imageDataMap.get(avatarUrl), holder.photoImg.getImageView());
	}

	private String getLastLoginLabel(Cursor cursor) {
		long loginTime = getLong(cursor, DbScheme.V_LAST_LOGIN_DATE) * 1000;
		lastLoginDate.setTimeInMillis(loginTime);

		int cnt = 0;
		while (today.compareTo(lastLoginDate) > 0) {
			lastLoginDate.add(Calendar.DAY_OF_MONTH, 1);
			cnt++;
		}
		if (cnt > 1) {
			return context.getString(R.string.last_was_online, context.getString(R.string.arg_days_ago, cnt));
			// last played X days ago
		} else {
			// last played today
			return context.getString(R.string.online_today);
		}
	}

	private class ViewHolder {
		public AvatarView photoImg;
		public TextView usernameTxt;
		public ImageView countryImg;
		public ImageView premiumImg;
		public TextView locationTxt;
		public TextView onlineTxt;
		public Button challengeImgBtn;
		public Button messageImgBtn;
	}
}
