package com.chess.ui.fragments.daily;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.*;
import com.chess.backend.LoadHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.DailySeekItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbConstants;
import com.chess.db.DbDataManager;
import com.chess.model.SelectionItem;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.engine.configs.DailyGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.views.NewGameDailyView;
import com.chess.ui.views.NewGameDefaultView;
import com.chess.ui.views.drawables.RatingProgressDrawable;
import com.chess.ui.views.drawables.smart_button.ButtonGlassyDrawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.04.13
 * Time: 14:26
 */
public class DailyGamesOptionsFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemSelectedListener {

	private static final String ERROR_TAG = "send request failed popup";

	private static final int DAILY_BASE_ID = 0x00001000;

	private final static int DAILY_RIGHT_BUTTON_ID = DAILY_BASE_ID + NewGameDailyView.RIGHT_BUTTON_ID;
	private final static int DAILY_LEFT_BUTTON_ID = DAILY_BASE_ID + NewGameDefaultView.LEFT_BUTTON_ID;
	private final static int DAILY_PLAY_BUTTON_ID = DAILY_BASE_ID + NewGameDefaultView.PLAY_BUTTON_ID;

	private DailyGamesButtonsAdapter dailyGamesButtonsAdapter;
	private DailyGameConfig.Builder gameConfigBuilder;
	private RoboRadioButton minRatingBtn;
	private RoboRadioButton maxRatingBtn;
	private SwitchButton ratedGameSwitch;

	private CreateChallengeUpdateListener createChallengeUpdateListener;
	private List<SelectionItem> firendsList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gameConfigBuilder = new DailyGameConfig.Builder();

		{ // load friends from DB          // TODO make it async and fill in popup
			final String[] arguments1 = new String[1];
			arguments1[0] = getAppData().getUsername();
			Cursor cursor = getContentResolver().query(DbConstants.uriArray[DbConstants.Tables.FRIENDS.ordinal()],
					DbDataManager.PROJECTION_USERNAME, DbDataManager.SELECTION_USER, arguments1, null);

			firendsList = new ArrayList<SelectionItem>();
			firendsList.add(new SelectionItem(null, getString(R.string.random)));
			if (cursor.moveToFirst()) {
				do{
					firendsList.add(new SelectionItem(null, DbDataManager.getString(cursor, DbConstants.V_USERNAME)));
				}while (cursor.moveToNext());
			}

			firendsList.get(0).setChecked(true);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_game_option_daily_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.dailyHeaderView).setOnClickListener(this);
//		opponentNameTxt = (TextView) view.findViewById(R.id.opponentNameTxt);

		RoboSpinner opponentSpinner = (RoboSpinner) view.findViewById(R.id.opponentSpinner);
		Resources resources = getResources();

		OpponentsAdapter selectionAdapter = new OpponentsAdapter(getActivity(), firendsList);
		opponentSpinner.setAdapter(selectionAdapter);
		opponentSpinner.setOnItemSelectedListener(this);

		opponentSpinner.setSelection(0);

		{// Daily Games setup
//			NewGameDefaultView.ViewConfig dailyConfig = new NewGameDefaultView.ViewConfig();
//			dailyConfig.setBaseId(DAILY_BASE_ID);
//			dailyConfig.setHeaderIcon(R.string.ic_daily_game);
//			dailyConfig.setHeaderText(R.string.new_daily_chess);
//			dailyConfig.setTitleText(R.string.new_per_turn);
//			int defaultDailyMode = getAppData().getDefaultDailyMode();
//			dailyConfig.setLeftButtonText(getString(R.string.days_arg, defaultDailyMode));
//			dailyConfig.setRightButtonTextId(R.string.random);
		}

