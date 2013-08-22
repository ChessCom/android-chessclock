package com.chess.ui.fragments.stats;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.api.stats.GraphData;
import com.chess.backend.statics.StaticData;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.QueryParams;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveGameStatsTask;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.views.ChartView;
import com.chess.ui.views.PieChartView;
import com.chess.ui.views.RatingGraphView;
import com.chess.utilities.AppUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.chess.ui.fragments.stats.StatsGameFragment.*;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 23.01.13
 * Time: 10:37
 */
public class StatsGameDetailsFragment extends CommonLogicFragment {

	private static final String TAG = "StatsGameFragment";
	public static final String GREY_COLOR_DIVIDER = "##";

	public static final int HIGHEST_ID = 0x00002000;
	public static final int LOWEST_ID = 0x00002100;
	public static final int AVERAGE_ID = 0x00002200;
	public static final int BEST_WIN_ID = 0x00002300;

	public static final int RATING_SUBTITLE_ID = 0x00000001;
	public static final int RATING_VALUE_ID = 0x00000002;
	// 05/27/08
	private final static SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yy");

	private final static String MODE = "mode";
	private static final String USERNAME = "username";

	private CursorUpdateListener standardCursorUpdateListener;
	private CursorUpdateListener lightningCursorUpdateListener;
	private CursorUpdateListener blitzCursorUpdateListener;
	private CursorUpdateListener chessCursorUpdateListener;
	private CursorUpdateListener chess960CursorUpdateListener;

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
	private PieChartView pieChartView;
	private TextView timeoutsValueTxt;
	private TextView glickoValueTxt;
	private TextView mostFrequentOpponentTxt;
	private TextView mostFrequentOpponentGamesTxt;
	private TextView timeoutsLabelTxt;
	private ForegroundColorSpan foregroundSpan;
	private int mode;
	private String username;
	private RatingGraphView ratingGraphView;

	public StatsGameDetailsFragment() {
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, 0);

