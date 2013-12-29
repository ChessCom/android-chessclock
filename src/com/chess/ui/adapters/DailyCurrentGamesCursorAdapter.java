package com.chess.ui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.image_load.AvatarView;
import com.chess.backend.image_load.ProgressImageView;
import com.chess.backend.image_load.bitmapfun.SmartImageFetcher;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.statics.AppData;
import com.chess.statics.Symbol;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

import java.util.HashMap;

public class DailyCurrentGamesCursorAdapter extends ItemsCursorAdapter {

	protected static final String CHESS_960 = " (960)";
	private static final int FEN_IMAGE_BIG_SIZE = 2;
	private final int fullPadding;
	private final int halfPadding;
	private final int imageSize;
	private final int redColor;
	private final int greyColor;
	private final int boardPreviewSize;
	private final HashMap<String, SmartImageFetcher.Data> imageDataMap;
	private final boolean sevenInchTablet;
	private final ItemClickListenerFace clickListenerFace;
	private boolean showMiniBoards;
	private boolean showNewGameAtFirst;
	private String timeLabel;

	public DailyCurrentGamesCursorAdapter(ItemClickListenerFace clickListenerFace, Cursor cursor, SmartImageFetcher imageFetcher) {
		super(clickListenerFace.getMeContext(), cursor, imageFetcher);
		this.clickListenerFace = clickListenerFace;
		fullPadding = (int) context.getResources().getDimension(R.dimen.default_scr_side_padding);
		halfPadding = fullPadding / 2;
		imageSize = resources.getDimensionPixelSize(R.dimen.daily_list_item_image_size);
		boardPreviewSize = resources.getDimensionPixelSize(R.dimen.daily_board_preview_size);

		redColor = resources.getColor(R.color.red);
		greyColor = resources.getColor(R.color.grey_button_flat);
		imageDataMap = new HashMap<String, SmartImageFetcher.Data>();

		sevenInchTablet = AppUtils.is7InchTablet(context);

		showMiniBoards = new AppData(context).isMiniBoardsEnabled();
	}

	@Override
	public int getCount() {
		if (showNewGameAtFirst && isTablet) {
			return super.getCount();
		} else {
			return super.getCount();
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view;
		if (showMiniBoards) {
			view = inflater.inflate(R.layout.new_daily_games_home_item, parent, false);
		} else {
			view = inflater.inflate(R.layout.new_daily_games_home_item_no_thumbs, parent, false);
		}

		ViewHolder holder = new ViewHolder();
		holder.playerImg = (AvatarView) view.findViewById(R.id.playerImg);
		holder.playerTxt = (TextView) view.findViewById(R.id.playerNameTxt);
		holder.gameInfoTxt = (TextView) view.findViewById(R.id.timeLeftTxt);
		holder.timeLeftIcon = (TextView) view.findViewById(R.id.timeLeftIcon);
		holder.boardPreviewFrame = (ProgressImageView) view.findViewById(R.id.boardPreviewFrame);

		if (isTablet) {
			holder.timeOptionView = view.findViewById(R.id.timeOptionView);
			holder.timeOptionBtn = (Button) view.findViewById(R.id.timeOptionBtn);
			holder.playNewGameBtn = (Button) view.findViewById(R.id.playNewGameBtn);

			holder.timeOptionBtn.setOnClickListener(clickListenerFace);
			holder.playNewGameBtn.setOnClickListener(clickListenerFace);
		}

		view.setTag(holder);
		return view;
	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) convertView.getTag();

		if (isTablet) {
			if (showNewGameAtFirst) {
				if (DbDataManager.getId(cursor) == -1) {
					holder.timeOptionView.setVisibility(View.VISIBLE);
//					String daysString = getDaysString(newGameButtonsArray[mode]);
					holder.timeOptionBtn.setText(timeLabel);
				} else {
					holder.timeOptionView.setVisibility(View.GONE);
				}
			} else {
				holder.timeOptionView.setVisibility(View.GONE);
			}
		}

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
			opponentName = getString(cursor, DbScheme.V_WHITE_USERNAME) + gameType + draw;
		} else {
			avatarUrl = getString(cursor, DbScheme.V_BLACK_AVATAR);
			opponentName = getString(cursor, DbScheme.V_BLACK_USERNAME) + gameType + draw;
		}

