package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.image_load.AvatarView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.db.DbScheme;
import com.chess.statics.Symbol;
import com.chess.utilities.AppUtils;

import java.util.HashMap;

public class DailyCurrentGamesTheirCursorRightAdapter extends ItemsCursorAdapter {

	protected static final String CHESS_960 = " (960)";
	private final int imageSize;
	private final HashMap<String, SmartImageFetcher.Data> imageDataMap;

	public DailyCurrentGamesTheirCursorRightAdapter(Context context, Cursor cursor, SmartImageFetcher imageFetcher) {
		super(context, cursor, imageFetcher);// TODO change later with CursorLoader
		imageSize = resources.getDimensionPixelSize(R.dimen.daily_list_item_image_size);
		imageDataMap = new HashMap<String, SmartImageFetcher.Data>();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.daily_games_item, parent, false);
		ViewHolder holder = new ViewHolder();
		holder.playerImg = (AvatarView) view.findViewById(R.id.playerImg);
		holder.playerTxt = (TextView) view.findViewById(R.id.playerNameTxt);
		holder.gameInfoTxt = (TextView) view.findViewById(R.id.timeLeftTxt);

		view.setTag(holder);
		return view;
	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		String gameType = Symbol.EMPTY;
		if (getInt(cursor, DbScheme.V_GAME_TYPE) == RestHelper.V_GAME_CHESS_960) {
			gameType = CHESS_960;
		}

		String draw = Symbol.EMPTY;
		if (getInt(cursor, DbScheme.V_OPPONENT_OFFERED_DRAW) > 0) {
			draw = "\n" + context.getString(R.string.draw_offered);
		}

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

		holder.playerTxt.setText(opponentName + gameType + draw);

		if (!imageDataMap.containsKey(avatarUrl)) {
			imageDataMap.put(avatarUrl, new SmartImageFetcher.Data(avatarUrl, imageSize));
		}

		imageFetcher.loadImage(imageDataMap.get(avatarUrl), holder.playerImg.getImageView());

		boolean isOpponentOnline = getInt(cursor, DbScheme.V_IS_OPPONENT_ONLINE) > 0;
		holder.playerImg.setOnline(isOpponentOnline);

		long amount = getLong(cursor, DbScheme.V_TIME_REMAINING);
		String infoText;
		if (amount == 0) {
			infoText = context.getString(R.string.few_min);
		} else {
			infoText = AppUtils.getTimeLeftFromSeconds(amount, context);
		}
		holder.gameInfoTxt.setText(infoText);
	}

	protected class ViewHolder {
		public AvatarView playerImg;
		public TextView playerTxt;
		public TextView gameInfoTxt;
	}
}
