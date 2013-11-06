package com.chess.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.utilities.FontsHelper;
import com.chess.R;
import com.chess.widgets.RoboButton;
import com.chess.widgets.RoboTextView;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.utilities.AppUtils;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 12.01.13
 * Time: 11:04
 */
public abstract class NewGameDefaultView extends LinearLayout implements View.OnClickListener {

	private static int OPTIONS_AND_PLAY_VIEW_HEIGHT;
	private static int OPTIONS_VIEW_MARGIN_TOP;
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

	private static int HEADER_PADDING_LEFT;
	private static int HEADER_TEXT_PADDING_LEFT;
	private static int HEADER_HEIGHT;
	private static int HEADER_PADDING_RIGHT;
	protected static int COMPACT_PADDING;

	protected float density;
	protected boolean optionsVisible;
	private RoboButton playButton;
	private RoboButton leftButton;
	protected TextView titleText;
	protected RoboTextView optionsTxt;
	protected RelativeLayout compactRelLay;
	protected View optionsView;
	private static LayoutParams optionsVisibleLayoutParams;
	private static LayoutParams defaultMatchWidthParams;
	private static LayoutParams defaultLinearWrapParams;
	private static RelativeLayout.LayoutParams defaultRelativeMatchWidthParams;
	private RelativeLayout.LayoutParams optionsLayParams;
	private RelativeLayout.LayoutParams optionsTxtParams;


	public NewGameDefaultView(Context context) {
		super(context);
		onCreate();
	}

	public NewGameDefaultView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate();
	}

	public void onCreate() {
		setOrientation(VERTICAL);

		density = getContext().getResources().getDisplayMetrics().density;

		HEADER_PADDING_LEFT = (int) (13 * density);   // TODO move to resources?
		HEADER_PADDING_RIGHT = (int) (11 * density);
		HEADER_TEXT_PADDING_LEFT = (int) (9 * density);
		HEADER_HEIGHT = (int) (44 * density);

		OPTIONS_AND_PLAY_VIEW_HEIGHT = (int) (45 * density);
		OPTIONS_VIEW_MARGIN_TOP = (int) (4 * density);
		COMPACT_PADDING = (int) getContext().getResources().getDimension(R.dimen.new_game_frame_padding_side);

		optionsVisibleLayoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, HEADER_HEIGHT);
		defaultMatchWidthParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		defaultLinearWrapParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		defaultRelativeMatchWidthParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

		optionsLayParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		optionsTxt = new RoboTextView(getContext());
		optionsTxtParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);


	}

	public void setConfig(ViewConfig viewConfig) {
		if (viewConfig.getBaseId() == 0) {
			throw new IllegalArgumentException("BASE_ID must be set");
		} else {
			BASE_ID = viewConfig.getBaseId();
		}

		addGameSetupView(viewConfig);
		addOptionsView();
	}

	private void addGameSetupView(ViewConfig viewConfig) {
		Context context = getContext();
		if (context == null) {
			return;
		}
		Resources resources = context.getResources();

		{// Header
			// Header View
			LinearLayout headerView = new LinearLayout(context);
			LayoutParams headerParams = new LayoutParams(LayoutParams.MATCH_PARENT, HEADER_HEIGHT);
			headerView.setLayoutParams(headerParams);
			ButtonDrawableBuilder.setBackgroundToView(headerView, R.style.ListItem);
			headerView.setPadding(HEADER_PADDING_LEFT, 0, HEADER_PADDING_RIGHT, 0);
			headerView.setGravity(Gravity.CENTER_VERTICAL);

			// Header icon
			RoboTextView headerIconTxt = new RoboTextView(context);
			headerIconTxt.setFont(FontsHelper.ICON_FONT);
			headerIconTxt.setText(viewConfig.getHeaderIcon());

			headerView.addView(headerIconTxt, defaultLinearWrapParams);

			// Text Header
			TextView headerText = new TextView(context);
			LayoutParams headerTxtParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT);
			headerTxtParams.weight = 1;

			headerText.setText(viewConfig.getHeaderText());
			headerText.setTextColor(resources.getColor(R.color.new_normal_grey));
			headerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TOP_TEXT_SIZE);
			headerText.setPadding(HEADER_TEXT_PADDING_LEFT, 0, 0, 0);
			headerView.addView(headerText, headerTxtParams);

			addView(headerView);
		}

		// Compact Options Quick view
		compactRelLay = new RelativeLayout(context);
		compactRelLay.setLayoutParams(defaultRelativeMatchWidthParams);
		ButtonDrawableBuilder.setBackgroundToView(compactRelLay, R.style.ListItem_Header);
		compactRelLay.setPadding(COMPACT_PADDING, 0, COMPACT_PADDING, COMPACT_PADDING);

		{// Add defaultMode View
			titleText = new TextView(context);
			titleText.setText(viewConfig.getTitleText());
			titleText.setTextColor(resources.getColor(R.color.new_normal_grey));
			titleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TOP_TEXT_SIZE);

			titleText.setId(BASE_ID + TITLE_ID);
			titleText.setPadding(0, (int) (9 * density), 0, (int) (5 * density));

			compactRelLay.addView(titleText, defaultLinearWrapParams);

			addButtons(viewConfig, compactRelLay);

		}

		{// Add Options View
			RelativeLayout optionsAndPlayView = new RelativeLayout(context);
			optionsLayParams.setMargins(0, OPTIONS_VIEW_MARGIN_TOP, 0, 0);
			optionsLayParams.addRule(RelativeLayout.BELOW, BASE_ID + LEFT_BUTTON_ID);

			optionsAndPlayView.setId(BASE_ID + OPTIONS_TXT_ID);
			optionsAndPlayView.setLayoutParams(optionsLayParams);
			optionsAndPlayView.setMinimumHeight(OPTIONS_AND_PLAY_VIEW_HEIGHT);
			optionsAndPlayView.setOnClickListener(this);

			// Options label/button
			optionsTxtParams.addRule(RelativeLayout.CENTER_VERTICAL);

			optionsTxt.setId(BASE_ID + OPTIONS_TXT_ID);
			optionsTxt.setText(R.string.new_options);
			optionsTxt.setTextColor(resources.getColor(R.color.new_soft_grey));
			optionsTxt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TOP_TEXT_SIZE);
			AppUtils.iconRestore();
