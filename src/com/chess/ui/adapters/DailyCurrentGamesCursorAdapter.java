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

public class DailyCurrentGamesCursorAdapter extends ItemsCursorAdapter {

	protected static final String CHESS_960 = " (960)";
	private final int fullPadding;
	private final int halfPadding;
	private final int imageSize;
	private final int redColor;
	private final int greyColor;

	public DailyCurrentGamesCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);// TODO change later with CursorLoader

		fullPadding = (int) context.getResources().getDimension(R.dimen.default_scr_side_padding);
		halfPadding = fullPadding / 2;
		imageSize = (int) (resources.getDimension(R.dimen.list_item_image_size_big) / resources.getDisplayMetrics().density);

		redColor = resources.getColor(R.color.red);
		greyColor = resources.getColor(R.color.grey_button_flat);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_daily_games_home_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.playerImg = (ProgressImageView) view.findViewById(R.id.playerImg);
		holder.playerTxt = (TextView) view.findViewById(R.id.playerNameTxt);
		holder.gameInfoTxt = (TextView) view.findViewById(R.id.timeLeftTxt);
		holder.timeLeftIcon = (TextView) view.findViewById(R.id.timeLeftIcon);

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

		// don't show time if it's not my move
		if (getInt(cursor, DBConstants.V_IS_MY_TURN) > 0) {
			long amount = getLong(cursor, DBConstants.V_TIME_REMAINING);
			if (lessThanDay(amount)) {
				holder.gameInfoTxt.setTextColor(redColor);
				holder.timeLeftIcon.setTextColor(redColor);
			} else {
				holder.gameInfoTxt.setTextColor(greyColor);
				holder.timeLeftIcon.setTextColor(greyColor);
			}

			String infoText;
			if (amount == 0) {
				infoText = context.getString(R.string.few_minutes);
			} else {
				infoText = AppUtils.getTimeLeftFromSeconds(amount, context);
			}

			holder.timeLeftIcon.setVisibility(View.VISIBLE);
			holder.gameInfoTxt.setVisibility(View.VISIBLE);

			holder.gameInfoTxt.setText(infoText/*  + " game id = " + getString(cursor, DBConstants.V_ID)*/);
		} else {
			holder.gameInfoTxt.setVisibility(View.GONE);
			holder.timeLeftIcon.setVisibility(View.GONE);
		}
//		holder.gameInfoTxt.setText(" game id = " + getString(cursor, DBConstants.V_ID));

		if (cursor.getPosition() == 0) {
			convertView.setPadding(fullPadding, fullPadding, fullPadding, halfPadding);
		} else if (cursor.getPosition() == getCount()) {
			convertView.setPadding(fullPadding, halfPadding, fullPadding, fullPadding);
		} else {
			convertView.setPadding(fullPadding, halfPadding, fullPadding, halfPadding);
		}

//		String avatarUrl = getString(cursor, DBConstants.OP)
		String avatarUrl = "https://s3.amazonaws.com/chess-7/images_users/avatars/erik_small.1.png";
		imageLoader.download(avatarUrl, holder.playerImg, imageSize);
	}

	private boolean lessThanDay(long amount) {
		return amount /86400 < 1;
	}

	protected class ViewHolder {
		public ProgressImageView playerImg;
		public TextView playerTxt;
		public TextView gameInfoTxt;
		public TextView timeLeftIcon;
	}
}
