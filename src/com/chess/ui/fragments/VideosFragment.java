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
import com.chess.backend.entity.new_api.VideoItem;
import com.chess.backend.interfaces.ActionBarUpdateListener;
import com.chess.backend.statics.AppData;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveVideosListTask;
import com.chess.ui.adapters.CustomSectionedAdapter;
import com.chess.ui.adapters.NewVideosAdapter;
import com.chess.ui.adapters.NewVideosCursorAdapter;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

	private VideosItemUpdateListener randomVideoUpdateListener;

	private String[] categories;
	private CustomSectionedAdapter sectionedAdapter;
	private NewVideosAdapter amazingGamesAdapter;
	private NewVideosAdapter endGamesGamesAdapter;
	private NewVideosAdapter openingsGamesAdapter;
	private NewVideosAdapter rulesBasicGamesAdapter;
	private NewVideosAdapter strategyGamesAdapter;
	private NewVideosAdapter tacticsGamesAdapter;

	private ViewHolder holder;
	private ForegroundColorSpan foregroundSpan;

	private SaveVideosListUpdateListener saveVideosListUpdateListener;
	private ListView listView;
	private View loadingView;
	private TextView emptyView;
	private VideosCursorUpdateListener videosCursorUpdateListener;
	private NewVideosCursorAdapter videosCursorAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		categories = getResources().getStringArray(R.array.category);

		saveVideosListUpdateListener = new SaveVideosListUpdateListener();
		videosCursorUpdateListener = new VideosCursorUpdateListener();

		videosCursorAdapter = new NewVideosCursorAdapter(getContext(), null);

		amazingGamesAdapter = new NewVideosAdapter(getActivity(), new ArrayList<VideoItem.VideoDataItem>());
		endGamesGamesAdapter = new NewVideosAdapter(getActivity(), new ArrayList<VideoItem.VideoDataItem>());
		openingsGamesAdapter = new NewVideosAdapter(getActivity(), new ArrayList<VideoItem.VideoDataItem>());
		rulesBasicGamesAdapter = new NewVideosAdapter(getActivity(), new ArrayList<VideoItem.VideoDataItem>());
		strategyGamesAdapter = new NewVideosAdapter(getActivity(), new ArrayList<VideoItem.VideoDataItem>());
		tacticsGamesAdapter = new NewVideosAdapter(getActivity(), new ArrayList<VideoItem.VideoDataItem>());

		sectionedAdapter = new CustomSectionedAdapter(this, R.layout.new_arrow_section_header);

		sectionedAdapter.addSection(categories[0], amazingGamesAdapter);
		sectionedAdapter.addSection(categories[1], endGamesGamesAdapter);
		sectionedAdapter.addSection(categories[2], openingsGamesAdapter);
		sectionedAdapter.addSection(categories[3], rulesBasicGamesAdapter);
		sectionedAdapter.addSection(categories[4], strategyGamesAdapter);
		sectionedAdapter.addSection(categories[5], tacticsGamesAdapter);

		int lightGrey = getResources().getColor(R.color.new_subtitle_light_grey);
		foregroundSpan = new ForegroundColorSpan(lightGrey);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_videos_frame, container, false); // TODO restore
//		return inflater.inflate(R.layout.new_common_test, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);


		listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(videosCursorAdapter);
//		listView.setAdapter(sectionedAdapter);
		listView.setOnItemClickListener(this);

		holder = new ViewHolder();
		holder.titleTxt = (TextView) view.findViewById(R.id.titleTxt);
		holder.authorTxt = (TextView) view.findViewById(R.id.authorTxt);
		holder.dateTxt = (TextView) view.findViewById(R.id.dateTxt);
	}

	@Override
	public void onStart() {
		super.onStart();

		init();

		if (AppUtils.isNetworkAvailable(getActivity())) {
			updateData();
		} else {
			emptyView.setText(R.string.no_network);
			showEmptyView(true);
		}

		if (DBDataManager.haveSavedFriends(getActivity())) {
			loadFromDb();
		}
	}

	@Override
	public void onStop() {
		super.onStop();
		randomVideoUpdateListener.releaseContext();
		randomVideoUpdateListener = null;
	}

	private void init() {
		randomVideoUpdateListener = new VideosItemUpdateListener(VideosItemUpdateListener.RANDOM);
	}

	private void updateData() {
		// get random video

		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_VIDEOS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
		loadItem.addRequestParams(RestHelper.P_PAGE_SIZE, RestHelper.V_VIDEO_ITEM_ONE);
		loadItem.addRequestParams(RestHelper.P_ITEMS_PER_PAGE, RestHelper.V_VIDEO_ITEM_ONE);

		new RequestJsonTask<VideoItem>(randomVideoUpdateListener).executeTask(loadItem);

		// get 2 items from every category
		for (int i = 0; i < categories.length; i++) {
			makeNextCategoryRequest(i);
		}

	}

	private void makeNextCategoryRequest(int code){  // TODO optimize
		String category = categories[code];
		VideosItemUpdateListener videoUpdateListener = new VideosItemUpdateListener(code);

		LoadItem loadItem = new LoadItem();

		loadItem.setLoadPath(RestHelper.CMD_VIDEOS);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
//		loadItem.addRequestParams(RestHelper.P_PAGE_SIZE, RestHelper.V_VIDEO_ITEM_ONE);
//		loadItem.addRequestParams(RestHelper.P_ITEMS_PER_PAGE, RestHelper.V_VIDEO_ITEM_ONE);
//		loadItem.addRequestParams(RestHelper.P_CATEGORY, category);
		new RequestJsonTask<VideoItem>(videoUpdateListener).executeTask(loadItem);
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);

		getActivityFace().openFragment(VideosDetailsFragment.newInstance(DBDataManager.getId(cursor)));
