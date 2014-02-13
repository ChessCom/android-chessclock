package com.chess.ui.fragments.lessons;

import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.entity.api.LessonSingleItem;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.ui.adapters.LessonsItemsAdapter;
import com.chess.ui.adapters.LessonsItemsAdapterTablet;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 09.11.13
 * Time: 6:20
 */
public class LessonsCourseFragmentTablet extends LessonsCourseFragment {

	private LessonsItemsAdapterTablet lessonsItemAdapter;

	public LessonsCourseFragmentTablet() {
	}

	public static LessonsCourseFragmentTablet createInstance(int courseId, int categoryId) {
		LessonsCourseFragmentTablet fragment = new LessonsCourseFragmentTablet();
		Bundle bundle = new Bundle();
		bundle.putInt(COURSE_ID, courseId);
		bundle.putInt(CATEGORY_ID, categoryId);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		lessonsItemAdapter = new LessonsItemsAdapterTablet(getActivity(), null);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		int lessonId = ((LessonSingleItem) parent.getItemAtPosition(position)).getId();

		if (isNetworkAvailable()) {
			getActivityFace().openFragment(GameLessonsFragmentTablet.createInstance(lessonId, courseId));
		} else { // else check if we have saved lesson with that id
			Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getMentorLessonById(lessonId));
			if (cursor != null && cursor.moveToFirst()) {
				getActivityFace().openFragment(GameLessonsFragmentTablet.createInstance(lessonId, courseId));
			} else {
				showToast(R.string.no_network);
			}
		}
	}

	@Override
	protected void init() {
		setAdapter(new LessonsItemsAdapterTablet(getActivity(), null));
		courseUpdateListener = new CourseUpdateListener();
		courseSaveListener = new SaveCourseListener();
	}

	@Override
	protected void widgetsInit(View view) {
		GridView gridView = (GridView) view.findViewById(R.id.listView);
		if (isNeedToUpgrade()) {
			upgradeLessonsBtn = (Button) view.findViewById(R.id.upgradeLessonsBtn);
			upgradeLessonsBtn.setOnClickListener(this);
		}

		courseTitleTxt = (TextView) view.findViewById(R.id.courseTitleTxt);
		courseDescriptionTxt = (TextView) view.findViewById(R.id.courseDescriptionTxt);
		gridView.setAdapter(lessonsItemAdapter);
		gridView.setOnItemClickListener(this);
	}

	@Override
	protected void setAdapter(LessonsItemsAdapter lessonsItemsAdapter) {
		this.lessonsItemAdapter = (LessonsItemsAdapterTablet) lessonsItemsAdapter;
	}

	@Override
	protected LessonsItemsAdapter getAdapter() {
		return lessonsItemAdapter;
	}
}
