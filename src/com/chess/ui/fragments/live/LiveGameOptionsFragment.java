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

	private static final int RATING_VARIABLE_DIFF = 100;
	private static final int MIN_RATING_MIN = 0;
	private static final int MIN_RATING_MAX = 2600;
	private static final int MAX_RATING_MIN = 0;
	private static final int MAX_RATING_MAX = 2800;

	private LiveGameConfig.Builder gameConfigBuilder;
	private RoboRadioButton minRatingBtn;
	private RoboRadioButton maxRatingBtn;
	private SwitchButton ratedGameSwitch;

	private List<SelectionItem> friendsList;
	private List<View> liveOptionsGroup;
	private HashMap<Integer, Button> liveButtonsModeMap;
	private boolean liveOptionsVisible;

	private String[] newGameButtonsArray;
	private int positionMode;
	//	private int liveRating;
	private String opponentName;
	private int liveStandardRating;
	private int liveBlitzRating;
	private int liveLightningRating;

	private HashMap<Integer, Integer> liveModesMap;
	private int liveGameMode;
	private SeekBar ratingBar;
	private IntentFilter statsUpdateFilter;
	private StatsSavedReceiver statsSavedReceiver;
	private boolean statsLoaded;

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

//		LiveGameConfig liveGameConfig = ;
		gameConfigBuilder = getAppData().getLiveGameConfigBuilder();

		if (gameConfigBuilder.getMinRating() == 0) {
			gameConfigBuilder.setMinRating(MIN_RATING_MIN);
		}
		if (gameConfigBuilder.getMaxRating() == 0) {
			gameConfigBuilder.setMaxRating(MAX_RATING_MIN);
		}

		{ // load friends from DB
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getTableForUser(getUsername(), DbScheme.Tables.FRIENDS));

			friendsList = new ArrayList<SelectionItem>();
			friendsList.add(new SelectionItem(null, getString(R.string.random)));
			if (cursor != null && cursor.moveToFirst()) {
				do {
					friendsList.add(new SelectionItem(null, DbDataManager.getString(cursor, DbScheme.V_USERNAME)));
				} while (cursor.moveToNext());
			}

			friendsList.get(0).setChecked(true);
		}

		liveStandardRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_LIVE_STANDARD.ordinal(), getUsername());
		liveBlitzRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_LIVE_BLITZ.ordinal(), getUsername());
		liveLightningRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_LIVE_LIGHTNING.ordinal(), getUsername());

		if (liveStandardRating == 0 || liveBlitzRating == 0 || liveLightningRating == 0 && !statsLoaded) { // if stats were not save
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

			liveStandardRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_LIVE_STANDARD.ordinal(), getUsername());
			liveBlitzRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_LIVE_BLITZ.ordinal(), getUsername());
			liveLightningRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_LIVE_LIGHTNING.ordinal(), getUsername());

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
		SelectionItem opponent = (SelectionItem) parent.getItemAtPosition(position);
		gameConfigBuilder.setOpponentName(opponent.getText());
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

		updateRatingRange();

		gameConfigBuilder.setTimeFromMode(mode);
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
		TextView checkedButton;

