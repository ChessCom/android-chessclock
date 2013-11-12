package com.chess.ui.fragments.stats;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.09.13
 * Time: 7:13
 */
public class StatsGameLessonsFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private RatingGraphView ratingGraphView;
	protected static final String USERNAME = "username";
	private String username;
	private SaveStatsUpdateListener saveStatsUpdateListener;
	private StatsItemUpdateListener statsItemUpdateListener;
	private RecentStatsAdapter recentStatsAdapter;
	private TextView recentProblemsTitleTxt;

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
		return inflater.inflate(R.layout.new_white_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.stats);

		View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_tactics_stats_header_view, null, false);
		recentProblemsTitleTxt = (TextView) headerView.findViewById(R.id.recentProblemsTitleTxt);
		recentProblemsTitleTxt.setText(R.string.recent_lessons);

		ratingGraphView = (RatingGraphView) headerView.findViewById(R.id.ratingGraphView);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.addHeaderView(headerView);
		listView.setAdapter(recentStatsAdapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (need2update) {
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_LESSONS_STATS);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
			loadItem.addRequestParams(RestHelper.P_USERNAME, username);

			new RequestJsonTask<LessonsStatsItem>(statsItemUpdateListener).executeTask(loadItem);
		} else {
			updateUiData();
		}
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
		fillGraph();

		// load recent problems stats
		QueryParams params = DbHelper.getTableForUser(username, DbScheme.Tables.LESSONS_RECENT_STATS);
		Cursor cursor = DbDataManager.query(getContentResolver(), params);

		cursor.moveToFirst();
		recentStatsAdapter.changeCursor(cursor);
		recentProblemsTitleTxt.setVisibility(View.VISIBLE);
	}

	protected void fillGraph() {
		// Graph Rating Data
		QueryParams params = DbHelper.getTableForUser(username, DbScheme.Tables.LESSONS_GRAPH_STATS);
		Cursor cursor = DbDataManager.query(getContentResolver(), params);

		if (cursor != null && cursor.moveToFirst()) {
			List<long[]> series = new ArrayList<long[]>();

			do {
				long timestamp = DbDataManager.getLong(cursor, DbScheme.V_TIMESTAMP) * 1000L;
				int rating = DbDataManager.getInt(cursor, DbScheme.V_RATING);
				long[] point = new long[]{timestamp, rating};
				series.add(point);
			} while (cursor.moveToNext());
			cursor.close();

			ratingGraphView.setGraphData(series, getView().getWidth());
		}
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