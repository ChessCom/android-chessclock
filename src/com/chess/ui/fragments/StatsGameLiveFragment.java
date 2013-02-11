package com.chess.ui.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.ui.views.ChartView;
import com.chess.ui.views.PieView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 23.01.13
 * Time: 10:37
 */
public class StatsGameLiveFragment extends CommonLogicFragment {

	private static final String TAG = "StatsGameFragment";

	public static final int HIGHEST_ID = 0x00002000;
	public static final int LOWEST_ID = 0x00002100;
	public static final int AVERAGE_ID = 0x00002200;
	public static final int BEST_WIN_ID = 0x00002300;

	public static final int RATING_SUBTITLE_ID = 0x00000001;
	public static final int RATING_VALUE_ID = 0x00000002;
	// 05/27/08
	private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy");

	private final static String MODE = "mode";

	private final static int LIVE_STANDARD = 0;
	private final static int LIVE_BLITZ = 1;
	private final static int LIVE_LIGHTNING = 2;
	private static final int CHART_HEIGHT = 400;


	private LiveDataCursorUpdateListener liveStandardCursorUpdateListener;
	private LiveDataCursorUpdateListener liveLightningCursorUpdateListener;
	private LiveDataCursorUpdateListener liveBlitzCursorUpdateListener;
	private TextView winCntValueTxt;
	private TextView loseCntValueTxt;
	private TextView drawCntValueTxt;
	private TextView winningStreakValueTxt;
	private TextView losingStreakValueTxt;
	private TextView currentRatingTxt;
	private TextView absoluteRankTxt;
	private TextView totalRankedTxt;
	private TextView percentileValueTxt;
	private TextView totalGamesValueTxt;
	private PieView pieView;
//	private TextView timeoutsValueTxt;
//	private TextView glickoValueTxt;
//	private TextView mostFrequentOpponentTxt;
//	private TextView mostFrequentOpponentGamesTxt;

	public static StatsGameLiveFragment newInstance(int code) {
		StatsGameLiveFragment frag = new StatsGameLiveFragment();
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

		currentRatingTxt = (TextView) view.findViewById(R.id.currentRatingTxt);

		absoluteRankTxt = (TextView) view.findViewById(R.id.absoluteRankTxt);
		totalRankedTxt = (TextView) view.findViewById(R.id.totalRankedTxt);

		percentileValueTxt = (TextView) view.findViewById(R.id.percentileValueTxt);

		totalGamesValueTxt = (TextView) view.findViewById(R.id.totalGamesValueTxt);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, CHART_HEIGHT);

		pieView = new PieView(getActivity());
		ChartView chartView = new ChartView(getActivity());

		LinearLayout statsLinLay = (LinearLayout) view.findViewById(R.id.statsLinLay);
		statsLinLay.addView(chartView, 1, params);
		statsLinLay.addView(pieView, 3, params);

		LinearLayout ratingsLinearView = (LinearLayout) view.findViewById(R.id.ratingsLinearView);
		addRatingsViews(ratingsLinearView);

		winCntValueTxt = (TextView) view.findViewById(R.id.winCntValueTxt);
		loseCntValueTxt = (TextView) view.findViewById(R.id.loseCntValueTxt);
		drawCntValueTxt = (TextView) view.findViewById(R.id.drawCntValueTxt);

		winningStreakValueTxt = (TextView) view.findViewById(R.id.winningStreakValueTxt);
		losingStreakValueTxt = (TextView) view.findViewById(R.id.losingStreakValueTxt);
//		timeoutsValueTxt = (TextView) view.findViewById(R.id.timeoutsValueTxt);
//		glickoValueTxt = (TextView) view.findViewById(R.id.glickoValueTxt);
//
//		mostFrequentOpponentTxt = (TextView) view.findViewById(R.id.mostFrequentOpponentTxt);
//		mostFrequentOpponentGamesTxt = (TextView) view.findViewById(R.id.mostFrequentOpponentGamesTxt);
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

