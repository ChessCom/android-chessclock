package com.chess.ui.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.VideoCategoryItem;
import com.chess.backend.entity.new_api.VideoItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveVideoCategoriesTask;
import com.chess.db.tasks.SaveVideosListTask;
import com.chess.ui.adapters.NewVideosSectionedCursorAdapter;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

import java.text.SimpleDateFormat;
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


	private ViewHolder holder;
	private ForegroundColorSpan foregroundSpan;

	private ListView listView;
	private View loadingView;
	private TextView emptyView;

	private NewVideosSectionedCursorAdapter videosCursorAdapter;

	private VideosItemUpdateListener videosItemUpdateListener;
	private SaveVideosUpdateListener saveVideosUpdateListener;
	private VideosCursorUpdateListener videosCursorUpdateListener;

	private VideoCategoryUpdateListener categoryVideoUpdateListener;
	private SaveVideoCategoriesUpdateListener saveVideoCategoriesUpdateListener;

	private boolean need2Update = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		saveVideosUpdateListener = new SaveVideosUpdateListener();
		saveVideoCategoriesUpdateListener = new SaveVideoCategoriesUpdateListener();
		videosCursorUpdateListener = new VideosCursorUpdateListener();

		videosCursorAdapter = new NewVideosSectionedCursorAdapter(getContext(), null);

		int lightGrey = getResources().getColor(R.color.new_subtitle_light_grey);
		foregroundSpan = new ForegroundColorSpan(lightGrey);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_videos_frame, container, false); // TODO restore
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(videosCursorAdapter);
		listView.setOnItemClickListener(this);

		holder = new ViewHolder();
		holder.titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		holder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);
		holder.dateTxt = (TextView) view.findViewById(R.id.dateTxt);
	}

	@Override
	public void onStart() {
		super.onStart();

		if (need2Update) {
			boolean haveSavedData = DBDataManager.haveSavedVideos(getActivity());

			if (AppUtils.isNetworkAvailable(getActivity())) {
				updateData();
				getCategories();
			} else if(!haveSavedData){
				emptyView.setText(R.string.no_network);
				showEmptyView(true);
			}

			if (haveSavedData) {
				loadFromDb();
			}
		}
	}

	@Override
	public void onStop() {
		super.onStop();

//		videosItemUpdateListener.releaseContext();   // TODO invent logic to release resources
//		videosItemUpdateListener = null;
	}

	private void init() {
		videosItemUpdateListener = new VideosItemUpdateListener();
		categoryVideoUpdateListener = new VideoCategoryUpdateListener();
	}

	private void updateData() {
		// get all video // TODO adjust to request only latest updates

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_VIDEOS);

		new RequestJsonTask<VideoItem>(videosItemUpdateListener).executeTask(loadItem);
	}


	private void getCategories() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_VIDEO_CATEGORIES);

		new RequestJsonTask<VideoCategoryItem>(categoryVideoUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (videosCursorAdapter.isHeader(position)) {
			String sectionName = videosCursorAdapter.getSectionName(position);

			getActivityFace().openFragment(VideoCategoriesFragment.newInstance(sectionName));
		} else {
			position = videosCursorAdapter.getRelativePosition(position);
			Cursor cursor = (Cursor) parent.getItemAtPosition(position);
			getActivityFace().openFragment(VideoDetailsFragment.newInstance(DBDataManager.getId(cursor)));
		}
	}

	private class VideoCategoryUpdateListener extends ActionBarUpdateListener<VideoCategoryItem> {
		public VideoCategoryUpdateListener() {
			super(getInstance(), VideoCategoryItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(VideoCategoryItem returnedObj) {
			super.updateData(returnedObj);

			List<VideoCategoryItem.Data> dataList = returnedObj.getData();
			for (VideoCategoryItem.Data category : dataList) {
				category.setName(category.getName().replace(StaticData.SYMBOL_AMP_CODE, StaticData.SYMBOL_AMP));
			}

			new SaveVideoCategoriesTask(saveVideoCategoriesUpdateListener, dataList, getContentResolver()).executeTask();
		}
	}

	private class VideosItemUpdateListener extends ActionBarUpdateListener<VideoItem> {

		public VideosItemUpdateListener() {
			super(getInstance(), VideoItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(VideoItem returnedObj) {
			new SaveVideosListTask(saveVideosUpdateListener, returnedObj.getData().getVideos(), getContentResolver()).executeTask();
		}
	}

	protected class ViewHolder {
		public TextView titleTxt;
		public TextView authorTxt;
		public TextView dateTxt;
	}

	private class SaveVideosUpdateListener extends ActionBarUpdateListener<VideoItem.VideoDataItem> {
		public SaveVideosUpdateListener() {
			super(getInstance());
		}

		@Override
		public void updateData(VideoItem.VideoDataItem returnedObj) {
			if (getActivity() == null) {
				return;
			}

			loadFromDb();
		}

	}

	private class SaveVideoCategoriesUpdateListener extends ActionBarUpdateListener<VideoCategoryItem.Data> {
		public SaveVideoCategoriesUpdateListener() {
			super(getInstance());
		}
	}

	private void loadFromDb() {
		new LoadDataFromDbTask(videosCursorUpdateListener, DbHelper.getVideosListParams(),
				getContentResolver()).executeTask();
	}

	private class VideosCursorUpdateListener extends ActionBarUpdateListener<Cursor> {

		public VideosCursorUpdateListener() {
			super(getInstance());

		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(Cursor returnedObj) {
			if (getActivity() == null) {
				return;
			}

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
