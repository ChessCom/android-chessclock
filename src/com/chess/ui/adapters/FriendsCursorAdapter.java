package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.db.DBConstants;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

import java.util.Calendar;

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

	public FriendsCursorAdapter(ItemClickListenerFace clickListenerFace, Cursor cursor) {
		super(clickListenerFace.getMeContext(), cursor);
		this.clickListenerFace = clickListenerFace;
		imageSize = (int) (resources.getDimension(R.dimen.friend_list_photo_size) / resources.getDisplayMetrics().density);

		String[] countryNames = resources.getStringArray(R.array.new_countries);
		int[] countryCodes = resources.getIntArray(R.array.new_country_ids);
		countryMap = new SparseArray<String>();
		for (int i = 0; i < countryNames.length; i++) {
			countryMap.put(countryCodes[i], countryNames[i]);
		}

		today = Calendar.getInstance();
		lastLoginDate = Calendar.getInstance();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_friends_list_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.photoImg = (ProgressImageView) view.findViewById(R.id.photoImg);
		holder.usernameTxt = (TextView) view.findViewById(R.id.usernameTxt);
		holder.countryImg = (ImageView) view.findViewById(R.id.countryImg);
		holder.premiumImg = (ImageView) view.findViewById(R.id.premiumImg);
		holder.locationTxt = (TextView) view.findViewById(R.id.locationTxt);
		holder.onlineTxt = (TextView) view.findViewById(R.id.onlineTxt);
		holder.challengeImgBtn = (Button) view.findViewById(R.id.challengeImgBtn);
		holder.challengeImgBtn.setOnClickListener(clickListenerFace);
		view.setTag(holder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		holder.challengeImgBtn.setTag(R.id.list_item_id, cursor);

		// set premium icon
		int status = getInt(cursor, DBConstants.V_PREMIUM_STATUS);
		holder.premiumImg.setImageResource(AppUtils.getPremiumIcon(status));

		boolean isOnline = getInt(cursor, DBConstants.V_IS_OPPONENT_ONLINE) > 0 ; // TODO adjust logic for offline mode
		if (isOnline) {
			holder.onlineTxt.setText(R.string.online_now);
		} else {
			holder.onlineTxt.setText(context.getString(R.string.last_played, getLastLoginLabel(cursor)));
		}
		holder.usernameTxt.setText(getString(cursor, DBConstants.V_USERNAME));
		holder.locationTxt.setText(getString(cursor, DBConstants.V_LOCATION));

		// set country flag
		Drawable drawable = AppUtils.getCountryFlagScaled(context, countryMap.get(getInt(cursor, DBConstants.V_COUNTRY_ID)));
		holder.countryImg.setImageDrawable(drawable);

		// load avatar
		imageLoader.download(getString(cursor, DBConstants.V_PHOTO_URL), holder.photoImg, imageSize);
	}

	private String getLastLoginLabel(Cursor cursor) {
		long loginTime = getLong(cursor, DBConstants.V_LAST_LOGIN_DATE) * 1000;
		lastLoginDate.setTimeInMillis(loginTime);

		int cnt = 0;
		while (today.compareTo(lastLoginDate) > 0) {
			lastLoginDate.add(Calendar.DAY_OF_MONTH, 1);
			cnt++;
		}
		if (cnt > 1) {
			return context.getString(R.string.x_days_ago, cnt);
			// last played X days ago
		} else {
			// last played today
			return context.getString(R.string.today);
		}
	}

	private class ViewHolder {
		public ProgressImageView photoImg;
		public TextView usernameTxt;
		public ImageView countryImg;
		public ImageView premiumImg;
		public TextView locationTxt;
		public TextView onlineTxt;
		public Button challengeImgBtn;
	}
}
