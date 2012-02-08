package com.chess.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import com.chess.R;

/**
 * Custom layout that arranges children in a grid-like manner, optimizing for
 * even horizontal and vertical whitespace.
 */
public class DashBoardLayout2 extends RelativeLayout {

	private static final int UNEVEN_GRID_PENALTY_MULTIPLIER = 10;

	private int mMaxChildWidth = 0;
	private int mMaxChildHeight = 0;
	private int[] backgrounds;

	public DashBoardLayout2(Context context) {
		super(context, null);
		init();
	}

	public DashBoardLayout2(Context context, AttributeSet attrs) {
		super(context, attrs, 0);
		init();
	}

	public DashBoardLayout2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init(){
		backgrounds = new int[]{
				R.drawable.dashboard_item_lt,
				R.drawable.dashboard_item_rt,
				R.drawable.dashboard_item_lm,
				R.drawable.dashboard_item_rm,
				R.drawable.dashboard_item_lb,
				R.drawable.dashboard_item_rb
		};

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mMaxChildWidth = 0;
		mMaxChildHeight = 0;

		// Measure once to find the maximum child size.

		int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec),
				MeasureSpec.AT_MOST);
		int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec),
				MeasureSpec.AT_MOST);

		final int count = getChildCount();
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() == GONE) {
				continue;
			}

			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

			mMaxChildWidth = Math.max(mMaxChildWidth, child.getMeasuredWidth());
			mMaxChildHeight = Math.max(mMaxChildHeight, child.getMeasuredHeight());
		}

		// Measure again for each child to be exactly the same size.

//		childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxChildWidth, MeasureSpec.EXACTLY);
//		childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxChildHeight, MeasureSpec.EXACTLY);
		childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxChildWidth, MeasureSpec.AT_MOST);
		childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxChildHeight, MeasureSpec.AT_MOST);

		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() == GONE) {
				continue;
			}

			child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
		}

		setMeasuredDimension(resolveSize(mMaxChildWidth, widthMeasureSpec),
				resolveSize(mMaxChildHeight, heightMeasureSpec));
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int width = r - l;
		int height = b - t;

		final int count = getChildCount();

		// Calculate the number of visible children.
		int visibleCount = 0;
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() == GONE) {
				continue;
			}
			++visibleCount;
		}

		if (visibleCount == 0) {
			return;
		}

		// Calculate what number of rows and columns will optimize for even
// horizontal and
		// vertical whitespace between items. Start with a 1 x N grid, then try
// 2 x N, and so on.
		int bestSpaceDifference = Integer.MAX_VALUE;
		int spaceDifference;

		// Horizontal and vertical space between items
		int hSpace = 0;
		int vSpace = 0;

		int cols = 1;
		int rows;

		while (true) {
			rows = (visibleCount - 1) / cols + 1;

			hSpace = ((width - mMaxChildWidth * cols) / (cols + 1));
			vSpace = ((height - mMaxChildHeight * rows) / (rows + 1));

			spaceDifference = Math.abs(vSpace - hSpace);
			if (rows * cols != visibleCount) {
				spaceDifference *= UNEVEN_GRID_PENALTY_MULTIPLIER;
			}

			if (spaceDifference < bestSpaceDifference) {
				// Found a better whitespace squareness/ratio
				bestSpaceDifference = spaceDifference;

				// If we found a better whitespace squareness and there's only 1
// row, this is
				// the best we can do.
				if (rows == 1) {
					break;
				}
			} else {
				// This is a worse whitespace ratio, use the previous value of
// cols and exit.
				--cols;
				rows = (visibleCount - 1) / cols + 1;
				hSpace = ((width - mMaxChildWidth * cols) / (cols + 1));
				vSpace = ((height - mMaxChildHeight * rows) / (rows + 1));

				break;
			}

			++cols;
		}

		// Lay out children based on calculated best-fit number of rows and
// cols.

		// If we chose a layout that has negative horizontal or vertical space,
// force it to zero.
		hSpace = Math.max(0, hSpace);
		vSpace = Math.max(0, vSpace);

//		// Re-use width/height variables to be child width/height.
//		width = (width - hSpace * (cols + 1)) / cols;
//		height = (height - vSpace * (rows + 1)) / rows;
		// set max cell width and height
		width = width / cols;
		height = height / rows;

		int left, top, right, bottom;
		int col, row;
		int visibleIndex = 0;
		for (int i = 0; i < count; i++) {
			final View child = getChildAt(i);
			if (child.getVisibility() == GONE) {
				continue;
			}

			row = visibleIndex / cols;
			col = visibleIndex % cols;

//			left = hSpace * (col + 1) + width * col;
//			top = vSpace * (row + 1) + height * row;
			left = width * col;
			top = height * row;

			right = (hSpace == 0 && col == cols - 1) ? r : (left + width);
			bottom = (vSpace == 0 && row == rows - 1) ? b : (top + height);

//			right = (hSpace == 0 && col == cols - 1) ? r : (left + width);
//			bottom = (vSpace == 0 && row == rows - 1) ? b : (top + height);

			child.layout(left, top, right, bottom);
			child.setBackgroundResource(backgrounds[visibleIndex]);
			++visibleIndex;
		}
	}
}
