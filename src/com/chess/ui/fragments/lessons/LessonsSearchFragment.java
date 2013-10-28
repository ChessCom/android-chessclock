package com.chess.ui.fragments.lessons;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.Spinner;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.CommonFeedCategoryItem;
import com.chess.backend.entity.api.LessonSingleItem;
import com.chess.backend.entity.api.LessonsItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.QueryParams;
import com.chess.db.tasks.SaveLessonsListTask;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.LessonsItemAdapter;
import com.chess.ui.adapters.StringSpinnerAdapter;
import com.chess.ui.fragments.BaseSearchFragment;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.AppUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.08.13
 * Time: 14:50
 */
public class LessonsSearchFragment extends BaseSearchFragment implements AdapterView.OnItemClickListener {

	private Spinner difficultySpinner;
	private LessonItemUpdateListener lessonItemUpdateListener;
	private LessonsItemAdapter lessonsItemsAdapter;
	private String lastDifficulty;
	private StringSpinnerAdapter difficultySpinnerAdapter;
	private SaveLessonsUpdateListener saveLessonsUpdateListener;
	private int categoryId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		lessonItemUpdateListener = new LessonItemUpdateListener();
		lessonsItemsAdapter = new LessonsItemAdapter(getActivity(), null);
		saveLessonsUpdateListener = new SaveLessonsUpdateListener();

		String[] difficultyArray = getResources().getStringArray(R.array.lesson_difficulty);
		List<String> difficultyList = AppUtils.convertArrayToList(difficultyArray);
		difficultyList.add(0, allStr);
		difficultySpinnerAdapter = new StringSpinnerAdapter(getActivity(), difficultyList);

		lastDifficulty = Symbol.EMPTY;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_lessons_search_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.lessons);

		difficultySpinner = (Spinner) view.findViewById(R.id.difficultySpinner);
		difficultySpinner.setAdapter(difficultySpinnerAdapter);
	}

	@Override
	protected ListAdapter getAdapter() {
		return lessonsItemsAdapter;
	}

	@Override
	protected QueryParams getQueryParams() {
		return DbHelper.getLessonsLibraryCategories();
	}

	@Override
	protected void getCategories() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_LESSONS_CATEGORIES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<CommonFeedCategoryItem>(new LessonsCategoriesUpdateListener()).executeTask(loadItem);
	}

	private class LessonsCategoriesUpdateListener extends CommonLogicFragment.ChessUpdateListener<CommonFeedCategoryItem> {
		public LessonsCategoriesUpdateListener() {
			super(CommonFeedCategoryItem.class);
		}

		@Override
		public void updateData(CommonFeedCategoryItem returnedObj) {
			super.updateData(returnedObj);

			int i = 0;
			for (CommonFeedCategoryItem.Data currentItem : returnedObj.getData()) {
				currentItem.setDisplay_order(i++);
				DbDataManager.saveLessonCategoryToDb(getContentResolver(), currentItem);
			}
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.LESSONS_CATEGORIES));
			if (cursor != null && cursor.moveToFirst()) {
				fillCategoriesList(cursor);
			}
		}
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.searchBtn) {
			String keyword = getTextFromField(keywordsEdt);
			String category = (String) categorySpinner.getSelectedItem();
			String difficulty = (String) difficultySpinner.getSelectedItem();

			// Check if search query has changed, to reduce load
			if (lastKeyword.equals(keyword) && lastCategory.equals(category)
					&& lastDifficulty.equals(difficulty) && resultsFound) {
				showSearchResults();
				return;
			}

			categoryId = -1;
			for (int i = 0; i < categoriesArray.size(); i++) {
				String categoryByIndex = categoriesArray.valueAt(i);
				if (categoryByIndex.equals(category)) {
					categoryId = categoriesArray.keyAt(i);
				}
			}

			lastKeyword = keyword;
			lastCategory = category;
			lastDifficulty = difficulty;

			resultsFound = false;

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_LESSONS);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
			loadItem.addRequestParams(RestHelper.P_KEYWORD, keyword);
			if (!category.equals(allStr)) {
				loadItem.addRequestParams(RestHelper.P_CATEGORY_ID, categoryId);
			}
			if (!difficulty.equals(allStr)) {
				loadItem.addRequestParams(RestHelper.P_DIFFICULTY, difficulty);
			}

			new RequestJsonTask<LessonsItem>(lessonItemUpdateListener).executeTask(loadItem);
		} else {
			super.onClick(view);
		}
	}

	@Override
	protected void startSearch(String keyword, int categoryId) {
	} // not used here

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		LessonSingleItem lessonItem = (LessonSingleItem) parent.getItemAtPosition(position);
		long lessonId = lessonItem.getId();
		getActivityFace().openFragment(GameLessonFragment.createInstance((int) lessonId, 0)); // we don't know courseId here
	}

	private class LessonItemUpdateListener extends ChessLoadUpdateListener<LessonsItem> {

		private LessonItemUpdateListener() {
			super(LessonsItem.class);
		}

		@Override
		public void updateData(LessonsItem returnedObj) {
			super.updateData(returnedObj);

			if (returnedObj.getData().getLessons().size() == 0) {
				showSinglePopupDialog(R.string.no_results_found);
				return;
			}

			List<LessonSingleItem> lessons = returnedObj.getData().getLessons();
			lessonsItemsAdapter.setItemsList(lessons);

			// save lessons for future offline reference
			for (LessonSingleItem lessonSingleItem : lessons) {
				lessonSingleItem.setUser(getUsername());
				lessonSingleItem.setCategoryId(categoryId);
				lessonSingleItem.setCourseId(0);
				lessonSingleItem.setStarted(false);
			}
			new SaveLessonsListTask(saveLessonsUpdateListener, lessons, getContentResolver()).executeTask();

			need2update = false;
			resultsFound = true;

			showSearchResults();
		}
	}

	private class SaveLessonsUpdateListener extends ChessUpdateListener<LessonSingleItem> {

		@Override
		public void showProgress(boolean show) {
		}

		@Override
		public void updateData(LessonSingleItem returnedObj) {
			super.updateData(returnedObj);

		}
	}
}
