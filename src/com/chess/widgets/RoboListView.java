package com.chess.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;
import com.chess.ui.views.drawables.smart_button.ButtonDrawableBuilder;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 19.11.13
 * Time: 6:26
 */
public class RoboListView extends ListView {

	public RoboListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		ButtonDrawableBuilder.setBackgroundToView(this, attrs);
	}
}
