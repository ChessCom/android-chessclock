package com.chess.ui.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.StatsItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.activities.CoreActivityActionBar;
import com.chess.ui.views.drawables.PieChartDrawable;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 23.01.13
 * Time: 10:37
 */
public class StatsFragment extends CommonLogicFragment {

	private ActionBarUpdateListener<StatsItem> statsItemUpdateListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_stats_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		LinearLayout ratingsLinearView = (LinearLayout) view.findViewById(R.id.ratingsLinearView);

		addRatingsViews(ratingsLinearView);


	}

	private void addRatingsViews(LinearLayout ratingsLinearView) {
		LayoutInflater inflater = LayoutInflater.from(getActivity());

		{// Highest Rating
			RelativeLayout hihghestRatingView = (RelativeLayout) inflater.inflate(R.layout.new_stats_rating_item_view, null, false);

//			ratingsLinearView.addView();

		}



	}

	@Override
	public void onStart() {
		super.onStart();

		updateData();
	}

	private void init() {
		statsItemUpdateListener = new StatsItemUpdateListener();

	}

	private void updateData() {
		// get full stats
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_STATS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));

		new RequestJsonTask<StatsItem>(statsItemUpdateListener).executeTask(loadItem);
	}

	private class StatsItemUpdateListener extends ActionBarUpdateListener<StatsItem> {

		public StatsItemUpdateListener() {
			super(getInstance(), StatsItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			// TODO -> File | Settings | File Templates.
		}

		@Override
		public void updateData(StatsItem returnedObj) {
			super.updateData(returnedObj);
			// TODO -> File | Settings | File Templates.
		}
	}
}
