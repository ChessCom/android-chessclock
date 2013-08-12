package com.chess.ui.fragments.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.stats.TacticsBasicStatsItem;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.game.GameTacticsFragment;
import com.chess.ui.fragments.lessons.LessonsFragment;
import com.chess.ui.fragments.stats.TacticsStatsFragment;
import com.chess.ui.fragments.videos.VideoDetailsFragment;
import com.chess.ui.fragments.videos.VideosFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 09.04.13
 * Time: 5:43
 */
public class HomeLearnFragment extends CommonLogicFragment {

	private TextView tacticsRatingTxt;
	private TextView lessonsRatingTxt;
	private TextView avgScoreValueTxt;
	private TextView todaysAttemptsValueTxt;
	private long loadedVideoId = 0;
	private int currentTacticsRating;
	private int tacitcsTodaysAttempts;
	private int todaysAverageScore;
	private boolean need2update = true;
	private StatsUpdateListener statsUpdateListener;
	private View todaysAttemptsLabelTxt;
	private View avgScoreLabelTxt;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		statsUpdateListener = new StatsUpdateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_home_learn_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		view.findViewById(R.id.tacticsHeaderView).setOnClickListener(this);
		view.findViewById(R.id.lessonsHeaderView).setOnClickListener(this);
		view.findViewById(R.id.videosHeaderView).setOnClickListener(this);
		view.findViewById(R.id.startTacticsBtn).setOnClickListener(this);
		view.findViewById(R.id.startLessonsBtn).setOnClickListener(this);
		view.findViewById(R.id.videoThumbItemView).setOnClickListener(this);

		tacticsRatingTxt = (TextView) view.findViewById(R.id.tacticsRatingTxt);
		lessonsRatingTxt = (TextView) view.findViewById(R.id.lessonsRatingTxt);

		todaysAttemptsLabelTxt = view.findViewById(R.id.todaysAttemptsLabelTxt);
		avgScoreLabelTxt = view.findViewById(R.id.avgScoreLabelTxt);
		avgScoreValueTxt = (TextView) view.findViewById(R.id.avgScoreValueTxt);
		todaysAttemptsValueTxt = (TextView) view.findViewById(R.id.todaysAttemptsValueTxt);
	}

	@Override
	public void onResume() {
		super.onResume();

		// load latest video
//		loadedVideoId
//		new LoadDataFromDbTask()
		// TODO request stats only for premium user
		if (getAppData().getUserPremiumStatus() > StaticData.BASIC_USER) {
			LoadItem loadItem = LoadHelper.getTacticsBasicStats(getUserToken());
			new RequestJsonTask<TacticsBasicStatsItem>(statsUpdateListener).executeTask(loadItem);
		} else {
			todaysAttemptsLabelTxt.setVisibility(View.GONE);
			avgScoreLabelTxt.setVisibility(View.GONE);
			avgScoreValueTxt.setVisibility(View.GONE);
			todaysAttemptsValueTxt.setVisibility(View.GONE);
		}
	}

	private class StatsUpdateListener extends ChessUpdateListener<TacticsBasicStatsItem> {

		public StatsUpdateListener() {
			super(TacticsBasicStatsItem.class);
		}

		@Override
		public void updateData(TacticsBasicStatsItem returnedObj) {
			super.updateData(returnedObj);

			currentTacticsRating = returnedObj.getData().getCurrent();
			tacitcsTodaysAttempts = returnedObj.getData().getTodaysAttempts();
			todaysAverageScore = returnedObj.getData().getTodaysAverageScore();

			tacticsRatingTxt.setText(String.valueOf(currentTacticsRating));
			todaysAttemptsValueTxt.setText(String.valueOf(tacitcsTodaysAttempts));
			avgScoreValueTxt.setText(String.valueOf(todaysAverageScore));

			need2update = false;
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		int id = view.getId();
		if (id == R.id.tacticsHeaderView) {
			getActivityFace().openFragment(new TacticsStatsFragment());
		} else if (id == R.id.lessonsHeaderView) {
			getActivityFace().openFragment(new LessonsFragment());
		} else if (id == R.id.videosHeaderView) {
			getActivityFace().openFragment(new VideosFragment());
		} else if (id == R.id.startTacticsBtn) {
			getActivityFace().openFragment(new GameTacticsFragment());
		} else if (id == R.id.startLessonsBtn) {
			getActivityFace().openFragment(new LessonsFragment());
		} else if (id == R.id.videoThumbItemView) {
			getActivityFace().openFragment(VideoDetailsFragment.createInstance(loadedVideoId)); // TODO
		}
	}
}
