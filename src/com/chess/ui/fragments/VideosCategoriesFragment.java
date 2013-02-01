package com.chess.ui.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;

import com.chess.backend.interfaces.ActionBarUpdateListener;

import com.chess.backend.statics.StaticData;

import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.ui.adapters.ChessDarkSpinnerAdapter;
import com.chess.ui.adapters.NewVideosThumbCursorAdapter;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 27.01.13
 * Time: 19:12
 */
public class VideosCategoriesFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

	public static final String SECTION_NAME = "section_name";

	//	private NewVideosAdapter videosAdapter;
	private NewVideosThumbCursorAdapter videosAdapter;

	private Spinner categorySpinner;
	private View loadingView;
	private TextView emptyView;
	private ListView listView;
	private EditText searchEdt;
	private Spinner sortSpinner;
	private boolean searchVisible;
	private boolean need2Update = true;
	private VideosCursorUpdateListener videosCursorUpdateListener;

	public static BasePopupsFragment newInstance(String sectionName) {
		VideosCategoriesFragment frag = new VideosCategoriesFragment();
		Bundle bundle = new Bundle();
		bundle.putString(SECTION_NAME, sectionName);
		frag.setArguments(bundle);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


//		videosAdapter = new NewVideosAdapter(getActivity(), new ArrayList<VideoItem.VideoDataItem>());
		videosAdapter = new NewVideosThumbCursorAdapter(getActivity(), null);
		videosCursorUpdateListener = new VideosCursorUpdateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_common_categories_frame, container, false); // TODO restore
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		searchEdt = (EditText) view.findViewById(R.id.searchEdt);

		view.findViewById(R.id.searchBtn).setOnClickListener(this);

		categorySpinner = (Spinner) view.findViewById(R.id.categoriesSpinner);
		String selectedCategory = getArguments().getString(SECTION_NAME);

		List<String> list = AppUtils.convertArrayToList(getResources().getStringArray(R.array.category));

		int sectionId;
		for (sectionId = 0; sectionId < list.size(); sectionId++) {
			String category = list.get(sectionId);
			if (category.equals(selectedCategory)) {
				break;
			}
		}

		categorySpinner.setAdapter(new ChessDarkSpinnerAdapter(getActivity(), list));
		categorySpinner.setOnItemSelectedListener(this);
		categorySpinner.setSelection(sectionId - 1);  // TODO remember last selection.

		sortSpinner = (Spinner) view.findViewById(R.id.sortSpinner);

		List<String> sortList = new ArrayList<String>();  // TODO set list of sort parameters
		sortList.add("Latest");
		sortList.add("Date");
		sortList.add("Author's Name");
		sortList.add("Author's Country");
		sortSpinner.setAdapter(new ChessDarkSpinnerAdapter(getActivity(), sortList));

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(videosAdapter);
		listView.setOnItemClickListener(this);

	}

	@Override
	public void onStart() {
		super.onStart();

		init();
//		if (need2Update) {
//
//			if (AppUtils.isNetworkAvailable(getActivity())) {
//				updateData();
//			} else {
//				emptyView.setText(R.string.no_network);
//				showEmptyView(true);
//			}
//
//			if (DBDataManager.haveSavedFriends(getActivity())) {
//				loadFromDb();
//			}
//		}

	}


	@Override
	public void onStop() {
		super.onStop();
		// TODO release resources
	}

	private void init() {

	}

//	private void updateData() {  // pass selected category
//		String category = (String) categorySpinner.getSelectedItem();
//		VideosItemUpdateListener videoUpdateListener = new VideosItemUpdateListener();
//
//		LoadItem loadItem = new LoadItem();
//
//		loadItem.setLoadPath(RestHelper.CMD_VIDEOS);
//		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, AppData.getUserToken(getContext()));
//		loadItem.addRequestParams(RestHelper.P_PAGE_SIZE, RestHelper.V_VIDEO_ITEM_ONE);
//		loadItem.addRequestParams(RestHelper.P_ITEMS_PER_PAGE, RestHelper.V_VIDEO_ITEM_ONE);
//		loadItem.addRequestParams(RestHelper.P_CATEGORY, category);
//		new RequestJsonTask<VideoItem>(videoUpdateListener).executeTask(loadItem);
//	}


	private void loadFromDb() {
		String category = (String) categorySpinner.getSelectedItem();

		new LoadDataFromDbTask(videosCursorUpdateListener,
				DbHelper.getVideosListCategoryParams(getContext(), category),
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

			videosAdapter.changeCursor(returnedObj);
//			listView.setAdapter(videosCursorAdapter);

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

	@Override
	public void onClick(View v) {
		super.onClick(v);

		if (v.getId() == R.id.searchBtn) {
			searchVisible = !searchVisible;

			showSearch(searchVisible);
		}
	}

	private void showSearch(boolean show) {
		if (show) {
			categorySpinner.setVisibility(View.GONE);
			sortSpinner.setVisibility(View.GONE);

			searchEdt.setVisibility(View.VISIBLE);

		} else {
			categorySpinner.setVisibility(View.VISIBLE);
			sortSpinner.setVisibility(View.VISIBLE);

			searchEdt.setVisibility(View.GONE);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		getActivityFace().openFragment(new VideosDetailsFragment());
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//		updateData();
		loadFromDb();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}


//	private class VideosItemUpdateListener extends ActionBarUpdateListener<VideoItem> {
//
//		public VideosItemUpdateListener() {
//			super(getInstance(), VideoItem.class);
//		}
//
//		@Override
//		public void showProgress(boolean show) {
//			super.showProgress(show);
//			showLoadingView(show);
//		}
//
//		@Override
//		public void updateData(VideoItem returnedObj) {
//
//			videosAdapter.setItemsList(returnedObj.getData().getVideos());
//			videosAdapter.notifyDataSetInvalidated();
//		}
//
//		@Override
//		public void errorHandle(Integer resultCode) {
//			super.errorHandle(resultCode);
//			if (resultCode == StaticData.EMPTY_DATA) {
//				emptyView.setText(R.string.no_games);
//			} else if (resultCode == StaticData.UNKNOWN_ERROR) {
//				emptyView.setText(R.string.no_network);
//			}
//			showEmptyView(true);
//		}
//	}

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
