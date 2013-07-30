package com.chess.ui.fragments.live;

import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.RoboRadioButton;
import com.chess.SwitchButton;
import com.chess.backend.statics.StaticData;
import com.chess.model.SelectionItem;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.engine.configs.LiveGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.views.drawables.RatingProgressDrawable;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;

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
public class LiveGameOptionsFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemSelectedListener {

	private static final int LIVE_BASE_ID = 0x00001100;

	private LiveGameConfig.Builder gameConfigBuilder;
	private RoboRadioButton minRatingBtn;
	private RoboRadioButton maxRatingBtn;
	private SwitchButton ratedGameSwitch;

	private List<SelectionItem> firendsList;
	private List<View> liveOptionsGroup;
	private HashMap<Integer, Button> liveButtonsModeMap;
	private boolean liveOptionsVisible;

	private int darkBtnColor;
	private String[] newGameButtonsArray;
	private Button liveTimeSelectBtn;
	private int positionMode;

	public LiveGameOptionsFragment() {
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, CENTER_MODE);
		setArguments(bundle);
	}

	public static LiveGameOptionsFragment createInstance(int mode) {
		LiveGameOptionsFragment fragment = new LiveGameOptionsFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, mode);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			positionMode = getArguments().getInt(MODE);
		} else {
			positionMode = savedInstanceState.getInt(MODE);
		}

		gameConfigBuilder = new LiveGameConfig.Builder();

		// get from live???
//		{ // load friends from DB          // TODO make it async and fill in popup
//			final String[] arguments1 = new String[1];
//			arguments1[0] = getAppData().getUsername();
//			Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.Tables.FRIENDS.ordinal()],
//					DBDataManager.PROJECTION_USERNAME, DBDataManager.SELECTION_USER, arguments1, null);
//
//			firendsList = new ArrayList<SelectionItem>();
//			firendsList.add(new SelectionItem(null, "Random"));
//			if (cursor.moveToFirst()) {
//				do {
//					firendsList.add(new SelectionItem(null, DBDataManager.getString(cursor, DBConstants.V_USERNAME)));
//				} while (cursor.moveToNext());
//			}
//
//			firendsList.get(0).setChecked(true);
//		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_game_option_live_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.liveOptionsView).setOnClickListener(this);

		LayoutInflater inflater = getActivity().getLayoutInflater();
		RelativeLayout liveHomeOptionsFrame = (RelativeLayout) view.findViewById(R.id.liveHomeOptionsFrame);
		inflater.inflate(R.layout.new_right_live_options_view, liveHomeOptionsFrame, true);
		View liveHeaderView = view.findViewById(R.id.liveHeaderView);
		ButtonDrawableBuilder.setBackgroundToView(liveHeaderView, R.style.ListItem_Header_Dark);