//		int minRatingValue = gameConfigBuilder.getMinRating();
//		int maxRatingValue = gameConfigBuilder.getMaxRating();
		int minRatingValue = Integer.parseInt(minRatingBtn.getText().toString());
		int maxRatingValue = Integer.parseInt(maxRatingBtn.getText().toString());

		int minRating;
		int maxRating;
		if (maxRatingBtn.isChecked()) {
			checkedButton = maxRatingBtn;
			minRating = MAX_RATING_MIN;
			maxRating = MAX_RATING_MAX;
		} else {
			checkedButton = minRatingBtn;
			minRating = MIN_RATING_MIN;
			maxRating = MIN_RATING_MAX;
		}

		// get percent progress and convert it to values
		float factor = (maxRating - minRating) / 100; // (maxRating - minRating) / maxSeekProgress
		// progress - percent
		int value = (int) (factor * progress) + minRating; // k * x + b

		if (maxRatingBtn.isChecked()) {
			int maxRatingToSave = value;
			if (maxRatingToSave < minRatingValue) { // if maxRating is lower than minRating
				maxRatingToSave = minRatingValue + RATING_VARIABLE_DIFF;
			}

			gameConfigBuilder.setMaxRating(maxRatingToSave);
			gameConfigBuilder.setMinRating(minRatingValue);
		} else {
			int minRatingToSave = value;
			if (minRatingToSave > maxRatingValue) { // if minRating is greater than maxRating
				minRatingToSave = maxRatingValue - RATING_VARIABLE_DIFF;
			}

			gameConfigBuilder.setMinRating(minRatingToSave);
			gameConfigBuilder.setMaxRating(maxRatingValue);
		}

		checkedButton.setText(String.valueOf(value));

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.liveHeaderView) {
			getActivityFace().toggleRightMenu();
		} else if (view.getId() == R.id.playBtn) {
			getActivityFace().openFragment(LiveGameWaitFragment.createInstance(getLiveGameConfig()));
			getActivityFace().toggleRightMenu();
		} else {
			handleLiveModeClicks(view);
		}
	}

	private void updateRatingRange() {
		int minRating = gameConfigBuilder.getMinRating();
		int maxRating = gameConfigBuilder.getMaxRating();
		if (minRating == 0 || maxRating == 0) {
			if (gameConfigBuilder.getTimeMode() == LiveGameConfig.STANDARD) {
				minRating = liveStandardRating - LiveGameConfig.MIN_RATING_DIFF;
				maxRating = liveStandardRating + LiveGameConfig.MAX_RATING_DIFF;

			} else if (liveGameMode == LiveGameConfig.BLITZ) {
				minRating = liveBlitzRating - LiveGameConfig.MIN_RATING_DIFF;
				maxRating = liveBlitzRating + LiveGameConfig.MAX_RATING_DIFF;

			} else if (liveGameMode == LiveGameConfig.BULLET) {
				minRating = liveLightningRating - LiveGameConfig.MIN_RATING_DIFF;
				maxRating = liveLightningRating + LiveGameConfig.MAX_RATING_DIFF;
			}

			gameConfigBuilder.setMinRating(minRating);
			gameConfigBuilder.setMaxRating(maxRating);
		}

		minRatingBtn.setText(String.valueOf(minRating));
		maxRatingBtn.setText(String.valueOf(maxRating));
	}

	public LiveGameConfig getLiveGameConfig() {
		// set params
		gameConfigBuilder.setRated(ratedGameSwitch.isChecked());
		getAppData().setLiveGameConfigBuilder(gameConfigBuilder);

		return gameConfigBuilder.build();
	}

	private void widgetsInit(View view) {
		view.findViewById(R.id.liveOptionsView).setOnClickListener(this);

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
					ratingBar = (SeekBar) view.findViewById(R.id.ratingBar);
					ratingBar.setOnSeekBarChangeListener(this);
					ratingBar.setProgressDrawable(new RatingProgressDrawable(getContext(), ratingBar));

					minRatingBtn = (RoboRadioButton) view.findViewById(R.id.minRatingBtn);
					minRatingBtn.setOnCheckedChangeListener(ratingSelectionChangeListener);

					maxRatingBtn = (RoboRadioButton) view.findViewById(R.id.maxRatingBtn);
					maxRatingBtn.setOnCheckedChangeListener(ratingSelectionChangeListener);

					// set checked minRating Button
					minRatingBtn.setChecked(true);
				}

				view.findViewById(R.id.playBtn).setOnClickListener(this);
			}

			{// time mode buttons
				int mode = getAppData().getDefaultLiveMode();
				// set texts to buttons
				newGameButtonsArray = getResources().getStringArray(R.array.new_live_game_button_values);
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
