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
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.stats.UserStatsItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.db.tasks.SaveUserStatsTask;
import com.chess.model.RatingListItem;
import com.chess.ui.adapters.RatingsAdapter;
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
public class ProfileRatingsFragment extends ProfileBaseFragment implements AdapterView.OnItemClickListener, ItemClickListenerFace {

	private final static int DAILY_CHESS = 0;
	private final static int LIVE_STANDARD = 1;
	private final static int LIVE_BLITZ = 2;
	private final static int LIVE_LIGHTNING = 3;
	private final static int DAILY_CHESS960 = 4;
	private final static int TACTICS = 5;
	private final static int LESSONS = 6;

	private List<RatingListItem> ratingList;
	private RatingsAdapter ratingsAdapter;
	private SaveStatsUpdateListener saveStatsUpdateListener;
	private StatsItemUpdateListener statsItemUpdateListener;
	private TextView emptyView;
	private ListView listView;

	public ProfileRatingsFragment() {
	}

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

		statsItemUpdateListener = new StatsItemUpdateListener();
		saveStatsUpdateListener = new SaveStatsUpdateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.white_list_view_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		emptyView = (TextView) view.findViewById(R.id.emptyView);

		listView = (ListView) view.findViewById(R.id.listView);
		ratingList = createStatsList(getActivity());
		ratingsAdapter = new RatingsAdapter(getActivity(), ratingList);
		listView.setAdapter(ratingsAdapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (need2update) {
			// get full stats
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_USER_STATS);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
			loadItem.addRequestParams(RestHelper.P_USERNAME, username);

			new RequestJsonTask<UserStatsItem>(statsItemUpdateListener).executeTask(loadItem);
		} else {
			fillUserStats();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		RatingListItem ratingListItem = ratingList.get(position);

		int code = Integer.parseInt(ratingListItem.getCode());
		openStatsForUser(code, username);
	}

	private void fillUserStats() {
		// fill ratings
		String[] argument = new String[]{username};

		List<RatingListItem> itemsToRemove = new ArrayList<RatingListItem>();
		{// standard
			Cursor cursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.USER_STATS_LIVE_STANDARD.ordinal()],
					DbDataManager.PROJECTION_USER_CURRENT_RATING, DbDataManager.SELECTION_USER, argument, null);
			if (cursor != null && cursor.moveToFirst()) {
				int currentRating = DbDataManager.getInt(cursor, DbScheme.V_CURRENT);
				if (currentRating == 0) {
					itemsToRemove.add(ratingList.get(LIVE_STANDARD));
				} else {
					ratingList.get(LIVE_STANDARD).setValue(currentRating);
				}
			}
		}
		{// blitz
			Cursor cursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.USER_STATS_LIVE_BLITZ.ordinal()],
					DbDataManager.PROJECTION_USER_CURRENT_RATING, DbDataManager.SELECTION_USER, argument, null);
			if (cursor != null && cursor.moveToFirst()) {
				int currentRating = DbDataManager.getInt(cursor, DbScheme.V_CURRENT);
				if (currentRating == 0) {
					itemsToRemove.add(ratingList.get(LIVE_BLITZ));
				} else {
					ratingList.get(LIVE_BLITZ).setValue(currentRating);
				}
			}
		}
		{// bullet
			Cursor cursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.USER_STATS_LIVE_LIGHTNING.ordinal()],
					DbDataManager.PROJECTION_USER_CURRENT_RATING, DbDataManager.SELECTION_USER, argument, null);
			if (cursor != null && cursor.moveToFirst()) {
				int currentRating = DbDataManager.getInt(cursor, DbScheme.V_CURRENT);
				if (currentRating == 0) {
					itemsToRemove.add(ratingList.get(LIVE_LIGHTNING));
				} else {
					ratingList.get(LIVE_LIGHTNING).setValue(currentRating);
				}
			}
		}
		{// chess
			Cursor cursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.USER_STATS_DAILY_CHESS.ordinal()],
					DbDataManager.PROJECTION_USER_CURRENT_RATING, DbDataManager.SELECTION_USER, argument, null);
			if (cursor != null && cursor.moveToFirst()) {
				int currentRating = DbDataManager.getInt(cursor, DbScheme.V_CURRENT);
				if (currentRating == 0) {
					itemsToRemove.add(ratingList.get(DAILY_CHESS));
				} else {
					ratingList.get(DAILY_CHESS).setValue(currentRating);
				}
			}
		}
		{// chess960
			Cursor cursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.USER_STATS_DAILY_CHESS960.ordinal()],
					DbDataManager.PROJECTION_USER_CURRENT_RATING, DbDataManager.SELECTION_USER, argument, null);
			if (cursor != null && cursor.moveToFirst()) {
				int currentRating = DbDataManager.getInt(cursor, DbScheme.V_CURRENT);
				if (currentRating == 0) {
					itemsToRemove.add(ratingList.get(DAILY_CHESS960));
				} else {
					ratingList.get(DAILY_CHESS960).setValue(currentRating);
				}
			}
		}
		{// tactics
			Cursor cursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.USER_STATS_TACTICS.ordinal()],
					DbDataManager.PROJECTION_USER_CURRENT_RATING, DbDataManager.SELECTION_USER, argument, null);
			if (cursor != null && cursor.moveToFirst()) {
				int currentRating = DbDataManager.getInt(cursor, DbScheme.V_CURRENT);
				if (currentRating == 0) {
					itemsToRemove.add(ratingList.get(TACTICS));
				} else {
					ratingList.get(TACTICS).setValue(currentRating);
				}
			}
		}
		{// chess mentor
			Cursor cursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.USER_STATS_LESSONS.ordinal()],
					DbDataManager.PROJECTION_USER_CURRENT_RATING, DbDataManager.SELECTION_USER, argument, null);
			if (cursor != null && cursor.moveToFirst()) {
				int currentRating = DbDataManager.getInt(cursor, DbScheme.V_CURRENT);
				if (currentRating == 0) {
					itemsToRemove.add(ratingList.get(LESSONS));
				} else {
					ratingList.get(LESSONS).setValue(currentRating);
				}
			}
		}

		ratingList.removeAll(itemsToRemove);

		if (ratingList.size() == 0) {
			listView.setVisibility(View.GONE);

			emptyView.setVisibility(View.VISIBLE);
			emptyView.setText(R.string.no_rated_activity);
		} else {
			emptyView.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}

		ratingsAdapter.notifyDataSetInvalidated();
		need2update = false;
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

			new SaveUserStatsTask(saveStatsUpdateListener, returnedObj.getData(), getContentResolver(), username).executeTask();
		}
	}

	private class SaveStatsUpdateListener extends ChessUpdateListener<UserStatsItem.Data> {

		@Override
		public void updateData(UserStatsItem.Data returnedObj) {
			super.updateData(returnedObj);

			fillUserStats();
		}

	}


	private List<RatingListItem> createStatsList(Context context) {
		ArrayList<RatingListItem> selectionItems = new ArrayList<RatingListItem>();

		String[] categories = context.getResources().getStringArray(R.array.game_stats_categories);
		for (int i = 0; i < categories.length; i++) {
			String category = categories[i];
			RatingListItem ratingListItem = new RatingListItem(getIconByCategory(i), category);
			ratingListItem.setCode(String.valueOf(i));
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

/*
	If there is no results-based rating for a given rating type,
	then we should just hide the whole row for that rating type in the UI.

	If the user (friend or self) has NO rated activity (so no line items),
	 we would show a message, like No rated activity.
*/
}