package com.chess.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioGroup;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.11.13
 * Time: 19:37
 */
public class RoboRadioGroup extends RadioGroup {

	public RoboRadioGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		ButtonDrawableBuilder.setBackgroundToView(this, attrs);
	}

}
