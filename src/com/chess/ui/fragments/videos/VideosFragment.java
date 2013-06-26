package com.chess.ui.fragments.videos;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.CommonFeedCategoryItem;
import com.chess.backend.entity.new_api.VideoItem;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveVideoCategoriesTask;
import com.chess.db.tasks.SaveVideosListTask;
import com.chess.ui.adapters.NewVideosSectionedCursorAdapter;
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
public class VideosFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener {

	public static final String GREY_COLOR_DIVIDER = "##";
	// 11/15/12 | 27 min
	private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yy");
	private static final int VIDEOS_PER_CATEGORY = 2;

	private ViewHolder holder;
	private ForegroundColorSpan foregroundSpan;

	private ListView listView;
	private View loadingView;
	private TextView emptyView;

	private NewVideosSectionedCursorAdapter videosCursorAdapter;

	private VideosItemUpdateListener videosItemUpdateListener;
	private VideosItemUpdateListener randomItemUpdateListener;
	private SaveVideosUpdateListener saveVideosUpdateListener;
	private VideosCursorUpdateListener videosCursorUpdateListener;

	private VideoCategoriesUpdateListener videoCategoriesUpdateListener;
	private SaveVideoCategoriesUpdateListener saveVideoCategoriesUpdateListener;

	private boolean need2Update = true;
	private boolean headerDataLoaded;
	private long headerDataId;
	private VideoItem.Data headerData;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		videosCursorAdapter = new NewVideosSectionedCursorAdapter(getContext(), null, VIDEOS_PER_CATEGORY);

		int lightGrey = getResources().getColor(R.color.new_subtitle_light_grey);
		foregroundSpan = new ForegroundColorSpan(lightGrey);
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

