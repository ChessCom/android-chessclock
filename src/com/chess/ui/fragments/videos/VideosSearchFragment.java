package com.chess.ui.fragments.videos;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.CommonFeedCategoryItem;
import com.chess.backend.entity.api.CommonViewedItem;
import com.chess.backend.entity.api.VideoSingleItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.QueryParams;
import com.chess.statics.AppConstants;
import com.chess.statics.Symbol;
import com.chess.ui.activities.VideoActivity;
import com.chess.ui.adapters.VideosItemAdapter;
import com.chess.ui.adapters.VideosPaginationItemAdapter;
import com.chess.ui.fragments.BaseSearchFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.09.13
 * Time: 17:47
 */
public class VideosSearchFragment extends BaseSearchFragment implements ItemClickListenerFace {

	private VideosItemAdapter videosAdapter;
	private SparseBooleanArray viewedVideosMap;
	private long playButtonClickTime;
	private long currentPlayingId;
	private VideosPaginationItemAdapter paginationAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		viewedVideosMap = new SparseBooleanArray();

		videosAdapter = new VideosItemAdapter(this, null);
		paginationAdapter = new VideosPaginationItemAdapter(getActivity(), videosAdapter,
				new VideoItemUpdateListener(), null);

		// restore state
		if (savedInstanceState != null) {
			playButtonClickTime = savedInstanceState.getLong(VideosFragment.CLICK_TIME);
			currentPlayingId = savedInstanceState.getLong(VideosFragment.CURRENT_PLAYING_ID);

			verifyAndSaveViewedState();
		}
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.videos);

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

		videosAdapter.addViewedMap(viewedVideosMap);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(VideosFragment.CLICK_TIME, playButtonClickTime);
		outState.putLong(VideosFragment.CURRENT_PLAYING_ID, currentPlayingId);
	}

	@Override
	protected ListAdapter getAdapter() {
		return paginationAdapter;
	}

	@Override
	protected QueryParams getQueryParams() {
		return DbHelper.getAll(DbScheme.Tables.VIDEO_CATEGORIES);
	}

	@Override
	protected void getCategories() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_VIDEO_CATEGORIES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<CommonFeedCategoryItem>(new VideoCategoriesUpdateListener()).executeTask(loadItem);
	}

	private void verifyAndSaveViewedState() {
		long resumeFromVideoTime = System.currentTimeMillis();

		if (resumeFromVideoTime - playButtonClickTime > VideosFragment.WATCHED_TIME) {
			CommonViewedItem item = new CommonViewedItem(currentPlayingId, getUsername());
			DbDataManager.saveVideoViewedState(getContentResolver(), item);

			// update current list
			viewedVideosMap.put((int) currentPlayingId, true); // TODO test logic for long to int conversion
			videosAdapter.addViewedMap(viewedVideosMap);
			videosAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.completedIconTxt) {
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			VideoSingleItem.Data videoItem = (VideoSingleItem.Data) listView.getItemAtPosition(position);

			currentPlayingId = (int) videoItem.getVideoId();

			Intent intent = new Intent(getActivity(), VideoActivity.class);
			intent.putExtra(AppConstants.VIDEO_LINK, videoItem.getUrl());
			startActivity(intent);

			// start record time to watch
			playButtonClickTime = System.currentTimeMillis();
		}
	}

	@Override
	protected void startSearch(String keyword, int categoryId) {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_VIDEOS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
		loadItem.addRequestParams(RestHelper.P_KEYWORD, keyword);
		if (!lastCategory.equals(allStr)) {
			loadItem.addRequestParams(RestHelper.P_CATEGORY_ID, categoryId);
		}

		showSearchResults();
		paginationAdapter.updateLoadItem(loadItem);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		VideoSingleItem.Data videoItem = (VideoSingleItem.Data) parent.getItemAtPosition(position);
		long videoId = videoItem.getVideoId();
		getActivityFace().openFragment(VideoDetailsFragment.createInstance(videoId));
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	private class VideoItemUpdateListener extends ChessLoadUpdateListener<VideoSingleItem.Data> {

		private VideoItemUpdateListener() {
			super(VideoSingleItem.Data.class);
			useList = true;
		}

		@Override
		public void updateListData(List<VideoSingleItem.Data> itemsList) {
			super.updateListData(itemsList);

			if (itemsList.size() == 0) {
				showSinglePopupDialog(R.string.no_results);
				return;
			}

			for (VideoSingleItem.Data data : itemsList) {
				DbDataManager.saveVideoItem(getContentResolver(), data);
			}

			paginationAdapter.notifyDataSetChanged();
			videosAdapter.setItemsList(itemsList);
			paginationAdapter.notifyDataSetChanged();
			need2update = false;
			resultsFound = true;

			showSearchResults();
		}
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
				DbDataManager.saveVideoCategory(getContentResolver(), category);
			}

			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.VIDEO_CATEGORIES));
			if (cursor != null && cursor.moveToFirst()) {
				fillCategoriesList(cursor);
			}
		}
	}
}
