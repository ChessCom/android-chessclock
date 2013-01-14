package com.chess.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.RoboButton;
import com.chess.RoboTextView;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.01.13
 * Time: 11:04
 */
public class NewDefaultGameView extends LinearLayout {

	/**
	 * Must be set outside to be able handle clicks
	 */
	protected static int BASE_ID;
	public static final int TITLE_ID = BASE_ID + 1;

	public static final int LEFT_BTN_ID = BASE_ID + 2;
	private static final int PLAY_BUTTON_ID = BASE_ID + 3;
	private static final int OPTIONS_ID = BASE_ID + 4;

	public static final int TOP_TEXT_SIZE = 14;
	public static final int BUTTON_TEXT_SIZE = 13;

	private int HEADER_PADDING_LEFT = 13;
	private int HEADER_PADDING_RIGHT = 11;
	private int COMPACT_PADDING = 14;
	protected float density;

	public NewDefaultGameView(Context context) {
		super(context);
		onCreate();
	}

	public NewDefaultGameView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate();
	}

	public NewDefaultGameView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		onCreate();
	}

	public void onCreate() {
		setOrientation(VERTICAL);

		density = getContext().getResources().getDisplayMetrics().density;

		HEADER_PADDING_LEFT *= density;
		HEADER_PADDING_RIGHT *= density;
		COMPACT_PADDING *= density;
	}

	public void setConfig(ConfigItem configItem) {
		if (configItem.getBaseId() == 0) {
			throw new IllegalArgumentException("BASE_ID must be set");
		} else {
			BASE_ID = configItem.getBaseId();
		}

		addGameSetupView(configItem);
	}



	private void addGameSetupView(ConfigItem configItem) {
		LinearLayout.LayoutParams defaultWrapParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		{// Header
			// Daily Games View
			LinearLayout headerView = new LinearLayout(getContext());
			LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					(int) (44 * density));
			headerView.setLayoutParams(headerParams);
			headerView.setBackgroundResource(R.drawable.nav_menu_item_default);
			headerView.setPadding(HEADER_PADDING_LEFT, 0, HEADER_PADDING_RIGHT, 0);
			headerView.setGravity(Gravity.CENTER_VERTICAL);
			// DailyGames icon
			ImageView imageView = new ImageView(getContext());
			imageView.setImageResource(configItem.getHeaderIcon());

			headerView.addView(imageView, defaultWrapParams);

			// DailyGames Header
			TextView headerText = new TextView(getContext());
			LinearLayout.LayoutParams headerTxtParams = new LinearLayout.LayoutParams(0,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			headerTxtParams.weight = 1;

			headerText.setText(configItem.getHeaderText());
			headerText.setTextColor(getContext().getResources().getColor(R.color.new_normal_gray));
			headerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TOP_TEXT_SIZE);
			headerText.setPadding((int) (9 * density), 0, 0, 0);
			headerView.addView(headerText, headerTxtParams);

			// DailyGames Info Button
			ImageView infoButton = new ImageView(getContext());
			infoButton.setImageResource(R.drawable.ic_help); // TODO set selector
			headerView.addView(infoButton, defaultWrapParams);

			addView(headerView);

		}

		// Compact Options Quick view
		RelativeLayout compactRelLay = new RelativeLayout(getContext());
		RelativeLayout.LayoutParams relParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		compactRelLay.setLayoutParams(relParams);
		compactRelLay.setBackgroundResource(R.drawable.nav_menu_item_selected);
		compactRelLay.setPadding(COMPACT_PADDING, (int) (9 * density), COMPACT_PADDING, COMPACT_PADDING);

		{// Add defaultMode View
			// Add Title
			TextView titleText = new TextView(getContext());
			titleText.setText(configItem.getTitleText());
			titleText.setTextColor(getContext().getResources().getColor(R.color.new_normal_gray));
			titleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TOP_TEXT_SIZE);

			titleText.setId(TITLE_ID);
			titleText.setPadding(0, 0, 0, (int) (5 * density));

			compactRelLay.addView(titleText, defaultWrapParams);

			addButtons(configItem, compactRelLay);

		}

		{// Add Options View
			RelativeLayout optionsAndPlayView = new RelativeLayout(getContext());
			RelativeLayout.LayoutParams optionsLayParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			optionsLayParams.setMargins(0, (int) (8 * density), 0, 0);
			optionsLayParams.addRule(RelativeLayout.BELOW, LEFT_BTN_ID);

			optionsAndPlayView.setLayoutParams(optionsLayParams);

			// Options label/button
			RoboTextView optionsTxt = new RoboTextView(getContext());
			RelativeLayout.LayoutParams optionsTxtParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			optionsTxtParams.addRule(RelativeLayout.CENTER_VERTICAL);

			optionsTxt.setId(OPTIONS_ID);
			optionsTxt.setText(R.string.new_options);
			optionsTxt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TOP_TEXT_SIZE);
			optionsTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right, 0);
			optionsTxt.setPadding((int) (3 * density), 0, 0, 0);

			optionsAndPlayView.addView(optionsTxt, optionsTxtParams);

			addCustomView(configItem, optionsAndPlayView);

			compactRelLay.addView(optionsAndPlayView);

			addView(compactRelLay);

		}
	}

	protected void addCustomView(ConfigItem configItem, RelativeLayout optionsAndPlayView) {
	}

	protected void addButtons(ConfigItem configItem, RelativeLayout compactRelLay) {
		// Left Button - "3 days Mode"
		RoboButton leftButton = new RoboButton(getContext(), null, R.attr.greyButtonSmallSolid);
		RelativeLayout.LayoutParams leftBtnParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		leftBtnParams.addRule(RelativeLayout.BELOW, TITLE_ID);
		leftButton.setText(configItem.getLeftButtonText());
		leftButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, BUTTON_TEXT_SIZE);
		leftButton.setId(LEFT_BTN_ID);
		compactRelLay.addView(leftButton, leftBtnParams);


		// Play Button
		RoboButton playButton = new RoboButton(getContext(), null, R.attr.orangeButtonSmall);
		RelativeLayout.LayoutParams playButtonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		playButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		playButtonParams.addRule(RelativeLayout.BELOW, TITLE_ID);

		playButton.setText(R.string.new_play_ex);
		playButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
		playButton.setId(BASE_ID + PLAY_BUTTON_ID);

		compactRelLay.addView(playButton, playButtonParams);
	}

	public static class ConfigItem {

		private int headerIcon;
		private int headerText;
		private int leftButtonText;
		private int rightButtonText;
		private int titleText;
		private int baseId;

		public int getHeaderIcon() {
			return headerIcon;
		}

		public void setHeaderIcon(int headerIcon) {
			this.headerIcon = headerIcon;
		}

		public int getHeaderText() {
			return headerText;
		}

		public void setHeaderText(int headerText) {
			this.headerText = headerText;
		}

		public int getLeftButtonText() {
			return leftButtonText;
		}

		public void setLeftButtonText(int leftButtonText) {
			this.leftButtonText = leftButtonText;
		}

		public int getRightButtonText() {
			return rightButtonText;
		}

		public void setRightButtonText(int rightButtonText) {
			this.rightButtonText = rightButtonText;
		}

		public int getTitleText() {
			return titleText;
		}

		public void setTitleText(int titleText) {
			this.titleText = titleText;
		}

		public int getBaseId() {
			return baseId;
		}

		public void setBaseId(int baseId) {
			this.baseId = baseId;
		}
	}
}
