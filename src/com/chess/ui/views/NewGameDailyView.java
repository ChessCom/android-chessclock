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
import android.widget.*;
import com.chess.*;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.engine.NewDailyGameConfig;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.views.drawables.RatingProgressDrawable;

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
	private RoboRadioButton minRatingBtn;
	private RoboRadioButton maxRatingBtn;
	private SwitchButton ratedGameSwitch;


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
	protected void addButtons(ViewConfig viewConfig, RelativeLayout compactRelLay) {
		// Left Button - "3 days Mode"
		leftButton = new RoboButton(getContext(), null, R.attr.greyButtonSmallSolid);
		RelativeLayout.LayoutParams leftBtnParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		leftBtnParams.addRule(RelativeLayout.BELOW, BASE_ID + TITLE_ID);

		if (viewConfig.getLeftButtonTextId() == 0) {
			leftButton.setText(viewConfig.getLeftButtonText());
		} else {
			leftButton.setText(viewConfig.getLeftButtonTextId());
		}
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
		vsText.setTextColor(getContext().getResources().getColor(R.color.new_normal_grey));
		vsText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TOP_TEXT_SIZE);
		vsText.setId(BASE_ID + VS_ID);

		compactRelLay.addView(vsText, vsTxtParams);

		// Right Button - "Random"
		rightButton = new EditButton(getContext()); // don't apply style programmatically as it will lead to unable to appear keyboard on touch
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
		rightButton.setText(viewConfig.getRightButtonTextId());
		rightButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, BUTTON_TEXT_SIZE);
		rightButton.setFont(RoboTextView.BOLD_FONT);

		compactRelLay.addView(rightButton, rightButtonParams);

	}

	@Override
	protected void addCustomView(ViewConfig viewConfig, RelativeLayout optionsAndPlayView) {
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

	@Override
	public void addOptionsView() {
		optionsView = LayoutInflater.from(getContext()).inflate(R.layout.new_game_option_daily_view, null, false);
		optionsView.setVisibility(GONE);

		{// Mode adapter init
			int[] newGameButtonsArray = getResources().getIntArray(R.array.days_per_move_array);
			List<NewDailyGameButtonItem> newGameButtonItems = new ArrayList<NewDailyGameButtonItem>();
			for (int label : newGameButtonsArray) {
				newGameButtonItems.add(NewDailyGameButtonItem.createNewButtonFromLabel(label, getContext()));
			}

			GridView gridView = (GridView) optionsView.findViewById(R.id.dailyGamesModeGrid);
			newDailyGamesButtonsAdapter = new NewDailyGamesButtonsAdapter(this, newGameButtonItems);
			gridView.setAdapter(newDailyGamesButtonsAdapter);
			newDailyGamesButtonsAdapter.checkButton(0);
			// set value to builder
			gameConfigBuilder.setDaysPerMove(newDailyGamesButtonsAdapter.getItem(0).days);
		}


		EditButtonSpinner opponentEditBtn = (EditButtonSpinner) optionsView.findViewById(R.id.opponentEditBtn);
		opponentEditBtn.addOnClickListener(this);

		EditButtonSpinner myColorEditBtn = (EditButtonSpinner) optionsView.findViewById(R.id.myColorEditBtn);
		myColorEditBtn.addOnClickListener(this);

		// rated games switch
		ratedGameSwitch = (SwitchButton) optionsView.findViewById(R.id.ratedGameSwitch);

		{// Rating part
			int minRatingDefault = 1500; // TODO adjust properly
			int maxRatingDefault = 1700;

			minRatingBtn = (RoboRadioButton) optionsView.findViewById(R.id.minRatingBtn);
			minRatingBtn.setOnCheckedChangeListener(ratingSelectionChangeListener);
			minRatingBtn.setText(String.valueOf(minRatingDefault));

			maxRatingBtn = (RoboRadioButton) optionsView.findViewById(R.id.maxRatingBtn);
			maxRatingBtn.setOnCheckedChangeListener(ratingSelectionChangeListener);
			maxRatingBtn.setText(String.valueOf(maxRatingDefault));

			// set checked minRating Button
			minRatingBtn.setChecked(true);

			SeekBar ratingBar = (SeekBar) optionsView.findViewById(R.id.ratingBar);
			ratingBar.setOnSeekBarChangeListener(ratingBarChangeListener);
			// TODO adjust progress drawable
			ratingBar.setProgressDrawable(new RatingProgressDrawable(getContext(), ratingBar));
		}

		optionsView.findViewById(R.id.playBtn).setOnClickListener(this);

		addView(optionsView);

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

	@Override
	public Context getMeContext() {
		return getContext();
	}
}
