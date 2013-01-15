package com.chess.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
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
public abstract class NewGameDefaultView extends LinearLayout implements View.OnClickListener {

	/**
	 * Must be set outside to be able handle clicks
	 */
	protected int BASE_ID;
	public static final int TITLE_ID = 1;

	public static final int LEFT_BUTTON_ID = 2;
	public static final int PLAY_BUTTON_ID = 3;
	public static final int OPTIONS_TXT_ID = 4;

	public static final int TOP_TEXT_SIZE = 14;
	public static final int BUTTON_TEXT_SIZE = 13;

	private int HEADER_PADDING_LEFT = 13;
	private int HEADER_PADDING_RIGHT = 11;
	protected int COMPACT_PADDING = 14;

	protected float density;
	protected boolean optionsVisible;
	private RoboButton playButton;
	private RoboButton leftButton;
	protected TextView titleText;
	protected RoboTextView optionsTxt;
	protected RelativeLayout compactRelLay;
	protected View optionsView;


	public NewGameDefaultView(Context context) {
		super(context);
		onCreate();
	}

	public NewGameDefaultView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate();
	}

	public NewGameDefaultView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		onCreate();
	}

	public void onCreate() {
		setOrientation(VERTICAL);

		density = getContext().getResources().getDisplayMetrics().density;

		HEADER_PADDING_LEFT *= density;
		HEADER_PADDING_RIGHT *= density;
		COMPACT_PADDING = (int) getContext().getResources().getDimension(R.dimen.new_game_frame_padding_side);
	}

	public void setConfig(ConfigItem configItem) {
		if (configItem.getBaseId() == 0) {
			throw new IllegalArgumentException("BASE_ID must be set");
		} else {
			BASE_ID = configItem.getBaseId();
		}

		addGameSetupView(configItem);
		addOptionsView();
	}

	private void addGameSetupView(ConfigItem configItem) {
		LinearLayout.LayoutParams defaultWrapParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		{// Header
			// Header View
			LinearLayout headerView = new LinearLayout(getContext());
			LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					(int) (44 * density));
			headerView.setLayoutParams(headerParams);
			headerView.setBackgroundResource(R.drawable.nav_menu_item_default);
			headerView.setPadding(HEADER_PADDING_LEFT, 0, HEADER_PADDING_RIGHT, 0);
			headerView.setGravity(Gravity.CENTER_VERTICAL);

			// Header icon
			ImageView imageView = new ImageView(getContext());
			imageView.setImageResource(configItem.getHeaderIcon());

			headerView.addView(imageView, defaultWrapParams);

			// Text Header
			TextView headerText = new TextView(getContext());
			LinearLayout.LayoutParams headerTxtParams = new LinearLayout.LayoutParams(0,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			headerTxtParams.weight = 1;

			headerText.setText(configItem.getHeaderText());
			headerText.setTextColor(getContext().getResources().getColor(R.color.new_normal_gray));
			headerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TOP_TEXT_SIZE);
			headerText.setPadding((int) (9 * density), 0, 0, 0);
			headerView.addView(headerText, headerTxtParams);

			// Info Button
			ImageView infoButton = new ImageView(getContext());
			infoButton.setImageResource(R.drawable.ic_help); // TODO set selector
			headerView.addView(infoButton, defaultWrapParams);

			addView(headerView);
		}

		// Compact Options Quick view
		compactRelLay = new RelativeLayout(getContext());
		RelativeLayout.LayoutParams relParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		compactRelLay.setLayoutParams(relParams);
		compactRelLay.setBackgroundResource(R.drawable.nav_menu_item_selected);
		compactRelLay.setPadding(COMPACT_PADDING, 0, COMPACT_PADDING, COMPACT_PADDING);

		{// Add defaultMode View
			titleText = new TextView(getContext());
			titleText.setText(configItem.getTitleText());
			titleText.setTextColor(getContext().getResources().getColor(R.color.new_normal_gray));
			titleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TOP_TEXT_SIZE);

			titleText.setId(BASE_ID + TITLE_ID);
			titleText.setPadding(0, (int) (9 * density), 0, (int) (5 * density));

			compactRelLay.addView(titleText, defaultWrapParams);

			addButtons(configItem, compactRelLay);

		}

		{// Add Options View
			RelativeLayout optionsAndPlayView = new RelativeLayout(getContext());
			RelativeLayout.LayoutParams optionsLayParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			optionsLayParams.setMargins(0, (int) (4 * density), 0, 0);
			optionsLayParams.addRule(RelativeLayout.BELOW, BASE_ID + LEFT_BUTTON_ID);

			optionsAndPlayView.setId(BASE_ID + OPTIONS_TXT_ID);
			optionsAndPlayView.setLayoutParams(optionsLayParams);
			optionsAndPlayView.setMinimumHeight((int) (45 * density));
			optionsAndPlayView.setOnClickListener(this);

			// Options label/button
			optionsTxt = new RoboTextView(getContext());
			RelativeLayout.LayoutParams optionsTxtParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			optionsTxtParams.addRule(RelativeLayout.CENTER_VERTICAL);

			optionsTxt.setId(BASE_ID + OPTIONS_TXT_ID);
			optionsTxt.setText(R.string.new_options);
			optionsTxt.setTextColor(getContext().getResources().getColor(R.color.new_soft_gray));
			optionsTxt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TOP_TEXT_SIZE);
			optionsTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right, 0);
			optionsTxt.setPadding((int) (3 * density), (int) (4 * density), 0, 0);
			optionsTxt.setOnClickListener(this);

			optionsAndPlayView.addView(optionsTxt, optionsTxtParams);

			addCustomView(configItem, optionsAndPlayView);

			compactRelLay.addView(optionsAndPlayView);

			addView(compactRelLay);
		}
	}

	protected void addCustomView(ConfigItem configItem, RelativeLayout optionsAndPlayView) {
	}

	protected abstract void addOptionsView();

	protected void addButtons(ConfigItem configItem, RelativeLayout compactRelLay) {
		// Left Mode Button
		leftButton = new RoboButton(getContext(), null, R.attr.greyButtonSmallSolid);
		RelativeLayout.LayoutParams leftBtnParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		leftBtnParams.addRule(RelativeLayout.BELOW, BASE_ID + TITLE_ID);
		leftButton.setText(configItem.getLeftButtonText());
		leftButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, BUTTON_TEXT_SIZE);
		leftButton.setId(BASE_ID + LEFT_BUTTON_ID);

		compactRelLay.addView(leftButton, leftBtnParams);

		// Play Button
		playButton = new RoboButton(getContext(), null, R.attr.orangeButtonSmall);
		RelativeLayout.LayoutParams playButtonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		playButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		playButtonParams.addRule(RelativeLayout.BELOW, BASE_ID + TITLE_ID);

		playButton.setText(R.string.new_play_ex);
		playButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, BUTTON_TEXT_SIZE);
		playButton.setId(BASE_ID + PLAY_BUTTON_ID);

		compactRelLay.addView(playButton, playButtonParams);
	}

	public void toggleOptions() {
		optionsVisible = !optionsVisible;

		int compactVisibility = optionsVisible? GONE: VISIBLE;

		playButton.setVisibility(compactVisibility);
		leftButton.setVisibility(compactVisibility);
		titleText.setVisibility(compactVisibility);

		toggleCompactView();
	}

	protected void toggleCompactView() {
		if(optionsVisible) {
			compactRelLay.setBackgroundResource(R.drawable.game_option_back_1);
			LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (44 *density));
			compactRelLay.setLayoutParams(layoutParams);
			optionsTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
		} else {
			compactRelLay.setBackgroundResource(R.drawable.nav_menu_item_selected);
			LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			compactRelLay.setLayoutParams(layoutParams);
			optionsTxt.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_arrow_right, 0);
		}
		compactRelLay.setPadding(COMPACT_PADDING, 0, COMPACT_PADDING, COMPACT_PADDING);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == BASE_ID + OPTIONS_TXT_ID) {
			toggleOptions();
		}
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
