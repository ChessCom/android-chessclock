package com.chess.ui.fragments.live;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.ui.fragments.CommonLogicFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 26.04.13
 * Time: 10:00
 */
public class LiveGameOptionsFragment extends CommonLogicFragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_game_option_live_view, container, false);
	}
}
