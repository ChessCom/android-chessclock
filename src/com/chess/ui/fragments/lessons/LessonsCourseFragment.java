package com.chess.ui.fragments.lessons;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.chess.R;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.ui.adapters.LessonCoursesAdapter;
import com.chess.ui.fragments.CommonLogicFragment;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 18.07.13
 * Time: 21:36
 */
public class LessonsCourseFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener {

	private static final String COURSE_ID = "course_id";

	private LessonCoursesAdapter coursesCursorAdapter;
	private int courseId;


	public LessonsCourseFragment() {

	}

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

		coursesCursorAdapter = new LessonCoursesAdapter(getActivity(), null);

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
		listView.addHeaderView(headerView);

		listView.setAdapter(coursesCursorAdapter);
		listView.setOnItemClickListener(this);

		// adjust action bar icons
		getActivityFace().showActionMenu(R.id.menu_search, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

//		if (getArguments() != null) {
//			courseId = getArguments().getInt(COURSE_ID);
//		} else {
//			courseId = savedInstanceState.getInt(COURSE_ID);
//		}
	}

	@Override
	public void onStart() {
		super.onStart();

		Cursor cursor = DBDataManager.executeQuery(getContentResolver(), DbHelper.getLessonCourseById(courseId));
		if (cursor.moveToFirst()) {
			coursesCursorAdapter.changeCursor(cursor);
		}

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(COURSE_ID, courseId);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

	}

}
