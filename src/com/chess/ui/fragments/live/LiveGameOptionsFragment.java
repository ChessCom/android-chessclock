package com.chess.ui.fragments.live;

import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.statics.AppConstants;
import com.chess.statics.Symbol;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.friends.FriendsRightFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.views.drawables.RatingProgressDrawable;
import com.chess.utilities.AppUtils;
import com.chess.widgets.RoboRadioButton;
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
		SeekBar.OnSeekBarChangeListener {

	private static final int MIN_RATING_MIN = -1000;
	private static final int MIN_RATING_MAX = 0;
	private static final int MAX_RATING_MIN = 0;
	private static final int MAX_RATING_MAX = 1000;
	private static final long SEEK_BAR_HIDE_DELAY = 7 * 1000;
	public static final int MAX_PROGRESS = 20;

	private LiveGameConfig.Builder gameConfigBuilder;
	private RoboRadioButton minRatingBtn;
	private RoboRadioButton maxRatingBtn;
	private SwitchButton ratedGameSwitch;

	private List<View> liveOptionsGroup;
	private HashMap<Integer, Button> liveButtonsModeMap;
	private boolean liveOptionsVisible;

	private int positionMode;
	private String opponentName;
	private int standardRating;
	private int blitzRating;
	private int lightningRating;

	private SeekBar ratingSeekBar;
	private ViewGroup ratingView;
	private TextView currentRatingTxt;
	private TextView opponentNameTxt;
	private ViewGroup ratedGameView;

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

		standardRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_LIVE_STANDARD.ordinal(), getUsername());
		blitzRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_LIVE_BLITZ.ordinal(), getUsername());
		lightningRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_LIVE_LIGHTNING.ordinal(), getUsername());

		standardRating = standardRating == 0 ? AppConstants.DEFAULT_PLAYER_RATING : standardRating;
		blitzRating = blitzRating == 0 ? AppConstants.DEFAULT_PLAYER_RATING : blitzRating;
		lightningRating = lightningRating == 0 ? AppConstants.DEFAULT_PLAYER_RATING : lightningRating;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.game_option_live_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		updateRatingRange();
	}

	@Override
	public void onPause() {
		super.onPause();

		// save config
		getAppData().setLiveGameConfigBuilder(gameConfigBuilder);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(MODE, positionMode);
	}

	@Override
	public Context getMeContext() {
		return getActivity();
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
			gameConfigBuilder.setMinRatingOffset(value);
		} else {
			checkedButton = maxRatingBtn;

			factor = (MAX_RATING_MAX - MAX_RATING_MIN) / MAX_PROGRESS; // (maxRatingDiff - minRatingDiff) / maxSeekProgress
			value = (int) (factor * progress);
			if (value != 0) {
				symbol = Symbol.PLUS;
			}

			// update max rating
			gameConfigBuilder.setMaxRatingOffset(value);
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

			// set progress bar to exact position
			int value = gameConfigBuilder.getMinRatingOffset();
			int factor = (MIN_RATING_MAX - MIN_RATING_MIN) / MAX_PROGRESS;
			int progress = (value - 1000) / (-factor);

			ratingSeekBar.setProgress(progress);

			handler.postDelayed(hideSeekBarRunnable, SEEK_BAR_HIDE_DELAY);
		} else if (view.getId() == R.id.maxRatingBtn) {
			ratingSeekBar.setVisibility(View.VISIBLE);

			// set progress bar to exact position
			int value = gameConfigBuilder.getMaxRatingOffset();
			int factor = (MIN_RATING_MAX - MIN_RATING_MIN) / MAX_PROGRESS;
			int progress = value / factor;

			ratingSeekBar.setProgress(progress);
			handler.postDelayed(hideSeekBarRunnable, SEEK_BAR_HIDE_DELAY);
		} else if (view.getId() == R.id.opponentView) {
			getActivityFace().changeRightFragment(FriendsRightFragment.createInstance(FriendsRightFragment.LIVE_OPPONENT_REQUEST));
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
		} else if (gameConfigBuilder.getTimeMode() == LiveGameConfig.BULLET) {
			currentRatingTxt.setText(String.valueOf(lightningRating));
		}
	}

	public LiveGameConfig getLiveGameConfig() {
		// set params
		gameConfigBuilder.setRated(ratedGameSwitch.isChecked());

		String minRatingBtnText = minRatingBtn.getText().toString();
		String maxRatingBtnText = maxRatingBtn.getText().toString();

		Integer minRatingValue = Integer.valueOf(minRatingBtnText.replace(Symbol.PLUS, Symbol.EMPTY).replace(Symbol.MINUS, Symbol.EMPTY));
		Integer maxRatingValue = Integer.valueOf(maxRatingBtnText.replace(Symbol.PLUS, Symbol.EMPTY).replace(Symbol.MINUS, Symbol.EMPTY));

		int rating = AppConstants.DEFAULT_PLAYER_RATING;
		if (gameConfigBuilder.getTimeMode() == LiveGameConfig.STANDARD) {
			rating = standardRating;
		} else if (gameConfigBuilder.getTimeMode() == LiveGameConfig.BLITZ) {
			rating = blitzRating;
		} else if (gameConfigBuilder.getTimeMode() == LiveGameConfig.BULLET) {
			rating = lightningRating;
		}

		gameConfigBuilder.setRating(rating);
		gameConfigBuilder.setMinRatingOffset(minRatingValue);
		gameConfigBuilder.setMaxRatingOffset(maxRatingValue);

		getAppData().setLiveGameConfigBuilder(gameConfigBuilder);

		return gameConfigBuilder.build(true);
	}

	private void widgetsInit(View view) {
		view.findViewById(R.id.liveOptionsView).setOnClickListener(this);


		LayoutInflater inflater = getActivity().getLayoutInflater();
		RelativeLayout liveHomeOptionsFrame = (RelativeLayout) view.findViewById(R.id.liveHomeOptionsFrame);
		inflater.inflate(R.layout.right_live_options_view, liveHomeOptionsFrame, true);

		if (getArguments().getInt(MODE) == CENTER_MODE) { // we use white background and dark titles for centered mode
			View liveHeaderView = view.findViewById(R.id.liveHeaderView);
			liveHeaderView.setVisibility(View.VISIBLE);
			liveHeaderView.setOnClickListener(this);
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
			liveButtonsModeMap.put(0, (Button) view.findViewById(R.id.standard1SelectBtn));  // 30
			liveButtonsModeMap.put(4, (Button) view.findViewById(R.id.standard2SelectBtn));  // 15 | 10
			liveButtonsModeMap.put(1, (Button) view.findViewById(R.id.blitz1SelectBtn));     // 10
			liveButtonsModeMap.put(2, (Button) view.findViewById(R.id.blitz2SelectBtn));     // 5 | 2
			liveButtonsModeMap.put(5, (Button) view.findViewById(R.id.blitz3SelectBtn));     // 5
			liveButtonsModeMap.put(6, (Button) view.findViewById(R.id.blitz4SelectBtn));     // 3
			liveButtonsModeMap.put(3, (Button) view.findViewById(R.id.bullet1SelectBtn));    // 2 | 1
			liveButtonsModeMap.put(7, (Button) view.findViewById(R.id.bullet2SelectBtn));    // 1

			{// options setup
				// rated games switch
				ratedGameSwitch = (SwitchButton) view.findViewById(R.id.ratedGameSwitch);
				ratedGameView = (ViewGroup) view.findViewById(R.id.ratedGameView);
				ratedGameView.setOnClickListener(this);

				{// Rating part
					ratingView = (ViewGroup) view.findViewById(R.id.ratingView);

					int minRatingDefault;
					int maxRatingDefault;

					int minRatingOffset = gameConfigBuilder.getMinRatingOffset();
					int maxRatingOffset = gameConfigBuilder.getMaxRatingOffset();

					if (minRatingOffset == 0) { // first time
						minRatingOffset = LiveGameConfig.RATING_STEP;
						gameConfigBuilder.setMinRatingOffset(minRatingOffset);
					}
					if (maxRatingOffset == 0) { // first time
						maxRatingOffset = LiveGameConfig.RATING_STEP;
						gameConfigBuilder.setMaxRatingOffset(maxRatingOffset);
					}

					minRatingDefault = minRatingOffset;
					maxRatingDefault = maxRatingOffset;

					String minRatingStr;
					String maxRatingStr;
					minRatingStr = Symbol.MINUS + String.valueOf(minRatingDefault);
					maxRatingStr = Symbol.PLUS + String.valueOf(Math.abs(maxRatingDefault));

					if (JELLY_BEAN_PLUS_API) {// make nice animation for view state changing
						LayoutTransition layoutTransition = ratingView.getLayoutTransition();
						layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
					}

					if (JELLY_BEAN_PLUS_API) {
						LayoutTransition layoutTransition = ratedGameView.getLayoutTransition();
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

					// set checked minRatingOffset Button
					minRatingBtn.setChecked(true);
				}

				{ // Opponent View
					view.findViewById(R.id.opponentView).setOnClickListener(this);
					opponentNameTxt = (TextView) view.findViewById(R.id.opponentNameTxt);
					if (!TextUtils.isEmpty(opponentName)) { // todo: why opponentName = "" here sometimes (not null but empty string)?
						opponentNameTxt.setText(opponentName);
						gameConfigBuilder.setOpponentName(opponentName);
						ratingView.setVisibility(View.GONE);
					} else {
						gameConfigBuilder.setOpponentName(null);
						ratingView.setVisibility(View.VISIBLE);
					}
				}

				view.findViewById(R.id.playBtn).setOnClickListener(this);
			}

			{// time mode buttons
				int mode = getAppData().getDefaultLiveMode();
				gameConfigBuilder.setTimeFromMode(mode);

				currentRatingTxt.setText(String.valueOf(standardRating));
				if (gameConfigBuilder.getTimeMode() == LiveGameConfig.BLITZ) {
					currentRatingTxt.setText(String.valueOf(blitzRating));
				} else if (gameConfigBuilder.getTimeMode() == LiveGameConfig.BULLET) {
					currentRatingTxt.setText(String.valueOf(lightningRating));
				}

				// set texts to buttons
				String[] newGameButtonsArray = getResources().getStringArray(R.array.new_live_game_button_values);
				for (Map.Entry<Integer, Button> buttonEntry : liveButtonsModeMap.entrySet()) {
					int key = buttonEntry.getKey();
					buttonEntry.getValue().setText(AppUtils.getLiveModeButtonLabel(newGameButtonsArray[key], getContext()));
					buttonEntry.getValue().setOnClickListener(this);

					if (key == mode) {
						setDefaultQuickLiveMode(buttonEntry.getValue(), buttonEntry.getKey());
					}
				}
			}
		}

		liveOptionsVisible = false; // should be false to make them visible when changing fragment above

		toggleLiveOptionsView();
	}
}
