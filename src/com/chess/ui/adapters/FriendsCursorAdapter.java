package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.01.13
 * Time: 17:28
 */
public class FriendsCursorAdapter extends ItemsCursorAdapter {

	public FriendsCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_friends_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.photoImg = (ImageView) view.findViewById(R.id.photoImg);
		holder.usernameTxt = (TextView) view.findViewById(R.id.usernameTxt);
		holder.countryImg = (ImageView) view.findViewById(R.id.countryImg);
		holder.locationTxt = (TextView) view.findViewById(R.id.locationTxt);
		holder.onlineTxt = (TextView) view.findViewById(R.id.onlineTxt);

		view.setTag(holder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
/*
		values.put(DBConstants.V_USER, userName);
		values.put(DBConstants.V_USERNAME, dataObj.getUsername());
		values.put(DBConstants.V_USER_ID, dataObj.getUserId());
		values.put(DBConstants.V_LOCATION, "none");
		values.put(DBConstants.V_COUNTRY_ID, 1);
		values.put(DBConstants.V_PHOTO_URL, "");
		 */

		holder.usernameTxt.setText(DBDataManager.getString(cursor, DBConstants.V_USERNAME));
		holder.locationTxt.setText(DBDataManager.getString(cursor, DBConstants.V_LOCATION));


	}

	private class ViewHolder {
		public ImageView photoImg;
		public TextView usernameTxt;
		public ImageView countryImg;
		public TextView locationTxt;
		public TextView onlineTxt;
	}
}