		switch (getArguments().getInt(MODE)){
			case LIVE_STANDARD:
				new LoadDataFromDbTask(liveStandardCursorUpdateListener, DbHelper.getUserParams(userName, DBConstants.GAME_STATS_LIVE_STANDARD), getContentResolver()).executeTask();
				break;
			case LIVE_LIGHTNING:
				new LoadDataFromDbTask(liveLightningCursorUpdateListener, DbHelper.getUserParams(userName, DBConstants.GAME_STATS_LIVE_LIGHTNING), getContentResolver()).executeTask();
				break;
			case LIVE_BLITZ:
				new LoadDataFromDbTask(liveBlitzCursorUpdateListener, DbHelper.getUserParams(userName, DBConstants.GAME_STATS_LIVE_BLITZ), getContentResolver()).executeTask();
				break;
		}
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

			{ // top info view
				int current = DBDataManager.getInt(returnedObj, DBConstants.V_CURRENT);
				currentRatingTxt.setText(String.valueOf(current));

				int rank = DBDataManager.getInt(returnedObj, DBConstants.V_RANK);
				absoluteRankTxt.setText(String.valueOf(rank));

				int totalPlayers = DBDataManager.getInt(returnedObj, DBConstants.V_TOTAL_PLAYER_COUNT);
				totalRankedTxt.setText(String.valueOf(totalPlayers));

				String percentile = DBDataManager.getString(returnedObj, DBConstants.V_PERCENTILE);
				percentileValueTxt.setText(percentile);
			}

			int totalGamesPlayed = DBDataManager.getInt(returnedObj, DBConstants.V_GAMES_TOTAL);
			totalGamesValueTxt.setText(String.valueOf(totalGamesPlayed));

			fillRatings(returnedObj);

			{// avg opponent rating when i
				int winCnt = DBDataManager.getInt(returnedObj, DBConstants.V_AVG_OPPONENT_RATING_WIN);
				winCntValueTxt.setText(String.valueOf(winCnt));

				int loseCnt = DBDataManager.getInt(returnedObj, DBConstants.V_AVG_OPPONENT_RATING_LOSE);
				loseCntValueTxt.setText(String.valueOf(loseCnt));

				int drawCnt = DBDataManager.getInt(returnedObj, DBConstants.V_AVG_OPPONENT_RATING_DRAW);
				drawCntValueTxt.setText(String.valueOf(drawCnt));
			}

			{// Streaks
				int winCnt = DBDataManager.getInt(returnedObj, DBConstants.V_WINNING_STREAK);
				winningStreakValueTxt.setText(String.valueOf(winCnt));

				int loseCnt = DBDataManager.getInt(returnedObj, DBConstants.V_LOSING_STREAK);
				losingStreakValueTxt.setText(String.valueOf(loseCnt));
			}

