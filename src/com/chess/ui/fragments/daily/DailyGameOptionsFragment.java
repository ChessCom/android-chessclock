package com.chess.ui.fragments.daily;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.daily_games.DailySeekItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.engine.configs.DailyGameConfig;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.friends.FriendsRightFragment;
import com.chess.ui.fragments.popup_fragments.PopupOptionsMenuFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.interfaces.PopupListSelectionFace;
import com.chess.ui.views.drawables.RatingProgressDrawable;
import com.chess.ui.views.drawables.smart_button.ButtonGlassyDrawable;
import com.chess.widgets.RoboButton;
import com.chess.widgets.RoboRadioButton;
import com.chess.widgets.SwitchButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.04.13
 * Time: 14:26
 */
public class DailyGameOptionsFragment extends CommonLogicFragment implements ItemClickListenerFace,
		AdapterView.OnItemSelectedListener, PopupListSelectionFace, SeekBar.OnSeekBarChangeListener {

	private static final int MIN_RATING_MIN = -1000;
	private static final int MIN_RATING_MAX = 0;
	private static final int MAX_RATING_MIN = 0;
	private static final int MAX_RATING_MAX = 1000;

	private static final int ID_CHESS = 0;
	private static final int ID_CHESS_960 = 1;
	private static final String OPTION_SELECTION_TAG = "options selection popup";
	private static final long SEEK_BAR_HIDE_DELAY = 5 * 1000;
	public static final int MAX_PROGRESS = 20;

	private DailyGamesButtonsAdapter dailyGamesButtonsAdapter;
	private DailyGameConfig.Builder gameConfigBuilder;
	private RoboRadioButton minRatingBtn;
	private RoboRadioButton maxRatingBtn;
	private SwitchButton ratedGameSwitch;

	private CreateChallengeUpdateListener createChallengeUpdateListener;
//	private List<SelectionItem> friendsList;
	private int chessRating;
	private int chess960Rating;
	private int positionMode;
	private ViewGroup ratingView;
	private SparseArray<String> optionsMap;
	private PopupOptionsMenuFragment optionsSelectFragment;
	private Button gameTypeBtn;
	private String opponentName;
	private SeekBar ratingSeekBar;
	private TextView currentRatingTxt;
	private TextView opponentNameTxt;

	public DailyGameOptionsFragment() {
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, RIGHT_MENU_MODE);
		setArguments(bundle);
	}

	public static DailyGameOptionsFragment createInstance(int mode) {
		DailyGameOptionsFragment fragment = new DailyGameOptionsFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, mode);
		fragment.setArguments(bundle);
		return fragment;
	}

	public static DailyGameOptionsFragment createInstance(int mode, String opponentName) {
		DailyGameOptionsFragment fragment = new DailyGameOptionsFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, mode);
		bundle.putString(OPPONENT_NAME, opponentName);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String username = getUsername();

		if (getArguments() != null) {
			positionMode = getArguments().getInt(MODE);
			opponentName = getArguments().getString(OPPONENT_NAME);
		} else {
			positionMode = savedInstanceState.getInt(MODE);
			opponentName = savedInstanceState.getString(OPPONENT_NAME);
		}

		gameConfigBuilder = getAppData().getDailyGameConfigBuilder();

		chessRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_DAILY_CHESS.ordinal(), username);
		chess960Rating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_DAILY_CHESS960.ordinal(), username);
		createChallengeUpdateListener = new CreateChallengeUpdateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_game_option_daily_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		updateDailyMode(getAppData().getDefaultDailyMode());
	}

	@Override
	public void onPause() {
		super.onPause();

		// save config
		getAppData().setDailyGameConfigBuilder(gameConfigBuilder);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(MODE, positionMode);
	}

	private void createDailyChallenge() {
		// create challenge using formed configuration
		DailyGameConfig dailyGameConfig = getDailyGameConfig();

		LoadItem loadItem = LoadHelper.postGameSeek(getUserToken(), dailyGameConfig);
		new RequestJsonTask<DailySeekItem>(createChallengeUpdateListener).executeTask(loadItem);
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		updateDailyMode(getAppData().getDefaultDailyMode());
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	private void updateDailyMode(final int position) {
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				dailyGamesButtonsAdapter.checkButton(position);
				// set value to builder
				gameConfigBuilder.setDaysPerMove(dailyGamesButtonsAdapter.getItem(position).days);
			}
		}, 1000);
	}

	@Override
	public void onValueSelected(int code) {
		if (code == ID_CHESS) {
			gameConfigBuilder.setGameType(RestHelper.V_GAME_CHESS);
			gameTypeBtn.setText(R.string.standard);

			currentRatingTxt.setText(String.valueOf(chessRating));
		} else if (code == ID_CHESS_960) {
			gameConfigBuilder.setGameType(RestHelper.V_GAME_CHESS_960);
			gameTypeBtn.setText(R.string.chess_960);

			currentRatingTxt.setText(String.valueOf(chess960Rating));
		}

		optionsSelectFragment.dismiss();
		optionsSelectFragment = null;
	}

	@Override
	public void onDialogCanceled() {
		optionsSelectFragment = null;
	}

	private class CreateChallengeUpdateListener extends ChessLoadUpdateListener<DailySeekItem> {

		public CreateChallengeUpdateListener() {
			super(DailySeekItem.class);
		}

		@Override
		public void updateData(DailySeekItem returnedObj) {
			showSinglePopupDialog(R.string.challenge_created, R.string.you_will_notified_when_game_starts);
		}
	}

	private CompoundButton.OnCheckedChangeListener ratingSelectionChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (buttonView.getId() == R.id.minRatingBtn && isChecked) {
				minRatingBtn.setChecked(true);
				maxRatingBtn.setChecked(false);

			} else if (buttonView.getId() == R.id.maxRatingBtn && isChecked) {
				maxRatingBtn.setChecked(true);
				minRatingBtn.setChecked(false);
			}
		}
	};

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		int rating = chessRating;
		if (gameConfigBuilder.getGameType() == RestHelper.V_GAME_CHESS_960) {
			rating = chess960Rating;
		}

		TextView checkedButton;

		float factor;
		int value;
		String symbol = Symbol.EMPTY;
		if (minRatingBtn.isChecked()) {
			checkedButton = minRatingBtn;

			factor = (MIN_RATING_MAX - MIN_RATING_MIN) / MAX_PROGRESS; // (maxRatingDiff - minRatingDiff) / maxSeekProgress
			value = 1000 - (int) (factor * progress);
			if (value != 0) {
				symbol = Symbol.MINUS;
			}

			// update min rating
			gameConfigBuilder.setMinRating(rating - value);
		} else {
			checkedButton = maxRatingBtn;

			factor = (MAX_RATING_MAX - MAX_RATING_MIN) / MAX_PROGRESS; // (maxRatingDiff - minRatingDiff) / maxSeekProgress
			value = (int) (factor * progress);
			if (value != 0) {
				symbol = Symbol.PLUS;
			}

			// update max rating
			gameConfigBuilder.setMaxRating(rating - value);
		}

		checkedButton.setText(symbol + String.valueOf(value));
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		handler.removeCallbacks(hideSeekBarRunnable);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		handler.postDelayed(hideSeekBarRunnable, SEEK_BAR_HIDE_DELAY);
	}



	private class DailyGameButtonItem {
		public boolean checked;
		public int days;
		public String label;

		DailyGameButtonItem(int label) {
			this.days = label;
			this.label = String.valueOf(label);
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == DailyGamesButtonsAdapter.BUTTON_ID) {
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			updateDailyMode(position);
			getAppData().setDefaultDailyMode(position);
		} else if (view.getId() == R.id.dailyHeaderView) {
			getActivityFace().toggleRightMenu();
		} else if (view.getId() == R.id.gameTypeBtn) {
			if (optionsSelectFragment != null) { // if we already showing these options
				return;
			}
			optionsSelectFragment = PopupOptionsMenuFragment.createInstance(this, optionsMap);
			optionsSelectFragment.show(getFragmentManager(), OPTION_SELECTION_TAG);
		} else if (view.getId() == R.id.ratedGameView) {
			ratedGameSwitch.toggle();
		} else if (view.getId() == R.id.minRatingBtn) {
			ratingSeekBar.setVisibility(View.VISIBLE);
			handler.postDelayed(hideSeekBarRunnable, SEEK_BAR_HIDE_DELAY);
		} else if (view.getId() == R.id.maxRatingBtn) {
			ratingSeekBar.setVisibility(View.VISIBLE);
			handler.postDelayed(hideSeekBarRunnable, SEEK_BAR_HIDE_DELAY);
		} else if (view.getId() == R.id.opponentView) {
			getActivityFace().changeRightFragment(FriendsRightFragment.createInstance(FriendsRightFragment.DAILY_OPPONENT_REQUEST));
		} else if (view.getId() == R.id.playBtn) {
			createDailyChallenge();
		}
	}


	Runnable hideSeekBarRunnable = new Runnable() {
		@Override
		public void run() {
			ratingSeekBar.setVisibility(View.GONE);
		}
	};

	private class DailyGamesButtonsAdapter extends ItemsAdapter<DailyGameButtonItem> {

		private ItemClickListenerFace clickListenerFace;
		public static final int BUTTON_ID = 0x00001234;
		private ColorStateList textColor;

		public DailyGamesButtonsAdapter(ItemClickListenerFace clickListenerFace, List<DailyGameButtonItem> itemList) {
			super(clickListenerFace.getMeContext(), itemList);
			this.clickListenerFace = clickListenerFace;

			textColor = getResources().getColorStateList(R.color.text_controls_icons);
		}

		@Override
		protected View createView(ViewGroup parent) {
			RoboButton button = new RoboButton(getContext(), null, R.attr.greyButtonSmallSolid);
			button.setId(BUTTON_ID);
			button.setMinimumWidth(getResources().getDimensionPixelSize(R.dimen.new_daily_grid_button_width));
			button.setDrawableStyle(R.style.Button_Glassy);
			button.setTextColor(textColor);
			button.setOnClickListener(clickListenerFace);
			return button;
		}

		@Override
		protected void bindView(DailyGameButtonItem item, int pos, View convertView) {
			convertView.setTag(itemListId, pos);

			((RoboButton) convertView).setText(item.label);

			Drawable background = convertView.getBackground();
			if (item.checked) {
				((RoboButton) convertView).setTextColor(Color.WHITE);
				background.mutate().setState(ButtonGlassyDrawable.STATE_SELECTED);
			} else {
				((RoboButton) convertView).setTextColor(textColor);
				background.mutate().setState(ButtonGlassyDrawable.STATE_ENABLED);
			}
		}

		public void checkButton(int checkedPosition) {
			for (DailyGameButtonItem item : itemsList) {
				item.checked = false;
			}
			itemsList.get(checkedPosition).checked = true;
			notifyDataSetChanged();
		}
	}

	public DailyGameConfig getDailyGameConfig() {
		// set params
		gameConfigBuilder.setRated(ratedGameSwitch.isChecked());

		String minRatingBtnText = minRatingBtn.getText().toString();
		String maxRatingBtnText = maxRatingBtn.getText().toString();

		Integer minRatingValue = Integer.valueOf(minRatingBtnText.replace(Symbol.PLUS, Symbol.EMPTY).replace(Symbol.MINUS, Symbol.EMPTY));
		Integer maxRatingValue = Integer.valueOf(maxRatingBtnText.replace(Symbol.PLUS, Symbol.EMPTY).replace(Symbol.MINUS, Symbol.EMPTY));

		int rating = chessRating;
		if (gameConfigBuilder.getGameType() == RestHelper.V_GAME_CHESS_960) {
			rating = chess960Rating;
		}

		gameConfigBuilder.setMinRating(rating - minRatingValue);
		gameConfigBuilder.setMaxRating(rating + maxRatingValue);

		getAppData().setDailyGameConfigBuilder(gameConfigBuilder);

		return gameConfigBuilder.build();
	}

	private void widgetsInit(View view) {
		Resources resources = getResources();

		if (positionMode == CENTER_MODE) {
			View dailyHeaderView = view.findViewById(R.id.dailyHeaderView);
			dailyHeaderView.setVisibility(View.VISIBLE);
			dailyHeaderView.setOnClickListener(this);
		}

		view.findViewById(R.id.ratedGameView).setOnClickListener(this);

		{// options setup
			gameTypeBtn = (Button) view.findViewById(R.id.gameTypeBtn);
			gameTypeBtn.setOnClickListener(this);
			{// Mode adapter init
				int[] newGameButtonsArray = resources.getIntArray(R.array.days_per_move_array);
				List<DailyGameButtonItem> newGameButtonItems = new ArrayList<DailyGameButtonItem>();
				for (int label : newGameButtonsArray) {
					newGameButtonItems.add(new DailyGameButtonItem(label));
				}
				int dailyMode = getAppData().getDefaultDailyMode();
				newGameButtonItems.get(dailyMode).checked = true;

				GridView gridView = (GridView) view.findViewById(R.id.dailyGamesModeGrid);
				dailyGamesButtonsAdapter = new DailyGamesButtonsAdapter(this, newGameButtonItems);
				gridView.setAdapter(dailyGamesButtonsAdapter);
			}

			// rated games switch
			ratedGameSwitch = (SwitchButton) view.findViewById(R.id.ratedGameSwitch);
			{// options list setup
				optionsMap = new SparseArray<String>();
				optionsMap.put(ID_CHESS, getString(R.string.standard));
				optionsMap.put(ID_CHESS_960, getString(R.string.chess_960));
			}
			{// Rating part
				ratingView = (ViewGroup) view.findViewById(R.id.ratingView);
				int minRatingDefault;
				int maxRatingDefault;

				int minRating = gameConfigBuilder.getMinRating();
				int maxRating = gameConfigBuilder.getMaxRating();

				int rating;
				if (gameConfigBuilder.getGameType() == RestHelper.V_GAME_CHESS) {
					rating = chessRating;
				} else {
					rating = chess960Rating;
				}

				if (minRating == 0) { // first time
					minRating = rating - LiveGameConfig.RATING_STEP;
					gameConfigBuilder.setMinRating(minRating);
				}
				if (maxRating == 0) { // first time
					maxRating = rating + LiveGameConfig.RATING_STEP;
					gameConfigBuilder.setMaxRating(maxRating);
				}

				minRatingDefault = minRating - rating;
				maxRatingDefault = maxRating - rating;

				String minRatingStr;
				String maxRatingStr;
				if (minRatingDefault == 0) {
					minRatingStr = Symbol.MINUS + LiveGameConfig.RATING_STEP;
				} else {
					minRatingStr = String.valueOf(minRatingDefault);
				}
				if (maxRatingDefault == 0) {
					maxRatingStr = Symbol.PLUS + LiveGameConfig.RATING_STEP;
				} else {
					maxRatingStr = Symbol.PLUS + String.valueOf(Math.abs(maxRatingDefault));
				}

				if (JELLY_BEAN_PLUS_API) {
					LayoutTransition layoutTransition = ratingView.getLayoutTransition();
					layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
				}

				currentRatingTxt = (TextView) view.findViewById(R.id.currentRatingTxt);
				currentRatingTxt.setText(String.valueOf(chessRating));

				minRatingBtn = (RoboRadioButton) view.findViewById(R.id.minRatingBtn);
				minRatingBtn.setOnCheckedChangeListener(ratingSelectionChangeListener);
				minRatingBtn.setOnClickListener(this);
				minRatingBtn.setText(minRatingStr);

				maxRatingBtn = (RoboRadioButton) view.findViewById(R.id.maxRatingBtn);
				maxRatingBtn.setOnCheckedChangeListener(ratingSelectionChangeListener);
				maxRatingBtn.setOnClickListener(this);
				maxRatingBtn.setText(maxRatingStr);

				// set checked minRating Button
				minRatingBtn.setChecked(true);

				ratingSeekBar = (SeekBar) view.findViewById(R.id.ratingSeekBar);
				ratingSeekBar.setOnSeekBarChangeListener(this);
				ratingSeekBar.setProgressDrawable(new RatingProgressDrawable(getContext(), ratingSeekBar));
				ratingSeekBar.setVisibility(View.GONE);
			}

			{ // Opponent View
				view.findViewById(R.id.opponentView).setOnClickListener(this);
				opponentNameTxt = (TextView) view.findViewById(R.id.opponentNameTxt);
				if (!TextUtils.isEmpty(opponentName)) {
					opponentNameTxt.setText(opponentName);
					gameConfigBuilder.setOpponentName(opponentName);
					ratingView.setVisibility(View.GONE);
				} else {
					gameConfigBuilder.setOpponentName(Symbol.EMPTY);
					ratingView.setVisibility(View.VISIBLE);
				}
			}

			view.findViewById(R.id.playBtn).setOnClickListener(this);
		}
	}
}
