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
public class NewGameSetupTemplateView extends LinearLayout {

	private static final int BASE_ID = 0x00001000;
	private static final int TITLE_ID = BASE_ID + 11;
	private static final int VS_ID = BASE_ID + 12;
	private static final int LEFT_BTN_ID = BASE_ID + 13;
	public static final int TOP_TEXT_SIZE = 15;

	private static int HEADER_PADDING_LEFT = 13;
	private static int HEADER_PADDING_RIGHT = 11;
	private static int COMPACT_PADDING = 14;
	private float density;
	private Resources resources;

	public NewGameSetupTemplateView(Context context) {
		super(context);
		onCreate();
	}

	public NewGameSetupTemplateView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate();
	}

	public NewGameSetupTemplateView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		onCreate();
	}

	public void onCreate() {
		setOrientation(VERTICAL);


		density = getContext().getResources().getDisplayMetrics().density;
		resources = getContext().getResources();

		HEADER_PADDING_LEFT *= density;
		HEADER_PADDING_RIGHT *= density;
		COMPACT_PADDING *= density;

		addDailyGameSetupView();
	}

	private void addDailyGameSetupView() {
		LinearLayout.LayoutParams defaultWrapParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		LinearLayout.LayoutParams defaultLinLayParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);


		{// Header
			// Daily Games View
			LinearLayout headerView = new LinearLayout(getContext());
			LinearLayout.LayoutParams headerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					(int) (42 * density));
			headerView.setLayoutParams(headerParams);
			headerView.setBackgroundResource(R.drawable.nav_menu_item_default);
			headerView.setPadding(HEADER_PADDING_LEFT, 0, HEADER_PADDING_RIGHT, 0);
			headerView.setGravity(Gravity.CENTER_VERTICAL);
			// DailyGames icon
			ImageView imageView = new ImageView(getContext());
			imageView.setImageResource(R.drawable.ic_daily_game);

			headerView.addView(imageView, defaultWrapParams);

			// DailyGames Header
			TextView headerText = new TextView(getContext());
			LinearLayout.LayoutParams headerTxtParams = new LinearLayout.LayoutParams(0,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			headerTxtParams.weight = 1;
			headerText.setText("Daily Games");
			headerText.setTextColor(getContext().getResources().getColor(R.color.new_normal_gray));
			headerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
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
		compactRelLay.setPadding(COMPACT_PADDING, (int) (9* density), COMPACT_PADDING, COMPACT_PADDING);

		{// Add defaultMode View
			// Add Title
			TextView titleText = new TextView(getContext());
			titleText.setText("Per Turn");
			titleText.setTextColor(getContext().getResources().getColor(R.color.new_normal_gray));
			titleText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TOP_TEXT_SIZE);

			titleText.setId(TITLE_ID);
			titleText.setPadding(0, 0, 0, (int) (9 * density));

			compactRelLay.addView(titleText, defaultWrapParams);

			// Left Button - "3 days Mode"
			RoboButton leftButton = new RoboButton(getContext(), null, R.attr.greyButton);
			RelativeLayout.LayoutParams leftBtnParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			leftBtnParams.addRule(RelativeLayout.BELOW, TITLE_ID);
			leftButton.setText("3 days");
			leftButton.setId(LEFT_BTN_ID);
			compactRelLay.addView(leftButton, leftBtnParams);

			// vs
			RoboTextView vsText = new RoboTextView(getContext());
			RelativeLayout.LayoutParams vsTxtParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);

			vsTxtParams.addRule(RelativeLayout.RIGHT_OF, LEFT_BTN_ID);
			vsTxtParams.addRule(RelativeLayout.BELOW, TITLE_ID);
			vsText.setPadding((int)(8 * density),(int) (10 * density), (int) (8 * density), 0);
			vsText.setText("vs");
			vsText.setTextColor(getContext().getResources().getColor(R.color.new_normal_gray));
			vsText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
			vsText.setId(VS_ID);

			compactRelLay.addView(vsText, vsTxtParams);

			// Right Button - "Random"
			RoboButton rightButton = new RoboButton(getContext(), null, R.attr.greyButton);
			RelativeLayout.LayoutParams rightButtonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);

			rightButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			rightButtonParams.addRule(RelativeLayout.RIGHT_OF, VS_ID);
			rightButtonParams.addRule(RelativeLayout.BELOW, TITLE_ID);

			rightButton.setText("Random");

			compactRelLay.addView(rightButton, rightButtonParams);

		}

		{// Add Options View
			RelativeLayout optionsAndPlayView = new RelativeLayout(getContext());
			RelativeLayout.LayoutParams optionsLayParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			optionsLayParams.addRule(RelativeLayout.BELOW, LEFT_BTN_ID);
			optionsAndPlayView.setPadding(0, (int) (8 * density), 0, 0);
			optionsAndPlayView.setLayoutParams(optionsLayParams);

			// Options label/button
			RoboTextView optionsTxt = new RoboTextView(getContext());
			RelativeLayout.LayoutParams optionsTxtParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			optionsTxtParams.addRule(RelativeLayout.CENTER_VERTICAL);

			optionsTxt.setText("Options");
			optionsTxt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, TOP_TEXT_SIZE);

			optionsTxt.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_right, 0);

			optionsAndPlayView.addView(optionsTxt, optionsTxtParams);

			// Play Button
			RoboButton playButton = new RoboButton(getContext(), null, R.attr.orangeButton);
			RelativeLayout.LayoutParams playButtonParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			playButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			playButtonParams.addRule(RelativeLayout.BELOW, LEFT_BTN_ID);
//			playButton.setLayoutParams(playButtonParams);
			playButton.setText("Play!");


			optionsAndPlayView.addView(playButton, playButtonParams);

			compactRelLay.addView(optionsAndPlayView);

			addView(compactRelLay);


		}


	}
}
