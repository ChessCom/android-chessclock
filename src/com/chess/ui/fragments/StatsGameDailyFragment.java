package com.chess.ui.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 23.01.13
 * Time: 10:37
 */
public class StatsGameDailyFragment extends CommonLogicFragment {

	private static final String TAG = "StatsGameDailyFragment";

	public static final int HIGHEST_ID = 0x00002000;
	public static final int LOWEST_ID = 0x00002100;
	public static final int AVERAGE_ID = 0x00002200;
	public static final int BEST_WIN_ID = 0x00002300;

	public static final int RATING_TIMESTAMP_ID = 0x00000001;
	public static final int RATING_VALUE_ID = 0x00000002;
	// 05/27/08
	private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy");

	private final static String MODE = "mode";

	private final static int LIVE_STANDARD = 0;
	private final static int LIVE_BLITZ = 1;
	private final static int LIVE_LIGHTNING = 2;


	private LiveDataCursorUpdateListener liveStandardCursorUpdateListener;
	private LiveDataCursorUpdateListener liveLightningCursorUpdateListener;
	private LiveDataCursorUpdateListener liveBlitzCursorUpdateListener;

	public static StatsGameDailyFragment newInstance(int code) {
		StatsGameDailyFragment frag = new StatsGameDailyFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, code);

		frag.setArguments(bundle);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_stats_live_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		LinearLayout ratingsLinearView = (LinearLayout) view.findViewById(R.id.ratingsLinearView);

		addRatingsViews(ratingsLinearView);
	}

	private void addRatingsViews(LinearLayout ratingsLinearView) {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		// set id's to view for further set data to them
		{// Highest Rating
			RelativeLayout highestRatingView = (RelativeLayout) inflater.inflate(R.layout.new_stats_rating_item_view, null, false);
			((TextView) highestRatingView.findViewById(R.id.ratingLabelTxt)).setText(R.string.highest_rating);
			highestRatingView.findViewById(R.id.subtitleTxt).setId(HIGHEST_ID + RATING_TIMESTAMP_ID);
			highestRatingView.findViewById(R.id.ratingValueTxt).setId(HIGHEST_ID + RATING_VALUE_ID);

			ratingsLinearView.addView(highestRatingView);
		}

		{// Lowest Rating
			RelativeLayout highestRatingView = (RelativeLayout) inflater.inflate(R.layout.new_stats_rating_item_view, null, false);
			((TextView) highestRatingView.findViewById(R.id.ratingLabelTxt)).setText(R.string.lowest_rating);
			highestRatingView.findViewById(R.id.subtitleTxt).setId(LOWEST_ID + RATING_TIMESTAMP_ID);
			highestRatingView.findViewById(R.id.ratingValueTxt).setId(LOWEST_ID + RATING_VALUE_ID);

			ratingsLinearView.addView(highestRatingView);
		}

		{// Average Opponent Rating
			RelativeLayout highestRatingView = (RelativeLayout) inflater.inflate(R.layout.new_stats_rating_item_view, null, false);
			((TextView) highestRatingView.findViewById(R.id.ratingLabelTxt)).setText(R.string.avg_opponent_rating);
			highestRatingView.findViewById(R.id.subtitleTxt).setId(AVERAGE_ID + RATING_TIMESTAMP_ID);
			highestRatingView.findViewById(R.id.ratingValueTxt).setId(AVERAGE_ID + RATING_VALUE_ID);

			ratingsLinearView.addView(highestRatingView);
		}

		{// Best Win Rating
			RelativeLayout highestRatingView = (RelativeLayout) inflater.inflate(R.layout.new_stats_rating_item_view, null, false);
			((TextView) highestRatingView.findViewById(R.id.ratingLabelTxt)).setText(R.string.best_win_rating);
			highestRatingView.findViewById(R.id.subtitleTxt).setId(BEST_WIN_ID + RATING_TIMESTAMP_ID);
			highestRatingView.findViewById(R.id.ratingValueTxt).setId(BEST_WIN_ID + RATING_VALUE_ID);

			ratingsLinearView.addView(highestRatingView);
		}

	}

	@Override
	public void onStart() {
		super.onStart();

		updateData();
	}

	private void init() {
		liveStandardCursorUpdateListener = new LiveDataCursorUpdateListener(LIVE_STANDARD);
		liveLightningCursorUpdateListener = new LiveDataCursorUpdateListener(LIVE_LIGHTNING);
		liveBlitzCursorUpdateListener = new LiveDataCursorUpdateListener(LIVE_BLITZ);
	}

	private void updateData() {
		String userName = AppData.getUserName(getActivity());

//		switch (getArguments().getInt(MODE)){ // TODO restore
//			case LIVE_STANDARD:
//				new LoadDataFromDbTask(liveStandardCursorUpdateListener, DbHelper.getStatsUserLiveStandardParams(userName), getContentResolver()).executeTask();
//				break;
//			case LIVE_LIGHTNING:
//				new LoadDataFromDbTask(liveLightningCursorUpdateListener, DbHelper.getStatsUserLiveLightningParams(userName), getContentResolver()).executeTask();
//				break;
//			case LIVE_BLITZ:
//				new LoadDataFromDbTask(liveBlitzCursorUpdateListener, DbHelper.getStatsUserLiveBlitzParams(userName), getContentResolver()).executeTask();
//				break;
//		}
	}

	private class LiveDataCursorUpdateListener extends ActionBarUpdateListener<Cursor> {

		private int listenerCode;

		public LiveDataCursorUpdateListener(int listenerCode) {
			super(getInstance());
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			switch (listenerCode) {
				case LIVE_STANDARD:
					fillLiveStandard(returnedObj);
					break;
				case LIVE_LIGHTNING:
					fillLiveLightning(returnedObj);
					break;
				case LIVE_BLITZ:
					fillLiveBlitz(returnedObj);
					break;
			}

		}
	}

	private void fillLiveStandard(Cursor cursor) {

		int liveHighestRating = DBDataManager.getInt(cursor, DBConstants.V_HIGHEST_RATING);
		long liveHighestRatingTime = DBDataManager.getLong(cursor, DBConstants.V_HIGHEST_RATING);

		((TextView) getView().findViewById(HIGHEST_ID + RATING_VALUE_ID)).setText(liveHighestRating);
		((TextView) getView().findViewById(HIGHEST_ID + RATING_TIMESTAMP_ID)).setText(dateFormatter.format(new Date(liveHighestRatingTime)));

	}

	private void fillLiveLightning(Cursor cursor) {

		int liveHighestRating = DBDataManager.getInt(cursor, DBConstants.V_HIGHEST_RATING);
		long liveHighestRatingTime = DBDataManager.getLong(cursor, DBConstants.V_HIGHEST_RATING);

		((TextView) getView().findViewById(HIGHEST_ID + RATING_VALUE_ID)).setText(liveHighestRating);
		((TextView) getView().findViewById(HIGHEST_ID + RATING_TIMESTAMP_ID)).setText(dateFormatter.format(new Date(liveHighestRatingTime)));

	}

	private void fillLiveBlitz(Cursor cursor) {

		int liveHighestRating = DBDataManager.getInt(cursor, DBConstants.V_HIGHEST_RATING);
		long liveHighestRatingTime = DBDataManager.getLong(cursor, DBConstants.V_HIGHEST_RATING);

		((TextView) getView().findViewById(HIGHEST_ID + RATING_VALUE_ID)).setText(liveHighestRating);
		((TextView) getView().findViewById(HIGHEST_ID + RATING_TIMESTAMP_ID)).setText(dateFormatter.format(new Date(liveHighestRatingTime)));

	}



}
