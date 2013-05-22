package com.chess.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.SeekBar;
import android.widget.TextView;
import com.chess.*;
import com.chess.ui.adapters.ItemsAdapter;
import com.chess.ui.engine.configs.NewLiveGameConfig;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.views.drawables.RatingProgressDrawable;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;

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
	private RoboRadioButton minRatingBtn;
	private RoboRadioButton maxRatingBtn;
	private SwitchButton ratedGameSwitch;

	public NewGameLiveView(Context context) {
		super(context);
	}

	public NewGameLiveView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

//	public NewGameLiveView(Context context, AttributeSet attrs, int defStyle) {
//		super(context, attrs, defStyle);
//	}

	@Override
	public void onCreate() {
		super.onCreate();
		gameConfigBuilder = new NewLiveGameConfig.Builder();

	}

	@Override
	public void toggleOptions() {
		super.toggleOptions();

		int expandVisibility = optionsVisible ? VISIBLE : GONE;

		optionsView.setVisibility(expandVisibility);

		if (optionsVisible) {
			ButtonDrawableBuilder.setBackgroundToView(compactRelLay, R.style.ListItem);
		} else {
			ButtonDrawableBuilder.setBackgroundToView(compactRelLay, R.style.ListItem_Header);
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
				buttonItem.label = context.getString(R.string.min_arg, label);

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
		// set params
		gameConfigBuilder.setRated(ratedGameSwitch.isChecked());

		return gameConfigBuilder.build();
	}

	public static class ViewLiveConfig extends ViewConfig {
		@Override
		public String getLeftButtonText() {
			return leftButtonText;
		}
	}

}
