package com.chess.ui.fragments.videos;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.ui.adapters.DarkSpinnerAdapter;
import com.chess.ui.adapters.NewVideosThumbCursorAdapter;
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

	private NewVideosThumbCursorAdapter videosAdapter;

	private Spinner categorySpinner;
	private View loadingView;
	private TextView emptyView;
	private ListView listView;
	private EditText searchEdt;
	private Spinner sortSpinner;
	private boolean searchVisible;
	private VideosCursorUpdateListener videosCursorUpdateListener;
//	private boolean categoriesLoaded;
	private List<String> sortOrders;
	private List<String> categoriesList;

	public static VideoCategoriesFragment newInstance(String sectionName) {
		VideoCategoriesFragment frag = new VideoCategoriesFragment();
		Bundle bundle = new Bundle();
		bundle.putString(SECTION_NAME, sectionName);
		frag.setArguments(bundle);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		videosAdapter = new NewVideosThumbCursorAdapter(this, null);
		videosCursorUpdateListener = new VideosCursorUpdateListener();
		categoriesList = new ArrayList<String>();
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

		searchEdt = (EditText) view.findViewById(R.id.searchEdt);

		view.findViewById(R.id.searchBtn).setOnClickListener(this);

		categorySpinner = (Spinner) view.findViewById(R.id.categoriesSpinner);

		sortSpinner = (Spinner) view.findViewById(R.id.sortSpinner);

/*
	+ V_NAME 					+ _TEXT_NOT_NULL + _COMMA
	+ V_SKILL_LEVEL 			+ _TEXT_NOT_NULL + _COMMA
	+ V_CREATE_DATE 	    	+ _LONG_NOT_NULL + _COMMA
	+ V_FIRST_NAME 	    		+ _TEXT_NOT_NULL + _COMMA
*/

		List<String> sortList = new ArrayList<String>();
		sortList.add(getString(R.string.title));
		sortList.add(getString(R.string.skill));
		sortList.add(getString(R.string.latest));
		sortList.add(getString(R.string.authors_name));
		sortSpinner.setAdapter(new DarkSpinnerAdapter(getActivity(), sortList));
		sortSpinner.setOnItemSelectedListener(this);

		sortOrders = new ArrayList<String>();
		sortOrders.add(DBConstants.V_NAME);
		sortOrders.add(DBConstants.V_SKILL_LEVEL);
		sortOrders.add(DBConstants.V_CREATE_DATE);
		sortOrders.add(DBConstants.V_FIRST_NAME);


		listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(videosAdapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onStart() {
		super.onStart();

		init();

		boolean loaded = categoriesList.size() != 0 || fillCategories();

		if (loaded) {
			// get passed argument
			String selectedCategory = getArguments().getString(SECTION_NAME);

			int sectionId;
			for (sectionId = 0; sectionId < categoriesList.size(); sectionId++) {
				String category = categoriesList.get(sectionId);
				if (category.equals(selectedCategory)) {
					break;
				}
			}

			categorySpinner.setAdapter(new DarkSpinnerAdapter(getActivity(), categoriesList));
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
			categoriesList.add(DBDataManager.getString(cursor, DBConstants.V_NAME));
		} while(cursor.moveToNext());

		return true;
	}


	@Override
	public void onStop() {
		super.onStop();
		// TODO release resources
	}

	private void init() {

	}

	private void loadFromDb() {
		String category = (String) categorySpinner.getSelectedItem();
		int sortPosition = sortSpinner.getSelectedItemPosition();

		String sortOrder = sortOrders.get(sortPosition);
		new LoadDataFromDbTask(videosCursorUpdateListener,
				DbHelper.getVideosListByCategoryParams(category, sortOrder),
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

			videosAdapter.changeCursor(returnedObj);
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
	public void onClick(View view) {
		super.onClick(view);
		int id = view.getId();
		if (id == R.id.searchBtn) {
			searchVisible = !searchVisible;

			showSearch(searchVisible);
		} else if (id == R.id.titleTxt || id == R.id.authorTxt || id == R.id.dateTxt){
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			Cursor cursor = (Cursor) listView.getItemAtPosition(position);
			getActivityFace().openFragment(VideoDetailsFragment.newInstance(DBDataManager.getId(cursor)));
		} else if (id == R.id.thumbnailImg || id == R.id.playBtn){
			Integer position = (Integer) view.getTag(R.id.list_item_id);
			Cursor cursor = (Cursor) listView.getItemAtPosition(position);

			Intent intent = new Intent(Intent.ACTION_VIEW);
//			intent.setDataAndType(Uri.parse("http://clips.vorwaerts-gmbh.de/VfE_html5.mp4"), "video/*"); // TODO restore
			intent.setDataAndType(Uri.parse(DBDataManager.getString(cursor, DBConstants.V_URL)), "video/*");
			startActivity(intent);
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
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		getActivityFace().openFragment(VideoDetailsFragment.newInstance(DBDataManager.getId(cursor)));
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		loadFromDb();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

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
