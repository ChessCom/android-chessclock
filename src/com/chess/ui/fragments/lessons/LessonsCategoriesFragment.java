package com.chess.ui.fragments.lessons;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.VideoItem;
import com.chess.backend.entity.new_api.VideoViewedItem;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveVideosListTask;
import com.chess.ui.adapters.DarkSpinnerAdapter;
import com.chess.ui.adapters.VideosThumbCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.videos.VideoCategoriesFragment;
import com.chess.ui.fragments.videos.VideoDetailsFragment;
import com.chess.ui.fragments.videos.VideosFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.07.13
 * Time: 14:56
 */
public class LessonsCategoriesFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

	public static final String SECTION_NAME = "section_name";
	private static final int WATCH_VIDEO_REQUEST = 9896;

	private VideosThumbCursorAdapter videosAdapter;

	private Spinner categorySpinner;
	private View loadingView;
	private TextView emptyView;
	private ListView listView;
	private VideosCursorUpdateListener videosCursorUpdateListener;
	private List<String> categoriesNames;
	private List<Integer> categoriesIds;
	private SaveVideosUpdateListener saveVideosUpdateListener;
	private VideosUpdateListener videosUpdateListener;
	private SparseBooleanArray viewedVideosMap;
	private long playButtonClickTime;
	private int currentPlayingId;
	private boolean need2update = true;
	private int previousCategoryId;

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

		viewedVideosMap = new SparseBooleanArray();
		videosUpdateListener = new VideosUpdateListener();
		saveVideosUpdateListener = new SaveVideosUpdateListener();
		videosAdapter = new VideosThumbCursorAdapter(this, null);
		videosAdapter.addViewedMap(viewedVideosMap);
		videosCursorUpdateListener = new VideosCursorUpdateListener();
		categoriesNames = new ArrayList<String>();
		categoriesIds = new ArrayList<Integer>();

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

		categorySpinner = (Spinner) view.findViewById(R.id.categoriesSpinner);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(videosAdapter);
		listView.setOnItemClickListener(this);

		getActivityFace().showActionMenu(R.id.menu_search, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onStart() {
		super.onStart();

		// get viewed marks
		Cursor cursor = DBDataManager.getVideoViewedCursor(getActivity(), getUsername());
		if (cursor != null) {
			do {
				int videoId = DBDataManager.getInt(cursor, DBConstants.V_ID);
				boolean isViewed = DBDataManager.getInt(cursor, DBConstants.V_VIDEO_VIEWED) > 0;
				viewedVideosMap.put(videoId, isViewed);
			} while (cursor.moveToNext());
			cursor.close();
		}


		boolean loaded = categoriesNames.size() != 0 || fillCategories();

		if (loaded) {
			// get passed argument
			String selectedCategory = getArguments().getString(SECTION_NAME);

			int sectionId;
			for (sectionId = 0; sectionId < categoriesNames.size(); sectionId++) {
				String category = categoriesNames.get(sectionId);
				if (category.equals(selectedCategory)) {
					categoriesIds.get(sectionId);
					break;
				}
			}

			categorySpinner.setAdapter(new DarkSpinnerAdapter(getActivity(), categoriesNames));
			categorySpinner.setOnItemSelectedListener(this);
			categorySpinner.setSelection(sectionId);  // TODO remember last selection.
		}
	}

	private boolean fillCategories() {
		Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.VIDEO_CATEGORIES], null, null, null, null);

		if (!cursor.moveToFirst()) {
			showToast("Categories are not loaded");
			return false;
		}

		do {
			categoriesNames.add(DBDataManager.getString(cursor, DBConstants.V_NAME));
			categoriesIds.add(Integer.valueOf(DBDataManager.getString(cursor, DBConstants.V_CATEGORY_ID)));
		} while(cursor.moveToNext());

		return true;
	}

	private void loadFromDb() {
//		String category = (String) categorySpinner.getSelectedItem();

		new LoadDataFromDbTask(videosCursorUpdateListener,
				DbHelper.getVideosByCategoryParams(previousCategoryId),
				getContentResolver()).executeTask();
	}

	private class VideosCursorUpdateListener extends ChessUpdateListener<Cursor> {

		@Override
		public void showProgress(boolean show) {
			showLoadingView(show);
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			videosAdapter.changeCursor(returnedObj);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.error);
			}
			showEmptyView(true);
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);
		int id = view.getId();
		if (id == R.id.titleTxt || id == R.id.authorTxt || id == R.id.dateTxt){
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			Cursor cursor = (Cursor) listView.getItemAtPosition(position);
			getActivityFace().openFragment(VideoDetailsFragment.createInstance(DBDataManager.getId(cursor)));
		} else if (id == R.id.thumbnailImg || id == R.id.playBtn){
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			Cursor cursor = (Cursor) listView.getItemAtPosition(position);

			currentPlayingId = DBDataManager.getInt(cursor, DBConstants.V_ID);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse(DBDataManager.getString(cursor, DBConstants.V_URL)), "video/*");
			startActivityForResult(Intent.createChooser(intent, getString(R.string.select_player)), WATCH_VIDEO_REQUEST);

			// start record time to watch
			playButtonClickTime = System.currentTimeMillis();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == WATCH_VIDEO_REQUEST) {

			verifyAndSaveViewedState();
		}
	}

	private void verifyAndSaveViewedState() {
		long resumeFromVideoTime = System.currentTimeMillis();

		if (resumeFromVideoTime - playButtonClickTime > VideosFragment.WATCHED_TIME) {
			VideoViewedItem item = new VideoViewedItem(currentPlayingId, getUsername(), true);
			DBDataManager.updateVideoViewedState(getContentResolver(), item);

			// update current list
			viewedVideosMap.put(currentPlayingId, true);
			videosAdapter.addViewedMap(viewedVideosMap);
			videosAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(VideosFragment.CLICK_TIME, playButtonClickTime);
		outState.putInt(VideosFragment.CURRENT_PLAYING_ID, currentPlayingId);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		getActivityFace().openFragment(VideoDetailsFragment.createInstance(DBDataManager.getId(cursor)));
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Integer categoryId = categoriesIds.get(position);

		if (need2update || categoryId != previousCategoryId) {
			previousCategoryId = categoryId;
			need2update = true;

			// check if we have saved videos more than 2(from previous page)
			Cursor cursor = DBDataManager.executeQuery(getContentResolver(),
					DbHelper.getVideosByCategoryParams(previousCategoryId));

			if (cursor != null && cursor.getCount() > VideosFragment.ITEMS_PER_CATEGORY) {
				cursor.moveToFirst();
				videosAdapter.changeCursor(cursor);
				videosAdapter.notifyDataSetChanged();
				showEmptyView(false);
			} else {
				// TODO adjust endless adapter here
				// Loading full video list from category here!

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.CMD_VIDEOS);
				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
				loadItem.addRequestParams(RestHelper.P_CATEGORY_ID, categoryId);
				loadItem.addRequestParams(RestHelper.P_LIMIT, RestHelper.DEFAULT_ITEMS_PER_PAGE);

				new RequestJsonTask<VideoItem>(videosUpdateListener).executeTask(loadItem);
			}
		} else {
			loadFromDb();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	private class VideosUpdateListener extends ChessLoadUpdateListener<VideoItem> {
		private VideosUpdateListener() {
			super(VideoItem.class);
		}

		@Override
		public void updateData(VideoItem returnedObj) {
			new SaveVideosListTask(saveVideosUpdateListener, returnedObj.getData(), getContentResolver()).executeTask();
		}
	}

	private class SaveVideosUpdateListener extends ChessUpdateListener<VideoItem.Data> {

		@Override
		public void showProgress(boolean show) {
			showLoadingView(show);
		}

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

	private void showLoadingView(boolean show) {
		if (show) {
			emptyView.setVisibility(View.GONE);
			if (videosAdapter.getCount() == 0) {
				listView.setVisibility(View.GONE);

			}
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

}
