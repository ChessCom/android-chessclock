package com.chess.ui.fragments.lessons;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.CommonFeedCategoryItem;
import com.chess.backend.entity.api.LessonCourseListItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.SaveLessonsCategoriesTask;
import com.chess.db.tasks.SaveLessonsCoursesListTask;
import com.chess.model.CurriculumLessonsItems;
import com.chess.ui.adapters.CommonCategoriesCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.FragmentParentFace;
import com.chess.utilities.AppUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.11.13
 * Time: 5:42
 */
public class LessonsCurriculumFragmentTablet extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private ExpandableListView expListView;
	private TextView emptyView;

	private CommonCategoriesCursorAdapter categoriesCursorAdapter;

	private LessonsCategoriesUpdateListener lessonsCategoriesUpdateListener;
	private SaveLessonsCategoriesUpdateListener saveLessonsCategoriesUpdateListener;
	private LessonsCoursesUpdateListener lessonsCoursesUpdateListener;
	private SaveLessonsCoursesUpdateListener saveLessonsCoursesUpdateListener;

	private CurriculumLessonsItems curriculumItems;
	private CurriculumListAdapter curriculumAdapter;
	private SparseArray<String> curriculumCategoriesArray;
	private SparseIntArray curriculumCategoriesOrder;

	private SparseArray<String> curriculumCategoriesMap;
	private FragmentParentFace parentFace;

	public static LessonsCurriculumFragmentTablet createInstance(FragmentParentFace parentFace) {
		LessonsCurriculumFragmentTablet fragment = new LessonsCurriculumFragmentTablet();
		fragment.parentFace = parentFace;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_lessons_curriculum_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		setNeedToChangeActionButtons(false);
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.lessons);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		{ // Curriculum mode
			expListView = (ExpandableListView) view.findViewById(R.id.expListView);
			expListView.setGroupIndicator(null);
		}

		showLibrary();
	}

	private void showLibrary() {

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
	}

	private void getCategories() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.getInstance().CMD_LESSONS_CATEGORIES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<CommonFeedCategoryItem>(lessonsCategoriesUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Integer groupPosition = (Integer) view.getTag(R.id.list_item_id_group);
		int categoryId = curriculumItems.getDisplayOrder().get(groupPosition);
		int courseId = curriculumItems.getIds().get(categoryId).get(position);

		parentFace.changeFragment(LessonsCourseFragmentTablet.createInstance(courseId, categoryId));
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
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getAll(DbScheme.Tables.LESSONS_CATEGORIES));

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
		cursor.close();

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

		curriculumAdapter = new CurriculumListAdapter(curriculumItems);
		expListView.setAdapter(curriculumAdapter);
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

		// put Categories names to appropriate sections.

		curriculumCategoriesMap = new SparseArray<String>();
		curriculumCategoriesMap.put(9, "Beginner");
		curriculumCategoriesMap.put(10, "Intermediate");
		curriculumCategoriesMap.put(11, "Advanced");
		curriculumCategoriesMap.put(12, "Expert");
		curriculumCategoriesMap.put(13, "Master");

		// get from DB categories for Full Lessons Library(not Curriculum)
		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getLessonsLibraryCategories());
		categoriesCursorAdapter.changeCursor(cursor);
	}

	public class CurriculumListAdapter extends BaseExpandableListAdapter {
		private final LayoutInflater inflater;

		private final CurriculumLessonsItems items;
		private final int columnHeight;
		private final int spacing;

		public CurriculumListAdapter(CurriculumLessonsItems items) {
			this.items = items;
			inflater = LayoutInflater.from(getActivity());
			columnHeight = getResources().getDimensionPixelSize(R.dimen.video_thumb_size);
			spacing = getResources().getDimensionPixelSize(R.dimen.grid_view_spacing);
		}

		@Override
		public int getGroupCount() {
			return items.getCategories().size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return 1;
		}

		@Override
		public Object getGroup(int groupPosition) {
			int categoryId = items.getDisplayOrder().get(groupPosition);
			return items.getCategories().get(categoryId);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			int categoryId = items.getDisplayOrder().get(groupPosition);
			return items.getTitles().get(categoryId);
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
			HeaderViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.new_common_titled_list_item, parent, false);
				holder = new HeaderViewHolder();

				holder.text = (TextView) convertView.findViewById(R.id.headerTitleTxt);
				holder.icon = (TextView) convertView.findViewById(R.id.headerIconTxt);
				convertView.setTag(holder);
			} else {
				holder = (HeaderViewHolder) convertView.getTag();
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
			convertView = inflater.inflate(R.layout.new_common_grid_title_item, parent, false);

			GridView gridView = (GridView) convertView.findViewById(R.id.gridView);

			SparseArray<String> child = (SparseArray<String>) getChild(groupPosition, childPosition);

			gridView.setAdapter(new CurriculumTitlesAdapter(getActivity(),  groupPosition)); // TODO improve
			gridView.setOnItemClickListener(LessonsCurriculumFragmentTablet.this);

			// calculate the column and row counts based on your display
			final int rowCount = (int) Math.ceil(child.size() / (float) 2);

			// calculate and set the height for the current gridView
			gridView.getLayoutParams().height = Math.round(rowCount * (columnHeight + spacing * 2));

			return convertView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

		private class HeaderViewHolder {
			TextView text;
			TextView icon;
		}
	}

	private class CurriculumTitlesAdapter extends BaseAdapter {

		private int groupPosition;
		private final LayoutInflater inflater;
		private final int incompleteIconColor;
		private final int completedTextColor;
		private final int incompleteTextColor;
		private final int completedIconColor;

		public CurriculumTitlesAdapter(Context context, int groupPosition) {
			this.groupPosition = groupPosition;
			inflater = LayoutInflater.from(context);

			completedTextColor = getResources().getColor(R.color.new_light_grey_3);
			incompleteTextColor = getResources().getColor(R.color.new_text_blue);
			completedIconColor = getResources().getColor(R.color.new_light_grey_2);
			incompleteIconColor = getResources().getColor(R.color.orange_button);
		}

		private boolean isCourseCompleted(int groupPosition, int childPosition) {
			int categoryId = curriculumItems.getDisplayOrder().get(groupPosition);
			return curriculumItems.getViewedMarks().get(categoryId).get(childPosition);
		}

		@Override
		public int getCount() {
			int categoryId = curriculumItems.getDisplayOrder().get(groupPosition);
			return curriculumItems.getTitles().get(categoryId).size();
		}

		@Override
		public Object getItem(int position) {
			int categoryId = curriculumItems.getDisplayOrder().get(groupPosition);
			return curriculumItems.getTitles().get(categoryId).get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.new_common_thumb_titles_list_item, parent, false);
				holder = new ViewHolder();

				holder.text = (TextView) convertView.findViewById(R.id.titleTxt);
				holder.statusTxt = (TextView) convertView.findViewById(R.id.completedIconTxt);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.text.setText(getItem(position).toString());
			convertView.setTag(R.id.list_item_id_group, groupPosition);

			if (isCourseCompleted(groupPosition, position)) {
				holder.text.setTextColor(completedTextColor);
				holder.statusTxt.setTextColor(completedIconColor);
				holder.statusTxt.setText(R.string.ic_check);
			} else {
				holder.text.setTextColor(incompleteTextColor);
				holder.statusTxt.setText(R.string.ic_lessons);
				holder.statusTxt.setTextColor(incompleteIconColor);
			}
			return convertView;
		}

		private class ViewHolder {
			TextView text;
			TextView statusTxt;
		}
	}

}
