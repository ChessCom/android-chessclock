package com.chess.ui.fragments.lessons;

import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.CommonFeedCategoryItem;
import com.chess.backend.entity.api.LessonCourseListItem;
import com.chess.backend.entity.api.LessonSingleItem;
import com.chess.backend.entity.api.LessonsRatingItem;
import com.chess.statics.Symbol;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.SaveLessonsCategoriesTask;
import com.chess.db.tasks.SaveLessonsCoursesListTask;
import com.chess.model.CurriculumLessonsItems;
import com.chess.ui.adapters.CommonCategoriesCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.upgrade.UpgradeFragment;
import com.chess.utilities.AppUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.07.13
 * Time: 14:59
 */
public class LessonsFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener,
		ExpandableListView.OnChildClickListener {

	private ListView listView;
	private ExpandableListView expListView;
	private View loadingView;
	private TextView emptyView;

	private CommonCategoriesCursorAdapter categoriesCursorAdapter;

	private LessonsCategoriesUpdateListener lessonsCategoriesUpdateListener;
	private SaveLessonsCategoriesUpdateListener saveLessonsCategoriesUpdateListener;
	private LessonsCoursesUpdateListener lessonsCoursesUpdateListener;
	private SaveLessonsCoursesUpdateListener saveLessonsCoursesUpdateListener;

	private CurriculumLessonsItems curriculumItems;
	private LessonsGroupsListAdapter curriculumAdapter;
	private SparseArray<String> curriculumCategoriesArray;
	private SparseIntArray curriculumCategoriesOrder;

	private LessonsRatingUpdateListener lessonsRatingUpdateListener;
	private TextView ratingTxt;
	private TextView lessonsCntTxt;
	private TextView coursesCntTxt;
	private LessonSingleItem incompleteLesson;
	private Button resumeLessonBtn;
	private SparseArray<String> curriculumCategoriesMap;
	private boolean libraryMode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_lessons_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.lessons);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		{ // Library mode init
			listView = (ListView) view.findViewById(R.id.listView);
			if (isNeedToUpgradePremium()) {
				view.findViewById(R.id.lessonsStatsView).setVisibility(View.GONE);
				view.findViewById(R.id.upgradeBtn).setOnClickListener(this);
			} else {
				View headerView = view.findViewById(R.id.lessonsStatsView);
				ratingTxt = (TextView) headerView.findViewById(R.id.lessonsRatingTxt);
				lessonsCntTxt = (TextView) headerView.findViewById(R.id.lessonsCompletedValueTxt);
				coursesCntTxt = (TextView) headerView.findViewById(R.id.coursesCompletedValueTxt);
				view.findViewById(R.id.upgradeView).setVisibility(View.GONE);

				ratingTxt.setText(String.valueOf(getAppData().getUserLessonsRating()));
				lessonsCntTxt.setText(String.valueOf(getAppData().getUserLessonsCompleteCnt()));
				coursesCntTxt.setText(String.valueOf(getAppData().getUserCourseCompleteCnt()));
			}

			View footerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_videos_curriculum_footer, null, false);
			((TextView) footerView.findViewById(R.id.headerTitleTxt)).setText(R.string.curriculum_lessons);
			footerView.setOnClickListener(this);
			listView.addFooterView(footerView);
			listView.setAdapter(categoriesCursorAdapter);
			listView.setOnItemClickListener(this);
		}

		{ // Curriculum mode
			expListView = (ExpandableListView) view.findViewById(R.id.expListView);
			View footerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_videos_curriculum_footer, null, false);
			((TextView) footerView.findViewById(R.id.headerTitleTxt)).setText(R.string.full_lesson_library);
			footerView.setOnClickListener(this);
			expListView.addFooterView(footerView);
			expListView.setOnChildClickListener(this);
			expListView.setGroupIndicator(null);
		}

		resumeLessonBtn = (Button) view.findViewById(R.id.resumeLessonBtn);
		resumeLessonBtn.setOnClickListener(this);

		showLibrary();

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_search_btn, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!isNeedToUpgradePremium()) {
			LoadItem loadItem = LoadHelper.getLessonsRating(getUserToken());
			new RequestJsonTask<LessonsRatingItem>(lessonsRatingUpdateListener).executeTask(loadItem);
		}
	}

	private void showLibrary() {
		libraryMode = getAppData().isUserChooseLessonsLibrary();

		listView.setVisibility(libraryMode ? View.VISIBLE : View.GONE);
		expListView.setVisibility(libraryMode ? View.GONE : View.VISIBLE);

		if (!libraryMode) {
			if (need2update) {

				// get saved categories
				Cursor categoriesCursor = DbDataManager.query(getContentResolver(), DbHelper.getLessonsCurriculumCategories());

				if (categoriesCursor != null && categoriesCursor.moveToFirst()) {
					fillCategoriesList(categoriesCursor);
					Cursor coursesCursor = DbDataManager.query(getContentResolver(), DbHelper.getLessonCoursesForUser(getUsername()));

					if (coursesCursor != null && coursesCursor.moveToFirst()) {
						updateUiData(coursesCursor);
					} else {
						getFullCourses();
					}
				} else if (AppUtils.isNetworkAvailable(getActivity())) {
					getCategories();
				}

			} else { // load data to listHeader view
				// update to display completed mark
				Cursor coursesCursor = DbDataManager.query(getContentResolver(), DbHelper.getLessonCoursesForUser(getUsername()));

				if (coursesCursor != null && coursesCursor.moveToFirst()) { // TODO adjust logic if nothing was really changed
					updateUiData(coursesCursor);
				}

				expListView.setAdapter(curriculumAdapter);
				curriculumAdapter.notifyDataSetChanged();
			}
		} else {
			listView.setAdapter(categoriesCursorAdapter);
		}
	}

	private void getCategories() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_LESSONS_CATEGORIES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<CommonFeedCategoryItem>(lessonsCategoriesUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		String sectionName = DbDataManager.getString(cursor, DbScheme.V_NAME);

		getActivityFace().openFragment(LessonsCategoriesFragment.createInstance(sectionName));
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v.getId() == R.id.resumeLessonBtn) {
			int lessonId = incompleteLesson.getId();
			long courseId = incompleteLesson.getCourseId();

			getActivityFace().openFragment(GameLessonFragment.createInstance(lessonId, courseId));
		} else if (v.getId() == R.id.curriculumHeader) {
			getAppData().setUserChooseLessonsLibrary(!libraryMode);
			showLibrary();
		} else if (v.getId() == R.id.upgradeBtn) {
			getActivityFace().openFragment(new UpgradeFragment());
		}
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		int categoryId = curriculumItems.getDisplayOrder().get(groupPosition);
		int courseId = curriculumItems.getIds().get(categoryId).get(childPosition);
		getActivityFace().openFragment(LessonsCourseFragment.createInstance(courseId, categoryId));
		return false;
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

	private class LessonsCategoriesUpdateListener extends CommonLogicFragment.ChessUpdateListener<CommonFeedCategoryItem> {
		public LessonsCategoriesUpdateListener() {
			super(CommonFeedCategoryItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			showLoadingView(show);
		}

		@Override
		public void updateData(CommonFeedCategoryItem returnedObj) {
			super.updateData(returnedObj);

			new SaveLessonsCategoriesTask(saveLessonsCategoriesUpdateListener, returnedObj.getData(), getContentResolver()).executeTask();
		}
	}

	private class SaveLessonsCategoriesUpdateListener extends CommonLogicFragment.ChessUpdateListener<CommonFeedCategoryItem.Data> {

		@Override
		public void updateData(CommonFeedCategoryItem.Data returnedObj) {
			// get saved categories
			Cursor cursor =	DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.LESSONS_CATEGORIES));

			if (cursor != null && cursor.moveToFirst()) {

				fillCategoriesList(cursor);

				getFullCourses();
			}

			Cursor libraryCursor = DbDataManager.query(getContentResolver(), DbHelper.getLessonsLibraryCategories());
			categoriesCursorAdapter.changeCursor(libraryCursor);
		}
	}

	private void getFullCourses() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_LESSONS_COURSES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<LessonCourseListItem>(lessonsCoursesUpdateListener).executeTask(loadItem);
	}

	private void fillCategoriesList(Cursor cursor) {
		curriculumCategoriesArray = new SparseArray<String>();
		SparseArray<String> libraryCategoriesArray = new SparseArray<String>();
		curriculumCategoriesOrder = new SparseIntArray();
		SparseIntArray libraryCategoriesOrder = new SparseIntArray();

		do {
			boolean isCurriculum = DbDataManager.getInt(cursor, DbScheme.V_IS_CURRICULUM) > 0;
			int id = DbDataManager.getInt(cursor, DbScheme.V_CATEGORY_ID);
			int displayOrder = DbDataManager.getInt(cursor, DbScheme.V_DISPLAY_ORDER);
			String name = DbDataManager.getString(cursor, DbScheme.V_NAME);
			if (isCurriculum) {
				curriculumCategoriesArray.put(id, name);
				curriculumCategoriesOrder.put(displayOrder, id);
			} else {
				libraryCategoriesArray.put(id, name);
				libraryCategoriesOrder.put(displayOrder, id);
			}

		} while (cursor.moveToNext());
		cursor.close();

		curriculumItems.setCategories(curriculumCategoriesArray);
		curriculumItems.setDisplayOrder(curriculumCategoriesOrder);
	}

	private class LessonsCoursesUpdateListener extends CommonLogicFragment.ChessUpdateListener<LessonCourseListItem> {
		public LessonsCoursesUpdateListener() {
			super(LessonCourseListItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			showLoadingView(show);
		}

		@Override
		public void updateData(LessonCourseListItem returnedObj) {
			super.updateData(returnedObj);

			new SaveLessonsCoursesListTask(saveLessonsCoursesUpdateListener, returnedObj.getData(), getContentResolver(),
					getUsername()).executeTask();
		}
	}

	private class SaveLessonsCoursesUpdateListener extends ChessUpdateListener<LessonCourseListItem.Data> {

		@Override
		public void updateData(LessonCourseListItem.Data returnedObj) {
			// get saved courses

			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getLessonCoursesForUser(getUsername()));
			if (cursor != null && cursor.moveToFirst()) {
				updateUiData(cursor);

				need2update = false;
			}
		}
	}

	private void updateUiData(Cursor cursor) {
		LinkedHashMap<Integer, List<LessonCourseListItem.Data>> curriculumCoursesTable = new LinkedHashMap<Integer, List<LessonCourseListItem.Data>>();
		int categoriesCnt = curriculumCategoriesArray.size();

		for (int z = 0; z < categoriesCnt; z++) {
			int categoryId = curriculumCategoriesArray.keyAt(z);
			curriculumCoursesTable.put(categoryId, new ArrayList<LessonCourseListItem.Data>());
		}
		do {
			int categoryId = DbDataManager.getInt(cursor, DbScheme.V_CATEGORY_ID);
			boolean isCurriculum = false;
			for (int k = 0; k < curriculumCategoriesMap.size(); k++) {
				// if categoryId belongs to curriculum categories
				if (curriculumCategoriesMap.keyAt(k) == categoryId) {
					isCurriculum = true;
					break;
				}
			}

			if (isCurriculum) {
				int id = DbDataManager.getInt(cursor, DbScheme.V_ID);
				String courseName = DbDataManager.getString(cursor, DbScheme.V_NAME);
				boolean isCompleted = DbDataManager.getInt(cursor, DbScheme.V_COURSE_COMPLETED) > 0;

				LessonCourseListItem.Data data = new LessonCourseListItem.Data();
				data.setId(id);
				data.setCategoryId(categoryId);
				data.setName(courseName);
				data.setCourseCompleted(isCompleted);

				curriculumCoursesTable.get(categoryId).add(data);
			}

		} while (cursor.moveToNext());

		{ // Titles
			// organize by category
			SparseArray<SparseArray<String>> categories = new SparseArray<SparseArray<String>>();
			for (int k = 0; k < categoriesCnt; k++) {
				int categoryId = curriculumCategoriesArray.keyAt(k);
				List<LessonCourseListItem.Data> list = curriculumCoursesTable.get(categoryId);
				int coursesCnt = list.size();
				categories.put(categoryId, new SparseArray<String>());
				for (int i = 0; i < coursesCnt; i++) {
					LessonCourseListItem.Data data = list.get(i);
					categories.get(categoryId).put(i, data.getName());
				}
			}
			curriculumItems.setTitles(categories);
		}

		{ // Ids
			SparseArray<SparseIntArray> ids = new SparseArray<SparseIntArray>();
			for (int k = 0; k < categoriesCnt; k++) {
				int categoryId = curriculumCategoriesArray.keyAt(k);
				List<LessonCourseListItem.Data> list = curriculumCoursesTable.get(categoryId);
				int coursesCnt = list.size();
				ids.put(categoryId, new SparseIntArray());
				for (int i = 0; i < coursesCnt; i++) {
					LessonCourseListItem.Data data = list.get(i);
					ids.get(categoryId).put(i, data.getId());
				}
			}

			curriculumItems.setIds(ids);
		}

		{ // Completed Marks
			SparseArray<SparseBooleanArray> completedMarks = new SparseArray<SparseBooleanArray>();
			for (int k = 0; k < categoriesCnt; k++) {
				int categoryId = curriculumCategoriesOrder.get(k);
				List<LessonCourseListItem.Data> list = curriculumCoursesTable.get(categoryId);
				int coursesCnt = list.size();
				completedMarks.put(categoryId, new SparseBooleanArray());
				for (int i = 0; i < coursesCnt; i++) {
					LessonCourseListItem.Data data = list.get(i);
					completedMarks.get(categoryId).put(i, data.isCourseCompleted());
				}
			}

			curriculumItems.setViewedMarks(completedMarks);
		}

		curriculumAdapter = new LessonsGroupsListAdapter(curriculumItems);
		expListView.setAdapter(curriculumAdapter);

		cursor.close();

		// check if we have incomplete lessons
		List<LessonSingleItem> incompleteLessons = DbDataManager.getIncompleteLessons(getContentResolver(), getUsername());
		if (incompleteLessons != null) {
			int last = incompleteLessons.size() - 1;
			incompleteLesson = incompleteLessons.get(last);
			resumeLessonBtn.setVisibility(View.VISIBLE);
		} else {
			resumeLessonBtn.setVisibility(View.GONE);
		}
	}

	private void showLoadingView(boolean show) {
		if (show) {
			emptyView.setVisibility(View.GONE);
			loadingView.setVisibility(View.VISIBLE);
		} else {
			loadingView.setVisibility(View.GONE);
		}
	}

	private void init() {

		curriculumItems = new CurriculumLessonsItems();
		categoriesCursorAdapter = new CommonCategoriesCursorAdapter(getActivity(), null);

		lessonsCategoriesUpdateListener = new LessonsCategoriesUpdateListener();
		saveLessonsCategoriesUpdateListener = new SaveLessonsCategoriesUpdateListener();
		lessonsCoursesUpdateListener = new LessonsCoursesUpdateListener();
		saveLessonsCoursesUpdateListener = new SaveLessonsCoursesUpdateListener();
		lessonsRatingUpdateListener = new LessonsRatingUpdateListener();

		// put Categories names to appropriate sections.

		curriculumCategoriesMap = new SparseArray<String>();
		curriculumCategoriesMap.put(9, "Beginner");
		curriculumCategoriesMap.put(10, "Intermediate");
		curriculumCategoriesMap.put(11, "Advanced");
		curriculumCategoriesMap.put(12, "Expert");
		curriculumCategoriesMap.put(13, "Master");

/*
these are the categories for CURRICULUM:
id: 9,name: "Beginner"
id: 10,name: "Intermediate"
id: 11,name: "Advanced"
id: 12,name: "Expert"
id: 13,name: "Master"

then these are the Library categories

id: 6,name: "Rules and Basics"
id: 4,name: "Strategy"
id: 5,name: "Tactics"
id: 3,name: "Attacks"
id: 7,name: "Openings"
id: 2,name: "Endgames"
id: 8,name: "Games"
id: 1,name: "Misc"
*/

		// get from DB categories for Full Lessons Library(not Curriculum)
		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getLessonsLibraryCategories());
		categoriesCursorAdapter.changeCursor(cursor);

	}

	public class LessonsGroupsListAdapter extends BaseExpandableListAdapter {
		private final LayoutInflater inflater;
		private final int watchedTextColor;
		private final int unWatchedTextColor;
		private final int watchedIconColor;
		private final CurriculumLessonsItems items;

		public LessonsGroupsListAdapter(CurriculumLessonsItems items) {
			this.items = items;
			inflater = LayoutInflater.from(getActivity());
			watchedTextColor = getResources().getColor(R.color.new_light_grey_3);
			unWatchedTextColor = getResources().getColor(R.color.new_text_blue);
			watchedIconColor = getResources().getColor(R.color.new_light_grey_2);
		}

		@Override
		public int getGroupCount() {
			return items.getCategories().size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			int categoryId = items.getDisplayOrder().get(groupPosition);
			return items.getTitles().get(categoryId).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			int categoryId = items.getDisplayOrder().get(groupPosition);
			return items.getCategories().get(categoryId);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			int categoryId = items.getDisplayOrder().get(groupPosition);
			return items.getTitles().get(categoryId).get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.new_video_header, parent, false);
				holder = new ViewHolder();

				holder.text = (TextView) convertView.findViewById(R.id.headerTitleTxt);
				holder.icon = (TextView) convertView.findViewById(R.id.headerIconTxt);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.text.setText(getGroup(groupPosition).toString());

			if (isExpanded) {
				holder.icon.setText(R.string.ic_up);
			} else {
				holder.icon.setText(R.string.ic_down);
			}

			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.new_completed_list_item, parent, false);
				holder = new ViewHolder();

				holder.text = (TextView) convertView.findViewById(R.id.titleTxt);
				holder.icon = (TextView) convertView.findViewById(R.id.completedIconTxt);
				convertView.setTag(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.text.setText(getChild(groupPosition, childPosition).toString());

			if (isCourseCompleted(groupPosition, childPosition)) {
				holder.text.setTextColor(watchedTextColor);
				holder.icon.setTextColor(watchedIconColor);
				holder.icon.setText(R.string.ic_check);
			} else {
				holder.text.setTextColor(unWatchedTextColor);
				holder.icon.setText(Symbol.EMPTY);
			}

			return convertView;
		}

		private boolean isCourseCompleted(int groupPosition, int childPosition) {
			int categoryId = items.getDisplayOrder().get(groupPosition);
			return items.getViewedMarks().get(categoryId).get(childPosition);
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		private class ViewHolder {
			TextView text;
			TextView icon;
		}
	}

	private class LessonsRatingUpdateListener extends ChessUpdateListener<LessonsRatingItem> {

		private LessonsRatingUpdateListener() {
			super(LessonsRatingItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			// don't show any progress
		}

		@Override
		public void updateData(LessonsRatingItem returnedObj) {
			super.updateData(returnedObj);

			LessonsRatingItem.Data lessonsRating = returnedObj.getData();
			ratingTxt.setText(String.valueOf(lessonsRating.getRating()));
			lessonsCntTxt.setText(String.valueOf(lessonsRating.getCompletedLessons()));
			coursesCntTxt.setText(String.valueOf(lessonsRating.getCompletedCourses()));

			getAppData().setUserLessonsRating(lessonsRating.getRating());
			getAppData().setUserLessonsCompleteCnt(lessonsRating.getCompletedLessons());
			getAppData().setUserCourseCompleteCnt(lessonsRating.getCompletedCourses());
		}
	}

}
