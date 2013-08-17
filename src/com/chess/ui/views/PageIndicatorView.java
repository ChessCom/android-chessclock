package com.chess.ui.views;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
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
	private static final float ARROW_TEXT_SIZE = 20;
	private static final int NON_INIT = -1;
	public static final String DOTS_SYMBOL = "...";
	private static final int FIRST_PAGE_NUMBER = 1;

	private int buttonSize;
	private LayoutParams buttonParams;
	private int buttonsCnt;
	private int totalPageCnt;
	private PagerFace pagerFace;
	private int visiblePageButtonsCnt;
	private int selectedPage = NON_INIT;
	private ButtonsMode buttonsMode;
	private int activeButton;
	private SparseIntArray buttonsMap;
//	private int lastPressedButton;

	public PageIndicatorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		setOrientation(HORIZONTAL);
		setGravity(Gravity.CENTER);

		buttonsMode = ButtonsMode.LEFT;
		buttonsMap = new SparseIntArray();

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

		// activate first page button by default
		selectedPage = 0;
		RoboButton firstButton = (RoboButton) findViewById(getFirst());
		firstButton.setDrawableStyle(R.style.Button_Page_Selected);
	}

	/**
	 * update all buttons page with according number
	 * set page number according to server numeration - from 0.
	 *
	 * @param totalPageCnt number of all pages
	 */
	public void setTotalPageCnt(int totalPageCnt) {
		if (this.totalPageCnt != totalPageCnt) {
			this.totalPageCnt = totalPageCnt;

			if (totalPageCnt > visiblePageButtonsCnt) { // if 65 > 6. then we add dots "..."
				visiblePageButtonsCnt = visiblePageButtonsCnt - 1;
			}
			updateButtonsNumbers();
		}
	}

	private void setButtonPageNumber(int pageNumber, int position) {
		int displayPageNumber = pageNumber + 1; // we add 1, bcz start from 0
		buttonsMap.put(position, pageNumber);
		((TextView) findViewById(BASE_BTN_ID + position)).setText(String.valueOf(displayPageNumber));
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
	public void onClick(View view) {
		int id = view.getId();
		int btnPos = id - BASE_BTN_ID;
		int pageToShow = buttonsMap.get(btnPos);

		deactivateSelectedButton();

		if (id == getLeftBtn()) { // left arrow (previous page)
			selectedPage--;
			pagerFace.showPrevPage();

			// current page = 4
			// visiblePageButtonsCnt = 6
			// visibleMiddleButtonsCnt = 3

			if (selectedPage < visiblePageButtonsCnt) { // 3 number of dots buttons
				buttonsMode = ButtonsMode.LEFT;
				// need to change buttonsMap from middle state, to edge state
			} else {
				if (buttonsMode == ButtonsMode.LEFT) {

				} else if (buttonsMode == ButtonsMode.RIGHT) {
					// | < | | 1 | ... | 61 | | 62 | | 63 | | 64 | | 65 | | > |
					//	 ^ - user pressed that button
					// then buttons goes like this
					// | < | | 1 | ... | 58 | | 59 | | 60 | ... | 65 | | > |
					//                               ^ - this button becomes highlighted
					buttonsMode = ButtonsMode.MIDDLE_FROM_RIGHT;
				} else {
					buttonsMode = ButtonsMode.MIDDLE_FROM_RIGHT;
				}
			}

		} else if (id == getRightBtn()) { // right arrow (next page)
			selectedPage++;
			pagerFace.showNextPage();
			if (buttonsMode == ButtonsMode.LEFT) {
				// | < | | 1 | | 2 | | 3 | | 4 | | 5 | ... | 65 | | > |
				//													^ - user pressed that button, and current page was 5
				// then buttons goes like this
				// | < | | 1 | ... | 6 | | 7 | | 8 | ... | 65 | | > |
				//                   ^ - this button becomes highlighted
				if (selectedPage >= visiblePageButtonsCnt) {
					buttonsMode = ButtonsMode.MIDDLE_FROM_LEFT;
				}
			} else if (buttonsMode == ButtonsMode.RIGHT) {

			}
		} else if (id == getLast() && buttonsMode == ButtonsMode.LEFT) {   // TODO adjust logic when we have 7 pages
			pagerFace.showPage(totalPageCnt);
			buttonsMode = ButtonsMode.RIGHT;
		} else if (id == getFirst() && buttonsMode == ButtonsMode.RIGHT) {   // TODO adjust logic when we have 7 pages
			pagerFace.showPage(FIRST_PAGE_NUMBER);
			buttonsMode = ButtonsMode.LEFT;
		} else if (id == getPreFirst() && buttonsMode == ButtonsMode.RIGHT) {
			// do nothing
			Log.d("TEST", " middle clicked");
		} else if (id == getPreLast() && buttonsMode == ButtonsMode.LEFT) {
			// do nothing
			Log.d("TEST", " middle clicked");
		} else {
			selectedPage = pageToShow;

			if (buttonsMode == ButtonsMode.RIGHT) {
				//       65 - (6 - 4)
				pageToShow = totalPageCnt - (buttonsCnt - pageToShow);
				pagerFace.showPage(pageToShow);
			} else {
				pagerFace.showPage(pageToShow);
			}
		}

		activatePressedButton();

		updateButtonsNumbers();
	}

	private void updateButtonsNumbers() {
		if (buttonsMode == ButtonsMode.LEFT) {
			// draw buttons like this
			// | < | | 1 | | 2 | | 3 | | 4 | | 5 | ... | 65 | | > |
			if (totalPageCnt > buttonsCnt) {

				if (selectedPage != FIRST + 1) {
					((RoboButton) findViewById(getPreFirst())).setDrawableStyle(R.style.Button_Page);
				}
				int pageNumber = 0;
				for (int i = getFirst() - BASE_BTN_ID; i < getPreLast() - BASE_BTN_ID; i++) {
					setButtonPageNumber(pageNumber++, i);
				}

				// change number of last button to total pages cnt
				((TextView) findViewById(getLast())).setText(String.valueOf(totalPageCnt));

				// change preLast button to dots
				((TextView) findViewById(getPreLast())).setText(DOTS_SYMBOL);
				(findViewById(getPreLast())).setBackgroundResource(R.drawable.empty);
			} else {
				for (int i = getLast() - BASE_BTN_ID; i > totalPageCnt; i--) {
					findViewById(BASE_BTN_ID + i).setVisibility(INVISIBLE);
				}
			}
		} else if (buttonsMode == ButtonsMode.MIDDLE_FROM_LEFT) {
			// draw buttons like this
			// | < | | 1 | ... | 5 | | 6 | | 7 | ... | 65 | | > |
			//                   ^ - current selected page
			int pageNumber = selectedPage;
			for (int i = getPreFirst() - BASE_BTN_ID + 1; i < getPreLast() - BASE_BTN_ID; i++) {
				setButtonPageNumber(pageNumber, i);
				pageNumber++;
			}

			// change preFirst button to dots
			((TextView) findViewById(getPreFirst())).setText(DOTS_SYMBOL);
			(findViewById(getPreFirst())).setBackgroundResource(R.drawable.empty);

			// change preLast button to dots
			((TextView) findViewById(getPreLast())).setText(DOTS_SYMBOL);
			(findViewById(getPreLast())).setBackgroundResource(R.drawable.empty);
		} else if (buttonsMode == ButtonsMode.MIDDLE_FROM_RIGHT) {
			// draw buttons like this
			// | < | | 1 | ... | 6 | | 7 | | 8 | ... | 65 | | > |
			//                   ^ -
			// | < | | 1 | ... | 58 | | 59 | | 60 | ... | 65 | | > |
			//                                  ^ - current selected page

			int pageNumber = selectedPage;
			for (int i = getPreLast() - BASE_BTN_ID; i > FIRST + 2; i--) {
				setButtonPageNumber(pageNumber++, i);
			}

			// change preFirst button to dots
			((TextView) findViewById(getPreFirst())).setText(DOTS_SYMBOL);
			(findViewById(getPreFirst())).setBackgroundResource(R.drawable.empty);

			// change preLast button to dots
			((TextView) findViewById(getPreLast())).setText(DOTS_SYMBOL);
			(findViewById(getPreLast())).setBackgroundResource(R.drawable.empty);
		} else if (buttonsMode == ButtonsMode.RIGHT) {
			// draw buttons like this
			// | < | | 1 | ... | 61 | | 62 | | 63 | | 64 | | 65 | | > |

//			((RoboButton) findViewById(getPreLast())).setDrawableStyle(R.style.Button_Page);

			int pageNumber = totalPageCnt;
			for (int i = getLast() - BASE_BTN_ID; i > FIRST + 1; i--) {
				setButtonPageNumber(pageNumber--, i);
			}

			((TextView) findViewById(getFirst())).setText(String.valueOf(FIRST_PAGE_NUMBER));

			// change preFirst button to dots
			((TextView) findViewById(getPreFirst())).setText(DOTS_SYMBOL);
			(findViewById(getPreFirst())).setBackgroundResource(R.drawable.empty);
		}
	}

	private void deactivateSelectedButton() {
		{ // deactivate current button
			int prevBtnPos = 0;
			for (int i = 0; i < buttonsMap.size(); i++) {
				int page = buttonsMap.valueAt(i);
				if (page == selectedPage) {
					prevBtnPos = buttonsMap.keyAt(i);
					break;
				}
			}
			RoboButton roboButton = (RoboButton) findViewById(BASE_BTN_ID + prevBtnPos);
			if (roboButton != null) {
				roboButton.setDrawableStyle(R.style.Button_Page);
			}
		}

		// deactivate buttons if goes from middle mode
		if (buttonsMode == ButtonsMode.MIDDLE_FROM_LEFT) {
			RoboButton roboButton = (RoboButton) findViewById(activeButton);
			if (roboButton != null) {
				roboButton.setDrawableStyle(R.style.Button_Page);
			}
		} else if (buttonsMode == ButtonsMode.MIDDLE_FROM_RIGHT) {
			RoboButton roboButton = (RoboButton) findViewById(activeButton);
			if (roboButton != null) {
				roboButton.setDrawableStyle(R.style.Button_Page);
			}
		}
	}

	private void activatePressedButton() {
		// activate pressed button
		if (buttonsMode == ButtonsMode.MIDDLE_FROM_LEFT) {
			// | < | | 1 | ... | 6 | | 7 | | 8 | ... | 65 | | > |
			//                   ^ activate this button
			activeButton = getPreFirst() + 1;
			RoboButton roboButton = (RoboButton) findViewById(activeButton);
			if (roboButton != null) {
				roboButton.setDrawableStyle(R.style.Button_Page_Selected);
			}
		} else if (buttonsMode == ButtonsMode.MIDDLE_FROM_RIGHT) {
			// | < | | 1 | ... | 6 | | 7 | | 8 | ... | 65 | | > |
			//                               ^ activate this button
			activeButton = getPreLast() - 1;
			RoboButton roboButton = (RoboButton) findViewById(activeButton);
			if (roboButton != null) {
				roboButton.setDrawableStyle(R.style.Button_Page_Selected);
			}
		} else {
			int prevBtnPos = 0;
			boolean pageFound = false;
			for (int i = 0; i < buttonsMap.size(); i++) {
				int page = buttonsMap.valueAt(i);
				if (page == selectedPage) {
					prevBtnPos = buttonsMap.keyAt(i);
					pageFound = true;
					break;
				}
			}

			if (!pageFound) { // we came from middle position
				prevBtnPos = selectedPage + 1;
			}

			RoboButton roboButton = (RoboButton) findViewById(BASE_BTN_ID + prevBtnPos);
			if (roboButton != null) {
				roboButton.setDrawableStyle(R.style.Button_Page_Selected);
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
	 *
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

	public interface PagerFace {
		void showPrevPage();

		void showNextPage();

		void showPage(int page);
	}

	enum ButtonsMode {
		LEFT,
		MIDDLE_FROM_LEFT,
		MIDDLE_FROM_RIGHT,
		RIGHT,
	}
}
