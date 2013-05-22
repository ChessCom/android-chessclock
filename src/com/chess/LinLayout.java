package com.chess;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 20.05.13
 * Time: 23:04
 */
public class LinLayout extends LinearLayout {

	public LinLayout(Context context) {
		super(context);
	}

	public LinLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		ButtonDrawableBuilder.setBackgroundToView(this, attrs);
	}

}
