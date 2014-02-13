package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.image_load.AvatarView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.db.DbScheme;
import com.chess.model.BaseGameItem;
import com.chess.statics.Symbol;
import com.chess.utilities.AppUtils;

import java.util.HashMap;

public class DailyFinishedGamesCursorAdapter extends ItemsCursorAdapter {

	protected final int imageSize;
	protected final String drawStr;
	protected final String lostStr;
	protected final String winStr;
	private final int lostColor;
	private final int wonColor;
	private final int drawColor;
	protected final HashMap<String, SmartImageFetcher.Data> imageDataMap;

	public DailyFinishedGamesCursorAdapter(Context context, Cursor cursor, SmartImageFetcher imageFetcher) {
		super(context, cursor, imageFetcher);
		imageSize = resources.getDimensionPixelSize(R.dimen.daily_list_item_image_size);

		lostStr = AppUtils.upCaseFirst(context.getString(R.string.lost));
		winStr = AppUtils.upCaseFirst(context.getString(R.string.won));
		drawStr = context.getString(R.string.draw);

		// Also, won should be green, draw should be grey, loss should say "lost" and be red.
		lostColor = resources.getColor(R.color.red_button);
		wonColor = resources.getColor(R.color.new_dark_green);
		drawColor = resources.getColor(R.color.stats_label_grey);

		imageDataMap = new HashMap<String, SmartImageFetcher.Data>();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.daily_finished_games_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.playerImg = (AvatarView) view.findViewById(R.id.playerImg);
		holder.playerTxt = (TextView) view.findViewById(R.id.playerNameTxt);
		holder.premiumImg = (ImageView) view.findViewById(R.id.premiumImg);
		holder.ratingTxt = (TextView) view.findViewById(R.id.ratingTxt);
		holder.gameResultTxt = (TextView) view.findViewById(R.id.gameResultTxt);
		holder.gameTypeTxt = (TextView) view.findViewById(R.id.gameTypeTxt);

		view.setTag(holder);
		return view;
	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		// get player side, and choose opponent
		String avatarUrl;
		String opponentName;
		String opponentRating;
		int premiumStatus;
		if (getInt(cursor, DbScheme.V_I_PLAY_AS) == RestHelper.P_BLACK) {
			avatarUrl = getString(cursor, DbScheme.V_WHITE_AVATAR);
			opponentName = getString(cursor, DbScheme.V_WHITE_USERNAME);
			opponentRating = getString(cursor, DbScheme.V_WHITE_RATING);
			premiumStatus = getInt(cursor, DbScheme.V_WHITE_PREMIUM_STATUS);
		} else {
			avatarUrl = getString(cursor, DbScheme.V_BLACK_AVATAR);
			opponentName = getString(cursor, DbScheme.V_BLACK_USERNAME);
			opponentRating = getString(cursor, DbScheme.V_BLACK_RATING);
			premiumStatus = getInt(cursor, DbScheme.V_BLACK_PREMIUM_STATUS);
		}

		holder.premiumImg.setImageResource(AppUtils.getPremiumIcon(premiumStatus));
		holder.playerTxt.setText(opponentName);
		holder.ratingTxt.setText(Symbol.wrapInPars(opponentRating));

		if (!imageDataMap.containsKey(avatarUrl)) {
			imageDataMap.put(avatarUrl, new SmartImageFetcher.Data(avatarUrl, imageSize));
		}

		ImageView imageView = holder.playerImg.getImageView();
		imageFetcher.loadImage(imageDataMap.get(avatarUrl), imageView);

		boolean isOpponentOnline = getInt(cursor, DbScheme.V_IS_OPPONENT_ONLINE) > 0;
		holder.playerImg.setOnline(isOpponentOnline);

		if(getInt(cursor, DbScheme.V_GAME_TYPE) == RestHelper.V_GAME_CHESS) {
			holder.gameTypeTxt.setText(R.string.ic_daily_game);
		} else {
			holder.gameTypeTxt.setText(R.string.ic_daily960_game);
		}

		// Lost red
		String result = lostStr;
		int resultColor = lostColor;
		if (getInt(cursor, DbScheme.V_GAME_SCORE) == BaseGameItem.GAME_WON) {
			// Win Green
			result = winStr;
			resultColor = wonColor;
		} else if (getInt(cursor, DbScheme.V_GAME_SCORE) == BaseGameItem.GAME_DRAW) {
			// Draw Grey
			result = drawStr;
			resultColor = drawColor;
		}

		holder.gameResultTxt.setText(result);
		holder.gameResultTxt.setTextColor(resultColor);
	}

	protected class ViewHolder {
		public AvatarView playerImg;
		public TextView playerTxt;
		public ImageView premiumImg;
		public TextView ratingTxt;
		public TextView gameResultTxt;
		public TextView gameTypeTxt;
	}
}
