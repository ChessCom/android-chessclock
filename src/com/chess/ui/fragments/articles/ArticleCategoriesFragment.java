package com.chess.ui.fragments.articles;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.chess.R;
import com.chess.backend.statics.StaticData;
import com.chess.db.DBConstants;
import com.chess.db.DBDataManager;
import com.chess.db.DbHelper;
import com.chess.db.tasks.LoadDataFromDbTask;
import com.chess.ui.adapters.ArticlesThumbCursorAdapter;
import com.chess.ui.adapters.DarkSpinnerAdapter;
import com.chess.ui.fragments.CommonLogicFragment;
import com.chess.ui.interfaces.ItemClickListenerFace;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: roger sent2roger@gmail.com
 * Date: 30.01.13
 * Time: 6:56
 */
public class ArticleCategoriesFragment extends CommonLogicFragment implements ItemClickListenerFace, AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

	public static final String SECTION_NAME = "section_name";

	private ArticlesThumbCursorAdapter articlesAdapter;

	private Spinner categorySpinner;
	private View loadingView;
	private TextView emptyView;
	private ListView listView;
	private ArticlesCursorUpdateListener articlesCursorUpdateListener;
	private boolean categoriesLoaded;

	public static ArticleCategoriesFragment createInstance(String sectionName) {
		ArticleCategoriesFragment frag = new ArticleCategoriesFragment();
		Bundle bundle = new Bundle();
		bundle.putString(SECTION_NAME, sectionName);
		frag.setArguments(bundle);
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		articlesAdapter = new ArticlesThumbCursorAdapter(getActivity(), null);
		articlesCursorUpdateListener = new ArticlesCursorUpdateListener();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.new_common_categories_frame, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		setTitle(R.string.articles);

		loadingView = view.findViewById(R.id.loadingView);
		emptyView = (TextView) view.findViewById(R.id.emptyView);

		categorySpinner = (Spinner) view.findViewById(R.id.categoriesSpinner);

		listView = (ListView) view.findViewById(R.id.listView);
		listView.setAdapter(articlesAdapter);
		listView.setOnItemClickListener(this);

		getActivityFace().showActionMenu(R.id.menu_search, true);
		getActivityFace().showActionMenu(R.id.menu_notifications, false);
		getActivityFace().showActionMenu(R.id.menu_games, false);

		setTitlePadding(ONE_ICON);
	}

	@Override
	public void onStart() {
		super.onStart();

		if (!categoriesLoaded) {
			// get list of categories
			categoriesLoaded = fillCategories();
		}

		if (!categoriesLoaded) { // load hardcoded categories with passed arg

		}
	}

	private boolean fillCategories() {
		Cursor cursor = getContentResolver().query(DBConstants.uriArray[DBConstants.Tables.ARTICLE_CATEGORIES.ordinal()], null, null, null, null);
		List<String> list = new ArrayList<String>();
		if (!cursor.moveToFirst()) {
			showToast("Categories are not loaded");
			return false;
		}

		do {
			list.add(DBDataManager.getString(cursor, DBConstants.V_NAME));
		} while (cursor.moveToNext());

		// get passed argument
		String selectedCategory = getArguments().getString(SECTION_NAME);

		int sectionId;
		for (sectionId = 0; sectionId < list.size(); sectionId++) {
			String category = list.get(sectionId);
			if (category.equals(selectedCategory)) {
				break;
			}
		}

		categorySpinner.setAdapter(new DarkSpinnerAdapter(getActivity(), list));
		categorySpinner.setOnItemSelectedListener(this);
		categorySpinner.setSelection(sectionId);  // TODO remember last selection.
		return true;
	}

	private void loadFromDb() {
		String category = (String) categorySpinner.getSelectedItem();

		new LoadDataFromDbTask(articlesCursorUpdateListener,
				DbHelper.getArticlesListByCategoryParams(category),
				getContentResolver()).executeTask();
	}

	private class ArticlesCursorUpdateListener extends ChessUpdateListener<Cursor> {

		@Override
		public void showProgress(boolean show) {
			showLoadingView(show);
		}

		@Override
		public void updateData(Cursor returnedObj) {
			super.updateData(returnedObj);

			articlesAdapter.changeCursor(returnedObj);
		}

		@Override
		public void errorHandle(Integer resultCode) {
			super.errorHandle(resultCode);
			if (resultCode == StaticData.UNKNOWN_ERROR) {
				emptyView.setText(R.string.no_data);
			}
			showEmptyView(true);
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Cursor cursor = (Cursor) parent.getItemAtPosition(position);
		getActivityFace().openFragment(ArticleDetailsFragment.createInstance(DBDataManager.getId(cursor)));
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		loadFromDb();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}


	private void showEmptyView(boolean show) {
		if (show) {
			// don't hide loadingView if it's loading
			if (loadingView.getVisibility() != View.VISIBLE) {
				loadingView.setVisibility(View.GONE);
			}

			emptyView.setVisibility(View.VISIBLE);
			listView.setVisibility(View.GONE);
		} else {
			emptyView.setVisibility(View.GONE);
			listView.setVisibility(View.VISIBLE);
		}
	}

	private void showLoadingView(boolean show) {
		if (show) {
			emptyView.setVisibility(View.GONE);
			if (articlesAdapter.getCount() == 0) {
				listView.setVisibility(View.GONE);

			}
			loadingView.setVisibility(View.VISIBLE);
		} else {
			listView.setVisibility(View.VISIBLE);
			loadingView.setVisibility(View.GONE);
		}
	}

	@Override
	public Context getMeContext() {
		return getActivity();
	}

}
