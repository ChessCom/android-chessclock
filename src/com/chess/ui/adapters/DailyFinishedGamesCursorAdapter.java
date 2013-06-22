package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.statics.StaticData;
import com.chess.db.DBConstants;
import com.chess.model.BaseGameItem;

public class DailyFinishedGamesCursorAdapter extends ItemsCursorAdapter {

	protected static final String CHESS_960 = " (960)";
	private final int imageSize;
	private final String drawStr;
	private final String lossStr;
	private final String winStr;
	private final int colorOrange;
	private final int colorGreen;
	private final int colorGrey;

	public DailyFinishedGamesCursorAdapter(Context context, Cursor cursor) {
		super(context, cursor);
		imageSize = (int) (resources.getDimension(R.dimen.list_item_image_size_big) / resources.getDisplayMetrics().density);

		lossStr = context.getString(R.string.loss);
		winStr = context.getString(R.string.won);
		drawStr = context.getString(R.string.draw);

		colorOrange = resources.getColor(R.color.orange_button_flat);
		colorGreen = resources.getColor(R.color.new_dark_green);
		colorGrey = resources.getColor(R.color.stats_label_grey);

		}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_daily_finished_games_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.playerImg = (ProgressImageView) view.findViewById(R.id.playerImg);
		holder.playerTxt = (TextView) view.findViewById(R.id.playerNameTxt);
		holder.premiumImg = (ImageView) view.findViewById(R.id.premiumImg);
		holder.ratingTxt = (TextView) view.findViewById(R.id.ratingTxt);
		holder.gameResultTxt = (TextView) view.findViewById(R.id.gameResultTxt);

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

		holder.playerTxt.setText(getString(cursor, DBConstants.V_WHITE_USERNAME) + gameType);
		String opponentRating = getString(cursor, DBConstants.V_WHITE_RATING);

		holder.ratingTxt.setText(StaticData.SYMBOL_LEFT_PAR + opponentRating + StaticData.SYMBOL_RIGHT_PAR);

		// Loss orange
		String result = lossStr;
		holder.gameResultTxt.setTextColor(colorOrange);
		if (getInt(cursor, DBConstants.V_GAME_SCORE) == BaseGameItem.GAME_WON) {
			// Win Green
			result = winStr;
			holder.gameResultTxt.setTextColor(colorGreen);
		} else if (getInt(cursor, DBConstants.V_GAME_SCORE) == BaseGameItem.GAME_DRAW) {
			// Draw Grey
			result = drawStr;
			holder.gameResultTxt.setTextColor(colorGrey);
		}
		holder.gameResultTxt.setText(result);

		// get player side
		String avatarUrl;
		if (getInt(cursor, DBConstants.V_I_PLAY_AS) == RestHelper.P_BLACK) {
			avatarUrl = getString(cursor, DBConstants.V_WHITE_AVATAR);
		} else {
			avatarUrl = getString(cursor, DBConstants.V_BLACK_AVATAR);
		}

		imageLoader.download(avatarUrl, holder.playerImg, imageSize);
	}

	protected class ViewHolder {
		public ProgressImageView playerImg;
		public TextView playerTxt;
		public ImageView premiumImg;
		public TextView ratingTxt;
		public TextView gameResultTxt;
	}
}