			// donut chart
			pieView.setGames(DBDataManager.getGameStatsGamesByResultFromCursor(returnedObj));

//			// timeouts // only for chess gameType
//			int timeouts = DBDataManager.getInt(returnedObj, DBConstants.V_TIMEOUTS);
//			timeoutsValueTxt.setText(String.valueOf(timeouts));
//
//			int glickoRd = DBDataManager.getInt(returnedObj, DBConstants.V_GLICKO_RD);
//			glickoValueTxt.setText(String.valueOf(glickoRd));
//
//			String mostFrequentOpponentName = DBDataManager.getString(returnedObj, DBConstants.V_FREQUENT_OPPONENT_NAME);
//			int mostFrequentOpponentGamesPlayed = DBDataManager.getInt(returnedObj, DBConstants.V_FREQUENT_OPPONENT_GAMES_PLAYED);
//			mostFrequentOpponentTxt.setText(mostFrequentOpponentName);
//			mostFrequentOpponentGamesTxt.setText(String.valueOf(mostFrequentOpponentGamesPlayed));
		}
	}

	private void fillRatings(Cursor cursor) {
		{ // highest
			int rating = DBDataManager.getInt(cursor, DBConstants.V_HIGHEST_RATING);
			long ratingTime = DBDataManager.getLong(cursor, DBConstants.V_HIGHEST_TIMESTAMP) * 1000L;

			((TextView) getView().findViewById(HIGHEST_ID + RATING_VALUE_ID)).setText(String.valueOf(rating));
			((TextView) getView().findViewById(HIGHEST_ID + RATING_SUBTITLE_ID)).setText(dateFormatter.format(new Date(ratingTime)));
		}

		{ // lowest
			int rating = DBDataManager.getInt(cursor, DBConstants.V_LOWEST_RATING);
			long ratingTime = DBDataManager.getLong(cursor, DBConstants.V_LOWEST_TIMESTAMP) * 1000L;

			((TextView) getView().findViewById(LOWEST_ID + RATING_VALUE_ID)).setText(String.valueOf(rating));
			((TextView) getView().findViewById(LOWEST_ID + RATING_SUBTITLE_ID)).setText(dateFormatter.format(new Date(ratingTime)));
		}

		{ // average opponent
			int rating = DBDataManager.getInt(cursor, DBConstants.V_AVERAGE_OPPONENT);

			((TextView) getView().findViewById(AVERAGE_ID + RATING_VALUE_ID)).setText(String.valueOf(rating));
		}

		{ // best win on
			int rating = DBDataManager.getInt(cursor, DBConstants.V_BEST_WIN_RATING);
			String userName = DBDataManager.getString(cursor, DBConstants.V_BEST_WIN_USERNAME);

			((TextView) getView().findViewById(BEST_WIN_ID + RATING_VALUE_ID)).setText(String.valueOf(rating));
			((TextView) getView().findViewById(BEST_WIN_ID + RATING_SUBTITLE_ID)).setText(userName);
		}
	}

	private void addRatingsViews(LinearLayout ratingsLinearView) {
		LayoutInflater inflater = LayoutInflater.from(getActivity());
		// set id's to view for further set data to them
		{// Highest Rating
			RelativeLayout highestRatingView = (RelativeLayout) inflater.inflate(R.layout.new_stats_rating_item_view, null, false);
			((TextView) highestRatingView.findViewById(R.id.ratingLabelTxt)).setText(R.string.highest_rating);
			highestRatingView.findViewById(R.id.subtitleTxt).setId(HIGHEST_ID + RATING_SUBTITLE_ID);
			highestRatingView.findViewById(R.id.ratingValueTxt).setId(HIGHEST_ID + RATING_VALUE_ID);

			ratingsLinearView.addView(highestRatingView);
		}

		{// Lowest Rating
			RelativeLayout highestRatingView = (RelativeLayout) inflater.inflate(R.layout.new_stats_rating_item_view, null, false);
			((TextView) highestRatingView.findViewById(R.id.ratingLabelTxt)).setText(R.string.lowest_rating);
			highestRatingView.findViewById(R.id.subtitleTxt).setId(LOWEST_ID + RATING_SUBTITLE_ID);
			highestRatingView.findViewById(R.id.ratingValueTxt).setId(LOWEST_ID + RATING_VALUE_ID);

			ratingsLinearView.addView(highestRatingView);
		}

		{// Average Opponent Rating
			RelativeLayout highestRatingView = (RelativeLayout) inflater.inflate(R.layout.new_stats_rating_item_view, null, false);
			((TextView) highestRatingView.findViewById(R.id.ratingLabelTxt)).setText(R.string.avg_opponent_rating);
			highestRatingView.findViewById(R.id.ratingValueTxt).setId(AVERAGE_ID + RATING_VALUE_ID);
			((TextView) highestRatingView.findViewById(R.id.subtitleTxt)).setText(R.string.three_months);

			ratingsLinearView.addView(highestRatingView);
		}

		{// Best Win Rating
			RelativeLayout highestRatingView = (RelativeLayout) inflater.inflate(R.layout.new_stats_rating_item_view, null, false);
			((TextView) highestRatingView.findViewById(R.id.ratingLabelTxt)).setText(R.string.best_win_rating);
			highestRatingView.findViewById(R.id.subtitleTxt).setId(BEST_WIN_ID + RATING_SUBTITLE_ID);
			highestRatingView.findViewById(R.id.ratingValueTxt).setId(BEST_WIN_ID + RATING_VALUE_ID);
			((TextView) highestRatingView.findViewById(BEST_WIN_ID + RATING_SUBTITLE_ID))
					.setTextColor(getResources().getColor(R.color.new_friends_blue_text));

			ratingsLinearView.addView(highestRatingView);
		}
	}

}
