package com.chess.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.chess.FontsHelper;
import com.chess.R;
import com.chess.RoboButton;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 14.07.13
 * Time: 21:47
 */
public class PageIndicatorView extends LinearLayout implements View.OnClickListener {

	private static final int BASE_BTN_ID = 0x00004889;

	private static final int FIRST = 0;
	private static final float ARROW_TEXT_SIZE = 25;
	private int buttonSize;
	private LayoutParams buttonParams;
	private int buttonsCnt;
	private int totalPageCnt;
	private PagerFace pagerFace;
	private int visiblePageButtonsCnt;

	public PageIndicatorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		setOrientation(HORIZONTAL);
		setGravity(Gravity.CENTER);
		// get width
		Resources resources = context.getResources();
		int widthPixels = resources.getDisplayMetrics().widthPixels;
		float density = resources.getDisplayMetrics().density;

		buttonSize = resources.getDimensionPixelSize(R.dimen.page_indicator_button_size);

		buttonParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		int buttonMargin = (int) (3 * density);
		buttonParams.setMargins(buttonMargin, 0, buttonMargin, 0);

		buttonsCnt = widthPixels / (buttonSize + buttonMargin * 2);
		for (int i = 0; i < buttonsCnt; i++) {
			RoboButton roboButton = getDefaultButton(context);
			roboButton.setId(BASE_BTN_ID + i);
			roboButton.setText(String.valueOf(i));
			roboButton.setOnClickListener(this);
			addView(roboButton);
		}

		visiblePageButtonsCnt = buttonsCnt - 3; // 2 for arrows, one for "..."
		// set most left & right as arrows
		RoboButton leftButton = (RoboButton) findViewById(getLeftBtn());
		leftButton.setFont(FontsHelper.ICON_FONT);
		leftButton.setTextSize(ARROW_TEXT_SIZE);
		leftButton.setText(R.string.ic_left);

		RoboButton rightButton = (RoboButton) findViewById(getRightBtn());
		rightButton.setFont(FontsHelper.ICON_FONT);
		rightButton.setTextSize(ARROW_TEXT_SIZE);
		rightButton.setText(R.string.ic_right);

	}

	public void setTotalPageCnt(int totalPageCnt) {
		this.totalPageCnt = totalPageCnt;
		if (totalPageCnt > buttonsCnt) {
			((TextView) findViewById(getLast())).setText(String.valueOf(totalPageCnt));
			((TextView) findViewById(getPreLast())).setText("...");
			(findViewById(getPreLast())).setBackgroundResource(R.drawable.empty);
		} else {
			for (int i = getLast() - BASE_BTN_ID; i > totalPageCnt; i--) {
				findViewById(BASE_BTN_ID + i).setVisibility(INVISIBLE);
			}
		}

	}

	private RoboButton getDefaultButton(Context context) {
		RoboButton roboButton = new RoboButton(context, null, R.attr.pageButton);
		roboButton.setDrawableStyle(R.style.Button_Page);
		roboButton.setTextAppearance(context, R.style.Button_Page);
		roboButton.setMinWidth(buttonSize);
		roboButton.setMinHeight(buttonSize);
		roboButton.setFont(FontsHelper.BOLD_FONT);

		roboButton.setLayoutParams(buttonParams);
		return roboButton;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		int btnPos = id - BASE_BTN_ID;
		if (id == getLeftBtn()) { // left arrow
			pagerFace.showPrevPage();
		} else if (id == getRightBtn()) {
			pagerFace.showNextPage();
		} else if (id == getLast()) {
			pagerFace.showPage(totalPageCnt);
		} else if (id == getPreLast()) {
			// do nothing
			Log.d("TEST", " middle clicked");
		} else {
			pagerFace.showPage(btnPos);
		}
	}

	private int getLeftBtn() {
		return BASE_BTN_ID + FIRST;
	}

	private int getRightBtn() {
		return BASE_BTN_ID + buttonsCnt - 1;
	}

	private int getLast() {
		return BASE_BTN_ID + buttonsCnt - 2;
	}

	private int getPreLast() {
		return BASE_BTN_ID + buttonsCnt - 3;
	}

	public void setPagerFace(PagerFace pagerFace) {
		this.pagerFace = pagerFace;
	}

	public int getVisiblePageButtonsCnt() {
		return visiblePageButtonsCnt;
	}

	public void enableLeftBtn(boolean enable) {
		findViewById(getLeftBtn()).setEnabled(enable);
	}

	public void enableRightBtn(boolean enable) {
		findViewById(getRightBtn()).setEnabled(enable);
	}

	public interface PagerFace {
		void showPrevPage();
		void showNextPage();
		void showPage(int page);
	}
}
