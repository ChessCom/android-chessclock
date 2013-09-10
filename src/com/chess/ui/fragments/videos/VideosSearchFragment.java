package com.chess.ui.fragments.videos;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import com.chess.MultiDirectionSlidingDrawer;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.CommonFeedCategoryItem;
import com.chess.backend.entity.api.CommonViewedItem;
import com.chess.backend.entity.api.VideoItem;
import com.chess.backend.statics.Symbol;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.SaveVideoCategoriesTask;
import com.chess.ui.adapters.StringSpinnerAdapter;
import com.chess.ui.adapters.VideosItemAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.09.13
 * Time: 17:47
 */
public class VideosSearchFragment extends CommonLogicFragment implements MultiDirectionSlidingDrawer.OnDrawerOpenListener, MultiDirectionSlidingDrawer.OnDrawerCloseListener, AdapterView.OnItemClickListener, ItemClickListenerFace {

	private static final int FADE_ANIM_DURATION = 300;
	private static final int WATCH_VIDEO_REQUEST = 9806;

	private EditText keywordsEdt;
	private Spinner categorySpinner;
	private String allStr;
	private VideoItemUpdateListener videoItemUpdateListener;
	private VideosItemAdapter videosAdapter;
	private MultiDirectionSlidingDrawer slidingDrawer;
	private ObjectAnimator fadeDrawerAnimator;
	private ObjectAnimator fadeSearchAnimator;
	private String lastKeyword;
	private String lastCategory;
	private SaveVideoCategoriesUpdateListener saveVideoCategoriesUpdateListener;
	private VideoCategoriesUpdateListener videoCategoriesUpdateListener;
	private SparseBooleanArray viewedVideosMap;
	private ListView listView;
	private long playButtonClickTime;
	private long currentPlayingId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		videoItemUpdateListener = new VideoItemUpdateListener();
		videoCategoriesUpdateListener = new VideoCategoriesUpdateListener();
		viewedVideosMap = new SparseBooleanArray();

		videosAdapter = new VideosItemAdapter(this, null);

		saveVideoCategoriesUpdateListener = new SaveVideoCategoriesUpdateListener();

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

		// restore state
		if (savedInstanceState != null) {
			playButtonClickTime = savedInstanceState.getLong(VideosFragment.CLICK_TIME);
			currentPlayingId = savedInstanceState.getLong(VideosFragment.CURRENT_PLAYING_ID);

			verifyAndSaveViewedState();
		}

