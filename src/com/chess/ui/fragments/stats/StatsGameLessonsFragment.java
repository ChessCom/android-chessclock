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
import com.chess.backend.entity.api.stats.LessonsStatsItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.QueryParams;
import com.chess.db.tasks.SaveLessonsStatsTask;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.ItemsCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.views.RatingGraphView;
import com.chess.utilities.AppUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.09.13
 * Time: 7:13
 */
public class StatsGameLessonsFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener, RadioGroup.OnCheckedChangeListener, ViewTreeObserver.OnGlobalLayoutListener {

	public static final int FIRST = 0;
	public static final int LAST = 1;

	private RatingGraphView ratingGraphView;

	private String username;
	private SaveStatsUpdateListener saveStatsUpdateListener;
	private StatsItemUpdateListener statsItemUpdateListener;
	private RecentStatsAdapter recentStatsAdapter;
	private TextView recentProblemsTitleTxt;
	private int previousCheckedId;
	private long lastTimestamp;

	public StatsGameLessonsFragment() {
		Bundle bundle = new Bundle();
		bundle.putInt(MODE, 0);

		setArguments(bundle);
	}

	public static StatsGameLessonsFragment createInstance(String username) {
		StatsGameLessonsFragment fragment = new StatsGameLessonsFragment();
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
		return inflater.inflate(R.layout.new_stats_frame, container, false);
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
		recentProblemsTitleTxt.setText(R.string.recent_lessons);

		ratingGraphView = (RatingGraphView) headerView.findViewById(R.id.ratingGraphView);
		ratingGraphView.setOnCheckChangeListener(this);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.addHeaderView(headerView);
		listView.setAdapter(recentStatsAdapter);
		listView.setOnItemClickListener(this);

		if (isNeedToUpgrade()) {
			view.findViewById(R.id.demoOverlayView).setVisibility(View.VISIBLE);
		}

		view.getViewTreeObserver().addOnGlobalLayoutListener(this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(USERNAME, username);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO load selected lesson from DB or load it from server
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

		long[] edgeTimestamps = DbDataManager.getEdgeTimestampForLessonsGraph(getContentResolver(), username);

		long today = System.currentTimeMillis() / 1000;
		long oneDay = AppUtils.SECONDS_IN_DAY;
		// if we have saved data from last timestamp(30 days ago) until today, we don't load it from server
		if ((edgeTimestamps[LAST] >= today - oneDay) && edgeTimestamps[FIRST] <= lastTimestamp - oneDay) {
			updateUiData();
		} else { // else we only load difference since last saved point
			getFullStats(lastTimestamp);
		}
	}

	private void getFullStats(long lastTimestamp) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_LESSONS_STATS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_USERNAME, username);
		loadItem.addRequestParams(RestHelper.P_LAST_GRAPH_TIMESTAMP, lastTimestamp);

		new RequestJsonTask<LessonsStatsItem>(statsItemUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onGlobalLayout() {
		if (need2update) {
			ratingGraphView.setChecked(R.id.thirtyDaysBtn);
		}
	}

	private class StatsItemUpdateListener extends ChessUpdateListener<LessonsStatsItem> {

		public StatsItemUpdateListener() {
			super(LessonsStatsItem.class);
		}

		@Override
		public void updateData(LessonsStatsItem returnedObj) {
			super.updateData(returnedObj);

			new SaveLessonsStatsTask(saveStatsUpdateListener, returnedObj.getData(),
					getContentResolver(), username).executeTask();
		}
	}

	private class SaveStatsUpdateListener extends ChessUpdateListener<LessonsStatsItem.Data> {

		@Override
		public void updateData(LessonsStatsItem.Data returnedObj) {
			super.updateData(returnedObj);

			updateUiData();
		}
	}

	private void updateUiData() {
		{// Graph Rating Data
			final List<long[]> series = new ArrayList<long[]>();
			if (isNeedToUpgrade()) {
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MONTH, -3);

				int pointsCnt = 100;
				long startPoint = calendar.getTimeInMillis();
				long endPoint = System.currentTimeMillis();
				long step = (endPoint - startPoint) / pointsCnt;

				for (int i = 0; i < pointsCnt; i++) {
					startPoint += step;
					int rating = (int) (1200 + Math.random() * 50);
					long[] point = new long[]{startPoint, rating};
					series.add(point);
				}

			} else {
				QueryParams params = DbHelper.getLessonsGraphItemForUser(username, lastTimestamp);
				Cursor cursor = DbDataManager.query(getContentResolver(), params);

				if (cursor != null && cursor.moveToFirst()) {
					do {
						long timestamp = DbDataManager.getLong(cursor, DbScheme.V_TIMESTAMP) * 1000L;
						int rating = DbDataManager.getInt(cursor, DbScheme.V_RATING);

						long[] point = new long[]{timestamp, rating};
						series.add(point);
					} while (cursor.moveToNext());
					cursor.close();
				}
			}

			if (getView().getWidth() == 0) {
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						ratingGraphView.setGraphData(series, getView().getWidth());
					}
				}, VIEW_UPDATE_DELAY);
			} else {
				ratingGraphView.setGraphData(series, getView().getWidth());
			}
		}

		if (!isNeedToUpgrade()) {
			// load recent problems stats
			QueryParams params = DbHelper.getTableForUser(username, DbScheme.Tables.LESSONS_RECENT_STATS);
			Cursor cursor = DbDataManager.query(getContentResolver(), params);

			cursor.moveToFirst();
			recentStatsAdapter.changeCursor(cursor);
			recentProblemsTitleTxt.setVisibility(View.VISIBLE);
		}

		need2update = false;
	}

	public static class RecentStatsAdapter extends ItemsCursorAdapter {

		public RecentStatsAdapter(Context context, Cursor cursor) {
			super(context, cursor);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = inflater.inflate(R.layout.new_lessons_recent_stat_item, parent, false);

			ViewHolder holder = new ViewHolder();
			holder.nameTxt = (TextView) view.findViewById(R.id.nameTxt);
			holder.categoryTxt = (TextView) view.findViewById(R.id.categoryTxt);
			holder.scoreTxt = (TextView) view.findViewById(R.id.scoreTxt);
			holder.ratingTxt = (TextView) view.findViewById(R.id.ratingTxt);

			view.setTag(holder);

			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			ViewHolder holder = (ViewHolder) view.getTag();

			holder.nameTxt.setText(getString(cursor, DbScheme.V_NAME));
			holder.categoryTxt.setText(getString(cursor, DbScheme.V_CATEGORY));

			String tacticRating = String.valueOf(getInt(cursor, DbScheme.V_RATING));
			holder.ratingTxt.setText(tacticRating);

			String scoreStr = String.valueOf(getInt(cursor, DbScheme.V_SCORE)) + Symbol.PERCENT;
			holder.scoreTxt.setText(scoreStr);
		}

		public static class ViewHolder {
			TextView nameTxt;
			TextView categoryTxt;
			TextView scoreTxt;
			TextView ratingTxt;
		}
	}
}