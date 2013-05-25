package com.chess.ui.fragments.daily;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.chess.*;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.DailySeekItem;
import com.chess.backend.statics.AppData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.engine.configs.NewDailyGameConfig;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.views.NewGameDailyView;
import com.chess.ui.views.NewGameDefaultView;
import com.chess.ui.views.drawables.RatingProgressDrawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.04.13
 * Time: 14:26
 */
public class DailyGamesOptionsFragment extends CommonLogicFragment implements ItemClickListenerFace {

	private static final String ERROR_TAG = "send request failed popup";

	private static final int DAILY_BASE_ID = 0x00001000;

	private final static int DAILY_RIGHT_BUTTON_ID = DAILY_BASE_ID + NewGameDailyView.RIGHT_BUTTON_ID;
	private final static int DAILY_LEFT_BUTTON_ID = DAILY_BASE_ID + NewGameDefaultView.LEFT_BUTTON_ID;
	private final static int DAILY_PLAY_BUTTON_ID = DAILY_BASE_ID + NewGameDefaultView.PLAY_BUTTON_ID;

	private NewDailyGamesButtonsAdapter newDailyGamesButtonsAdapter;
	private NewDailyGameConfig.Builder gameConfigBuilder;
	private RoboRadioButton minRatingBtn;
	private RoboRadioButton maxRatingBtn;
	private SwitchButton ratedGameSwitch;

//	private NewGameDailyView dailyGamesSetupView;
	private CreateChallengeUpdateListener createChallengeUpdateListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		gameConfigBuilder = new NewDailyGameConfig.Builder();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_game_option_daily_view, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.dailyHeaderView).setOnClickListener(this);

		{// Daily Games setup
			NewGameDefaultView.ViewConfig dailyConfig = new NewGameDefaultView.ViewConfig();
			dailyConfig.setBaseId(DAILY_BASE_ID);
			dailyConfig.setHeaderIcon(R.string.ic_daily_game);
			dailyConfig.setHeaderText(R.string.new_daily_chess);
			dailyConfig.setTitleText(R.string.new_per_turn);
			// set default value
			{// TODO remove after debug - we set here a test value
				AppData.setDefaultDailyMode(getActivity(), 3);
			}
			int defaultDailyMode = AppData.getDefaultDailyMode(getActivity());
			dailyConfig.setLeftButtonText(getString(R.string.days_arg, defaultDailyMode));
			dailyConfig.setRightButtonTextId(R.string.random);
		}

		{// options setup
			{// Mode adapter init
				int[] newGameButtonsArray = getResources().getIntArray(R.array.days_per_move_array);
				List<NewDailyGameButtonItem> newGameButtonItems = new ArrayList<NewDailyGameButtonItem>();
				for (int label : newGameButtonsArray) {
					newGameButtonItems.add(NewDailyGameButtonItem.createNewButtonFromLabel(label, getContext()));
				}

				GridView gridView = (GridView) view.findViewById(R.id.dailyGamesModeGrid);
				newDailyGamesButtonsAdapter = new NewDailyGamesButtonsAdapter(this, newGameButtonItems);
				gridView.setAdapter(newDailyGamesButtonsAdapter);
				newDailyGamesButtonsAdapter.checkButton(0);
				// set value to builder
				gameConfigBuilder.setDaysPerMove(newDailyGamesButtonsAdapter.getItem(0).days);
			}

			EditButtonSpinner opponentEditBtn = (EditButtonSpinner) view.findViewById(R.id.opponentEditBtn);
			opponentEditBtn.addOnClickListener(this);

			EditButtonSpinner myColorEditBtn = (EditButtonSpinner) view.findViewById(R.id.myColorEditBtn);
			myColorEditBtn.addOnClickListener(this);

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

	private void createDailyChallenge() {
		// create challenge using formed configuration
		NewDailyGameConfig newDailyGameConfig = getNewDailyGameConfig();

		int color = newDailyGameConfig.getUserColor();
		int days = newDailyGameConfig.getDaysPerMove();
		int gameType = newDailyGameConfig.getGameType();
		String isRated = newDailyGameConfig.isRated() ? RestHelper.V_TRUE : RestHelper.V_FALSE;
		String opponentName = newDailyGameConfig.getOpponentName();

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_SEEKS);
		loadItem.setRequestMethod(RestHelper.POST);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));
		loadItem.addRequestParams(RestHelper.P_DAYS_PER_MOVE, days);
		loadItem.addRequestParams(RestHelper.P_USER_SIDE, color);
		loadItem.addRequestParams(RestHelper.P_IS_RATED, isRated);
		loadItem.addRequestParams(RestHelper.P_GAME_TYPE, gameType);
		if (!TextUtils.isEmpty(opponentName)) {
			loadItem.addRequestParams(RestHelper.P_OPPONENT, opponentName);
		}

		new RequestJsonTask<DailySeekItem>(createChallengeUpdateListener).executeTask(loadItem);
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	private class CreateChallengeUpdateListener extends ChessUpdateListener<DailySeekItem> {

		public CreateChallengeUpdateListener() {
			super(DailySeekItem.class);
		}

		@Override
		public void updateData(DailySeekItem returnedObj) {
			showSinglePopupDialog(R.string.congratulations, R.string.online_game_created);
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

			newDailyGamesButtonsAdapter.checkButton(position);
			// set value to builder
			gameConfigBuilder.setDaysPerMove(newDailyGamesButtonsAdapter.getItem(position).days);
		} else if (view.getId() == R.id.opponentEditBtn){
			Log.d("TEST", "opponentEditBtn clicked");
		} else if (view.getId() == R.id.myColorEditBtn){
		} else if (view.getId() == R.id.minRatingBtn){
		} else if (view.getId() == R.id.maxRatingBtn){
		} else if (view.getId() == R.id.dailyHeaderView){
			getActivityFace().toggleRightMenu();
		} else if (view.getId() == R.id.playBtn){
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
			RoboToggleButton view = new RoboToggleButton(getContext(), null, R.attr.greyButtonSmallSolid);
			view.setId(BUTTON_ID);
			view.setOnClickListener(clickListenerFace);
			return view;
		}

		@Override
		protected void bindView(NewDailyGameButtonItem item, int pos, View convertView) {
			((RoboToggleButton)convertView).setText(item.label);
			((RoboToggleButton)convertView).setChecked(item.checked);
			convertView.setTag(itemListId, pos);
		}

		public void checkButton(int checkedPosition){
			for (NewDailyGameButtonItem item : itemsList) {
				item.checked = false;
			}

			itemsList.get(checkedPosition).checked = true;
			notifyDataSetChanged();
		}
	}

	public NewDailyGameConfig getNewDailyGameConfig(){
		// set params
		gameConfigBuilder.setRated(ratedGameSwitch.isChecked());

		return gameConfigBuilder.build();
	}
}
