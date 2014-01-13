package com.chess.ui.fragments.videos;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.CommonViewedItem;
import com.chess.backend.entity.api.VideoSingleItem;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveVideosListTask;
import com.chess.statics.StaticData;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.DarkSpinnerAdapter;
import com.chess.ui.adapters.VideosCursorAdapter;
import com.chess.ui.adapters.VideosPaginationAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.01.13
 * Time: 19:12
 */
public class VideoCategoriesFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

	public static final String SECTION_NAME = "section_name";
	public static final int WATCH_VIDEO_REQUEST = 9896;

	private VideosCursorUpdateListener videosCursorUpdateListener;
	private SaveVideosUpdateListener saveVideosUpdateListener;
	private VideosCursorAdapter videosAdapter;
	protected SparseBooleanArray viewedVideosMap;
	protected List<String> categoriesNames;
	protected List<Integer> categoriesIds;
	protected long playButtonClickTime;
	protected int currentPlayingId;
	private int previousCategoryId;
	protected String sectionName;

	protected TextView emptyView;
	private ListView listView;

	protected VideosPaginationAdapter paginationAdapter;
	protected Spinner categorySpinner;
	protected int selectedCategoryId;

	public VideoCategoriesFragment() {
		Bundle bundle = new Bundle();
		bundle.putString(SECTION_NAME, Symbol.EMPTY);
		setArguments(bundle);
	}

	public static VideoCategoriesFragment createInstance(String sectionName) {
		VideoCategoriesFragment fragment = new VideoCategoriesFragment();
		Bundle bundle = new Bundle();
		bundle.putString(SECTION_NAME, sectionName);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			sectionName = getArguments().getString(SECTION_NAME);
		} else {
			sectionName = savedInstanceState.getString(SECTION_NAME);
		}

		videosCursorUpdateListener = new VideosCursorUpdateListener();
		categoriesNames = new ArrayList<String>();
		categoriesIds = new ArrayList<Integer>();

		viewedVideosMap = new SparseBooleanArray();
		saveVideosUpdateListener = new SaveVideosUpdateListener();
		setAdapter(new VideosCursorAdapter(this, null));
		getAdapter().addViewedMap(viewedVideosMap);

		paginationAdapter = new VideosPaginationAdapter(getActivity(), getAdapter(), new VideosUpdateListener(), null);

		// restore state
		if (savedInstanceState != null) {
			playButtonClickTime = savedInstanceState.getLong(VideosFragment.CLICK_TIME);
			currentPlayingId = savedInstanceState.getInt(VideosFragment.CURRENT_PLAYING_ID);

			verifyAndSaveViewedState();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_common_categories_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.videos);

		widgetsInit(view);

		getActivityFace().showActionMenu(R.id.menu_search_btn, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(SECTION_NAME, sectionName);
		outState.putLong(VideosFragment.CLICK_TIME, playButtonClickTime);
		outState.putInt(VideosFragment.CURRENT_PLAYING_ID, currentPlayingId);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == WATCH_VIDEO_REQUEST) {

			verifyAndSaveViewedState();
		}
	}

	protected boolean fillCategories() {
		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.VIDEO_CATEGORIES));
		if (!(cursor != null && cursor.moveToFirst())) {
			showToast("Categories are not loaded");
			return false;
		}

		do {
			categoriesNames.add(DbDataManager.getString(cursor, DbScheme.V_NAME));
			categoriesIds.add(Integer.valueOf(DbDataManager.getString(cursor, DbScheme.V_CATEGORY_ID)));
		} while (cursor.moveToNext());

		return true;
	}

	private void loadFromDb() {
		new LoadDataFromDbTask(videosCursorUpdateListener,
				DbHelper.getVideosByCategory(previousCategoryId),
				getContentResolver()).executeTask();
	}

	private class VideosCursorUpdateListener extends ChessUpdateListener<Cursor> {

		@Override
		public void showProgress(boolean show) {
			if (!isTablet) { // don't show progress for pagination here for tablet
				super.showProgress(show);
			}
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			paginationAdapter.notifyDataSetChanged();
			getAdapter().changeCursor(returnedObj);
			paginationAdapter.notifyDataSetChanged();
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.error);
				showEmptyView(true);
			}
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		if (view.getId() == R.id.completedIconTxt) {
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			Cursor cursor = (Cursor) listView.getItemAtPosition(position);

			currentPlayingId = DbDataManager.getInt(cursor, DbScheme.V_ID);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse(DbDataManager.getString(cursor, DbScheme.V_URL)), "video/*");
			startActivityForResult(Intent.createChooser(intent, getString(R.string.select_player)), WATCH_VIDEO_REQUEST);

			// start record time to watch
			playButtonClickTime = System.currentTimeMillis();
		}
	}

	private void verifyAndSaveViewedState() {
		long resumeFromVideoTime = System.currentTimeMillis();

		if (resumeFromVideoTime - playButtonClickTime > VideosFragment.WATCHED_TIME) {
			CommonViewedItem item = new CommonViewedItem(currentPlayingId, getUsername());
			DbDataManager.saveVideoViewedState(getContentResolver(), item);

			// update current list
			viewedVideosMap.put(currentPlayingId, true);
			getAdapter().addViewedMap(viewedVideosMap);
			getAdapter().notifyDataSetChanged();
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
		// don't process clicks on pending view
		if (position == videosAdapter.getCount()) {
			return;
		}
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		long videoId = DbDataManager.getLong(cursor, DbScheme.V_ID);
		getActivityFace().openFragment(VideoDetailsFragment.createInstance(videoId));
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		selectedCategoryId = categoriesIds.get(position);

		updateByCategory();
	}

	protected void updateByCategory() {
		if (need2update || selectedCategoryId != previousCategoryId) {
			previousCategoryId = selectedCategoryId;
			need2update = true;

			// clear current list
			getAdapter().changeCursor(null);

			if (isNetworkAvailable()) {
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.getInstance().CMD_VIDEOS);
				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
				loadItem.addRequestParams(RestHelper.P_CATEGORY_ID, selectedCategoryId);
				loadItem.addRequestParams(RestHelper.P_ITEMS_PER_PAGE, RestHelper.DEFAULT_ITEMS_PER_PAGE);

				paginationAdapter.updateLoadItem(loadItem);
			} else {
				loadFromDb();
			}
		} else {
			paginationAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	private class VideosUpdateListener extends ChessUpdateListener<VideoSingleItem.Data> {

		@Override
		public void updateListData(List<VideoSingleItem.Data> itemsList) {
			logTest("VideosUpdateListener updateListData itemsList count = " + itemsList.size());


			new SaveVideosListTask(saveVideosUpdateListener, itemsList, getContentResolver()).executeTask();
		}
	}

	private class SaveVideosUpdateListener extends ChessUpdateListener<VideoSingleItem.Data> {

		@Override
		public void updateData(VideoSingleItem.Data returnedObj) {
			super.updateData(returnedObj);

			logTest("SaveVideosUpdateListener updateData returnedObj= " + returnedObj);

			loadFromDb();
		}
	}

	protected void showEmptyView(boolean show) {
		if (show) {
			// don't hide loadingView if it's loading
			if (loadingView.getVisibility() != View.VISIBLE) {
				loadingView.setVisibility(View.GONE);
			}

			emptyView.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		} else {
			emptyView.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	protected void setAdapter(VideosCursorAdapter adapter) {
		this.videosAdapter = adapter;
	}

	protected VideosCursorAdapter getAdapter() {
		return videosAdapter;
	}

	protected void widgetsInit(View view) {
		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(paginationAdapter);
		listView.setOnItemClickListener(this);

		// get viewed marks
		Cursor cursor = DbDataManager.getVideoViewedCursor(getActivity(), getUsername());
		if (cursor != null) {
			do {
				int videoId = DbDataManager.getInt(cursor, DbScheme.V_ID);
				boolean isViewed = DbDataManager.getInt(cursor, DbScheme.V_DATA_VIEWED) > 0;
				viewedVideosMap.put(videoId, isViewed);
			} while (cursor.moveToNext());
			cursor.close();
		}

		boolean loaded = categoriesNames.size() != 0 || fillCategories();

		if (loaded) {
			int position = 0;
			if (TextUtils.isEmpty(sectionName)) {
				selectedCategoryId = categoriesIds.get(0);
			} else {
				for (position = 0; position < categoriesNames.size(); position++) {
					String category = categoriesNames.get(position);
					if (category.equals(sectionName)) {
						selectedCategoryId = categoriesIds.get(position);
						break;
					}
				}
			}

			categorySpinner = (Spinner) view.findViewById(R.id.categoriesSpinner);
			categorySpinner.setAdapter(new DarkSpinnerAdapter(getActivity(), categoriesNames));
			categorySpinner.setOnItemSelectedListener(this);
			categorySpinner.setSelection(position);  // TODO remember last selection.
		}
	}


}