		setArguments(bundle);
	}

	public static StatsGameDetailsFragment createInstance(int code, String username) {
		StatsGameDetailsFragment fragment = new StatsGameDetailsFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, code);
		bundle.putString(USERNAME, username);

		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			username = getArguments().getString(USERNAME);
			mode = getArguments().getInt(MODE);
		} else {
			username = savedInstanceState.getString(USERNAME);
			mode = savedInstanceState.getInt(MODE);
		}

		if (TextUtils.isEmpty(username)) {
			username = getUsername();
		}

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_stats_game_details_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.stats);

		currentRatingTxt = (TextView) view.findViewById(R.id.currentRatingTxt);

		absoluteRankTxt = (TextView) view.findViewById(R.id.absoluteRankTxt);
		totalRankedTxt = (TextView) view.findViewById(R.id.totalRankedTxt);

		percentileValueTxt = (TextView) view.findViewById(R.id.percentileValueTxt);

		totalGamesValueTxt = (TextView) view.findViewById(R.id.totalGamesValueTxt);


		pieChartView = (PieChartView) view.findViewById(R.id.pieChartView);
		ratingGraphView = (RatingGraphView) view.findViewById(R.id.ratingGraphView);

		LinearLayout ratingsLinearView = (LinearLayout) view.findViewById(R.id.ratingsLinearView);
		addRatingsViews(ratingsLinearView);

		winCntValueTxt = (TextView) view.findViewById(R.id.winCntValueTxt);
		loseCntValueTxt = (TextView) view.findViewById(R.id.loseCntValueTxt);
		drawCntValueTxt = (TextView) view.findViewById(R.id.drawCntValueTxt);

		winningStreakValueTxt = (TextView) view.findViewById(R.id.winningStreakValueTxt);
		losingStreakValueTxt = (TextView) view.findViewById(R.id.losingStreakValueTxt);
		timeoutsLabelTxt = (TextView) view.findViewById(R.id.timeoutsLabelTxt);
		timeoutsValueTxt = (TextView) view.findViewById(R.id.timeoutsValueTxt);
		glickoValueTxt = (TextView) view.findViewById(R.id.glickoValueTxt);

		mostFrequentOpponentTxt = (TextView) view.findViewById(R.id.mostFrequentOpponentTxt);
		mostFrequentOpponentGamesTxt = (TextView) view.findViewById(R.id.mostFrequentOpponentGamesTxt);
	}


	@Override
	public void onResume() {
		super.onResume();

		if (need2update) {
			updateData();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(MODE, mode);
		outState.putString(USERNAME, username);
	}

	private void init() {
		standardCursorUpdateListener = new CursorUpdateListener(LIVE_STANDARD);
		lightningCursorUpdateListener = new CursorUpdateListener(LIVE_LIGHTNING);
		blitzCursorUpdateListener = new CursorUpdateListener(LIVE_BLITZ);
		chessCursorUpdateListener = new CursorUpdateListener(DAILY_CHESS);
		chess960CursorUpdateListener = new CursorUpdateListener(DAILY_CHESS960);

		int lightGrey = getResources().getColor(R.color.stats_label_light_grey);
		foregroundSpan = new ForegroundColorSpan(lightGrey);

	}

	private void updateData() {

		switch (mode) {
			case LIVE_STANDARD:
				new LoadDataFromDbTask(standardCursorUpdateListener, DbHelper.getTableForUser(username,
						DbScheme.Tables.GAME_STATS_LIVE_STANDARD), getContentResolver()).executeTask();
				break;
			case LIVE_LIGHTNING:
				new LoadDataFromDbTask(lightningCursorUpdateListener, DbHelper.getTableForUser(username,
						DbScheme.Tables.GAME_STATS_LIVE_LIGHTNING), getContentResolver()).executeTask();
				break;
			case LIVE_BLITZ:
				new LoadDataFromDbTask(blitzCursorUpdateListener, DbHelper.getTableForUser(username,
						DbScheme.Tables.GAME_STATS_LIVE_BLITZ), getContentResolver()).executeTask();
				break;
			case DAILY_CHESS:
				new LoadDataFromDbTask(chessCursorUpdateListener, DbHelper.getTableForUser(username,
						DbScheme.Tables.GAME_STATS_DAILY_CHESS), getContentResolver()).executeTask();
				break;
			case DAILY_CHESS960:
				new LoadDataFromDbTask(chess960CursorUpdateListener, DbHelper.getTableForUser(username,
						DbScheme.Tables.GAME_STATS_DAILY_CHESS960), getContentResolver()).executeTask();
				break;
		}
	}

	private class CursorUpdateListener extends ChessUpdateListener<Cursor> {

		private int listenerCode;

		public CursorUpdateListener(int listenerCode) {
			super();
			this.listenerCode = listenerCode;
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			String gameType;
			switch (listenerCode) {
				case LIVE_STANDARD:
					gameType = SaveGameStatsTask.STANDARD;
					break;
				case LIVE_LIGHTNING:
					gameType = SaveGameStatsTask.LIGHTNING;
					break;
				case LIVE_BLITZ:
					gameType = SaveGameStatsTask.BLITZ;
					break;
				case DAILY_CHESS:
					gameType = SaveGameStatsTask.CHESS;
					break;
				default:
				case DAILY_CHESS960:
					gameType = SaveGameStatsTask.CHESS960;
					break;
			}

			{ // top info view
				int current = DbDataManager.getInt(returnedObj, DbScheme.V_CURRENT);
				currentRatingTxt.setText(String.valueOf(current));

				int rank = DbDataManager.getInt(returnedObj, DbScheme.V_RANK);
				if (rank == 0) {
					absoluteRankTxt.setText(R.string.not_available);

				} else {
					absoluteRankTxt.setText(String.valueOf(rank));
					int totalPlayers = DbDataManager.getInt(returnedObj, DbScheme.V_TOTAL_PLAYER_COUNT);
					totalRankedTxt.setText(getString(R.string.of_arg, totalPlayers));
				}

				String percentile = DbDataManager.getString(returnedObj, DbScheme.V_PERCENTILE);
				if (percentile.equals(String.valueOf(0.f))) {
					percentileValueTxt.setText(R.string.not_available);
				} else {
					percentileValueTxt.setText(percentile + StaticData.SYMBOL_PERCENT);
				}
			}

			int totalGamesPlayed = DbDataManager.getInt(returnedObj, DbScheme.V_GAMES_TOTAL);
			totalGamesValueTxt.setText(String.valueOf(totalGamesPlayed));

			fillRatings(returnedObj);

			{// avg opponent rating when i
				int winCnt = DbDataManager.getInt(returnedObj, DbScheme.V_AVG_OPPONENT_RATING_WIN);
				winCntValueTxt.setText(String.valueOf(winCnt));

				int loseCnt = DbDataManager.getInt(returnedObj, DbScheme.V_AVG_OPPONENT_RATING_LOSE);
				loseCntValueTxt.setText(String.valueOf(loseCnt));

				int drawCnt = DbDataManager.getInt(returnedObj, DbScheme.V_AVG_OPPONENT_RATING_DRAW);
				drawCntValueTxt.setText(String.valueOf(drawCnt));
			}

			{// Streaks
				int winCnt = DbDataManager.getInt(returnedObj, DbScheme.V_WINNING_STREAK);
				winningStreakValueTxt.setText(String.valueOf(winCnt));

				int loseCnt = DbDataManager.getInt(returnedObj, DbScheme.V_LOSING_STREAK);
				losingStreakValueTxt.setText(String.valueOf(loseCnt));
			}

			{ // Graph Rating Data
				QueryParams params = DbHelper.getGraphItemForUser(username, gameType);
				Cursor cursor = DbDataManager.executeQuery(getContentResolver(), params);

				if (cursor != null && cursor.moveToFirst()) {
					List<long[]> series = new ArrayList<long[]>();
					do {
						GraphData.SingleItem singleItem = DbDataManager.getGraphSingleItemFromCursor(cursor);
						long[] point = new long[]{singleItem.getTimestamp(), singleItem.getRating() };
						series.add(point);
					} while (cursor.moveToNext());

					ratingGraphView.setGraphData(series);
				}
			}

			// donut/pie chart
			pieChartView.setGames(DbDataManager.getGameStatsGamesByResultFromCursor(returnedObj));

			{// timeouts
				String timeoutsStr = getString(R.string.timeouts_last_90_days);
				timeoutsStr = timeoutsStr.replace(StaticData.SYMBOL_LEFT_PAR, GREY_COLOR_DIVIDER + StaticData.SYMBOL_LEFT_PAR);
				timeoutsStr = timeoutsStr.replace(StaticData.SYMBOL_RIGHT_PAR, StaticData.SYMBOL_RIGHT_PAR + GREY_COLOR_DIVIDER);
				CharSequence timeoutChr = timeoutsStr;
				timeoutChr = AppUtils.setSpanBetweenTokens(timeoutChr, GREY_COLOR_DIVIDER, foregroundSpan);
				timeoutsLabelTxt.setText(timeoutChr);

				int timeouts = DbDataManager.getInt(returnedObj, DbScheme.V_TIMEOUTS);
				if (timeouts == 0) {
					timeoutsValueTxt.setText(R.string.not_available);
				} else {
					timeoutsValueTxt.setText(String.valueOf(timeouts));
				}
			}

			int glickoRd = DbDataManager.getInt(returnedObj, DbScheme.V_GLICKO_RD);
			glickoValueTxt.setText(String.valueOf(glickoRd));

			String mostFrequentOpponentName = DbDataManager.getString(returnedObj, DbScheme.V_FREQUENT_OPPONENT_NAME);
			int mostFrequentOpponentGamesPlayed = DbDataManager.getInt(returnedObj, DbScheme.V_FREQUENT_OPPONENT_GAMES_PLAYED);
			if (mostFrequentOpponentGamesPlayed == 0) {
				mostFrequentOpponentGamesTxt.setText(R.string.not_available);
			} else {
				mostFrequentOpponentTxt.setText(mostFrequentOpponentName);
				mostFrequentOpponentGamesTxt.setText(getString(R.string.games_arg, mostFrequentOpponentGamesPlayed));
			}
		}
	}

	private void fillRatings(Cursor cursor) {
		{ // highest
			int rating = DbDataManager.getInt(cursor, DbScheme.V_HIGHEST_RATING);
			long ratingTime = DbDataManager.getLong(cursor, DbScheme.V_HIGHEST_TIMESTAMP) * 1000L;

			setTextById((HIGHEST_ID + RATING_VALUE_ID), String.valueOf(rating));
			setTextById((HIGHEST_ID + RATING_SUBTITLE_ID), dateFormatter.format(new Date(ratingTime)));
		}

		{ // lowest
			int rating = DbDataManager.getInt(cursor, DbScheme.V_LOWEST_RATING);
			long ratingTime = DbDataManager.getLong(cursor, DbScheme.V_LOWEST_TIMESTAMP) * 1000L;

			setTextById((LOWEST_ID + RATING_VALUE_ID), String.valueOf(rating));
			setTextById((LOWEST_ID + RATING_SUBTITLE_ID), dateFormatter.format(new Date(ratingTime)));
		}

		{ // average opponent
			String rating = DbDataManager.getString(cursor, DbScheme.V_AVERAGE_OPPONENT_RATING);

			setTextById((AVERAGE_ID + RATING_VALUE_ID), rating);
		}

		{ // best win on
			int rating = DbDataManager.getInt(cursor, DbScheme.V_BEST_WIN_RATING);
			if (rating == 0) {
				setTextById((BEST_WIN_ID + RATING_VALUE_ID), R.string.not_available);
			} else {
				String userName = DbDataManager.getString(cursor, DbScheme.V_BEST_WIN_USERNAME);

				setTextById((BEST_WIN_ID + RATING_VALUE_ID), String.valueOf(rating));
				setTextById((BEST_WIN_ID + RATING_SUBTITLE_ID), userName);
			}
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
					.setTextColor(getResources().getColor(R.color.new_text_blue));

			ratingsLinearView.addView(highestRatingView);
		}
	}

	private void setTextById(int id, String text) {
		((TextView) getView().findViewById(id)).setText(text);
	}

	private void setTextById(int id, int textId) {
		((TextView) getView().findViewById(id)).setText(textId);
	}
}
