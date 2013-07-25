package com.chess.ui.fragments.lessons;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.entity.LoadItem;
import com.chess.backend.entity.new_api.LessonCourseItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.SaveLessonsCourseTask;
import com.chess.ui.adapters.LessonsItemAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.upgrade.UpgradeFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.07.13
 * Time: 21:36
 */
public class LessonsCourseFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private static final String COURSE_ID = "course_id";

	private LessonsItemAdapter lessonsItemsAdapter;
	private int courseId;
	private TextView courseTitleTxt;
	private TextView courseDescriptionTxt;
	private CourseUpdateListener courseUpdateListener;
	private boolean need2update = true;
	private LessonCourseItem.Data courseItem;
	private CourseSaveListener courseSaveListener;

	public LessonsCourseFragment() {}

	public static LessonsCourseFragment createInstance(int courseId) {
		LessonsCourseFragment fragment = new LessonsCourseFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(COURSE_ID, courseId);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments() != null) {
			courseId = getArguments().getInt(COURSE_ID);
		} else {
			courseId = savedInstanceState.getInt(COURSE_ID);
		}

		lessonsItemsAdapter = new LessonsItemAdapter(getActivity(), null);
		courseUpdateListener = new CourseUpdateListener();
		courseSaveListener = new CourseSaveListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_lessons_course_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.lessons);

		view.findViewById(R.id.upgradeBtn).setOnClickListener(this);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		// Set header
		View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.new_lessons_course_header_view, null, false);
		courseTitleTxt = (TextView) headerView.findViewById(R.id.courseTitleTxt);
		courseDescriptionTxt = (TextView) headerView.findViewById(R.id.courseDescriptionTxt);
		listView.addHeaderView(headerView);

		listView.setAdapter(lessonsItemsAdapter);
		listView.setOnItemClickListener(this);

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_search, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onStart() {
		super.onStart();

		if (need2update) {

			Cursor cursor = DBDataManager.executeQuery(getContentResolver(), DbHelper.getLessonCourseById(courseId));

			if (cursor != null && cursor.moveToFirst()) {
				courseItem = DBDataManager.getLessonsCourseItemFromCursor(cursor);

				Cursor lessonsListCursor = DBDataManager.executeQuery(getContentResolver(), DbHelper.getLessonsListByCourseId(courseId));
				if (lessonsListCursor.moveToFirst()) {
					List<LessonCourseItem.LessonListItem> lessons = new ArrayList<LessonCourseItem.LessonListItem>();
					do {
						lessons.add(DBDataManager.getLessonsListItemFromCursor(lessonsListCursor));
					} while(lessonsListCursor.moveToNext());
					courseItem.setLessons(lessons);

					fillCourseData();
				}
			} else {
				LoadItem loadItem = LoadHelper.getLessonsByCourseId(getUserToken(), courseId);

				new RequestJsonTask<LessonCourseItem>(courseUpdateListener).executeTask(loadItem);
			}
		} else {
			courseTitleTxt.setText(courseItem.getCourseName());
			courseDescriptionTxt.setText(courseItem.getDescription());

			List<LessonCourseItem.LessonListItem> lessons = courseItem.getLessons();
			lessonsItemsAdapter.setItemsList(lessons);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(COURSE_ID, courseId);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		LessonCourseItem.LessonListItem lessonItem = (LessonCourseItem.LessonListItem) parent.getItemAtPosition(position);
		getActivityFace().openFragment(GameLessonFragment.createInstance(lessonItem.getId()));
	}


	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.upgradeBtn) {
			getActivityFace().openFragment(new UpgradeFragment());
		}
	}

	private class CourseUpdateListener extends ChessLoadUpdateListener<LessonCourseItem> {

		private CourseUpdateListener() {
			super(LessonCourseItem.class);
		}

		@Override
		public void updateData(LessonCourseItem returnedObj) {
			super.updateData(returnedObj);

			courseItem = returnedObj.getData();
			fillCourseData();

			new SaveLessonsCourseTask(courseSaveListener, courseItem, getContentResolver()).executeTask();
		}
	}

	private void fillCourseData() {
		courseItem.setId(courseId);

		courseTitleTxt.setText(courseItem.getCourseName());
		courseDescriptionTxt.setText(courseItem.getDescription());

		List<LessonCourseItem.LessonListItem> lessons = courseItem.getLessons();
		lessonsItemsAdapter.setItemsList(lessons);

		need2update = false;
	}

	private class CourseSaveListener extends ChessUpdateListener<LessonCourseItem.Data> {

	}

}