		lastKeyword = Symbol.EMPTY;
		lastCategory = Symbol.EMPTY;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_base_search_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.videos);

		keywordsEdt = (EditText) view.findViewById(R.id.keywordsEdt);
		allStr = getString(R.string.all);

		categorySpinner = (Spinner) view.findViewById(R.id.categorySpinner);

		view.findViewById(R.id.searchBtn).setOnClickListener(this);

		slidingDrawer = (MultiDirectionSlidingDrawer) view.findViewById(R.id.slidingDrawer);
		slidingDrawer.setOnDrawerOpenListener(this);
		slidingDrawer.setOnDrawerCloseListener(this);
		fadeDrawerAnimator = ObjectAnimator.ofFloat(slidingDrawer, "alpha", 1, 0);
		fadeDrawerAnimator.setDuration(FADE_ANIM_DURATION);
		slidingDrawer.setVisibility(View.GONE);
		fadeDrawerAnimator.start();

		View searchFieldsView = view.findViewById(R.id.searchFieldsView);
		fadeSearchAnimator = ObjectAnimator.ofFloat(searchFieldsView, "alpha", 1, 0);
		fadeSearchAnimator.setDuration(FADE_ANIM_DURATION);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(videosAdapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		// get saved categories
		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.VIDEO_CATEGORIES));
		if (cursor != null && cursor.moveToFirst()) {
			fillCategoriesList(cursor);
		} else {
			getCategories();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(VideosFragment.CLICK_TIME, playButtonClickTime);
		outState.putLong(VideosFragment.CURRENT_PLAYING_ID, currentPlayingId);
	}

	private void fillCategoriesList(Cursor cursor) {
		List<String> categories = new ArrayList<String>();
		SparseArray<String> categoriesArray = new SparseArray<String>();
		categories.add(allStr);

		do {
			int id = DbDataManager.getInt(cursor, DbScheme.V_CATEGORY_ID);
			String name = DbDataManager.getString(cursor, DbScheme.V_NAME);
			categoriesArray.put(id, name);
			categories.add(name);
		} while (cursor.moveToNext());

		categorySpinner.setAdapter(new StringSpinnerAdapter(getActivity(), categories));
	}

	private void getCategories() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_VIDEO_CATEGORIES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<CommonFeedCategoryItem>(videoCategoriesUpdateListener).executeTask(loadItem);
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

		if (view.getId() == R.id.searchBtn) {
			String keyword = getTextFromField(keywordsEdt);
			String category = (String) categorySpinner.getSelectedItem();

			// Check if search query has changed, to reduce load
			if (lastKeyword.equals(keyword) && lastCategory.equals(category)) {
				showSearchResults();
				return;
			}

			lastKeyword = keyword;
			lastCategory = category;

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_VIDEOS);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
			loadItem.addRequestParams(RestHelper.P_KEYWORD, keyword);
			if (!category.equals(allStr)) {
				loadItem.addRequestParams(RestHelper.P_CATEGORY_CODE, category);
			}

			new RequestJsonTask<VideoItem>(videoItemUpdateListener).executeTask(loadItem);
		} else if (view.getId() == R.id.completedIconTxt) {
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			VideoItem.Data videoItem = (VideoItem.Data) listView.getItemAtPosition(position);

			currentPlayingId = (int) videoItem.getVideoId();
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse(videoItem.getUrl()), "video/*");
			startActivityForResult(Intent.createChooser(intent, getString(R.string.select_player)), WATCH_VIDEO_REQUEST);

			// start record time to watch
			playButtonClickTime = System.currentTimeMillis();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		VideoItem.Data videoItem = (VideoItem.Data)  parent.getItemAtPosition(position);
		long videoId = videoItem.getVideoId();
		getActivityFace().openFragment(VideoDetailsFragment.createInstance(videoId));
	}

	@Override
	public void onDrawerOpened() {

	}

	@Override
	public void onDrawerClosed() {
		slidingDrawer.setVisibility(View.GONE);
		fadeSearchAnimator.reverse();
		fadeDrawerAnimator.start();
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	private class VideoItemUpdateListener extends ChessLoadUpdateListener<VideoItem> {

		private VideoItemUpdateListener() {
			super(VideoItem.class);
		}

		@Override
		public void updateData(VideoItem returnedObj) {
			super.updateData(returnedObj);

			videosAdapter.setItemsList(returnedObj.getData());

			showSearchResults();
		}
	}

	private void showSearchResults() {
		slidingDrawer.setVisibility(View.VISIBLE);
		fadeDrawerAnimator.reverse();
		fadeDrawerAnimator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {
			}

			@Override
			public void onAnimationEnd(Animator animator) {
				if (!slidingDrawer.isOpened()) {
					slidingDrawer.animateOpen();
				}
			}

			@Override
			public void onAnimationCancel(Animator animator) {
			}

			@Override
			public void onAnimationRepeat(Animator animator) {
			}
		});
		fadeSearchAnimator.start();
	}

	private class VideoCategoriesUpdateListener extends ChessUpdateListener<CommonFeedCategoryItem> {
		public VideoCategoriesUpdateListener() {
			super(CommonFeedCategoryItem.class);
		}

//		@Override
//		public void showProgress(boolean show) {
//			showLoadingView(show);
//		}

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

	private class SaveVideoCategoriesUpdateListener extends ChessUpdateListener<CommonFeedCategoryItem.Data> {

		@Override
		public void updateData(CommonFeedCategoryItem.Data returnedObj) {
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.VIDEO_CATEGORIES));
			if (cursor != null && cursor.moveToFirst()) {
				fillCategoriesList(cursor);
			}
		}
	}
}
