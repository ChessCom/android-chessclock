package com.chess.ui.views;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.chess.FontsHelper;
import com.chess.R;
import com.chess.RoboTextView;
import com.chess.backend.statics.StaticData;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;
import com.chess.utilities.AppUtils;


/**
 * GamePanelTestActivity class
 *
 * @author alien_roger
 * @created at: 06.03.12 7:39
 */
public class NotationView extends LinearLayout {

	public static final int NOTATION_ID = 0x00003321;

	private NotationsPagerAdapter notationsAdapter;
	private String[] originalNotations;
	private OnClickListener selectionFace;
	private int textPadding;
	private ViewPager viewPager;
	private boolean newNotations;
	private int viewPerPage;
	public int textSize;
	public int textColor;
	public int textColorSelected;
	private LayoutParams notationTextParams;

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
		Resources resources = context.getResources();
		float density = resources.getDisplayMetrics().density;
		viewPerPage = resources.getInteger(R.integer.notations_per_page);
		textSize = (int) (resources.getDimension(R.dimen.notations_text_size) / density);
		textColor = resources.getColor(R.color.notations_text_color);
		textColorSelected = resources.getColor(R.color.notations_text_color_selected);


		LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		viewPager = new ViewPager(context);
		viewPager.setId(R.id.viewPager);
		addView(viewPager, params);

		notationsAdapter = new NotationsPagerAdapter();
		viewPager.setAdapter(notationsAdapter);
		boolean smallScreen = AppUtils.noNeedTitleBar(context);

		int padding = (int) (2 * density);
		if (smallScreen) {
			textPadding = (int) (2 * density);
			padding = 1; // mdpi = 1
		} else {
			textPadding = (int) (2 * density);
		}
		setPadding(0, padding, 0, padding);

		notationTextParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		notationTextParams.weight = 1;
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
		smallScreen = AppUtils.hasSoftKeys(((Activity)context).getWindowManager());
		if (smallScreen) {
			setVisibility(GONE);
		}

	}

	public void updateNotations(String[] notations, OnClickListener selectionFace, int hply) {
		this.selectionFace = selectionFace;

		newNotations = false;
		if (originalNotations != null) {
			for (int i = 0; i < originalNotations.length; i++) {
				String notation = originalNotations[i];
				if (notations.length > i) {
					if (!notation.equals(notations[i])) {
						newNotations = true;
					}
				}
			}
		}

		if (newNotations || originalNotations == null || originalNotations.length < notations.length) {
			originalNotations = notations;
		}

		if (notationsAdapter == null) {
			notationsAdapter = new NotationsPagerAdapter();
			viewPager.setAdapter(notationsAdapter);
		} else {
			notationsAdapter.notifyDataSetChanged();
		}
		highlightPosition(hply - 1);
	}

	private class NotationsPagerAdapter extends PagerAdapter {
		private int selectedPosition;
		private LinearLayout currentView;

		@Override
		public int getCount() {
			if (originalNotations == null) {
				return 0;
			}
			return (int) Math.ceil((originalNotations.length / (float) viewPerPage));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			LinearLayout linearLayout = new LinearLayout(getContext());

			fillNotationsText(position, linearLayout);

			container.addView(linearLayout);

			return linearLayout;
		}

		private void fillNotationsText(int position, LinearLayout linearLayout) {
			int absoluteNumber = viewPerPage * position;
			int i;
			for (i = 0; i < viewPerPage; i++) {

				RoboTextView textView = new RoboTextView(getContext());

				textView.setId(NOTATION_ID);
				textView.setTextSize(textSize);
				textView.setTextColor(textColor);
				textView.setFont(FontsHelper.HELV_NEUE_FONT);
				textView.setOnClickListener(mOnButtonClicked);
				textView.setGravity(Gravity.CENTER);

				int currentPosition = i + absoluteNumber;
				if (currentPosition >= originalNotations.length) {
					break;
				}

				String notation = originalNotations[currentPosition];
				if (currentPosition % 2 == 0) {
					int number = (currentPosition) / 2 + 1;
					notation = number + StaticData.SYMBOL_DOT + notation;
				}

				textView.setText(notation);

				if (currentPosition == selectedPosition) {
					textView.setBackgroundResource(R.drawable.button_grey_flat);
					textView.setTextColor(textColorSelected);
				} else {
					textView.setTextColor(textColor);
					textView.setBackgroundDrawable(null);
				}

				textView.setTag(R.id.list_item_id, currentPosition);
				textView.setPadding(textPadding, textPadding, textPadding, textPadding);

				linearLayout.addView(textView, notationTextParams);
			}

			while (i <= viewPerPage) { // stubs
				RoboTextView textView = new RoboTextView(getContext());

				textView.setId(NOTATION_ID);
				textView.setText(StaticData.SYMBOL_SPACE);
				textView.setTextSize(textSize);
				textView.setFont(FontsHelper.HELV_NEUE_FONT);
				textView.setPadding(textPadding, textPadding, textPadding, textPadding);
				textView.setGravity(Gravity.CENTER);

				linearLayout.addView(textView, notationTextParams);
				i++;
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

				int absoluteNumber = viewPerPage * viewPager.getCurrentItem();
				for (int i = 0; i < childCount; i++) {
					int currentPosition = i + absoluteNumber;
					RoboTextView textView = (RoboTextView) currentView.getChildAt(i);

					if (currentPosition < originalNotations.length) {
						String notation = originalNotations[currentPosition];

						if (currentPosition % 2 == 0) {
							int number = (currentPosition) / 2 + 1;
							notation = number + StaticData.SYMBOL_DOT + notation;
						}

						textView.setText(notation);
						textView.setTag(R.id.list_item_id, currentPosition);
						textView.setOnClickListener(mOnButtonClicked);
					} else if (newNotations) {
						textView.setText(StaticData.SYMBOL_SPACE);
					}

					if (currentPosition == selectedPosition) {
						textView.setBackgroundResource(R.drawable.button_grey_flat);
						textView.setTextColor(textColorSelected);
					} else {
						textView.setTextColor(textColor);
						textView.setBackgroundDrawable(null);
					}
					textView.setPadding(textPadding, textPadding, textPadding, textPadding);
				}
			}
		}

		public void updateSelection() {
			if (currentView != null) {
				int childCount = currentView.getChildCount();

				int absoluteNumber = viewPerPage * viewPager.getCurrentItem();

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
					textView.setPadding(textPadding, textPadding, textPadding, textPadding);
				}
				invalidate();
			}
		}
	}


	public void show(boolean show) {
		setVisibility(show ? VISIBLE : GONE);
	}

	public void moveBack(int hply) {
		highlightPosition(hply - 1);
	}

	public void moveForward(int hply) {
		highlightPosition(hply - 1);
	}

	private OnClickListener mOnButtonClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Integer pos = (Integer) v.getTag(R.id.list_item_id);
			highlightPosition(pos);

			selectionFace.onClick(v);
		}
	};

	private void highlightPosition(int pos) {
		if (pos < 0) {
			return;
		}
		viewPager.setCurrentItem(pos / viewPerPage);
		notationsAdapter.selectItem(pos);
	}

	public void resetNotations() {
		originalNotations = new String[]{};
		notationsAdapter.notifyDataSetChanged();
		newNotations = true;
		notationsAdapter.selectItem(-1);
		viewPager.invalidate();
	}

}