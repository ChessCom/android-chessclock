package com.chess.ui.fragments.live;

import android.animation.LayoutTransition;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.GetAndSaveUserStats;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.model.SelectionItem;
import com.chess.statics.IntentConstants;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.views.drawables.RatingProgressDrawable;
import com.chess.widgets.RoboRadioButton;
import com.chess.widgets.RoboSpinner;
import com.chess.widgets.SwitchButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 26.04.13
 * Time: 10:00
 */
public class LiveGameOptionsFragment extends CommonLogicFragment implements ItemClickListenerFace,
		AdapterView.OnItemSelectedListener, SeekBar.OnSeekBarChangeListener {

	private static final int MIN_RATING_MIN = -1000;
	private static final int MIN_RATING_MAX = 0;
	private static final int MAX_RATING_MIN = 0;
	private static final int MAX_RATING_MAX = 1000;
	private static final long SEEK_BAR_HIDE_DELAY = 5 * 1000;
	public static final int MAX_PROGRESS = 20;

	private LiveGameConfig.Builder gameConfigBuilder;
	private RoboRadioButton minRatingBtn;
	private RoboRadioButton maxRatingBtn;
	private SwitchButton ratedGameSwitch;

	private List<SelectionItem> friendsList;
	private List<View> liveOptionsGroup;
	private HashMap<Integer, Button> liveButtonsModeMap;
	private boolean liveOptionsVisible;

	private int positionMode;
	private String opponentName;
	private int standardRating;
	private int blitzRating;
	private int lightningRating;

	private SeekBar ratingSeekBar;
	private IntentFilter statsUpdateFilter;
	private StatsSavedReceiver statsSavedReceiver;
	private boolean statsLoaded;
	private ViewGroup ratingView;
	private TextView currentRatingTxt;

	public LiveGameOptionsFragment() {
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, RIGHT_MENU_MODE);
		setArguments(bundle);
	}

	public static LiveGameOptionsFragment createInstance(int mode) {
		LiveGameOptionsFragment fragment = new LiveGameOptionsFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, mode);
		fragment.setArguments(bundle);
		return fragment;
	}

	public static LiveGameOptionsFragment createInstance(int mode, String opponentName) {
		LiveGameOptionsFragment fragment = new LiveGameOptionsFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, mode);
		bundle.putString(OPPONENT_NAME, opponentName);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			positionMode = getArguments().getInt(MODE);
			opponentName = getArguments().getString(OPPONENT_NAME);
		} else {
			positionMode = savedInstanceState.getInt(MODE);
			opponentName = savedInstanceState.getString(OPPONENT_NAME);
		}

		gameConfigBuilder = getAppData().getLiveGameConfigBuilder();

		{ // load friends from DB
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getTableForUser(getUsername(), DbScheme.Tables.FRIENDS));

			friendsList = new ArrayList<SelectionItem>();
			friendsList.add(new SelectionItem(null, getString(R.string.random)));
			boolean opponentFound = false;
			if (cursor != null && cursor.moveToFirst()) {
				do {
					String friendName = DbDataManager.getString(cursor, DbScheme.V_USERNAME);
					friendsList.add(new SelectionItem(null, friendName));
					if (friendName.equals(opponentName)) {
						opponentFound = true;
					}
				} while (cursor.moveToNext());
			}
			if (cursor != null) {
				cursor.close();
			}

			friendsList.get(0).setChecked(true);

			if (!opponentFound) {// add manually opponent if it wasn't found
				friendsList.add(new SelectionItem(null, opponentName));
			}
		}

		standardRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_LIVE_STANDARD.ordinal(), getUsername());
		blitzRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_LIVE_BLITZ.ordinal(), getUsername());
		lightningRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_LIVE_LIGHTNING.ordinal(), getUsername());

		if (standardRating == 0 || blitzRating == 0 || lightningRating == 0 && !statsLoaded) { // if stats were not save
			showLoadingProgress(true);

			getActivity().startService(new Intent(getActivity(), GetAndSaveUserStats.class));

			statsUpdateFilter = new IntentFilter(IntentConstants.STATS_SAVED);

			statsLoaded = false;
		} else {
			statsLoaded = true;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_game_option_live_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (statsUpdateFilter != null) {
			statsSavedReceiver = new StatsSavedReceiver();
			registerReceiver(statsSavedReceiver, statsUpdateFilter);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (statsUpdateFilter != null) {
			unRegisterMyReceiver(statsSavedReceiver);
		}

		// save config
		getAppData().setLiveGameConfigBuilder(gameConfigBuilder);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(MODE, positionMode);
	}

	private class StatsSavedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			statsLoaded = true;
			showLoadingProgress(false);

			standardRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_LIVE_STANDARD.ordinal(), getUsername());
			blitzRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_LIVE_BLITZ.ordinal(), getUsername());
			lightningRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_LIVE_LIGHTNING.ordinal(), getUsername());

			updateRatingRange();
		}
	}

	private String getLiveModeButtonLabel(String label) {
		if (label.contains(Symbol.SLASH)) { // "5 | 2"
			return label;
		} else { // "10 min"
			return getString(R.string.min_arg, label);
		}
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		if (parent.getAdapter() instanceof OpponentsAdapter) {
			SelectionItem opponent = (SelectionItem) parent.getItemAtPosition(position);
			String opponentName = opponent.getText();
			if (opponentName.equals(getString(R.string.random))) {
				ratingView.setVisibility(View.VISIBLE);
				opponentName = null;
			} else {
				ratingView.setVisibility(View.GONE);
			}
			gameConfigBuilder.setOpponentName(opponentName);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	private void handleLiveModeClicks(View view) {
		int id = view.getId();
		boolean liveModeButton = false;
		for (Button button : liveButtonsModeMap.values()) {
			if (id == button.getId()) {
				liveModeButton = true;
				break;
			}
		}

		if (liveModeButton) {
			for (Map.Entry<Integer, Button> buttonEntry : liveButtonsModeMap.entrySet()) {
				Button button = buttonEntry.getValue();
				button.setSelected(false);
				if (id == button.getId()) {
					setDefaultQuickLiveMode(view, buttonEntry.getKey());
				}
			}
		}
	}

	private void setDefaultQuickLiveMode(View view, int mode) {
		view.setSelected(true);

		gameConfigBuilder.setTimeFromMode(mode);

		updateRatingRange();
		getAppData().setDefaultLiveMode(mode);
	}

	private void toggleLiveOptionsView() {
		liveOptionsVisible = !liveOptionsVisible;
		for (View view : liveOptionsGroup) {
			view.setVisibility(liveOptionsVisible ? View.VISIBLE : View.GONE);
		}

		int selectedLiveTimeMode = getAppData().getDefaultLiveMode();
		for (Map.Entry<Integer, Button> buttonEntry : liveButtonsModeMap.entrySet()) {
			Button button = buttonEntry.getValue();
			button.setVisibility(liveOptionsVisible ? View.VISIBLE : View.GONE);
			if (liveOptionsVisible) {
				if (selectedLiveTimeMode == buttonEntry.getKey()) {
					button.setSelected(true);
				}
			}
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
		int rating = standardRating;
		if (gameConfigBuilder.getTimeMode() == LiveGameConfig.BLITZ) {
			rating = blitzRating;
		} else if (gameConfigBuilder.getTimeMode() == LiveGameConfig.LIGHTNING) {
			rating = lightningRating;
		}

		TextView checkedButton;

		float factor;
		int value;
		String symbol = Symbol.EMPTY;
		if (minRatingBtn.isChecked()) {
			checkedButton = minRatingBtn;

			factor = (MIN_RATING_MAX - MIN_RATING_MIN) / MAX_PROGRESS; // (maxRatingDiff - minRatingDiff) / maxSeekProgress
			value =  1000 - (int) (factor * progress);
			if (value != 0) {
				symbol = Symbol.MINUS;
			}

			// update min rating
			gameConfigBuilder.setMinRating(rating - value);
		} else {
			checkedButton = maxRatingBtn;

			factor = (MAX_RATING_MAX - MAX_RATING_MIN) / MAX_PROGRESS; // (maxRatingDiff - minRatingDiff) / maxSeekProgress
			value =  (int) (factor * progress);
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
		handler.removeCallbacks(hideSeekBarRunnable);
	}

	Runnable hideSeekBarRunnable = new Runnable() {
		@Override
		public void run() {
			ratingSeekBar.setVisibility(View.GONE);
		}
	};

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.liveHeaderView) {
			getActivityFace().toggleRightMenu();
		} else if (view.getId() == R.id.ratedGameView) {
			ratedGameSwitch.toggle();
		} else if (view.getId() == R.id.minRatingBtn) {
			ratingSeekBar.setVisibility(View.VISIBLE);
			handler.postDelayed(hideSeekBarRunnable, SEEK_BAR_HIDE_DELAY);
		} else if (view.getId() == R.id.maxRatingBtn) {
			ratingSeekBar.setVisibility(View.VISIBLE);
			handler.postDelayed(hideSeekBarRunnable, SEEK_BAR_HIDE_DELAY);
		} else if (view.getId() == R.id.playBtn) {
			getActivityFace().openFragment(LiveGameWaitFragment.createInstance(getLiveGameConfig()));
			getActivityFace().toggleRightMenu();
		} else {
			handleLiveModeClicks(view);
		}
	}

	private void updateRatingRange() {
		if (gameConfigBuilder.getTimeMode() == LiveGameConfig.STANDARD) {
			currentRatingTxt.setText(String.valueOf(standardRating));
		} else if (gameConfigBuilder.getTimeMode() == LiveGameConfig.BLITZ) {
			currentRatingTxt.setText(String.valueOf(blitzRating));
		} else if (gameConfigBuilder.getTimeMode() == LiveGameConfig.LIGHTNING) {
			currentRatingTxt.setText(String.valueOf(lightningRating));
		}
	}

	public LiveGameConfig getLiveGameConfig() {
		// set params
		gameConfigBuilder.setRated(ratedGameSwitch.isChecked());

		String minRatingBtnText = minRatingBtn.getText().toString();
		String maxRatingBtnText = maxRatingBtn.getText().toString();

		Integer minRatingValue = Integer.valueOf(minRatingBtnText.replace(Symbol.PLUS,Symbol.EMPTY).replace(Symbol.MINUS, Symbol.EMPTY));
		Integer maxRatingValue = Integer.valueOf(maxRatingBtnText.replace(Symbol.PLUS,Symbol.EMPTY).replace(Symbol.MINUS, Symbol.EMPTY));

		int rating = 0;
		if (gameConfigBuilder.getTimeMode() == LiveGameConfig.STANDARD) {
			rating = standardRating;
		} else if (gameConfigBuilder.getTimeMode() == LiveGameConfig.BLITZ) {
			rating = blitzRating;
		} else if (gameConfigBuilder.getTimeMode() == LiveGameConfig.LIGHTNING) {
			rating = lightningRating;
		}

		gameConfigBuilder.setMinRating(rating - minRatingValue);
		gameConfigBuilder.setMaxRating(rating + maxRatingValue);

		getAppData().setLiveGameConfigBuilder(gameConfigBuilder);

		return gameConfigBuilder.build(true);
	}

	private void widgetsInit(View view) {
		view.findViewById(R.id.liveOptionsView).setOnClickListener(this);
		view.findViewById(R.id.ratedGameView).setOnClickListener(this);

		LayoutInflater inflater = getActivity().getLayoutInflater();
		RelativeLayout liveHomeOptionsFrame = (RelativeLayout) view.findViewById(R.id.liveHomeOptionsFrame);
		inflater.inflate(R.layout.new_right_live_options_view, liveHomeOptionsFrame, true);

		if (getArguments().getInt(MODE) == CENTER_MODE) { // we use white background and dark titles for centered mode
			View liveHeaderView = view.findViewById(R.id.liveHeaderView);
			liveHeaderView.setVisibility(View.VISIBLE);
			liveHeaderView.setOnClickListener(this);
		}

		RoboSpinner opponentSpinner = (RoboSpinner) view.findViewById(R.id.opponentSpinner);

		OpponentsAdapter selectionAdapter = new OpponentsAdapter(getActivity(), friendsList);
		opponentSpinner.setAdapter(selectionAdapter);
		opponentSpinner.setOnItemSelectedListener(this);
		opponentSpinner.setSelection(0);

		if (!TextUtils.isEmpty(opponentName)) {
			for (int i = 0; i < friendsList.size(); i++) {
				SelectionItem selectionItem = friendsList.get(i);
				if (selectionItem.getText().equals(opponentName)) {
					opponentSpinner.setSelection(i);
					break;
				}
			}
		}

		{ // live options
			if (JELLY_BEAN_PLUS_API) {
				ViewGroup liveOptionsView = (ViewGroup) view.findViewById(R.id.gameOptionsLiveLinLay);
				LayoutTransition layoutTransition = liveOptionsView.getLayoutTransition();
				layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
			}

			liveOptionsGroup = new ArrayList<View>();
			liveOptionsGroup.add(view.findViewById(R.id.liveLabelStandardTxt));
			liveOptionsGroup.add(view.findViewById(R.id.liveLabelBlitzTxt));
			liveOptionsGroup.add(view.findViewById(R.id.liveLabelBulletTxt));

			liveButtonsModeMap = new HashMap<Integer, Button>();
			liveButtonsModeMap.put(0, (Button) view.findViewById(R.id.standard1SelectBtn));
			liveButtonsModeMap.put(1, (Button) view.findViewById(R.id.blitz1SelectBtn));
			liveButtonsModeMap.put(2, (Button) view.findViewById(R.id.blitz2SelectBtn));
			liveButtonsModeMap.put(3, (Button) view.findViewById(R.id.bullet1SelectBtn));
			liveButtonsModeMap.put(4, (Button) view.findViewById(R.id.standard2SelectBtn));
			liveButtonsModeMap.put(5, (Button) view.findViewById(R.id.blitz3SelectBtn));
			liveButtonsModeMap.put(6, (Button) view.findViewById(R.id.blitz4SelectBtn));
			liveButtonsModeMap.put(7, (Button) view.findViewById(R.id.bullet2SelectBtn));

			{// options setup
				// rated games switch
				ratedGameSwitch = (SwitchButton) view.findViewById(R.id.ratedGameSwitch);

				{// Rating part
					ratingView = (ViewGroup) view.findViewById(R.id.ratingView);

					int minRatingDefault;
					int maxRatingDefault;

					int minRating = gameConfigBuilder.getMinRating();
					int maxRating = gameConfigBuilder.getMaxRating();

					int rating = 0;
					if (gameConfigBuilder.getTimeMode() == LiveGameConfig.STANDARD) {
						rating = standardRating;
					} else if (gameConfigBuilder.getTimeMode() == LiveGameConfig.BLITZ) {
						rating = blitzRating;
					} else if (gameConfigBuilder.getTimeMode() == LiveGameConfig.LIGHTNING) {
						rating = lightningRating;
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

					ratingSeekBar = (SeekBar) view.findViewById(R.id.ratingSeekBar);
					ratingSeekBar.setOnSeekBarChangeListener(this);
					ratingSeekBar.setProgressDrawable(new RatingProgressDrawable(getContext(), ratingSeekBar));
					ratingSeekBar.setVisibility(View.GONE);

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
				}

				view.findViewById(R.id.playBtn).setOnClickListener(this);
			}

			{// time mode buttons
				int mode = getAppData().getDefaultLiveMode();
				gameConfigBuilder.setTimeFromMode(mode);

				currentRatingTxt.setText(String.valueOf(standardRating));
				if (gameConfigBuilder.getTimeMode() == LiveGameConfig.BLITZ) {
					currentRatingTxt.setText(String.valueOf(blitzRating));
				} else if (gameConfigBuilder.getTimeMode() == LiveGameConfig.LIGHTNING) {
					currentRatingTxt.setText(String.valueOf(lightningRating));
				}

				// set texts to buttons
				String[] newGameButtonsArray = getResources().getStringArray(R.array.new_live_game_button_values);
				for (Map.Entry<Integer, Button> buttonEntry : liveButtonsModeMap.entrySet()) {
					int key = buttonEntry.getKey();
					buttonEntry.getValue().setText(getLiveModeButtonLabel(newGameButtonsArray[key]));
					buttonEntry.getValue().setOnClickListener(this);

					if (key == mode) {
						setDefaultQuickLiveMode(buttonEntry.getValue(), buttonEntry.getKey());
					}
				}
			}
		}

		toggleLiveOptionsView();
	}

	public class OpponentsAdapter extends ItemsAdapter<SelectionItem> {

		public OpponentsAdapter(Context context, List<SelectionItem> items) {
			super(context, items);
		}

		@Override
		protected View createView(ViewGroup parent) {
			View view = inflater.inflate(R.layout.new_game_opponent_spinner_item, parent, false);
			ViewHolder holder = new ViewHolder();
			holder.textTxt = (TextView) view.findViewById(R.id.opponentNameTxt);

			view.setTag(holder);
			return view;
		}

		@Override
		protected void bindView(SelectionItem item, int pos, View convertView) {
			ViewHolder holder = (ViewHolder) convertView.getTag();
			holder.textTxt.setText(item.getText());
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			DropViewHolder holder = new DropViewHolder();
			if (convertView == null) {
				convertView = inflater.inflate(android.R.layout.simple_list_item_single_choice, parent, false);
				holder.textTxt = (TextView) convertView.findViewById(android.R.id.text1);

				convertView.setTag(holder);
			} else {
				holder = (DropViewHolder) convertView.getTag();
			}

			holder.textTxt.setTextColor(context.getResources().getColor(R.color.black));
			holder.textTxt.setText(itemsList.get(position).getText());

			return convertView;
		}

		private class ViewHolder {
			TextView textTxt;
		}

		private class DropViewHolder {
			TextView textTxt;
		}

	}
}
