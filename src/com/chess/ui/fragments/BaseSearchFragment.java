package com.chess.ui.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.db.DbDataManager;
import com.chess.db.DbScheme;
import com.chess.db.QueryParams;
import com.chess.statics.Symbol;
import com.chess.ui.adapters.StringSpinnerAdapter;
import com.chess.widgets.MultiDirectionSlidingDrawer;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 11.09.13
 * Time: 20:20
 */
public abstract class BaseSearchFragment extends CommonLogicFragment implements MultiDirectionSlidingDrawer.OnDrawerOpenListener, MultiDirectionSlidingDrawer.OnDrawerCloseListener, AdapterView.OnItemClickListener {

	private static final int FADE_ANIM_DURATION = 100;

	protected EditText keywordsEdt;
	protected Spinner categorySpinner;

	private MultiDirectionSlidingDrawer slidingDrawer;
	private ObjectAnimator fadeDrawerAnimator;
	private ObjectAnimator fadeSearchAnimator;
	protected String lastKeyword;
	protected String lastCategory;
	protected SparseArray<String> categoriesArray;
	protected boolean resultsFound;
	protected List<String> categories;
	private StringSpinnerAdapter spinnerAdapter;
	protected ListView listView;
	protected String allStr;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		allStr = getString(R.string.all);

		categories = new ArrayList<String>();
		categoriesArray = new SparseArray<String>();
		categories.add(getString(R.string.all));
		spinnerAdapter = new StringSpinnerAdapter(getActivity(), categories);

		lastKeyword = Symbol.EMPTY;
		lastCategory = Symbol.EMPTY;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.base_search_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		widgetsInit(view);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (need2update) {
			// get saved categories

			Cursor cursor = DbDataManager.query(getContentResolver(), getQueryParams());
			if (cursor != null && cursor.moveToFirst()) {
				fillCategoriesList(cursor);
			} else {
				getCategories();
			}
		} else {
			spinnerAdapter.setItemsList(categories);

			if (resultsFound) {
				showSearchResults();
			}
		}
	}

	protected abstract QueryParams getQueryParams();

	protected abstract void getCategories();

	protected abstract ListAdapter getAdapter();

	protected void fillCategoriesList(Cursor cursor) {
		do {
			int id = DbDataManager.getInt(cursor, DbScheme.V_CATEGORY_ID);
			String name = DbDataManager.getString(cursor, DbScheme.V_NAME);
			categoriesArray.put(id, name);
			categories.add(name);
		} while (cursor.moveToNext());
		cursor.close();

		spinnerAdapter.setItemsList(categories);
	}


	@Override
	public void onClick(View view) {
		super.onClick(view);

		if (view.getId() == R.id.searchBtn) {
			String keyword = getTextFromField(keywordsEdt);
			String category = (String) categorySpinner.getSelectedItem();
			int categoryId = -1;
			for (int i = 0; i < categoriesArray.size(); i++) {
				String categoryByIndex = categoriesArray.valueAt(i);
				if (categoryByIndex.equals(category)) {
					categoryId = categoriesArray.keyAt(i);
				}
			}

			// Check if search query has changed, to reduce load
			if (lastKeyword.equals(keyword) && lastCategory.equals(category) && resultsFound) {
				showSearchResults();
				return;
			}

			lastKeyword = keyword;
			lastCategory = category;

			resultsFound = false;

			startSearch(keyword, categoryId);

			hideKeyBoard(keywordsEdt);
		}
	}

	protected abstract void startSearch(String keyword, int categoryId);

	@Override
	public void onDrawerOpened() {
	}

	@Override
	public void onDrawerClosed() {
		slidingDrawer.setVisibility(View.GONE);
		fadeSearchAnimator.reverse();
		fadeDrawerAnimator.start();
	}

	protected void showSearchResults() {
		if (slidingDrawer.getVisibility() != View.VISIBLE) {
			slidingDrawer.setVisibility(View.VISIBLE);
			fadeDrawerAnimator.reverse();
			fadeDrawerAnimator.addListener(new Animator.AnimatorListener() {
				@Override
				public void onAnimationStart(Animator animator) {
				}

				@Override
				public void onAnimationEnd(Animator animator) {
					if (!slidingDrawer.isOpened()) {
						slidingDrawer.animateOpen();
					}
				}

				@Override
				public void onAnimationCancel(Animator animator) {
				}

				@Override
				public void onAnimationRepeat(Animator animator) {
				}
			});
			fadeSearchAnimator.start();
		}
	}

	@Override
	public boolean showPreviousFragment() {
		if (slidingDrawer.isOpened() && slidingDrawer.getVisibility() == View.VISIBLE) {
			slidingDrawer.close();
			return true;
		}

		return super.showPreviousFragment();
	}

	private void widgetsInit(View view) {
		keywordsEdt = (EditText) view.findViewById(R.id.keywordsEdt);

		categorySpinner = (Spinner) view.findViewById(R.id.categorySpinner);
		categorySpinner.setAdapter(spinnerAdapter);

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

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(getAdapter());
		listView.setOnItemClickListener(this);

		initUpgradeAndAdWidgets(view);
	}
}
