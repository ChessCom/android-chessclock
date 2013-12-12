package com.chess.ui.fragments.stats;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.stats.TacticsHistoryItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.QueryParams;
import com.chess.db.tasks.SaveTacticsStatsTask;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.ItemsCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.views.RatingGraphView;
import com.chess.utilities.AppUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.02.13
 * Time: 19:16
 */
public class StatsGameTacticsFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener, RadioGroup.OnCheckedChangeListener, ViewTreeObserver.OnGlobalLayoutListener {

	public static final int FIRST = 0;
	public static final int LAST = 1;
	public static final String NUMBER = "#";

	private RatingGraphView ratingGraphView;
	private String username;
	private SaveStatsUpdateListener saveStatsUpdateListener;
	private StatsItemUpdateListener statsItemUpdateListener;
	private RecentStatsAdapter recentStatsAdapter;
	private TextView recentProblemsTitleTxt;
	private long lastTimestamp;
	private int previousCheckedId = NON_INIT;

	public StatsGameTacticsFragment() {
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, 0);

		setArguments(bundle);
	}

	public static StatsGameTacticsFragment createInstance(String username) {
		StatsGameTacticsFragment fragment = new StatsGameTacticsFragment();
		Bundle bundle = new Bundle();
		bundle.putString(USERNAME, username);

		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			username = getArguments().getString(USERNAME);
		} else {
			username = savedInstanceState.getString(USERNAME);
		}

		if (TextUtils.isEmpty(username)) {
			username = getUsername();
		}

		saveStatsUpdateListener = new SaveStatsUpdateListener();
		statsItemUpdateListener = new StatsItemUpdateListener();
		recentStatsAdapter = new RecentStatsAdapter(getActivity(), null);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_white_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (!username.equals(getUsername())) {
			setTitle(username + Symbol.SPACE + getString(R.string.stats));
		} else {
			setTitle(R.string.stats);
		}

		View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_tactics_stats_header_view, null, false);
		recentProblemsTitleTxt = (TextView) headerView.findViewById(R.id.recentProblemsTitleTxt);
		ratingGraphView = (RatingGraphView) headerView.findViewById(R.id.ratingGraphView);
		ratingGraphView.setOnCheckChangeListener(this);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.addHeaderView(headerView);
		listView.setAdapter(recentStatsAdapter);
		listView.setOnItemClickListener(this);

		view.getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(USERNAME, username);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO load selected tactic from DB
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		if (previousCheckedId == checkedId) {
			return;
		}

		previousCheckedId = checkedId;
		lastTimestamp = System.currentTimeMillis();
		switch (checkedId) {
			case R.id.thirtyDaysBtn:
				lastTimestamp = AppUtils.getLast30DaysTimeStamp();
				break;
			case R.id.ninetyDaysBtn:
				lastTimestamp = AppUtils.getLast90DaysTimeStamp();
				break;
			case R.id.oneYearBtn:
				lastTimestamp = AppUtils.getLastYearTimeStamp();
				break;
			case R.id.allTimeBtn:
				lastTimestamp = getAppData().getUserCreateDate();
				break;
		}
		updateGraphAfter(lastTimestamp);
	}

	private void updateGraphAfter(long lastTimestamp) {
		this.lastTimestamp = lastTimestamp;

		long[] edgeTimestamps = DbDataManager.getEdgeTimestampForTacticsGraph(getContentResolver(), username);

		long today = System.currentTimeMillis() / 1000;
		long oneDay = AppUtils.SECONDS_IN_DAY;
		// if we have saved data from last timestamp(30 days ago) until today, we don't load it from server
		if ((edgeTimestamps[LAST] >= today - oneDay) && edgeTimestamps[FIRST] <= lastTimestamp - oneDay) {
			updateUiData();
		} else { // else we only load difference since last saved point
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_TACTICS_STATS);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
			loadItem.addRequestParams(RestHelper.P_USERNAME, username);
			loadItem.addRequestParams(RestHelper.P_LAST_GRAPH_TIMESTAMP, lastTimestamp);

			new RequestJsonTask<TacticsHistoryItem>(statsItemUpdateListener).executeTask(loadItem);
		}
	}

	@Override
	public void onGlobalLayout() {
		if (need2update) {
			ratingGraphView.setChecked(R.id.thirtyDaysBtn);
		}
	}

	private class StatsItemUpdateListener extends ChessUpdateListener<TacticsHistoryItem> {

		public StatsItemUpdateListener() {
			super(TacticsHistoryItem.class);
		}

		@Override
		public void updateData(TacticsHistoryItem returnedObj) {
			super.updateData(returnedObj);

			new SaveTacticsStatsTask(saveStatsUpdateListener, returnedObj.getData(),
					getContentResolver(), username).executeTask();
		}
	}

	private class SaveStatsUpdateListener extends ChessUpdateListener<TacticsHistoryItem.Data> {

		@Override
		public void updateData(TacticsHistoryItem.Data returnedObj) {
			super.updateData(returnedObj);

			updateUiData();
		}
	}

	private void updateUiData() {
		fillGraph();

		// load recent problems stats
		QueryParams params = DbHelper.getTableForUser(username, DbScheme.Tables.TACTICS_RECENT_STATS);
		Cursor cursor = DbDataManager.query(getContentResolver(), params);

		cursor.moveToFirst();
		recentStatsAdapter.changeCursor(cursor);
		recentProblemsTitleTxt.setVisibility(View.VISIBLE);
		need2update = false;
	}

	protected void fillGraph() {
		// Graph Rating Data
		logTest(" get data from DB lastTimestamp = " + lastTimestamp);
		QueryParams params = DbHelper.getTacticGraphItemForUser(username, lastTimestamp);
		Cursor cursor = DbDataManager.query(getContentResolver(), params);

		if (cursor != null && cursor.moveToFirst()) {
			List<long[]> series = new ArrayList<long[]>();
			do {
				long timestamp = DbDataManager.getLong(cursor, DbScheme.V_TIMESTAMP) * 1000L;
				int rating = DbDataManager.getInt(cursor, DbScheme.V_CLOSE_RATING);

				long[] point = new long[]{timestamp, rating};
				series.add(point);
			} while (cursor.moveToNext());
			cursor.close();

			ratingGraphView.setGraphData(series, getView().getWidth());
		}
	}

	public static class RecentStatsAdapter extends ItemsCursorAdapter {

		private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM' 'HH:mm");
		public static final String PASSED = "passed";
		private final Date passedDate;
		private final int failColor;
		private final int passedColor;

		public RecentStatsAdapter(Context context, Cursor cursor) {
			super(context, cursor);
			passedDate = new Date();

			passedColor = resources.getColor(R.color.new_dark_green);
			failColor = resources.getColor(R.color.red_button);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = inflater.inflate(R.layout.new_tactic_recent_stat_item, parent, false);

			ViewHolder holder = new ViewHolder();
			holder.dateTxt = (TextView) view.findViewById(R.id.dateTxt);
			holder.idTxt = (TextView) view.findViewById(R.id.idTxt);
			holder.ratingTxt = (TextView) view.findViewById(R.id.ratingTxt);
			holder.userRatingTxt = (TextView) view.findViewById(R.id.userRatingTxt);
			holder.avgTimeTxt = (TextView) view.findViewById(R.id.avgTimeTxt);
			holder.userTimeTxt = (TextView) view.findViewById(R.id.userTimeTxt);
			holder.scoreTxt = (TextView) view.findViewById(R.id.scoreTxt);
			holder.ratingChangeTxt = (TextView) view.findViewById(R.id.ratingChangeTxt);

			view.setTag(holder);

			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder holder = (ViewHolder) view.getTag();

			passedDate.setTime(getLong(cursor, DbScheme.V_CREATE_DATE));
			String dateStr = dateFormatter.format(passedDate);
			holder.dateTxt.setText(dateStr);
			holder.idTxt.setText(NUMBER + String.valueOf(getLong(cursor, DbScheme.V_ID)));

			String tacticRating = String.valueOf(getInt(cursor, DbScheme.V_RATING));
			holder.ratingTxt.setText(tacticRating);


			String userRating = String.valueOf(getInt(cursor, DbScheme.V_USER_RATING));
			holder.userRatingTxt.setText(userRating);

			int avgSeconds = getInt(cursor, DbScheme.V_AVG_SECONDS);

			String avgSecondsStr = AppUtils.getSecondsTimeFromSecondsStr(avgSeconds);
			holder.avgTimeTxt.setText(avgSecondsStr);

			int userSeconds = getInt(cursor, DbScheme.V_SECONDS_SPENT);

			String userTimeStr = AppUtils.getSecondsTimeFromSecondsStr(userSeconds);
			holder.userTimeTxt.setText(userTimeStr);

			String status = getString(cursor, DbScheme.V_OUTCOME_STATUS);
			if (status.equals(PASSED)) {
				holder.ratingChangeTxt.setTextColor(passedColor);
			} else {
				holder.ratingChangeTxt.setTextColor(failColor);
			}
			String scoreStr = String.valueOf(getInt(cursor, DbScheme.V_OUTCOME_SCORE)) + Symbol.PERCENT;
			holder.scoreTxt.setText(scoreStr);
			holder.ratingChangeTxt.setText(String.valueOf(getLong(cursor, DbScheme.V_OUTCOME_RATING_CHANGE)));
		}

		public static class ViewHolder {
			TextView dateTxt;
			TextView idTxt;
			TextView ratingTxt;
			TextView userRatingTxt;
			TextView avgTimeTxt;
			TextView userTimeTxt;
			TextView scoreTxt;
			TextView ratingChangeTxt;
		}
	}
}