		holder.playerTxt.setText(opponentName);
		if (!imageDataMap.containsKey(avatarUrl)) {
			imageDataMap.put(avatarUrl, new SmartImageFetcher.Data(avatarUrl, imageSize));
		}

		imageFetcher.loadImage(imageDataMap.get(avatarUrl), holder.playerImg.getImageView());

		boolean isOpponentOnline = getInt(cursor, DbScheme.V_IS_OPPONENT_ONLINE) > 0;
		holder.playerImg.setOnline(isOpponentOnline);

		// don't show time if it's not my move
		if (getInt(cursor, DbScheme.V_IS_MY_TURN) > 0) {
			long amount = getLong(cursor, DbScheme.V_TIME_REMAINING);
			if (lessThanDay(amount)) {
				holder.gameInfoTxt.setTextColor(redColor);
				holder.timeLeftIcon.setTextColor(redColor);
			} else {
				holder.gameInfoTxt.setTextColor(greyColor);
				holder.timeLeftIcon.setTextColor(greyColor);
			}

			String infoText;
			if (amount == 0) {
				infoText = context.getString(R.string.few_min);
			} else {
				infoText = AppUtils.getTimeLeftFromSeconds(amount, context);
			}

			holder.timeLeftIcon.setVisibility(View.VISIBLE);
			holder.gameInfoTxt.setVisibility(View.VISIBLE);

			holder.gameInfoTxt.setText(infoText);
		} else {
			holder.gameInfoTxt.setVisibility(View.GONE);
			holder.timeLeftIcon.setVisibility(View.GONE);
		}

		if (!isTablet) {
			if (cursor.getPosition() == 0) {
				convertView.setPadding(fullPadding, fullPadding, fullPadding, halfPadding);
			} else if (cursor.getPosition() == getCount()) {
				convertView.setPadding(fullPadding, halfPadding, fullPadding, fullPadding);
			} else {
				convertView.setPadding(fullPadding, halfPadding, fullPadding, halfPadding);
			}
		}

		String fen = getString(cursor, DbScheme.V_FEN);
		// take only first part
		fen = fen.split(Symbol.SPACE)[0];

		boolean useFlip = false;
		if (getInt(cursor, DbScheme.V_I_PLAY_AS) == RestHelper.P_BLACK) { // if need to flip board
			useFlip = true;
		}
		String imageUrl;
		if (sevenInchTablet) {
			imageUrl = RestHelper.GET_FEN_IMAGE(fen, FEN_IMAGE_BIG_SIZE, useFlip);
		} else {
			imageUrl = RestHelper.GET_FEN_IMAGE(fen, useFlip);
		}

		if (!imageDataMap.containsKey(imageUrl)) {
			imageDataMap.put(imageUrl, new SmartImageFetcher.Data(imageUrl, boardPreviewSize));
		}

		imageFetcher.loadImage(imageDataMap.get(imageUrl), holder.boardPreviewFrame.getImageView());
	}

	private boolean lessThanDay(long amount) {
		return amount / 86400 < 1;
	}

	public void showNewGameAtFirst(boolean showNewGameAtFirst) {
		this.showNewGameAtFirst = showNewGameAtFirst;
	}

	protected String getDaysString(int cnt) {
		if (cnt > 1) {
			return context.getString(R.string.days_arg, cnt);
		} else {
			return context.getString(R.string.day_arg, cnt);
		}
	}

	public void setTimeLabel(String timeLabel) {
		this.timeLabel = timeLabel;
		notifyDataSetChanged();
	}

	public void setShowMiniBoards(boolean showMiniBoards) {
		this.showMiniBoards = showMiniBoards;
	}

	protected class ViewHolder {
		public AvatarView playerImg;
		public TextView playerTxt;
		public TextView gameInfoTxt;
		public TextView timeLeftIcon;
		public ProgressImageView boardPreviewFrame;
		public View timeOptionView;
		public Button timeOptionBtn;
		public Button playNewGameBtn;
	}
}
