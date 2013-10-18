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
import com.chess.statics.Symbol;
import com.chess.db.DbScheme;
import com.chess.model.BaseGameItem;

import java.util.HashMap;

public class DailyFinishedGamesCursorRightAdapter extends ItemsCursorAdapter {

	protected static final String CHESS_960 = " (960)";
	private final int imageSize;
	private final HashMap<String, SmartImageFetcher.Data> imageDataMap;


	public DailyFinishedGamesCursorRightAdapter(Context context, Cursor cursor, SmartImageFetcher imageFetcher) {
		super(context, cursor, imageFetcher);
		imageSize = resources.getDimensionPixelSize(R.dimen.daily_list_item_image_size);

		imageDataMap = new HashMap<String, SmartImageFetcher.Data>();
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = inflater.inflate(R.layout.new_daily_games_item, parent, false);
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
		if (getInt(cursor, DbScheme.V_GAME_TYPE) == BaseGameItem.CHESS_960) {
			gameType = CHESS_960;
		}

		// get player side, and choose opponent
		String avatarUrl;
		String opponentName;
		if (getInt(cursor, DbScheme.V_I_PLAY_AS) == RestHelper.P_BLACK) {
			avatarUrl = getString(cursor, DbScheme.V_WHITE_AVATAR);
			opponentName = getString(cursor, DbScheme.V_WHITE_USERNAME) + gameType;
		} else {
			avatarUrl = getString(cursor, DbScheme.V_BLACK_AVATAR);
			opponentName = getString(cursor, DbScheme.V_BLACK_USERNAME) + gameType;
		}

		holder.playerTxt.setText(opponentName + gameType);
		if (!imageDataMap.containsKey(avatarUrl)) {
			imageDataMap.put(avatarUrl, new SmartImageFetcher.Data(avatarUrl, imageSize));
		}

		imageFetcher.loadImage(imageDataMap.get(avatarUrl), holder.playerImg.getImageView());

		boolean isOpponentOnline = getInt(cursor, DbScheme.V_IS_OPPONENT_ONLINE) > 0;
		holder.playerImg.setOnline(isOpponentOnline);

		String result = context.getString(R.string.loss);
		if (getInt(cursor, DbScheme.V_GAME_SCORE) == BaseGameItem.GAME_WON) {
			result = context.getString(R.string.won);
		} else if (getInt(cursor, DbScheme.V_GAME_SCORE) == BaseGameItem.GAME_DRAW) {
			result = context.getString(R.string.draw);
		}
		holder.gameInfoTxt.setText(result);

	}

	protected class ViewHolder {
		public AvatarView playerImg;
		public TextView playerTxt;
		public TextView gameInfoTxt;
	}
}
