package com.chess.clock.viewgroup;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chess.clock.R;

public class CheckableLinearLayout extends LinearLayout implements Checkable {

	private static final int CHECKABLE_CHILD_INDEX = 0;
	private Checkable child;
	private TextView labelChild;

	public CheckableLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		child = (Checkable) getChildAt(CHECKABLE_CHILD_INDEX);
		labelChild = (TextView) getChildAt(1);
	}

	@Override
	public boolean isChecked() {
		return child.isChecked();
	}

	@Override
	public void setChecked(boolean checked) {
		child.setChecked(checked);
		labelChild.setTextColor(checked ?
				getResources().getColor(R.color.list_item_text_color_checked) :
				getResources().getColor(R.color.list_item_text_color_normal));
	}

	@Override
	public void toggle() {
		child.toggle();
	}
}