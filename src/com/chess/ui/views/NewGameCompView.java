package com.chess.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import com.chess.R;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 14.01.13
 * Time: 12:23
 */
public class NewGameCompView extends NewGameDefaultView {

	public NewGameCompView(Context context) {
		super(context);
	}

	public NewGameCompView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NewGameCompView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public void toggleOptions() {
		super.toggleOptions();

		int expandVisibility = optionsVisible? VISIBLE: GONE;

		optionsView.setVisibility(expandVisibility);

		if(optionsVisible) {
			compactRelLay.setBackgroundResource(R.drawable.game_option_back_1);
		} else {
			compactRelLay.setBackgroundResource(R.drawable.nav_menu_item_selected);
		}
		compactRelLay.setPadding(COMPACT_PADDING, 0, COMPACT_PADDING, COMPACT_PADDING);
	}


	@Override
	public void addOptionsView() {
		optionsView = LayoutInflater.from(getContext()).inflate(R.layout.new_game_option_comp_view, null, false);
		optionsView.setVisibility(GONE);
		addView(optionsView);

	}

}
