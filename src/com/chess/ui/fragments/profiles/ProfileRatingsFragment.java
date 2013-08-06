package com.chess.ui.fragments.profiles;

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
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.tasks.SaveUserStatsTask;
import com.chess.model.RatingListItem;
import com.chess.ui.adapters.RatingsAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.stats.StatsGameFragment;
import com.chess.ui.fragments.stats.StatsGameTacticsFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.ui.views.drawables.IconDrawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 05.08.13
 * Time: 10:32
 */
public class ProfileRatingsFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener, ItemClickListenerFace {

	private final static int DAILY_CHESS = 0;
	private final static int LIVE_STANDARD = 1;
	private final static int LIVE_BLITZ = 2;
	private final static int LIVE_LIGHTNING = 3;
	private final static int DAILY_CHESS960 = 4;
	private final static int TACTICS = 5;
	private final static int LESSONS = 6;
	private static final String USERNAME = "username";

	private List<RatingListItem> ratingList;
	private RatingsAdapter ratingsAdapter;
	private String username;
	private SaveStatsUpdateListener saveStatsUpdateListener;
	private StatsItemUpdateListener statsItemUpdateListener;

	public ProfileRatingsFragment() {}

	public static ProfileRatingsFragment createInstance(String username) {
		ProfileRatingsFragment fragment = new ProfileRatingsFragment();
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

		statsItemUpdateListener = new StatsItemUpdateListener();
		saveStatsUpdateListener = new SaveStatsUpdateListener();

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_white_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		ratingList = createStatsList(getActivity());
		ratingsAdapter = new RatingsAdapter(getActivity(), ratingList);
		listView.setAdapter(ratingsAdapter);
		listView.setOnItemClickListener(this);

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_message, true);
		getActivityFace().showActionMenu(R.id.menu_challenge, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);
	}

	@Override
	public void onResume() {
		super.onResume();

		// get full stats
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_USER_STATS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_VIEW_USERNAME, username);

		new RequestJsonTask<UserStatsItem>(statsItemUpdateListener).executeTask(loadItem);

//		fillUserStats();  // TODO adjust properly when API is ready
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(USERNAME, username);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		switch (position) {
			case LIVE_STANDARD:
			case LIVE_BLITZ:
			case LIVE_LIGHTNING:
			case DAILY_CHESS:
			case DAILY_CHESS960:
				getActivityFace().openFragment(StatsGameFragment.createInstance(position));
				break;
			case TACTICS:
				getActivityFace().openFragment(new StatsGameTacticsFragment());
				break;
			case LESSONS: // not used yet
				getActivityFace().openFragment(new StatsGameTacticsFragment()); // TODO adjust Lessons
				break;
		}
	}

	private void fillUserStats() {
		// fill ratings
		String[] argument = new String[]{getAppData().getUsername()};

		{// standard
			Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.Tables.USER_STATS_LIVE_STANDARD.ordinal()],
					DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
			if (cursor.moveToFirst()) {
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);
				ratingList.get(LIVE_STANDARD).setValue(currentRating);
			}
		}
		{// blitz
			Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.Tables.USER_STATS_LIVE_BLITZ.ordinal()],
					DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
			if (cursor.moveToFirst()) {
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);
				ratingList.get(LIVE_BLITZ).setValue(currentRating);
			}
		}
		{// bullet
			Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.Tables.USER_STATS_LIVE_LIGHTNING.ordinal()],
					DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
			if (cursor.moveToFirst()) {
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);

				ratingList.get(LIVE_LIGHTNING).setValue(currentRating);
			}
		}
		{// chess
			Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.Tables.USER_STATS_DAILY_CHESS.ordinal()],
					DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
			if (cursor.moveToFirst()) {
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);
				ratingList.get(DAILY_CHESS).setValue(currentRating);
			}
		}
		{// chess960
			Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.Tables.USER_STATS_DAILY_CHESS960.ordinal()],
					DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
			if (cursor.moveToFirst()) {
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);
				ratingList.get(DAILY_CHESS960).setValue(currentRating);
			}
		}
		{// tactics
			Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.Tables.USER_STATS_TACTICS.ordinal()],
					DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
			if (cursor.moveToFirst()) {
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);
				ratingList.get(TACTICS).setValue(currentRating);
			}
		}
		{// chess mentor
			Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.Tables.USER_STATS_LESSONS.ordinal()],
					DBDataManager.PROJECTION_USER_CURRENT_RATING, DBDataManager.SELECTION_USER, argument, null);
			if (cursor.moveToFirst()) {
				int currentRating = DBDataManager.getInt(cursor, DBConstants.V_CURRENT);
				ratingList.get(LESSONS).setValue(currentRating);
			}
		}

		ratingsAdapter.notifyDataSetInvalidated();
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	private class StatsItemUpdateListener extends ChessUpdateListener<UserStatsItem> {

		public StatsItemUpdateListener() {
			super(UserStatsItem.class);
		}

		@Override
		public void updateData(UserStatsItem returnedObj) {
			super.updateData(returnedObj);

			new SaveUserStatsTask(saveStatsUpdateListener, returnedObj.getData(), getContentResolver()).executeTask();
		}
	}

	private class SaveStatsUpdateListener extends ChessUpdateListener<UserStatsItem.Data> {

		@Override
		public void updateData(UserStatsItem.Data returnedObj) {
			super.updateData(returnedObj);

			fillUserStats();
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
	 * @param index to get needed drawable
	 * @return Drawable icon for index
	 */
	private Drawable getIconByCategory(int index) {
		Context context = getActivity();
		switch (index) {
			case LIVE_STANDARD:
				return new IconDrawable(context, R.string.ic_live_standard, R.color.hint_text, R.dimen.glyph_icon_big);
			case LIVE_BLITZ:
				return new IconDrawable(context, R.string.ic_live_blitz, R.color.hint_text, R.dimen.glyph_icon_big);
			case LIVE_LIGHTNING:
				return new IconDrawable(context, R.string.ic_live_bullet, R.color.hint_text, R.dimen.glyph_icon_big);
			case DAILY_CHESS:
				return new IconDrawable(context, R.string.ic_daily_game, R.color.hint_text, R.dimen.glyph_icon_big);
			case DAILY_CHESS960:
				return new IconDrawable(context, R.string.ic_daily960_game, R.color.hint_text, R.dimen.glyph_icon_big);
			case TACTICS:
				return new IconDrawable(context, R.string.ic_help, R.color.hint_text, R.dimen.glyph_icon_big);
			default: // case LESSONS:
				return new IconDrawable(context, R.string.ic_lessons, R.color.hint_text, R.dimen.glyph_icon_big);
		}
	}
}