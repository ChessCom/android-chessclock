package com.chess.clock.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckedTextView;

import com.chess.clock.R;

/**
 * Custom CheckedTextView making text color also sensitive to checked state.
 */
public class TimeControlCheckedTextView extends CheckedTextView {

	public TimeControlCheckedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void setChecked(boolean checked) {
		super.setChecked(checked);

		this.setTextColor(checked ?
				getResources().getColor(R.color.list_item_text_color_checked) :
				getResources().getColor(R.color.list_item_text_color_normal));
	}
}
