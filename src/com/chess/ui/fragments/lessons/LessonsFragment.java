package com.chess.ui.fragments.lessons;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.CommonFeedCategoryItem;
import com.chess.backend.entity.new_api.LessonCourseItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.tasks.SaveLessonsCategoriesTask;
import com.chess.db.tasks.SaveLessonsCoursesTask;
import com.chess.model.CurriculumItems;
import com.chess.ui.adapters.CommonCategoriesCursorAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.videos.VideoDetailsCurriculumFragment;
import com.chess.ui.fragments.videos.VideoDetailsFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.07.13
 * Time: 14:59
 */
public class LessonsFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener,
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

	private boolean need2Update = true;

	private CurriculumItems curriculumItems;
	private boolean curriculumMode = true;
	private LessonsGroupsListAdapter curriculumAdapter;
	private SparseArray<String> categoriesArray;

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
			View footerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_videos_curriculum_footer, null, false);
			((TextView)footerView.findViewById(R.id.headerTitleTxt)).setText(R.string.full_lesson_library);
			footerView.setOnClickListener(this);
			listView.addFooterView(footerView);
			listView.setAdapter(categoriesCursorAdapter);
			listView.setOnItemClickListener(this);
		}

		{ // Curriculum mode
			expListView = (ExpandableListView) view.findViewById(R.id.expListView);
			View footerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_videos_curriculum_footer, null, false);
			((TextView)footerView.findViewById(R.id.headerTitleTxt)).setText(R.string.full_lesson_library);
			footerView.setOnClickListener(this);
			expListView.addFooterView(footerView);
			expListView.setOnChildClickListener(this);
			expListView.setGroupIndicator(null);
		}

		showLibrary();

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_search, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	private void showLibrary() {
		boolean show = !curriculumMode;
		listView.setVisibility(show ? View.VISIBLE : View.GONE);
		expListView.setVisibility(show ? View.GONE : View.VISIBLE);

		getCategories();
//		if (show) {
//			if (need2Update) {
//
//				// get saved categories
//				Cursor categoriesCursor = getContentResolver().query(DBConstants.uriArray[DBConstants.LESSONS_CATEGORIES], null, null, null, null);
//
//				if (categoriesCursor != null && categoriesCursor.moveToFirst()) {
//					categoriesCursorAdapter.changeCursor(categoriesCursor);
//				}
//
//				if (AppUtils.isNetworkAvailable(getActivity())) {
//					getCategories();
//				}
//
//			} else { // load data to listHeader view
//				categoriesCursorAdapter.notifyDataSetChanged();
//			}
//		} else {
//			expListView.setAdapter(curriculumAdapter);
//		}
	}

	private void getCategories() {
		LoadItem loadItem = new LoadItem();
		loadItem.setLoadPath(RestHelper.CMD_LESSONS_CATEGORIES);
		loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

		new RequestJsonTask<CommonFeedCategoryItem>(lessonsCategoriesUpdateListener).executeTask(loadItem);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		String sectionName = DBDataManager.getString(cursor, DBConstants.V_NAME);

		getActivityFace().openFragment(LessonsCategoriesFragment.createInstance(sectionName));
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v.getId() == R.id.titleTxt) { // Clicked on title in Curriculum mode, open details
			Integer childPosition = (Integer) v.getTag(R.id.list_item_id);
			Integer groupPosition = (Integer) v.getTag(R.id.list_item_id_group);

			int id = curriculumItems.getIds()[groupPosition][childPosition];
			long savedId = DBDataManager.haveSavedVideoById(getActivity(), id);
			if (savedId != -1) {
				getActivityFace().openFragment(VideoDetailsFragment.createInstance(savedId));
			} else {
				getActivityFace().openFragment(VideoDetailsCurriculumFragment.createInstance4Curriculum(id));
			}

		} else if (v.getId() == R.id.curriculumHeader) {



//			getAppData().setUserChooseLessonsLibrary(false);
//			curriculumMode = true;
//			showLibrary();
		}
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
		int courseId = curriculumItems.getIds()[groupPosition][childPosition];
		getActivityFace().openFragment(LessonsCourseFragment.createInstance(courseId));
		return false;
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
			Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.LESSONS_CATEGORIES], null, null, null, null);

			if (cursor != null && cursor.moveToFirst()) {
				String[] categories = new String[cursor.getCount()];
				categoriesArray = new SparseArray<String>();

				int i = 0;
				do {
					int id = DBDataManager.getInt(cursor, DBConstants.V_CATEGORY_ID);
					String name = DBDataManager.getString(cursor, DBConstants.V_NAME);
					categoriesArray.put(id, name);
					categories[i] = name;
					i++;
				} while (cursor.moveToNext());


				curriculumItems.setCategories(categories);

				// get courses
				LoadItem loadItem = new LoadItem();
				loadItem.setLoadPath(RestHelper.CMD_LESSONS_COURSES);
				loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());

				new RequestJsonTask<LessonCourseItem>(lessonsCoursesUpdateListener).executeTask(loadItem);
			}
		}
	}

	private class LessonsCoursesUpdateListener extends CommonLogicFragment.ChessUpdateListener<LessonCourseItem> {
		public LessonsCoursesUpdateListener() {
			super(LessonCourseItem.class);
		}

		@Override
		public void showProgress(boolean show) {
			showLoadingView(show);
		}

		@Override
		public void updateData(LessonCourseItem returnedObj) {
			super.updateData(returnedObj);

			new SaveLessonsCoursesTask(saveLessonsCoursesUpdateListener, returnedObj.getData(), getContentResolver(),
					getUsername()).executeTask();
		}
	}

	private class SaveLessonsCoursesUpdateListener extends CommonLogicFragment.ChessUpdateListener<LessonCourseItem.Data> {

		@Override
		public void updateData(LessonCourseItem.Data returnedObj) {
			// get saved courses           // TODO check strict mode

			// TODO add user parameter

			Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.LESSONS_COURSES], null, null, null, null);

			if (cursor != null && cursor.moveToFirst()) {
				LinkedHashMap<Integer, List<LessonCourseItem.Data>> courseTable = new LinkedHashMap<Integer, List<LessonCourseItem.Data>>();
				int categoriesCnt = categoriesArray.size();
				for (int z = 0; z < categoriesCnt; z++) {
					int categoryId = categoriesArray.keyAt(z);
					courseTable.put(categoryId, new ArrayList<LessonCourseItem.Data>());
				}
				do {
					int id = DBDataManager.getInt(cursor, DBConstants.V_ID);
					int categoryId = DBDataManager.getInt(cursor, DBConstants.V_CATEGORY_ID);
					String courseName = DBDataManager.getString(cursor, DBConstants.V_NAME);
					boolean isCompleted = DBDataManager.getInt(cursor, DBConstants.V_COURSE_COMPLETED) > 0;

					LessonCourseItem.Data data = new LessonCourseItem.Data();
					data.setId(id);
					data.setCategoryId(categoryId);
					data.setName(courseName);
					data.setCourseCompleted(isCompleted);

					courseTable.get(categoryId).add(data);

				} while (cursor.moveToNext());

				{ // Titles
					// organize by category
					String[][] categories = new String[categoriesCnt][];
					for (int k = 0; k < categoriesCnt; k++) {
						int categoryId = categoriesArray.keyAt(k);
						List<LessonCourseItem.Data> list = courseTable.get(categoryId);
						int coursesCnt = list.size();
						categories[k] = new String[coursesCnt];
						for (int i = 0; i < coursesCnt; i++) {
							LessonCourseItem.Data data = list.get(i);
							categories[k][i] = data.getName();
						}
					}
					curriculumItems.setTitles(categories);
				}

				{ // Ids
					int[][] ids = new int[categoriesCnt][];
					for (int k = 0; k < categoriesCnt; k++) {
						int categoryId = categoriesArray.keyAt(k);
						List<LessonCourseItem.Data> list = courseTable.get(categoryId);
						int coursesCnt = list.size();
						ids[k] = new int[coursesCnt];
						for (int i = 0; i < coursesCnt; i++) {
							LessonCourseItem.Data data = list.get(i);
							ids[k][i] = data.getId();
						}
					}

					curriculumItems.setIds(ids);
				}

				{ // Completed Marks
					boolean[][] completedMarks = new boolean[categoriesCnt][];
					for (int k = 0; k < categoriesCnt; k++) {
						int categoryId = categoriesArray.keyAt(k);
						List<LessonCourseItem.Data> list = courseTable.get(categoryId);
						int coursesCnt = list.size();
						completedMarks[k] = new boolean[coursesCnt];
						for (int i = 0; i < coursesCnt; i++) {
							LessonCourseItem.Data data = list.get(i);
							completedMarks[k][i] = data.isCourseCompleted();
						}
					}

					curriculumItems.setViewedMarks(completedMarks);
				}

				curriculumAdapter = new LessonsGroupsListAdapter(LessonsFragment.this, curriculumItems);
				expListView.setAdapter(curriculumAdapter);


//				categoriesCursorAdapter.changeCursor(cursor);
//				listView.setAdapter(categoriesCursorAdapter);

				need2Update = false;
			}
		}
	}

	private void showLoadingView(boolean show) {
		if (show) {
			emptyView.setVisibility(View.GONE);
				loadingView.setVisibility(View.VISIBLE);
		} else {
			if (curriculumMode) {
				expListView.setVisibility(View.VISIBLE);
			} else {
				listView.setVisibility(View.VISIBLE);
			}
			loadingView.setVisibility(View.GONE);
		}
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

	private void init() {
		curriculumMode = !getAppData().isUserChooseLessonsLibrary();

		curriculumItems = new CurriculumItems();
		categoriesCursorAdapter = new CommonCategoriesCursorAdapter(getActivity(), null);

		lessonsCategoriesUpdateListener = new LessonsCategoriesUpdateListener();
		saveLessonsCategoriesUpdateListener = new SaveLessonsCategoriesUpdateListener();
		lessonsCoursesUpdateListener = new LessonsCoursesUpdateListener();
		saveLessonsCoursesUpdateListener = new SaveLessonsCoursesUpdateListener();
	}

	public class LessonsGroupsListAdapter extends BaseExpandableListAdapter {
		private final LayoutInflater inflater;
		private final int watchedTextColor;
		private final int unWatchedTextColor;
		private final int watchedIconColor;
		private final CurriculumItems items;
		private ItemClickListenerFace clickFace;

		public LessonsGroupsListAdapter(ItemClickListenerFace clickFace, CurriculumItems items) {
			this.clickFace = clickFace;
			this.items = items;
			inflater = LayoutInflater.from(getActivity());
			watchedTextColor = getResources().getColor(R.color.new_light_grey_3);
			unWatchedTextColor = getResources().getColor(R.color.new_text_blue);
			watchedIconColor = getResources().getColor(R.color.new_light_grey_2);
		}

		@Override
		public int getGroupCount() {
			return items.getCategories().length;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return items.getTitles()[groupPosition].length;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return items.getCategories()[groupPosition];
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return items.getTitles()[groupPosition][childPosition];
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


//			if (groupPosition == LIBRARY) {
//				holder.icon.setText(R.string.ic_right);
//			} else {
				if (isExpanded) {
					holder.icon.setText(R.string.ic_down);
				} else {
					holder.icon.setText(R.string.ic_up);
				}
//			}

			return convertView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.new_video_list_item, parent, false);
				holder = new ViewHolder();

				holder.text = (TextView) convertView.findViewById(R.id.titleTxt);
				holder.icon = (TextView) convertView.findViewById(R.id.watchedIconTxt);
				convertView.setTag(holder);

//				holder.text.setOnClickListener(clickFace);
//				holder.icon.setOnClickListener(clickFace);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

//			holder.text.setTag(R.id.list_item_id, childPosition);
//			holder.text.setTag(R.id.list_item_id_group, groupPosition);
//
//			holder.icon.setTag(R.id.list_item_id, childPosition);
//			holder.icon.setTag(R.id.list_item_id_group, groupPosition);

			holder.text.setText(getChild(groupPosition, childPosition).toString());

			if (isCourseCompleted(groupPosition, childPosition)) {
				holder.text.setTextColor(watchedTextColor);
				holder.icon.setTextColor(watchedIconColor);
				holder.icon.setText(R.string.ic_check);
			} else {
				holder.text.setTextColor(unWatchedTextColor);
				holder.icon.setText(R.string.ic_right);
			}

			return convertView;
		}

		private boolean isCourseCompleted(int groupPosition, int childPosition) {
			return items.getViewedMarks()[groupPosition][childPosition];
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


}
