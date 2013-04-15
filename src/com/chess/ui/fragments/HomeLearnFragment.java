package com.chess.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.ui.fragments.game.GameTacticsFragment;

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

		avgScoreValueTxt = (TextView) view.findViewById(R.id.avgScoreValueTxt);
		todaysAttemptsValueTxt = (TextView) view.findViewById(R.id.todaysAttemptsValueTxt);
	}

	@Override
	public void onStart() {
		super.onStart();

		// load ratings

		// load latest video
//		loadedVideoId
//		new LoadDataFromDbTask()
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
			getActivityFace().openFragment(VideoDetailsFragment.newInstance(loadedVideoId)); // TODO
		}
	}
}
