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
	private static final int NON_INIT = -1;
	public static final String DOTS_SYMBOL = "...";
	private static final int FIRST_PAGE_NUMBER = 1;

	private int buttonSize;
	private LayoutParams buttonParams;
	private int buttonsCnt;
	private int totalPageCnt;
	private PagerFace pagerFace;
	private int visiblePageButtonsCnt;
	private int previousActivePage = NON_INIT;
	private boolean reversedMode;

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
		if (!reversedMode) {
			if (totalPageCnt > buttonsCnt) {
				((RoboButton) findViewById(getPreFirst())).setDrawableStyle(R.style.Button_Page);

				int pageNumber = 1;
				for (int i = getFirst() - BASE_BTN_ID; i < getPreLast() - BASE_BTN_ID; i++) {
					((TextView) findViewById(BASE_BTN_ID + i)).setText(String.valueOf(pageNumber++));
				}

				((TextView) findViewById(getLast())).setText(String.valueOf(totalPageCnt));

				// change preLast button to dots
				((TextView) findViewById(getPreLast())).setText(DOTS_SYMBOL);
				(findViewById(getPreLast())).setBackgroundResource(R.drawable.empty);

				reversedMode = false;
			} else {
				for (int i = getLast() - BASE_BTN_ID; i > buttonsCnt; i--) {  // TODO check
					findViewById(BASE_BTN_ID + i).setVisibility(INVISIBLE);
				}
			}
		} else {
			// redraw buttons like this
			// | < | | 1 | ... | 61 | | 62 | | 63 | | 64 | | 65 | | > |


			((RoboButton) findViewById(getPreLast())).setDrawableStyle(R.style.Button_Page);

			int pageNumber = totalPageCnt;
			for (int i = getLast() - BASE_BTN_ID; i > FIRST + 1; i--) {
				((TextView) findViewById(BASE_BTN_ID + i)).setText(String.valueOf(pageNumber--));
			}

			((TextView) findViewById(getFirst())).setText(String.valueOf(FIRST_PAGE_NUMBER));

			// change preFirst button to dots
			((TextView) findViewById(getPreFirst())).setText(DOTS_SYMBOL);
			(findViewById(getPreFirst())).setBackgroundResource(R.drawable.empty);
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

		if (id == getLeftBtn()) { // left arrow (previous page)
			pagerFace.showPrevPage();
		} else if (id == getRightBtn()) { // right arrow (next page)
			pagerFace.showNextPage();
		} else if (id == getLast() && !reversedMode) {   // TODO adjust logic when we have 7 pages
			pagerFace.showPage(totalPageCnt);
			reversedMode = true;
		} else if (id == getFirst() && reversedMode) {   // TODO adjust logic when we have 7 pages
			pagerFace.showPage(FIRST_PAGE_NUMBER);
			reversedMode = false;
		} else if (id == getPreFirst() && reversedMode) {
			// do nothing
			Log.d("TEST", " middle clicked");
		} else if (id == getPreLast() && !reversedMode) {
			// do nothing
			Log.d("TEST", " middle clicked");
		} else {
			if (reversedMode) {
				//       65 - (6 - 4)
				btnPos = totalPageCnt - (buttonsCnt - btnPos);
				pagerFace.showPage(btnPos);

			} else {
				pagerFace.showPage(btnPos);
			}

		}
	}

	/**
	 * Left arrow. Means previous page button
	 */
	private int getLeftBtn() {
		return BASE_BTN_ID + FIRST;
	}

	/**
	 * Right arrow. Means Next page button
	 */
	private int getRightBtn() {
		return BASE_BTN_ID + buttonsCnt - 1;
	}

	private int getFirst() {
		return BASE_BTN_ID + FIRST + 1;
	}

	private int getLast() {
		return BASE_BTN_ID + buttonsCnt - 2;
	}

	private int getPreLast() {
		return BASE_BTN_ID + buttonsCnt - 3;
	}

	/**
	 * Used when user goes from last page to first. we replace 2nd button with "..."
	 * @return id of 2nd button
	 */
	private int getPreFirst() {
		return BASE_BTN_ID + FIRST + 2;
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

	public void activateCurrentPage(int page) {
		if (previousActivePage != NON_INIT) {
			RoboButton roboButton = (RoboButton) findViewById(BASE_BTN_ID + previousActivePage + 1);
			if (roboButton != null) { // TODO adjust properly!!!
				roboButton.setDrawableStyle(R.style.Button_Page);
			}
		}
		RoboButton roboButton = (RoboButton) findViewById(BASE_BTN_ID + page + 1);
		if (roboButton != null) { // TODO adjust properly!!!
			roboButton.setDrawableStyle(R.style.Button_Page_Selected);
		}
		previousActivePage = page;
	}

	public interface PagerFace {
		void showPrevPage();
		void showNextPage();
		void showPage(int page);
	}
}
