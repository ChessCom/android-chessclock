package com.chess.ui.fragments.videos;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import com.chess.backend.entity.api.VideoItem;
import com.chess.statics.StaticData;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveVideosListTask;
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
	private static final int WATCH_VIDEO_REQUEST = 9896;

	private VideosCursorAdapter videosAdapter;

	private View loadingView;
	private TextView emptyView;
	private ListView listView;
	private VideosCursorUpdateListener videosCursorUpdateListener;
	private List<String> categoriesNames;
	private List<Integer> categoriesIds;
	private SaveVideosUpdateListener saveVideosUpdateListener;
	private SparseBooleanArray viewedVideosMap;
	private long playButtonClickTime;
	private int currentPlayingId;
	private int previousCategoryId;
	private String sectionName;
	private VideosPaginationAdapter paginationAdapter;

	public static VideoCategoriesFragment createInstance(String sectionName) {
		VideoCategoriesFragment frag = new VideoCategoriesFragment();
		Bundle bundle = new Bundle();
		bundle.putString(SECTION_NAME, sectionName);
		frag.setArguments(bundle);
		return frag;
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
		videosAdapter = new VideosCursorAdapter(this, null);
		videosAdapter.addViewedMap(viewedVideosMap);

		paginationAdapter = new VideosPaginationAdapter(getActivity(), videosAdapter, new VideosUpdateListener(), null);

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
			int sectionId;
			for (sectionId = 0; sectionId < categoriesNames.size(); sectionId++) {
				String category = categoriesNames.get(sectionId);
				if (category.equals(sectionName)) {
					break;
				}
			}

			Spinner categorySpinner = (Spinner) view.findViewById(R.id.categoriesSpinner);
			categorySpinner.setAdapter(new DarkSpinnerAdapter(getActivity(), categoriesNames));
			categorySpinner.setOnItemSelectedListener(this);
			categorySpinner.setSelection(sectionId);  // TODO remember last selection.
		}

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

	private boolean fillCategories() {
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
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			videosAdapter.changeCursor(returnedObj);
			if (paginationAdapter != null) {
				paginationAdapter.notifyDataSetChanged();
			}
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
			videosAdapter.addViewedMap(viewedVideosMap);
			videosAdapter.notifyDataSetChanged();
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
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		long videoId = DbDataManager.getLong(cursor, DbScheme.V_ID);
		getActivityFace().openFragment(VideoDetailsFragment.createInstance(videoId));
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Integer categoryId = categoriesIds.get(position);

		if (need2update || categoryId != previousCategoryId) {
			previousCategoryId = categoryId;
			need2update = true;

			// clear current list
			videosAdapter.changeCursor(null);
			// TODO add logic to check if new video was added on server since last fetch

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_VIDEOS);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
			loadItem.addRequestParams(RestHelper.P_CATEGORY_ID, categoryId);
			loadItem.addRequestParams(RestHelper.P_ITEMS_PER_PAGE, RestHelper.DEFAULT_ITEMS_PER_PAGE);

			paginationAdapter.updateLoadItem(loadItem);
		} else {
			paginationAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	private class VideosUpdateListener extends ChessUpdateListener<VideoItem.Data> {

		@Override
		public void updateListData(List<VideoItem.Data> itemsList) {
			new SaveVideosListTask(saveVideosUpdateListener, itemsList, getContentResolver()).executeTask();
		}
	}

	private class SaveVideosUpdateListener extends ChessUpdateListener<VideoItem.Data> {

		@Override
		public void updateData(VideoItem.Data returnedObj) {
			super.updateData(returnedObj);

			need2update = false;

			loadFromDb();
		}
	}

	private void showEmptyView(boolean show) {
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

}