//		RoboSpinner opponentSpinner = (RoboSpinner) view.findViewById(R.id.opponentSpinner);
//		Resources resources = getResources();
//
//		OpponentsAdapter selectionAdapter = new OpponentsAdapter(getActivity(), firendsList);
//		opponentSpinner.setAdapter(selectionAdapter);
//		opponentSpinner.setOnItemSelectedListener(this);
//
//		opponentSpinner.setSelection(0);

		{// Live Games setup
//			NewGameDefaultView.ViewConfig dailyConfig = new NewGameDefaultView.ViewConfig();
//			dailyConfig.setBaseId(LIVE_BASE_ID);
//			dailyConfig.setHeaderIcon(R.string.ic_daily_game);
//			dailyConfig.setHeaderText(R.string.new_daily_chess);
//			dailyConfig.setTitleText(R.string.new_per_turn);
//			int defaultDailyMode = getAppData().getDefaultDailyMode();
//			dailyConfig.setLeftButtonText(getString(R.string.days_arg, defaultDailyMode));
//			dailyConfig.setRightButtonTextId(R.string.random);
		}

		liveTimeSelectBtn = (Button) view.findViewById(R.id.liveTimeSelectBtn);
		liveTimeSelectBtn.setOnClickListener(this);
		view.findViewById(R.id.livePlayBtn).setOnClickListener(this);

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

			int mode = getAppData().getDefaultLiveMode();
			darkBtnColor = getResources().getColor(R.color.text_controls_icons_white);
			// set texts to buttons
			newGameButtonsArray = getResources().getStringArray(R.array.new_live_game_button_values);
			for (Map.Entry<Integer, Button> buttonEntry : liveButtonsModeMap.entrySet()) {
				int key = buttonEntry.getKey();
				buttonEntry.getValue().setText(getLiveModeButtonLabel(newGameButtonsArray[key]));
				buttonEntry.getValue().setOnClickListener(this);

				if (getArguments().getInt(MODE) == CENTER_MODE) {
					buttonEntry.getValue().setTextColor(darkBtnColor);
				}

				if (key == mode) {
					setDefaultQuickLiveMode(buttonEntry.getValue(), buttonEntry.getKey());
				}
			}
		}

		liveTimeSelectBtn = (Button) view.findViewById(R.id.liveTimeSelectBtn);
		liveTimeSelectBtn.setOnClickListener(this);

		{// options setup


			// rated games switch
			ratedGameSwitch = (SwitchButton) view.findViewById(R.id.ratedGameSwitch);

			{// Rating part
				int minRatingDefault = 1500; // TODO adjust properly
				int maxRatingDefault = 1700;

				minRatingBtn = (RoboRadioButton) view.findViewById(R.id.minRatingBtn);
				minRatingBtn.setOnCheckedChangeListener(ratingSelectionChangeListener);
				minRatingBtn.setText(String.valueOf(minRatingDefault));

				maxRatingBtn = (RoboRadioButton) view.findViewById(R.id.maxRatingBtn);
				maxRatingBtn.setOnCheckedChangeListener(ratingSelectionChangeListener);
				maxRatingBtn.setText(String.valueOf(maxRatingDefault));

				// set checked minRating Button
				minRatingBtn.setChecked(true);

				SeekBar ratingBar = (SeekBar) view.findViewById(R.id.ratingBar);
				ratingBar.setOnSeekBarChangeListener(ratingBarChangeListener);
				// TODO adjust progress drawable
				ratingBar.setProgressDrawable(new RatingProgressDrawable(getContext(), ratingBar));
			}
			view.findViewById(R.id.playBtn).setOnClickListener(this);
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(MODE, positionMode);
	}

	private String getLiveModeButtonLabel(String label) {
		if (label.contains(StaticData.SYMBOL_SLASH)) { // "5 | 2"
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
		liveTimeSelectBtn.setText(getLiveModeButtonLabel(newGameButtonsArray[mode]));
		gameConfigBuilder.setTimeFromLabel(newGameButtonsArray[mode]);
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

	private SeekBar.OnSeekBarChangeListener ratingBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			TextView checkedButton;
			int minRating;
			int maxRating;
			if (maxRatingBtn.isChecked()) {
				checkedButton = maxRatingBtn;
				minRating = 1000; // TODO set from resources
				maxRating = 2400;
			} else {
				checkedButton = minRatingBtn;
				minRating = 1000;
				maxRating = 2000;
			}
			// get percent progress and convert it to values

			int diff = minRating;
			float factor = (maxRating - minRating) / 100; // (maxRating - minRating) / maxSeekProgress
			// progress - percent
			int value = (int) (factor * progress) + diff; // k * x + b

			checkedButton.setText(String.valueOf(value));

			if (maxRatingBtn.isChecked()) {
				gameConfigBuilder.setMaxRating(value);
				gameConfigBuilder.setMinRating(Integer.parseInt(minRatingBtn.getText().toString()));
			} else {
				gameConfigBuilder.setMinRating(value);
				gameConfigBuilder.setMaxRating(Integer.parseInt(maxRatingBtn.getText().toString()));
			}

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
	};

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.liveTimeSelectBtn) {
			toggleLiveOptionsView();
		} else if (view.getId() == R.id.myColorBtn) {
		} else if (view.getId() == R.id.minRatingBtn) {
		} else if (view.getId() == R.id.maxRatingBtn) {
		} else if (view.getId() == R.id.liveOptionsView) {
			getActivityFace().toggleRightMenu();
		} else if (view.getId() == R.id.playBtn) {
			getActivityFace().openFragment(new LiveGameWaitFragment());
			if (getArguments().getInt(MODE) == RIGHT_MENU_MODE) {
				getActivityFace().toggleRightMenu();
			}
		} else {
			handleLiveModeClicks(view);
		}
	}



	public LiveGameConfig getLiveGameConfig() {
		// set params
		gameConfigBuilder.setRated(ratedGameSwitch.isChecked());

		return gameConfigBuilder.build();
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
