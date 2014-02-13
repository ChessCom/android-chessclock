package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.image_load.AvatarView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.db.DbScheme;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.widgets.ProfileImageView;

import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.01.14
 * Time: 12:15
 */
public class FriendsSimpleCursorAdapter extends FriendsCursorAdapter {

	private final int imageSize;
	private final ItemClickListenerFace clickListenerFace;
	private ProfileImageView.ProfileOpenFace profileOpenFace;
	private final HashMap<String, SmartImageFetcher.Data> imageDataMap;

	public FriendsSimpleCursorAdapter(ItemClickListenerFace clickListenerFace, Cursor cursor,
									  SmartImageFetcher imageFetcher, ProfileImageView.ProfileOpenFace profileOpenFace) {
		super(clickListenerFace, cursor, imageFetcher);
		this.clickListenerFace = clickListenerFace;
		this.profileOpenFace = profileOpenFace;
		imageSize = resources.getDimensionPixelSize(R.dimen.friend_list_photo_size);

		imageDataMap = new HashMap<String, SmartImageFetcher.Data>();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.recent_opponent_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.playerImg = (AvatarView) view.findViewById(R.id.playerImg);
		holder.playerNameTxt = (TextView) view.findViewById(R.id.playerNameTxt);

		View friendListItemView = view.findViewById(R.id.friendListItemView);
		friendListItemView.setOnClickListener(clickListenerFace);

		view.setTag(holder);

		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();

		view.setTag(R.id.list_item_id, cursor.getPosition());

		boolean isOnline = getInt(cursor, DbScheme.V_IS_OPPONENT_ONLINE) > 0;
		holder.playerImg.setOnline(isOnline);

		String opponentName = getString(cursor, DbScheme.V_USERNAME);
		holder.playerImg.getImageView().setUsername(opponentName, profileOpenFace);
		holder.playerNameTxt.setText(opponentName);

		boolean isOpponentOnline = getInt(cursor, DbScheme.V_IS_OPPONENT_ONLINE) > 0;
		holder.playerImg.setOnline(isOpponentOnline);

		// load avatar
		String avatarUrl = getString(cursor, DbScheme.V_PHOTO_URL);

		if (!imageDataMap.containsKey(avatarUrl)) {
			imageDataMap.put(avatarUrl, new SmartImageFetcher.Data(avatarUrl, imageSize));
		}

		imageFetcher.loadImage(imageDataMap.get(avatarUrl), holder.playerImg.getImageView());
	}

	private class ViewHolder {
		public AvatarView playerImg;
		public TextView playerNameTxt;
	}
}
