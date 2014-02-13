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

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 07.11.13
 * Time: 15:22
 */
public class LiveArchiveGamesAdapterTablet extends LiveArchiveGamesAdapter {

	public LiveArchiveGamesAdapterTablet(Context context, Cursor cursor, SmartImageFetcher imageFetcher) {
		super(context, cursor, imageFetcher);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_archive_tablet_game_item, parent, false);
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

//		holder.premiumImg.setImageResource(AppUtils.getPremiumIcon(premiumStatus));
		holder.playerTxt.setText(opponentName);
		holder.ratingTxt.setText(Symbol.wrapInPars(opponentRating));

		if (!imageDataMap.containsKey(avatarUrl)) {
			imageDataMap.put(avatarUrl, new SmartImageFetcher.Data(avatarUrl, imageSize));
		}

		imageFetcher.loadImage(imageDataMap.get(avatarUrl), holder.playerImg.getImageView());

		boolean isOpponentOnline = getInt(cursor, DbScheme.V_IS_OPPONENT_ONLINE) > 0;
		holder.playerImg.setOnline(isOpponentOnline);

		String gameType = getString(cursor, DbScheme.V_GAME_TIME_CLASS);
		if (gameType.equals(STANDARD)) {
			holder.gameTypeTxt.setText(R.string.ic_live_standard);
		} else if (gameType.equals(LIGHTNING)) {
			holder.gameTypeTxt.setText(R.string.ic_live_bullet);
		} else {
			holder.gameTypeTxt.setText(R.string.ic_live_blitz);
		}

		// Loss orange
		String result = lossStr;
		if (getInt(cursor, DbScheme.V_GAME_SCORE) == BaseGameItem.GAME_WON) {
			// Win Green
			result = winStr;
		} else if (getInt(cursor, DbScheme.V_GAME_SCORE) == BaseGameItem.GAME_DRAW) {
			// Draw Grey
			result = drawStr;
		}
		holder.gameResultTxt.setText(result);
	}
}
