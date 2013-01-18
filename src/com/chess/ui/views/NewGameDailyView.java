package com.chess.ui.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import com.chess.*;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 14.01.13
 * Time: 7:23
 */
public class NewGameDailyView extends NewGameDefaultView implements ItemClickListenerFace {

	public static final int VS_ID = 101;
	public static final int RIGHT_BUTTON_ID = 102;

	private RoboButton playButton;
	private RoboButton leftButton;
	private RoboTextView vsText;
	private EditButton rightButton;
	private NewDailyGamesButtonsAdapter newDailyGamesButtonsAdapter;
	private NewDailyGameConfig.Builder gameConfigBuilder;
	private float minButtonHeight;

	public NewGameDailyView(Context context) {
		super(context);
	}

	public NewGameDailyView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NewGameDailyView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		gameConfigBuilder = new NewDailyGameConfig.Builder();

		minButtonHeight = getContext().getResources().getDimension(R.dimen.small_button_height);
	}

	@Override
	protected void addButtons(ConfigItem configItem, RelativeLayout compactRelLay) {
		// Left Button - "3 days Mode"
		leftButton = new RoboButton(getContext(), null, R.attr.greyButtonSmallSolid);
		RelativeLayout.LayoutParams leftBtnParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		leftBtnParams.addRule(RelativeLayout.BELOW, BASE_ID + TITLE_ID);
		leftButton.setText(configItem.getLeftButtonText());
		leftButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, BUTTON_TEXT_SIZE);
		leftButton.setId(BASE_ID + LEFT_BUTTON_ID);
		compactRelLay.addView(leftButton, leftBtnParams);

		// vs
		vsText = new RoboTextView(getContext());
		RelativeLayout.LayoutParams vsTxtParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		vsTxtParams.addRule(RelativeLayout.RIGHT_OF, BASE_ID + LEFT_BUTTON_ID);
		vsTxtParams.addRule(RelativeLayout.BELOW, BASE_ID + TITLE_ID);
		vsText.setPadding((int) (8 * density), (int) (10 * density), (int) (8 * density), 0);
		vsText.setText("vs");
		vsText.setTextColor(getContext().getResources().getColor(R.color.new_normal_gray));
		vsText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TOP_TEXT_SIZE);
		vsText.setId(BASE_ID + VS_ID);

		compactRelLay.addView(vsText, vsTxtParams);

		// Right Button - "Random"
		rightButton = new EditButton(getContext()/*, null, R.attr.greyButtonSmallSolid*/); // don't apply programmatically as it will lead to unable to appear keyboard on touch
		RelativeLayout.LayoutParams rightButtonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		rightButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		rightButtonParams.addRule(RelativeLayout.RIGHT_OF, BASE_ID + VS_ID);
		rightButtonParams.addRule(RelativeLayout.BELOW, BASE_ID + TITLE_ID);

		rightButton.setBackgroundResource(R.drawable.button_grey_solid_selector);
		rightButton.setMinHeight((int) minButtonHeight);
		rightButton.setTextColor(0xFFFFFFFF);
		rightButton.setGravity(Gravity.CENTER);
		rightButton.setCursorVisible(false);
		float shadowRadius = 0.5f ;
		float shadowDx = 0;
		float shadowDy = -1;
		rightButton.setShadowLayer(shadowRadius, shadowDx, shadowDy, Color.BLACK);
		rightButton.setId(BASE_ID + RIGHT_BUTTON_ID);
		rightButton.setText(configItem.getRightButtonText());
		rightButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, BUTTON_TEXT_SIZE);
		rightButton.setFont(RoboTextView.BOLD_FONT);

		compactRelLay.addView(rightButton, rightButtonParams);

	}

	@Override
	protected void addCustomView(ConfigItem configItem, RelativeLayout optionsAndPlayView) {
		// Play Button
		playButton = new RoboButton(getContext(), null, R.attr.orangeButtonSmall);
		RelativeLayout.LayoutParams playButtonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		playButtonParams.setMargins(0, (int) (4 * density), 0, 0);
		playButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		playButtonParams.addRule(RelativeLayout.CENTER_VERTICAL);

		playButton.setId(BASE_ID + PLAY_BUTTON_ID);
		playButton.setText(R.string.new_play_ex);
		playButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, BUTTON_TEXT_SIZE);

		optionsAndPlayView.addView(playButton, playButtonParams);
	}

	@Override
	public void toggleOptions() {
		optionsVisible = !optionsVisible;

		int compactVisibility = optionsVisible? GONE: VISIBLE;
		int expandVisibility = optionsVisible? VISIBLE: GONE;

		// Compact Items
		playButton.setVisibility(compactVisibility);
		rightButton.setVisibility(compactVisibility);
		leftButton.setVisibility(compactVisibility);
		vsText.setVisibility(compactVisibility);
		titleText.setVisibility(compactVisibility);

		optionsView.setVisibility(expandVisibility);

		toggleCompactView();
	}

	public void addOptionsView() {
		optionsView = LayoutInflater.from(getContext()).inflate(R.layout.new_game_option_daily_view, null, false);
		optionsView.setVisibility(GONE);
		addView(optionsView);

		{// Mode adapter init
			int[] newGameButtonsArray = getResources().getIntArray(R.array.days_per_move_array);
			List<NewDailyGameButtonItem> newGameButtonItems = new ArrayList<NewDailyGameButtonItem>();
			for (int label : newGameButtonsArray) {
				newGameButtonItems.add(NewDailyGameButtonItem.createNewButtonFromLabel(label, getContext()));
			}

			GridView gridView = (GridView) optionsView.findViewById(R.id.dailyGamesModeGrid);
			newDailyGamesButtonsAdapter = new NewDailyGamesButtonsAdapter(this, newGameButtonItems);
			gridView.setAdapter(newDailyGamesButtonsAdapter);
		}


		EditButton opponentEditBtn = (EditButton) findViewById(R.id.opponentEditBtn);
		opponentEditBtn.addOnClickListener(this);

		EditButton myColorEditBtn = (EditButton) findViewById(R.id.myColorEditBtn);
		myColorEditBtn.addOnClickListener(this);
	}

	@Override
	public Context getMeContext() {
		return getContext();
	}

	private static class NewDailyGameButtonItem {
		public boolean checked;
		public int days;
		public String label;

		static NewDailyGameButtonItem createNewButtonFromLabel(int label, Context context){
			NewDailyGameButtonItem buttonItem = new NewDailyGameButtonItem();

			buttonItem.days = label;
			buttonItem.label = context.getString(R.string.days_arg, label);
			return buttonItem;
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == NewDailyGamesButtonsAdapter.BUTTON_ID) {
			Integer position = (Integer) view.getTag(R.id.list_item_id);

			newDailyGamesButtonsAdapter.unCheckButtons(position);
			// set value to builder
			gameConfigBuilder.setDaysPerMove(newDailyGamesButtonsAdapter.getItem(position).days);
		} else if (view.getId() == R.id.opponentEditBtn){
			Log.d("TEST", "opponentEditBtn clicked");
		} else if (view.getId() == R.id.myColorEditBtn){
			Log.d("TEST", "myColorEditBtn clicked");

		}
	}

	private class NewDailyGamesButtonsAdapter extends ItemsAdapter<NewDailyGameButtonItem> {

		private ItemClickListenerFace clickListenerFace;
		public static final int BUTTON_ID = 0x00001234;

		public NewDailyGamesButtonsAdapter(ItemClickListenerFace clickListenerFace, List<NewDailyGameButtonItem> itemList) {
			super(clickListenerFace.getMeContext(), itemList);
			this.clickListenerFace = clickListenerFace;
		}

		@Override
		protected View createView(ViewGroup parent) {
//			ViewHolder holder = new ViewHolder();
			RoboToggleButton view = new RoboToggleButton(getContext(), null, R.attr.greyButtonSmallSolid);
//			holder.label = view;
//			view.setTag(holder);
			view.setId(BUTTON_ID);
			view.setOnClickListener(clickListenerFace);
			return view;
		}

		@Override
		protected void bindView(NewDailyGameButtonItem item, int pos, View convertView) {
//			ViewHolder holder = (ViewHolder) convertView.getTag();
//			holder.label.setText(item.label);
			((RoboToggleButton)convertView).setText(item.label);
			((RoboToggleButton)convertView).setChecked(item.checked);
			convertView.setTag(itemListId, pos);
		}

//		private class ViewHolder{
//			RoboToggleButton label;
//		}

		public void unCheckButtons(int checkedPosition){
			for (NewDailyGameButtonItem item : itemsList) {
				item.checked = false;
			}

			itemsList.get(checkedPosition).checked = true;
			notifyDataSetChanged();
		}
	}

	public NewDailyGameConfig getNewDailyGameConfig(){

		return gameConfigBuilder.build();
	}

	private static class NewDailyGameConfig {
		private int daysPerMove;
		private int userColor;
		private boolean rated;
		private int gameType;
		private String opponentName;

		public static class Builder{
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
			public Builder(){
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

			public NewDailyGameConfig build(){
				return new NewDailyGameConfig(this);
			}
		}

		private NewDailyGameConfig(Builder builder) {
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
