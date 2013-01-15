package com.chess.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.RelativeLayout;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.RoboTextView;
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
	private RoboButton rightButton;

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
		rightButton = new RoboButton(getContext(), null, R.attr.greyButtonSmallSolid);
		RelativeLayout.LayoutParams rightButtonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		rightButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		rightButtonParams.addRule(RelativeLayout.RIGHT_OF, BASE_ID + VS_ID);
		rightButtonParams.addRule(RelativeLayout.BELOW, BASE_ID + TITLE_ID);

		rightButton.setId(BASE_ID + RIGHT_BUTTON_ID);
		rightButton.setText(configItem.getRightButtonText());
		rightButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, BUTTON_TEXT_SIZE);

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

		int[] newGameButtonsArray = getResources().getIntArray(R.array.days_per_move_array);
		List<NewDailyGameButtonItem> newGameButtonItems = new ArrayList<NewDailyGameButtonItem>();
		for (int label : newGameButtonsArray) {
			newGameButtonItems.add(NewDailyGameButtonItem.createNewButtonFromLabel(label, getContext()));
		}

		GridView gridView = (GridView) optionsView.findViewById(R.id.dailyGamesModeGrid);
		gridView.setAdapter(new NewDailyGamesButtonsAdapter(this, newGameButtonItems));
	}

	@Override
	public Context getMeContext() {
		return getContext();
	}

	private static class NewDailyGameButtonItem {

		public int days;
		public String label;

		static NewDailyGameButtonItem createNewButtonFromLabel(int label, Context context){
			NewDailyGameButtonItem buttonItem = new NewDailyGameButtonItem();

			buttonItem.days = label;
			buttonItem.label = context.getString(R.string.days_arg, label);
			return buttonItem;
		}
	}

	private class NewDailyGamesButtonsAdapter extends ItemsAdapter<NewDailyGameButtonItem> {

		private ItemClickListenerFace clickListenerFace;

		public NewDailyGamesButtonsAdapter(ItemClickListenerFace clickListenerFace, List<NewDailyGameButtonItem> itemList) {
			super(clickListenerFace.getMeContext(), itemList);
			this.clickListenerFace = clickListenerFace;
		}

		@Override
		protected View createView(ViewGroup parent) {
			ViewHolder holder = new ViewHolder();
			RoboButton view = new RoboButton(getContext(), null, R.attr.greyButtonSmallSolid);
			holder.label = view;
			view.setTag(holder);
			view.setOnClickListener(clickListenerFace);
			return view;
		}

		@Override
		protected void bindView(NewDailyGameButtonItem item, int pos, View convertView) {
			ViewHolder holder = (ViewHolder) convertView.getTag();
			holder.label.setText(item.label);

			convertView.setTag(itemListId, pos);
		}

		private class ViewHolder{
			RoboButton label;
		}
	}
}
