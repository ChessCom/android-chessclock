package com.chess.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import com.chess.R;
import com.chess.RoboToggleButton;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 14.01.13
 * Time: 12:23
 */
public class NewGameLiveView extends NewGameDefaultView implements ItemClickListenerFace {

	private NewLiveGameConfig.Builder gameConfigBuilder;
	private NewLiveGamesButtonsAdapter newLiveGamesButtonsAdapter;

	public NewGameLiveView(Context context) {
		super(context);
	}

	public NewGameLiveView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NewGameLiveView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		gameConfigBuilder = new NewLiveGameConfig.Builder();

	}

	public void toggleOptions() {
		super.toggleOptions();

		int expandVisibility = optionsVisible ? VISIBLE : GONE;

		optionsView.setVisibility(expandVisibility);

		if (optionsVisible) {
			compactRelLay.setBackgroundResource(R.drawable.game_option_back_1);
		} else {
			compactRelLay.setBackgroundResource(R.drawable.nav_menu_item_selected);
		}
		compactRelLay.setPadding(COMPACT_PADDING, 0, COMPACT_PADDING, COMPACT_PADDING);
	}

	@Override
	public void addOptionsView() {
		optionsView = LayoutInflater.from(getContext()).inflate(R.layout.new_game_option_live_view, null, false);
		optionsView.setVisibility(GONE);
		addView(optionsView);

		{// Mode adapter init
			String[] newGameButtonsArray = getResources().getStringArray(R.array.new_live_game_button_values);
			List<NewLiveGameButtonItem> newGameButtonItems = new ArrayList<NewLiveGameButtonItem>();
			for (String label : newGameButtonsArray) {
				newGameButtonItems.add(NewLiveGameButtonItem.createNewButtonFromLabel(label, getContext()));
			}

			GridView gridView = (GridView) optionsView.findViewById(R.id.liveModeGrid);
			newLiveGamesButtonsAdapter = new NewLiveGamesButtonsAdapter(this, newGameButtonItems);
			gridView.setAdapter(newLiveGamesButtonsAdapter);
			newLiveGamesButtonsAdapter.checkButton(0);
			// set value to builder
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == NewLiveGamesButtonsAdapter.BUTTON_ID) {
			Integer position = (Integer) view.getTag(R.id.list_item_id);

			newLiveGamesButtonsAdapter.checkButton(position);
		}
	}

	@Override
	public Context getMeContext() {
		return getContext();
	}

	public static class NewLiveGameButtonItem {
		public boolean checked;
		public int min;
		public int sec;
		public String label;
		private static final String PAIR_DIVIDER = " | ";

		public static NewLiveGameButtonItem createNewButtonFromLabel(String label, Context context) {
			NewLiveGameButtonItem buttonItem = new NewLiveGameButtonItem();
			if (label.contains(PAIR_DIVIDER)) {
				// "5 | 2"),
				String[] params = label.split(PAIR_DIVIDER);
				buttonItem.min = Integer.valueOf(params[0]);
				buttonItem.sec = Integer.valueOf(params[2]);
				buttonItem.label = label;

			} else {
				// "10 min"),
				buttonItem.min = Integer.valueOf(label);
				buttonItem.label = context.getString(R.string.min_, label);

			}
			return buttonItem;
		}
	}

	public class NewLiveGamesButtonsAdapter extends ItemsAdapter<NewLiveGameButtonItem> {

		private ItemClickListenerFace clickListenerFace;
		public static final int BUTTON_ID = 0x00001234;

		public NewLiveGamesButtonsAdapter(ItemClickListenerFace clickListenerFace, List<NewLiveGameButtonItem> itemList) {
			super(clickListenerFace.getMeContext(), itemList);
			this.clickListenerFace = clickListenerFace;
		}

		@Override
		protected View createView(ViewGroup parent) {
			RoboToggleButton view = new RoboToggleButton(getContext(), null, R.attr.greyButtonSmallSolid);
			view.setId(BUTTON_ID);
			view.setOnClickListener(clickListenerFace);
			return view;
		}

		@Override
		protected void bindView(NewLiveGameButtonItem item, int pos, View convertView) {
			((RoboToggleButton) convertView).setText(item.label);
			((RoboToggleButton) convertView).setChecked(item.checked);

			convertView.setTag(itemListId, pos);
		}

		public void checkButton(int checkedPosition) {
			for (NewLiveGameButtonItem item : itemsList) {
				item.checked = false;
			}

			itemsList.get(checkedPosition).checked = true;
			notifyDataSetChanged();
		}
	}

	public NewLiveGameConfig getNewLiveGameConfig() {

		return gameConfigBuilder.build();
	}

	private static class NewLiveGameConfig {
		private int daysPerMove;
		private int userColor;
		private boolean rated;
		private int gameType;
		private String opponentName;

		public static class Builder {
			private int daysPerMove;
			private int userColor;
			private boolean rated;
			private int gameType;
			private String opponentName;
/*
		loadItem.addRequestParams(RestHelper.P_DAYS_PER_MOVE, days);
		loadItem.addRequestParams(RestHelper.P_USER_SIDE, color);
		loadItem.addRequestParams(RestHelper.P_IS_RATED, isRated);
		loadItem.addRequestParams(RestHelper.P_GAME_TYPE, gameType);
		loadItem.addRequestParams(RestHelper.P_OPPONENT, opponentName);

fields____|___values__|required|______explanation__________________________________________________
opponent				false	See explanation above for possible values. Default is `null`.
daysPerMove		\d+		true	Days per move. 1,3,5,7,14
userPosition	0|1|2	true	User will play as - 0 = random, 1 = white, 2 = black. Default is `0`.
minRating		\d+		false	Minimum rating.
maxRating		\d+		false	Maximum rating.
isRated			0|1		true	Is game seek rated or not. Default is `1`.
gameType	chess(960)?	true	Game type code. Default is `1`.
gameSeekName	\w+		false	Name of new game/challenge. Default is `Let's Play!`.
			 */

			/**
			 * Create new Seek game with default values
			 */
			public Builder() {
				daysPerMove = 3;
				rated = true;
			}

			public Builder setDaysPerMove(int daysPerMove) {
				this.daysPerMove = daysPerMove;
				return this;
			}

			public Builder setUserColor(int userColor) {
				this.userColor = userColor;
				return this;
			}

			public Builder setRated(boolean rated) {
				this.rated = rated;
				return this;
			}

			public Builder setGameType(int gameType) {
				this.gameType = gameType;
				return this;
			}

			public Builder setOpponentName(String opponentName) {
				this.opponentName = opponentName;
				return this;
			}

			public NewLiveGameConfig build() {
				return new NewLiveGameConfig(this);
			}
		}

		private NewLiveGameConfig(Builder builder) {
			this.daysPerMove = builder.daysPerMove;
			this.userColor = builder.userColor;
			this.rated = builder.rated;
			this.gameType = builder.gameType;
			this.opponentName = builder.opponentName;
		}

		public int getDaysPerMove() {
			return daysPerMove;
		}

		public int getUserColor() {
			return userColor;
		}

		public boolean isRated() {
			return rated;
		}

		public int getGameType() {
			return gameType;
		}

		public String getOpponentName() {
			return opponentName;
		}
	}
}
