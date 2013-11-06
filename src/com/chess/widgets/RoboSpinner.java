package com.chess.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Spinner;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 22.05.13
 * Time: 8:29
 */
public class RoboSpinner extends Spinner {

	public RoboSpinner(Context context) {
		super(context);
	}

	public RoboSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public RoboSpinner(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		ButtonDrawableBuilder.setBackgroundToView(this, attrs);
	}
}
