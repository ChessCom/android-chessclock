package com.chess.ui.fragments.videos;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.CommonFeedCategoryItem;
import com.chess.backend.entity.api.CommonViewedItem;
import com.chess.backend.entity.api.VideoSingleItem;
import com.chess.backend.entity.api.VideosItem;
import com.chess.statics.AppConstants;
import com.chess.statics.Symbol;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.SaveVideoCategoriesTask;
import com.chess.model.CurriculumItems;
import com.chess.ui.activities.VideoActivity;
import com.chess.ui.adapters.CommonCategoriesCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.01.13
 * Time: 19:12
 */
public class VideosFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener,
		ExpandableListView.OnGroupClickListener {

	public static final String CLICK_TIME = "click time";
	public static final String CURRENT_PLAYING_ID = "current playing id";

	public static final String GREY_COLOR_DIVIDER = "##";
	// 11/15/12 | 27 min
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");
	public static final String SLASH_DIVIDER = " | ";

	private static final int LIBRARY = 6;
	private static final int WATCH_VIDEO_REQUEST = 9898;
	public static final long WATCHED_TIME = 3 * 60 * 1000;
	private static final int USER_PRO_RATING = 1600;

	private ViewHolder holder;
	private ForegroundColorSpan foregroundSpan;

	private ListView listView;
	private ExpandableListView expListView;
	private View loadingView;
	private TextView emptyView;

	private CommonCategoriesCursorAdapter categoriesCursorAdapter;
	private VideoGroupsListAdapter curriculumAdapter;

	private VideosItemUpdateListener latestItemUpdateListener;

	private VideoCategoriesUpdateListener videoCategoriesUpdateListener;
	private SaveVideoCategoriesUpdateListener saveVideoCategoriesUpdateListener;

	private boolean headerDataLoaded;
	private long headerVideoId;
	private VideoSingleItem.Data headerData;

	private CurriculumItems curriculumItems;
	private boolean curriculumMode;
	private long playButtonClickTime;
	private long currentPlayingId;
	private SparseBooleanArray curriculumViewedMap;
	private View headerView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();

		// restore state
		if (savedInstanceState != null) {
			playButtonClickTime = savedInstanceState.getLong(CLICK_TIME);
			currentPlayingId = savedInstanceState.getLong(CURRENT_PLAYING_ID);

			verifyAndSaveViewedState();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_videos_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.videos);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		{ // Library mode init
			listView = (ListView) view.findViewById(R.id.listView);
			// add header
			headerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_videos_thumb_list_item, null, false);
			headerView.setOnClickListener(this);
			View footerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_videos_curriculum_footer, null, false);
			footerView.setOnClickListener(this);
			listView.addHeaderView(headerView);
			listView.addFooterView(footerView);
			listView.setAdapter(categoriesCursorAdapter);
			listView.setOnItemClickListener(this);

			// TODO create loading view for header
			holder = new ViewHolder();
			holder.titleTxt = (TextView) headerView.findViewById(R.id.titleTxt);
			holder.authorTxt = (TextView) headerView.findViewById(R.id.authorTxt);
			holder.dateTxt = (TextView) headerView.findViewById(R.id.dateTxt);
		}

		{ // Curriculum mode
			expListView = (ExpandableListView) view.findViewById(R.id.expListView);
			expListView.setOnGroupClickListener(this);
			expListView.setGroupIndicator(null);
		}

		showLibrary();

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_search_btn, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onResume() {
		super.onResume();

		verifyAndSaveViewedState();
	}

	private void showLibrary() {
		boolean show = !curriculumMode;
		listView.setVisibility(show ? View.VISIBLE : View.GONE);
		expListView.setVisibility(show ? View.GONE : View.VISIBLE);

		// get viewed marks here after we return to this from details fragment
		Cursor cursor = DbDataManager.getVideoViewedCursor(getActivity(), getUsername());
		if (cursor != null) {
			do {
				int videoId = DbDataManager.getInt(cursor, DbScheme.V_ID);
				boolean isViewed = DbDataManager.getInt(cursor, DbScheme.V_DATA_VIEWED) > 0;
				curriculumViewedMap.put(videoId, isViewed);
			} while (cursor.moveToNext());
			cursor.close();
		}

		if (show) {
			if (need2update) {

				// get saved categories
				Cursor categoriesCursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.VIDEO_CATEGORIES.ordinal()], null, null, null, null);

				if (categoriesCursor != null && categoriesCursor.moveToFirst()) {
					categoriesCursorAdapter.changeCursor(categoriesCursor);
				}

				if (isNetworkAvailable()) {
					updateData();
					getCategories();
				}

			} else { // load data to listHeader view
				fillListViewHeaderData();
				categoriesCursorAdapter.notifyDataSetChanged();
			}
		} else {
			expListView.setAdapter(curriculumAdapter);
			expandLastSection();
		}
	}

	/**
	 * We should auto-open the category with the lowest unfinished video/lesson.
	 * so, first time i use the app the Rules and Basics for both would be expanded.
	 * that's more beautiful and inviting!
	 */
	private void expandLastSection() {
		// get last unfinished video
		if (ICS_PLUS_API) {
			expListView.expandGroup(0, true); // TODO adjust properly last incomplete
		} else {
			expListView.expandGroup(0);
		}
	}

	private void updateData() {
		// request latest data for the header
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_VIDEOS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_ITEMS_PER_PAGE, 1);

		new RequestJsonTask<VideosItem>(latestItemUpdateListener).executeTask(loadItem);
	}

	private void getCategories() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_VIDEO_CATEGORIES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<CommonFeedCategoryItem>(videoCategoriesUpdateListener).executeTask(loadItem);
	}

	@Override
	public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
		if (groupPosition == LIBRARY) { // turn in to library mode
			getAppData().setUserChooseVideoLibrary(true);
			curriculumMode = false;
			showLibrary();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_search_btn:
				getActivityFace().openFragment(new VideosSearchFragment());
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//		boolean headerAdded = listView.getHeaderViewsCount() > 0; // used to check if header added
//		int offset = headerAdded ? -1 : 0;

		if (position != 0) { // if listView header
			Cursor cursor = (Cursor) parent.getItemAtPosition(position);
			String sectionName = DbDataManager.getString(cursor, DbScheme.V_NAME);

			getActivityFace().openFragment(VideoCategoriesFragment.createInstance(sectionName));
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v.getId() == R.id.videoThumbItemView) {
			if (headerDataLoaded) {
				getActivityFace().openFragment(VideoDetailsFragment.createInstance(headerVideoId));
			}
		} else if (v.getId() == R.id.titleTxt) { // Clicked on title in Curriculum mode, open details
			View parent = (View) v.getParent();
			if (parent != null) {
				parent.performClick();
			}
			Integer childPosition = (Integer) v.getTag(R.id.list_item_id);
			Integer groupPosition = (Integer) v.getTag(R.id.list_item_id_group);
			if (childPosition == null || groupPosition == null) {
				return;
			}

			int id = curriculumItems.getIds()[groupPosition][childPosition];
			getActivityFace().openFragment(VideoDetailsFragment.createInstance(id));

		} else if (v.getId() == R.id.completedIconTxt) {
			Integer childPosition = (Integer) v.getTag(R.id.list_item_id);
			Integer groupPosition = (Integer) v.getTag(R.id.list_item_id_group);
			if (childPosition == null || groupPosition == null) {
				return;
			}

			String currentPlayingLink = curriculumItems.getUrls()[groupPosition][childPosition];
			currentPlayingId = curriculumItems.getIds()[groupPosition][childPosition];

			Intent intent = new Intent(getActivity(), VideoActivity.class);
			intent.putExtra(AppConstants.VIDEO_LINK, currentPlayingLink);
			startActivity(intent);

			// start record time to watch
			playButtonClickTime = System.currentTimeMillis();
		} else if (v.getId() == R.id.curriculumHeader) {
			getAppData().setUserChooseVideoLibrary(false);
			curriculumMode = true;
			showLibrary();
		}
	}

	private void verifyAndSaveViewedState() {
		long resumeFromVideoTime = System.currentTimeMillis();

		if (resumeFromVideoTime - playButtonClickTime > WATCHED_TIME) {
			CommonViewedItem item = new CommonViewedItem(currentPlayingId, getUsername());
			DbDataManager.saveVideoViewedState(getContentResolver(), item);

			// update current list
			curriculumViewedMap.put((int) currentPlayingId, true); // TODO test logic
			curriculumAdapter.notifyDataSetChanged();

			// save updates to DB
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(CLICK_TIME, playButtonClickTime);
		outState.putLong(CURRENT_PLAYING_ID, currentPlayingId);
	}

	private class VideoCategoriesUpdateListener extends ChessUpdateListener<CommonFeedCategoryItem> {
		public VideoCategoriesUpdateListener() {
			super(CommonFeedCategoryItem.class);
		}

		@Override
		public void updateData(CommonFeedCategoryItem returnedObj) {
			super.updateData(returnedObj);

			List<CommonFeedCategoryItem.Data> dataList = returnedObj.getData();
			for (CommonFeedCategoryItem.Data category : dataList) {
				category.setName(category.getName().replace(Symbol.AMP_CODE, Symbol.AMP));
			}

			new SaveVideoCategoriesTask(saveVideoCategoriesUpdateListener, dataList, getContentResolver()).executeTask();
		}
	}

	private class VideosItemUpdateListener extends ChessUpdateListener<VideosItem> {

		public VideosItemUpdateListener() {
			super(VideosItem.class);
		}

		@Override
		public void updateData(VideosItem returnedObj) {
			headerData = returnedObj.getData().get(0);

			headerVideoId = headerData.getVideoId();
			// save in Db to open in Details View
			ContentResolver contentResolver = getContentResolver();

			Uri uri = DbScheme.uriArray[DbScheme.Tables.VIDEOS.ordinal()];
			String[] arguments = new String[1];
			arguments[0] = String.valueOf(headerData.getTitle());
			Cursor cursor = contentResolver.query(uri, DbDataManager.PROJECTION_TITLE,
					DbDataManager.SELECTION_TITLE, arguments, null);

			ContentValues values = DbDataManager.putVideoItemToValues(headerData);

			DbDataManager.updateOrInsertValues(contentResolver, cursor, uri, values);

			headerDataLoaded = true;

			fillListViewHeaderData();
		}
	}

	private void fillListViewHeaderData() {
		if (!headerDataLoaded) {
			return;
		}

		String firstName = headerData.getFirstName();
		String chessTitle = headerData.getChessTitle();
		String lastName = headerData.getLastName();
		CharSequence authorStr;
		if (TextUtils.isEmpty(chessTitle)) {
			authorStr = firstName + Symbol.SPACE + lastName;
		} else {
			authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER
					+ Symbol.SPACE + firstName + Symbol.SPACE + lastName;
			authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, foregroundSpan);
		}
		holder.authorTxt.setText(authorStr);
		holder.titleTxt.setText(headerData.getTitle());
		holder.dateTxt.setText(dateFormatter.format(new Date(headerData.getCreateDate()))
				+ SLASH_DIVIDER + getString(R.string.min_arg, headerData.getMinutes()));

		headerView.invalidate();
	}

	protected class ViewHolder {
		public TextView titleTxt;
		public TextView authorTxt;
		public TextView dateTxt;
	}

	private class SaveVideoCategoriesUpdateListener extends ChessUpdateListener<CommonFeedCategoryItem.Data> {

		@Override
		public void updateData(CommonFeedCategoryItem.Data returnedObj) {
			// get saved categories
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.VIDEO_CATEGORIES));
			if (cursor.moveToFirst()) {
				categoriesCursorAdapter.changeCursor(cursor);
				listView.setAdapter(categoriesCursorAdapter);

				need2update = false;
			}
		}
	}

	private void showLoadingView(boolean show) {
		if (show) {
			emptyView.setVisibility(View.GONE);
			loadingView.setVisibility(View.VISIBLE);
		} else {
			listView.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
		}
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	private void init() {
		int lightGrey = getResources().getColor(R.color.new_subtitle_light_grey);
		foregroundSpan = new ForegroundColorSpan(lightGrey);

		int userRating = DbDataManager.getUserRatingFromUsersStats(getActivity(), DbScheme.Tables.USER_STATS_DAILY_CHESS.ordinal(), getUsername());

		// TODO add api logic to check if user saw all videos
		// everyone is presented with CURRICULUM view by default unless:
		// a) they have seen every video or lesson, or
		// b) they have a rating above 1600, or
		// c) they chose "full library" from the bottom of the curriculum list ( http://i.imgur.com/aWcHqUh.png )

		curriculumMode = !(getAppData().isUserChooseVideoLibrary() || userRating > USER_PRO_RATING);

		curriculumItems = new CurriculumItems();

		curriculumItems.setCategories(getResources().getStringArray(R.array.videos_curriculum));
		{ // Titles
			String[] beginners = getResources().getStringArray(R.array.video_cur_beginners_titles);
			String[] openings = getResources().getStringArray(R.array.video_cur_openings_titles);
			String[] tactics = getResources().getStringArray(R.array.video_cur_tactics_titles);
			String[] strategy = getResources().getStringArray(R.array.video_cur_strategy_titles);
			String[] endgames = getResources().getStringArray(R.array.video_cur_endgames_titles);
			String[] amazingGames = getResources().getStringArray(R.array.video_cur_amazing_games_titles);

			curriculumItems.setTitles(new String[][]{beginners, openings, tactics, strategy, endgames, amazingGames});
		}
		{ // Links
			String[] beginners = getResources().getStringArray(R.array.video_cur_beginners);
			String[] openings = getResources().getStringArray(R.array.video_cur_openings);
			String[] tactics = getResources().getStringArray(R.array.video_cur_tactics);
			String[] strategy = getResources().getStringArray(R.array.video_cur_strategy);
			String[] endgames = getResources().getStringArray(R.array.video_cur_endgames);
			String[] amazingGames = getResources().getStringArray(R.array.video_cur_amazing_games);

			curriculumItems.setUrls(new String[][]{beginners, openings, tactics, strategy, endgames, amazingGames});
		}
		{ // Ids
			int[] beginners = getResources().getIntArray(R.array.video_cur_beginners_ids);
			int[] openings = getResources().getIntArray(R.array.video_cur_openings_ids);
			int[] tactics = getResources().getIntArray(R.array.video_cur_tactics_ids);
			int[] strategy = getResources().getIntArray(R.array.video_cur_strategy_ids);
			int[] endgames = getResources().getIntArray(R.array.video_cur_endgames_ids);
			int[] amazingGames = getResources().getIntArray(R.array.video_cur_amazing_games_ids);

			curriculumItems.setIds(new int[][]{beginners, openings, tactics, strategy, endgames, amazingGames});
		}

		categoriesCursorAdapter = new CommonCategoriesCursorAdapter(getActivity(), null);
		latestItemUpdateListener = new VideosItemUpdateListener();

		curriculumViewedMap = new SparseBooleanArray();

		videoCategoriesUpdateListener = new VideoCategoriesUpdateListener();
		saveVideoCategoriesUpdateListener = new SaveVideoCategoriesUpdateListener();
		curriculumAdapter = new VideoGroupsListAdapter(this, curriculumItems);   // categories, titles
	}

	public class VideoGroupsListAdapter extends BaseExpandableListAdapter {
		private final LayoutInflater inflater;
		private final int watchedTextColor;
		private final int unWatchedTextColor;
		private final int watchedIconColor;
		private final int unWatchedIconColor;
		private final CurriculumItems items;
		private ItemClickListenerFace clickFace;

		public VideoGroupsListAdapter(ItemClickListenerFace clickFace, CurriculumItems items) {
			this.clickFace = clickFace;
			this.items = items;
			inflater = LayoutInflater.from(getActivity());
			watchedTextColor = getResources().getColor(R.color.new_light_grey_3);
			unWatchedTextColor = getResources().getColor(R.color.new_text_blue);
			watchedIconColor = getResources().getColor(R.color.new_light_grey_2);
			unWatchedIconColor = getResources().getColor(R.color.orange_button);
		}

		@Override
		public int getGroupCount() {
			return items.getCategories().length;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return items.getTitles()[groupPosition].length;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return items.getCategories()[groupPosition];
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return items.getTitles()[groupPosition][childPosition];
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.new_common_titled_list_item, parent, false);
				holder = new ViewHolder();

				holder.text = (TextView) convertView.findViewById(R.id.headerTitleTxt);
				holder.icon = (TextView) convertView.findViewById(R.id.headerIconTxt);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.text.setText(getGroup(groupPosition).toString());
			if (groupPosition == LIBRARY) {
				holder.icon.setText(Symbol.EMPTY);
			} else {
				if (isExpanded) {
					holder.icon.setText(R.string.ic_up);
				} else {
					holder.icon.setText(R.string.ic_down);
				}
			}

			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.new_completed_list_item, parent, false);
				holder = new ViewHolder();

				holder.text = (TextView) convertView.findViewById(R.id.titleTxt);
				holder.icon = (TextView) convertView.findViewById(R.id.completedIconTxt);
				convertView.setTag(holder);

				holder.text.setOnClickListener(clickFace);
				holder.icon.setOnClickListener(clickFace);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.text.setTag(R.id.list_item_id, childPosition);
			holder.text.setTag(R.id.list_item_id_group, groupPosition);

			holder.icon.setTag(R.id.list_item_id, childPosition);
			holder.icon.setTag(R.id.list_item_id_group, groupPosition);

			holder.text.setText(getChild(groupPosition, childPosition).toString());

			if (isVideoWatched(groupPosition, childPosition)) {
				holder.text.setTextColor(watchedTextColor);
				holder.icon.setTextColor(watchedIconColor);
				holder.icon.setText(R.string.ic_check);
			} else {
				holder.text.setTextColor(unWatchedTextColor);
				holder.icon.setTextColor(unWatchedIconColor);
				holder.icon.setText(R.string.ic_play);
			}

			return convertView;
		}

		private boolean isVideoWatched(int groupPosition, int childPosition) {
			return curriculumViewedMap.get(items.getIds()[groupPosition][childPosition], false);
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		private class ViewHolder {
			TextView text;
			TextView icon;
		}
	}

}
