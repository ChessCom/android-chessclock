package com.chess.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.stats.UserStatsItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.tasks.SaveUserStatsTask;
import com.chess.model.RatingListItem;
import com.chess.ui.adapters.RatingsAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.12.12
 * Time: 22:04
 */
public class HomeRatingsFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private final static int LIVE_STANDARD = 0;
	private final static int LIVE_BLITZ = 1;
	private final static int LIVE_LIGHTNING = 2;
	private final static int DAILY_CHESS = 3;
	private final static int DAILY_CHESS960 = 4;
	private final static int TACTICS = 5;
	private final static int CHESS_MENTOR = 6;


	private StatsItemUpdateListener statsItemUpdateListener;
	private SaveStatsUpdateListener saveStatsUpdateListener;
	private List<RatingListItem> ratingList;
	private RatingsAdapter ratingsAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		statsItemUpdateListener = new StatsItemUpdateListener();
		saveStatsUpdateListener = new SaveStatsUpdateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_home_ratings_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		ratingList = createStatsList(getActivity());
		ratingsAdapter = new RatingsAdapter(getActivity(), ratingList);
		listView.setAdapter(ratingsAdapter);
		listView.setOnItemClickListener(this);

	}

	@Override
	public void onResume() {
		super.onResume();

		{// get full users stats

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_USER_STATS);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getActivity()));

			new RequestJsonTask<UserStatsItem>(statsItemUpdateListener).executeTask(loadItem);
		}

		getActivityFace().setBadgeValueForId(R.id.menu_games, 2); // TODO use properly later for notifications
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (position) {
			case LIVE_STANDARD:
			case LIVE_BLITZ:
			case LIVE_LIGHTNING:
			case DAILY_CHESS:
			case DAILY_CHESS960:
				getActivityFace().openFragment(StatsGameFragment.newInstance(position));
				break;
			case TACTICS:
				getActivityFace().openFragment(new StatsGameTacticsFragment());
				break;
			case CHESS_MENTOR: // not used yet
				getActivityFace().openFragment(new StatsGameTacticsFragment());
				break;
		}
	}

	private class StatsItemUpdateListener extends ActionBarUpdateListener<UserStatsItem> {

		public StatsItemUpdateListener() {
			super(getInstance(), UserStatsItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			if (show) {
				showToast("Loading ...");
			} else {
				showToast("Done!");
			}
		}

		@Override
		public void updateData(UserStatsItem returnedObj) {
			super.updateData(returnedObj);

			new SaveUserStatsTask(saveStatsUpdateListener, returnedObj.getData(), getContentResolver()).executeTask();
		}
	}

	private class SaveStatsUpdateListener extends ActionBarUpdateListener<UserStatsItem.Data> {

		public SaveStatsUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(UserStatsItem.Data returnedObj) {
			super.updateData(returnedObj);
			Activity activity = getActivity();
			if (activity == null) {
				return;
			}

			// fill ratings
			String[] argument = new String[]{AppData.getUserName(activity)};

			{// standard
				Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.USER_STATS_LIVE_STANDARD],
						DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
				cursor.moveToFirst();
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);

				ratingList.get(LIVE_STANDARD).setValue(currentRating);
			}
			{// blitz
				Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.USER_STATS_LIVE_BLITZ],
						DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
				cursor.moveToFirst();
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);

				ratingList.get(LIVE_BLITZ).setValue(currentRating);
			}
			{// bullet
				Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.USER_STATS_LIVE_LIGHTNING],
						DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
				cursor.moveToFirst();
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);

				ratingList.get(LIVE_LIGHTNING).setValue(currentRating);
			}
			{// chess
				Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.USER_STATS_DAILY_CHESS],
						DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
				cursor.moveToFirst();
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);

				ratingList.get(DAILY_CHESS).setValue(currentRating);
			}
			{// chess960
				Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.USER_STATS_DAILY_CHESS960],
						DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
				cursor.moveToFirst();
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);

				ratingList.get(DAILY_CHESS960).setValue(currentRating);
			}
			{// tactics
				Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.USER_STATS_TACTICS],
						DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
				cursor.moveToFirst();
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);

				ratingList.get(TACTICS).setValue(currentRating);
			}
			{// chess mentor
				Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.USER_STATS_CHESS_MENTOR],
						DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
				cursor.moveToFirst();
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);

				ratingList.get(CHESS_MENTOR).setValue(currentRating);
			}

			ratingsAdapter.notifyDataSetInvalidated();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);

			showToast(" code " + resultCode);
		}
	}

	private List<RatingListItem> createStatsList(Context context) {
		ArrayList<RatingListItem> selectionItems = new ArrayList<RatingListItem>();

		String[] categories = context.getResources().getStringArray(R.array.user_stats_categories);
		for (int i = 0; i < categories.length; i++) {
			String category = categories[i];
			RatingListItem ratingListItem = new RatingListItem(getIconByCategory(i), category);
			selectionItems.add(ratingListItem);
		}
		return selectionItems;
	}

	/**
	 * Fill list according :
	 * Live - Standard
	 * Live - Blitz
	 * Live - Bullet
	 * Daily - Chess
	 * Daily - Chess960
	 * Tactics
	 * Coach Manager
	 *
	 * @param index
	 * @return Drawable icon for index
	 */
	private Drawable getIconByCategory(int index) {
		switch (index) {
			case LIVE_STANDARD:
				return getResources().getDrawable(R.drawable.ic_live_game);
			case LIVE_BLITZ:
				return getResources().getDrawable(R.drawable.ic_live_blitz);
			case LIVE_LIGHTNING:
				return getResources().getDrawable(R.drawable.ic_live_bullet);
			case DAILY_CHESS:
				return getResources().getDrawable(R.drawable.ic_daily_game);
			case DAILY_CHESS960:
				return getResources().getDrawable(R.drawable.ic_daily960_game);
			case TACTICS:
				return getResources().getDrawable(R.drawable.ic_tactics_game);
			default: // case CHESS_MENTOR:
				return getResources().getDrawable(R.drawable.ic_tactics_game);
		}
	}
}
