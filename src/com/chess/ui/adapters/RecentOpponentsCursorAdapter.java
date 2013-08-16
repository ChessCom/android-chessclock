package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.image_load.AvatarView;
import com.chess.db.DbScheme;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 16.06.13
 * Time: 17:59
 */
public class RecentOpponentsCursorAdapter extends ItemsCursorAdapter {

	protected static final String CHESS_960 = " (960)";
	private final int imageSize;

	public RecentOpponentsCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		imageSize = (int) (resources.getDimension(R.dimen.list_item_image_size_big) / resources.getDisplayMetrics().density);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_recent_opponent_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.playerImg = (AvatarView) view.findViewById(R.id.playerImg);
		holder.playerTxt = (TextView) view.findViewById(R.id.playerNameTxt);

		view.setTag(holder);
		return view;
	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		// get player side, and choose opponent
		String avatarUrl;
		String opponentName;
		if (getInt(cursor, DbScheme.V_I_PLAY_AS) == RestHelper.P_BLACK) {
			avatarUrl = getString(cursor, DbScheme.V_WHITE_AVATAR);
			opponentName = getString(cursor, DbScheme.V_WHITE_USERNAME);
		} else {
			avatarUrl = getString(cursor, DbScheme.V_BLACK_AVATAR);
			opponentName = getString(cursor, DbScheme.V_BLACK_USERNAME);
		}

		holder.playerTxt.setText(opponentName);
		imageLoader.download(avatarUrl, holder.playerImg, imageSize);

		boolean isOpponentOnline = getInt(cursor, DbScheme.V_IS_OPPONENT_ONLINE) > 0;
		holder.playerImg.setOnline(isOpponentOnline);
	}

	protected class ViewHolder {
		public AvatarView playerImg;
		public TextView playerTxt;
	}
}
