package com.chess.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 02.10.13
 * Time: 10:18
 */
public class ControlledListView extends ListView {

	private boolean scrollingEnabled = true;

	public ControlledListView(Context context) {
		super(context);
	}

	public ControlledListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		return scrollingEnabled && super.onInterceptTouchEvent(event);
	}

	public boolean isScrollingEnabled() {
		return scrollingEnabled;
	}

	public void setScrollingEnabled(boolean scrollingEnabled) {
		this.scrollingEnabled = scrollingEnabled;
	}
}
