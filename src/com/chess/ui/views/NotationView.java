package com.chess.ui.views;


import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.chess.R;
import com.chess.statics.AppData;
import com.chess.widgets.RoboTextView;
import com.chess.statics.Symbol;
import com.chess.ui.views.chess_boards.NotationFace;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.utilities.AppUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class NotationView extends LinearLayout implements NotationFace,
		View.OnClickListener, ViewPager.OnPageChangeListener {

	public static final int NOTATION_ID = 0x00003321;

	private NotationsPagerAdapter notationsAdapter;
	private String[] originalNotations;
	private BoardForNotationFace selectionFace;
	private int textPadding;
	private ViewPager viewPager;
	private boolean newNotations;
	public int textSize;
	public int textColor;
	public int textColorSelected;
	private LayoutParams notationTextParams;
	private int textSidePadding;
	private SparseIntArray viewPerPageMap;
	private int viewWidth;
	private Paint notationTxtPaint;
	private int screenPadding;
	private float density;
	private Handler handler;
	private boolean clickable;

	public NotationView(Context context) {
		super(context);
		onCreate(null);
	}

	public NotationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		onCreate(attrs);
	}

	public void onCreate(AttributeSet attrs) {
		Context context = getContext();

		AppData appData = new AppData(context);
		Resources resources = context.getResources();
		density = resources.getDisplayMetrics().density;

		viewPerPageMap = new SparseIntArray();
		textSize = (int) (resources.getDimension(R.dimen.notations_text_size) / density);
		notationTxtPaint = new Paint();
		notationTxtPaint.setTextSize(textSize);

		handler = getHandler();
		if (handler == null) {
			handler = new Handler();
		}

		textColor = resources.getColor(R.color.notations_text_color);
		textColorSelected = resources.getColor(R.color.notations_text_color_selected);
		TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.NotationView);
		if (array == null) {
			return;
		}
		try {
			if (array.hasValue(R.styleable.NotationView_textColorBack)) {
				textColor = array.getColor(R.styleable.NotationView_textColorBack, 0xFF00FF00);
			}
			if (array.hasValue(R.styleable.NotationView_textColorSelected)) {
				textColorSelected = array.getColor(R.styleable.NotationView_textColorSelected, 0xFF00FF00);
			}
		} finally {
			array.recycle();
		}

		LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		viewPager = new ViewPager(context);
		viewPager.setId(R.id.viewPager);
		addView(viewPager, params);

		notationsAdapter = new NotationsPagerAdapter();
		viewPager.setAdapter(notationsAdapter);
		viewPager.setOnPageChangeListener(this);
		boolean smallScreen = AppUtils.noNeedTitleBar(context);

		textPadding = resources.getDimensionPixelSize(R.dimen.notations_text_padding);
		textSidePadding = resources.getDimensionPixelSize(R.dimen.notations_text_side_padding);
		screenPadding = (int) (2 * density);
		if (smallScreen) {
			screenPadding = 1; // mdpi = 1
		}
		setPadding(0, screenPadding, 0, screenPadding);

		notationTextParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		notationTextParams.gravity = Gravity.CENTER_VERTICAL;

		if (attrs != null) {
			ButtonDrawableBuilder.setBackgroundToView(this, attrs);
		}

		smallScreen = AppUtils.noNeedTitleBar(context);
		if (smallScreen) {
			setVisibility(GONE);
		}
		smallScreen = AppUtils.isHdpi800(context);
		if (smallScreen) {
			setVisibility(GONE);
		}
		smallScreen = AppUtils.isNexus4Kind(context) && !appData.isFullScreen();
		if (smallScreen) {
			setVisibility(GONE);
		}

		clickable = true;
	}

	@Override
	protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
		super.onSizeChanged(xNew, yNew, xOld, yOld);
		if (xNew != 0) {
			viewWidth = xNew;

			if (originalNotations == null) {
				return;
			}

			calculateViewsPerPage();
			if (notationsAdapter != null) {
				notationsAdapter.updateSelection();
				notationsAdapter.notifyDataSetChanged();
			}

			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					if (notationsAdapter != null) {
						notationsAdapter.notifyDataSetChanged();
						notationsAdapter.selectItem(notationsAdapter.selectedPosition);
					}
				}
			}, 100);
		}
	}

	@Override
	public void updateNotations(String[] notations, BoardForNotationFace selectionFace, int ply) {
		this.selectionFace = selectionFace;

		newNotations = false;
		if (originalNotations != null) {
			// if notations array was replaced with different notations
			for (int i = 0; i < originalNotations.length; i++) {
				String notation = originalNotations[i];
				if (notations.length > i) {
					// if stored notation do not equals to notation from new array
					if (!notation.equals(notations[i])) {
						newNotations = true;
					}
				}
			}
		}

		if (newNotations || originalNotations == null || originalNotations.length < notations.length) {
			newNotations = true;
			originalNotations = notations;
		}

		calculateViewsPerPage();

		if (notationsAdapter == null || newNotations) { // TODO invent logic to just invalidate adapter
			notationsAdapter = new NotationsPagerAdapter();
			viewPager.setAdapter(notationsAdapter);
		} else {
			notationsAdapter.notifyDataSetChanged();
		}

		highlightPosition(ply - 1);
	}

	private void calculateViewsPerPage() {
		if (viewWidth == 0) {
			return;
		}
		// we need to know width of each view and distribute it evenly
		List<Integer> notationWidthList = new ArrayList<Integer>();
		int page = 0;
		int viewsPerPage = 0;

		for (int i = 0; i < originalNotations.length; i++) {
			String notation = setNumberToNotation(originalNotations[i], i);

			int notationStrWidth = (int) (notationTxtPaint.measureText(notation) * density);
//			Log.d("TEST", " notation = " + notation + " notationStrWidth = " + notationStrWidth);
			int notationViewWidth = notationStrWidth + textSidePadding * 2;

			notationWidthList.add(notationViewWidth);

			float totalWidthsPerPage = 0;
			viewsPerPage = 0;
			for (Integer notationViewWidth1 : notationWidthList) {
				viewsPerPage++;
				totalWidthsPerPage += notationViewWidth1;
			}

			// add NotationView padding
			totalWidthsPerPage += screenPadding * 2;

//			Log.d("TEST"," totalWidthsPerPage " + totalWidthsPerPage);
			int nextNotationViewWidth;
			if (i + 1 < originalNotations.length) {

				String nextNotation = setNumberToNotation(originalNotations[i + 1], i + 1);

				int nextNotationStrWidth = (int) (notationTxtPaint.measureText(nextNotation) * density);
				nextNotationViewWidth = nextNotationStrWidth + textSidePadding * 2;
//				Log.d("TEST", "viewWidth - totalWidthsPerPage = " + (viewWidth - totalWidthsPerPage)
//						+ " nextNotationViewWidth = " + nextNotationViewWidth + " value = " + nextNotation);
			} else {
				nextNotationViewWidth = 0;
			}

			if (viewWidth - totalWidthsPerPage < nextNotationViewWidth) {
//				Log.d("TEST", "previous notation = " + originalNotations[i - 1] );
//				Log.d("TEST", "last notation = " + notation);
//				Log.d("TEST", "next notation = " + originalNotations[i + 1]);
				viewPerPageMap.put(page, viewsPerPage);
//				Log.d("TEST", " viewsPerPage = " + viewsPerPage);
				page++;
				notationWidthList.clear();
			}
		}

		// add last views that are less than viewWidth
		viewPerPageMap.put(page, viewsPerPage);
	}

	private String setNumberToNotation(String notation, int position) {
		if (position % 2 == 0) {
			int number = (position) / 2 + 1;
			notation = number + Symbol.DOT + notation;
		}
		return notation;
	}

	@Override
	public void onClick(View v) {

		if (!clickable) {
			return;
		}

		Integer pos = (Integer) v.getTag(R.id.list_item_id);
		highlightPosition(pos);

		selectionFace.onClick(v);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

	@Override
	public void onPageSelected(int position) {
		selectionFace.updateParent();
	}

	@Override
	public void onPageScrollStateChanged(int state) {}

	private class NotationsPagerAdapter extends PagerAdapter {
		private int selectedPosition;
		private LinearLayout currentView;

		@Override
		public int getCount() {
			if (originalNotations == null) {
				return 0;
			}

			return viewPerPageMap.size(); // number of pages == number of map entries
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			LinearLayout linearLayout = new LinearLayout(getContext());

			fillNotationsText(position, linearLayout);

			container.addView(linearLayout);

			return linearLayout;
		}

		private void fillNotationsText(int position, LinearLayout linearLayout) {
			int viewPerPage = viewPerPageMap.get(position);

			int absoluteNumber = 0;
			// add all previous numbers
			for (int z = 0; z < position; z++) {
				absoluteNumber += viewPerPageMap.get(z);
			}

			int i;
			for (i = 0; i < viewPerPage; i++) {

				RoboTextView textView = new RoboTextView(getContext());

				textView.setId(NOTATION_ID);
				textView.setTextSize(textSize);
				textView.setTextColor(textColor);
				textView.setOnClickListener(NotationView.this);
				textView.setGravity(Gravity.CENTER);

				int currentPosition = i + absoluteNumber;
				if (currentPosition >= originalNotations.length) {
					break;
				}

				String notation = setNumberToNotation(originalNotations[currentPosition], currentPosition);
				textView.setText(notation);

				if (currentPosition == selectedPosition) {
					textView.setBackgroundResource(R.drawable.button_grey_flat);
					textView.setTextColor(textColorSelected);
				} else {
					textView.setTextColor(textColor);
					textView.setBackgroundDrawable(null);
				}

				textView.setTag(R.id.list_item_id, currentPosition);
				textView.setPadding(textSidePadding, textPadding, textSidePadding, textPadding);

				linearLayout.addView(textView, notationTextParams);
			}
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object view) {
			container.removeView((View) view);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public void setPrimaryItem(ViewGroup container, int position, Object object) {
			super.setPrimaryItem(container, position, object);
			currentView = (LinearLayout) object;
			updateSelection();
		}

		public void selectItem(int pos) {
			selectedPosition = pos;
			if (currentView != null) {
				int childCount = currentView.getChildCount();

				int absoluteNumber = getAbsoluteNumberForPosition();
				for (int i = 0; i < childCount; i++) {
					int currentPosition = i + absoluteNumber;
					RoboTextView textView = (RoboTextView) currentView.getChildAt(i);

					if (currentPosition < originalNotations.length) {
						String notation = originalNotations[currentPosition];

						if (currentPosition % 2 == 0) {
							int number = (currentPosition) / 2 + 1;
							notation = number + Symbol.DOT + notation;
						}

						textView.setText(notation);
						textView.setTag(R.id.list_item_id, currentPosition);
						textView.setOnClickListener(NotationView.this);
					} else if (newNotations) {
						textView.setText(Symbol.SPACE);
					}

					if (currentPosition == selectedPosition) {
						textView.setBackgroundResource(R.drawable.button_grey_flat);
						textView.setTextColor(textColorSelected);
					} else {
						textView.setTextColor(textColor);
						textView.setBackgroundDrawable(null);
					}
					textView.setPadding(textSidePadding, textPadding, textSidePadding, textPadding);
				}
			}
		}

		public void updateSelection() {
			if (currentView != null) {
				int childCount = currentView.getChildCount();

				int absoluteNumber = getAbsoluteNumberForPosition();

				for (int i = 0; i < childCount; i++) {
					int currentPosition = i + absoluteNumber;
					RoboTextView textView = (RoboTextView) currentView.getChildAt(i);


					if (currentPosition == selectedPosition) {
						textView.setBackgroundResource(R.drawable.button_grey_flat);
						textView.setTextColor(textColorSelected);
					} else {
						textView.setTextColor(textColor);
						textView.setBackgroundDrawable(null);
					}
					textView.setPadding(textSidePadding, textPadding, textSidePadding, textPadding);
					textView.setOnClickListener(NotationView.this);

				}
				invalidate();
			}
		}
	}

	private int getAbsoluteNumberForPosition() {
		int position = viewPager.getCurrentItem();

		int absoluteNumber = 0;
		// add all previous numbers
		for (int z = 0; z < position; z++) {
			absoluteNumber += viewPerPageMap.get(z);
		}

		return absoluteNumber;
	}

	public void show(boolean show) {
		setVisibility(show ? VISIBLE : GONE);
	}

	@Override
	public void moveBack(int ply) {
		highlightPosition(ply - 1);
	}

	@Override
	public void moveForward(int ply) {
		highlightPosition(ply - 1);
	}

	@Override
	public void rewindBack() {
		viewPager.setCurrentItem(0);
		notationsAdapter.selectItem(0);
	}

	@Override
	public void rewindForward() {
		int totalCnt = originalNotations.length - 1;

		viewPager.setCurrentItem(viewPerPageMap.size());
		notationsAdapter.selectItem(totalCnt);
	}

	private void highlightPosition(int pos) {
		if (pos < 0) {
			return;
		}

		// calculate hit on page manually
		int viewsPerPage = 0;
		int pageToSelect = 0;
		for (int i = 0; i < viewPerPageMap.size(); i++) {

			viewsPerPage += viewPerPageMap.get(i);
			if (pos  < viewsPerPage) {
				break;
			}
			pageToSelect++;
		}

		viewPager.setCurrentItem(pageToSelect);
		notationsAdapter.selectItem(pos);
	}

	@Override
	public void resetNotations() {
		originalNotations = new String[]{};
		notationsAdapter.notifyDataSetChanged();
		newNotations = true;
		notationsAdapter.selectItem(-1);
		viewPager.invalidate();
	}

	@Override
	public void setClickable(boolean clickable) {
		super.setClickable(clickable);
		// todo: rename field and do not override base method?
		this.clickable = clickable;
	}

	public static interface BoardForNotationFace extends OnClickListener {

		void updateParent();
	}
}