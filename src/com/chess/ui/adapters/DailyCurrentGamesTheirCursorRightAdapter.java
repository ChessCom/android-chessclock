package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.statics.StaticData;
import com.chess.db.DBConstants;
import com.chess.model.BaseGameItem;
import com.chess.utilities.AppUtils;

public class DailyCurrentGamesTheirCursorRightAdapter extends ItemsCursorAdapter {

	protected static final String CHESS_960 = " (960)";
	private final int imageSize;

	public DailyCurrentGamesTheirCursorRightAdapter(Context context, Cursor cursor) {
		super(context, cursor);// TODO change later with CursorLoader
		imageSize = (int) (resources.getDimension(R.dimen.list_item_image_size_big) / resources.getDisplayMetrics().density);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_daily_games_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.playerImg = (ProgressImageView) view.findViewById(R.id.playerImg);
		holder.playerTxt = (TextView) view.findViewById(R.id.playerNameTxt);
		holder.gameInfoTxt = (TextView) view.findViewById(R.id.timeLeftTxt);

		view.setTag(holder);
		return view;
	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		String gameType = StaticData.SYMBOL_EMPTY;
		if (getInt(cursor, DBConstants.V_GAME_TYPE) == BaseGameItem.CHESS_960) {
			gameType = CHESS_960;
		}

		String draw = StaticData.SYMBOL_EMPTY;
		if (getInt(cursor, DBConstants.V_OPPONENT_OFFERED_DRAW) > 0) {
			draw = "\n" + context.getString(R.string.draw_offered);
		}

		holder.playerTxt.setText(getString(cursor, DBConstants.V_OPPONENT_NAME) + gameType + draw);

		long amount = getLong(cursor, DBConstants.V_TIME_REMAINING);
		String infoText;
		if (amount == 0) {
			infoText = context.getString(R.string.few_minutes);
		} else {
			infoText = AppUtils.getTimeLeftFromSeconds(amount, context);
		}
		holder.gameInfoTxt.setText(infoText);

		//		String avatarUrl = getString(cursor, DBConstants.OP)
		String avatarUrl = "https://s3.amazonaws.com/chess-7/images_users/avatars/erik_small.1.png";
		imageLoader.download(avatarUrl, holder.playerImg, imageSize);
	}

	protected class ViewHolder {
		public ProgressImageView playerImg;
		public TextView playerTxt;
		public TextView gameInfoTxt;
	}
}