//			optionsTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right, 0);
			optionsTxt.setPadding((int) (3 * density), (int) (4 * density), 0, 0);
			optionsTxt.setOnClickListener(this);

			optionsAndPlayView.addView(optionsTxt, optionsTxtParams);

			addCustomView(viewConfig, optionsAndPlayView);

			compactRelLay.addView(optionsAndPlayView);

			addView(compactRelLay);
		}
	}

	protected void addCustomView(ViewConfig viewConfig, RelativeLayout optionsAndPlayView) {
	}

	public abstract void addOptionsView();

	protected void addButtons(ViewConfig viewConfig, RelativeLayout compactRelLay) {
		// Left Mode Button
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
			ButtonDrawableBuilder.setBackgroundToView(compactRelLay, R.style.ListItem);
			compactRelLay.setLayoutParams(optionsVisibleLayoutParams);
			AppUtils.iconRestore();
//			optionsTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_down, 0);
		} else {
			ButtonDrawableBuilder.setBackgroundToView(compactRelLay, R.style.ListItem_Header);

			compactRelLay.setLayoutParams(defaultMatchWidthParams);
			AppUtils.iconRestore();
//			optionsTxt.setCompoundDrawablesWithIntrinsicBounds(0,0, R.drawable.ic_arrow_right, 0);
		}
		compactRelLay.setPadding(COMPACT_PADDING, 0, COMPACT_PADDING, COMPACT_PADDING);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == BASE_ID + OPTIONS_TXT_ID) {
			toggleOptions();
		}
	}

	public static class ViewConfig {

		private int headerIcon;
		private int headerText;
		private int leftButtonTextId;
		protected String leftButtonText;
		private int rightButtonTextId;
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

		public int getLeftButtonTextId() {
			return leftButtonTextId;
		}

		public void setLeftButtonTextId(int leftButtonTextId) {
			this.leftButtonTextId = leftButtonTextId;
		}

		public void setLeftButtonText(String leftButtonText) {
			this.leftButtonText = leftButtonText;
		}

		public String getLeftButtonText() {
			return leftButtonText;
		}

		public int getRightButtonTextId() {
			return rightButtonTextId;
		}

		public void setRightButtonTextId(int rightButtonTextId) {
			this.rightButtonTextId = rightButtonTextId;
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
