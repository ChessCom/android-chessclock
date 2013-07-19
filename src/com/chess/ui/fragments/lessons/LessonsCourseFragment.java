package com.chess.ui.fragments.lessons;

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
import com.chess.ui.adapters.LessonsItemAdapter;
import com.chess.ui.fragments.CommonLogicFragment;

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
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_lessons_course_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.lessons);

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

		LoadItem loadItem = LoadHelper.getLessonsByCourseId(getUserToken(), courseId);

		new RequestJsonTask<LessonCourseItem>(new CourseUpdateListener()).executeTask(loadItem);
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

	private class CourseUpdateListener extends ChessLoadUpdateListener<LessonCourseItem> {

		private CourseUpdateListener() {
			super(LessonCourseItem.class);
		}

		@Override
		public void updateData(LessonCourseItem returnedObj) {
			super.updateData(returnedObj);

			LessonCourseItem.Data courseItem = returnedObj.getData();

			courseTitleTxt.setText(courseItem.getCourseName());
			courseDescriptionTxt.setText(courseItem.getDescription());

			List<LessonCourseItem.LessonListItem> lessons = courseItem.getLessons();
			lessonsItemsAdapter.setItemsList(lessons);
		}
	}

}
