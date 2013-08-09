package com.chess.ui.fragments.lessons;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
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
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbConstants;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveVideosListTask;
import com.chess.ui.adapters.DarkSpinnerAdapter;
import com.chess.ui.adapters.LessonsCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
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

	private LessonsCursorAdapter categoriesAdapter;
	private Spinner categorySpinner;
	private View loadingView;
	private TextView emptyView;
	private ListView listView;
	private LessonsCursorUpdateListener lessonsCursorUpdateListener;
	private List<String> categoriesNames;
	private List<Integer> categoriesIds;
	private SaveLessonsUpdateListener saveLessonsUpdateListener;
	private LessonsUpdateListener lessonsUpdateListener;

	private boolean need2update = true;
	private int previousCategoryId;
	private String sectionName;

	public static LessonsCategoriesFragment createInstance(String sectionName) {
		LessonsCategoriesFragment fragment = new LessonsCategoriesFragment();
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

		categoriesAdapter = new LessonsCursorAdapter(getActivity(), null);
		lessonsUpdateListener = new LessonsUpdateListener();
		saveLessonsUpdateListener = new SaveLessonsUpdateListener();
		lessonsCursorUpdateListener = new LessonsCursorUpdateListener();
		categoriesNames = new ArrayList<String>();
		categoriesIds = new ArrayList<Integer>();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_common_categories_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.lessons);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		categorySpinner = (Spinner) view.findViewById(R.id.categoriesSpinner);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(categoriesAdapter);
		listView.setOnItemClickListener(this);

		getActivityFace().showActionMenu(R.id.menu_search, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onStart() {
		super.onStart();

		boolean loaded = categoriesNames.size() != 0 || fillCategories();

		if (loaded) {
			// get passed argument
			String selectedCategory = sectionName;

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

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(SECTION_NAME, sectionName);
	}

	private boolean fillCategories() {
		Cursor cursor = getContentResolver().query(DbConstants.uriArray[DbConstants.Tables.LESSONS_CATEGORIES.ordinal()], null, null, null, null);

		if (!cursor.moveToFirst()) {
			showToast("Categories are not loaded");
			return false;
		}

		do {
			categoriesNames.add(DbDataManager.getString(cursor, DbConstants.V_NAME));
			categoriesIds.add(Integer.valueOf(DbDataManager.getString(cursor, DbConstants.V_CATEGORY_ID)));
		} while(cursor.moveToNext());

		return true;
	}

	private void loadFromDb() {
//		String category = (String) categorySpinner.getSelectedItem();

		new LoadDataFromDbTask(lessonsCursorUpdateListener,
				DbHelper.getLessonsByCategory(previousCategoryId, getUsername()),
				getContentResolver()).executeTask();
	}

	private class LessonsCursorUpdateListener extends ChessUpdateListener<Cursor> {

		@Override
		public void showProgress(boolean show) {
			showLoadingView(show);
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			categoriesAdapter.changeCursor(returnedObj);
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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Integer categoryId = categoriesIds.get(position);

		if (need2update || categoryId != previousCategoryId) {
			previousCategoryId = categoryId;
			need2update = true;

			// check if we have saved videos more than 2(from previous page)
			Cursor cursor = DbDataManager.executeQuery(getContentResolver(),
					DbHelper.getLessonsByCategory(previousCategoryId, getUsername()));

			if (cursor != null && cursor.moveToFirst()) {
				categoriesAdapter.changeCursor(cursor);
				categoriesAdapter.notifyDataSetChanged();
				showEmptyView(false);
			} else {
				// TODO adjust endless adapter here
				// Loading full lessons list from category here!

				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.CMD_LESSONS);
				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
				loadItem.addRequestParams(RestHelper.P_CATEGORY_ID, categoryId);
				loadItem.addRequestParams(RestHelper.P_LIMIT, RestHelper.DEFAULT_ITEMS_PER_PAGE);

				new RequestJsonTask<VideoItem>(lessonsUpdateListener).executeTask(loadItem);
			}
		} else {
			loadFromDb();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	private class LessonsUpdateListener extends ChessLoadUpdateListener<VideoItem> {
		private LessonsUpdateListener() {
			super(VideoItem.class);
		}

		@Override
		public void updateData(VideoItem returnedObj) {
			new SaveVideosListTask(saveLessonsUpdateListener, returnedObj.getData(), getContentResolver()).executeTask();
		}
	}

	private class SaveLessonsUpdateListener extends ChessUpdateListener<VideoItem.Data> {

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
			if (categoriesAdapter.getCount() == 0) {
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