		listView = (ListView) view.findViewById(R.id.listView);
		// add header
		View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_videos_thumb_list_item, null, false);
		headerView.setOnClickListener(this);
		listView.addHeaderView(headerView);
		listView.setAdapter(videosCursorAdapter);
		listView.setOnItemClickListener(this);

		// TODO create loading view for header
		holder = new ViewHolder();
		holder.titleTxt = (TextView) headerView.findViewById(R.id.titleTxt);
		holder.authorTxt = (TextView) headerView.findViewById(R.id.authorTxt);
		holder.dateTxt = (TextView) headerView.findViewById(R.id.dateTxt);

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_search, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onStart() {
		super.onStart();

		init();

		if (need2Update) {
			boolean haveSavedData = DBDataManager.haveSavedVideos(getActivity());

			if (AppUtils.isNetworkAvailable(getActivity())) {
				updateData();
				getCategories();
			} else if (!haveSavedData) {
				emptyView.setText(R.string.no_network);
				showEmptyView(true);
			}

			if (haveSavedData) {
				loadFromDb();
			}
		} else { // load data to listHeader view
			fillListViewHeaderData();
		}
	}

	@Override
	public void onStop() {
		super.onStop();

//		videosItemUpdateListener.releaseContext();   // TODO invent logic to release resources
//		videosItemUpdateListener = null;
	}

	private void init() {
		randomItemUpdateListener = new VideosItemUpdateListener(VideosItemUpdateListener.RANDOM);
		videosItemUpdateListener = new VideosItemUpdateListener(VideosItemUpdateListener.DATA_LIST);

		saveVideosUpdateListener = new SaveVideosUpdateListener();
		videosCursorUpdateListener = new VideosCursorUpdateListener();

		videoCategoriesUpdateListener = new VideoCategoriesUpdateListener();
		saveVideoCategoriesUpdateListener = new SaveVideoCategoriesUpdateListener();
	}

	private void updateData() {
		{// request random data for the header
			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.CMD_VIDEOS);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getAppData().getUserToken());
			loadItem.addRequestParams(RestHelper.P_ITEMS_PER_PAGE, RestHelper.V_VIDEO_ITEM_ONE);

			new RequestJsonTask<VideoItem>(randomItemUpdateListener).executeTask(loadItem);
		}
		// get all video // TODO adjust to request only latest updates

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_VIDEOS);
		loadItem.addRequestParams(RestHelper.P_ITEMS_PER_PAGE, 8);
		loadItem.addRequestParams(RestHelper.P_ITEMS_PER_CATEGORY, VIDEOS_PER_CATEGORY);

		new RequestJsonTask<VideoItem>(videosItemUpdateListener).executeTask(loadItem);
	}


	private void getCategories() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_VIDEO_CATEGORIES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getAppData().getUserToken());

		new RequestJsonTask<CommonFeedCategoryItem>(videoCategoriesUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		boolean headerAdded = listView.getHeaderViewsCount() > 0;
		int offset = headerAdded ? -1 : 0;

		if (position == 0) { // if listView header
			// see onClick(View) handle
		} else if (videosCursorAdapter.isSectionHeader(position + offset)) {
			String sectionName = videosCursorAdapter.getSectionName(position + offset);

			getActivityFace().openFragment(VideoCategoriesFragment.createInstance(sectionName));
		} else {
			int internalPosition = videosCursorAdapter.getRelativePosition(position + offset);
			Cursor cursor = (Cursor) parent.getItemAtPosition(internalPosition + 1);
			getActivityFace().openFragment(VideoDetailsFragment.createInstance(DBDataManager.getId(cursor)));
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v.getId() == R.id.videoThumbItemView) {
			if (headerDataLoaded) {
				getActivityFace().openFragment(VideoDetailsFragment.createInstance(headerDataId));
			}
		}
	}

	private class VideoCategoriesUpdateListener extends ChessUpdateListener<CommonFeedCategoryItem> {
		public VideoCategoriesUpdateListener() {
			super(CommonFeedCategoryItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(CommonFeedCategoryItem returnedObj) {
			super.updateData(returnedObj);

			List<CommonFeedCategoryItem.Data> dataList = returnedObj.getData();
			for (CommonFeedCategoryItem.Data category : dataList) {
				category.setName(category.getName().replace(StaticData.SYMBOL_AMP_CODE, StaticData.SYMBOL_AMP));
			}

			new SaveVideoCategoriesTask(saveVideoCategoriesUpdateListener, dataList, getContentResolver()).executeTask();
		}
	}

	private class VideosItemUpdateListener extends ChessUpdateListener<VideoItem> {

		private static final int RANDOM = 0;
		private static final int DATA_LIST = 1;
		private int listenerCode;

		public VideosItemUpdateListener(int listenerCode) {
			super(VideoItem.class);
			this.listenerCode = listenerCode;
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(VideoItem returnedObj) {
			switch (listenerCode) {
				case RANDOM:
					headerData = returnedObj.getData().get(0);

					fillListViewHeaderData();

					// save in Db to open in Details View
					ContentResolver contentResolver = getContentResolver();

					Uri uri = DBConstants.uriArray[DBConstants.VIDEOS];
					String[] arguments = new String[1];
					arguments[0] = String.valueOf(headerData.getName());
					Cursor cursor = contentResolver.query(uri, DBDataManager.PROJECTION_NAME,
							DBDataManager.SELECTION_NAME, arguments, null);

					ContentValues values = DBDataManager.putVideoItemToValues(headerData);

					if (cursor.moveToFirst()) {
						headerDataId = DBDataManager.getId(cursor);
						contentResolver.update(ContentUris.withAppendedId(uri, headerDataId), values, null, null);
					} else {
						Uri savedUri = contentResolver.insert(uri, values);
						headerDataId = Long.parseLong(savedUri.getPathSegments().get(1));
					}

					headerDataLoaded = true;

					break;
				case DATA_LIST:
					new SaveVideosListTask(saveVideosUpdateListener, returnedObj.getData(), getContentResolver()).executeTask();
					break;
			}
		}
	}

	private void fillListViewHeaderData() {
		if (!headerDataLoaded) {
			return;
		}

		String firstName = headerData.getFirstName();
		String chessTitle = headerData.getChessTitle();
		String lastName = headerData.getLastName();
		CharSequence authorStr = GREY_COLOR_DIVIDER + chessTitle + GREY_COLOR_DIVIDER + StaticData.SYMBOL_SPACE
				+ firstName + StaticData.SYMBOL_SPACE + lastName;
		authorStr = AppUtils.setSpanBetweenTokens(authorStr, GREY_COLOR_DIVIDER, foregroundSpan);
		holder.authorTxt.setText(authorStr);

		holder.titleTxt.setText(headerData.getName());
		holder.dateTxt.setText(dateFormatter.format(new Date(headerData.getCreateDate())));
	}

	protected class ViewHolder {
		public TextView titleTxt;
		public TextView authorTxt;
		public TextView dateTxt;
	}

	private class SaveVideosUpdateListener extends ChessUpdateListener<VideoItem.Data> {
		public SaveVideosUpdateListener() {
			super();
		}

		@Override
		public void updateData(VideoItem.Data returnedObj) {
			super.updateData(returnedObj);

			loadFromDb();
		}
	}

	private class SaveVideoCategoriesUpdateListener extends ChessUpdateListener<CommonFeedCategoryItem.Data> {
	}

	private void loadFromDb() {
		new LoadDataFromDbTask(videosCursorUpdateListener, DbHelper.getVideosListParams(),
				getContentResolver()).executeTask();
	}

	private class VideosCursorUpdateListener extends ChessUpdateListener<Cursor> {

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			videosCursorAdapter.changeCursor(returnedObj);
			listView.setAdapter(videosCursorAdapter);

			need2Update = false;
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.EMPTY_DATA) {
				emptyView.setText(R.string.no_games);
			} else if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.no_network);
			}
			showEmptyView(true);
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
			if (videosCursorAdapter.getCount() == 0) {
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