//		getActivityFace().openFragment(new VideosCategoriesFragment());
	}

	private class VideosItemUpdateListener extends ActionBarUpdateListener<VideoItem> {

		final static int AMAZING_GAMES = 0;
		final static int END_GAMES = 1;
		final static int OPENINGS = 2;
		final static int RULES_BASICS = 3;
		final static int STRATEGY = 4;
		final static int TACTICS = 5;
		final static int RANDOM = 6;

		private int listenerCode;

		public VideosItemUpdateListener(int listenerCode) {
			super(getInstance(), VideoItem.class);
			this.listenerCode = listenerCode;
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}


		@Override
		public void updateData(VideoItem returnedObj) {

			switch (listenerCode){
				case RANDOM:



					break;
				case AMAZING_GAMES:

					amazingGamesAdapter.setItemsList(returnedObj.getData().getVideos());
					amazingGamesAdapter.notifyDataSetInvalidated();

					break;
				case END_GAMES:
					endGamesGamesAdapter.setItemsList(returnedObj.getData().getVideos());
					endGamesGamesAdapter.notifyDataSetInvalidated();

					new SaveVideosListTask(saveVideosListUpdateListener, returnedObj.getData().getVideos(), getContentResolver()).executeTask();
					break;
				case OPENINGS:
					openingsGamesAdapter.setItemsList(returnedObj.getData().getVideos());
					openingsGamesAdapter.notifyDataSetInvalidated();
					break;
				case RULES_BASICS:
					rulesBasicGamesAdapter.setItemsList(returnedObj.getData().getVideos());
					rulesBasicGamesAdapter.notifyDataSetInvalidated();
					break;
				case STRATEGY:
					strategyGamesAdapter.setItemsList(returnedObj.getData().getVideos());
					strategyGamesAdapter.notifyDataSetInvalidated();
					break;
				case TACTICS:
					tacticsGamesAdapter.setItemsList(returnedObj.getData().getVideos());
					tacticsGamesAdapter.notifyDataSetInvalidated();
					break;

			}

			// add data to sectioned adapter

//			recent.setVisibility(View.VISIBLE);
//			int cnt = Integer.parseInt(returnedObj.getData().getTotal_videos_count());
//			if (cnt > 0){
//				item = returnedObj.getData().getVideos().get(0); // new VideoItemOld(returnedObj.split(RestHelper.SYMBOL_ITEM_SPLIT)[2].split("<->"));
//				title.setText(item.getName());
//				desc.setText(item.getDescription());
//
//				playBtn.setEnabled(true);
//			}
		}
	}

	protected class ViewHolder {
		public TextView titleTxt;
		public TextView authorTxt;
		public TextView dateTxt;
	}

	private class SaveVideosListUpdateListener extends ActionBarUpdateListener<VideoItem.VideoDataItem> {
		public SaveVideosListUpdateListener() {
			super(getInstance());
		}

		@Override
		public void showProgress(boolean show) {
			super.showProgress(show);
			showLoadingView(show);
		}

		@Override
		public void updateData(VideoItem.VideoDataItem returnedObj) {
			if (getActivity() == null) {
				return;
			}

			loadFromDb();
		}
	}

	private void loadFromDb() {
		new LoadDataFromDbTask(videosCursorUpdateListener,
				DbHelper.getVideosListParams(getContext()),
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
		Log.d("TEST", "showEmptyView show = " + show);

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

}
