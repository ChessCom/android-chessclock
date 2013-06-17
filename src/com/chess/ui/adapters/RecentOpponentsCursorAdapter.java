package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.db.DBConstants;

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
		holder.playerImg = (ProgressImageView) view.findViewById(R.id.playerImg);
		holder.playerTxt = (TextView) view.findViewById(R.id.playerNameTxt);

		view.setTag(holder);
		return view;
	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		holder.playerTxt.setText(getString(cursor, DBConstants.V_OPPONENT_NAME));

		//		String avatarUrl = getString(cursor, DBConstants.OP)
		String avatarUrl = "https://s3.amazonaws.com/chess-7/images_users/avatars/erik_small.1.png";
		imageLoader.download(avatarUrl, holder.playerImg, imageSize);
	}

	protected class ViewHolder {
		public ProgressImageView playerImg;
		public TextView playerTxt;
	}
}
