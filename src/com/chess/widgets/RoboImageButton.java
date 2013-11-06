package com.chess.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageButton;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 24.05.13
 * Time: 22:08
 */
public class RoboImageButton extends ImageButton {

	public RoboImageButton(Context context) {
		super(context);
	}

	public RoboImageButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	public RoboImageButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		ButtonDrawableBuilder.setBackgroundToView(this, attrs);
	}
}
