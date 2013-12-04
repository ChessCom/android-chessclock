package com.chess.ui.fragments.lessons;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.chess.R;
import com.chess.backend.LoadHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.RestHelper;
import com.chess.backend.entity.api.CommonFeedCategoryItem;
import com.chess.backend.entity.api.LessonCourseListItem;
import com.chess.backend.entity.api.LessonSingleItem;
import com.chess.backend.entity.api.LessonsRatingItem;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbHelper;
import com.chess.db.DbScheme;
import com.chess.db.tasks.SaveLessonsCategoriesTask;
import com.chess.ui.adapters.CommonCategoriesCursorAdapter;
import com.chess.ui.fragments.BasePopupsFragment;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.fragments.upgrade.UpgradeFragment;
import com.chess.ui.fragments.upgrade.UpgradeFragmentTablet;
import com.chess.ui.interfaces.FragmentParentFace;
import com.chess.utilities.AppUtils;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 08.11.13
 * Time: 5:24
 */
public class LessonsFragmentTablet extends CommonLogicFragment implements AdapterView.OnItemClickListener, FragmentParentFace {

	private ListView listView;
	private View loadingView;
	private TextView emptyView;

	private CommonCategoriesCursorAdapter categoriesCursorAdapter;

	private LessonsCategoriesUpdateListener lessonsCategoriesUpdateListener;
	private SaveLessonsCategoriesUpdateListener saveLessonsCategoriesUpdateListener;
	private LessonsCoursesUpdateListener lessonsCoursesUpdateListener;

	private LessonsRatingUpdateListener lessonsRatingUpdateListener;
	private TextView ratingTxt;
	private TextView lessonsCntTxt;
	private TextView coursesCntTxt;
	private LessonSingleItem incompleteLesson;
	private Button resumeLessonBtn;
	private boolean noCategoriesFragmentsAdded;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		init();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_common_tablet_content_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		{ // Library mode init
			listView = (ListView) view.findViewById(R.id.listView);
			View headerView;
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			if (isNeedToUpgradePremium()) {
				headerView = inflater.inflate(R.layout.new_lessons_upgrade_view, null, false);
				headerView.findViewById(R.id.upgradeBtn).setOnClickListener(this);
			} else {
				headerView = inflater.inflate(R.layout.new_lesson_stats_view, null, false);
				ratingTxt = (TextView) headerView.findViewById(R.id.lessonsRatingTxt);
				lessonsCntTxt = (TextView) headerView.findViewById(R.id.lessonsCompletedValueTxt);
				coursesCntTxt = (TextView) headerView.findViewById(R.id.coursesCompletedValueTxt);

				ratingTxt.setText(String.valueOf(getAppData().getUserLessonsRating()));
				lessonsCntTxt.setText(String.valueOf(getAppData().getUserLessonsCompleteCnt()));
				coursesCntTxt.setText(String.valueOf(getAppData().getUserCourseCompleteCnt()));
			}

			listView.addHeaderView(headerView);
			listView.setAdapter(categoriesCursorAdapter);
			listView.setOnItemClickListener(this);
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
		if (need2update) {

			// check if we have saved categories
			// get saved categories
			Cursor categoriesCursor = DbDataManager.query(getContentResolver(), DbHelper.getLessonsCurriculumCategories());

			if (categoriesCursor == null || !categoriesCursor.moveToFirst() && AppUtils.isNetworkAvailable(getActivity())) {
				getCategories();
			}

			// get courses
			Cursor coursesCursor = DbDataManager.query(getContentResolver(), DbHelper.getLessonCoursesForUser(getUsername()));

			if (coursesCursor != null && coursesCursor.moveToFirst()) {
				updateUiData();
			} else {
				getFullCourses();
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

		if (noCategoriesFragmentsAdded) {
			openInternalFragment(LessonsCategoriesFragmentTablet.createInstance(sectionName));
			noCategoriesFragmentsAdded = false;
		} else {
			changeInternalFragment(LessonsCategoriesFragmentTablet.createInstance(sectionName));
		}
	}

	@Override
	public void onClick(View v) {
		super.onClick(v);
		if (v.getId() == R.id.resumeLessonBtn) {
			int lessonId = incompleteLesson.getId();
			long courseId = incompleteLesson.getCourseId();
			getActivityFace().openFragment(GameLessonsFragmentTablet.createInstance(lessonId, courseId));
		} else if (v.getId() == R.id.upgradeBtn) {
			if (!isTablet) {
				getActivityFace().openFragment(new UpgradeFragment());
			} else {
				getActivityFace().openFragment(new UpgradeFragmentTablet());
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_search_btn:
				getActivityFace().changeRightFragment(new LessonsSearchFragment());
				getActivityFace().toggleRightMenu();
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

			updateUiData();
			need2update = false;
		}
	}

	private void updateUiData() {

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
		categoriesCursorAdapter = new CommonCategoriesCursorAdapter(getActivity(), null);
		categoriesCursorAdapter.setLayoutId(R.layout.new_common_titled_list_item_thin_white);

		lessonsCategoriesUpdateListener = new LessonsCategoriesUpdateListener();
		saveLessonsCategoriesUpdateListener = new SaveLessonsCategoriesUpdateListener();
		lessonsCoursesUpdateListener = new LessonsCoursesUpdateListener();
		lessonsRatingUpdateListener = new LessonsRatingUpdateListener();

		// get from DB categories for Full Lessons Library(not Curriculum)
		Cursor cursor = DbDataManager.query(getContentResolver(), DbHelper.getLessonsLibraryCategories());
		categoriesCursorAdapter.changeCursor(cursor);

		changeInternalFragment(LessonsCurriculumFragmentTablet.createInstance(this));

		noCategoriesFragmentsAdded = true;
	}

	@Override
	public void changeFragment(BasePopupsFragment fragment) {
		openInternalFragment(fragment);
	}

	private void changeInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.innerFragmentContainer, fragment);
		transaction.commitAllowingStateLoss();
	}

	private void openInternalFragment(Fragment fragment) {
		FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
		transaction.replace(R.id.innerFragmentContainer, fragment, fragment.getClass().getSimpleName());
		transaction.addToBackStack(fragment.getClass().getSimpleName());
		transaction.commitAllowingStateLoss();
	}

	@Override
	public boolean showPreviousFragment() {
		if (getActivity() == null) {
			return false;
		}
		int entryCount = getChildFragmentManager().getBackStackEntryCount();
		if (entryCount > 0) {
			int last = entryCount - 1;
			FragmentManager.BackStackEntry stackEntry = getChildFragmentManager().getBackStackEntryAt(last);
			if (stackEntry != null && stackEntry.getName().equals(LessonsCategoriesFragmentTablet.class.getSimpleName())) {
				noCategoriesFragmentsAdded = true;
			}

			return getChildFragmentManager().popBackStackImmediate();
		} else {
			return super.showPreviousFragment();
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
