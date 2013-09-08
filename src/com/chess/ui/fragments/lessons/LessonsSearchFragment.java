package com.chess.ui.fragments.lessons;

import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import com.chess.MultiDirectionSlidingDrawer;
import com.chess.R;
import com.chess.backend.RestHelper;
import com.chess.backend.LoadItem;
import com.chess.backend.entity.api.LessonListItem;
import com.chess.backend.entity.api.LessonSearchItem;
import com.chess.backend.statics.StaticData;
import com.chess.backend.tasks.RequestJsonTask;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.ui.adapters.LessonsItemAdapter;
import com.chess.ui.adapters.StringSpinnerAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.utilities.AppUtils;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 06.08.13
 * Time: 14:50
 */
public class LessonsSearchFragment extends CommonLogicFragment implements AdapterView.OnItemClickListener, MultiDirectionSlidingDrawer.OnDrawerOpenListener, MultiDirectionSlidingDrawer.OnDrawerCloseListener {

	private static final int FADE_ANIM_DURATION = 300;

	private EditText keywordsEdt;
	private Spinner difficultySpinner;
	private Spinner categorySpinner;
	private String allStr;
	private LessonItemUpdateListener lessonItemUpdateListener;
	private LessonsItemAdapter lessonsItemsAdapter;
	private MultiDirectionSlidingDrawer slidingDrawer;
	private ObjectAnimator fadeDrawerAnimator;
	private ObjectAnimator fadeSearchAnimator;
	private String lastKeyword;
	private String lastCategory;
	private String lastDifficulty;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		lessonItemUpdateListener = new LessonItemUpdateListener();
		lessonsItemsAdapter = new LessonsItemAdapter(getActivity(), null);

		lastKeyword = StaticData.SYMBOL_EMPTY;
		lastCategory = StaticData.SYMBOL_EMPTY;
		lastDifficulty = StaticData.SYMBOL_EMPTY;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_lessons_search_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.lessons);

		keywordsEdt = (EditText) view.findViewById(R.id.keywordsEdt);
		allStr = getString(R.string.all);

		difficultySpinner = (Spinner) view.findViewById(R.id.difficultySpinner);
		String[] difficultyArray = getResources().getStringArray(R.array.lesson_difficulty);
		List<String> difficultyList = AppUtils.convertArrayToList(difficultyArray);
		difficultyList.add(0, allStr);

		difficultySpinner.setAdapter(new StringSpinnerAdapter(getActivity(), difficultyList));
		categorySpinner = (Spinner) view.findViewById(R.id.categorySpinner);

		view.findViewById(R.id.searchBtn).setOnClickListener(this);

		slidingDrawer = (MultiDirectionSlidingDrawer) view.findViewById(R.id.slidingDrawer);
		slidingDrawer.setOnDrawerOpenListener(this);
		slidingDrawer.setOnDrawerCloseListener(this);
		fadeDrawerAnimator = ObjectAnimator.ofFloat(slidingDrawer, "alpha", 1, 0);
		fadeDrawerAnimator.setDuration(FADE_ANIM_DURATION);
		slidingDrawer.setVisibility(View.GONE);
		fadeDrawerAnimator.start();

		View searchFieldsView = view.findViewById(R.id.searchFieldsView);
		fadeSearchAnimator = ObjectAnimator.ofFloat(searchFieldsView, "alpha", 1, 0);
		fadeSearchAnimator.setDuration(FADE_ANIM_DURATION);

		ListView listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(lessonsItemsAdapter);
		listView.setOnItemClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		// get saved categories
		Cursor cursor = getContentResolver().query(DbScheme.uriArray[DbScheme.Tables.LESSONS_CATEGORIES.ordinal()], null, null, null, null);

		if (cursor != null && cursor.moveToFirst()) {
			fillCategoriesList(cursor);
		}
	}

	private void fillCategoriesList(Cursor cursor) {
		List<String> categories = new ArrayList<String>();
		SparseArray<String> categoriesArray = new SparseArray<String>();
		categories.add(allStr);

		do {
			int id = DbDataManager.getInt(cursor, DbScheme.V_CATEGORY_ID);
			String name = DbDataManager.getString(cursor, DbScheme.V_NAME);
			categoriesArray.put(id, name);
			categories.add(name);
		} while (cursor.moveToNext());

		categorySpinner.setAdapter(new StringSpinnerAdapter(getActivity(), categories));
	}

	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.searchBtn) {
			String keyword = getTextFromField(keywordsEdt);
			String category = (String) categorySpinner.getSelectedItem();
			String difficulty = (String) difficultySpinner.getSelectedItem();

			// Check if search query has changed, to reduce load
			if (lastKeyword.equals(keyword) && lastCategory.equals(category) && lastDifficulty.equals(difficulty)) {
				showSearchResults();
				return;
			}

			lastKeyword = keyword;
			lastCategory = category;
			lastDifficulty = difficulty;

			LoadItem loadItem = new LoadItem();
			loadItem.setLoadPath(RestHelper.getInstance().CMD_LESSONS);
			loadItem.addRequestParams(RestHelper.P_LOGIN_TOKEN, getUserToken());
			loadItem.addRequestParams(RestHelper.P_KEYWORD, keyword);
			if (!category.equals(allStr)) {
				loadItem.addRequestParams(RestHelper.P_CATEGORY_CODE, category);
			}
			if (!category.equals(difficulty)) {
				loadItem.addRequestParams(RestHelper.P_DIFFICULTY, difficulty);
			}

			new RequestJsonTask<LessonSearchItem>(lessonItemUpdateListener).executeTask(loadItem);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		LessonListItem lessonItem = (LessonListItem) parent.getItemAtPosition(position);
		long lessonId = lessonItem.getId();
		getActivityFace().openFragment(GameLessonFragment.createInstance((int) lessonId, 0)); // we don't know courseId here
	}

	@Override
	public void onDrawerOpened() {

	}

	@Override
	public void onDrawerClosed() {
		slidingDrawer.setVisibility(View.GONE);
		fadeSearchAnimator.reverse();
		fadeDrawerAnimator.start();
	}

	private class LessonItemUpdateListener extends ChessLoadUpdateListener<LessonSearchItem> {

		private LessonItemUpdateListener() {
			super(LessonSearchItem.class);
		}

		@Override
		public void updateData(LessonSearchItem returnedObj) {
			super.updateData(returnedObj);


			List<LessonListItem> lessons = returnedObj.getData().getLessons();
			lessonsItemsAdapter.setItemsList(lessons);

			showSearchResults();
		}
	}

	private void showSearchResults() {
		slidingDrawer.setVisibility(View.VISIBLE);
		fadeDrawerAnimator.reverse();
		fadeDrawerAnimator.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animator) {}

			@Override
			public void onAnimationEnd(Animator animator) {
				if (!slidingDrawer.isOpened()) {
					slidingDrawer.animateOpen();
				}
			}

			@Override
			public void onAnimationCancel(Animator animator) { }

			@Override
			public void onAnimationRepeat(Animator animator) { }
		});
		fadeSearchAnimator.start();
	}

}