		{// options setup
			{// Mode adapter init
				int[] newGameButtonsArray = resources.getIntArray(R.array.days_per_move_array);
				List<DailyGameButtonItem> newGameButtonItems = new ArrayList<DailyGameButtonItem>();
				for (int label : newGameButtonsArray) {
					newGameButtonItems.add(new DailyGameButtonItem(label, getContext()));
				}
				int dailyMode = getAppData().getDefaultDailyMode();
				newGameButtonItems.get(dailyMode).checked = true;

				GridView gridView = (GridView) view.findViewById(R.id.dailyGamesModeGrid);
				dailyGamesButtonsAdapter = new DailyGamesButtonsAdapter(this, newGameButtonItems);
				gridView.setAdapter(dailyGamesButtonsAdapter);
			}

//			EditButtonSpinner opponentEditBtn = (EditButtonSpinner) view.findViewById(R.id.opponentEditBtn);
//			opponentEditBtn.addOnClickListener(this);

//			EditButtonSpinner myColorEditBtn = (EditButtonSpinner) view.findViewById(R.id.myColorEditBtn);
//			myColorEditBtn.addOnClickListener(this);

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
		createChallengeUpdateListener = new CreateChallengeUpdateListener();
	}

	@Override
	public void onResume() {
		super.onResume();

		updateDailyMode(getAppData().getDefaultDailyMode());
	}

	private void createDailyChallenge() {
		// create challenge using formed configuration
		DailyGameConfig dailyGameConfig = getDailyGameConfig();

		int color = dailyGameConfig.getUserColor();
		int days = dailyGameConfig.getDaysPerMove();
		int gameType = dailyGameConfig.getGameType();
		int isRated = dailyGameConfig.isRated() ? 1 : 0;
		String opponentName = dailyGameConfig.getOpponentName();

		LoadItem loadItem = LoadHelper.postGameSeek(getUserToken(), days, color, isRated, gameType, opponentName);
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
		}, 250);
	}


	private class CreateChallengeUpdateListener extends ChessUpdateListener<DailySeekItem> {

		public CreateChallengeUpdateListener() {
			super(DailySeekItem.class);
		}

		@Override
		public void updateData(DailySeekItem returnedObj) {
			showSinglePopupDialog(R.string.congratulations, R.string.daily_game_created);
		}

		@Override
		public void errorHandle(String resultMessage) {
			showPopupDialog(getString(R.string.error), resultMessage, ERROR_TAG);
		}
	}

	private CompoundButton.OnCheckedChangeListener ratingSelectionChangeListener = new CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			if (buttonView.getId() == R.id.minRatingBtn && isChecked) {
				minRatingBtn.setChecked(true);
				maxRatingBtn.setChecked(false);

			} else if (buttonView.getId() == R.id.maxRatingBtn && isChecked){
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

			checkedButton.setText(String.valueOf(value ));

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


	private class DailyGameButtonItem {
		public boolean checked;
		public int days;
		public String label;

		DailyGameButtonItem(int label, Context context){
			this.days = label;
			this.label = context.getString(R.string.days_arg, label);
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == DailyGamesButtonsAdapter.BUTTON_ID) {
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			updateDailyMode(position);
			getAppData().setDefaultDailyMode(position);
		} else if (view.getId() == R.id.myColorBtn){
		} else if (view.getId() == R.id.minRatingBtn){
		} else if (view.getId() == R.id.maxRatingBtn){
		} else if (view.getId() == R.id.dailyHeaderView){
			getActivityFace().toggleRightMenu();
		} else if (view.getId() == R.id.playBtn){
			createDailyChallenge();
		}
	}

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

			((RoboButton)convertView).setText(item.label);

			Drawable background = convertView.getBackground();
			if (item.checked) {
				((RoboButton)convertView).setTextColor(Color.WHITE);
				background.mutate().setState(ButtonGlassyDrawable.STATE_SELECTED);
			} else {
				((RoboButton)convertView).setTextColor(textColor);
				background.mutate().setState(ButtonGlassyDrawable.STATE_ENABLED);
			}
		}

		public void checkButton(int checkedPosition){
			for (DailyGameButtonItem item : itemsList) {
				item.checked = false;
			}

			itemsList.get(checkedPosition).checked = true;
			notifyDataSetChanged();
		}
	}

	public DailyGameConfig getDailyGameConfig(){
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
