package com.chess.ui.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.chess.R;
import com.chess.ui.views.drawables.PieChartDrawable;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.02.13
 * Time: 19:16
 */
public class StatsGameTacticsFragment extends CommonLogicFragment {

	private View graphView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_stats_tactics_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		graphView = view.findViewById(R.id.graphView);

		Drawable drawable = new PieChartDrawable(getActivity());

		graphView.setBackgroundDrawable(drawable);
	}
}
