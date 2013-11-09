package com.chess.ui.fragments.lessons;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.LessonSingleItem;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.db.tasks.SaveLessonsListTask;
import com.chess.statics.StaticData;
import com.chess.ui.adapters.DarkSpinnerAdapter;
import com.chess.ui.adapters.LessonsCursorAdapter;
import com.chess.ui.adapters.LessonsPaginationAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;
import com.chess.utilities.AppUtils;

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

	private LessonsCursorAdapter lessonsAdapter;
	protected Spinner categorySpinner;
	protected View loadingView;
	protected TextView emptyView;
	private ListView listView;
	private LessonsCursorUpdateListener lessonsCursorUpdateListener;
	private List<String> categoriesNames;
	private List<Integer> categoriesIds;
	private SaveLessonsUpdateListener saveLessonsUpdateListener;
	private LessonsUpdateListener lessonsUpdateListener;

	private int previousCategoryId;
	protected String sectionName;
	protected LessonsPaginationAdapter paginationAdapter;
	private Integer selectedCategoryId;

	public LessonsCategoriesFragment() {}

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

		setAdapter(new LessonsCursorAdapter(getActivity(), null));
		paginationAdapter = new LessonsPaginationAdapter(getActivity(), getAdapter(), new LessonsUpdateListener(), null);

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

		widgetsInit(view);

		getActivityFace().showActionMenu(R.id.menu_search_btn, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	protected void widgetsInit(View view) {
		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		categorySpinner = (Spinner) view.findViewById(R.id.categoriesSpinner);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(paginationAdapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		boolean loaded = categoriesNames.size() != 0 || fillCategories();

		if (loaded) {
			// get passed argument
			String selectedCategory = sectionName;

			int position;

			for (position = 0; position < categoriesNames.size(); position++) {
				String category = categoriesNames.get(position);
				if (category.equals(selectedCategory)) {
					selectedCategoryId = categoriesIds.get(position);
					break;
				}
			}

			categorySpinner.setAdapter(new DarkSpinnerAdapter(getActivity(), categoriesNames));
			categorySpinner.setOnItemSelectedListener(this);
			categorySpinner.setSelection(position);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putString(SECTION_NAME, sectionName);
	}

	private boolean fillCategories() {
		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getLessonsLibraryCategories());

		if (cursor != null && cursor.moveToFirst()){
			do {
				categoriesNames.add(DbDataManager.getString(cursor, DbScheme.V_NAME));
				categoriesIds.add(Integer.valueOf(DbDataManager.getString(cursor, DbScheme.V_CATEGORY_ID)));
			} while(cursor.moveToNext());
			cursor.close();
			return true;
		} else {
			return false;
		}
	}

	private void loadFromDb() {
		new LoadDataFromDbTask(lessonsCursorUpdateListener,
				DbHelper.getLessonsByCategory(previousCategoryId, getUsername()),
				getContentResolver()).executeTask();
	}

	protected void setAdapter(LessonsCursorAdapter adapter) {
		this.lessonsAdapter = adapter;
	}

	protected LessonsCursorAdapter getAdapter() {
		return lessonsAdapter;
	}

	private class LessonsCursorUpdateListener extends ChessUpdateListener<Cursor> {

		@Override
		public void showProgress(boolean show) {
			showLoadingView(show);
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			getAdapter().changeCursor(returnedObj);
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_search_btn:
				getActivityFace().openFragment(new LessonsSearchFragment());
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		long lessonId = DbDataManager.getLong(cursor, DbScheme.V_ID);

		if (!isTablet) {
			getActivityFace().openFragment(GameLessonFragment.createInstance((int) lessonId, 0));
		} else {
			getActivityFace().openFragment(GameLessonsFragmentTablet.createInstance((int) lessonId, 0)); // we don't know courseId here
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		selectedCategoryId = categoriesIds.get(position);

		updateByCategory();
	}

	protected void updateByCategory() {
		if (need2update || selectedCategoryId != previousCategoryId) {
			previousCategoryId = selectedCategoryId;
			need2update = true;

			// clear current list
			getAdapter().changeCursor(null);

			if (AppUtils.isNetworkAvailable(getActivity())) {
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.getInstance().CMD_LESSONS);
				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
				loadItem.addRequestParams(RestHelper.P_CATEGORY_ID, selectedCategoryId);
				loadItem.addRequestParams(RestHelper.P_ITEMS_PER_PAGE, RestHelper.DEFAULT_ITEMS_PER_PAGE);

				paginationAdapter.updateLoadItem(loadItem);
			} else {
				loadFromDb();
			}
		} else {
			paginationAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

	private class LessonsUpdateListener extends ChessUpdateListener<LessonSingleItem> {

		@Override
		public void updateListData(List<LessonSingleItem> returnedObj) {
			for (LessonSingleItem lessonSingleItem : returnedObj) {
				lessonSingleItem.setUser(getUsername());
				lessonSingleItem.setCategoryId(selectedCategoryId);
				lessonSingleItem.setCourseId(0);
				lessonSingleItem.setStarted(false);
			}
			new SaveLessonsListTask(saveLessonsUpdateListener, returnedObj, getContentResolver()).executeTask();
		}
	}

	private class SaveLessonsUpdateListener extends ChessUpdateListener<LessonSingleItem> {

		@Override
		public void showProgress(boolean show) {
			showLoadingView(show);
		}

		@Override
		public void updateData(LessonSingleItem returnedObj) {
			super.updateData(returnedObj);

			need2update = false;

			loadFromDb();
		}
	}

	protected void showEmptyView(boolean show) {
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

	protected void showLoadingView(boolean show) {
		if (show) {
			emptyView.setVisibility(View.GONE);
			if (getAdapter().getCount() == 0) {
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
